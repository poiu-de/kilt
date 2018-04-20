/*
 * Copyright 2018 mherrn.
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
package org.omnaest.i18nbinder.internal.xls;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.omnaest.i18nbinder.internal.Language;
import org.omnaest.i18nbinder.internal.Translation;


/**
 * Not thread safe!
 *
 * @author mherrn
 */
public class XlsFile {
  private static final Logger LOGGER= LogManager.getLogger();

  private static final String DEFAULT_I18N_SHEET_NAME = "i18n";


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  private final Workbook workbook;
  private final Sheet i18nSheet;

  private final File file;

  private final BiMap<Language, Integer> languageColumnMap= HashBiMap.create();
  private final BiMap<I18nBundleKey, Integer> i18nKeyRowMap= HashBiMap.create();


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  public XlsFile(final File file) {
    //FIXME: Support file to be null?
    Objects.requireNonNull(file);

    this.file= file;

    this.workbook= this.prepareWorkbook(file);
    //TODO: Support custom sheet name
    this.i18nSheet= this.prepareI18nSheet(this.workbook, DEFAULT_I18N_SHEET_NAME);

    this.init();
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  private Workbook prepareWorkbook(final File inputFile) {
    final Workbook workbook;

    if (inputFile.exists()) {
      LOGGER.log(Level.INFO, "Loading contents from file " + inputFile.getAbsolutePath());
      try (final FileInputStream fis= new FileInputStream(inputFile)) {
        workbook= WorkbookFactory.create(fis);
      } catch (IOException | InvalidFormatException ex) {
        throw new RuntimeException("Error reading XLS data from file " + inputFile.getAbsolutePath(), ex);
      }
    } else {
      LOGGER.log(Level.INFO, "Creating new file " + inputFile.getAbsolutePath());
      if (this.file.getName().endsWith(".xls")) {
        LOGGER.log(Level.INFO, "Create new file in XLS format");
        workbook= new HSSFWorkbook();
      } else {
        LOGGER.log(Level.INFO, "Create new file in XLSX format");
        workbook= new XSSFWorkbook();
      }
    }

    return workbook;
  }


  private Sheet prepareI18nSheet(Workbook workbook, final String sheetName) {
    final Sheet sheet = workbook.getSheet(sheetName);
    if (sheet != null) {
      return sheet;
    } else {
      LOGGER.log(Level.INFO, "No sheet with name " + sheetName + " found. Creating new sheet.");
      return workbook.createSheet(sheetName);
    }
  }


  private void init() {
    LOGGER.traceEntry();
    Row headerRow= this.i18nSheet.getRow(0);

    // if headerRow does not exist, create it!
    if (headerRow == null) {
      headerRow= this.i18nSheet.createRow(0);
      final Cell bundleBasenameCell= headerRow.createCell(0);
      bundleBasenameCell.setCellValue(this.workbook.getCreationHelper().createRichTextString("Bundle Basename"));
      final Cell keyCell= headerRow.createCell(1);
      keyCell.setCellValue(this.workbook.getCreationHelper().createRichTextString("I18n Key"));
    }

    // the first column is the bundleBaseName
    // the seconds column is the i18n key
    // the remaining columns are the languages with the translations

    // store the indexes of the languages columns for easier access
    for (int i= 2; i < headerRow.getLastCellNum(); i++) {
      final Cell cell= headerRow.getCell(i);
      if (cell == null){
        continue;
      } else {
        final String cellValue= cell != null ? cell.getStringCellValue() : "";
        final Language language= Language.of(cellValue);
        if (this.languageColumnMap.containsKey(language)) {
          LOGGER.log(Level.WARN, "Language '" + language.getLang() + "' is found multiple times in file. Only using the first one.");
        } else {
          this.languageColumnMap.put(language, i);
        }
      }
    }

    // store the indexes of the i18n keys rows for easier access
    for (int i= this.i18nSheet.getFirstRowNum() + 1; i < this.i18nSheet.getLastRowNum(); i++) {
      final String baseBundleName= this.i18nSheet.getRow(i).getCell(0).getStringCellValue(); //FIXME Avoid NPE?
      final String key= this.i18nSheet.getRow(i).getCell(1).getStringCellValue(); //FIXME Avoid NPE?
      final I18nBundleKey i18nKey= new I18nBundleKey(baseBundleName, key);

      if (this.i18nKeyRowMap.containsKey(i18nKey)) {
        LOGGER.log(Level.WARN, "I18n Key " + i18nKey + " is found multiple times in file. Only using the first one.");
      } else {
        this.i18nKeyRowMap.put(i18nKey, i);
      }
    }
    LOGGER.traceExit();
  }


  public void setValue(final I18nBundleKey i18nKey, final Language language, final String value) {
    // get or create the row for the key
    final Row row;
    if (this.i18nKeyRowMap.containsKey(i18nKey)) {
      row= this.i18nSheet.getRow(this.i18nKeyRowMap.get(i18nKey));
    } else {
      row= this.i18nSheet.createRow(this.i18nSheet.getLastRowNum() + 1);
      row.createCell(0).setCellValue(this.workbook.getCreationHelper().createRichTextString(i18nKey.getBundleBaseName()));
      row.createCell(1).setCellValue(this.workbook.getCreationHelper().createRichTextString(i18nKey.getKey()));
      this.i18nKeyRowMap.put(i18nKey, row.getRowNum());
    }

    if (!this.languageColumnMap.containsKey(language)) {
      this.appendLanguageColumn(language);
    }

    // update or create the cell value
    final Cell cell;
    final int languageColumnIdx= this.languageColumnMap.get(language);
    if (row.getCell(languageColumnIdx) != null) {
      cell= row.getCell(languageColumnIdx);
    } else {
      cell= row.createCell(languageColumnIdx);
    }
    cell.setCellValue(this.workbook.getCreationHelper().createRichTextString(value));
  }


  public void setValue(final I18nBundleKey i18nKey, final Translation translation) {
    this.setValue(i18nKey, translation.getLang(), translation.getValue());
  }


  public void setValue(final I18nBundleKey i18nKey, final Collection<Translation> translations) {
    for (final Translation translation : translations) {
      this.setValue(i18nKey, translation);
    }
  }


  private void appendLanguageColumn(final Language language) {
    int highestLanguageColumnIdx= 1; // start a index 1, because 0 and 1 are already used for bundle basename and key
    for (final int idx : this.languageColumnMap.values()) {
      highestLanguageColumnIdx= Math.max(highestLanguageColumnIdx, idx);
    }

    final Row headerRow= this.i18nSheet.getRow(0);
    final Cell languageColumn = headerRow.createCell(highestLanguageColumnIdx + 1);
    languageColumn.setCellValue(this.workbook.getCreationHelper().createRichTextString(language.getLang()));

    this.languageColumnMap.put(language, languageColumn.getColumnIndex());
  }


  public void save() {
    try {
      final File tmpFile= File.createTempFile(this.file.getName(), "tmp");
      this.saveTo(tmpFile);
      Files.move(tmpFile, this.file);
    } catch (IOException ex) {
      throw new RuntimeException("Error writing XLS file.", ex);
    }
  }


  public void saveTo(final File file) {
    LOGGER.traceEntry(file.getAbsolutePath());

    try (final FileOutputStream fis= new FileOutputStream(file)) {
      this.workbook.write(fis);
    } catch (IOException ex) {
      throw new RuntimeException("Error writing to file "+file.getAbsolutePath(), ex);
    }

    LOGGER.traceExit();
  }


  public Map<I18nBundleKey, Collection<Translation>> getContent() {
    final Multimap<I18nBundleKey, Translation> contentMap= LinkedHashMultimap.create();

    for (int i= this.i18nSheet.getFirstRowNum() + 1; i < this.i18nSheet.getLastRowNum(); i++) {
      final Row row= this.i18nSheet.getRow(i);
      final String baseBundleName= this.i18nSheet.getRow(i).getCell(0).getStringCellValue(); //FIXME Avoid NPE?
      final String key= this.i18nSheet.getRow(i).getCell(1).getStringCellValue(); //FIXME Avoid NPE?
      final I18nBundleKey i18nKey= new I18nBundleKey(baseBundleName, key);

      for (final Map.Entry<Language, Integer> entry : this.languageColumnMap.entrySet()) {
        final Language language = entry.getKey();
        final Integer columnIdx = entry.getValue();
        final Cell cell= row.getCell(columnIdx);
        if (cell != null) {
          contentMap.put(i18nKey, new Translation(language, cell.getStringCellValue()));
        }
      }
    }

    return contentMap.asMap();
  }
}
