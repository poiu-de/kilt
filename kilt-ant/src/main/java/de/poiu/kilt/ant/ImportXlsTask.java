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
package de.poiu.kilt.ant;

import de.poiu.apron.MissingKeyAction;
import de.poiu.kilt.importexport.XlsImExporter;
import de.poiu.kilt.util.FileMatcher;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;


/**
 * Imports the translations from an XLS file back into the resource bundle
 * files.
 */
public class ImportXlsTask extends Task {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes


  /**
   * The location of the source i18n resource bundle files.
   */
  private String propertiesRootDirectory= "i18n";

  private List<String> i18nIncludes= new ArrayList<>();

  private List<String> i18nExcludes= new ArrayList<>();

  private boolean verbose= false;

  private Charset propertyFileEncoding;

  private String xlsFile= null;

  private MissingKeyAction missingKeyAction= MissingKeyAction.NOTHING;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void execute() throws BuildException {
    if (this.verbose) {
      Configurator.setLevel(LogManager.getLogger("de.poiu.kilt").getName(), Level.DEBUG);
    }

    if (this.verbose) {
      printProperties();
    }

    if (this.xlsFile == null) {
      this.log("No xls file specified. Please provide a file name (with or without path) for the xls file from which to read.", Project.MSG_ERR);
      throw new RuntimeException("No xls file specified. Please provide a file name (with or without path) for the xls file from which to read.");
    } else {
      this.log("Write properties from XLS file back to property files...");

      final FileMatcher fileMatcher= new FileMatcher(Paths.get(this.propertiesRootDirectory), i18nIncludes, i18nExcludes);
      final File file = new File(this.xlsFile);

      if (file.exists()) {
        XlsImExporter.importXls(fileMatcher,
                                file,
                                this.propertyFileEncoding,
                                this.missingKeyAction);
      }

      this.log("...done");
    }
  }


  public String getXlsFile() {
    return this.xlsFile;
  }


  public void setXlsFile(String xlsFile) {
    this.xlsFile = xlsFile;
  }


  public MissingKeyAction getMissingKeyAction() {
    return this.missingKeyAction;
  }


  public void setMissingKeyAction(String missingKeyAction) {
    if (missingKeyAction != null) {
      this.missingKeyAction= MissingKeyAction.valueOf(missingKeyAction.toUpperCase());
    }
  }


  public void setPropertiesRootDirectory(String propertiesRootDirectory) {
    this.propertiesRootDirectory = propertiesRootDirectory;
  }


  public void setI18nIncludes(final String i18nIncludes) {
    this.i18nIncludes= Arrays.asList(i18nIncludes.split("\\s+"));
  }


  public void setI18nExcludes(final String i18nExcludes) {
    this.i18nExcludes= Arrays.asList(i18nExcludes.split("\\s+"));
  }


  public void setPropertyFileEncoding(String propertyFileEncoding) {
    if (propertyFileEncoding != null) {
      this.propertyFileEncoding= Charset.forName(propertyFileEncoding);
    } else {
      this.propertyFileEncoding= null;
    }
  }


  public void setVerbose(final boolean verbose) {
    this.verbose= verbose;
  }


  private void printProperties(){
    final StringBuilder sb= new StringBuilder();

    sb.append("verbose                 = ").append(this.verbose).append("\n");
    sb.append("propertiesRootDirectory = ").append(this.propertiesRootDirectory).append("\n");
    sb.append("i18nIncludes            = ").append(this.i18nIncludes).append("\n");
    sb.append("i18nExcludes            = ").append(this.i18nExcludes).append("\n");
    sb.append("propertyFileEncoding    = ").append(this.propertyFileEncoding).append("\n");
    sb.append("xlsFile                 = ").append(this.xlsFile).append("\n");
    sb.append("missingKeyAction        = ").append(this.missingKeyAction).append("\n");

    System.out.println(sb.toString());
  }

}
