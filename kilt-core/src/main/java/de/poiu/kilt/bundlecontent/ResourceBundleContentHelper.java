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

import de.poiu.fez.Require;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A helper class for creating a {@link ResourceBundleContent} from a set of resource
 * bundle files.
 * <p>
 * It allow setting an ignorableBasePath (which is usually necessary) when building the bundle name.
 * For example if all your resource bundle files are stored in
 * <code>${project.dir}/src/main/resources/i18n/</code>
 * and are contained in the built jar unter the path <code>/i18n</code> then the ignorableBasePath
 * is <code>${project.dir}/src/main/resources/</code>.
 *
 * @author mherrn
 */
public class ResourceBundleContentHelper {
  private static final Logger LOGGER= LogManager.getLogger();

  // The format of the optional parts was taken from the Javadoc of the Locale class
  // This format allow the form basename_language_variant which seems to be invalid according to {@link java.util.ResourceBundle#getBundle(java.lang.String,java.util.Locale,java.lang.ClassLoader)}
  // See for example: https://docs.oracle.com/javase/7/docs/api/java/util/ResourceBundle.html#getBundle(java.lang.String,%20java.util.Locale,%20java.lang.ClassLoader)
  // However this glitch is accepted for simplicity
  // Also the format of the variant is much less restricted than described in the Javadoc of the {@link java.util.Locale} class. It seems that the format given in the javadoc
  // of Locale conflicts with the example myBasename_en_Latn_US_WINDOWS_VISTA in the javdoc of ResourceBundle#getBundle(...)
  /** A pattern to match the names of localization resource bundle files. */
  protected static final Pattern PATTERN_RESOURCE_BUNDLE_FILE_NAME= Pattern.compile(
        "(?<BUNDLE>[a-zA-Z0-9\\-]+)"                                      //the bundle basename (mandatory)
      + "(?:_"                                                            //ignore the underscore of the optional LANG
      +   "(?<LOCALE>"                                                    //a group capturing all locale parts (LANG, SCRIPT, COUNTRY and VARIANT)
      +     "(?<LANG>[a-zA-Z]{2,8})"                                        //the optional LANG
      +     "(?:_"                                                          //ignore the underscore of the optional SCRIPT
      +       "(?<SCRIPT>[a-zA-Z]{4})"                                      //the optional SCRIPT
      +     ")?"
      +     "(?:_"                                                          //ignore the underscore of the optional COUNTRY
      +       "(?<COUNTRY>[a-zA-Z]{2}|[0-9]{3})"                            //the optional COUNTRY
      +     ")?"
      +     "(?:_"                                                          //ignore the underscore of the optional VARIANT
      +       "(?<VARIANT>[0-9a-zA-Z-]+.)"                                  //the optional VARIANT
      +     ")?"
      +   ")"
      + ")?"
      + "\\.properties"                                                   // the file must always have the suffix .properties
    , Pattern.UNICODE_CHARACTER_CLASS);


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * The path of the resource bundle files to be ignored when building the bundle name.
   * Defaults to the current working directory.
   */
  private Path ignorableBasePath= Paths.get("");


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new FacadeBundleContentHelper with the default ignorableBasePath (the current working
   * directory.
   */
  public ResourceBundleContentHelper() {
  }

  /**
   * Creates a new FacadeBundleContentHelper with the given ignorableBasePath.
   * @param ignorableBasePath the path to ignore when building the bundle name
   */
  public ResourceBundleContentHelper(final File ignorableBasePath) {
    Require.nonNull(ignorableBasePath);
    this.ignorableBasePath= ignorableBasePath.toPath();
  }

  /**
   * Creates a new FacadeBundleContentHelper with the default ignorableBasePath.
   * @param ignorableBasePath the path to ignore when building the bundle name
   */
  public ResourceBundleContentHelper(final Path ignorableBasePath) {
    Require.nonNull(ignorableBasePath);
    this.ignorableBasePath= ignorableBasePath;
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * Converts a collection of resource bundle files to a nested map containing the
   * bundle basepath as the key and another map containing the translations in these
   * resource bundles as the value.
   * <p>
   * This is done by applying a pattern to the given files. The files need to be named according
   * to the general rules for Java localization resource bundle files.
   * <p>
   * It is valid to have resource bundle files without any language (for the fallback resource
   * bundle). The language for this file will not be null, but instead a Language object with an
   * empty string.
   *
   * @param propertyFiles the files to convert
   * @return the map created from the given collection of files
   */
  public Map<String, Map<Language, File>> toBundleNameToFilesMap(final Collection<File> propertyFiles) {
    Require.nonNull(propertyFiles);
    final Map<String, Map<Language, File>> result= new LinkedHashMap<>();

    for (final File file : propertyFiles) {
      final Matcher matcher = PATTERN_RESOURCE_BUNDLE_FILE_NAME.matcher(file.getName());
      if (matcher.matches()) {
        final String bundleBasename= matcher.group("BUNDLE");
        final String langCode= matcher.group("LOCALE");
        final Language language= Language.of(langCode != null ? langCode : "");

        final String bundlePrefix= getBundlePrefix(file);
        final String fullBundleName;
        if (bundlePrefix == null || bundlePrefix.trim().isEmpty()) {
          fullBundleName= bundleBasename;
        } else {
          fullBundleName= bundlePrefix + "/" + bundleBasename;
        }

        if (!result.containsKey(fullBundleName)) {
          result.put(fullBundleName, new LinkedHashMap<>());
        }

        final Map<Language, File> langToFileMap= result.get(fullBundleName);
        if (langToFileMap.containsKey(language)) {
          throw new RuntimeException("Language "+langCode+" already in map. Should never happen.");
        }
        langToFileMap.put(language, file);
      } else {
        LOGGER.log(Level.WARN, "File {} doesn't match the expected pattern. It will not be processed! Does your file name end with .properties?", file.getAbsolutePath());
      }
    }

    return result;
  }


  /**
   * Returns the bundle prefix for the given path to a resource bundle file.
   * This takes the {@link #ignorableBasePath} into account. Path separators are replaced by
   * underscores.
   * <p>
   * For example if this method is called for the resource bundle file
   * <code>${project.dir}/src/main/resources/i18n/messages/mymessages_de.properties</code>
   * and the ignorable base path is set to
   * <code>${project.dir}/src/main/resources/</code> then the bundle prefix will be
   * <code>i18n_messages</code>
   * <p>
   * If the given path is not located under the given ignorable base path, then an
   * IllegalArgumentException will be thrown.
   *
   * @param path the path to the resource bundle file for which to get the bundle prefix
   * @return the bundle prefix for the given resource bundle file
   * @throws IllegalArgumentException if the given path is not located below the ignorable base path
   */
  protected String getBundlePrefix(final Path path) {
    LOGGER.traceEntry("getBundlePrefix for path: {}", path);

    Require.nonNull(path);
    if (!path.toAbsolutePath().startsWith(this.ignorableBasePath.toAbsolutePath())) {
      throw new IllegalArgumentException("All files should live below the ignorable base path "+this.ignorableBasePath.toAbsolutePath().toString()+". Given path is "+path.toAbsolutePath().toString());
    }

    final Path prefixPath= this.ignorableBasePath.toAbsolutePath().relativize(path.toAbsolutePath().getParent());
    final String bundlePrefix= prefixPath.toString()
      .replaceFirst("^/+", "")         //never start with a slash
      .replaceFirst("$/+", "")         //never end with a slash
      .replaceAll("\\/+", "/")       //reduce multiple slashes to only one
      ;



    return LOGGER.traceExit(bundlePrefix);
  }


  /**
   * Returns the bundle prefix for the given path to a resource bundle file.
   * This takes the {@link #ignorableBasePath} into account. Path separators are replaced by
   * underscores.
   * <p>
   * For example if this method is called for the resource bundle file
   * <code>${project.dir}/src/main/resources/i18n/messages/mymessages_de.properties</code>
   * and the ignorable base path is set to
   * <code>${project.dir}/src/main/resources/</code> then the bundle prefix will be
   * <code>i18n_messages</code>
   * <p>
   * If the given path is not located under the given ignorable base path, then an
   * IllegalArgumentException will be thrown.
   *
   * @param file the path to the resource bundle file for which to get the bundle prefix
   * @return the bundle prefix for the given resource bundle file
   * @throws IllegalArgumentException if the given path is not located below the ignorable base path
   */
  protected String getBundlePrefix(final File file) {
    return getBundlePrefix(file.toPath());
  }

}
