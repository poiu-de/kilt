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
package de.poiu.kilt.reformatting;

import com.google.common.collect.ImmutableSet;
import de.poiu.apron.reformatting.AttachCommentsTo;
import de.poiu.apron.reformatting.InvalidFormatException;
import de.poiu.kilt.util.FileMatcher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.contentOf;


/**
 *
 * @author mherrn
 */
public class KiltReformatterTest {

  @Rule
  public TemporaryFolder tmpFolder= new TemporaryFolder();



  @Test
  public void testReformat_differentFormat() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "key1 = value1\n"
      + "key2 : value2\r"
      + "key3   value3\r\n");
    final File f2= createI18nBundle(propertiesRootDirectory, ""
      + "  key1 =  value1\n"
      + " key2 :   value2\r"
      + "\tkey3    value3\r\n");

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reformat(fileMatcher, "<key>\\t=\\t<value>\\n", false, UTF_8);

    // - verification

    assertThat(contentOf(f1)).isEqualTo(""
      + "key1\t=\tvalue1\n"
      + "key2\t=\tvalue2\n"
      + "key3\t=\tvalue3\n");
    assertThat(contentOf(f2)).isEqualTo(""
      + "key1\t=\tvalue1\n"
      + "key2\t=\tvalue2\n"
      + "key3\t=\tvalue3\n");
  }


  @Test
  public void testReformat_invalidFormat() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "key1 = value1\n"
      + "key2 : value2\r"
      + "key3   value3\r\n");
    final File f2= createI18nBundle(propertiesRootDirectory, ""
      + "  key1 =  value1\n"
      + " key2 :   value2\r"
      + "\tkey3    value3\r\n");

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution
    // - verification

    assertThatExceptionOfType(InvalidFormatException.class)
      .isThrownBy(() -> {
        new KiltReformatter().reformat(fileMatcher, "<key> = <valueismissing>\\n", false, UTF_8);});

  }


  @Test
  public void testReformat_multilineProperties() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "\tkey\\ \\\n"
      + "  one\\\n"
      + "  : value \\\n"
      + "    1\n"
      + "key\\ \\\n"
      + "  two\\\r"
      + "  = \t value \\\r"
      + "    2\n"
    );

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reformat(fileMatcher, "<key> = <value>\\n", false, UTF_8);

    // - verification

    assertThat(contentOf(f1)).isEqualTo(""
      + "key\\ \\\n"
      + "  one\\\n"
      + " = value \\\n"
      + "    1\n"
      + "key\\ \\\n"
      + "  two\\\r"
      + " = value \\\r"
      + "    2\n"
    );
  }


  @Test
  public void testReformat_multilineProperties_ToSingleLine() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "\tkey\\ \\\n"
      + "  one\\\n"
      + "  : value \\\n"
      + "    1\n"
      + "key\\ \\\n"
      + "  two\\\r"
      + "  = \t value \\\r"
      + "    2\n"
    );

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reformat(fileMatcher, "<key> = <value>\\n", true, UTF_8);

    // - verification

    System.out.println("SOLL:\n"
      + "key one = value 1\n"
      + "key two = value 2\n"
    );
    System.out.println("IST:\n"
      + contentOf(f1));

    assertThat(contentOf(f1)).isEqualTo(""
      + "key\\ one = value 1\n"
      + "key\\ two = value 2\n"
    );
  }


  @Test
  public void testReorder_noFiles() throws IOException {

    // - preparation

    final FileMatcher fileMatcher= new FileMatcher(Paths.get(""), new String[]{""}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reorderByKey(fileMatcher, AttachCommentsTo.NEXT_PROPERTY, UTF_8);

    //TODO: Verify that warning message gets logged?

    // - verification
  }


  @Test
  public void testReorder_orderByLanguage() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
    );
    final File f2= createI18nBundle(propertiesRootDirectory, ""
      + "keyE = valueE\n"
      + "keyD = valueD\n"
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
    );

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reorderByTemplate(f1, fileMatcher, AttachCommentsTo.NEXT_PROPERTY, UTF_8);

    // - verification

    assertThat(contentOf(f1)).isEqualTo(""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
    );
    assertThat(contentOf(f2)).isEqualTo(""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
    );
  }


  @Test
  @Ignore(value = "Reordering multiple files at one is currently not implemented. "
    + "The reason is that the current format for i18include doesn't really allow an order of the "
    + "input files.")
  public void testReorder_orderByLanguage_multipleFiles() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
    );
    final File f2= createI18nBundle(propertiesRootDirectory, ""
      + "keyE = valueE\n"
      + "keyD = valueD\n"
      + "keyC = valueC\n"
      + "keyZ = valueZ\n"
      + "keyB = valueB\n"
      + "keyY = valueY\n"
      + "keyA = valueA\n"
    );final File f3= createI18nBundle(propertiesRootDirectory, ""
      + "keyY = valueY\n"
      + "keyE = valueE\n"
      + "keyC = valueC\n"
      + "keyA = valueA\n"
      + "keyZ = valueZ\n"
    );

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reorderByTemplate(f1, fileMatcher, AttachCommentsTo.NEXT_PROPERTY, UTF_8);

    // - verification

    assertThat(contentOf(f1)).isEqualTo(""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
    );
    assertThat(contentOf(f2)).isEqualTo(""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
      + "keyZ = valueZ\n"
      + "keyY = valueY\n"
    );
    assertThat(contentOf(f3)).isEqualTo(""
      + "keyC = valueC\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyZ = valueZ\n"
      + "keyY = valueY\n"
    );
  }


  @Test
  public void testReorder_orderByLanguage_multipleFiles_Separately() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
    );
    final File f2= createI18nBundle(propertiesRootDirectory, ""
      + "keyE = valueE\n"
      + "keyD = valueD\n"
      + "keyC = valueC\n"
      + "keyZ = valueZ\n"
      + "keyB = valueB\n"
      + "keyY = valueY\n"
      + "keyA = valueA\n"
    );final File f3= createI18nBundle(propertiesRootDirectory, ""
      + "keyY = valueY\n"
      + "keyE = valueE\n"
      + "keyC = valueC\n"
      + "keyA = valueA\n"
      + "keyZ = valueZ\n"
    );

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reorderByTemplate(f1, fileMatcher, AttachCommentsTo.NEXT_PROPERTY, UTF_8);
    new KiltReformatter()
      .reorderByTemplate(f2, fileMatcher, AttachCommentsTo.NEXT_PROPERTY, UTF_8);

    // - verification

    assertThat(contentOf(f1)).isEqualTo(""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
    );
    assertThat(contentOf(f2)).isEqualTo(""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
      + "keyZ = valueZ\n"
      + "keyY = valueY\n"
    );
    assertThat(contentOf(f3)).isEqualTo(""
      + "keyC = valueC\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyZ = valueZ\n"
      + "keyY = valueY\n"
    );
  }


  @Test
  public void testReorder_orderByLanguage_SameFileTwice() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
    );
    final File f2= createI18nBundle(propertiesRootDirectory, ""
      + "keyE = valueE\n"
      + "keyD = valueD\n"
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
    );

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reorderByTemplate(f1, fileMatcher, AttachCommentsTo.NEXT_PROPERTY, UTF_8);

    // - verification

    assertThat(contentOf(f1)).isEqualTo(""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
    );
    assertThat(contentOf(f2)).isEqualTo(""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
    );
  }


  @Test
  public void testReorder_orderByLanguage_NothingInCommon() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
    );
    final File f2= createI18nBundle(propertiesRootDirectory, ""
      + "keyZ = valueZ\n"
      + "keyY = valueY\n"
      + "keyX = valueX\n"
    );

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reorderByTemplate(f1, fileMatcher, AttachCommentsTo.NEXT_PROPERTY, UTF_8);

    // - verification

    assertThat(contentOf(f1)).isEqualTo(""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
    );
    assertThat(contentOf(f2)).isEqualTo(""
      + "keyZ = valueZ\n"
      + "keyY = valueY\n"
      + "keyX = valueX\n"
    );
  }


  @Test
  public void testReorder_orderByName() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
      + "keyE = valueE\n"
      + "keyD = valueD\n"
    );
    final File f2= createI18nBundle(propertiesRootDirectory, ""
      + "keyE = valueE\n"
      + "keyD = valueD\n"
      + "keyC = valueC\n"
      + "keyB = valueB\n"
      + "keyA = valueA\n"
    );

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reorderByKey(fileMatcher, AttachCommentsTo.NEXT_PROPERTY, UTF_8);

    // - verification

    assertThat(contentOf(f1)).isEqualTo(""
      + "keyA = valueA\n"
      + "keyB = valueB\n"
      + "keyC = valueC\n"
      + "keyD = valueD\n"
      + "keyE = valueE\n"
    );
    assertThat(contentOf(f2)).isEqualTo(""
      + "keyA = valueA\n"
      + "keyB = valueB\n"
      + "keyC = valueC\n"
      + "keyD = valueD\n"
      + "keyE = valueE\n"
    );
  }


  @Test
  public void testReorder_attachCommentToNext() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "# Comment 1\n"
      + "key_F = F\n"
      + "key_L = L\n"
      + "\n"
      + "# Comment 2\n"
      + "key_B = B\n"
      + "# Comment 3\n"
      + "key_A = A\n"
    );

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reorderByKey(fileMatcher, AttachCommentsTo.NEXT_PROPERTY, UTF_8);

    // - verification

    assertThat(contentOf(f1)).isEqualTo(""
      + "# Comment 3\n"
      + "key_A = A\n"
      + "\n"
      + "# Comment 2\n"
      + "key_B = B\n"
      + "# Comment 1\n"
      + "key_F = F\n"
      + "key_L = L\n"
    );
  }


  @Test
  public void testReorder_attachCommentToPrev() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "# Comment 1\n"
      + "key_F = F\n"
      + "key_L = L\n"
      + "\n"
      + "# Comment 2\n"
      + "key_B = B\n"
      + "# Comment 3\n"
      + "key_A = A\n"
    );

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reorderByKey(fileMatcher, AttachCommentsTo.PREV_PROPERTY, UTF_8);

    // - verification

    assertThat(contentOf(f1)).isEqualTo(""
      + "# Comment 1\n"
      + "key_A = A\n"
      + "key_B = B\n"
      + "# Comment 3\n"
      + "key_F = F\n"
      + "key_L = L\n"
      + "\n"
      + "# Comment 2\n"
    );
  }


  @Test
  public void testReorder_attachCommentToLine() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "# Comment 1\n"
      + "key_F = F\n"
      + "key_L = L\n"
      + "\n"
      + "# Comment 2\n"
      + "key_B = B\n"
      + "# Comment 3\n"
      + "key_A = A\n"
    );

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reorderByKey(fileMatcher, AttachCommentsTo.ORIG_LINE, UTF_8);

    // - verification

    assertThat(contentOf(f1)).isEqualTo(""
      + "# Comment 1\n"
      + "key_A = A\n"
      + "key_B = B\n"
      + "\n"
      + "# Comment 2\n"
      + "key_F = F\n"
      + "# Comment 3\n"
      + "key_L = L\n"
    );
  }


  @Test
  public void testReorder_multilineProperties() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "key\\ \\\n"
      + "  2\\\n"
      + "  = value \\\n"
      + "    2\n"
      + "\n"
      + " # some comment\n"
      + "key\\ \\\n"
      + "  1\\\n"
      + "  = value \\\n"
      + "    1\n"
      + "key\\ \\\n"
      + "  3\\\n"
      + "  = value \\\n"
      + "    3\n"
    );

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    new KiltReformatter()
      .reorderByKey(fileMatcher, AttachCommentsTo.ORIG_LINE, UTF_8);

    // - verification

    assertThat(contentOf(f1)).isEqualTo(""
      + "key\\ \\\n"
      + "  1\\\n"
      + "  = value \\\n"
      + "    1\n"
      + "\n"
      + " # some comment\n"
      + "key\\ \\\n"
      + "  2\\\n"
      + "  = value \\\n"
      + "    2\n"
      + "key\\ \\\n"
      + "  3\\\n"
      + "  = value \\\n"
      + "    3\n"
    );
  }


  @Test
  public void testReformatAndReorder() throws IOException {

    // - preparation

    final Path propertiesRootDirectory= this.tmpFolder.getRoot().toPath();
    final File f1= createI18nBundle(propertiesRootDirectory, ""
      + "# Comment 1\n"
      + "key_F = F\n"
      + "key_L = L\n"
      + "\n"
      + "# Comment 2\n"
      + "key_B = B\n"
      + "# Comment 3\n"
      + "key_A = A\n"
    );

    final FileMatcher fileMatcher= new FileMatcher(propertiesRootDirectory, new String[]{"**/*.properties"}, new String[]{""});

    // - execution

    final KiltReformatter reformatter= new KiltReformatter();
    reformatter.reformat(fileMatcher, "\\t<key> : <value>\\r\\n", false, UTF_8);
    reformatter.reorderByKey(fileMatcher, AttachCommentsTo.NEXT_PROPERTY, UTF_8);

    // - verification

    assertThat(contentOf(f1)).isEqualTo(""
      + "# Comment 3\r\n"
      + "\tkey_A : A\r\n"
      + "\r\n"
      + "# Comment 2\r\n"
      + "\tkey_B : B\r\n"
      + "# Comment 1\r\n"
      + "\tkey_F : F\r\n"
      + "\tkey_L : L\r\n"
    );
  }



  private File createI18nBundle(final Path rootDirectory, final String content) {
    try {
      final File f= File.createTempFile("ReformatterTest", ".properties", rootDirectory.toFile());

      try(final PrintWriter pw= new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), UTF_8));) {
        pw.print(content);
      }

      return f;
    } catch (IOException ex) {
      throw new RuntimeException("Error in test preparation", ex);
    }
  }
}
