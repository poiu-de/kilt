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
package de.poiu.kilt.bundlecontent;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import java.util.Objects;

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
  public int hashCode() {
    int hash = 7;
    hash = 41 * hash + Objects.hashCode(this.lang);
    hash = 41 * hash + Objects.hashCode(this.value);
    return hash;
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Translation other = (Translation) obj;
    if (!Objects.equals(this.value, other.value)) {
      return false;
    }
    if (!Objects.equals(this.lang, other.lang)) {
      return false;
    }
    return true;
  }





  @Override
  public int compareTo(Translation o) {
    return ComparisonChain.start()
      .compare(this.getLang(), o.getLang(), Ordering.natural().nullsFirst())
      .compare(this.getValue(), o.getValue(), Ordering.natural().nullsFirst())
      .result();
  }
}
