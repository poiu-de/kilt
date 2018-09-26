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
import de.poiu.kilt.internal.xls.I18nBundleKey;
import de.poiu.kilt.internal.xls.XlsFile;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;


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

    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, UTF_8, MissingKeyAction.NOTHING);

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

    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, UTF_8, MissingKeyAction.NOTHING);

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

    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, UTF_8, MissingKeyAction.DELETE);

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

    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, UTF_8, MissingKeyAction.COMMENT);

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

    XlsImExporter.importXls(propertiesRootDirectory, xlsFile, UTF_8, MissingKeyAction.NOTHING);

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



  private File copyResourceToTmp(final String resourceName) throws IOException {
    final URL resource= Resources.getResource(resourceName);
    final File targetFile= new File(this.tmpFolder.getRoot(), resourceName);
    targetFile.getParentFile().mkdirs();

    Files.copy(resource.openStream(), targetFile.toPath());
    return targetFile;
  }
}
