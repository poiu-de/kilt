/*
 * Copyright (C) 2019 Marco Herrn
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
package de.poiu.kilt.facade.creation;

import de.poiu.kilt.bundlecontent.Language;
import de.poiu.kilt.bundlecontent.Translation;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;


/**
 *
 * @author mherrn
 */
public class TranslationComparatorTest {

  private static final TranslationComparator TRANSLATION_COMPARATOR= TranslationComparator.INSTANCE;

  @Test
  public void testCompare_DE_EN() {
    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("de"), "de"),
      new Translation(Language.of("en"), "en")
    )).isLessThan(0);

    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("en"), "en"),
      new Translation(Language.of("de"), "de")
    )).isGreaterThan(0);
  }


  @Test
  public void testCompare_EMPTY_DE() {
    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of(""), "empty"),
      new Translation(Language.of("de"), "de")
    )).isLessThan(0);

    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("de"), "de"),
      new Translation(Language.of(""), "empty")
    )).isGreaterThan(0);
  }


  @Test
  public void testCompare_UNDERSCORE_DE() {
    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("_"), "underscore"),
      new Translation(Language.of("de"), "de")
    )).isLessThan(0);

    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("de"), "de"),
      new Translation(Language.of("_"), "underscore")
    )).isGreaterThan(0);
  }


  @Test
  public void testCompare_DEFAULT_DE() {
    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("<default>"), "default"),
      new Translation(Language.of("de"), "de")
    )).isLessThan(0);

    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("de"), "de"),
      new Translation(Language.of("<default>"), "default")
    )).isGreaterThan(0);
  }


  @Test
  public void testCompare_EMPTY_UNDERSCORE() {
    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of(""), "empty"),
      new Translation(Language.of("_"), "underscore")
    )).isLessThan(0);

    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("_"), "underscore"),
      new Translation(Language.of(""), "empty")
    )).isGreaterThan(0);
  }


  @Test
  public void testCompare_EMPTY_DEFAULT() {
    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of(""), "empty"),
      new Translation(Language.of("<default>"), "default")
    )).isLessThan(0);

    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("<default>"), "default"),
      new Translation(Language.of(""), "empty")
    )).isGreaterThan(0);
  }


  @Test
  public void testCompare_UNDERSCORE_DEFAULT() {
    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("_"), "underscore"),
      new Translation(Language.of("<default>"), "default")
    )).isLessThan(0);

    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("<default>"), "default"),
      new Translation(Language.of("_"), "underscore")
    )).isGreaterThan(0);
  }


  @Test
  public void testCompare_DE_DE() {
    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("de"), "de"),
      new Translation(Language.of("de"), "de")
    )).isEqualTo(0);
  }


  @Test
  public void testCompare_DE_DE_AT() {
    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("de"), "de"),
      new Translation(Language.of("de_AT"), "de_AT")
    )).isLessThan(0);

    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("de_AT"), "de_AT"),
      new Translation(Language.of("de"), "de")
    )).isGreaterThan(0);
  }


  @Test
  public void testCompare_DE_DE_differentValue() {
    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("de"), "de"),
      new Translation(Language.of("de"), "zzz")
    )).isLessThan(0);

    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("de"), "zzz"),
      new Translation(Language.of("de"), "de")
    )).isGreaterThan(0);
  }


  @Test
  public void testCompare_with_self() {
    final Translation t= new Translation(Language.of("de"), "de");

    assertThat(TRANSLATION_COMPARATOR.compare(
      t,
      t
    )).isEqualTo(0);
  }


  @Test
  public void testCompare_DE_NULL() {
    assertThat(TRANSLATION_COMPARATOR.compare(
      new Translation(Language.of("de"), "de"),
      null
    )).isLessThan(0);

    assertThat(TRANSLATION_COMPARATOR.compare(
      null,
      new Translation(Language.of("de"), "de")
    )).isGreaterThan(0);
  }


  @Test
  public void testCompare_NULL_NULL() {
    assertThat(TRANSLATION_COMPARATOR.compare(
      null,
      null
    )).isEqualTo(0);
  }

}
