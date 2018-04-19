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
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.omnaest.i18nbinder.internal.LocaleFilter;
import org.omnaest.i18nbinder.internal.ModifierHelper;

/**
 * Goal which executes the i18nBinder property files write back from the xls file
 *
 * @author <a href="mailto:awonderland6@googlemail.com">Danny Kunz</a>
 */
@Mojo(name = "import-xls")
public class ImportXlsMojo extends AbstractMojo {

  /**
   * Location of the output directory root.
   */
  //FIXME: Diesen Parameter sollte es nicht geben. Stattdessen nur das explizite XLS-File
  @Parameter(property="outputDirectory", defaultValue="${project.build.directory}", required = true)
  private File    xlsOutputDirectory;

  /**
   * Location of the source i18n files.
   */
  @Parameter(property = "propertiesRootDirectory", defaultValue = "src/main/resources/i18n")
  private File propertiesRootDirectory ;

  @Parameter(property="verbose", defaultValue="false")
  private boolean verbose;

  //FIXME: Should be taken from jaxb2 maven plugin
  //FIXME: Die werden hier gar nicht genutzt. Aber das sollte auch eingeschränkt werden können
//  @Parameter
  private String[] i18nIncludes;

//  @Parameter
  private String[] i18nExcludes;


  //@Parameter(defaultValue = ".*")
  //FIXME: SOllte wegfallen
  private String localeFilterRegex= ".*";

  @Parameter(property = "propertyFileEncoding")
  private String propertyFileEncoding;

  @Parameter(property = "xlsFileEncoding", defaultValue="UTF-8")
  private String xlsFileEncoding;

  @Parameter(property = "xlsFileName", defaultValue="i18n.xls")
  private String xlsFileName;

  @Parameter(property = "deleteEmptyProperties", defaultValue = "false")
  private boolean deleteEmptyProperties;

  //@Parameter(defaultValue = "true")
  private boolean useJavaStyleUnicodeEscaping    = true;

  /* *************************************************** Methods **************************************************** */

  @Override
  public void execute() throws MojoExecutionException
  {

    //
    this.getLog().info( "Write properties from XLS file back to property files..." );
    this.logConfigurationProperties();

    //
    final LocaleFilter localeFilter = this.determineLocaleFilter();
    try
    {
      //
      if ( this.xlsFileName != null )
      {
        //
        File file = new File( this.xlsOutputDirectory, this.xlsFileName );
        this.getLog().info( "Looking for xls file at:" + file );
        if ( file.exists() )
        {
          ModifierHelper.writeXLSFileContentToPropertyFiles(propertiesRootDirectory.toPath(), file, this.propertyFileEncoding, localeFilter,
                                                             this.deleteEmptyProperties,
                                                             this.useJavaStyleUnicodeEscaping );
        }
      }

    }
    catch ( Exception e )
    {
      this.getLog().error( "Could not write properties from xls", e );
    }

    //
    this.getLog().info( "...done" );

  }

  /**
   *
   */
  private void logConfigurationProperties()
  {
    this.getLog().info( "localeFilterRegex=" + this.localeFilterRegex );
    this.getLog().info( "xlsOutputDirectory=" + this.xlsOutputDirectory );
    this.getLog().info( "xlsFileName=" + this.xlsFileName );
    this.getLog().info( "useJavaStyleUnicodeEscaping=" + this.useJavaStyleUnicodeEscaping );

  }

  private LocaleFilter determineLocaleFilter()
  {
    final LocaleFilter localeFilter = new LocaleFilter();
    localeFilter.setPattern( Pattern.compile( this.localeFilterRegex ) );
    return localeFilter;
  }

}
