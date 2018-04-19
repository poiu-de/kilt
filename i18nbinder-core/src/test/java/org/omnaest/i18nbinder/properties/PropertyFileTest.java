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
package org.omnaest.i18nbinder.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;


/**
 *
 * @author mherrn
 */
public class PropertyFileTest {

  @Test
  public void test_CompareFullExample() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1=valueA1\n"
      + " keyA2  =  valueA2\n"
      + "\tkeyA3\t=\tvalue A3\t\n"
      + "keyA4 = very long\\\n"
      + "value A4 over \\\n"
      + "multiple lines\n"
      + "        \n"
      + "keyB1:valueB1\n"
      + " keyB2 : valueB2\n"
      + "\t keyB3\t:\t value B3 \n"
      + "keyB4 : very long\\\n"
      + "value B4 over \\\n"
      + "multiple lines\\\n"
      + "\n"
      + "keyC1 valueC1\n"
      + "  keyC2   valueC2\n"
      + "\t keyC3\t\tvalue C3 \n"
      + "keyC4   very long\\\n"
      + "value C4 over \\\n"
      + "\t \tmultiple lines");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(12);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA3")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA4")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyB1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyB2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyB3")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyB4")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyC1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyC2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyC3")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyC4")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");
    assertThat(javaUtilProperties.getProperty("keyA3")).as("Check assumption about java.util.Properties values").isEqualTo("value A3\t");
    assertThat(javaUtilProperties.getProperty("keyA4")).as("Check assumption about java.util.Properties values").isEqualTo("very longvalue A4 over multiple lines");
    assertThat(javaUtilProperties.getProperty("keyB1")).as("Check assumption about java.util.Properties values").isEqualTo("valueB1");
    assertThat(javaUtilProperties.getProperty("keyB2")).as("Check assumption about java.util.Properties values").isEqualTo("valueB2");
    assertThat(javaUtilProperties.getProperty("keyB3")).as("Check assumption about java.util.Properties values").isEqualTo("value B3 ");
    assertThat(javaUtilProperties.getProperty("keyB4")).as("Check assumption about java.util.Properties values").isEqualTo("very longvalue B4 over multiple lines");
    assertThat(javaUtilProperties.getProperty("keyC1")).as("Check assumption about java.util.Properties values").isEqualTo("valueC1");
    assertThat(javaUtilProperties.getProperty("keyC2")).as("Check assumption about java.util.Properties values").isEqualTo("valueC2");
    assertThat(javaUtilProperties.getProperty("keyC3")).as("Check assumption about java.util.Properties values").isEqualTo("value C3 ");
    assertThat(javaUtilProperties.getProperty("keyC4")).as("Check assumption about java.util.Properties values").isEqualTo("very longvalue C4 over multiple lines");

    // - execution
    final PropertyFile readPropertyFile= PropertyFileReader.read(propertyFile);

    // - validation
    assertThat(readPropertyFile.size()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("key %s contains value %s", key, value).isTrue();
    }
  }


  @Test
  public void test_SeparatorCharInValue() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "keyA1 =  valueA1\n"
        + "keyA2 = value A=2\n"
        + "keyA3 : value A:3\n"
        + "keyA4   value A 4");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(4);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA3")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA4")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("value A=2");
    assertThat(javaUtilProperties.getProperty("keyA3")).as("Check assumption about java.util.Properties values").isEqualTo("value A:3");
    assertThat(javaUtilProperties.getProperty("keyA4")).as("Check assumption about java.util.Properties values").isEqualTo("value A 4");

    // - execution
    final PropertyFile readPropertyFile= PropertyFileReader.read(propertyFile);

    // - validation
    assertThat(readPropertyFile.size()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("key %s contains value %s", key, value).isTrue();
    }
  }


  @Test
  public void test_MultilinesWithAdditionalSeparatorChars() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "   keyA1 =  my very \t \\\n"
      + "   long value that \\\n"
      + "   \tspans several lines = \\\n"
      + " and contains = characters \t \n"
      + "keyA2 = some simple value \t ");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("my very \t long value that spans several lines = and contains = characters \t ");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("some simple value \t ");

    // - execution
    final PropertyFile readPropertyFile= PropertyFileReader.read(propertyFile);

    // - validation
    assertThat(readPropertyFile.size()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      System.out.println("'"+key+"' = '"+value+"'");
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  private File createTestFile(final String content) {
    try {
      final File propertyTestFile= File.createTempFile("propertyFile", ".properties");
      propertyTestFile.deleteOnExit();

      try (final PrintWriter pw= new PrintWriter(propertyTestFile);) {
        pw.println(content);
      }

      return propertyTestFile;
    } catch (IOException ex) {
      throw new RuntimeException("Error in test preparation", ex);
    }
  }
}
