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
import de.poiu.apron.reformatting.AttachCommentsTo;
import de.poiu.kilt.reformatting.KiltReformatter;
import de.poiu.kilt.util.FileMatcher;
import java.io.File;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


/**
 *
 * @author mherrn
 */
@Command(name = "reorder",
         description= "Reorders the key-value pairs in Java i18n resource bundle files.",
         sortOptions = false)
public class KiltReorder extends AbstractKiltCommand implements Runnable {

  private static final Logger LOGGER= LogManager.getLogger();


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * Format the key-value pairs alphabetically by the name of their keys.
   */
  @Option(names={"-k", "--byKey"},
          description= "Reorder the key-value pairs alphabetically by the name of their keys.")
  private boolean byKey;

  /**
   * Format the key-value pairs in the same order as the key-value pairs in this template file.
   */
  @Option(names={"-t", "--byTemplate"},
          description= "Reorder the key-value pairs in the same order as the key-value pairs in this template file.")
  private File template;


  /**
   * How to handle comment lines and empty lines.
   */
  @Option(names={"-a", "--attachCommentsTo"},
          description= "How to handle comment lines and empty lines."
            + " Possible values: ${COMPLETION-CANDIDATES}"
            + " (default: ${DEFAULT-VALUE})")
  private AttachCommentsTo attachCommentsTo= AttachCommentsTo.NEXT_PROPERTY;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  public KiltReorder() {
    super();
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void run() {
    validateParameters();

    if (this.verbose) {
      printProperties();
    }

    final FileMatcher fileMatcher= new FileMatcher(this.propertiesRootDirectory, i18nIncludes, i18nExcludes);

    final KiltReformatter reformatter= new KiltReformatter();
    if (this.byKey) {
      reformatter.reorderByKey(fileMatcher, this.attachCommentsTo, super.propertyFileEncoding);
    } else {
      reformatter.reorderByTemplate(this.template, fileMatcher, this.attachCommentsTo, super.propertyFileEncoding);
    }
  }


  private void printProperties(){
    final StringBuilder sb= new StringBuilder();

    sb.append("verbose                   = ").append(this.verbose).append("\n");
    sb.append("propertiesRootDirectory   = ").append(this.propertiesRootDirectory).append("\n");
    sb.append("i18nIncludes              = ").append(Joiner.on(", ").join(this.i18nIncludes)).append("\n");
    sb.append("i18nExcludes              = ").append(Joiner.on(", ").join(this.i18nExcludes)).append("\n");
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
      throw new ValidationException("The options --byKey and --byTemplate may not be given at the same time.");
    }

    if (!this.byKey && this.template == null) {
      throw new ValidationException("One of --byKey or--byTemplate must be given.");
    }
  }
}
