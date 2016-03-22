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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Simple representation of an XLS file of Microsoft Excel.
 * 
 * @author Omnaest
 */
public class XLSFile implements Serializable
{
  /* ********************************************** Constants ********************************************** */
  private static final long   serialVersionUID  = 4924867114503312907L;
  private static final String MAINSHEETPAGENAME = "all";
  private static final String FILESUFFIX_XLS    = ".xls";
  private static final String FILESUFFIX_XLSX   = ".xlsx";
  
  /* ********************************************** Variables ********************************************** */
  private List<TableRow>      tableRowList      = new ArrayList<TableRow>();
  
  protected File              file              = null;
  
  /* ********************************************** Classes/Interfaces ********************************************** */
  
  /**
   * Representation of a row.
   */
  public static class TableRow extends ArrayList<String>
  {
    private static final long serialVersionUID = 4599939864378182879L;
  }
  
  /* ********************************************** Methods ********************************************** */
  
  /**
   * Creates a unlinked instance. The underlying file has to be set before invoking {@link XLSFile#load()} or
   * {@link XLSFile#store()} methods.
   * 
   * @see XLSFile#setFile(File)
   */
  public XLSFile()
  {
  }
  
  /**
   * @param file
   */
  public XLSFile( File file )
  {
    this.setFile( file );
  }
  
  /**
   * Loads the data from the disk into this object.
   */
  public void load()
  {
    try
    {
      //
      InputStream inputStream = new BufferedInputStream( new FileInputStream( this.file ) );
      Workbook wb = this.newWorkbookFrom( inputStream );
      Sheet sheet = wb.getSheet( MAINSHEETPAGENAME );
      
      //
      this.clear();
      for ( Row iRow : sheet )
      {
        //
        TableRow newTableRow = new TableRow();
        
        //
        for ( Cell iCell : iRow )
        {

		  //
		  if ( iCell.getCellType() == Cell.CELL_TYPE_NUMERIC )
		  {
            newTableRow.add( String.valueOf(iCell.getNumericCellValue()) );
		  }
		  else
		  {
            newTableRow.add( iCell.getStringCellValue() );
		  }
        }
        
        //
        this.tableRowList.add( newTableRow );
      }
      
      //
    }
    catch ( FileNotFoundException e )
    {
      e.printStackTrace();
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
    
  }
  
  private boolean useXLSXFileFormat()
  {
    return this.file != null && this.file.getName().toLowerCase().endsWith( FILESUFFIX_XLSX );
  }
  
  private Workbook newWorkbookFrom( InputStream inputStream ) throws IOException
  {
    //
    Workbook retval = null;
    
    //
    if ( this.useXLSXFileFormat() )
    {
      retval = new XSSFWorkbook( inputStream );
    }
    else
    {
      retval = new HSSFWorkbook( new POIFSFileSystem( inputStream ) );
    }
    
    //
    return retval;
  }
  
  private Workbook newWorkbookToWrite()
  {
    //
    Workbook retval = null;
    
    //
    if ( this.useXLSXFileFormat() )
    {
      retval = new SXSSFWorkbook();
    }
    else
    {
      retval = new HSSFWorkbook();
    }
    
    //
    return retval;
  }
  
  /**
   * Stores the data from the object onto disk.
   */
  public void store()
  {
    Workbook wb = this.newWorkbookToWrite();
    CreationHelper createHelper = wb.getCreationHelper();
    Sheet sheet = wb.createSheet( "all" );
    
    int lineNumber = 0;
    for ( TableRow iLine : this.tableRowList )
    {
      //
      Row row = sheet.createRow( lineNumber++ );
      
      //
      int cellIndex = 0;
      for ( String iCellText : iLine )
      {
        Cell cell = row.createCell( cellIndex++ );
        cell.setCellValue( createHelper.createRichTextString( iCellText ) );
      }
    }
    
    try
    {
      final FileOutputStream fileOutputStream = new FileOutputStream( this.file );
      final OutputStream outputStream = new BufferedOutputStream( fileOutputStream );
      wb.write( outputStream );
      outputStream.close();
      fileOutputStream.close();
    }
    catch ( FileNotFoundException e )
    {
      e.printStackTrace();
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
    
  }
  
  public static boolean isXLSFile( File file )
  {
    //
    boolean retval = false;
    
    //
    retval = ( file != null )
             && file.exists()
             && file.isFile()
             && ( file.getAbsolutePath().toLowerCase().endsWith( FILESUFFIX_XLS ) || file.getAbsolutePath()
                                                                                         .toLowerCase()
                                                                                         .endsWith( FILESUFFIX_XLSX ) );
    
    //
    return retval;
  }
  
  public List<TableRow> getTableRowList()
  {
    return this.tableRowList;
  }
  
  public File getFile()
  {
    return this.file;
  }
  
  public void setFile( File file )
  {
    this.file = file;
  }
  
  public void clear()
  {
    this.tableRowList.clear();
  }
  
}
