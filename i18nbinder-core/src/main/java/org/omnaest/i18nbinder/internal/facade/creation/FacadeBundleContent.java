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
package org.omnaest.i18nbinder.internal.facade.creation;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import javax.annotation.concurrent.Immutable;
import org.omnaest.utils.propertyfile.PropertyFile;
import org.omnaest.utils.propertyfile.content.PropertyMap;
import org.omnaest.utils.propertyfile.content.element.Property;


/**
 * A holder for all information related to a localization resource bundle.
 * <p>
 * It contains a name for the bundle (actually the bundleBasename) and all keys with their
 * translations.
 * <p>
 * This is a thread-safe immutable class.
 * <p>
 * This class provides a fluent interface. An example to create it and fill it with translations
 * is:
 * <pre>
 *   final FacadeBundleContent facadeBundleContent= FacadeBundleContent.forName("myBundle")
 *                                                                     .fromFriles(myFilesMap);
 * </pre>
 *
 * To create the map with the content files, the class {@link FacadeBundleContentHelper} can be used.
 *
 * @author mherrn
 */
public class FacadeBundleContent {

  /**
   * Container for a language.
   * <p>
   * This is only a dumb wrapper. It does not recognize whether 'de' and 'deu' are actually the
   * same language and be therefore treated as different languages.
   * <p>
   * This is a thread-safe immutable class.
   */
  @Immutable
  public static class Language implements Comparable<Language> {
    final String lang;

    /**
     * Creates a new instance of this class for the given language string.
     * @param lang the language string for this Language
     */
    public Language(final String lang) {
      Objects.requireNonNull(lang);
      this.lang= lang;
    }

    /**
     * Creates a new instance of this class for the given language string.
     * @param lang the language string for this Language
     * @return the Language for the given language string
     */
    public static Language of(final String lang) {
      Objects.requireNonNull(lang);
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


  /**
   * Encapsulates a single translated value for a specific language.
   */
  public static class Translation implements Comparable<Translation> {
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


  /** Comparator to compare Translations by their language and value. */
  public static final Comparator<Translation> TRANSLATION_COMPARATOR= (Translation o1, Translation o2) -> ComparisonChain.start()
    .compare(o1.getLang(), o2.getLang(), Ordering.natural().nullsFirst())
    .compare(o1.getValue(), o2.getValue(), Ordering.natural().nullsFirst())
    .result();


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /** The bundleBasename of this bundle. */
  private final String bundleName;

  /** All keys of this bundle and their available translations. */
  private final Multimap<String, Translation> content;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new FacadeBundleContent for the given bundle with the given translations.
   *
   * @param bundleName the bundleBasename
   * @param content all keys and their translations for this bundle
   */
  private FacadeBundleContent(final String bundleName, final Multimap<String, Translation> content) {
    this.bundleName= bundleName;
    //FIXME: Sollte das irgendwie sortiert sein? Alphabetisch? Oder nach der gelesenen Reihenfolge?
    this.content= ImmutableMultimap.copyOf(content);
  }

  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods


  /**
   * Returns the bundleBasename.
   *
   * @return the bundleBasename
   */
  public String getBundleName() {
    return bundleName;
  }


  /**
   * Returns the keys and their translations for this bundle.
   *
   * @return a Multimap containing the translated keys as the keys in the map and all translations
   * for that key as the corresponding values.
   */
  public Multimap<String, Translation> getContent() {
    return content;
  }



  /**
   * Creates a new FacadeBundleContent for the given bundle without any translations.
   * To fill it with translations use the method {@link #fromFiles(java.util.Map) ).
   *
   * @param bundleName the bundleBasename
   * @return a new FacadeBundleContent for the the given bundle
   */
  public static FacadeBundleContent forName(final String bundleName) {
    final ImmutableMultimap<String, Translation> emptyMap= ImmutableMultimap.of();
    return new FacadeBundleContent(bundleName, emptyMap);
  }


  /**
   * Returns a new FacadeBundleContent derived from this one and set its translations
   * based on the given map of bundle files.
   *
   * @param bundleFiles the file containing the translations for this bundle for each language
   * @return a FacadeBundleContent with the translations from the given files
   */
  public FacadeBundleContent fromFiles(final Map<Language, File> bundleFiles) {
    final SetMultimap<String, Translation> translations= MultimapBuilder.hashKeys().linkedHashSetValues().build();

    for (final Map.Entry<Language, File> entry : bundleFiles.entrySet()) {
      final Language lang = entry.getKey();
      final File file = entry.getValue();

      final PropertyFile propertyFile = new PropertyFile(file);
      //FIXME: Set encoding
      //propertyFile.setFileEncoding(fileEncoding);
      propertyFile.setUseJavaStyleUnicodeEscaping(true);
      propertyFile.load();
      final PropertyMap propertyMap = propertyFile.getPropertyFileContent().getPropertyMap();

      propertyMap.forEach((String key, Property value) -> {

        translations.put(key, new Translation(lang, Joiner.on(",").join(value.getValueList())));
      });
    }

    return new FacadeBundleContent(this.bundleName, translations);
  }


  @Override
  public String toString() {
    return "FacadeBundleContent{" + "bundleName=" + bundleName + "\n\t, content=" + content + '}';
  }


}
