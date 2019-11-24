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

import com.google.common.collect.ImmutableSet;
import de.poiu.kilt.util.PathUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import de.poiu.kilt.internal.XlsImExporter;
import java.nio.charset.Charset;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;


/**
 * Exports the translations in the resource bundle files into an XLS file.
 */
@Mojo(name = "export-xls")
public class ExportXlsMojo extends AbstractKiltMojo {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * Location of the output directory root.
   */
  @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}", required = true)
  private File xlsOutputDirectory;


  @Parameter(property = "xlsFileName", required= true, defaultValue = "i18n.xlsx")
  private String xlsFileName;


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

    final Set<File> propertyFileSet = PathUtils.getIncludedPropertyFiles(this.propertiesRootDirectory.toPath(), this.i18nIncludes, this.i18nExcludes);
    this.getLog().info("Exporting the following files to XLS: "+propertyFileSet);



    try {
      Files.createDirectories(this.xlsOutputDirectory.toPath());
      final File file = new File(this.xlsOutputDirectory, this.xlsFileName);

      XlsImExporter.exportXls(this.propertiesRootDirectory.toPath(),
                              propertyFileSet,
                              this.propertyFileEncoding != null ? Charset.forName(this.propertyFileEncoding) : null,
                              file.toPath());
    } catch (IOException e) {
      throw new RuntimeException("Error exporting property files to XLS.", e);
    }

    this.getLog().info("...done");
  }
}
