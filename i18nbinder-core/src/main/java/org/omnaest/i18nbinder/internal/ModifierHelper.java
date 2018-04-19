/*******************************************************************************
 * Copyright 2011 Danny Kunz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.omnaest.i18nbinder.internal;

import org.omnaest.i18nbinder.internal.xls.XLSFile;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnaest.i18nbinder.internal.xls.XLSFile.TableRow;
import org.omnaest.i18nbinder.internal.xls.Row;
import org.omnaest.i18nbinder.internal.xls.Sheet;
import org.omnaest.utils.propertyfile.PropertyFile;
import org.omnaest.utils.propertyfile.content.PropertyFileContent;
import org.omnaest.utils.propertyfile.content.element.Property;

/**
 * Creates a {@link XLSFile} from a given list of property files or modifies the property files based on the {@link XLSFile}.
 *
 * @author Omnaest
 */
public class ModifierHelper
{
  /* ********************************************** Constants ********************************************** */
  public static final Logger LOGGER                                      = LogManager.getLogger();


  /* ********************************************** Methods ********************************************** */
  /**
   * This method writes the values resolved by the xls file back to the property files. Non existing property files are created,
   * existing ones modified.
   *
   * @param file
   * @param fileEncoding
   *          : encoding of the file like UTF-8. If null is passed the default file encoding is used.
   * @param localeFilter
   * @param deletePropertiesWithBlankValue
   * @param useJavaStyleUnicodeEscaping
   */
  public static void writeXLSFileContentToPropertyFiles(final Path propertiesRootDirectory,
                                                           File file,
                                                         String fileEncoding,
                                                         LocaleFilter localeFilter,
                                                         boolean deletePropertiesWithBlankValue,
                                                         boolean useJavaStyleUnicodeEscaping ) {

    // read XLS file
    final XLSFile xlsFile= new XLSFile(file);
    xlsFile.load();
    final List<TableRow> tableRowList = xlsFile.getTableRowList();

    // save the locales defined in the header with their indexes
    final TableRow headerRow= tableRowList.get(0);
    final Map<Language, Integer> localeIndexMap= new LinkedHashMap<>();
    for (int i= 2; i<headerRow.size(); i++) {
      final Language language= Language.of(headerRow.get(i));
      localeIndexMap.put(language, i);
    }

    // remember all the bundles read and the translated values in these bundles
    final Map<String, ResourceBundleContent> translatedBundleContents= new LinkedHashMap<>();

    for (final TableRow tableRow : tableRowList.subList(0, tableRowList.size())) {
      final String bundleBaseName= tableRow.get(0);
      final String bundleKey= tableRow.get(1);

      // create the bundleContent object, if we haven't done already
      if (!translatedBundleContents.containsKey(bundleBaseName)) {
        final ResourceBundleContent bundleContent= ResourceBundleContent.forName(bundleBaseName);
        translatedBundleContents.put(bundleBaseName, bundleContent);
      }

      // insert the translation for each language into the bundleContent object
      final ResourceBundleContent bundleContent= translatedBundleContents.get(bundleBaseName);
      for (Map.Entry<Language, Integer> e : localeIndexMap.entrySet()) {
        final Language language= e.getKey();
        final String translatedValue= tableRow.get(e.getValue());
        bundleContent.addTranslation(bundleKey, new Translation(language, translatedValue));
      }
    }

    final Map<String, Map<Language, PropertyFile>> bundleFileMapping= new LinkedHashMap<>();

    //FIXME: Auf diese Weise werden gelöschte Schlüssel nicht entfernt. Aber wäre das sinnvoll? Nur, wenn vom Benutzer explizit verlangt.
    // for each bundle…
    for (final ResourceBundleContent bundleContent : translatedBundleContents.values()) {
      // …for each key…
      for (Map.Entry<String, Collection<Translation>> e : bundleContent.getContent().asMap().entrySet()) {
        final String resourceKey= e.getKey();
        final Collection<Translation> translations= e.getValue();

        // …update the translation for each language
        for (final Translation translation : translations) {
          if (!bundleFileMapping.containsKey(bundleContent.getBundleBaseName())) {
            bundleFileMapping.put(bundleContent.getBundleBaseName(), new LinkedHashMap<>());
          }
          if (!bundleFileMapping.get(bundleContent.getBundleBaseName()).containsKey(translation.getLang())) {
            final File fileForBundle= getFileForBundle(propertiesRootDirectory.toFile(), bundleContent.getBundleBaseName(), translation.getLang());
            final PropertyFile propertyFile= new PropertyFile(fileForBundle);
            if (fileEncoding != null) {
              propertyFile.setFileEncoding(fileEncoding);
            }
            propertyFile.setUseJavaStyleUnicodeEscaping(useJavaStyleUnicodeEscaping);
            propertyFile.load();
            bundleFileMapping.get(bundleContent.getBundleBaseName()).put(translation.getLang(), propertyFile);
          }
          final PropertyFile propertyFile= bundleFileMapping.get(bundleContent.getBundleBaseName()).get(translation.getLang());
          final PropertyFileContent propertyFileContent= propertyFile.getPropertyFileContent();
          final Property defaultProperty= new Property();
          defaultProperty.setKey(resourceKey);
          final Property property= propertyFileContent.getPropertyMap().getOrDefault(resourceKey, defaultProperty);
          if (!property.getValueList().equals(Arrays.asList(translation.getValue()))) {
            property.clearValues();
            property.addValue(translation.getValue());
          }
        }
      }
    }

    //now write the property files back to disk
    //FIXME: Könnte man das nicht oben in der Liste öffnen und schließen, um nicht alle gleichzeitig offen zu haben?
    bundleFileMapping.values().forEach((Map<Language, PropertyFile> langPropMap) -> {
      langPropMap.values().forEach((PropertyFile propertyFile) -> {
        // only write files if they have some content (avoid creating unwanted empty files for unsupported locales)
        if (propertyFile.getPropertyFileContent().size() > 0) {
          propertyFile.store();
        }
      });
    });
  }


  public static XLSFile createXLSFileFromPropertyFiles(final Path propertiesRootDirectory,
                                                       Set<File> resourceBundleFiles,
                                                       String fileEncoding,
                                                       LocaleFilter localeFilter,
                                                       String fileNameLocaleGroupPattern,
                                                       List<Integer> groupingPatternGroupingGroupIndexList,
                                                       boolean useJavaStyleUnicodeEscaping) {

    final ResourceBundleContentHelper fbcHelper= new ResourceBundleContentHelper(propertiesRootDirectory);
    final Map<String, Map<Language, File>> bundleNameToFilesMap= fbcHelper.toBundleNameToFilesMap(resourceBundleFiles);

    final Sheet i18nSheet= new Sheet("Resource Bundle", "Translation Key");

    for (final Map.Entry<String, Map<Language, File>> entry : bundleNameToFilesMap.entrySet()) {
      final String bundleName= entry.getKey();
      final Map<Language, File> bundleTranslations= entry.getValue();

      final ResourceBundleContent resourceBundleContent= ResourceBundleContent.forName(bundleName).fromFiles(bundleTranslations);
      for (final Map.Entry<String, Collection<Translation>> e : resourceBundleContent.getContent().asMap().entrySet()) {
        final String propertyKey= e.getKey();
        final Collection<Translation> translations= e.getValue();

        i18nSheet.addContentRow(new Row(bundleName, propertyKey, translations));
      }
    }

    final XLSFile xlsFile= new XLSFile();
    for (String[] row : i18nSheet.getRows()) {
      xlsFile.addRow(row);
    }

    return xlsFile;
  }


  private static File getFileForBundle(final File propertiesRootDirectory, final String bundleBasename, final Language language) {
    final StringBuilder sb= new StringBuilder();

    sb.append(bundleBasename);
    if (!language.getLang().isEmpty()) {
      sb.append("_").append(language.getLang());
    }
    sb.append(".properties");

    return new File(propertiesRootDirectory, sb.toString());
  }

}
