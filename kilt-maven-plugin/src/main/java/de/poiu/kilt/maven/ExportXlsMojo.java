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

import de.poiu.kilt.importexport.XlsImExporter;
import de.poiu.kilt.util.FileMatcher;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


/**
 * Exports the translations in the resource bundle files into an XLS file.
 */
@Mojo(name = "export-xls")
public class ExportXlsMojo extends AbstractKiltMojo {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * The XLS(X) file to export to.
   */
  @Parameter(property = "xlsFile", required= true, defaultValue = "${project.build.directory}/i18n.xlsx")
  private File xlsFile;


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

    this.getLog().info("Exporting properties to XLS.");

    final FileMatcher fileMatcher= new FileMatcher(this.propertiesRootDirectory.toPath(),this.i18nIncludes, this.i18nExcludes);

    try {
      Files.createDirectories(this.xlsFile.getAbsoluteFile().getParentFile().toPath());

      XlsImExporter.exportXls(fileMatcher,
                              this.propertyFileEncoding != null ? Charset.forName(this.propertyFileEncoding) : null,
                              this.xlsFile);
    } catch (IOException e) {
      throw new RuntimeException("Error exporting property files to XLS.", e);
    }

    this.getLog().info("...done");
  }
}
