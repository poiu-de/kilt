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
package de.poiu.kilt.internal;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.poiu.kilt.internal.xls.I18nBundleKey;
import de.poiu.kilt.internal.xls.XlsFile;
import org.omnaest.utils.propertyfile.PropertyFile;
import org.omnaest.utils.propertyfile.content.PropertyFileContent;
import org.omnaest.utils.propertyfile.content.element.Property;

/**
 * Creates a {@link XLSFile} from a given list of property files or modifies the property files based on the {@link XLSFile}.
 *
 * @author Omnaest
 */
public class XlsImExporter
{
  /* ********************************************** Constants ********************************************** */
  public static final Logger LOGGER                                      = LogManager.getLogger();




  public static void importXls(final Path propertiesRootDirectory,
                                 final File file,
                                 final String fileEncoding,
                                 final boolean deletePropertiesWithBlankValue) {

    // read XLS file
    final XlsFile xlsFile= new XlsFile(file);
    final Map<I18nBundleKey, Collection<Translation>> content= xlsFile.getContent();

    // stores the mapping of resource bundle basenames and languages to the corresponding property files
    final Map<String, Map<Language, PropertyFile>> bundleFileMapping= new LinkedHashMap<>();

    // FIXME: Sort by bundleBasename and language? In that case we only have to have 1 property file open at a time
    for (final Map.Entry<I18nBundleKey, Collection<Translation>> entry : content.entrySet()) {
      final I18nBundleKey bundleKey= entry.getKey();
      final String bundleBasename= bundleKey.getBundleBaseName();
      final String propertyKey= bundleKey.getKey();
      final Collection<Translation> translations= entry.getValue();

      //FIXME: Auf diese Weise werden gelöschte Schlüssel nicht entfernt. Aber wäre das sinnvoll? Nur, wenn vom Benutzer explizit verlangt.
      // for each bundle…
      for (final Translation translation : translations) {
        if (!bundleFileMapping.containsKey(bundleBasename)) {
          bundleFileMapping.put(bundleBasename, new LinkedHashMap<>());
        }

        if (!bundleFileMapping.get(bundleBasename).containsKey(translation.getLang())) {
          final File fileForBundle= getFileForBundle(propertiesRootDirectory.toFile(), bundleBasename, translation.getLang());
          //TODO: Und hier müsste geprüft werden, ob das File in den i18nIncludes enthalten ist oder nicht.
          final PropertyFile propertyFile= new PropertyFile(fileForBundle);
          if (fileEncoding != null) {
            propertyFile.setFileEncoding(fileEncoding);
          }
          propertyFile.setUseJavaStyleUnicodeEscaping(true);
          propertyFile.load();
          bundleFileMapping.get(bundleBasename).put(translation.getLang(), propertyFile);
        }

        final PropertyFile propertyFile= bundleFileMapping.get(bundleBasename).get(translation.getLang());
        final PropertyFileContent propertyFileContent= propertyFile.getPropertyFileContent();
        final Property defaultProperty= new Property();
        defaultProperty.setKey(propertyKey);
        final Property property= propertyFileContent.getPropertyMap().getOrDefault(propertyKey, defaultProperty);
        if (!property.getValueList().equals(Arrays.asList(translation.getValue()))) {
          property.clearValues();
          property.addValue(translation.getValue());
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


  public static void exportXls(final Path propertiesRootDirectory,
                               final Set<File> resourceBundleFiles,
                               final String propertyFileEncoding,
                               final Path xlsFilePath) {
    final ResourceBundleContentHelper fbcHelper= new ResourceBundleContentHelper(propertiesRootDirectory);
    final Map<String, Map<Language, File>> bundleNameToFilesMap= fbcHelper.toBundleNameToFilesMap(resourceBundleFiles);

    final XlsFile xlsFile= new XlsFile(xlsFilePath.toFile());

    for (final Map.Entry<String, Map<Language, File>> entry : bundleNameToFilesMap.entrySet()) {
      final String bundleName= entry.getKey();
      final Map<Language, File> bundleTranslations= entry.getValue();

      final ResourceBundleContent resourceBundleContent= ResourceBundleContent.forName(bundleName).fromFiles(bundleTranslations);
      for (final Map.Entry<String, Collection<Translation>> e : resourceBundleContent.getContent().asMap().entrySet()) {
        final String propertyKey= e.getKey();
        final Collection<Translation> translations= e.getValue();

        xlsFile.setValue(new I18nBundleKey(bundleName, propertyKey), translations);
      }
    }

    xlsFile.save();
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
