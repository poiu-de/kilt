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
package de.poiu.kilt.importexport;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import de.poiu.apron.MissingKeyAction;
import de.poiu.apron.PropertyFile;
import de.poiu.kilt.bundlecontent.Translation;
import de.poiu.kilt.importexport.xls.I18nBundleKey;
import de.poiu.kilt.importexport.xls.XlsFile;
import de.poiu.kilt.util.FileMatcher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;


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

    final FileMatcher fileMatcher= this.createFileMatcher(propertiesRootDirectory, resourceBundleFiles);

    final File xlsFile= this.tmpFolder.newFile("i18n.xlsx");
    xlsFile.delete();

    // - test

    XlsImExporter.exportXls(fileMatcher, UTF_8, xlsFile);

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

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    final File xlsFile= new File(Resources.getResource("i18n_expected.xlsx").toURI());

    // - test

    XlsImExporter.importXls(fileMatcher, xlsFile, UTF_8, MissingKeyAction.NOTHING);

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
  public void testImportXls_OnlySpecifiedIncluded() throws IOException, URISyntaxException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.newFolder().toPath();

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/messages_de.properties"}, new String[]{""});

    final File xlsFile= new File(Resources.getResource("i18n_expected.xlsx").toURI());

    // - test

    XlsImExporter.importXls(fileMatcher, xlsFile, UTF_8, MissingKeyAction.NOTHING);

    // - verification

    final Path[] writtenResourceBundleFiles = Files.list(propertiesRootDirectory.resolve("i18n")).toArray(Path[]::new);
    assertThat(writtenResourceBundleFiles).containsOnly(
      propertiesRootDirectory.resolve("i18n/messages_de.properties")
    );
    assertThat(propertiesRootDirectory.resolve("i18n").resolve("messages_de.properties").toFile())
      .hasSameContentAs(propertiesRootDirectory.resolve("i18n").resolve("messages_de.properties").toFile());
  }


  @Test
  public void testImportXls_WithoutSpecifiedExcluded() throws IOException, URISyntaxException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.newFolder().toPath();

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{"**/*_de.properties"});

    final File xlsFile= new File(Resources.getResource("i18n_expected.xlsx").toURI());

    // - test

    XlsImExporter.importXls(fileMatcher, xlsFile, UTF_8, MissingKeyAction.NOTHING);

    // - verification

    final Path[] writtenResourceBundleFiles = Files.list(propertiesRootDirectory.resolve("i18n")).toArray(Path[]::new);
    assertThat(writtenResourceBundleFiles).containsOnly(
      propertiesRootDirectory.resolve("i18n/messages.properties")
    );
    assertThat(propertiesRootDirectory.resolve("i18n").resolve("messages.properties").toFile())
      .hasSameContentAs(propertiesRootDirectory.resolve("i18n").resolve("messages.properties").toFile());
  }


  @Test
  public void testImportXls_AddedLanguageColumn() throws IOException, URISyntaxException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.newFolder().toPath();

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    final File xlsFile= new File(Resources.getResource("i18n_added_fr.xlsx").toURI());

    // - test

    XlsImExporter.importXls(fileMatcher, xlsFile, UTF_8, MissingKeyAction.NOTHING);

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

    final FileMatcher fileMatcher= this.createFileMatcher(propertiesRootDirectory, resourceBundleFiles);

    final File xlsFile= new File(Resources.getResource("i18n_line_deleted.xlsx").toURI());

    // - test

    XlsImExporter.importXls(fileMatcher, xlsFile, UTF_8, MissingKeyAction.DELETE);

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

    final FileMatcher fileMatcher= this.createFileMatcher(propertiesRootDirectory, resourceBundleFiles);

    final File xlsFile= new File(Resources.getResource("i18n_line_deleted.xlsx").toURI());

    // - test

    XlsImExporter.importXls(fileMatcher, xlsFile, UTF_8, MissingKeyAction.COMMENT);

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

    final FileMatcher fileMatcher= this.createFileMatcher(propertiesRootDirectory, resourceBundleFiles);

    final File xlsFile= new File(Resources.getResource("i18n_line_deleted.xlsx").toURI());

    // - test

    XlsImExporter.importXls(fileMatcher, xlsFile, UTF_8, MissingKeyAction.NOTHING);

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

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    final File xlsFile= new File(Resources.getResource("libreoffice_nullValues.xlsx").toURI());

    // - test

    XlsImExporter.importXls(fileMatcher, xlsFile, UTF_8, MissingKeyAction.NOTHING);

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

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    final File xlsFile= new File(Resources.getResource("libreoffice_emptyStrings.xlsx").toURI());

    // - test

    XlsImExporter.importXls(fileMatcher, xlsFile, UTF_8, MissingKeyAction.NOTHING);

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

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    final File xlsFile= new File(Resources.getResource("libreoffice_emptyStrings.xlsx").toURI());

    final Path buttons_fr= this.createFile(propertiesRootDirectory.resolve("buttons_fr.properties"),
                                           "btn.cancel = to be overwritten\n");
    final Path butons_nl= this.createFile(propertiesRootDirectory.resolve("buttons_nl.properties"),
                                          "btn.cancel = to be overwritten\n");
    final Path items_nl= this.createFile(propertiesRootDirectory.resolve("items_nl.properties"),
                                         " = to be overwritten\n");

    // - test

    XlsImExporter.importXls(fileMatcher, xlsFile, UTF_8, MissingKeyAction.NOTHING);

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


  private FileMatcher createFileMatcher(Path propertiesRootDirectory, Set<File> resourceBundleFiles) {
    final List<String> includes= new ArrayList<>(resourceBundleFiles.size());
    for (final File f : resourceBundleFiles) {
      includes.add(f.getPath());
    }

    return new FileMatcher(propertiesRootDirectory, includes, Collections.EMPTY_LIST);
  }
}
