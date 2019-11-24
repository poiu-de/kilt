/*
 * Copyright (C) 2018 Marco Herrn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.poiu.kilt.maven;

import de.poiu.apron.MissingKeyAction;
import de.poiu.kilt.importexport.XlsImExporter;
import de.poiu.kilt.util.FileMatcher;
import java.io.File;
import java.nio.charset.Charset;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


/**
 * Imports the translations from an XLS file back into the resource bundle
 * files.
 */
@Mojo(name = "import-xls")
public class ImportXlsMojo extends AbstractKiltMojo {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * The XLS(X) file to import from.
   */
  @Parameter(property = "xlsFile", required= true, defaultValue = "${project.build.directory}/i18n.xlsx")
  private File xlsFile;



  /**
   * How to handle key-value-pairs that exist in the .properties file, but not in the XLS(S) file
   * to import.
   * <p>
   * The following values are valid:
   * <ul>
   *  <li>NOTHING: Leave exising key-value-pairs as they are</li>
   *  <li>DELETE: Delete the missing key-value-pairs</li>
   *  <li>COMMENT: Comment out the missing key-value-pairs</li>
   * </ul>
   *
   */
  @Parameter(property = "missingKeyAction", defaultValue = "NOTHING")
  private MissingKeyAction missingKeyAction;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void execute() throws MojoExecutionException {
    if (this.verbose) {
      Configurator.setLevel(LogManager.getLogger("de.poiu.kilt").getName(), Level.DEBUG);
    }

    this.getLog().info("Importing translated properties from XLS.");

    if (!this.xlsFile.exists()) {
      throw new RuntimeException("XLS file "+this.xlsFile.getAbsolutePath()+" does not exist.");
    }

    final FileMatcher fileMatcher= new FileMatcher(this.propertiesRootDirectory.toPath(),this.i18nIncludes, this.i18nExcludes);

    XlsImExporter.importXls(fileMatcher,
                            this.xlsFile,
                            this.propertyFileEncoding != null ? Charset.forName(this.propertyFileEncoding) : null,
                            this.missingKeyAction);

    this.getLog().info("...done");
  }
}
