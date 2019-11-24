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

import de.poiu.apron.reformatting.AttachCommentsTo;
import de.poiu.kilt.reformatting.KiltReformatter;
import de.poiu.kilt.util.FileMatcher;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Reorders the key-value pairs in a set of java i18n .properties files.
 */
@Mojo(name="reorder")
@Execute(phase=LifecyclePhase.GENERATE_SOURCES)
public class ReorderMojo extends AbstractKiltMojo {


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes


  /** Reorder the key-value pairs alphabetically by the name of their keys. */
  @Parameter(property="byKey", defaultValue = "false")
  private boolean byKey;


  /** Reorder the key-value pairs in the same order as the key-value pairs in this template file. */
  @Parameter(property="template")
  private File template;


  /**
   * How to handle comments and empty lines in the .properties files.
   * <p>
   * The following values are valid:
   * <ul>
   *  <li>NEXT_PROPERTY: Comments and empty lines are attached to the key-value pair <i>after</i> them</li>
   *  <li>PREV_PROPERTY: Comments and empty lines are attached to the key-value pair <i>before</i> them</li>
   *  <li>ORIG_LINE: Comments and empty lines remain at their current position</li>
   * </ul>
   */
  @Parameter(property="attachCommentsTo", defaultValue="NEXT_PROPERTY")
  private AttachCommentsTo attachCommentsTo;



  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void execute() throws MojoExecutionException {
    this.validateParameters();

    if (this.verbose) {
      Configurator.setLevel(LogManager.getLogger("de.poiu.kilt").getName(), Level.DEBUG);
    }

    final FileMatcher fileMatcher= new FileMatcher(this.propertiesRootDirectory.toPath(), this.i18nIncludes, this.i18nExcludes);

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

    this.getLog().info("...done");
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
