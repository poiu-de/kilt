/*
 * Copyright (C) 2018 Marco Herrn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.poiu.kilt.importexport.xls;

import de.poiu.fez.Require;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.apache.poi.ss.usermodel.AutoFilter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;


/**
 * A thin wrapper around a {@link Sheet} to provide handy helper methods.
 * <p>
 * At the moment the only additional method is {@link #getStringValue(int, int)}.
 *
 * @author mherrn
 */
public class SheetWrapper {
  /** The wrapped Sheet. */
  private final Sheet wrapped;


  /**
   * Create a new SheetWrapper wrapping the given Sheet.
   * @param wrapped the sheet to wrap
   */
  public SheetWrapper(final Sheet wrapped) {
    Require.nonNull(wrapped);
    this.wrapped= wrapped;
  }


  /**
   * Returns the content of the cell specified by its row index and column index as String.
   * <p>
   * The returned is optional is empty if either the row or column doesn't exist or the actual
   * cell value is null.
   *
   * @param rowIdx the row index
   * @param colIdx the column index
   * @return an Optional with the actual cell content as string
   */
  public Optional<String> getStringValue(final int rowIdx, final int colIdx) {
    final Row row= this.wrapped.getRow(rowIdx);
    if (row == null) {
      return Optional.empty();
    }

    final Cell cell= row.getCell(colIdx);
    if (cell == null) {
      return Optional.empty();
    }

    return Optional.of(cell.getStringCellValue());
  }


  public Row createRow(int rownum) {
    return wrapped.createRow(rownum);
  }


  public void removeRow(Row row) {
    wrapped.removeRow(row);
  }


  public Row getRow(int rownum) {
    return wrapped.getRow(rownum);
  }


  public int getPhysicalNumberOfRows() {
    return wrapped.getPhysicalNumberOfRows();
  }


  public int getFirstRowNum() {
    return wrapped.getFirstRowNum();
  }


  public int getLastRowNum() {
    return wrapped.getLastRowNum();
  }


  public void setColumnHidden(int columnIndex, boolean hidden) {
    wrapped.setColumnHidden(columnIndex, hidden);
  }


  public boolean isColumnHidden(int columnIndex) {
    return wrapped.isColumnHidden(columnIndex);
  }


  public void setRightToLeft(boolean value) {
    wrapped.setRightToLeft(value);
  }


  public boolean isRightToLeft() {
    return wrapped.isRightToLeft();
  }


  public void setColumnWidth(int columnIndex, int width) {
    wrapped.setColumnWidth(columnIndex, width);
  }


  public int getColumnWidth(int columnIndex) {
    return wrapped.getColumnWidth(columnIndex);
  }


  public void setDefaultColumnWidth(int width) {
    wrapped.setDefaultColumnWidth(width);
  }


  public int getDefaultColumnWidth() {
    return wrapped.getDefaultColumnWidth();
  }


  public short getDefaultRowHeight() {
    return wrapped.getDefaultRowHeight();
  }


  public float getDefaultRowHeightInPoints() {
    return wrapped.getDefaultRowHeightInPoints();
  }


  public void setDefaultRowHeight(short height) {
    wrapped.setDefaultRowHeight(height);
  }


  public void setDefaultRowHeightInPoints(float height) {
    wrapped.setDefaultRowHeightInPoints(height);
  }


  public CellStyle getColumnStyle(int column) {
    return wrapped.getColumnStyle(column);
  }


  public int addMergedRegion(CellRangeAddress region) {
    return wrapped.addMergedRegion(region);
  }


  public void setVerticallyCenter(boolean value) {
    wrapped.setVerticallyCenter(value);
  }


  public void setHorizontallyCenter(boolean value) {
    wrapped.setHorizontallyCenter(value);
  }


  public boolean getHorizontallyCenter() {
    return wrapped.getHorizontallyCenter();
  }


  public boolean getVerticallyCenter() {
    return wrapped.getVerticallyCenter();
  }


  public void removeMergedRegion(int index) {
    wrapped.removeMergedRegion(index);
  }


  public int getNumMergedRegions() {
    return wrapped.getNumMergedRegions();
  }


  public CellRangeAddress getMergedRegion(int index) {
    return wrapped.getMergedRegion(index);
  }


  public Iterator<Row> rowIterator() {
    return wrapped.rowIterator();
  }


  public void setForceFormulaRecalculation(boolean value) {
    wrapped.setForceFormulaRecalculation(value);
  }


  public boolean getForceFormulaRecalculation() {
    return wrapped.getForceFormulaRecalculation();
  }


  public void setAutobreaks(boolean value) {
    wrapped.setAutobreaks(value);
  }


  public void setDisplayGuts(boolean value) {
    wrapped.setDisplayGuts(value);
  }


  public void setDisplayZeros(boolean value) {
    wrapped.setDisplayZeros(value);
  }


  public boolean isDisplayZeros() {
    return wrapped.isDisplayZeros();
  }


  public void setFitToPage(boolean value) {
    wrapped.setFitToPage(value);
  }


  public void setRowSumsBelow(boolean value) {
    wrapped.setRowSumsBelow(value);
  }


  public void setRowSumsRight(boolean value) {
    wrapped.setRowSumsRight(value);
  }


  public boolean getAutobreaks() {
    return wrapped.getAutobreaks();
  }


  public boolean getDisplayGuts() {
    return wrapped.getDisplayGuts();
  }


  public boolean getFitToPage() {
    return wrapped.getFitToPage();
  }


  public boolean getRowSumsBelow() {
    return wrapped.getRowSumsBelow();
  }


  public boolean getRowSumsRight() {
    return wrapped.getRowSumsRight();
  }


  public boolean isPrintGridlines() {
    return wrapped.isPrintGridlines();
  }


  public void setPrintGridlines(boolean show) {
    wrapped.setPrintGridlines(show);
  }


  public PrintSetup getPrintSetup() {
    return wrapped.getPrintSetup();
  }


  public Header getHeader() {
    return wrapped.getHeader();
  }


  public Footer getFooter() {
    return wrapped.getFooter();
  }


  public void setSelected(boolean value) {
    wrapped.setSelected(value);
  }


  public double getMargin(short margin) {
    return wrapped.getMargin(margin);
  }


  public void setMargin(short margin, double size) {
    wrapped.setMargin(margin, size);
  }


  public boolean getProtect() {
    return wrapped.getProtect();
  }


  public void protectSheet(String password) {
    wrapped.protectSheet(password);
  }


  public boolean getScenarioProtect() {
    return wrapped.getScenarioProtect();
  }


  public void setZoom(int i) {
    wrapped.setZoom(i);
  }


  public short getTopRow() {
    return wrapped.getTopRow();
  }


  public short getLeftCol() {
    return wrapped.getLeftCol();
  }


  public void showInPane(short toprow, short leftcol) {
    wrapped.showInPane(toprow, leftcol);
  }


  public void shiftRows(int startRow, int endRow, int n) {
    wrapped.shiftRows(startRow, endRow, n);
  }


  public void shiftRows(int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight) {
    wrapped.shiftRows(startRow, endRow, n, copyRowHeight, resetOriginalRowHeight);
  }


  public void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow) {
    wrapped.createFreezePane(colSplit, rowSplit, leftmostColumn, topRow);
  }


  public void createFreezePane(int colSplit, int rowSplit) {
    wrapped.createFreezePane(colSplit, rowSplit);
  }


  public void createSplitPane(int xSplitPos, int ySplitPos, int leftmostColumn, int topRow, int activePane) {
    wrapped.createSplitPane(xSplitPos, ySplitPos, leftmostColumn, topRow, activePane);
  }


  public PaneInformation getPaneInformation() {
    return wrapped.getPaneInformation();
  }


  public void setDisplayGridlines(boolean show) {
    wrapped.setDisplayGridlines(show);
  }


  public boolean isDisplayGridlines() {
    return wrapped.isDisplayGridlines();
  }


  public void setDisplayFormulas(boolean show) {
    wrapped.setDisplayFormulas(show);
  }


  public boolean isDisplayFormulas() {
    return wrapped.isDisplayFormulas();
  }


  public void setDisplayRowColHeadings(boolean show) {
    wrapped.setDisplayRowColHeadings(show);
  }


  public boolean isDisplayRowColHeadings() {
    return wrapped.isDisplayRowColHeadings();
  }


  public void setRowBreak(int row) {
    wrapped.setRowBreak(row);
  }


  public boolean isRowBroken(int row) {
    return wrapped.isRowBroken(row);
  }


  public void removeRowBreak(int row) {
    wrapped.removeRowBreak(row);
  }


  public int[] getRowBreaks() {
    return wrapped.getRowBreaks();
  }


  public int[] getColumnBreaks() {
    return wrapped.getColumnBreaks();
  }


  public void setColumnBreak(int column) {
    wrapped.setColumnBreak(column);
  }


  public boolean isColumnBroken(int column) {
    return wrapped.isColumnBroken(column);
  }


  public void removeColumnBreak(int column) {
    wrapped.removeColumnBreak(column);
  }


  public void setColumnGroupCollapsed(int columnNumber, boolean collapsed) {
    wrapped.setColumnGroupCollapsed(columnNumber, collapsed);
  }


  public void groupColumn(int fromColumn, int toColumn) {
    wrapped.groupColumn(fromColumn, toColumn);
  }


  public void ungroupColumn(int fromColumn, int toColumn) {
    wrapped.ungroupColumn(fromColumn, toColumn);
  }


  public void groupRow(int fromRow, int toRow) {
    wrapped.groupRow(fromRow, toRow);
  }


  public void ungroupRow(int fromRow, int toRow) {
    wrapped.ungroupRow(fromRow, toRow);
  }


  public void setRowGroupCollapsed(int row, boolean collapse) {
    wrapped.setRowGroupCollapsed(row, collapse);
  }


  public void setDefaultColumnStyle(int column, CellStyle style) {
    wrapped.setDefaultColumnStyle(column, style);
  }


  public void autoSizeColumn(int column) {
    wrapped.autoSizeColumn(column);
  }


  public void autoSizeColumn(int column, boolean useMergedCells) {
    wrapped.autoSizeColumn(column, useMergedCells);
  }


  public Comment getCellComment(CellAddress ref) {
    return wrapped.getCellComment(ref);
  }


  public Drawing createDrawingPatriarch() {
    return wrapped.createDrawingPatriarch();
  }


  public Workbook getWorkbook() {
    return wrapped.getWorkbook();
  }


  public String getSheetName() {
    return wrapped.getSheetName();
  }


  public boolean isSelected() {
    return wrapped.isSelected();
  }


  public void validateMergedRegions() {
    wrapped.validateMergedRegions();
  }


  public float getColumnWidthInPixels(int columnIndex) {
    return wrapped.getColumnWidthInPixels(columnIndex);
  }


  public int addMergedRegionUnsafe(CellRangeAddress region) {
    return wrapped.addMergedRegionUnsafe(region);
  }


  public void removeMergedRegions(Collection<Integer> indices) {
    wrapped.removeMergedRegions(indices);
  }


  public List<CellRangeAddress> getMergedRegions() {
    return wrapped.getMergedRegions();
  }


  public boolean isPrintRowAndColumnHeadings() {
    return wrapped.isPrintRowAndColumnHeadings();
  }


  public void setPrintRowAndColumnHeadings(boolean show) {
    wrapped.setPrintRowAndColumnHeadings(show);
  }


  public void showInPane(int toprow, int leftcol) {
    wrapped.showInPane(toprow, leftcol);
  }


  public void shiftColumns(int startColumn, int endColumn, int n) {
    wrapped.shiftColumns(startColumn, endColumn, n);
  }


  public Map<CellAddress, ? extends Comment> getCellComments() {
    return wrapped.getCellComments();
  }


  public Drawing<?> getDrawingPatriarch() {
    return wrapped.getDrawingPatriarch();
  }


  public List<? extends DataValidation> getDataValidations() {
    return wrapped.getDataValidations();
  }


  public CellRangeAddress getRepeatingRows() {
    return wrapped.getRepeatingRows();
  }


  public CellRangeAddress getRepeatingColumns() {
    return wrapped.getRepeatingColumns();
  }


  public void setRepeatingRows(CellRangeAddress rowRangeRef) {
    wrapped.setRepeatingRows(rowRangeRef);
  }


  public void setRepeatingColumns(CellRangeAddress columnRangeRef) {
    wrapped.setRepeatingColumns(columnRangeRef);
  }


  public int getColumnOutlineLevel(int columnIndex) {
    return wrapped.getColumnOutlineLevel(columnIndex);
  }


  public Hyperlink getHyperlink(int row, int column) {
    return wrapped.getHyperlink(row, column);
  }


  public Hyperlink getHyperlink(CellAddress addr) {
    return wrapped.getHyperlink(addr);
  }


  public List<? extends Hyperlink> getHyperlinkList() {
    return wrapped.getHyperlinkList();
  }


  public CellAddress getActiveCell() {
    return wrapped.getActiveCell();
  }


  public void setActiveCell(CellAddress address) {
    wrapped.setActiveCell(address);
  }


  public CellRange<? extends Cell> setArrayFormula(String formula, CellRangeAddress range) {
    return wrapped.setArrayFormula(formula, range);
  }


  public CellRange<? extends Cell> removeArrayFormula(Cell cell) {
    return wrapped.removeArrayFormula(cell);
  }


  public DataValidationHelper getDataValidationHelper() {
    return wrapped.getDataValidationHelper();
  }


  public void addValidationData(DataValidation dataValidation) {
    wrapped.addValidationData(dataValidation);
  }


  public AutoFilter setAutoFilter(CellRangeAddress range) {
    return wrapped.setAutoFilter(range);
  }


  public SheetConditionalFormatting getSheetConditionalFormatting() {
    return wrapped.getSheetConditionalFormatting();
  }


  public Iterator<Row> iterator() {
    return wrapped.iterator();
  }


  public void forEach(Consumer<? super Row> action) {
    wrapped.forEach(action);
  }


  public Spliterator<Row> spliterator() {
    return wrapped.spliterator();
  }


  @Override
  public int hashCode() {
    return wrapped.hashCode();
  }


  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SheetWrapper) {
      final SheetWrapper other= (SheetWrapper) obj;
      return wrapped.equals(other.wrapped);
    } else {
      return wrapped.equals(obj);
    }
  }


  @Override
  public String toString() {
    return wrapped.toString();
  }


}
