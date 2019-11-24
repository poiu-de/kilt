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

import de.poiu.apron.reformatting.AttachCommentsTo;
import de.poiu.kilt.reformatting.KiltReformatter;
import de.poiu.kilt.util.FileMatcher;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Reorders the key-value pairs in a set of java i18n .properties files.
 */
public class ReorderTask extends Task {

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

  private String propertyFileEncoding;

  /** Reorder the key-value pairs alphabetically by the name of their keys. */
  private boolean byKey= false;

  /** Reorder the key-value pairs in the same order as the key-value pairs in this template file. */
  private File template= null;

  /** How to handle comments and empty lines in the .properties files. */
  private AttachCommentsTo attachCommentsTo= AttachCommentsTo.NEXT_PROPERTY;


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

    this.validateParameters();

    if (this.verbose) {
      printProperties();
    }

    this.log("Reorder key-value pairs in .properties files.");
    final FileMatcher fileMatcher= new FileMatcher(Paths.get(this.propertiesRootDirectory), i18nIncludes, i18nExcludes);

    final KiltReformatter reformatter= new KiltReformatter();
    if (this.byKey) {
      reformatter.reorderByKey(fileMatcher,
                               attachCommentsTo,
                               this.propertyFileEncoding != null ? Charset.forName(this.propertyFileEncoding) : UTF_8);
    } else {
      reformatter.reorderByTemplate(this.template,
                                    fileMatcher,
                                    this.attachCommentsTo,
                                    this.propertyFileEncoding != null ? Charset.forName(this.propertyFileEncoding) : UTF_8);
    }

    this.log("...done");
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
    this.propertyFileEncoding = propertyFileEncoding;
  }


  public void setByKey(boolean byKey) {
    this.byKey = byKey;
  }


  public void setTemplate(String template) {
    if (template != null && !template.isEmpty()) {
      this.template = new File(template);
    }
  }


  public void setAttachCommentsTo(AttachCommentsTo attachCommentsTo) {
    this.attachCommentsTo = attachCommentsTo;
  }


  public void setVerbose(final boolean verbose) {
    this.verbose= verbose;
  }


  private void printProperties(){
    final StringBuilder sb= new StringBuilder();

    sb.append("verbose                   = ").append(this.verbose).append("\n");
    sb.append("propertiesRootDirectory   = ").append(this.propertiesRootDirectory).append("\n");
    sb.append("i18nIncludes              = ").append(this.i18nIncludes).append("\n");
    sb.append("i18nExcludes              = ").append(this.i18nExcludes).append("\n");
    sb.append("propertyFileEncoding      = ").append(this.propertyFileEncoding).append("\n");
    sb.append("byKey                     = ").append(this.byKey).append("\n");
    sb.append("template                  = ").append(this.template).append("\n");
    sb.append("attachCommentsTo          = ").append(this.attachCommentsTo).append("\n");

    System.out.println(sb.toString());
  }


  /**
   * Validates the existence of conflicting parameters.
   */
  private void validateParameters() {
    if (this.byKey && this.template != null) {
      throw new RuntimeException("The options --byKey and --byTemplate may not be given at the same time.");
    }

    if (!this.byKey && this.template == null) {
      throw new RuntimeException("One of --byKey or--byTemplate must be given.");
    }
  }
}
