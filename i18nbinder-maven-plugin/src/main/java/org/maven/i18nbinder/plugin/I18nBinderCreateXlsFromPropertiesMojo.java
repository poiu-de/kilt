/*******************************************************************************
 * Copyright 2012 Danny Kunz
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
package org.maven.i18nbinder.plugin;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.omnaest.i18nbinder.internal.LocaleFilter;
import org.omnaest.i18nbinder.internal.ModifierHelper;
import org.omnaest.i18nbinder.internal.XLSFile;

/**
 * Goal which executes the i18nBinder xls file generation
 * 
 * @goal create-xls
 * @author <a href="mailto:awonderland6@googlemail.com">Danny Kunz</a>
 */
public class I18nBinderCreateXlsFromPropertiesMojo extends AbstractMojo
{
  
  /* ************************************** Variables / State (internal/hiding) ************************************* */
  /**
   * Location of the output directory root
   * 
   * @parameter expression="${project.build.directory}"
   * @required
   */
  private File          xlsOutputDirectory;
  
  /**
   * Location of the source i18n files.
   * 
   * @parameter
   */
  private File          propertiesRootDirectory                  = new File( "src/main/resources/i18n" );
  
  /**
   * @parameter
   */
  private String        localeFilterRegex                        = ".*";
  
  /**
   * @parameter
   */
  private String        fileNameLocaleGroupPattern               = ".*?((_\\w{2,3}_\\w{2,3})|(_\\w{2,3})|())\\.properties";
  
  /**
   * @parameter
   */
  private List<Integer> fileNameLocaleGroupPatternGroupIndexList = Arrays.asList( 2, 3, 4 );
  
  /**
   * @parameter
   */
  private boolean       logResolvedPropertyFileNames             = false;
  
  /**
   * @parameter
   */
  private boolean       useJavaStyleUnicodeEscaping              = false;
  
  /**
   * @parameter
   */
  private String        xlsFileName                              = "i18n.xls";
  
  /**
   * @parameter
   */
  private String        propertyFileEncoding                     = "utf-8";
  
  /* *************************************************** Methods **************************************************** */
  
  @Override
  public void execute() throws MojoExecutionException
  {
    
    //
    this.getLog().info( "Create XLS file from property files..." );
    this.logConfigurationProperties();
    
    //        
    final LocaleFilter localeFilter = this.determineLocaleFilter();
    final Set<File> propertyFileSet = this.resolveFilesFromDirectoryRoot( this.propertiesRootDirectory );
    
    try
    {
      if ( this.xlsFileName != null && !propertyFileSet.isEmpty() )
      {
        //
        XLSFile xlsFile = ModifierHelper.createXLSFileFromPropertyFiles( propertyFileSet, this.propertyFileEncoding,
                                                                         localeFilter, this.fileNameLocaleGroupPattern,
                                                                         this.fileNameLocaleGroupPatternGroupIndexList,
                                                                         this.useJavaStyleUnicodeEscaping );
        
        //
        File file = new File( this.xlsOutputDirectory, this.xlsFileName );
        xlsFile.setFile( file );
        xlsFile.store();
        
      }
      else
      {
        this.getLog().error( "No xls file name specified. Please provide a file name for the xls file which should be created." );
      }
      
    }
    catch ( Exception e )
    {
      this.getLog().error( "Could not write xls file", e );
    }
    
    //
    this.getLog().info( "...done" );
    
  }
  
  /**
   * 
   */
  private void logConfigurationProperties()
  {
    this.getLog().info( "fileNameLocaleGroupPattern=" + this.fileNameLocaleGroupPattern );
    this.getLog().info( "fileNameLocaleGroupPatternGroupIndexList=" + this.fileNameLocaleGroupPatternGroupIndexList );
    this.getLog().info( "localeFilterRegex=" + this.localeFilterRegex );
    this.getLog().info( "xlsOutputDirectory=" + this.xlsOutputDirectory );
    this.getLog().info( "xlsFileName=" + this.xlsFileName );
    this.getLog().info( "propertiesRootDirectory=" + this.propertiesRootDirectory );
    this.getLog().info( "useJavaStyleUnicodeEscaping=" + this.useJavaStyleUnicodeEscaping );
  }
  
  private Set<File> resolveFilesFromDirectoryRoot( File propertiesRootDirectory )
  {
    //
    final Set<File> retset = new LinkedHashSet<File>();
    
    //
    final String[] includes = { "**/*.properties" };
    DirectoryScanner directoryScanner = new DirectoryScanner();
    {
      directoryScanner.setIncludes( includes );
      directoryScanner.setBasedir( propertiesRootDirectory );
      directoryScanner.setCaseSensitive( true );
      directoryScanner.scan();
    }
    
    //
    final String[] fileNames = directoryScanner.getIncludedFiles();
    for ( int i = 0; i < fileNames.length; i++ )
    {
      final String fileName = fileNames[i].replaceAll( "\\\\", "/" );
      if ( this.logResolvedPropertyFileNames )
      {
        this.getLog().info( "Resolved: " + fileName );
      }
      retset.add( new File( propertiesRootDirectory, fileName ) );
    }
    
    // 
    return retset;
  }
  
  private LocaleFilter determineLocaleFilter()
  {
    final LocaleFilter localeFilter = new LocaleFilter();
    localeFilter.setPattern( Pattern.compile( this.localeFilterRegex ) );
    return localeFilter;
  }
  
}
