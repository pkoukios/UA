/*
 * $Id:: ExcelServiceImpl.java 2021/03/01 09:07 dvelegra
 *  
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intelectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *  
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt' 
 */

package eu.euipo.etmdn.userarea.business.core.impl.service;

import eu.euipo.etmdn.userarea.business.core.api.service.ApplicationService;
import eu.euipo.etmdn.userarea.business.core.api.service.ExcelService;
import eu.euipo.etmdn.userarea.common.domain.exception.UserAreaException;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.domain.application.ApplicationDetails;
import eu.euipo.etmdn.userarea.domain.application.ApplicationRequest;
import eu.euipo.etmdn.userarea.domain.application.ApplicationSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.NOT_APPLICABLE;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.convertBooleanToStringAnswer;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.convertDateToString;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.convertLocarnoClassesToString;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.getStringValue;

/**
 * The ExcelServiceImpl class.
 */
@Slf4j
@Service
public class ExcelServiceImpl implements ExcelService {

    private static final String TRADEMARK_FILENAME = "trademarks.xlsx";
    private static final String TRADEMARK_SHEET = "Trademarks";

    private static final String DESIGN_FILENAME = "designs.xlsx";
    private static final String DESIGN_SHEET = "Designs";

    private static final String ESERVICE_FILENAME = "eservices.xlsx";
    private static final String ESERVICE_SHEET = "Eservices";

    private final ApplicationService applicationService;

    /**
     * Instantiates Application controller.
     *
     * @param applicationService  the application service
     */
    @Autowired
    public ExcelServiceImpl(final ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Generate excel file.
     *
     * @param applicationRequest the applicationRequest
     * @param roles the logged in user's set of roles
     * @return {@link FileInfo} the generated excel file information
     */
    @Override
    public FileInfo generateExcel(final ApplicationRequest applicationRequest, Set<String> roles) {
        switch (ApplicationType.getApplicationType(applicationRequest.getApplicationType())) {
            case TRADEMARK:
                    return this.trademarkToExcel(applicationService.getApplications(applicationRequest, roles), applicationRequest.getColumns(), applicationRequest.getIsDraft());
            case DESIGN:
                    return this.designToExcel(applicationService.getApplications(applicationRequest, roles), applicationRequest.getColumns(), applicationRequest.getIsDraft());
            case ESERVICE:
                    return this.eserviceToExcel(applicationService.getApplications(applicationRequest, roles), applicationRequest.getColumns(), applicationRequest.getIsDraft());
            default:
                return null;
        }
    }

    /**
     * Generate excel report for trademarks.
     *
     * @param applications the trademark applications
     * @param columns the list of columns to be used
     * @param isDraft the flag that indicates if the application is draft or not
     * @return {@link FileInfo} the generated excel report with trademarks
     */
    private FileInfo trademarkToExcel(ApplicationSearchResult applications, List<String> columns, boolean isDraft) {
        final Workbook workbook = isDraft ? generateExcelForTrademarkDrafts(applications.getContent(), columns) :
                generateExcelForTrademarks(applications.getContent(), columns);
        final byte[] fileContent = Optional.of(writeExcel(workbook)).map(ByteArrayOutputStream::toByteArray).orElse(new byte[]{});
        return new FileInfo(fileContent, TRADEMARK_FILENAME);
    }

    /**
     * Generate excel report for designs.
     *
     * @param applications the design applications
     * @param columns the list of columns to be used
     * @param isDraft the flag that indicates if the application is draft or not
     * @return {@link FileInfo} the generated excel report with designs
     */
    private FileInfo designToExcel(ApplicationSearchResult applications, List<String> columns,  boolean isDraft) {
        final Workbook workbook = isDraft ? generateExcelForDesignDrafts(applications.getContent(), columns) :
                generateExcelForDesigns(applications.getContent(), columns);
        final byte[] fileContent = Optional.of(writeExcel(workbook)).map(ByteArrayOutputStream::toByteArray).orElse(new byte[]{});
        return new FileInfo(fileContent, DESIGN_FILENAME);
    }

    /**
     * Generate excel report for eservices.
     *
     * @param applications the eservice applications
     * @param columns the list of columns to be used
     * @param isDraft the flag that indicates if the application is draft or not
     * @return {@link FileInfo} the generated excel file reports with eservices
     */
    private FileInfo eserviceToExcel(ApplicationSearchResult applications, List<String> columns,  boolean isDraft) {
        final Workbook workbook = isDraft ? generateExcelForEserviceDrafts(applications.getContent(), columns) :
                generateExcelForEservices(applications.getContent(), columns);
        final byte[] fileContent = Optional.of(writeExcel(workbook)).map(ByteArrayOutputStream::toByteArray).orElse(new byte[]{});
        return new FileInfo(fileContent, ESERVICE_FILENAME);
    }

    private Workbook generateExcelForTrademarks(final List<ApplicationDetails> applications, List<String> columns) {
        final Workbook workbook = new XSSFWorkbook();
        final CellStyle headerStyle = createHeaderStyle(workbook);
        final CellStyle cellStyle = createCellStyle(workbook);
        final Sheet sheet = workbook.createSheet(TRADEMARK_SHEET);
        int rowCount = 0;
        insertHeader(sheet, rowCount, headerStyle, columns);
        rowCount++;
        for (final ApplicationDetails application : applications) {
            final Row row = sheet.createRow(rowCount++);
            insertRowImage(workbook, sheet, row, getStringValue(application.getGraphicalRepresentation()), rowCount);
            insertTrademark(row, application, cellStyle);
        }
        return workbook;
    }

    private Workbook generateExcelForTrademarkDrafts(final List<ApplicationDetails> applications, List<String> columns) {
        final Workbook workbook = new XSSFWorkbook();
        final CellStyle headerStyle = createHeaderStyle(workbook);
        final CellStyle cellStyle = createCellStyle(workbook);
        final Sheet sheet = workbook.createSheet(TRADEMARK_SHEET);
        int rowCount = 0;
        insertHeader(sheet, rowCount, headerStyle, columns);
        rowCount++;
        for (final ApplicationDetails application : applications) {
            final Row row = sheet.createRow(rowCount++);
            insertTrademarkDraft(row, application, cellStyle);
        }
        return workbook;
    }

    private Workbook generateExcelForDesigns(final List<ApplicationDetails> applications, List<String> columns) {
        final Workbook workbook = new XSSFWorkbook();
        final CellStyle headerStyle = createHeaderStyle(workbook);
        final CellStyle cellStyle = createCellStyle(workbook);
        final Sheet sheet = workbook.createSheet(DESIGN_SHEET);
        int rowCount = 0;
        insertHeader(sheet, rowCount, headerStyle, columns);
        rowCount++;
        for (final ApplicationDetails application : applications) {
            final Row row = sheet.createRow(rowCount++);
            insertRowImage(workbook, sheet, row, getStringValue(application.getGraphicalRepresentation()), rowCount);
            insertDesign(row, application, cellStyle);
        }
        return workbook;
    }

    private Workbook generateExcelForDesignDrafts(final List<ApplicationDetails> applications, List<String> columns) {
        final Workbook workbook = new XSSFWorkbook();
        final CellStyle headerStyle = createHeaderStyle(workbook);
        final CellStyle cellStyle = createCellStyle(workbook);
        final Sheet sheet = workbook.createSheet(DESIGN_SHEET);
        int rowCount = 0;
        insertHeader(sheet, rowCount, headerStyle, columns);
        rowCount++;
        for (final ApplicationDetails application : applications) {
            final Row row = sheet.createRow(rowCount++);
            insertDesignDraft(row, application, cellStyle);
        }
        return workbook;
    }

    private Workbook generateExcelForEservices(final List<ApplicationDetails> applications, List<String> columns) {
        final Workbook workbook = new XSSFWorkbook();
        final CellStyle headerStyle = createHeaderStyle(workbook);
        final CellStyle cellStyle = createCellStyle(workbook);
        final Sheet sheet = workbook.createSheet(ESERVICE_SHEET);
        int rowCount = 0;
        insertHeader(sheet, rowCount, headerStyle, columns);
        rowCount++;
        for (final ApplicationDetails application : applications) {
            final Row row = sheet.createRow(rowCount++);
            insertEservice(row, application, cellStyle);
        }
        return workbook;
    }

    private Workbook generateExcelForEserviceDrafts(final List<ApplicationDetails> applications, List<String> columns) {
        final Workbook workbook = new XSSFWorkbook();
        final CellStyle headerStyle = createHeaderStyle(workbook);
        final CellStyle cellStyle = createCellStyle(workbook);
        final Sheet sheet = workbook.createSheet(ESERVICE_SHEET);
        int rowCount = 0;
        insertHeader(sheet, rowCount, headerStyle, columns);
        rowCount++;
        for (final ApplicationDetails application : applications) {
            final Row row = sheet.createRow(rowCount++);
            insertEserviceDraft(row, application, cellStyle);
        }
        return workbook;
    }

    private static ByteArrayOutputStream writeExcel(final Workbook workbook) {
        try {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            workbook.write(os);
            return os;
        } catch (final IOException ex) {
            throw new UserAreaException("Error generating excel", ex);
        }
    }

    private void insertRowImage(final Workbook workbook, final Sheet sheet, final Row row, final String graphicalRepresentation, int rowIndex) {
        row.setHeight((short)2000);
        if(NOT_APPLICABLE.equals(graphicalRepresentation)) {
            return;
        }
        //Get the contents of an InputStream as a byte[].
        byte[] image = Base64Utils.decodeFromString(graphicalRepresentation);
        //Adds a picture to the workbook
        int pictureIdx = workbook.addPicture(image, Workbook.PICTURE_TYPE_JPEG);
        //Returns an object that handles instantiating concrete classes
        XSSFCreationHelper helper = (XSSFCreationHelper) workbook.getCreationHelper();
        //Creates the top-level drawing patriarch.
        XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
        //Create an anchor that is attached to the worksheet
        XSSFClientAnchor anchor = helper.createClientAnchor();
        //create an anchor with upper left cell
        anchor.setCol1(0);
        anchor.setCol2(1);
        anchor.setRow1(rowIndex-1);
        anchor.setRow2(rowIndex);
        //Creates a picture
        XSSFPicture pic = drawing.createPicture(anchor, pictureIdx);
        // 0 indicates solid line
        pic.setLineStyle(0);
        // rgb color code for black line
        pic.setLineStyleColor(0, 0, 0);
        // double number for line width
        pic.setLineWidth(1.5);
    }

    private void insertTrademark(final Row row, final ApplicationDetails application, CellStyle cellStyle) {
        createCell(row, 1, getStringValue(application.getNumber()), cellStyle);
        createCell(row, 2, convertDateToString(application.getApplicationDate()), cellStyle);
        createCell(row, 3, getStringValue(application.getType()), cellStyle);
        createCell(row, 4, getStringValue(application.getKind()), cellStyle);
        createCell(row, 5, getStringValue(application.getDenomination()), cellStyle);
        createCell(row, 6, getStringValue(application.getNiceClass()), cellStyle);
        createCell(row, 7, getStringValue(application.getStatus()), cellStyle);
        createCell(row, 8, convertDateToString(application.getStatusDate()), cellStyle);
        createCell(row, 9, getStringValue(application.getRegistrationNumber()), cellStyle);
        createCell(row, 10, convertDateToString(application.getRegistrationDate()), cellStyle);
        createCell(row, 11, convertDateToString(application.getExpirationDate()),cellStyle);
        createCell(row, 12, convertDateToString(application.getPublicationDate()), cellStyle);
        createCell(row, 13, getStringValue(application.getApplicant()), cellStyle);
        createCell(row, 14, getStringValue(application.getRepresentative()),cellStyle);
    }

    private void insertTrademarkDraft(final Row row, final ApplicationDetails application, CellStyle cellStyle) {
        createCell(row, 0, getStringValue(application.getDenomination()), cellStyle);
        createCell(row, 1, getStringValue(application.getNumber()), cellStyle);
        createCell(row, 2, convertDateToString(application.getCreationDate()), cellStyle);
        createCell(row, 3, getStringValue(application.getRepresentative()),cellStyle);
        createCell(row, 4, getStringValue(application.getApplicant()), cellStyle);
        createCell(row, 5, getStringValue(application.getNiceClass()), cellStyle);
        createCell(row, 6, convertDateToString(application.getLastModifiedDate()), cellStyle);
        createCell(row, 7, getStringValue(application.getLastModifiedBy()), cellStyle);
    }

    private void insertDesign(final Row row, final ApplicationDetails application, CellStyle cellStyle) {
        createCell(row, 1, getStringValue(application.getDesignNumber()), cellStyle);
        createCell(row, 2, convertDateToString(application.getApplicationDate()), cellStyle);
        createCell(row, 3, getStringValue(application.getIndication()), cellStyle);
        createCell(row, 4, convertLocarnoClassesToString(application.getLocarnos()), cellStyle);
        createCell(row, 5, convertBooleanToStringAnswer(application.getDeferPublication()), cellStyle);
        createCell(row, 6, getStringValue(application.getDesigner()),cellStyle);
        createCell(row, 7, getStringValue(application.getNumber()), cellStyle);
        createCell(row, 8, getStringValue(application.getStatus()), cellStyle);
        createCell(row, 9, convertDateToString(application.getStatusDate()), cellStyle);
        createCell(row, 10, getStringValue(application.getRegistrationNumber()), cellStyle);
        createCell(row, 11, convertDateToString(application.getRegistrationDate()), cellStyle);
        createCell(row, 12, convertDateToString(application.getExpirationDate()),cellStyle);
        createCell(row, 13, convertDateToString(application.getPublicationDate()), cellStyle);
        createCell(row, 14, getStringValue(application.getApplicant()), cellStyle);
        createCell(row, 15, getStringValue(application.getRepresentative()),cellStyle);
    }

    private void insertDesignDraft(final Row row, final ApplicationDetails application, CellStyle cellStyle) {
        createCell(row, 0, getStringValue(String.valueOf(application.getAssociatedDesignNumber())), cellStyle);
        createCell(row, 1, getStringValue(application.getNumber()), cellStyle);
        createCell(row, 2, convertDateToString(application.getCreationDate()), cellStyle);
        createCell(row, 3, getStringValue(application.getRepresentative()),cellStyle);
        createCell(row, 4, getStringValue(application.getApplicant()), cellStyle);
        createCell(row, 5, convertDateToString(application.getLastModifiedDate()), cellStyle);
        createCell(row, 6, getStringValue(application.getLastModifiedBy()), cellStyle);
    }

    private void insertEservice(final Row row, final ApplicationDetails application, CellStyle cellStyle) {
        createCell(row, 0, getStringValue(application.getEserviceName()), cellStyle);
        createCell(row, 1, getStringValue(application.getAssociatedRight()), cellStyle);
        createCell(row, 2, getStringValue(application.getNumber()), cellStyle);
        createCell(row, 3, convertDateToString(application.getApplicationDate()), cellStyle);
        createCell(row, 4, getStringValue(application.getStatus()), cellStyle);
        createCell(row, 5, convertDateToString(application.getStatusDate()), cellStyle);
        createCell(row, 6, getStringValue(application.getApplicant()), cellStyle);
        createCell(row, 7, getStringValue(application.getRepresentative()),cellStyle);
    }

    private void insertEserviceDraft(final Row row, final ApplicationDetails application, CellStyle cellStyle) {
        createCell(row, 0, getStringValue(application.getEserviceName()), cellStyle);
        createCell(row, 1, getStringValue(application.getNumber()), cellStyle);
        createCell(row, 2, getStringValue(application.getAssociatedRight()), cellStyle);
        createCell(row, 3, convertDateToString(application.getCreationDate()), cellStyle);
        createCell(row, 4, getStringValue(application.getRepresentative()), cellStyle);
        createCell(row, 5, getStringValue(application.getApplicant()), cellStyle);
        createCell(row, 6, convertDateToString(application.getLastModifiedDate()), cellStyle);
        createCell(row, 7, getStringValue(application.getLastModifiedBy()), cellStyle);
    }

    private void insertHeader(final Sheet sheet, final int rowCount, final CellStyle headerStyle, List<String> headers) {
        final Row rowHeader = sheet.createRow(rowCount);
        for (int col = 0; col < headers.size(); col++) {
            Cell cell = rowHeader.createCell(col);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(headers.get(col));
            sheet.autoSizeColumn(col);
        }
    }

    private void createCell(final Row row, final Integer columnIndex, final String value, final CellStyle style) {
        final Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        Optional.ofNullable(style).ifPresent(s -> cell.setCellStyle(style));
    }

    private CellStyle createHeaderStyle(final Workbook workbook) {
        final CellStyle cellStyle = workbook.createCellStyle();
        final Font font = workbook.createFont();
        font.setColor(IndexedColors.BRIGHT_GREEN.index);
        font.setBold(true);
        cellStyle.setFont(font);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setWrapText(true);
        return cellStyle;
    }

    private CellStyle createCellStyle(final Workbook workbook) {
        final CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setWrapText(true);
        return cellStyle;
    }
}