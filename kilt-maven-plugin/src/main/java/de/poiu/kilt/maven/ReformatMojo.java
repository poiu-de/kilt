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
 * Reformats the key-value pairs a set of java i18n .properties files.
 */
@Mojo(name="reformat")
@Execute(phase=LifecyclePhase.GENERATE_SOURCES)
public class ReformatMojo extends AbstractKiltMojo {


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /** The format string to use for formatting the key-value pairs. */
  @Parameter(property="format", defaultValue = "<key> = <value>\\n")
  private String format;

  /**
   * Whether to reformat the keys and values themselves by removing insignificant whitespace and linebreaks.
   */
  @Parameter(property="reformatKeysAndValues", defaultValue= "false")
  private boolean reformatKeysAndValues= false;


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

    final FileMatcher fileMatcher = new FileMatcher(this.propertiesRootDirectory.toPath(), this.i18nIncludes, this.i18nExcludes);

    final KiltReformatter reformatter= new KiltReformatter();
    reformatter.reformat(fileMatcher,
                         format,
                         reformatKeysAndValues,
                         this.propertyFileEncoding != null ? Charset.forName(this.propertyFileEncoding) : UTF_8);

    this.getLog().info("...done");
  }
}
