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
package de.poiu.kilt.internal;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import de.poiu.apron.MissingKeyAction;
import de.poiu.apron.PropertyFile;
import de.poiu.kilt.internal.xls.I18nBundleKey;
import de.poiu.kilt.internal.xls.XlsFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.Fail;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import static java.nio.charset.StandardCharsets.ISO_8859_1;


/**
 *
 * @author mherrn
 */
public class XlsImExporterTest {

  @Rule
  public TemporaryFolder tmpFolder= new TemporaryFolder();


  @Test
  public void testExportXls() throws URISyntaxException, IOException {

    // - preparation

    //FIXME: Should these resources lie below de.poiu.kilt.internal?
    //       Or even below de.poiu.kilt.internal.XlsImExporterTest?

    final Path propertiesRootDirectory= Paths.get(Resources.getResource("").toURI());
    final Set<File> resourceBundleFiles= ImmutableSet.of(
      propertiesRootDirectory.resolve("i18n/messages.properties").toFile(),
      propertiesRootDirectory.resolve("i18n/messages_de.properties").toFile());

    final File xlsFile= this.tmpFolder.newFile("i18n.xlsx");
    xlsFile.delete();

    // - test

    XlsImExporter.exportXls(propertiesRootDirectory, resourceBundleFiles, UTF_8, xlsFile.toPath());

    // - verification

    final XlsFile readXlsFile= new XlsFile(xlsFile);
    final Map<I18nBundleKey, Collection<Translation>> readContent= readXlsFile.getContent();
    final XlsFile expectedXlsFile= new XlsFile(new File(Resources.getResource("i18n_expected.xlsx").toURI()));
    final Map<I18nBundleKey, Collection<Translation>> expectedContent= expectedXlsFile.getContent();

    assertThat(readContent).hasSameSizeAs(expectedContent);
    assertThat(readContent).containsOnlyKeys(FluentIterable.from(expectedContent.keySet()).toArray(I18nBundleKey.class));
    for (final I18nBundleKey bundleKey : readContent.keySet()) {
      assertThat(readContent.get(bundleKey)).containsOnly(FluentIterable.from(expectedContent.get(bundleKey)).toArray(Translation.class));
    }
    //actually only this check is necessary
    assertThat(readContent).isEqualTo(expectedContent);
  }


  @Test
  public void testImportXls() throws IOException, URISyntaxException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.newFolder().toPath();

    final File xlsFile= new File(Resources.getResource("i18n_expected.xlsx").toURI());

    // - test

    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, Collections.EMPTY_SET, UTF_8, MissingKeyAction.NOTHING);

    // - verification

    final Path[] writtenResourceBundleFiles = Files.list(propertiesRootDirectory.resolve("i18n")).toArray(Path[]::new);
    assertThat(writtenResourceBundleFiles).containsOnly(
      propertiesRootDirectory.resolve("i18n/messages.properties"),
      propertiesRootDirectory.resolve("i18n/messages_de.properties")
    );
    assertThat(propertiesRootDirectory.resolve("i18n").resolve("messages.properties").toFile())
      .hasSameContentAs(propertiesRootDirectory.resolve("i18n").resolve("messages.properties").toFile());
    assertThat(propertiesRootDirectory.resolve("i18n").resolve("messages_de.properties").toFile())
      .hasSameContentAs(propertiesRootDirectory.resolve("i18n").resolve("messages_de.properties").toFile());
  }


  @Test
  public void testImportXls_AddedLanguageColumn() throws IOException, URISyntaxException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.newFolder().toPath();

    final File xlsFile= new File(Resources.getResource("i18n_added_fr.xlsx").toURI());

    // - test

    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, Collections.EMPTY_SET, UTF_8, MissingKeyAction.NOTHING);

    // - verification

    final Path[] writtenResourceBundleFiles = Files.list(propertiesRootDirectory.resolve("i18n")).toArray(Path[]::new);
    assertThat(writtenResourceBundleFiles).containsOnly(
      propertiesRootDirectory.resolve("i18n/messages.properties"),
      propertiesRootDirectory.resolve("i18n/messages_de.properties"),
      propertiesRootDirectory.resolve("i18n/messages_fr.properties")
    );
    assertThat(propertiesRootDirectory.resolve("i18n").resolve("messages.properties").toFile())
      .hasSameContentAs(propertiesRootDirectory.resolve("i18n").resolve("messages.properties").toFile());
    assertThat(propertiesRootDirectory.resolve("i18n").resolve("messages_de.properties").toFile())
      .hasSameContentAs(propertiesRootDirectory.resolve("i18n").resolve("messages_de.properties").toFile());
    assertThat(propertiesRootDirectory.resolve("i18n").resolve("messages_fr.properties").toFile())
      .hasContent("yes = Oui\nno = Non\n");
  }


  @Test
  public void testImportXls_MissingKeys_Delete() throws IOException, URISyntaxException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final Set<File> resourceBundleFiles= ImmutableSet.of(
      copyResourceToTmp("i18n/messages.properties"),
      copyResourceToTmp("i18n/messages_de.properties"));

    final File xlsFile= new File(Resources.getResource("i18n_line_deleted.xlsx").toURI());

    // - test

    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, Collections.EMPTY_SET, UTF_8, MissingKeyAction.DELETE);

    // - verification

    final Path[] writtenResourceBundleFiles = Files.list(propertiesRootDirectory.resolve("i18n")).toArray(Path[]::new);
    assertThat(writtenResourceBundleFiles).containsOnly(
      propertiesRootDirectory.resolve("i18n/messages.properties"),
      propertiesRootDirectory.resolve("i18n/messages_de.properties")
    );
    assertThat(propertiesRootDirectory.resolve("i18n").resolve("messages.properties").toFile())
      .hasContent(""
        + "cancel = Cancel\n"
        + "\n");
    assertThat(propertiesRootDirectory.resolve("i18n").resolve("messages_de.properties").toFile())
      .hasContent(""
        + "cancel = Abbrechen\n"
        + "\n");
  }


  @Test
  public void testImportXls_MissingKeys_CommentOut() throws IOException, URISyntaxException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final Set<File> resourceBundleFiles= ImmutableSet.of(
      copyResourceToTmp("i18n/messages.properties"),
      copyResourceToTmp("i18n/messages_de.properties"));

    final File xlsFile= new File(Resources.getResource("i18n_line_deleted.xlsx").toURI());

    // - test

    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, Collections.EMPTY_SET, UTF_8, MissingKeyAction.COMMENT);

    // - verification

    final Path[] writtenResourceBundleFiles = Files.list(propertiesRootDirectory.resolve("i18n")).toArray(Path[]::new);
    assertThat(writtenResourceBundleFiles).containsOnly(
      propertiesRootDirectory.resolve("i18n/messages.properties"),
      propertiesRootDirectory.resolve("i18n/messages_de.properties")
    );
    assertThat(propertiesRootDirectory.resolve("i18n").resolve("messages.properties").toFile())
      .hasContent(""
        + "#ok = OK\n"
        + "cancel = Cancel\n"
        + "\n");
    assertThat(propertiesRootDirectory.resolve("i18n").resolve("messages_de.properties").toFile())
      .hasContent(""
        + "#ok = OK\n"
        + "cancel = Abbrechen\n"
        + "\n");
  }


  @Test
  public void testImportXls_MissingKeys_DoNothing() throws IOException, URISyntaxException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final Set<File> resourceBundleFiles= ImmutableSet.of(
      copyResourceToTmp("i18n/messages.properties"),
      copyResourceToTmp("i18n/messages_de.properties"));

    final File xlsFile= new File(Resources.getResource("i18n_line_deleted.xlsx").toURI());

    // - test

    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, Collections.EMPTY_SET, UTF_8, MissingKeyAction.NOTHING);

    // - verification

    final Path[] writtenResourceBundleFiles = Files.list(propertiesRootDirectory.resolve("i18n")).toArray(Path[]::new);
    assertThat(writtenResourceBundleFiles).containsOnly(
      propertiesRootDirectory.resolve("i18n/messages.properties"),
      propertiesRootDirectory.resolve("i18n/messages_de.properties")
    );
    assertThat(propertiesRootDirectory.resolve("i18n").resolve("messages.properties").toFile())
      .hasContent(""
        + "ok = OK\n"
        + "cancel = Cancel\n"
        + "\n");
    assertThat(propertiesRootDirectory.resolve("i18n").resolve("messages_de.properties").toFile())
      .hasContent(""
        + "ok = OK\n"
        + "cancel = Abbrechen\n"
        + "\n");
  }


  /**
   * This test verifies the behavior when reading in XLSX files that were generated by LibreOffice.
   * Those may contain "null" entries. See #3: https://github.com/hupfdule/kilt/issues/3
   */
  @Test
  public void testImportXls_LibreOfficeBug() throws IOException, URISyntaxException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.newFolder().toPath();

    final File xlsFile= new File(Resources.getResource("libreoffice_nullValues.xlsx").toURI());

    // - test

    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, Collections.EMPTY_SET, UTF_8, MissingKeyAction.NOTHING);

    // - verification

    final Path[] writtenResourceBundleFiles = Files.list(propertiesRootDirectory).toArray(Path[]::new);
    assertThat(writtenResourceBundleFiles).containsOnly(
      propertiesRootDirectory.resolve("buttons_nl.properties"),
      propertiesRootDirectory.resolve("buttons_de.properties"),
      propertiesRootDirectory.resolve("items_de.properties")
    );

    final PropertyFile pfButtonsDe= PropertyFile.from(propertiesRootDirectory.resolve("buttons_de.properties").toFile());
    assertThat(pfButtonsDe.keys()).containsOnly("btn.enter", "btn.cancel");
    assertThat(pfButtonsDe.get("btn.enter")).isEqualTo("OK");
    assertThat(pfButtonsDe.get("btn.cancel")).isEqualTo("Abbrechen");

    final PropertyFile pfButtonsNl= PropertyFile.from(propertiesRootDirectory.resolve("buttons_nl.properties").toFile());
    assertThat(pfButtonsNl.keys()).containsOnly("btn.enter", "btn.cancel");
    assertThat(pfButtonsNl.get("btn.enter")).isEqualTo("Ok");
    assertThat(pfButtonsNl.get("btn.cancel")).isEqualTo("Annuleren");

    final PropertyFile pfItemDe= PropertyFile.from(propertiesRootDirectory.resolve("items_de.properties").toFile());
    assertThat(pfItemDe.keys()).containsOnly("");
    assertThat(pfItemDe.get("")).isEqualTo("ID");
  }


  /**
   * This test verifies bug #4: https://github.com/hupfdule/kilt/issues/4
   */
  @Test
  public void testImportXls_DoNotCreateEmptyBundleFiles() throws IOException, URISyntaxException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.newFolder().toPath();

    final File xlsFile= new File(Resources.getResource("libreoffice_emptyStrings.xlsx").toURI());

    // - test

    //FIXME: Das ist unsauber. Ein leeres Set für "alles" ist nicht ganz passend
    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, Collections.EMPTY_SET, UTF_8, MissingKeyAction.NOTHING);

    // - verification

    final Path[] writtenResourceBundleFiles = Files.list(propertiesRootDirectory).toArray(Path[]::new);
    assertThat(writtenResourceBundleFiles).containsOnly(
      propertiesRootDirectory.resolve("buttons_nl.properties"),
      propertiesRootDirectory.resolve("buttons_de.properties"),
      propertiesRootDirectory.resolve("items_de.properties")
    );

    final PropertyFile pfButtonsDe= PropertyFile.from(propertiesRootDirectory.resolve("buttons_de.properties").toFile());
    assertThat(pfButtonsDe.keys()).containsOnly("btn.enter", "btn.cancel");
    assertThat(pfButtonsDe.get("btn.enter")).isEqualTo("OK");
    assertThat(pfButtonsDe.get("btn.cancel")).isEqualTo("Abbrechen");

    final PropertyFile pfButtonsNl= PropertyFile.from(propertiesRootDirectory.resolve("buttons_nl.properties").toFile());
    assertThat(pfButtonsNl.keys()).containsOnly("btn.enter", "btn.cancel");
    assertThat(pfButtonsNl.get("btn.enter")).isEqualTo("Ok");
    assertThat(pfButtonsNl.get("btn.cancel")).isEqualTo("Annuleren");

    final PropertyFile pfItemDe= PropertyFile.from(propertiesRootDirectory.resolve("items_de.properties").toFile());
    assertThat(pfItemDe.keys()).containsOnly("");
    assertThat(pfItemDe.get("")).isEqualTo("ID");
  }


  @Test
  @Ignore(value = "This requires larger changes to importXls, as we need to read in the existing files first to compare")
  public void testImportXls_OnlyWriteEmptyValuesIfKeyAlreadyExists() throws IOException, URISyntaxException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.newFolder().toPath();

    final File xlsFile= new File(Resources.getResource("libreoffice_emptyStrings.xlsx").toURI());

    final File buttons_fr= this.createFile(propertiesRootDirectory.resolve("buttons_fr.properties"),
                                           "btn.cancel = to be overwritten\n").toFile();
    final File butons_nl= this.createFile(propertiesRootDirectory.resolve("buttons_nl.properties"),
                                          "btn.cancel = to be overwritten\n").toFile();
    final File items_nl= this.createFile(propertiesRootDirectory.resolve("items_nl.properties"),
                                         " = to be overwritten\n").toFile();

    // - test

    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, new HashSet<>(Arrays.asList(buttons_fr, butons_nl, items_nl)), UTF_8, MissingKeyAction.NOTHING);

    // - verification

    final Path[] writtenResourceBundleFiles = Files.list(propertiesRootDirectory).toArray(Path[]::new);
    assertThat(writtenResourceBundleFiles).containsOnly(
      propertiesRootDirectory.resolve("buttons_nl.properties"),
      propertiesRootDirectory.resolve("buttons_de.properties"),
      propertiesRootDirectory.resolve("buttons_fr.properties"),
      propertiesRootDirectory.resolve("items_de.properties"),
      propertiesRootDirectory.resolve("items_nl.properties")
    );

    final PropertyFile pfButtonsDe= PropertyFile.from(propertiesRootDirectory.resolve("buttons_de.properties").toFile());
    assertThat(pfButtonsDe.keys()).containsOnly("btn.enter", "btn.cancel");
    assertThat(pfButtonsDe.get("btn.enter")).isEqualTo("OK");
    assertThat(pfButtonsDe.get("btn.cancel")).isEqualTo("Abbrechen");

    final PropertyFile pfButtonsNl= PropertyFile.from(propertiesRootDirectory.resolve("buttons_nl.properties").toFile());
    assertThat(pfButtonsNl.keys()).containsOnly("btn.enter", "btn.cancel");
    assertThat(pfButtonsNl.get("btn.enter")).isEqualTo("Ok");
    assertThat(pfButtonsNl.get("btn.cancel")).isEqualTo("Annuleren");

    final PropertyFile pfButtonsFr= PropertyFile.from(propertiesRootDirectory.resolve("buttons_fr.properties").toFile());
    assertThat(pfButtonsFr.keys()).containsOnly("btn.cancel");
    assertThat(pfButtonsFr.keys()).doesNotContain("btn.ok"); // this key was not existand and therefore is not written with an empty value
    assertThat(pfButtonsFr.get("btn.cancel")).isEqualTo("");  // this key was existant and therefore is written again with an empty value

    final PropertyFile pfItemDe= PropertyFile.from(propertiesRootDirectory.resolve("items_de.properties").toFile());
    assertThat(pfItemDe.keys()).containsOnly("");
    assertThat(pfItemDe.get("")).isEqualTo("ID");

    final PropertyFile pfItemNl= PropertyFile.from(propertiesRootDirectory.resolve("items_nl.properties").toFile());
    assertThat(pfItemNl.keys()).containsOnly("");
    assertThat(pfItemNl.get("")).isEqualTo("");

    final PropertyFile pfItemFr= PropertyFile.from(propertiesRootDirectory.resolve("items_fr.properties").toFile());
    assertThat(pfItemFr.keys()).isEmpty();
  }


  @Test
  public void testImportXls_ImportSingleBundle() throws IOException, URISyntaxException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.newFolder().toPath();

    final File xlsFile= this.createXlsFile(toLists(new String[][]{
      {"Bundle Basename", "I18n Key", "de", "en", "de_bayrisch"},
      {"/buttons", "btn.ok", "OK", "OK"},
      {"/buttons", "btn.cancel", "Abbrechen", "Cancel"},
      {"/messages", "msg.hello", "Hallo", "", "Servus"},
      {"/messages", "msg.bye", "Auf Wiedersehen!", "", "Pfuit di!"}
    }));

    // this one should not be altered, although the XLS file contains other content for it
    final File buttons_de= this.createFile(propertiesRootDirectory.resolve("buttons_de.properties"),
                                           "btn.save = Speichern\n").toFile();
    // this one doesn't exist yet, but should be created, since we import it
    final File messages_de= propertiesRootDirectory.resolve("messages_de.properties").toFile();
    // - test

    // import only messages_de
    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, new HashSet<>(Arrays.asList(messages_de)), UTF_8, MissingKeyAction.NOTHING);

    // - verification

    final Path[] writtenResourceBundleFiles = Files.list(propertiesRootDirectory).toArray(Path[]::new);
    assertThat(writtenResourceBundleFiles).containsOnly(
      propertiesRootDirectory.resolve("buttons_de.properties"),
      propertiesRootDirectory.resolve("messages_de.properties")
    );

    final PropertyFile pfButtonsDe= PropertyFile.from(propertiesRootDirectory.resolve("buttons_de.properties").toFile());
    assertThat(pfButtonsDe.keys()).containsOnly("btn.save");
    assertThat(pfButtonsDe.get("btn.save")).isEqualTo("Speichern");

    final PropertyFile pfMessagesDe= PropertyFile.from(propertiesRootDirectory.resolve("messages_de.properties").toFile());
    assertThat(pfMessagesDe.keys()).containsOnly("msg.hello", "msg.bye");
    assertThat(pfMessagesDe.get("msg.hello")).isEqualTo("Hallo");
    assertThat(pfMessagesDe.get("msg.bye")).isEqualTo("Auf Wiedersehen!");
  }


  @Test
  public void testExportXls_AddToExistingData() throws IOException, URISyntaxException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.newFolder().toPath();

    final File xlsFile= this.createXlsFile(toLists(new String[][]{
      {"Bundle Basename", "I18n Key", "de", "en"},
      {"/buttons", "btn.ok", "OK", "OK"},
      {"/buttons", "btn.cancel", "Abbrechen", "Cancel"}
    }));

    final File messages_de= this.createFile(propertiesRootDirectory.resolve("messages_de.properties"), "msg.hello = Hallo\nmsg.bye = Auf Wiedersehen!\n").toFile();
    final File messages_de_bayrisch= this.createFile(propertiesRootDirectory.resolve("messages_de_bayrisch.properties"), "msg.hello = Servus\nmsg.bye = Pfuit di!\n").toFile();

    // - test

    XlsImExporter.exportXls(propertiesRootDirectory, new HashSet<>(Arrays.asList(messages_de, messages_de_bayrisch)), UTF_8, xlsFile.toPath());

    // - verification
    final List<List<String>> contentMatrix= this.getContentMatrix(xlsFile);
    System.out.println(contentMatrix);
    assertThat(contentMatrix).isEqualTo(toLists(new String[][]{
      {"Bundle Basename", "I18n Key", "de", "en", "de_bayrisch"},
      {"/buttons", "btn.ok", "OK", "OK"},
      {"/buttons", "btn.cancel", "Abbrechen", "Cancel"},
      {"/messages", "msg.hello", "Hallo", "", "Servus"},
      {"/messages", "msg.bye", "Auf Wiedersehen!", "", "Pfuit di!"}
    }));
  }


  private File copyResourceToTmp(final String resourceName) throws IOException {
    final URL resource= Resources.getResource(resourceName);
    final File targetFile= new File(this.tmpFolder.getRoot(), resourceName);
    targetFile.getParentFile().mkdirs();

    Files.copy(resource.openStream(), targetFile.toPath());
    return targetFile;
  }


  private Path createFile(final Path pathToFile, final String content) {
    try (final PrintWriter writer= new PrintWriter(
      new OutputStreamWriter(
        new FileOutputStream(pathToFile.toFile()), ISO_8859_1));) {
      writer.print(content);
      return pathToFile;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  private File createXlsFile(final List<List<String>> rows) {
    try {
      final File xlsFile= this.tmpFolder.newFile("test.xlsx");
      final XSSFWorkbook workbook = new XSSFWorkbook();
      final XSSFSheet sheet = workbook.createSheet("i18n");
      int rowIdx= 0;
      int colIdx= 0;
      for (final List<String> rowContent : rows) {
        final XSSFRow row= sheet.createRow(rowIdx++);
        for (final String cellValue : rowContent) {
          final XSSFCell cell= row.createCell(colIdx++);
          cell.setCellValue(workbook.getCreationHelper().createRichTextString(cellValue));
        }
        colIdx= 0;
      }

      try (final FileOutputStream fis= new FileOutputStream(xlsFile)) {
        workbook.write(fis);
      } catch (IOException ex) {
        throw new RuntimeException("Error writing to file "+xlsFile.getAbsolutePath(), ex);
      }

      return xlsFile;
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }


  private List<List<String>> toLists(final String[][] contentMatrix) {
    final List<List<String>> result= new ArrayList<>();
    for (final String[] row : contentMatrix) {
      final List<String> newRow= new ArrayList<>();
      result.add(newRow);
      for (final String cellValue : row) {
        newRow.add(cellValue);
      }
    }
    return result;
  }


  public List<List<String>> getContentMatrix(final File xlsFile) {
    final List<List<String>> rows= new ArrayList<>();

    final Workbook workbook;
    try (final FileInputStream fis= new FileInputStream(xlsFile)) {
      workbook= WorkbookFactory.create(fis);
    } catch (IOException | InvalidFormatException ex) {
      throw new RuntimeException("Error reading XLS data from file " + xlsFile.getAbsolutePath(), ex);
    }

    final Sheet sheet = workbook.getSheet("i18n");

    for (int rowId= sheet.getFirstRowNum(); rowId < sheet.getLastRowNum() + 1; rowId++) {
      final Row row= sheet.getRow(rowId);
      final List<String> resultRow= new ArrayList<>();
      rows.add(resultRow);
      for (int colId= row.getFirstCellNum(); colId < row.getLastCellNum(); colId++) {
        final Cell cell= row.getCell(colId);
        resultRow.add(cell != null ? cell.getStringCellValue() : "");
      }
    }

    return rows;
  }
}
