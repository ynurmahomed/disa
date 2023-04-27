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

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.HIVVLLabResult;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.module.disa.extension.util.RestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ViralLoadResultsDelegate {

	private static final Logger log = LoggerFactory.getLogger(RestUtil.class);

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

	public List<LabResult> getViralLoadDataList(Date startDate, Date endDate, String vlState) throws Exception {

		List<String> sismaCodes = Arrays.asList(Context.getAdministrationService()
				.getGlobalPropertyObject(Constants.DISA_SISMA_CODE).getPropertyValue().split(","));

		String jsonViralLoadInfo = rest.getRequestGetFsrByStatusAndDates("/viral-status-dates", sismaCodes, vlState,
				formatDate(startDate, 1), formatDate(endDate, 2));
		return new Gson().fromJson(jsonViralLoadInfo, new TypeToken<ArrayList<LabResult>>() {
		}.getType());

	}

	public List<LabResult> getViralLoadDataList(String requestId, String nid,
			String vlState, String notProcessingCause, Date startDate, Date endDate, List<String> healthFacCodes)
			throws Exception {

		String jsonViralLoadInfo = rest.getRequestByForm("/search-form", requestId, nid, vlState, notProcessingCause,
				formatDate(startDate, 1), formatDate(endDate, 2), healthFacCodes);
		return new Gson().fromJson(jsonViralLoadInfo, new TypeToken<ArrayList<LabResult>>() {
		}.getType());
	}

	public void createExcelFileStaging(List<LabResult> listDisa, HttpServletResponse response,
			MessageSourceService messageSourceService) throws Exception {
		Locale locale = Context.getLocale();
		try (ByteArrayOutputStream outByteStream = new ByteArrayOutputStream()) {
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet("ViralLoadData Staging Server");
			sheet.setColumnWidth(0, 8000);
			sheet.setColumnWidth(1, 8000);
			sheet.setColumnWidth(2, 8000);
			sheet.setColumnWidth(3, 8000);
			sheet.setColumnWidth(4, 8000);
			sheet.setColumnWidth(5, 8000);
			sheet.setColumnWidth(6, 8000);
			sheet.setColumnWidth(7, 8000);
			sheet.setColumnWidth(8, 8000);
			sheet.setColumnWidth(9, 8000);
			sheet.setColumnWidth(10, 8000);
			sheet.setColumnWidth(11, 8000);
			sheet.setColumnWidth(12, 8000);
			sheet.setColumnWidth(13, 8000);
			sheet.setColumnWidth(14, 8000);
			sheet.setColumnWidth(15, 8000);
			sheet.setColumnWidth(16, 8000);
			sheet.setColumnWidth(17, 8000);
			sheet.setColumnWidth(18, 8000);
			sheet.setColumnWidth(19, 8000);
			createHeaderRowStaging(sheet, locale, messageSourceService);

			int rowCount = 0;

			for (LabResult disa : listDisa) {
				Row row = sheet.createRow(++rowCount);
				writeDisaListStaging(disa, row, locale, messageSourceService);
			}

			workbook.write(outByteStream);
			byte[] outArray = outByteStream.toByteArray();
			response.setContentType("application/ms-excel");
			response.setContentLength(outArray.length);
			response.setHeader("Expires:", "0");
			response.setHeader("Content-Disposition",
					"attachment; filename=Viral Load Data Details Staging Server.xls");
			OutputStream outStream = response.getOutputStream();
			outStream.write(outArray);
			outStream.flush();
			workbook.close();
		}
	}

	private void writeDisaListStaging(LabResult disa, Row row, Locale locale, MessageSourceService messageSourceService) {
		Cell cell = row.createCell(0);
		cell.setCellValue(disa.getLocation());

		cell = row.createCell(1);
		cell.setCellValue(disa.getRequestingFacilityName());

		cell = row.createCell(2);
		cell.setCellValue(disa.getRequestingDistrictName());

		cell = row.createCell(3);
		cell.setCellValue(disa.getHealthFacilityLabCode());

		cell = row.createCell(4);
		cell.setCellValue(disa.getNid());

		cell = row.createCell(5);
		cell.setCellValue(disa.getFirstName() + " " + disa.getLastName());

		cell = row.createCell(6);
		cell.setCellValue(disa.getGender());

		cell = row.createCell(7);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		cell.setCellValue("" + disa.getAge());

		cell = row.createCell(8);
		cell.setCellValue(disa.getRequestId());

		cell = row.createCell(9);
		cell.setCellValue(disa.getProcessingDate());

		cell = row.createCell(10);
		cell.setCellValue(disa.getLabResultDate());

		HIVVLLabResult vl = (HIVVLLabResult) disa;

		cell = row.createCell(11);
		cell.setCellValue(vl.getViralLoadResultCopies());

		cell = row.createCell(12);
		cell.setCellValue(vl.getViralLoadResultLog());

		cell = row.createCell(13);
		cell.setCellValue(vl.getHivViralLoadResult());

		cell = row.createCell(14);
		cell.setCellValue(disa.getLabResultStatus());

		cell = row.createCell(15);
		cell.setCellValue(disa.getCreatedAt());

		cell = row.createCell(16);
		cell.setCellValue(disa.getUpdatedAt());

		cell = row.createCell(17);
		cell.setCellValue(disa.getNotProcessingCause());
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
		causaNaoProcessamento.setCellValue(messageSourceService.getMessage("disa.notProcessingCause", null, locale));
	}

	private void createHeaderRowStaging(Sheet sheet, Locale locale, MessageSourceService messageSourceService) {
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
		cellNid.setCellValue(messageSourceService.getMessage("disa.location", null, locale));

		Cell cellNome = row.createCell(1);
		cellNome.setCellStyle(cellStyle);
		cellNome.setCellValue(messageSourceService.getMessage("disa.requesting.facility.name", null, locale));

		Cell cellSexo = row.createCell(2);
		cellSexo.setCellStyle(cellStyle);
		cellSexo.setCellValue(messageSourceService.getMessage("disa.requesting.district.name", null, locale));

		Cell cellIdade = row.createCell(3);
		cellIdade.setCellStyle(cellStyle);
		cellIdade.setCellValue(messageSourceService.getMessage("disa.sisma.code", null, locale));

		Cell cellCargaViralCopies = row.createCell(4);
		cellCargaViralCopies.setCellStyle(cellStyle);
		cellCargaViralCopies.setCellValue(messageSourceService.getMessage("disa.nid", null, locale));

		Cell cellCargaViralLog = row.createCell(5);
		cellCargaViralLog.setCellStyle(cellStyle);
		cellCargaViralLog.setCellValue(messageSourceService.getMessage("general.name", null, locale));

		Cell cellCargaViralCoded = row.createCell(6);
		cellCargaViralCoded.setCellStyle(cellStyle);
		cellCargaViralCoded.setCellValue(messageSourceService.getMessage("disa.gender", null, locale));

		Cell cellCargaViralStatus = row.createCell(7);
		cellCargaViralStatus.setCellStyle(cellStyle);
		cellCargaViralStatus.setCellValue(messageSourceService.getMessage("disa.age", null, locale));

		Cell cellRequestId = row.createCell(8);
		cellRequestId.setCellStyle(cellStyle);
		cellRequestId.setCellValue(messageSourceService.getMessage("disa.request.id", null, locale));

		Cell cellAnalysisDateTime = row.createCell(9);
		cellAnalysisDateTime.setCellStyle(cellStyle);
		cellAnalysisDateTime.setCellValue(messageSourceService.getMessage("disa.analysis.date.time", null, locale));

		Cell cellAuthorisedDateTime = row.createCell(10);
		cellAuthorisedDateTime.setCellStyle(cellStyle);
		cellAuthorisedDateTime.setCellValue(messageSourceService.getMessage("disa.authorised.date.time", null, locale));

		Cell cellViralLoadCopy = row.createCell(11);
		cellViralLoadCopy.setCellStyle(cellStyle);
		cellViralLoadCopy.setCellValue(messageSourceService.getMessage("disa.viralload.result.copy", null, locale));

		Cell cellViralLoadLog = row.createCell(12);
		cellViralLoadLog.setCellStyle(cellStyle);
		cellViralLoadLog.setCellValue(messageSourceService.getMessage("disa.viralload.result.log", null, locale));

		Cell cellViralLoadCoded = row.createCell(13);
		cellViralLoadCoded.setCellStyle(cellStyle);
		cellViralLoadCoded.setCellValue(messageSourceService.getMessage("disa.viralload.result.coded", null, locale));

		Cell cellLabResultStatus = row.createCell(14);
		cellLabResultStatus.setCellStyle(cellStyle);
		cellLabResultStatus.setCellValue(messageSourceService.getMessage("disa.status", null, locale));

		Cell cellViralLoadCreatedAt = row.createCell(15);
		cellViralLoadCreatedAt.setCellStyle(cellStyle);
		cellViralLoadCreatedAt.setCellValue(messageSourceService.getMessage("disa.created.at", null, locale));

		Cell cellViralLoadCreated = row.createCell(16);
		cellViralLoadCreated.setCellStyle(cellStyle);
		cellViralLoadCreated.setCellValue(messageSourceService.getMessage("disa.updated.at", null, locale));

		Cell cellViralLoadProcessing = row.createCell(17);
		cellViralLoadProcessing.setCellStyle(cellStyle);
		cellViralLoadProcessing
				.setCellValue(messageSourceService.getMessage("disa.notProcessingCause", null, locale));
	}

	private String formatDate(Date date, int i) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return dateFormat.format(date).replace("12:00:00", i == 1 ? "00:00:00" : "23:59:59");
	}
}
