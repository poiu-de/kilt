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
@Mojo(name = "write-properties")
public class CreatePropertiesFromXlsMojo extends AbstractMojo
{
  /* ************************************** Variables / State (internal/hiding) ************************************* */
  /**
   * Location of the output directory root
   */
  @Parameter(defaultValue="${project.build.directory}", required = true)
  private File    xlsOutputDirectory;

  @Parameter(defaultValue = ".*")
  private String  localeFilterRegex              = ".*";

  @Parameter(defaultValue = "i18n.xls")
  private String  xlsFileName                    = "i18n.xls";

  @Parameter(defaultValue = "utf-8")
  private String  propertyFileEncoding           = "utf-8";

  @Parameter(defaultValue = "true")
  private boolean deletePropertiesWithBlankValue = true;

  @Parameter(defaultValue = "false")
  private boolean useJavaStyleUnicodeEscaping    = false;

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
          ModifierHelper.writeXLSFileContentToPropertyFiles( file, this.propertyFileEncoding, localeFilter,
                                                             this.deletePropertiesWithBlankValue,
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
