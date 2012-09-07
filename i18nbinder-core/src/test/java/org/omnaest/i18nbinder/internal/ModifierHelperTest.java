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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.omnaest.i18nbinder.internal.XLSFile.TableRow;
import org.omnaest.utils.propertyfile.PropertyFile;
import org.omnaest.utils.propertyfile.content.PropertyFileContent;

public class ModifierHelperTest
{
  /* ********************************************** Constants ********************************************** */
  private final static String[] PROPERTY_FILENAMES = { "adminTest_de_DE.properties", "adminTest_en_US.properties",
      "viewTest_de_DE.properties", "viewTest_en_US.properties", "localelessTest.properties" };
  
  private static final String   fileEncoding       = "utf-8";
  
  /* ********************************************** Variables ********************************************** */
  private Set<File>             propertyFileSet    = new HashSet<File>();
  private File                  xlsFile            = null;
  
  /* ********************************************** Methods ********************************************** */
  
  @Before
  public void setUp() throws Exception
  {
    //
    for ( String propertyFilename : PROPERTY_FILENAMES )
    {
      this.propertyFileSet.add( new File( this.getClass().getResource( propertyFilename ).getFile() ) );
    }
    
    //
    this.xlsFile = new File( new File( this.getClass().getResource( PROPERTY_FILENAMES[0] ).getFile() ).getParent()
                             + "\\result.xls" );
    
    if ( this.xlsFile.exists() )
    {
      this.xlsFile.delete();
    }
    
    //
    URL resource = this.getClass().getResource( "viewTest_.properties" );
    if ( resource != null )
    {
      File newKeyPropertyFile = new File( resource.getFile() );
      if ( newKeyPropertyFile.exists() )
      {
        newKeyPropertyFile.delete();
      }
    }
    
  }
  
  @Test
  public void testModifierHelperLoadAndStore()
  {
    //
    XLSFile xlsFile = ModifierHelper.createXLSFileFromPropertyFiles( this.propertyFileSet, fileEncoding, new LocaleFilter(),
                                                                     null, null );
    
    //
    xlsFile.setFile( this.xlsFile );
    xlsFile.store();
    
    //
    ModifierHelperTest.assertContent( xlsFile );
    
    //
    ModifierHelper.writeXLSFileContentToPropertyFiles( xlsFile.getFile(), null, new LocaleFilter(), true );
    
    //
    xlsFile.load();
    
    //
    ModifierHelperTest.assertContent( xlsFile );
    
  }
  
  private static void assertContent( XLSFile xlsFile )
  {
    //
    List<TableRow> tableRowList = xlsFile.getTableRowList();
    assertEquals( 6 + 1, tableRowList.size() );
    
    //
    int index = 0;
    {
      TableRow tableRow = tableRowList.get( index++ );
      assertEquals( Arrays.asList( "File", "Property key", "", "de_DE", "en_US" ), tableRow );
    }
    {
      TableRow tableRow = tableRowList.get( index++ );
      assertEquals( Arrays.asList( "my.property.key1", "", "wert1", "value1" ), tableRow.subList( 1, tableRow.size() ) );
    }
    {
      TableRow tableRow = tableRowList.get( index++ );
      assertEquals( Arrays.asList( "my.property.key2", "", "wert2", "value2" ), tableRow.subList( 1, tableRow.size() ) );
    }
    {
      TableRow tableRow = tableRowList.get( index++ );
      assertEquals( Arrays.asList( "my.property.key9", "value9", "", "" ), tableRow.subList( 1, tableRow.size() ) );
    }
    {
      TableRow tableRow = tableRowList.get( index++ );
      assertEquals( Arrays.asList( "my.property.key1", "", "wert1", "value1" ), tableRow.subList( 1, tableRow.size() ) );
    }
    {
      TableRow tableRow = tableRowList.get( index++ );
      assertEquals( Arrays.asList( "my.property.key3", "", "", "value3" ), tableRow.subList( 1, tableRow.size() ) );
    }
    {
      TableRow tableRow = tableRowList.get( index++ );
      assertEquals( Arrays.asList( "my.property.key4", "", "wert4", "" ), tableRow.subList( 1, tableRow.size() ) );
    }
  }
  
  @Test
  public void testModifierHelperAddKey()
  {
    //
    XLSFile xlsFile = ModifierHelper.createXLSFileFromPropertyFiles( this.propertyFileSet, fileEncoding, new LocaleFilter(),
                                                                     null, null );
    
    //
    List<TableRow> tableRowList = xlsFile.getTableRowList();
    
    //
    String propertyKey = "new.key";
    List<String> propertyValueList = Arrays.asList( "new.value" );
    
    //
    TableRow tableRow = new TableRow();
    tableRow.addAll( tableRowList.get( tableRowList.size() - 1 ) );
    tableRow.set( 1, propertyKey );
    tableRow.set( 2, propertyValueList.get( 0 ) );
    xlsFile.getTableRowList().add( tableRow );
    
    //
    xlsFile.setFile( this.xlsFile );
    xlsFile.store();
    
    //
    ModifierHelper.writeXLSFileContentToPropertyFiles( xlsFile.getFile(), null, new LocaleFilter(), true );
    
    //
    String locale = tableRowList.get( 0 ).get( 2 );
    String propertyFileName = tableRow.get( 0 ).replaceAll( Pattern.quote( "{locale}" ), locale );
    PropertyFile propertyFile = new PropertyFile( propertyFileName );
    
    //
    assertTrue( propertyFile.getFile().exists() );
    
    //
    propertyFile.load();
    PropertyFileContent propertyFileContent = propertyFile.getPropertyFileContent();
    
    //
    assertTrue( propertyFileContent.hasPropertyKeyAndValueList( propertyKey, propertyValueList ) );
    
    //clean up
    {
      //
      propertyFileName = tableRow.get( 0 ).replaceAll( Pattern.quote( "{locale}" ), "" );
      propertyFile = new PropertyFile( propertyFileName );
      propertyFile.load();
      propertyFile.getPropertyFileContent().getPropertyMap().remove( propertyKey );
      propertyFile.store();
      
      //
      propertyFileName = tableRow.get( 0 ).replaceAll( Pattern.quote( "{locale}" ), "de_DE" );
      propertyFile = new PropertyFile( propertyFileName );
      propertyFile.load();
      propertyFile.getPropertyFileContent().getPropertyMap().remove( propertyKey );
      propertyFile.store();
    }
    
  }
  
}
