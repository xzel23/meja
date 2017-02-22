/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.model.poi;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.RefOption;
import com.dua3.meja.text.RichText;
import com.dua3.meja.text.RichTextBuilder;
import com.dua3.meja.text.Run;
import com.dua3.meja.text.Style;
import com.dua3.meja.util.MejaHelper;
import com.dua3.meja.util.RectangularRegion;

/**
 *
 * @author axel
 */
public final class PoiCell implements Cell {

    private static final Logger LOGGER = Logger.getLogger(PoiCell.class.getName());

    private static CellType translateCellType(org.apache.poi.ss.usermodel.CellType poiType) {
        switch (poiType) {
            case BLANK:
                return CellType.BLANK;
            case BOOLEAN:
                return CellType.BOOLEAN;
            case ERROR:
                return CellType.ERROR;
            case NUMERIC:
                return CellType.NUMERIC;
            case STRING:
                return CellType.TEXT;
            case FORMULA:
                return CellType.FORMULA;
            default:
                throw new IllegalArgumentException();
        }
    }

    final PoiWorkbook workbook;
    final PoiRow row;
    final org.apache.poi.ss.usermodel.Cell poiCell;
    int spanX;
    int spanY;
    PoiCell logicalCell;

    public PoiCell(PoiRow row, org.apache.poi.ss.usermodel.Cell cell) {
        this.workbook = row.getWorkbook();
        this.row = row;
        this.poiCell = cell;

        RectangularRegion mergedRegion = row.getSheet().getMergedRegion(cell.getRowIndex(), cell.getColumnIndex());

        if (mergedRegion == null) {
            // cell is not merged
            this.spanX = 1;
            this.spanY = 1;
            this.logicalCell = this;
        } else {
            boolean isTop = getRowNumber() == mergedRegion.getFirstRow();
            boolean isTopLeft = isTop && getColumnNumber() == mergedRegion.getFirstColumn();
            PoiCell topLeftCell;
            if (isTopLeft) {
                topLeftCell = this;
            } else {
                PoiRow topRow = isTop ? row : row.getSheet().getRow(mergedRegion.getFirstRow());
                topLeftCell = topRow.getCell(mergedRegion.getFirstColumn());
            }

            int spanX_ = 1 + mergedRegion.getLastColumn() - mergedRegion.getFirstColumn();
            int spanY_ = 1 + mergedRegion.getLastRow() - mergedRegion.getFirstRow();

            addedToMergedRegion(topLeftCell, spanX_, spanY_);
        }

    }

    @Override
    public PoiWorkbook getWorkbook() {
        return workbook;
    }

    @Override
    public PoiSheet getSheet() {
        return row.getSheet();
    }

    @Override
    public PoiRow getRow() {
        return row;
    }

    @Override
    public CellType getCellType() {
        @SuppressWarnings("deprecation")
        CellType type = translateCellType(poiCell.getCellTypeEnum());
        // since formulas returning dates should return CellType.FORMULA
        // rather than CellType.DATE, only test for dates if cell is numeric.
        if (type == CellType.NUMERIC && isCellDateFormatted()) {
            type = CellType.DATE;
        }
        return type;
    }

    @SuppressWarnings("deprecation")
    @Override
    public CellType getResultType() {
        org.apache.poi.ss.usermodel.CellType poiType = poiCell.getCellTypeEnum();
        if (poiType == org.apache.poi.ss.usermodel.CellType.FORMULA) {
            poiType = poiCell.getCachedFormulaResultTypeEnum();
        }
        CellType type = translateCellType(poiType);
        if (type == CellType.NUMERIC && isCellDateFormatted()) {
            type = CellType.DATE;
        }
        return type;
    }

    @Override
    public Object get() {
        if (isEmpty()) {
            return null;
        }

        switch (getCellType()) {
            case BLANK:
                return null;
            case DATE:
                return poiCell.getDateCellValue();
            case NUMERIC:
                return poiCell.getNumericCellValue();
            case FORMULA:
                return poiCell.getCellFormula();
            case BOOLEAN:
                return poiCell.getBooleanCellValue();
            case TEXT:
                return getText();
            case ERROR:
                return ERROR_TEXT;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public boolean getBoolean() {
      if (getCellType()!=CellType.BOOLEAN) {
        throw new IllegalStateException("Cell does not contain a boolean value.");
      }
      return poiCell.getBooleanCellValue();
    }

    @Override
    public String getFormula() {
        return poiCell.getCellFormula();
    }

    @Override
    @Deprecated
    public Date getDate() {
        if (isEmpty()) { // POI will throw for wrong CellType but return null for empty cells
          throw new IllegalStateException("Cell does not contain a date.");
        }
        return poiCell.getDateCellValue();
    }

    @Override
    public LocalDateTime getDateTime() {
        if (isEmpty()) { // POI will throw for wrong CellType but return null for empty cells
          throw new IllegalStateException("Cell does not contain date/time.");
        }
        return LocalDateTime.ofInstant(poiCell.getDateCellValue().toInstant(), ZoneId.systemDefault());
    }

    @Override
    public Number getNumber() {
      if (getCellType()!=CellType.NUMERIC) {
        throw new IllegalStateException("Cell does not contain a numeric value.");
      }
      return poiCell.getNumericCellValue();
    }

    @Override
    public RichText getText() {
        return isEmpty() ? RichText.emptyText() : toRichText(poiCell.getRichStringCellValue());
    }

    @Override
    public int getRowNumber() {
        return poiCell.getRowIndex();
    }

    @Override
    public int getColumnNumber() {
        return poiCell.getColumnIndex();
    }

    @Override
    public int getHorizontalSpan() {
        return spanX;
    }

    @Override
    public int getVerticalSpan() {
        return spanY;
    }

    @Override
    public Cell getLogicalCell() {
        return logicalCell;
    }

    @Override
    public Cell set(RichText s) {
        Object old = get();

        RichTextString richText = workbook.createRichTextString(s.toString());
        for (Run run : s) {
            PoiFont font = getWorkbook().getPoiFont(getCellStyle().getFont(), run.getStyle());
            richText.applyFont(run.getStart(), run.getEnd(), font.getPoiFont());
        }
        poiCell.setCellValue(richText);

        updateRow();

        getSheet().cellValueChanged(this, old, s);

        return this;
    }

    @Override
    public RichText getAsText() {
        if (getCellType() == CellType.TEXT) {
            return toRichText(poiCell.getRichStringCellValue());
        } else {
            if (isEmpty()) {
                return RichText.emptyText();
            }

            DataFormatter dataFormatter = getWorkbook().getDataFormatter();

            try {
                FormulaEvaluator evaluator = getWorkbook().evaluator;
                return RichText.valueOf(dataFormatter.formatCellValue(poiCell, evaluator));
            } catch (Exception ex) {
                return RichText.valueOf(Cell.ERROR_TEXT);
            }
        }
    }

    @Override
    public String toString() {
        if (getCellType() == CellType.TEXT) {
            return poiCell.getStringCellValue();
        } else {
            if (isEmpty()) {
                return "";
            }

            DataFormatter dataFormatter = getWorkbook().getDataFormatter();
            try {
                FormulaEvaluator evaluator = getWorkbook().evaluator;
                return dataFormatter.formatCellValue(poiCell, evaluator);
            } catch (Exception ex) {
                return Cell.ERROR_TEXT;
            }
        }
    }

    @Override
    public void clear() {
        if (!isEmpty()) {
            Object old = get();
            poiCell.setCellType(org.apache.poi.ss.usermodel.CellType.BLANK);
            updateRow();
            getSheet().cellValueChanged(this, old, null);
        }
    }

    @Override
    public PoiCell set(Boolean arg) {
        Object old = get();
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg);
        }
        updateRow();
        getSheet().cellValueChanged(this, old, arg);
        return this;
    }

    @Override
    @Deprecated
    public PoiCell set(Date arg) {
        Object old = get();
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg);
            if (!isCellDateFormatted()) {
                // Excel does not have a cell type for dates!
                // Warn if cell is not date formatted
                LOGGER.warning("Cell is not date formatted!");
            }
        }
        updateRow();
        getSheet().cellValueChanged(this, old, arg);
        return this;
    }

    @Override
    public PoiCell set(LocalDateTime arg) {
        Object old = get();
        if (arg == null) {
            clear();
        } else {
            Date d = Date.from(arg.atZone(ZoneId.systemDefault()).toInstant());
            poiCell.setCellValue(d);
            if (!isCellDateFormatted()) {
                // Excel does not have a cell type for dates!
                // Warn if cell is not date formatted
                LOGGER.warning("Cell is not date formatted!");
            }
        }
        updateRow();
        getSheet().cellValueChanged(this, old, arg);
        return this;
    }

    @Override
    public PoiCell set(Object arg) {
        MejaHelper.set(this, arg);
        return this;
    }

    @SuppressWarnings("deprecation")
    private boolean isCellDateFormatted() {
        /*
         * DateUtil.isCellDateFormatted() throws IllegalStateException
         * when cell is not numeric, so we have to work around this.
         * TODO create SCCSE and report bug against POI
         */
        org.apache.poi.ss.usermodel.CellType poiType = poiCell.getCellTypeEnum();
        if (poiType == org.apache.poi.ss.usermodel.CellType.FORMULA) {
            poiType = poiCell.getCachedFormulaResultTypeEnum();
        }
        return poiType == org.apache.poi.ss.usermodel.CellType.NUMERIC
                && DateUtil.isCellDateFormatted(poiCell);
    }

    @Override
    public PoiCell set(Number arg) {
        Object old = get();
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg.doubleValue());
            if (isCellDateFormatted()) {
                // Excel does not have a cell type for dates!
                // Warn if cell is date formatted, but a plain number is stored
                LOGGER.warning("Cell is date formatted, but plain number written!");
            }
        }
        updateRow();
        getSheet().cellValueChanged(this, old, null);
        return this;
    }

    @Override
    public PoiCell set(String arg) {
        Object old = get();
        poiCell.setCellValue(arg);
        updateRow();
        getSheet().cellValueChanged(this, old, null);
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public PoiCell setFormula(String arg) {
        Object old = get();
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellFormula(arg);
            poiCell.setCellType(org.apache.poi.ss.usermodel.CellType.FORMULA);
            final PoiWorkbook wb = getWorkbook();
            if(wb.isFormulaEvaluationSupported()) {
                wb.evaluator.evaluateFormulaCell(poiCell);
            }
        }
        updateRow();
        getSheet().cellValueChanged(this, old, null);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PoiCell) {
            return Objects.equals(poiCell, ((PoiCell) obj).poiCell);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return poiCell.hashCode();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isEmpty() {
        switch (poiCell.getCellTypeEnum()) {
            case BLANK:
                return true;
            case STRING:
                return poiCell.getStringCellValue().isEmpty();
            default:
                return false;
        }
    }

    @Override
    public void setCellStyle(CellStyle cellStyle) {
        if (cellStyle instanceof PoiCellStyle) {
            Object old = getCellStyle();
            poiCell.setCellStyle(((PoiCellStyle) cellStyle).poiCellStyle);
            getSheet().cellStyleChanged(this, old, cellStyle);
        } else {
            throw new IllegalArgumentException("Incompatible implementation.");
        }
    }

    @Override
    public void setStyle(String cellStyleName) {
        setCellStyle(getWorkbook().getCellStyle(cellStyleName));
    }

    @Override
    public PoiCellStyle getCellStyle() {
        return workbook.getPoiCellStyle(poiCell.getCellStyle());
    }

    private PoiFont getFontForFormattingRun(RichTextString richText, int i) {
        if (richText instanceof HSSFRichTextString) {
            HSSFRichTextString hssfRichText = (HSSFRichTextString) richText;
            return ((PoiWorkbook.PoiHssfWorkbook) workbook).getFont(hssfRichText.getFontOfFormattingRun(i));
        } else {
            return workbook.getFont(((XSSFRichTextString) richText).getFontOfFormattingRun(i));
        }
    }

    public RichText toRichText(RichTextString rts) {
        String text = rts.getString();
        //TODO: properly process tabs
        text = text.replace('\t', ' '); // tab
        text = text.replace((char) 160, ' '); // non-breaking space

        RichTextBuilder rtb = new RichTextBuilder();
        int start = 0;
        for (int i = 0; i < rts.numFormattingRuns(); i++) {
            start = rts.getIndexOfFormattingRun(i);
            int end = i + 1 < rts.numFormattingRuns() ? rts.getIndexOfFormattingRun(i + 1) : rts.length();

            if (start == end) {
                // skip empty
                continue;
            }

            // apply font attributes for formatting run
            PoiFont runFont = getFontForFormattingRun(rts, i);
            rtb.push(Style.FONT_FAMILY, runFont.getFamily());
            rtb.push(Style.FONT_SIZE, runFont.getSizeInPoints() + "pt");
            rtb.push(Style.COLOR, runFont.getColor().toString());
            if (runFont.isBold()) {
                rtb.push(Style.FONT_WEIGHT, "bold");
            }
            if (runFont.isItalic()) {
                rtb.push(Style.FONT_STYLE, "italic");
            }
            if (runFont.isUnderlined()) {
                rtb.push(Style.TEXT_DECORATION, "underline");
            }
            if (runFont.isStrikeThrough()) {
                rtb.push(Style.TEXT_DECORATION, "line-through");
            }

            rtb.append(text, start, end);
            start = end;
        }
        rtb.append(text, start, text.length());

        return rtb.toRichText();
    }

    @SuppressWarnings("deprecation") // because use of set(java.util.Date)
    @Override
    public void copy(Cell other) {
        setStyle(other.getCellStyle().getName());

        switch (other.getCellType()) {
            case BLANK:
                clear();
                break;
            case BOOLEAN:
                set(other.getBoolean());
                break;
            case ERROR:
                // FIXME
                setFormula("1/0");
                break;
            case FORMULA:
                setFormula(other.getFormula());
                break;
            case NUMERIC:
                set(other.getNumber());
                break;
            case DATE:
                set(other.getDate());
                break;
            case TEXT:
                set(other.getText());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported Cell Type: "+other.getCellType());
        }
    }

    /**
     * Update sheet data, ie. first and last cell numbers.
     */
    private void updateRow() {
        if (getCellType() != CellType.BLANK) {
            getRow().setColumnUsed(getColumnNumber());
        }
    }

    void addedToMergedRegion(PoiCell topLeftCell, int spanX, int spanY) {
        if (this.getRowNumber() == topLeftCell.getRowNumber()
                && this.getColumnNumber() == topLeftCell.getColumnNumber()) {
            this.logicalCell = topLeftCell;
            this.spanX = spanX;
            this.spanY = spanY;
        } else {
            clear();
            this.logicalCell = topLeftCell;
            this.spanX = 0;
            this.spanY = 0;
        }
    }

    void removedFromMergedRegion() {
        this.logicalCell = this;
        this.spanX = 1;
        this.spanY = 1;
    }

    @Override
    public void unMerge() {
        if (logicalCell != this) {
            // this should never happen because we checked for this cell being
            // the top left cell of the merged region
            throw new IllegalArgumentException("Cell is not top left cell of a merged region");
        }

        getSheet().removeMergedRegion(getRowNumber(), getColumnNumber());
    }

    @Override
    public String getCellRef(RefOption... options) {
        return MejaHelper.getCellRef(this, options);
    }

}
