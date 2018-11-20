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
import de.poiu.apron.MissingKeyAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.poiu.kilt.internal.XlsImExporter;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.codehaus.plexus.util.DirectoryScanner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


/**
 *
 * @author mherrn
 */
@Command(name = "import-xls",
         description= "Imports Java i18n resource bundle files back from XLS(X)")
public class KiltImportXls extends AbstractKiltCommand implements Runnable {

  private static final Logger LOGGER= LogManager.getLogger();

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  @Option(names = {"-x", "--xlsFile"}, description= "The XLS(X) file to import. (default: ${DEFAULT-VALUE})")
  private Path xlsFile= Paths.get("i18n.xlsx");


  @Option(names = {"-m", "--missingKeyAction"}, description= "What to do with keys that exist in the .properties file, but not in the XLS(X) that is about to be imported. default: ${DEFAULT-VALUE})")
  private MissingKeyAction missingKeyAction= MissingKeyAction.NOTHING;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  public KiltImportXls() {
    super();

    if (propsFromFile.containsKey(KiltProperty.XLS_FILE.getKey())) {
      this.xlsFile= Paths.get(propsFromFile.getProperty(KiltProperty.XLS_FILE.getKey()));
    }

    if (propsFromFile.containsKey(KiltProperty.MISSING_KEY_ACTION.getKey())) {
      this.missingKeyAction= MissingKeyAction.valueOf(propsFromFile.getProperty(KiltProperty.MISSING_KEY_ACTION.getKey()).toUpperCase());
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

    if (!this.xlsFile.toFile().exists()) {
      throw new RuntimeException("XLS(X) file "+this.xlsFile.toAbsolutePath()+" does not exist.");
    }

    //TODO: Hier müsste ich einschränken können, welche Ressourcen importiert werden sollen
    //FIXME: Das ist noch nicht richtig rund. Damit kann ich nichts importieren, was nicht schon da ist
    //       Sollte ich besser den String-Parameter direkt übergeben? Wie ist das dann mit Ant und Maven
    final Set<File> propertyFileSet = this.getIncludedPropertyFiles(this.propertiesRootDirectory);
    LOGGER.log(Level.INFO, "Importing the following files from XLS: "+propertyFileSet);

    XlsImExporter.importXls(propertiesRootDirectory,
                                this.xlsFile.toFile(),
                                propertyFileSet,
                                this.propertyFileEncoding,
                                this.missingKeyAction);
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
    sb.append("missingKeyAction        = ").append(this.missingKeyAction).append("\n");

    System.out.println(sb.toString());
  }
}
