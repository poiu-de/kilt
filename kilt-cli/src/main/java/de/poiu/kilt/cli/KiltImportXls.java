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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.poiu.kilt.internal.XlsImExporter;
import java.nio.file.Path;
import java.nio.file.Paths;
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


  @Option(names = {"-d", "--deleteEmptyProperties"}, description= "Whether to delete properties that have no value in the XLS(X) file. default: ${DEFAULT-VALUE})")
  private boolean deleteEmptyProperties;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  public KiltImportXls() {
    super();

    if (propsFromFile.containsKey(KiltProperty.XLS_FILE.getKey())) {
      this.xlsFile= Paths.get(propsFromFile.getProperty(KiltProperty.XLS_FILE.getKey()));
    }

    if (propsFromFile.containsKey(KiltProperty.DELETE_EMPTY_PROPERTIES.getKey())) {
      this.deleteEmptyProperties= Boolean.valueOf(propsFromFile.getProperty(KiltProperty.DELETE_EMPTY_PROPERTIES.getKey()));
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void run() {
    if (this.verbose) {
      printProperties();
    }

    if (!this.xlsFile.toFile().exists()) {
      throw new RuntimeException("XLS(X) file "+this.xlsFile.toAbsolutePath()+" does not exist.");
    }

    //TODO: Hier müsste ich einschränken können, welche Ressourcen importiert werden sollen

    XlsImExporter.importXls(propertiesRootDirectory,
                                this.xlsFile.toFile(),
                                this.propertyFileEncoding,
                                this.deleteEmptyProperties);
  }




  private void printProperties(){
    final StringBuilder sb= new StringBuilder();

    sb.append("verbose                 = ").append(this.verbose).append("\n");
    sb.append("propertiesRootDirectory = ").append(this.propertiesRootDirectory).append("\n");
    sb.append("i18nIncludes            = ").append(Joiner.on(", ").join(this.i18nIncludes)).append("\n");
    sb.append("i18nExcludes            = ").append(Joiner.on(", ").join(this.i18nExcludes)).append("\n");
    sb.append("propertyFileEncoding    = ").append(this.propertyFileEncoding).append("\n");
    sb.append("xlsFile                 = ").append(this.xlsFile.toAbsolutePath()).append("\n");
    sb.append("deleteEmptyProperties   = ").append(this.deleteEmptyProperties).append("\n");

    System.out.println(sb.toString());
  }
}
