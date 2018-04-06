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
package org.omnaest.i18nbinder.internal.facade;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;


/**
 * Helper class to access locale bundles in a type safe way.
 * <p>
 * It also allows to mark missing keys by returning the given for which translations are missing
 * surrounded by <code>:MISSING:</code> instead of throwing an exception (turned on by default).
 * This allows missing translations without breaking the client code at runtime and easily spotting
 * the missing translations.
 * <p>
 * In addition this class allow to retrieve localized values by giving a bundle name and key
 * as strings. This way this class can be used without any generated Enum facade.
 *
 * @author mherrn
 */
public class I18n {


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /** Whether to mark missing translations by surrounding them with <code>:MISSING:</code>. */
  private final boolean markMissingTranslations;

  /** The locale to use when retrieving localizations. If not given the current locale will be used. */
  private final Optional<Locale> locale;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new I18n for the default locale and marking missing translations.
   * When retrieving localizations that are not existing and no fallback can be found , instead
   * of throwing a MissingResourceException the bundle name and requested key will be returned
   * surrounded by <code>:MISSING:</code>.
   */
  public I18n() {
    this(true);
  }


  /**
   * Creates a new I18n for the given locale and marking missing translations.
   * When retrieving localizations that are not existing and no fallback can be found , instead
   * of throwing a MissingResourceException the bundle name and requested key will be returned
   * surrounded by <code>:MISSING:</code>.
   *
   * @param locale the locale to use when retrieving localized strings from this class.
   */
  public I18n(final Locale locale) {
    this(locale, true);
  }


  /**
   * Creates a new I18n for the default locale.
   * Whether missing translations are marked is specified by <code>markMissingTranslations</code>
   * When retrieving localizations that are not existing and no fallback can be found, instead
   * of throwing a MissingResourceException the bundle name and requested key will be returned
   * surrounded by <code>:MISSING:</code>.
   * Otherwise the default behaviour of the java ResourceBundle class will apply which will be a
   * MissingResourceException being thrown.
   *
   * @param markMissingTranslations whether to mark missing translations
   */
  public I18n(final boolean markMissingTranslations) {
    this.locale= Optional.empty();
    this.markMissingTranslations= markMissingTranslations;
  }


  /**
   * Creates a new I18n for the given locale.
   * Whether missing translations are marked is specified by <code>markMissingTranslations</code>
   * When retrieving localizations that are not existing and no fallback can be found, instead
   * of throwing a MissingResourceException the bundle name and requested key will be returned
   * surrounded by <code>:MISSING:</code>.
   * Otherwise the default behaviour of the java ResourceBundle class will apply which will be a
   * MissingResourceException being thrown.
   *
   * @param locale the locale to use when retrieving localized strings from this class.
   * @param markMissingTranslations whether to mark missing translations
   */
  public I18n(final Locale locale, final boolean markMissingTranslations) {
    Objects.requireNonNull(locale);
    this.locale= Optional.of(locale);
    this.markMissingTranslations= markMissingTranslations;
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * Creates a new I18n for the given locale based on this I18n.
   * This basically means that the current value of <code>markMissingTranslations</code> will
   * be used and the locale will be set to the given one.
   *
   * @param locale the locale to use when retrieving localized strings from this class.
   * @return a new I18n derived from this one for the given locale
   */
  public I18n forLocale(final Locale locale) {
    return new I18n(locale, this.markMissingTranslations);
  }


  /**
   * Returns the translation for a key or the key itself surrounded by <code>:MISSING:</code>
   * if no translation can be found.
   * This code will never throw a MissingResourceException.
   *
   * @param bundle the bundle in which to search for the translated key
   * @param key the key for which to return the translation
   * @return the translation for the key or <code>:MISSING:&lt;bundle&gt;#&lt;key&gt;:MISSING</code>
   *          if no translation can be found
   */
  private String getStringOrPlaceholder(final ResourceBundle bundle, final String key){
    Objects.requireNonNull(bundle);
    Objects.requireNonNull(key);
    if (bundle.containsKey(key)){
      return bundle.getString(key);
    }else{
      return ":MISSING:"+bundle.getBaseBundleName()+"#"+key+":MISSING:";
    }
  }


  /**
   * Returns the translation for a resource bundle key.
   * If no translation can be found the behaviour is defined by whether {@link #markMissingTranslations}
   * is set. If false (the default), a MissingResourceException will be thrown.
   * Otherwise the key itself will be returned surrounded by <code>:MISSING:</code>.
   *
   * @param bundleKey the bundle key for which to return the translation
   * @return the translation for the bundle key or <code>:MISSING:&lt;bundle&gt;#&lt;key&gt;:MISSING</code>
   *          if no translation can be found and {@link #markMissingTranslations} is set
   * @throws MissingResourceException if no translation can be found and {@link #markMissingTranslations} is not set
   */
  public String get(final I18nBundleKey bundleKey){
    Objects.requireNonNull(bundleKey);
    final ResourceBundle bundle= this.locale.isPresent()
                                 ? ResourceBundle.getBundle(bundleKey.getBasename(), this.locale.get())
                                 : ResourceBundle.getBundle(bundleKey.getBasename());
    if (markMissingTranslations) {
      return getStringOrPlaceholder(bundle, bundleKey.getKey());
    } else {
      return bundle.getString(bundleKey.getKey());
    }
  }


  /**
   * Returns the translation for a key.
   * If no translation can be found the behaviour is defined by whether {@link #markMissingTranslations}
   * is set. If false (the default), a MissingResourceException will be thrown.
   * Otherwise the key itself will be returned surrounded by <code>:MISSING:</code>.
   *
   * @param bundleName the bundle in which to search for the translated key
   * @param key the key for which to return the translation
   * @return the translation for the bundle key or <code>:MISSING:&lt;bundle&gt;#&lt;key&gt;:MISSING</code>
   *          if no translation can be found and {@link #markMissingTranslations} is set
   * @throws MissingResourceException if no translation can be found and {@link #markMissingTranslations} is not set
   */
  public String get(final String bundleName, final String key) {
    Objects.requireNonNull(bundleName);
    Objects.requireNonNull(key);
    final ResourceBundle bundle= this.locale.isPresent()
                                 ? ResourceBundle.getBundle(bundleName, this.locale.get())
                                 : ResourceBundle.getBundle(bundleName);
    if (markMissingTranslations) {
      if (bundle.containsKey(key)) {
        return bundle.getString(key);
      } else {
        return ":MISSING:"+bundleName+"#"+key+":MISSING";
      }
    } else {
      return bundle.getString(key);
    }
  }
}
