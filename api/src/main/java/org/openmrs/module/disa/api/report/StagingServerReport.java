package org.openmrs.module.disa.api.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.api.LabResult;

public class StagingServerReport {

    public static final String REPORT_NAME = "Lab Results Data Details Staging Server";

    private static final int HF_NAME = 0;
    private static final int DISTRICT_NAME = 1;
    private static final int SISMA_CODE = 2;
    private static final int NID = 3;
    private static final int NAME = 4;
    private static final int GENDER = 5;
    private static final int AGE = 6;
    private static final int REQUEST_ID = 7;
    private static final int HARVEST_DATE = 8;
    private static final int AUTHORIZED_DATE = 9;
    private static final int FINAL_RESULT = 10;
    private static final int TYPE_OF_RESULT = 11;
    private static final int STATUS = 12;
    private static final int CREATED_AT = 13;
    private static final int UPDATED_AT = 14;
    private static final int NOT_PROCESSING_CAUSE = 15;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Workbook workbook;
    private MessageSourceService messageSourceService;

    public StagingServerReport(MessageSourceService messageSourceService) {
        workbook = new HSSFWorkbook();
        this.messageSourceService = messageSourceService;
    }

    public void addStagingServerSheet(List<LabResult> labResults) {
        HSSFSheet sheet = (HSSFSheet) workbook.createSheet("Lab Results Data Details");

        addHeaderRow(sheet);

        int rowCount = 0;
        for (LabResult labResult : labResults) {
            Row row = sheet.createRow(++rowCount);
            addLabResultRow(labResult, row);
        }

        sheet.autoSizeColumn(HF_NAME);
        sheet.autoSizeColumn(DISTRICT_NAME);
        sheet.autoSizeColumn(SISMA_CODE);
        sheet.autoSizeColumn(NID);
        sheet.autoSizeColumn(NAME);
        sheet.autoSizeColumn(GENDER);
        sheet.autoSizeColumn(AGE);
        sheet.autoSizeColumn(REQUEST_ID);
        sheet.autoSizeColumn(HARVEST_DATE);
        sheet.autoSizeColumn(AUTHORIZED_DATE);
        sheet.autoSizeColumn(FINAL_RESULT);
        sheet.autoSizeColumn(TYPE_OF_RESULT);
        sheet.autoSizeColumn(STATUS);
        sheet.autoSizeColumn(CREATED_AT);
        sheet.autoSizeColumn(UPDATED_AT);
        sheet.autoSizeColumn(NOT_PROCESSING_CAUSE);
    }

    public byte[] generateReport() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }

    private void addHeaderRow(Sheet sheet) {
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        cellStyle.setFont(font);
        cellStyle.setWrapText(true);

        Row row = sheet.createRow(0);

        Cell cellNome = row.createCell(HF_NAME);
        cellNome.setCellStyle(cellStyle);
        cellNome.setCellValue(messageSourceService.getMessage("disa.requesting.facility.name"));

        Cell cellSexo = row.createCell(DISTRICT_NAME);
        cellSexo.setCellStyle(cellStyle);
        cellSexo.setCellValue(messageSourceService.getMessage("disa.requesting.district.name"));

        Cell cellIdade = row.createCell(SISMA_CODE);
        cellIdade.setCellStyle(cellStyle);
        cellIdade.setCellValue(messageSourceService.getMessage("disa.sisma.code"));

        Cell cellCargaViralCopies = row.createCell(NID);
        cellCargaViralCopies.setCellStyle(cellStyle);
        cellCargaViralCopies.setCellValue(messageSourceService.getMessage("disa.nid"));

        Cell cellCargaViralLog = row.createCell(NAME);
        cellCargaViralLog.setCellStyle(cellStyle);
        cellCargaViralLog.setCellValue(messageSourceService.getMessage("general.name"));

        Cell cellCargaViralCoded = row.createCell(GENDER);
        cellCargaViralCoded.setCellStyle(cellStyle);
        cellCargaViralCoded.setCellValue(messageSourceService.getMessage("disa.gender"));

        Cell cellCargaViralStatus = row.createCell(AGE);
        cellCargaViralStatus.setCellStyle(cellStyle);
        cellCargaViralStatus.setCellValue(messageSourceService.getMessage("disa.age"));

        Cell cellRequestId = row.createCell(REQUEST_ID);
        cellRequestId.setCellStyle(cellStyle);
        cellRequestId.setCellValue(messageSourceService.getMessage("disa.request.id"));

        Cell harvestDate = row.createCell(HARVEST_DATE);
        harvestDate.setCellStyle(cellStyle);
        harvestDate.setCellValue(messageSourceService.getMessage("disa.harvest.date"));

        Cell cellAuthorisedDateTime = row.createCell(AUTHORIZED_DATE);
        cellAuthorisedDateTime.setCellStyle(cellStyle);
        cellAuthorisedDateTime.setCellValue(messageSourceService.getMessage("disa.authorised.date.time"));

        Cell cellViralLoadCopy = row.createCell(FINAL_RESULT);
        cellViralLoadCopy.setCellStyle(cellStyle);
        cellViralLoadCopy.setCellValue(messageSourceService.getMessage("disa.lab.result"));

        Cell cellViralLoadLog = row.createCell(TYPE_OF_RESULT);
        cellViralLoadLog.setCellStyle(cellStyle);
        cellViralLoadLog.setCellValue(messageSourceService.getMessage("disa.lab.result.type"));

        Cell cellLabResultStatus = row.createCell(STATUS);
        cellLabResultStatus.setCellStyle(cellStyle);
        cellLabResultStatus.setCellValue(messageSourceService.getMessage("disa.status"));

        Cell cellViralLoadCreatedAt = row.createCell(CREATED_AT);
        cellViralLoadCreatedAt.setCellStyle(cellStyle);
        cellViralLoadCreatedAt.setCellValue(messageSourceService.getMessage("disa.created.at"));

        Cell cellViralLoadCreated = row.createCell(UPDATED_AT);
        cellViralLoadCreated.setCellStyle(cellStyle);
        cellViralLoadCreated.setCellValue(messageSourceService.getMessage("disa.updated.at"));

        Cell cellViralLoadProcessing = row.createCell(NOT_PROCESSING_CAUSE);
        cellViralLoadProcessing.setCellStyle(cellStyle);
        cellViralLoadProcessing
                .setCellValue(messageSourceService.getMessage("disa.notProcessingCause"));
    }

    private void addLabResultRow(LabResult disa, Row row) {
        Cell cell = row.createCell(HF_NAME);
        cell.setCellValue(disa.getRequestingFacilityName());

        cell = row.createCell(DISTRICT_NAME);
        cell.setCellValue(disa.getRequestingDistrictName());

        cell = row.createCell(SISMA_CODE);
        cell.setCellValue(disa.getHealthFacilityLabCode());

        cell = row.createCell(NID);
        cell.setCellValue(disa.getNid());

        cell = row.createCell(NAME);
        cell.setCellValue(disa.getFirstName() + " " + disa.getLastName());

        cell = row.createCell(GENDER);
        cell.setCellValue(disa.getGender());

        cell = row.createCell(AGE);
        cell.setCellValue(disa.getAge() != null ? "" + disa.getAge() : "");

        cell = row.createCell(REQUEST_ID);
        cell.setCellValue(disa.getRequestId());

        cell = row.createCell(HARVEST_DATE);
        cell.setCellValue(
                disa.getHarvestDate() != null ? disa.getHarvestDate().format(DATE_FORMAT) : "");

        cell = row.createCell(AUTHORIZED_DATE);
        cell.setCellValue(
                disa.getLabResultDate() != null ? disa.getLabResultDate().format(DATE_FORMAT) : "");

        cell = row.createCell(FINAL_RESULT);
        cell.setCellValue(disa.getFinalResult());

        cell = row.createCell(TYPE_OF_RESULT);
        cell.setCellValue(messageSourceService.getMessage("disa.typeOfResult." + disa.getTypeOfResult()));

        cell = row.createCell(STATUS);
        cell.setCellValue(messageSourceService.getMessage("disa.viral.load.status." + disa.getLabResultStatus()));

        cell = row.createCell(CREATED_AT);
        cell.setCellValue(disa.getCreatedAt().format(DATE_FORMAT));

        cell = row.createCell(UPDATED_AT);
        cell.setCellValue(disa.getUpdatedAt() != null ? disa.getUpdatedAt().format(DATE_FORMAT) : "");

        cell = row.createCell(NOT_PROCESSING_CAUSE);
        if (disa.getNotProcessingCause() != null) {
            cell.setCellValue(
                    messageSourceService.getMessage("disa.notProcessingCause." + disa.getNotProcessingCause()));
        }
    }
}
