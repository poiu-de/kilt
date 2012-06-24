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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

public class FacadeCreatorHelperTest
{
  /* ********************************************** Constants ********************************************** */
  private final static String[] PROPERTY_FILENAMES = { "adminTest_de_DE.properties", "adminTest_en_US.properties",
      "viewTest_de_DE.properties", "viewTest_en_US.properties", "localelessTest.properties", "673numericalTest_en_US.properties",
      "sub1/shared_de_DE.properties", "sub2/shared_de_DE.properties" };
  
  /* ********************************************** Variables ********************************************** */
  private Set<File>             propertyFileSet    = new HashSet<File>();
  
  /* ********************************************** Methods ********************************************** */
  
  @Before
  public void setUp() throws Exception
  {
    //
    for ( String propertyFilename : PROPERTY_FILENAMES )
    {
      this.propertyFileSet.add( new File( this.getClass().getResource( propertyFilename ).getFile() ) );
    }
  }
  
  @Test
  public void testCreateI18nInterfaceFacadeFromPropertyFiles() throws IOException
  {
    //
    final String packageBaseFolder = "org\\omnaest\\i18nbinder\\internal";
    
    //
    String fileNameLocaleGroupPattern = null;
    List<Integer> groupingPatternGroupingGroupIndexList = null;
    String baseNameInTargetPlattform = "i18n";
    String baseFolderIgnoredPath = new File( "" ).getAbsolutePath() + "\\target\\test-classes\\" + packageBaseFolder + "\\";
    String packageName = "org.omnaest.i18nbinder.internal.facade";
    String javaFacadeFileName = "I18nFacade";
    boolean externalizeTypes = true;
    
    //
    final Map<String, String> facadeFromPropertyFiles = FacadeCreatorHelper.createI18nInterfaceFacadeFromPropertyFiles( this.propertyFileSet,
                                                                                                                        new LocaleFilter(),
                                                                                                                        fileNameLocaleGroupPattern,
                                                                                                                        groupingPatternGroupingGroupIndexList,
                                                                                                                        baseNameInTargetPlattform,
                                                                                                                        baseFolderIgnoredPath,
                                                                                                                        packageName,
                                                                                                                        javaFacadeFileName,
                                                                                                                        externalizeTypes );
    
    //    
    for ( String fileName : facadeFromPropertyFiles.keySet() )
    {
      //
      final String basePath = new File( "" ).getAbsolutePath() + "\\src\\test\\java\\" + packageBaseFolder + "\\facade\\";
      final String fileContent = facadeFromPropertyFiles.get( fileName );
      
      //
      if ( fileName.contains( "." ) )
      {
        fileName = StringUtils.removeStart( fileName, packageName + "." );
        File directory = new File( basePath );
        String[] tokens = fileName.split( "\\." );
        for ( int ii = 0; ii < tokens.length - 1; ii++ )
        {
          String directoryName = tokens[ii];
          directory = new File( directory, directoryName );
          if ( !directory.exists() )
          {
            directory.mkdir();
          }
        }
        fileName = StringUtils.join( Arrays.copyOf( tokens, tokens.length - 1 ), "/" ) + ( tokens.length > 1 ? "/" : "" )
                   + tokens[tokens.length - 1];
      }
      
      final File file = new File( basePath, fileName + ".java" );
      FileUtils.writeStringToFile( file, fileContent, "utf-8" );
    }
    
  }
}
