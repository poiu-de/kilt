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

import com.google.common.base.Joiner;
import de.poiu.apron.MissingKeyAction;
import de.poiu.kilt.cli.config.KiltProperty;
import de.poiu.kilt.importexport.XlsImExporter;
import de.poiu.kilt.util.FileMatcher;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


/**
 *
 * @author mherrn
 */
@Command(name = "import-xls",
         description= "Imports Java i18n resource bundle files back from XLS(X)",
         sortOptions = false)
public class KiltImportXls extends AbstractKiltCommand implements Runnable {

  private static final Logger LOGGER= LogManager.getLogger();

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  @Option(names = {"-x", "--xlsFile"}, description= "The XLS(X) file to import. (default: ${DEFAULT-VALUE})")
  private Path xlsFile= Paths.get("i18n.xlsx");


  @Option(names = {"-m", "--missingKeyAction"}, description= "What to do with keys that exist in the .properties file, but not in the XLS(X) that is about to be imported. Valid values: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
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


    final FileMatcher fileMatcher= new FileMatcher(this.propertiesRootDirectory, i18nIncludes, i18nExcludes);


    XlsImExporter.importXls(fileMatcher,
                            this.xlsFile.toFile(),
                            this.propertyFileEncoding,
                            this.missingKeyAction);
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
