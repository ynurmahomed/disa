package org.openmrs.module.disa.web.delegate;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.extension.util.RestUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ViralLoadResultsDelegate {

	private RestUtil rest;

	public ViralLoadResultsDelegate() {

		rest = new RestUtil();
		rest.setURLBase(
				Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_URL).getPropertyValue());
		rest.setUsername(
				Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_USERNAME).getPropertyValue());
		rest.setPassword(
				Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_PASSWORD).getPropertyValue());
	}

	public List<Disa> getViralLoadDataList(Date startDate, Date endDate, String vlState) throws Exception {

		List<String> sismaCodes = Arrays.asList(Context.getAdministrationService()
				.getGlobalPropertyObject(Constants.DISA_SISMA_CODE).getPropertyValue().split(","));

		String jsonViralLoadInfo = rest.getRequestGetFsrByStatusAndDates("/viral-status-dates", sismaCodes, vlState,
				formatDate(startDate, 1), formatDate(endDate, 2));
		return new Gson().fromJson(jsonViralLoadInfo, new TypeToken<ArrayList<Disa>>() {
		}.getType());

	}

	public List<Patient> getPatients(Disa selectedPatient) {
		return Context.getPatientService()
				.getPatients(selectedPatient.getFirstName() + " " + selectedPatient.getLastName(), null, null, Boolean.FALSE);
	}

	public void addPatientToList(List<Patient> patients, Patient patient) {

		Patient patientToAdd = Context.getPatientService().getPatient(patient.getId());
		if (!patients.contains(patientToAdd)) {
			// TODO This is a workaround to LazyInitialization error when getting
			// identifiers from patient on jsp
			Set<PatientIdentifier> identifiers = new TreeSet<PatientIdentifier>();
			identifiers.add(patientToAdd.getPatientIdentifier());
			patientToAdd.setIdentifiers(identifiers);
			patients.add(patientToAdd);
		}
	}

	public void doMapIdentifier(String patientUuid, String nidDisa) {
		Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(Constants.DISA_NID);
		List<PatientIdentifierType> patientIdentifierTypes = new ArrayList<PatientIdentifierType>();
		patientIdentifierTypes.add(identifierType);

		List<PatientIdentifier> patIdentidier = Context.getPatientService().getPatientIdentifiers(nidDisa, patientIdentifierTypes, null, null, null);
		if (patIdentidier.isEmpty()) {
			patientIdentifier.setPatient(patient);
			patientIdentifier.setIdentifier(nidDisa);
			patientIdentifier.setIdentifierType(identifierType);
			patientIdentifier.setLocation(Context.getLocationService().getDefaultLocation());
			Context.getPatientService().savePatientIdentifier(patientIdentifier);

			try {
				rest.getRequestPutPending("/pending", new ArrayList<String>(Arrays.asList(nidDisa)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void createExcelFile(List<Disa> listDisa, HttpServletResponse response,
			MessageSourceService messageSourceService) throws Exception {
		Locale locale = Context.getLocale();
		try (ByteArrayOutputStream outByteStream = new ByteArrayOutputStream()) {
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet("ViralLoadData");
			sheet.setColumnWidth(0, 8000);
			sheet.setColumnWidth(1, 8000);
			sheet.setColumnWidth(2, 8000);
			sheet.setColumnWidth(3, 8000);
			sheet.setColumnWidth(4, 8000);
			sheet.setColumnWidth(5, 8000);
			sheet.setColumnWidth(6, 8000);
			sheet.setColumnWidth(7, 8000);
			sheet.setColumnWidth(8, 8000);
			createHeaderRow(sheet, locale, messageSourceService);

			int rowCount = 0;

			for (Disa disa : listDisa) {
				Row row = sheet.createRow(++rowCount);
				writeDisaList(disa, row, locale, messageSourceService);
			}

			workbook.write(outByteStream);
			byte[] outArray = outByteStream.toByteArray();
			response.setContentType("application/ms-excel");
			response.setContentLength(outArray.length);
			response.setHeader("Expires:", "0");
			response.setHeader("Content-Disposition", "attachment; filename=Viral Load Data Details.xls");
			OutputStream outStream = response.getOutputStream();
			outStream.write(outArray);
			outStream.flush();
			workbook.close();
		}
	}

	private void writeDisaList(Disa disa, Row row, Locale locale, MessageSourceService messageSourceService) {
		Cell cell = row.createCell(0);
		cell.setCellValue(disa.getNid());

		cell = row.createCell(1);
		cell.setCellValue(disa.getFirstName() + " " + disa.getLastName());

		cell = row.createCell(2);
		cell.setCellValue(disa.getGender());

		cell = row.createCell(3);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		cell.setCellValue(disa.getAge());

		cell = row.createCell(4);
		cell.setCellValue(disa.getRequestId());

		cell = row.createCell(5);
		cell.setCellValue(disa.getViralLoadResultCopies());

		cell = row.createCell(6);
		cell.setCellValue(disa.getViralLoadResultLog());

		cell = row.createCell(7);
		cell.setCellValue(disa.getHivViralLoadResult());

		cell = row.createCell(8);
		cell.setCellValue(
				messageSourceService.getMessage("disa.viral.load.status." + disa.getViralLoadStatus(), null, locale));

		if (disa.getViralLoadStatus().equals("NOT_PROCESSED")) {
			cell = row.createCell(9);
			String notProcessingCause = disa.getNotProcessingCause();
			if (notProcessingCause == null) {
				cell.setCellValue(notProcessingCause);
			} else {
				cell.setCellValue(messageSourceService.getMessage("disa." + notProcessingCause, null, locale));
			}
		}
	}

	private void createHeaderRow(Sheet sheet, Locale locale, MessageSourceService messageSourceService) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
		Font font = sheet.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 12);
		cellStyle.setFont(font);
		cellStyle.setWrapText(true);

		Row row = sheet.createRow(0);

		Cell cellNid = row.createCell(0);
		cellNid.setCellStyle(cellStyle);
		cellNid.setCellValue(messageSourceService.getMessage("disa.nid", null, locale));

		Cell cellNome = row.createCell(1);
		cellNome.setCellStyle(cellStyle);
		cellNome.setCellValue(messageSourceService.getMessage("general.name", null, locale));

		Cell cellSexo = row.createCell(2);
		cellSexo.setCellStyle(cellStyle);
		cellSexo.setCellValue(messageSourceService.getMessage("disa.gender", null, locale));

		Cell cellIdade = row.createCell(3);
		cellIdade.setCellStyle(cellStyle);
		cellIdade.setCellValue(messageSourceService.getMessage("disa.age", null, locale));

		Cell cellIDRequisacao = row.createCell(4);
		cellIDRequisacao.setCellStyle(cellStyle);
		cellIDRequisacao.setCellValue(messageSourceService.getMessage("disa.request.id", null, locale));

		Cell cellCargaViralCopies = row.createCell(5);
		cellCargaViralCopies.setCellStyle(cellStyle);
		cellCargaViralCopies.setCellValue(messageSourceService.getMessage("disa.viralload.result.copy", null, locale));

		Cell cellCargaViralLog = row.createCell(6);
		cellCargaViralLog.setCellStyle(cellStyle);
		cellCargaViralLog.setCellValue(messageSourceService.getMessage("disa.viralload.result.log", null, locale));

		Cell cellCargaViralCoded = row.createCell(7);
		cellCargaViralCoded.setCellStyle(cellStyle);
		cellCargaViralCoded.setCellValue(messageSourceService.getMessage("disa.viralload.result.coded", null, locale));

		Cell cellCargaViralStatus = row.createCell(8);
		cellCargaViralStatus.setCellStyle(cellStyle);
		cellCargaViralStatus.setCellValue("Status");

		Cell causaNaoProcessamento = row.createCell(9);
		causaNaoProcessamento.setCellStyle(cellStyle);
		causaNaoProcessamento.setCellValue(messageSourceService.getMessage("disa.not.processing.cause", null, locale));
	}

	private String formatDate(Date date, int i) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return dateFormat.format(date).replace("12:00:00", i == 1 ? "00:00:00" : "23:59:59");
	}
}
