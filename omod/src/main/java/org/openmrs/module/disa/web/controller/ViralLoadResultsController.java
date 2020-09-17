package org.openmrs.module.disa.web.controller;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.openmrs.LocationAttribute;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.extension.util.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Controller
public class ViralLoadResultsController {

	private RestUtil rest;

	private MessageSourceService messageSourceService;

	@Autowired
	public void setMessageSourceService(MessageSourceService messageSourceService) {
		this.messageSourceService = messageSourceService;
	}

	@RequestMapping(value = "/module/disa/viralLoadStatusList", method = RequestMethod.GET)
	public void showViralLoadStatusList(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}

	@RequestMapping(value = "/module/disa/viralLoadStatusList", method = RequestMethod.POST)
	public ModelAndView showViralLoadList(HttpServletRequest request, @RequestParam("vlState") String state,
			@RequestParam("startDate") Date startDate, @RequestParam("endDate") Date endDate) throws Exception {
		ModelAndView model = new ModelAndView();

		if (startDate == null) {
			model.addObject("errorStartDateRequired", "disa.error.startDate");
		}
		if (endDate == null) {
			model.addObject("errorEndDateRequired", "disa.error.endDate");
		}

		if (startDate == null || endDate == null) {
			return model;
		}
		rest = new RestUtil();
		rest.setURLBase(
				Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_URL).getPropertyValue());
		rest.setUsername(
				Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_USERNAME).getPropertyValue());
		rest.setPassword(
				Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_PASSWORD).getPropertyValue());

		HttpSession httpSession = request.getSession();
		httpSession.setAttribute("vlState", state);
		httpSession.setAttribute("startDate", startDate);
		httpSession.setAttribute("endDate", endDate);

		return new ModelAndView(new RedirectView(request.getContextPath() + "/module/disa/viralLoadResultsList.form"));
	}

	@RequestMapping(value = "/module/disa/viralLoadResultsList", method = RequestMethod.GET)
	public void showViralLoadResultList(HttpServletRequest request, ModelMap model,
			@RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) throws Exception {

		List<LocationAttribute> loAttribute = new ArrayList<LocationAttribute>(
				Context.getLocationService().getDefaultLocation().getAttributes());

		List<String> sismaCodes = new ArrayList<String>();
		sismaCodes.add(loAttribute.get(0).getValueReference());

		HttpSession httpSession = request.getSession();
		String vlState = (String) httpSession.getAttribute("vlState");
		Date startDate = (Date) httpSession.getAttribute("startDate");
		Date endDate = (Date) httpSession.getAttribute("endDate");
		String strStartDate = formatDate(startDate).replace("12:00:00", "00:00:00");
		String strEndDate = formatDate(endDate).replace("12:00:00", "23:59:59");

		String jsonViralLoadInfo = rest.getRequestGetFsrByStatusAndDates("/viral-status-dates",
				new ArrayList<String>(Arrays.asList(loAttribute.get(0).getValueReference())), vlState, strStartDate,
				strEndDate);
		List<Disa> vlDataLst = new Gson().fromJson(jsonViralLoadInfo, new TypeToken<ArrayList<Disa>>() {
		}.getType());

		httpSession.setAttribute("vlDataLst", vlDataLst);
		httpSession.setAttribute("openmrs_msg", openmrs_msg);
	}

	@RequestMapping(value = "/module/disa/viralLoadResultsList", method = RequestMethod.POST)
	public void downloadExcelFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpSession httpSession = request.getSession();
		@SuppressWarnings("unchecked")
		List<Disa> vlDataLst = (List<Disa>) httpSession.getAttribute("vlDataLst");
		createExcelFile(vlDataLst, response);
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	@RequestMapping(value = "/module/disa/mapPatientIdentifierForm", method = RequestMethod.GET)
	public void patientIdentifierMapping(@ModelAttribute("patient") Patient patient, HttpSession session,
			HttpServletRequest request,
			@RequestParam(required = false, value = "errorPatientRequired") String errorPatientRequired,
			@RequestParam(required = false, value = "errorSelectPatient") String errorSelectPatient) {
		String nid = (String) request.getParameter("nid");
		List<Patient> matchingPatients = null;

		if (nid == null) {
			nid = (String) session.getAttribute("nid");
			matchingPatients = (List<Patient>) session.getAttribute("patients");
		}

		List<Disa> vlDataLst = (List<Disa>) session.getAttribute("vlDataLst");
		Disa selectedPatient = null;
		for (Disa disa : vlDataLst) {
			if (disa.getNid().equals(nid)) {
				selectedPatient = disa;
				break;
			}
		}

		if (matchingPatients == null) {
			matchingPatients = Context.getPatientService()
					.getPatientsByName(selectedPatient.getFirstName() + " " + selectedPatient.getLastName());
		}

		session.setAttribute("selectedPatient", selectedPatient);
		session.setAttribute("patients", matchingPatients);
		session.setAttribute("nid", nid);
		session.setAttribute("errorPatientRequired", errorPatientRequired);
		session.setAttribute("errorSelectPatient", errorSelectPatient);
	}

	@SuppressWarnings({ "unchecked" })
	@RequestMapping(value = "/module/disa/addPatient.form", method = RequestMethod.POST)
	public ModelAndView addPatient(@ModelAttribute("patient") Patient patient, BindingResult result,
			HttpServletRequest request, HttpSession session) throws Exception {

		ModelAndView model = new ModelAndView(
				new RedirectView(request.getContextPath() + "/module/disa/mapPatientIdentifierForm.form"));
		if (patient.getId() == null) {
			model.addObject("errorPatientRequired", "disa.error.patient.required");
			return model;
		}

		List<Patient> patients = (List<Patient>) session.getAttribute("patients");
		Patient patientToAdd = Context.getPatientService().getPatient(patient.getId());
		if (!patients.contains(patientToAdd)) {
			// TODO This is a workaround to LazyInitialization error when getting
			// identifiers from patient on jsp
			Set<PatientIdentifier> identifiers = new TreeSet<PatientIdentifier>();
			identifiers.add(patientToAdd.getPatientIdentifier());
			patientToAdd.setIdentifiers(identifiers);
			patients.add(patientToAdd);
		}

		session.setAttribute("patients", patients);

		return model;
	}

	@SuppressWarnings("deprecation")
	@RequestMapping(value = "/module/disa/mapPatientIdentifierForm", method = RequestMethod.POST)
	public ModelAndView mapPatientIdentifier(HttpServletRequest request,
			@RequestParam(required = false, value = "patientUuid") String patientUuid) throws Exception {

		ModelAndView modelAndView = new ModelAndView(
				new RedirectView(request.getContextPath() + "/module/disa/mapPatientIdentifierForm.form"));

		if (patientUuid == null) {
			modelAndView.addObject("errorSelectPatient", "disa.select.patient");
			return modelAndView;
		}

		String nidDisa = (String) request.getSession().getAttribute("nid");

		Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		PatientIdentifierType identifierType = Context.getPatientService()
				.getPatientIdentifierTypeByUuid(Constants.DISA_NID);

		List<PatientIdentifier> patIdentidier = Context.getPatientService().getPatientIdentifiers(nidDisa,
				identifierType);
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
		return new ModelAndView(new RedirectView(request.getContextPath() + "/module/disa/viralLoadResultsList.form"));
	}

	private void createExcelFile(List<Disa> listDisa, HttpServletResponse response) throws Exception {
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
			createHeaderRow(sheet, locale);

			int rowCount = 0;

			for (Disa disa : listDisa) {
				Row row = sheet.createRow(++rowCount);
				writeDisaList(disa, row, locale);
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

	private void writeDisaList(Disa disa, Row row, Locale locale) {
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

		cell = row.createCell(9);
		String notProcessingCause = disa.getNotProcessingCause();
		if (notProcessingCause == null) {
			cell.setCellValue(notProcessingCause);
		} else {
			cell.setCellValue(messageSourceService.getMessage("disa." + notProcessingCause, null, locale));
		}
	}

	private void createHeaderRow(Sheet sheet, Locale locale) {
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

	private String formatDate(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return dateFormat.format(date);
	}
}
