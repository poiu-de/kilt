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
package org.omnaest.i18nbinder;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.omnaest.i18nbinder.internal.Language;
import org.omnaest.i18nbinder.internal.LocaleFilter;
import org.omnaest.i18nbinder.internal.ModifierHelper;
import org.omnaest.i18nbinder.internal.XLSFile;
import org.omnaest.i18nbinder.internal.facade.creation.FacadeBundleContent;
import org.omnaest.i18nbinder.internal.facade.creation.FacadeBundleContentHelper;
import org.omnaest.i18nbinder.internal.facade.creation.FacadeCreator;

public class I18nBinder extends Task
{
  /* ********************************************** Variables ********************************************** */
  private List<FileSet> fileSetList                              = new ArrayList<FileSet>();

  private boolean       createXLSFile                            = false;
  private String        xlsFileName                              = null;

  private String        javaFileEncoding                         = "utf-8";                                                      ;

  private LocaleFilter  localeFilter                             = new LocaleFilter();
  private boolean       deletePropertiesWithBlankValue           = true;

  private String        fileNameLocaleGroupPattern               = null;
  private List<Integer> fileNameLocaleGroupPatternGroupIndexList = null;

  private boolean       createJavaFacade                         = false;
  private String        javaFacadeFileName                       = "I18n";
  private String        baseNameInTargetPlattform                = "";
  private String        baseFolderIgnoredPath                    = "";
  private String        packageName                              = "org.omnaest.i18nbinder.facade";
  private Path          facadeGenerationDir                      = Paths.get("generated-sources");
  private boolean       externalizeTypes;

  private String        propertyFileEncoding                     = "utf-8";

  private boolean       useJavaStyleUnicodeEscaping              = false;

  /**
   * Whether to copy the facade accessor class and the base interface I18nBundleKey to the
   * generation target dir.
   * This is only useful if it is necessary to avoid a runtime dependency on i18nbinder-runtime.
   */
  private boolean copyFacadeAccessorClasses;

  /**
   * The name of the facade accessor class when copying the facade accessor classes.
   * This is only meaningful in combination with {@link #copyFacadeAccessorClasses}.
   */
  private String facadeAccessorClassName;

  /* ********************************************** Methods ********************************************** */
  @Override
  public void execute() throws BuildException
  {
    //
    super.execute();

    //
    this.run();
  }

  public void run()
  {

    //
    if ( this.fileSetList.size() > 0 )
    {
      //
      if ( this.createJavaFacade )
      {
        this.createJavaFacadeFromFiles();
      }

      //
      if ( this.createXLSFile )
      {
        this.createXLSFileFromFiles();
      }
    }
    else
    {
      this.writeXLSFileBackToFiles();
    }

  }

  protected void writeXLSFileBackToFiles()
  {
    //
    if ( this.xlsFileName != null )
    {
      //
      this.log( "Write properties from XLS file back to property files..." );

      //
      File file = new File( this.xlsFileName );
      if ( file.exists() )
      {
        ModifierHelper.writeXLSFileContentToPropertyFiles( file, this.propertyFileEncoding, this.localeFilter,
                                                           this.deletePropertiesWithBlankValue, this.useJavaStyleUnicodeEscaping );
      }

      //
      this.log( "...done" );
    }
  }

  /**
   * Parses the property files and creates the xls file.
   */
  protected void createXLSFileFromFiles()
  {
    //
    if ( this.xlsFileName != null && this.fileSetList.size() > 0 )
    {
      //
      this.log( "Create XLS file from property files..." );

      //
      Set<File> propertyFileSet = this.resolveFilesFromFileSetList( this.fileSetList );

      //
      XLSFile xlsFile = ModifierHelper.createXLSFileFromPropertyFiles(Paths.get(this.baseFolderIgnoredPath), propertyFileSet, this.propertyFileEncoding,
                                                                       this.localeFilter, this.fileNameLocaleGroupPattern,
                                                                       this.fileNameLocaleGroupPatternGroupIndexList,
                                                                       this.useJavaStyleUnicodeEscaping );

      //
      File file = new File( this.xlsFileName );
      xlsFile.setFile( file );
      xlsFile.store();

      //
      this.log( "...done" );
    }
    else
    {
      this.log( "No xls file name specified. Please provide a file name for the xls file which should be created.",
                Project.MSG_ERR );
    }

  }

  protected void createJavaFacadeFromFiles() {
      this.log("Create Java source code facade file from property files.");

      final Set<File> propertyFileSet = this.resolveFilesFromFileSetList( this.fileSetList );

      try {
        final FacadeBundleContentHelper fbcHelper = new FacadeBundleContentHelper(Paths.get(baseFolderIgnoredPath));
        final Map<String, Map<Language, File>> bundleNameToFilesMap = fbcHelper.toBundleNameToFilesMap(propertyFileSet);

        final FacadeCreator facadeCreator = new FacadeCreator();
        for (final Map.Entry<String, Map<Language, File>> entry : bundleNameToFilesMap.entrySet()) {
          final String bundleName = entry.getKey();
          final Map<Language, File> bundleTranslations = entry.getValue();

          final FacadeBundleContent resourceBundleContent = FacadeBundleContent.forName(bundleName).fromFiles(bundleTranslations);
          final TypeSpec resourceBundleEnumTypeSpec = facadeCreator.createFacadeEnumFor(resourceBundleContent);
          final JavaFile javaFile = JavaFile.builder(packageName, resourceBundleEnumTypeSpec).build();
          javaFile.writeTo(Paths.get(""));
          // TODO: To allow for custom charsets, we need to call javaFile.toString.getBytes(Charset), but this involves
          //       creating the directoy structure and identifying the correct file name.
        }

        // copy the facade accessor classes if requested
        if (copyFacadeAccessorClasses) {
          facadeCreator.copyFacadeAccessorTemplates(facadeAccessorClassName, packageName, facadeGenerationDir);
        }
      } catch (Exception e) {
        this.log("Could not write Java facade to file", e, Project.MSG_ERR);
        e.printStackTrace();
      }

      this.log("...done");
  }

  private String reduceFileNameAndCreateDirectoryPath( String fileName )
  {
    fileName = StringUtils.removeStart( fileName, this.packageName + "." );
    File directory = new File( this.baseNameInTargetPlattform );
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
    return fileName;
  }

  /**
   * @see #resolveFilesFromFileSet(FileSet)
   * @param fileSetList
   * @return
   */
  protected Set<File> resolveFilesFromFileSetList( List<FileSet> fileSetList )
  {
    //
    Set<File> retset = new HashSet<File>();

    //
    if ( fileSetList != null )
    {
      for ( FileSet fileSet : fileSetList )
      {
        retset.addAll( this.resolveFilesFromFileSet( fileSet ) );
      }
    }

    //
    return retset;
  }

  /**
   * @see #resolveFilesFromFileSetList(List)
   * @param fileSet
   * @return
   */
  protected List<File> resolveFilesFromFileSet( FileSet fileSet )
  {
    //
    List<File> retlist = new ArrayList<File>();

    //
    if ( fileSet != null )
    {
      //
      DirectoryScanner directoryScanner = fileSet.getDirectoryScanner();
      String[] includedFileNames = directoryScanner.getIncludedFiles();

      //
      if ( includedFileNames != null )
      {
        //
        File basedir = directoryScanner.getBasedir();

        //
        for ( String fileNameUnnormalized : includedFileNames )
        {
          //
          String fileName = fileNameUnnormalized.replaceAll( Pattern.quote( "\\" ), "/" );

          //
          File file = new File( basedir, fileName );
          if ( file.exists() )
          {
            retlist.add( file );
          }
        }
      }

    }

    //
    return retlist;
  }

  public void addFileset( FileSet fileset )
  {
    if ( fileset != null )
    {
      this.fileSetList.add( fileset );
    }
  }

  public String getXlsFileName()
  {
    return this.xlsFileName;
  }

  public void setXlsFileName( String xlsFileName )
  {
    this.log( "xlsFileName=" + xlsFileName );
    this.xlsFileName = xlsFileName;
  }

  public void setFileEncoding( String fileEncoding )
  {
    this.log( "fileEncoding=" + fileEncoding );
    this.propertyFileEncoding = fileEncoding;
    this.javaFileEncoding = fileEncoding;
  }

  public String getLocaleFilterRegex()
  {
    return this.localeFilter.getPattern().pattern();
  }

  public void setLocaleFilterRegex( String localeFilterRegex )
  {
    this.log( "localeFilterRegex=" + localeFilterRegex );
    this.localeFilter.setPattern( Pattern.compile( localeFilterRegex ) );
  }

  public String getFileNameLocaleGroupPattern()
  {
    return this.fileNameLocaleGroupPattern;
  }

  public void setFileNameLocaleGroupPattern( String fileNameLocaleGroupPattern )
  {
    //
    this.log( "fileNameLocaleGroupPattern=" + fileNameLocaleGroupPattern );

    //
    this.fileNameLocaleGroupPattern = fileNameLocaleGroupPattern;

    //
    if ( this.fileNameLocaleGroupPatternGroupIndexList == null )
    {
      this.fileNameLocaleGroupPatternGroupIndexList = Arrays.asList( 1 );
    }
  }

  public boolean isDeletePropertiesWithBlankValue()
  {
    return this.deletePropertiesWithBlankValue;
  }

  public void setDeletePropertiesWithBlankValue( boolean deletePropertiesWithBlankValue )
  {
    this.log( "deletePropertiesWithBlankValue=" + deletePropertiesWithBlankValue );
    this.deletePropertiesWithBlankValue = deletePropertiesWithBlankValue;
  }

  public String getFileNameLocaleGroupPatternGroupIndexList()
  {
    return StringUtils.join( this.fileNameLocaleGroupPatternGroupIndexList, "," );
  }

  public void setFileNameLocaleGroupPatternGroupIndexList( String fileNameLocaleGroupPatternGroupIndexStringList )
  {
    //
    this.log( "fileNameLocaleGroupPatternGroupIndexList=" + fileNameLocaleGroupPatternGroupIndexStringList );
    String[] tokens = StringUtils.split( fileNameLocaleGroupPatternGroupIndexStringList.replaceAll( ";", "," ), "," );

    //
    List<Integer> fileNameLocaleGroupPatternGroupIndexList = new ArrayList<Integer>();
    for ( String token : tokens )
    {
      fileNameLocaleGroupPatternGroupIndexList.add( Integer.valueOf( token ) );
    }

    //
    this.fileNameLocaleGroupPatternGroupIndexList = fileNameLocaleGroupPatternGroupIndexList;
  }

  public void setCreateXLSFile( boolean createXLSFile )
  {
    this.log( "createXLSFile=" + createXLSFile );
    this.createXLSFile = createXLSFile;
  }

  public void setCreateJavaFacade( boolean createJavaFacade )
  {
    this.log( "createJavaFacade=" + createJavaFacade );
    this.createJavaFacade = createJavaFacade;
  }

  public void setJavaFacadeFileName( String javaFacadeFileName )
  {
    this.log( "javaFacadeFileName=" + javaFacadeFileName );
    this.javaFacadeFileName = javaFacadeFileName;
  }

  public void setBaseNameInTargetPlattform( String baseNameInTargetPlattform )
  {
    this.log( "baseNameInTargetPlattform=" + baseNameInTargetPlattform );
    this.baseNameInTargetPlattform = baseNameInTargetPlattform;
  }

  public void setBaseFolderIgnoredPath( String baseFolderIgnoredPath )
  {
    this.log( "baseFolderIgnoredPath=" + baseFolderIgnoredPath );
    this.baseFolderIgnoredPath = baseFolderIgnoredPath;
  }

  public void setPackageName( String packageName )
  {
    org.omnaest.i18nbinder.internal.facade.creation.Objects.requireNonWhitespace(packageName, "packageName may not be empty");
    this.log( "packageName=" + packageName );
    this.packageName = packageName;
  }

  public void setExternalizeTypes( boolean externalizeTypes )
  {
    this.log( "externalizeTypes=" + externalizeTypes );
    this.externalizeTypes = externalizeTypes;
  }

  public void setPropertyFileEncoding( String propertyFileEncoding )
  {
    this.log( "propertyFileEncoding=" + propertyFileEncoding );
    this.propertyFileEncoding = propertyFileEncoding;
  }

  public void setJavaFileEncoding( String javaFileEncoding )
  {
    this.log( "javaFileEncoding=" + javaFileEncoding );
    this.javaFileEncoding = javaFileEncoding;
  }

  public void setUseJavaStyleUnicodeEscaping( boolean useJavaStyleUnicodeEscaping )
  {
    this.useJavaStyleUnicodeEscaping = useJavaStyleUnicodeEscaping;
  }

  public void setCopyFacadeAccessorClasses(final boolean copyFacadeAccessorClasses) {
    this.log("copyFacadeAccessorClasses=" + copyFacadeAccessorClasses);
    this.copyFacadeAccessorClasses= copyFacadeAccessorClasses;
  }

  public void setFacadeAccessorClassName(final String facadeAccessorClassName) {
    this.log("facadeAccessorClassName=" + facadeAccessorClassName);
    this.facadeAccessorClassName= facadeAccessorClassName;
  }

  public void setFacadeGenerationDir(final String facadeGenerationDir) {
    this.log("facadeGenerationDir=" + facadeGenerationDir);
    this.facadeGenerationDir= Paths.get(facadeGenerationDir);
  }
}
