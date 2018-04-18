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
package org.omnaest.i18nbinder.internal;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

/**
 * Encapsulates a single translated value for a specific language.
 */
public class Translation implements Comparable<Translation> {

  /** The language of this translation. */
  private final Language lang;

  /** The actual translated string. */
  private final String value;

  /**
   * Creates a new Translation for the given language and with the given translated
   * value.
   *
   * @param lang the language of this translation
   * @param value the translated value
   */
  public Translation(final Language lang, final String value) {
    this.lang= lang;
    this.value= value;
  }


  /**
   * Returns the language of this translation
   * @return the language of this translation
   */
  public Language getLang() {
    return lang;
  }


  /**
   * Returns the translated value
   * @return the translated value
   */
  public String getValue() {
    return value;
  }


  @Override
  public String toString() {
    return "Translation{" + "lang=" + lang + ", value=" + value + '}';
  }


  @Override
  public int compareTo(Translation o) {
    return ComparisonChain.start()
      .compare(this.getLang(), o.getLang(), Ordering.natural().nullsFirst())
      .compare(this.getValue(), o.getValue(), Ordering.natural().nullsFirst())
      .result();
  }
}
