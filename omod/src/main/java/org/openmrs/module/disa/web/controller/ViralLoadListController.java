package org.openmrs.module.disa.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import org.openmrs.module.disa.Disa;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import java.io.*;
import java.text.SimpleDateFormat;

@Controller
public class ViralLoadListController {

	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "/module/disa/viralLoadList", method = RequestMethod.GET)
	public void showViralLoadList(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}
	
	@RequestMapping(value = "/module/disa/viralLoadList", method = RequestMethod.POST)
	public void downloadExcelFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpSession httpSession = request.getSession();
		@SuppressWarnings("unchecked")
		List<Disa> vlDataLst = (List<Disa>) httpSession.getAttribute("vlDataLst");
		createExcelFile(vlDataLst,response);
	}
	
	private void writeDisaList(Disa disa, Row row) {
	    Cell cell = row.createCell(0);
	    cell.setCellValue(disa.getNid());
	 
	    cell = row.createCell(1);
	    cell.setCellValue(disa.getFirstName());
	 
	    cell = row.createCell(2);
	    cell.setCellValue(disa.getGender());
	    
	    cell = row.createCell(3);
	    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
	    SimpleDateFormat datetemp = new SimpleDateFormat("yyyy-MM-dd");
	    String cellValue = datetemp.format(disa.getDateOfBirth());
	    cell.setCellValue(cellValue);
	    
	    cell = row.createCell(4);
	    cell.setCellValue(disa.getRequestId());
	    
	    cell = row.createCell(5);
	    cell.setCellValue(disa.getViralLoadResultCopies());
	    
	    cell = row.createCell(6);
	    cell.setCellValue(disa.getViralLoadResultLog());
	    
	    cell = row.createCell(7);
	    cell.setCellValue(disa.getHivViralLoadResult());
	    
	    cell = row.createCell(8);
	    cell.setCellValue(disa.getViralLoadStatus());
	}
	
	public void createHeaderRow(Sheet sheet) {
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
	    cellNid.setCellValue("NID");
	 
	    Cell cellNome = row.createCell(1);
	    cellNome.setCellStyle(cellStyle);
	    cellNome.setCellValue("Nome");
	 
	    Cell cellSexo = row.createCell(2);
	    cellSexo.setCellStyle(cellStyle);
	    cellSexo.setCellValue("Sexo");
	    
	    Cell cellIdade = row.createCell(3);
	    cellIdade.setCellStyle(cellStyle);
	    cellIdade.setCellValue("Idade");
	    
	    Cell cellIDRequisacao = row.createCell(4);
	    cellIDRequisacao.setCellStyle(cellStyle);
	    cellIDRequisacao.setCellValue("ID(Requisição)");
	    
	    Cell cellCargaViralCopies = row.createCell(5);
	    cellCargaViralCopies.setCellStyle(cellStyle);
	    cellCargaViralCopies.setCellValue("Carga Viral(Cópia)");
	    
	    Cell cellCargaViralLog = row.createCell(6);
	    cellCargaViralLog.setCellStyle(cellStyle);
	    cellCargaViralLog.setCellValue("Carga Viral(Log)");
	    
	    Cell cellCargaViralCoded = row.createCell(7);
	    cellCargaViralCoded.setCellStyle(cellStyle);
	    cellCargaViralCoded.setCellValue("Carga Viral(Coded)");
	    
	    Cell cellCargaViralStatus = row.createCell(8);
	    cellCargaViralStatus.setCellStyle(cellStyle);
	    cellCargaViralStatus.setCellValue("Status");
	}
	
	public void createExcelFile(List<Disa> listDisa, HttpServletResponse response) throws Exception {
		try (ByteArrayOutputStream outByteStream = new ByteArrayOutputStream()){
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
			createHeaderRow(sheet);
			
		    int rowCount = 0;
		    
		    for (Disa disa : listDisa) {
		        Row row = sheet.createRow(++rowCount);
		        writeDisaList(disa, row);
		    }
			
			workbook.write(outByteStream);
			byte [] outArray = outByteStream.toByteArray();
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
}
