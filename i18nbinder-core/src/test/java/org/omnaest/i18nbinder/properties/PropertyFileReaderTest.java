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

import java.util.regex.Matcher;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;


/**
 *
 * @author mherrn
 */
public class PropertyFileReaderTest {

  @Test
  public void testLinePattern_EqualsSeparatorWithoutSpaces() {
    final Matcher matcher= PropertyFileReader.PATTERN_PROPERTY_LINE.matcher("key1=value1");
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("LEADINGSPACE")).isEmpty();
    assertThat(matcher.group("KEY")).isEqualTo("key1");
    assertThat(matcher.group("SEPARATOR")).isEqualTo("=");
    assertThat(matcher.group("VALUE")).isEqualTo("value1");
    assertThat(matcher.group("TRAILINGSPACE")).isEmpty();
  }

  @Test
  public void testLinePattern_CollonSeparatorWithoutSpaces() {
    final Matcher matcher= PropertyFileReader.PATTERN_PROPERTY_LINE.matcher("key1:value1");
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("LEADINGSPACE")).isEmpty();
    assertThat(matcher.group("KEY")).isEqualTo("key1");
    assertThat(matcher.group("SEPARATOR")).isEqualTo(":");
    assertThat(matcher.group("VALUE")).isEqualTo("value1");
    assertThat(matcher.group("TRAILINGSPACE")).isEmpty();
  }

  @Test
  public void testLinePattern_SingleSpaceSeparator() {
    final Matcher matcher= PropertyFileReader.PATTERN_PROPERTY_LINE.matcher("key1 value1");
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("LEADINGSPACE")).isEmpty();
    assertThat(matcher.group("KEY")).isEqualTo("key1");
    assertThat(matcher.group("SEPARATOR")).isEqualTo(" ");
    assertThat(matcher.group("VALUE")).isEqualTo("value1");
    assertThat(matcher.group("TRAILINGSPACE")).isEmpty();
  }

  @Test
  public void testLinePattern_MultipleSpaceSeparator() {
    final Matcher matcher= PropertyFileReader.PATTERN_PROPERTY_LINE.matcher("key1\t  value1");
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("LEADINGSPACE")).isEmpty();
    assertThat(matcher.group("KEY")).isEqualTo("key1");
    assertThat(matcher.group("SEPARATOR")).isEqualTo("\t  ");
    assertThat(matcher.group("VALUE")).isEqualTo("value1");
    assertThat(matcher.group("TRAILINGSPACE")).isEmpty();
  }

  @Test
  public void testLinePattern_EqualsSeparatorWithLotsOfSpaces() {
    final Matcher matcher= PropertyFileReader.PATTERN_PROPERTY_LINE.matcher(" \tkey1\t=  value1 \t ");
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("LEADINGSPACE")).isEqualTo(" \t");
    assertThat(matcher.group("KEY")).isEqualTo("key1");
    assertThat(matcher.group("SEPARATOR")).isEqualTo("\t=  ");
    assertThat(matcher.group("VALUE")).isEqualTo("value1");
    assertThat(matcher.group("TRAILINGSPACE")).isEqualTo(" \t ");
  }


  @Test
  public void testLinePattern_WithContinuation() {
    final Matcher matcher= PropertyFileReader.PATTERN_PROPERTY_LINE.matcher(" \tkey1\t=  value1 \t \\");
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("LEADINGSPACE")).isEqualTo(" \t");
    assertThat(matcher.group("KEY")).isEqualTo("key1");
    assertThat(matcher.group("SEPARATOR")).isEqualTo("\t=  ");
    assertThat(matcher.group("VALUE")).isEqualTo("value1 \t \\");
    assertThat(matcher.group("TRAILINGSPACE")).isEmpty();
  }
}
