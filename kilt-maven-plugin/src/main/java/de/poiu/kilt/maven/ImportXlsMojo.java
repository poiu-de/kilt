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
import de.poiu.apron.MissingKeyAction;
import java.io.File;
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
 * Imports the translations from an XLS file back into the resource bundle
 * files.
 */
@Mojo(name = "import-xls")
public class ImportXlsMojo extends AbstractKiltMojo {

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

    System.out.println("MIKIAK: "+this.missingKeyAction);

    this.getLog().info("Importing translated properties from XLS.");

    final File file = new File(this.xlsOutputDirectory, this.xlsFileName);
    if (!file.exists()) {
      throw new RuntimeException("XLS file "+file.getAbsolutePath()+" does not exist.");
    }

    //TODO: Hier müsste ich einschränken können, welche Ressourcen importiert werden sollen

    XlsImExporter.importXls(propertiesRootDirectory.toPath(),
                            file,
                            this.propertyFileEncoding != null ? Charset.forName(this.propertyFileEncoding) : null,
                            this.missingKeyAction);

    this.getLog().info("...done");
  }


  private Set<File> getIncludedPropertyFiles(final File propertiesRootDirectory) {
    //FIXME: Hier d[rfte es keine Warnungen geben. Das wird halt alles erstellt.
    //       Doch! Warnung, wenn matchingFiles is empty. Aber das weiß ich doch erst _nach_ der Bearbeitung!
    if (!propertiesRootDirectory.exists()) {
      this.getLog().warn("resource bundle directory "+propertiesRootDirectory+" does not exist. No resources will be imported.");
      return ImmutableSet.of();
    }

    final Set<File> matchingFiles= new LinkedHashSet<>();

    final DirectoryScanner directoryScanner = new DirectoryScanner();
    directoryScanner.setIncludes(this.i18nIncludes);
    directoryScanner.setExcludes(this.i18nExcludes);
    directoryScanner.setBasedir(propertiesRootDirectory);
    directoryScanner.scan();

    final String[] fileNames= directoryScanner.getIncludedFiles();
    for (String fileName : fileNames) {
      if (this.verbose) {
        this.getLog().info("Including in facade: " + fileName);
      }
      matchingFiles.add(new File(propertiesRootDirectory, fileName));
    }

    if (matchingFiles.isEmpty()) {
      this.getLog().warn("No resource bundles found. No resources will be imported.");
    }

    return matchingFiles;
  }
}
