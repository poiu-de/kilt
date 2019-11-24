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
import de.poiu.fez.Require;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

/**
 * Container for a language.
 * <p>
 * This is only a dumb wrapper. It does not recognize whether 'de' and 'deu' are actually the
 * same language and be therefore treated as different languages.
 * <p>
 * This is a thread-safe immutable class.
 */
@Immutable
public class Language implements Comparable<Language> {
  final String lang;

  /**
   * Creates a new instance of this class for the given language string.
   * @param lang the language string for this Language
   */
  public Language(final String lang) {
    Require.nonNull(lang);
    this.lang= lang;
  }

  /**
   * Creates a new instance of this class for the given language string.
   * @param lang the language string for this Language
   * @return the Language for the given language string
   */
  public static Language of(final String lang) {
    Require.nonNull(lang);
    return new Language(lang);
  }

  /**
   * Returns the language string for this Language
   * @return the language string for this Language
   */
  public String getLang() {
    return lang;
  }

  @Override
  public int compareTo(Language o) {
    return ComparisonChain.start()
      .compare(this.getLang(), o.getLang(), Ordering.natural().nullsFirst())
      .result();
  }


  @Override
  public int hashCode() {
    int hash = 7;
    hash = 73 * hash + Objects.hashCode(this.lang);
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
    final Language other = (Language) obj;
    if (!Objects.equals(this.lang, other.lang)) {
      return false;
    }
    return true;
  }


  @Override
  public String toString() {
    return lang;
  }
}
