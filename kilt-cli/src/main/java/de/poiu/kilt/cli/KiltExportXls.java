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
package de.poiu.kilt.cli;

import de.poiu.kilt.cli.config.KiltProperty;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.DirectoryScanner;
import de.poiu.kilt.internal.XlsImExporter;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


/**
 *
 * @author mherrn
 */
@Command(name = "export-xls",
         description= "Exports Java i18n resource bundle files to XLS(X)")
public class KiltExportXls extends AbstractKiltCommand implements Runnable {

  private static final Logger LOGGER= LogManager.getLogger();


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  @Option(names = {"-x", "--xlsFile"}, description= "The XLS(X) file to export to. (default: ${DEFAULT-VALUE})")
  private Path xlsFile= Paths.get("i18n.xlsx");


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  public KiltExportXls() {
    super();

    if (propsFromFile.containsKey(KiltProperty.XLS_FILE.getKey())) {
      this.xlsFile= Paths.get(propsFromFile.getProperty(KiltProperty.XLS_FILE.getKey()));
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void run() {
    if (this.verbose) {
      Configurator.setLevel(LogManager.getLogger("de.poiu.kilt").getName(), Level.DEBUG);
    }

    if (this.verbose) {
      printProperties();
    }

    final Set<File> propertyFileSet = this.getIncludedPropertyFiles(this.propertiesRootDirectory);
    LOGGER.log(Level.INFO, "Exporting the following files to XLS: "+propertyFileSet);

    try {
      Files.createDirectories(this.xlsFile.toAbsolutePath().getParent());

      XlsImExporter.exportXls(this.propertiesRootDirectory,
                              propertyFileSet,
                              this.propertyFileEncoding,
                              this.xlsFile);
    } catch (IOException e) {
      throw new RuntimeException("Error exporting property files to XLS.", e);
    }
  }


  //FIXME: This is a duplication of code from the maven mojo. That should be avoided
  private Set<File> getIncludedPropertyFiles(final Path propertiesRootDirectory) {
    if (!propertiesRootDirectory.toFile().exists()) {
      LOGGER.warn("resource bundle directory " + propertiesRootDirectory + " does not exist. Nothing will be exported.");
      return ImmutableSet.of();
    }

    final Set<File> matchingFiles = new LinkedHashSet<>();

    final DirectoryScanner directoryScanner = new DirectoryScanner();
    directoryScanner.setIncludes(this.i18nIncludes);
    directoryScanner.setExcludes(this.i18nExcludes);
    directoryScanner.setBasedir(propertiesRootDirectory.toFile());
    directoryScanner.scan();

    final String[] fileNames = directoryScanner.getIncludedFiles();
    for (String fileName : fileNames) {
      if (this.verbose) {
        LOGGER.info("Including in XLS: " + fileName);
      }
      matchingFiles.add(propertiesRootDirectory.resolve(fileName).toFile());
    }

    if (matchingFiles.isEmpty()) {
      LOGGER.warn("No resource bundles found. Nothing will be exported.");
    }

    return matchingFiles;
  }


  private void printProperties(){
    final StringBuilder sb= new StringBuilder();

    sb.append("verbose                 = ").append(this.verbose).append("\n");
    sb.append("propertiesRootDirectory = ").append(this.propertiesRootDirectory).append("\n");
    sb.append("i18nIncludes            = ").append(Joiner.on(", ").join(this.i18nIncludes)).append("\n");
    sb.append("i18nExcludes            = ").append(Joiner.on(", ").join(this.i18nExcludes)).append("\n");
    sb.append("propertyFileEncoding    = ").append(this.propertyFileEncoding).append("\n");
    sb.append("xlsFile                 = ").append(this.xlsFile.toAbsolutePath()).append("\n");

    System.out.println(sb.toString());
  }
}
