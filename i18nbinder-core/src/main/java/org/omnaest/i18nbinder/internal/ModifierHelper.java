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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnaest.i18nbinder.grouping.FileGroupToPropertiesAdapter;
import org.omnaest.i18nbinder.internal.XLSFile.TableRow;
import org.omnaest.i18nbinder.internal.facade.creation.FacadeBundleContent;
import org.omnaest.i18nbinder.internal.facade.creation.FacadeBundleContentHelper;
import org.omnaest.i18nbinder.internal.xls.Row;
import org.omnaest.i18nbinder.internal.xls.Sheet;
import org.omnaest.utils.propertyfile.PropertyFile;
import org.omnaest.utils.propertyfile.content.PropertyMap;
import org.omnaest.utils.propertyfile.content.element.Property;

/**
 * Creates a {@link XLSFile} from a given list of property files or modifies the property files based on the {@link XLSFile}.
 *
 * @author Omnaest
 */
public class ModifierHelper
{
  /* ********************************************** Constants ********************************************** */
  public static final String GROUPING_PATTERN_REPLACEMENT_PATTERN_STRING = "{locale}";
  public static final Logger LOGGER                                      = LogManager.getLogger();

  /* ********************************************** Classes/Interfaces ********************************************** */
  protected static class PropertyKeyToValueMap extends HashMap<String, String>
  {
    private static final long serialVersionUID = 8625552580988921881L;
  }

  protected static class FilenameToPropertyKeyToValueMap extends HashMap<String, PropertyKeyToValueMap>
  {
    private static final long serialVersionUID = 8631992954259477041L;
  }

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
  public static void writeXLSFileContentToPropertyFiles( File file,
                                                         String fileEncoding,
                                                         LocaleFilter localeFilter,
                                                         boolean deletePropertiesWithBlankValue,
                                                         boolean useJavaStyleUnicodeEscaping )
  {
    //
    if ( XLSFile.isXLSFile( file ) )
    {
      //
      XLSFile xlsFile = new XLSFile( file );
      xlsFile.load();

      //
      List<TableRow> tableRowList = xlsFile.getTableRowList();

      //
      final List<String> missingPropertyInformationList = new ArrayList<String>();

      //
      List<String> localeList = new ArrayList<String>();
      {
        //
        TableRow tableRow = tableRowList.get( 0 );
        localeList.addAll( tableRow.subList( 2, tableRow.size() ) );
        localeList.remove( null );

        //
        for ( String locale : new ArrayList<String>( localeList ) )
        {
          if ( !localeFilter.isLocaleAccepted( locale ) )
          {
            localeList.remove( locale );
          }
        }
      }

      //
      FilenameToPropertyKeyToValueMap filenameToPropertyKeyToValueMap = new FilenameToPropertyKeyToValueMap();
      for ( TableRow tableRow : tableRowList.subList( 1, tableRowList.size() ) )
      {
        //
        String fileNameLocaleIndependent = tableRow.get( 0 );
        String propertyKey = tableRow.get( 1 );

        //
        ModifierHelper.LOGGER.info( "Processing: " + fileNameLocaleIndependent + " " + propertyKey );

        //
        int index = 2;
        for ( String locale : localeList )
        {
          //
          try
          {
            //
            String value = tableRow.get( index++ );

            //
            String fileName = fileNameLocaleIndependent.replaceAll( Pattern.quote( GROUPING_PATTERN_REPLACEMENT_PATTERN_STRING ),
                                                                    locale );

            //
            if ( !filenameToPropertyKeyToValueMap.containsKey( fileName ) )
            {
              filenameToPropertyKeyToValueMap.put( fileName, new PropertyKeyToValueMap() );
            }

            //
            PropertyKeyToValueMap propertyKeyToValueMap = filenameToPropertyKeyToValueMap.get( fileName );

            //
            if ( value != null )
            {
              propertyKeyToValueMap.put( propertyKey, value );
            }
          }
          catch ( Exception e )
          {
            //
            String message = "Missing property value within " + fileNameLocaleIndependent + " for locale " + locale
                             + " and property" + propertyKey;
            missingPropertyInformationList.add( message );
          }
        }

      }

      //
      for ( String fileName : filenameToPropertyKeyToValueMap.keySet() )
      {
        //
        PropertyKeyToValueMap propertyKeyToValueMap = filenameToPropertyKeyToValueMap.get( fileName );

        //
        PropertyFile propertyFile = new PropertyFile( fileName );
        if ( fileEncoding != null )
        {
          propertyFile.setFileEncoding( fileEncoding );
        }
        propertyFile.setUseJavaStyleUnicodeEscaping( useJavaStyleUnicodeEscaping );
        propertyFile.load();
        PropertyMap propertyMap = propertyFile.getPropertyFileContent().getPropertyMap();

        //
        boolean contentChanged = false;
        for ( String propertyKey : propertyKeyToValueMap.keySet() )
        {
          //
          String value = propertyKeyToValueMap.get( propertyKey );

          //
          if ( StringUtils.isNotEmpty( value ) )
          {
            //
            String[] values = value.split( Pattern.quote( FileGroupToPropertiesAdapter.MULTILINE_VALUES_SEPARATOR ) );

            //
            Property property = propertyMap.containsKey( propertyKey ) ? property = propertyMap.get( propertyKey )
                                                                      : new Property();

            //
            property.setKey( propertyKey );
            property.clearValues();
            property.addAllValues( Arrays.asList( values ) );

            //
            propertyMap.put( property );

            //
            contentChanged = true;
          }
          else if ( propertyMap.containsKey( propertyKey ) && deletePropertiesWithBlankValue )
          {
            //
            propertyMap.remove( propertyKey );

            //
            contentChanged = true;
          }
        }

        //
        if ( contentChanged )
        {
          propertyFile.store();
        }
      }

      //
      if ( !missingPropertyInformationList.isEmpty() )
      {
        //
        ModifierHelper.LOGGER.info( "Following property information were incomplete..." );
        for ( String missingPropertyInformation : missingPropertyInformationList )
        {
          ModifierHelper.LOGGER.info( missingPropertyInformation );
        }
      }
    }
  }


  public static XLSFile createXLSFileFromPropertyFiles(final Path propertiesRootDirectory,
                                                       Set<File> resourceBundleFiles,
                                                       String fileEncoding,
                                                       LocaleFilter localeFilter,
                                                       String fileNameLocaleGroupPattern,
                                                       List<Integer> groupingPatternGroupingGroupIndexList,
                                                       boolean useJavaStyleUnicodeEscaping) {

    final FacadeBundleContentHelper fbcHelper= new FacadeBundleContentHelper(propertiesRootDirectory);
    final Map<String, Map<Language, File>> bundleNameToFilesMap= fbcHelper.toBundleNameToFilesMap(resourceBundleFiles);

    final Sheet i18nSheet= new Sheet("Resource Bundle", "Translation Key");

    for (final Map.Entry<String, Map<Language, File>> entry : bundleNameToFilesMap.entrySet()) {
      final String bundleName= entry.getKey();
      final Map<Language, File> bundleTranslations= entry.getValue();

      final FacadeBundleContent resourceBundleContent= FacadeBundleContent.forName(bundleName).fromFiles(bundleTranslations);
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
}
