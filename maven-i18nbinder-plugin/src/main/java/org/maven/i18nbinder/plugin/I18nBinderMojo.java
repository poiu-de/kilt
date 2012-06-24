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
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.omnaest.i18nbinder.internal.FacadeCreatorHelper;
import org.omnaest.i18nbinder.internal.LocaleFilter;

/**
 * Goal which executes the i18nBinder java facade generator
 * 
 * @goal i18nbinder
 * @phase generate-sources
 * @requiresDependencyResolution compile
 * @author <a href="mailto:awonderland6@googlemail.com">Danny Kunz</a>
 */
public class I18nBinderMojo extends AbstractMojo
{
  /* ************************************************** Constants *************************************************** */
  private static final String GENERATED_SOURCES                        = "generated-sources";
  private static final String GENERATED_SOURCES_I18NBINDER             = "i18nbinder";
  
  /* ************************************** Variables / State (internal/hiding) ************************************* */
  /**
   * Location of the output directory root
   * 
   * @parameter expression="${project.build.directory}"
   * @required
   */
  private File                outputDirectory;
  
  /**
   * Location of the source i18n files.
   * 
   * @parameter
   */
  private File                propertiesRootDirectory                  = new File( "src/main/resources/i18n" );
  
  /**
   * @parameter
   */
  private String              localeFilterRegex                        = ".*";
  
  /**
   * @parameter
   */
  private String              fileNameLocaleGroupPattern               = ".*?((_\\w{2,3}_\\w{2,3})|(_\\w{2,3})|())\\.properties";
  
  /**
   * @parameter
   */
  private List<Integer>       fileNameLocaleGroupPatternGroupIndexList = Arrays.asList( 2, 3, 4 );
  
  /**
   * @parameter
   */
  private boolean             createJavaFacade                         = true;
  
  /**
   * @parameter
   */
  private boolean             logResolvedPropertyFileNames             = false;
  
  /**
   * @parameter
   */
  private String              i18nFacadeName                           = FacadeCreatorHelper.DEFAULT_JAVA_FACADE_FILENAME_I18N_FACADE;
  
  /**
   * @parameter
   */
  private String              baseNameInTargetPlattform                = "i18n";
  
  /**
   * @parameter
   */
  private String              packageName                              = "";
  
  /**
   * @parameter
   */
  private boolean             externalizeTypes                         = true;
  
  /* ***************************** Beans / Services / References / Delegates (external) ***************************** */
  
  /**
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject        project;
  
  /**
   * @parameter
   */
  private String              javaFileEncoding                         = "utf-8";
  
  /**
   * @parameter
   */
  private String              propertyFileEncoding                     = "utf-8";
  
  /* *************************************************** Methods **************************************************** */
  
  @Override
  public void execute() throws MojoExecutionException
  {
    if ( !this.createJavaFacade )
    {
      this.getLog()
          .info( "Skipping to create the i18n Java facade since it is disabled by the createJavaFacade property within the configuration" );
    }
    else
    {
      //
      this.getLog().info( "Create Java source code facade file from property files..." );
      this.logConfigurationProperties();
      //    
      final String baseFolderIgnoredPath = getBaseFolderIgnoredPath();
      final LocaleFilter localeFilter = this.determineLocaleFilter();
      final Set<File> propertyFileSet = this.resolveFilesFromDirectoryRoot( this.propertiesRootDirectory );
      
      try
      {
        //
        final String i18nFacadeName = StringUtils.defaultString( this.i18nFacadeName,
                                                                 FacadeCreatorHelper.DEFAULT_JAVA_FACADE_FILENAME_I18N_FACADE );
        final Map<String, String> facadeFromPropertyFiles = FacadeCreatorHelper.createI18nInterfaceFacadeFromPropertyFiles( propertyFileSet,
                                                                                                                            localeFilter,
                                                                                                                            this.fileNameLocaleGroupPattern,
                                                                                                                            this.fileNameLocaleGroupPatternGroupIndexList,
                                                                                                                            this.baseNameInTargetPlattform,
                                                                                                                            baseFolderIgnoredPath,
                                                                                                                            this.packageName,
                                                                                                                            i18nFacadeName,
                                                                                                                            this.externalizeTypes,
                                                                                                                            this.propertyFileEncoding );
        
        final File generatedSourceDirectory = createGeneratedSourcesI18nBinderDirectory();
        final File targetPackageDirectory = createTargetPackageDirectory( generatedSourceDirectory );
        for ( String fileName : facadeFromPropertyFiles.keySet() )
        {
          //
          final String fileContent = facadeFromPropertyFiles.get( fileName );
          
          //
          if ( fileName.contains( "." ) )
          {
            fileName = reduceFileNameAndCreateDirectoryPath( fileName, targetPackageDirectory );
          }
          
          //        
          final File file = new File( targetPackageDirectory, fileName + ".java" );
          this.writeContentToFile( fileContent, file, fileName );
        }
        
        //
        this.project.addCompileSourceRoot( this.getCompileSourceRoots() );
      }
      catch ( Exception e )
      {
        this.getLog().error( "Could not write Java facade to file", e );
      }
      
      //
      this.getLog().info( "...done" );
    }
  }
  
  private void writeContentToFile( final String fileContent, final File file, String fileName ) throws IOException
  {
    if ( this.logResolvedPropertyFileNames )
    {
      this.getLog().info( "Writes to: " + fileName );
    }
    FileUtils.writeStringToFile( file, fileContent, this.javaFileEncoding );
  }
  
  private String getBaseFolderIgnoredPath()
  {
    try
    {
      return this.propertiesRootDirectory.getCanonicalPath();
    }
    catch ( Exception e )
    {
      this.getLog().error( e );
      return this.propertiesRootDirectory.getAbsolutePath();
    }
    
  }
  
  private File createTargetPackageDirectory( final File generatedSourceDirectory )
  {
    //
    final String[] pathTokens = StringUtils.split( this.packageName, "." );
    File currentDirectory = generatedSourceDirectory;
    for ( String pathToken : pathTokens )
    {
      currentDirectory = new File( currentDirectory, pathToken );
      if ( !currentDirectory.exists() )
      {
        currentDirectory.mkdir();
      }
    }
    
    //
    return new File( generatedSourceDirectory, this.packageName.replaceAll( "\\.", "/" ) );
  }
  
  /**
   * 
   */
  private void logConfigurationProperties()
  {
    this.getLog().info( "baseNameInTargetPlattform=" + this.baseNameInTargetPlattform );
    this.getLog().info( "createJavaFacade=" + this.createJavaFacade );
    this.getLog().info( "javaFileEncoding=" + this.javaFileEncoding );
    this.getLog().info( "externalizeTypes=" + this.externalizeTypes );
    this.getLog().info( "fileNameLocaleGroupPattern=" + this.fileNameLocaleGroupPattern );
    this.getLog().info( "fileNameLocaleGroupPatternGroupIndexList=" + this.fileNameLocaleGroupPatternGroupIndexList );
    this.getLog().info( "i18nFacadeName=" + this.i18nFacadeName );
    this.getLog().info( "localeFilterRegex=" + this.localeFilterRegex );
    this.getLog().info( "outputDirectory=" + this.outputDirectory );
    this.getLog().info( "packageName=" + this.packageName );
    this.getLog().info( "propertiesRootDirectory=" + this.propertiesRootDirectory );
  }
  
  /**
   * @return {@link File}
   */
  private File createGeneratedSourcesI18nBinderDirectory()
  {
    final File generatedSourcesDirectory = new File( this.outputDirectory, GENERATED_SOURCES );
    final File generatedSourcesI18nBinderDirectory = new File( generatedSourcesDirectory,
                                                               I18nBinderMojo.GENERATED_SOURCES_I18NBINDER );
    if ( !generatedSourcesDirectory.exists() )
    {
      generatedSourcesDirectory.mkdir();
    }
    if ( !generatedSourcesI18nBinderDirectory.exists() )
    {
      generatedSourcesI18nBinderDirectory.mkdir();
    }
    return generatedSourcesI18nBinderDirectory;
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
  
  private String reduceFileNameAndCreateDirectoryPath( String fileName, File targetPackageDirectory )
  {
    String reducedFileName = StringUtils.removeStart( fileName, this.packageName + "." );
    
    //
    fileName = reducedFileName;
    File directory = targetPackageDirectory;
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
    
    //
    return reducedFileName.replaceAll( "\\.", "/" );
  }
  
  public String getCompileSourceRoots() throws IOException
  {
    return new File( this.outputDirectory, GENERATED_SOURCES + "/" + GENERATED_SOURCES_I18NBINDER ).getCanonicalPath();
  }
  
}
