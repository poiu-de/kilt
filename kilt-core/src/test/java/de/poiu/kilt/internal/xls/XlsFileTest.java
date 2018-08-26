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
package de.poiu.kilt.internal.xls;

import de.poiu.kilt.internal.Language;
import de.poiu.kilt.internal.Translation;
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

}
