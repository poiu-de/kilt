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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.omnaest.i18nbinder.grouping.FileGroup;
import org.omnaest.i18nbinder.grouping.FileGroupToPropertiesAdapter;
import org.omnaest.i18nbinder.grouping.FileGrouper;
import org.omnaest.i18nbinder.internal.XLSFile.TableRow;
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
  public static Logger       logger                                      = new Logger()
                                                                         {
                                                                           @Override
                                                                           public void info( String message )
                                                                           {
                                                                           }
                                                                         };
  
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
   * @param xlsFile
   * @param fileEncoding
   *          : encoding of the file like UTF-8. If null is passed the default file encoding is used.
   * @param localeFilter
   * @param deletePropertiesWithBlankValue
   */
  public static void writeXLSFileContentToPropertyFiles( File file,
                                                         String fileEncoding,
                                                         LocaleFilter localeFilter,
                                                         boolean deletePropertiesWithBlankValue )
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
        ModifierHelper.logger.info( "Processing: " + fileNameLocaleIndependent + " " + propertyKey );
        
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
        ModifierHelper.logger.info( "Following property information were incomplete..." );
        for ( String missingPropertyInformation : missingPropertyInformationList )
        {
          ModifierHelper.logger.info( missingPropertyInformation );
        }
      }
    }
  }
  
  /**
   * Creates a new {@link XLSFile} based on a set of files.
   * 
   * @param propertyFileSet
   * @param localeFilter
   * @param fileNameLocaleGroupPattern
   * @param groupingPatternGroupingGroupIndexList
   * @return
   */
  public static XLSFile createXLSFileFromPropertyFiles( Set<File> propertyFileSet,
                                                        LocaleFilter localeFilter,
                                                        String fileNameLocaleGroupPattern,
                                                        List<Integer> groupingPatternGroupingGroupIndexList )
  {
    //
    XLSFile retval = null;
    
    //
    if ( propertyFileSet != null )
    {
      //
      Map<String, FileGroup> fileGroupIdentifierToFileGroupMap;
      {
        FileGrouper fileGrouper = new FileGrouper();
        try
        {
          if ( fileNameLocaleGroupPattern != null )
          {
            fileGrouper.setGroupingPatternString( fileNameLocaleGroupPattern );
          }
          if ( groupingPatternGroupingGroupIndexList != null )
          {
            fileGrouper.setGroupingPatternGroupingGroupIndexList( groupingPatternGroupingGroupIndexList );
          }
        }
        catch ( Exception e )
        {
          ModifierHelper.logger.info( e.getMessage() );
        }
        fileGrouper.setGroupingPatternReplacementToken( GROUPING_PATTERN_REPLACEMENT_PATTERN_STRING );
        fileGrouper.addAllFiles( propertyFileSet );
        fileGroupIdentifierToFileGroupMap = fileGrouper.determineFileGroupIdentifierToFileGroupMap();
      }
      
      //
      List<FileGroupToPropertiesAdapter> fileGroupToPropertiesAdapterList = new ArrayList<FileGroupToPropertiesAdapter>();
      {
        //
        for ( String fileGroupIdentifier : fileGroupIdentifierToFileGroupMap.keySet() )
        {
          //
          FileGroup fileGroup = fileGroupIdentifierToFileGroupMap.get( fileGroupIdentifier );
          
          //
          FileGroupToPropertiesAdapter fileGroupToPropertiesAdapter = new FileGroupToPropertiesAdapter( fileGroup );
          
          //
          fileGroupToPropertiesAdapterList.add( fileGroupToPropertiesAdapter );
        }
        
        //
        Collections.sort( fileGroupToPropertiesAdapterList, new Comparator<FileGroupToPropertiesAdapter>()
        {
          @Override
          public int compare( FileGroupToPropertiesAdapter fileGroupToPropertiesAdapter1,
                              FileGroupToPropertiesAdapter fileGroupToPropertiesAdapter2 )
          {
            //
            String fileGroupIdentifier1 = fileGroupToPropertiesAdapter1.getFileGroup().getFileGroupIdentifier();
            String fileGroupIdentifier2 = fileGroupToPropertiesAdapter2.getFileGroup().getFileGroupIdentifier();
            
            //
            return fileGroupIdentifier1.compareTo( fileGroupIdentifier2 );
          }
        } );
      }
      
      //determine all locales but fix the order
      List<String> localeList = new ArrayList<String>();
      {
        //
        Set<String> localeSet = new HashSet<String>();
        for ( FileGroupToPropertiesAdapter fileGroupToPropertiesAdapter : fileGroupToPropertiesAdapterList )
        {
          localeSet.addAll( fileGroupToPropertiesAdapter.determineGroupTokenList() );
        }
        localeList.addAll( localeSet );
        
        //
        for ( String locale : localeSet )
        {
          if ( !localeFilter.isLocaleAccepted( locale ) )
          {
            localeList.remove( locale );
          }
        }
        
        //
        Collections.sort( localeList );
      }
      
      //
      XLSFile xlsFile = new XLSFile();
      {
        //
        List<TableRow> tableRowList = xlsFile.getTableRowList();
        
        //titles
        {
          //
          TableRow tableRow = new TableRow();
          tableRow.add( "File" );
          tableRow.add( "Property key" );
          tableRow.addAll( localeList );
          
          //
          tableRowList.add( tableRow );
        }
        
        //
        for ( FileGroupToPropertiesAdapter fileGroupToPropertiesAdapter : fileGroupToPropertiesAdapterList )
        {
          //
          String fileGroupIdentifier = fileGroupToPropertiesAdapter.getFileGroup().getFileGroupIdentifier();
          
          //
          ModifierHelper.logger.info( "Processing: " + fileGroupIdentifier );
          
          //
          List<String> propertyKeyList = new ArrayList<String>( fileGroupToPropertiesAdapter.determinePropertyKeySet() );
          Collections.sort( propertyKeyList );
          for ( String propertyKey : propertyKeyList )
          {
            //
            TableRow tableRow = new TableRow();
            tableRow.add( fileGroupIdentifier );
            tableRow.add( propertyKey );
            
            //
            for ( String locale : localeList )
            {
              //
              String value = fileGroupToPropertiesAdapter.resolvePropertyValue( propertyKey, locale );
              
              //
              value = StringUtils.defaultString( value );
              
              //
              tableRow.add( value );
            }
            
            //
            tableRowList.add( tableRow );
          }
        }
        
        //
        retval = xlsFile;
      }
    }
    
    //
    return retval;
  }
}
