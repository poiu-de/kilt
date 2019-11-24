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
package de.poiu.kilt.importexport.xls;

import de.poiu.kilt.importexport.xls.XlsFile;
import de.poiu.kilt.importexport.xls.I18nBundleKey;
import com.google.common.io.Resources;
import de.poiu.kilt.bundlecontent.Language;
import de.poiu.kilt.bundlecontent.Translation;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;


/**
 *
 * @author mherrn
 */
public class XlsFileTest {


  @Test
  public void testWriteEmptyFile() throws Exception {
    // preparation
    final File testFile= File.createTempFile("XlsFile-test", "xlsx");
    testFile.deleteOnExit();
    testFile.delete();
    final XlsFile xlsFile= new XlsFile(testFile);

    // execution
    xlsFile.save();

    // verification
    final Map<I18nBundleKey, Collection<Translation>> content= xlsFile.getContent();
    assertThat(content).isEmpty();

    final XlsFile writtenFile= new XlsFile(testFile);
    final Map<I18nBundleKey, Collection<Translation>> writtenContent= writtenFile.getContent();
    assertThat(writtenContent).isEqualTo(content);
  }


  @Test
  public void testWriteTwoEntries() throws Exception {
    // preparation
    final File testFile= File.createTempFile("XlsFile-test", "xlsx");
    testFile.deleteOnExit();
    testFile.delete();
    final XlsFile xlsFile= new XlsFile(testFile);

    // execution
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key1"), Language.of("de"), "value1");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key2"), Language.of("de"), "value2");
    xlsFile.save();

    // verification
    final Map<I18nBundleKey, Collection<Translation>> content= xlsFile.getContent();
    assertThat(content).size().isEqualTo(2);
    assertThat(content).containsOnlyKeys(new I18nBundleKey("myBundleBasePath", "key1"), new I18nBundleKey("myBundleBasePath", "key2"));
    assertThat(content.get(new I18nBundleKey("myBundleBasePath", "key1"))).containsExactly(new Translation(Language.of("de"), "value1"));
    assertThat(content.get(new I18nBundleKey("myBundleBasePath", "key2"))).containsExactly(new Translation(Language.of("de"), "value2"));

    final XlsFile writtenFile= new XlsFile(testFile);
    final Map<I18nBundleKey, Collection<Translation>> writtenContent= writtenFile.getContent();
    assertThat(writtenContent).isEqualTo(content);
  }


  @Test
  public void testWriteTwoEntriesTwoLanguages() throws Exception {
    // preparation
    final File testFile= File.createTempFile("XlsFile-test", "xlsx");
    testFile.deleteOnExit();
    testFile.delete();
    final XlsFile xlsFile= new XlsFile(testFile);

    // execution
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key1"), Language.of("de"), "wert1");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key1"), Language.of("en"), "value1");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key2"), Language.of("de"), "wert2");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key2"), Language.of("en"), "value2");
    xlsFile.save();

    // verification
    final Map<I18nBundleKey, Collection<Translation>> content= xlsFile.getContent();
    assertThat(content).size().isEqualTo(2);
    assertThat(content).containsOnlyKeys(new I18nBundleKey("myBundleBasePath", "key1"), new I18nBundleKey("myBundleBasePath", "key2"));
    assertThat(content.get(new I18nBundleKey("myBundleBasePath", "key1"))).containsExactly(
      new Translation(Language.of("de"), "wert1"),
      new Translation(Language.of("en"), "value1"));
    assertThat(content.get(new I18nBundleKey("myBundleBasePath", "key2"))).containsExactly(
      new Translation(Language.of("de"), "wert2"),
      new Translation(Language.of("en"), "value2"));

    final XlsFile writtenFile= new XlsFile(testFile);
    final Map<I18nBundleKey, Collection<Translation>> writtenContent= writtenFile.getContent();
    assertThat(writtenContent).isEqualTo(content);
  }


  @Test
  public void testWriteTwoEntriesTwoLanguagesInTwoBundles() throws Exception {
    // preparation
    final File testFile= File.createTempFile("XlsFile-test", "xlsx");
    testFile.deleteOnExit();
    testFile.delete();
    final XlsFile xlsFile= new XlsFile(testFile);

    // execution
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key1"), Language.of("de"), "wert1");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key1"), Language.of("en"), "value1");
    xlsFile.setValue(new I18nBundleKey("otherBundle", "key2"), Language.of("de"), "wert2");
    xlsFile.setValue(new I18nBundleKey("otherBundle", "key2"), Language.of("en"), "value2");
    xlsFile.save();

    // verification
    final Map<I18nBundleKey, Collection<Translation>> content= xlsFile.getContent();
    assertThat(content).size().isEqualTo(2);
    assertThat(content).containsOnlyKeys(new I18nBundleKey("myBundleBasePath", "key1"), new I18nBundleKey("otherBundle", "key2"));
    assertThat(content.get(new I18nBundleKey("myBundleBasePath", "key1"))).containsExactly(
      new Translation(Language.of("de"), "wert1"),
      new Translation(Language.of("en"), "value1"));
    assertThat(content.get(new I18nBundleKey("otherBundle", "key2"))).containsExactly(
      new Translation(Language.of("de"), "wert2"),
      new Translation(Language.of("en"), "value2"));

    final XlsFile writtenFile= new XlsFile(testFile);
    final Map<I18nBundleKey, Collection<Translation>> writtenContent= writtenFile.getContent();
    assertThat(writtenContent).isEqualTo(content);
  }


  @Test
  public void testWriteDuplicateValue() throws Exception {
    // preparation
    final File testFile= File.createTempFile("XlsFile-test", "xlsx");
    testFile.deleteOnExit();
    testFile.delete();
    final XlsFile xlsFile= new XlsFile(testFile);

    // execution
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key1"), Language.of("de"), "wert1");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key1"), Language.of("en"), "value1");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key2"), Language.of("de"), "wert2");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key2"), Language.of("en"), "value2");
    // now set a different value to key1-en
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key1"), Language.of("en"), "someOtherValue1");
    xlsFile.save();

    // verification
    final Map<I18nBundleKey, Collection<Translation>> content= xlsFile.getContent();
    assertThat(content).size().isEqualTo(2);
    assertThat(content).containsOnlyKeys(new I18nBundleKey("myBundleBasePath", "key1"), new I18nBundleKey("myBundleBasePath", "key2"));
    assertThat(content.get(new I18nBundleKey("myBundleBasePath", "key1"))).containsExactly(
      new Translation(Language.of("de"), "wert1"),
      new Translation(Language.of("en"), "someOtherValue1"));
    assertThat(content.get(new I18nBundleKey("myBundleBasePath", "key2"))).containsExactly(
      new Translation(Language.of("de"), "wert2"),
      new Translation(Language.of("en"), "value2"));

    final XlsFile writtenFile= new XlsFile(testFile);
    final Map<I18nBundleKey, Collection<Translation>> writtenContent= writtenFile.getContent();
    assertThat(writtenContent).isEqualTo(content);
  }


  @Test
  public void testEmptyCellsAndDefaultLanguage() throws Exception {
    // preparation
    final File testFile= File.createTempFile("XlsFile-test", "xlsx");
    testFile.deleteOnExit();
    testFile.delete();
    final XlsFile xlsFile= new XlsFile(testFile);

    // execution
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key1"), Language.of(""), "wert1");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key1"), Language.of("en"), "value1");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key2"), Language.of(""), "wert2");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key2"), Language.of("en"), "value2");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key3"), Language.of(""), "wert3");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key4"), Language.of("en"), "value4");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key5"), Language.of(""), "");
    xlsFile.save();

    // verification
    final Map<I18nBundleKey, Collection<Translation>> content= xlsFile.getContent();
    assertThat(content).size().isEqualTo(5);
    assertThat(content).containsOnlyKeys(
      new I18nBundleKey("myBundleBasePath", "key1"),
      new I18nBundleKey("myBundleBasePath", "key2"),
      new I18nBundleKey("myBundleBasePath", "key3"),
      new I18nBundleKey("myBundleBasePath", "key4"),
      new I18nBundleKey("myBundleBasePath", "key5"));
    assertThat(content.get(new I18nBundleKey("myBundleBasePath", "key1"))).containsExactly(
      new Translation(Language.of(""), "wert1"),
      new Translation(Language.of("en"), "value1"));
    assertThat(content.get(new I18nBundleKey("myBundleBasePath", "key2"))).containsExactly(
      new Translation(Language.of(""), "wert2"),
      new Translation(Language.of("en"), "value2"));
    assertThat(content.get(new I18nBundleKey("myBundleBasePath", "key3"))).containsExactly(
      new Translation(Language.of(""), "wert3"));
    assertThat(content.get(new I18nBundleKey("myBundleBasePath", "key4"))).containsExactly(
      new Translation(Language.of("en"), "value4"));
    assertThat(content.get(new I18nBundleKey("myBundleBasePath", "key5"))).containsExactly(
      new Translation(Language.of(""), ""));

    final XlsFile writtenFile= new XlsFile(testFile);
    final Map<I18nBundleKey, Collection<Translation>> writtenContent= writtenFile.getContent();
    assertThat(writtenContent).isEqualTo(content);
  }


  @Test
  public void testMultiLineValue() throws Exception {
    // preparation
    final File testFile= File.createTempFile("XlsFile-test", "xlsx");
    testFile.deleteOnExit();
    testFile.delete();
    final XlsFile xlsFile= new XlsFile(testFile);

    // execution
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key1"), Language.of("de"), "Dieser Text \ngeht über mehrere \nZeilen.");
    xlsFile.setValue(new I18nBundleKey("myBundleBasePath", "key1"), Language.of("en"), "This text \nspan multiple \nlines.");
    xlsFile.save();

    // verification
    final Map<I18nBundleKey, Collection<Translation>> content= xlsFile.getContent();
    assertThat(content).size().isEqualTo(1);
    assertThat(content).containsOnlyKeys(
      new I18nBundleKey("myBundleBasePath", "key1"));
    assertThat(content.get(new I18nBundleKey("myBundleBasePath", "key1"))).containsExactly(
      new Translation(Language.of("de"), "Dieser Text \ngeht über mehrere \nZeilen."),
      new Translation(Language.of("en"), "This text \nspan multiple \nlines."));

    final XlsFile writtenFile= new XlsFile(testFile);
    final Map<I18nBundleKey, Collection<Translation>> writtenContent= writtenFile.getContent();
    assertThat(writtenContent).isEqualTo(content);
  }


  @Test
  public void testRead_WithEmptyDefaultLanguage() throws Exception {
    // - preparation
    final File file= new File(Resources.getResource("i18n_expected.xlsx").toURI());

    // - execution
    final XlsFile xlsFile= new XlsFile(file);

    // - verification
    final Map<I18nBundleKey, Collection<Translation>> content= xlsFile.getContent();

    assertThat(content).containsOnlyKeys(new I18nBundleKey("i18n/messages", "ok"), new I18nBundleKey("i18n/messages", "cancel"));
    assertThat(content.get(new I18nBundleKey("i18n/messages", "ok"))).containsOnly(
      new Translation(Language.of("de"), "OK"),
      new Translation(Language.of(""), "OK")
    );
    assertThat(content.get(new I18nBundleKey("i18n/messages", "cancel"))).containsOnly(
      new Translation(Language.of("de"), "Abbrechen"),
      new Translation(Language.of(""), "Cancel")
    );
  }
}
