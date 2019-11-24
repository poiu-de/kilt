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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine;


/**
 *
 * @author mherrn
 */
public abstract class AbstractKiltCommand {

  private static final Logger LOGGER= LogManager.getLogger();


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * The location of the source i18n resource bundle files.
   */
  @CommandLine.Option(names= {"-r", "--propertiesRootDirectory"}, defaultValue= "i18n", required= true, description= "The location of the source i18n resource bundle files. (default: ${DEFAULT-VALUE})")
  Path propertiesRootDirectory= Paths.get("i18n");


  /**
   * The files to process as resource bundles.
   * File globbing is supported with the following semantics>
   * <p>
   * <code>'?'</code> matches a single character
   * <p>
   * <code>'*'</code> matches zero or more characters
   * <p>
   * <code>'**'</code> matches zero or more directories
   * <p>
   *
   * For example if you have the following resource bundles:
   * <ul>
   *   <li>messages_de.properties</li>
   *   <li>messages_en.properties</li>
   *   <li>buttons_de.properties</li>
   *   <li>buttons_en.properties</li>
   *   <li>internal/exceptions_de.properties</li>
   *   <li>internal/exceptions_en.properties</li>
   *   <li>internal/messages.properties</li>
   *   <li>internal/messages_en.properties</li>
   * </ul>
   * these are the results for the following patterns>
   * <table>
   *   <tr><th>Pattern</th><th>Resulting files</th></tr>
   *   <tr><td>**&#47;*.properties</td><td>All properties files</td></tr>
   *   <tr><td>messages*.properties</td><td>messages_de.properties<br/>messages_en.properties</td></tr>
   *   <tr><td>**&#47;messages_en.properties</td><td>messages_en.properties<br/>internal/messages_en.properties</td></tr>
   * </table>
   * <p>
   * File separators may be given as forward (/) or backward slash (\). They can be used independently
   * of the actual filesystem.
   *
   * @see #i18nExcludes
   */
  @CommandLine.Option(names= {"-i", "--include", "--i18nIncludes"}, defaultValue= "**/*.properties", description= "The files to process as resource bundles. (default: ${DEFAULT-VALUE})")
  String[] i18nIncludes= {"**/*.properties"};


  /**
   * The files to exclude from the list of resources bundles given in {@link #i18nIncludes}.
   * <p>
   * File globbing supported with the same semantics as for the <code>i18nIncludes</code>
   *
   * @see #i18nIncludes
   */
  @CommandLine.Option(names= {"-e", "--exclude", "--i18nExcludes"}, description= "The files to exclude from the list of resources bundles.")
  String[] i18nExcludes= {};


  /**
   * The encoding of the properties files.
   */
  @CommandLine.Option(names= {"--penc", "--propertyFileEncoding"}, description= "The encoding of the properties files.")
  Charset propertyFileEncoding;


  /**
   * Whether to give more verbose output.
   */
  @CommandLine.Option(names= {"-v", "--verbose"}, defaultValue= "false", description= "Whether to give more verbose output.", order = 100)
  boolean verbose;


  final Properties propsFromFile;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  AbstractKiltCommand() {
    this.propsFromFile= readConfigFile();

    if (propsFromFile.containsKey(KiltProperty.PROPERTIES_ROOT_DIRECTORY.getKey())) {
      this.propertiesRootDirectory= Paths.get(propsFromFile.getProperty(KiltProperty.PROPERTIES_ROOT_DIRECTORY.getKey()));
    }

    if (propsFromFile.containsKey(KiltProperty.I18N_INCLUDES.getKey())) {
      this.i18nIncludes= new String[]{propsFromFile.getProperty(KiltProperty.I18N_INCLUDES.getKey())};
    }

    if (propsFromFile.containsKey(KiltProperty.I18N_EXCLUDES.getKey())) {
      this.i18nExcludes= new String[]{propsFromFile.getProperty(KiltProperty.I18N_EXCLUDES.getKey())};
    }

    if (propsFromFile.containsKey(KiltProperty.PROPERTY_FILE_ENCODING.getKey())) {
      this.propertyFileEncoding= Charset.forName(propsFromFile.getProperty(KiltProperty.PROPERTY_FILE_ENCODING.getKey()));
    }

    if (propsFromFile.containsKey(KiltProperty.VERBOSE.getKey())) {
      this.verbose= Boolean.valueOf(propsFromFile.getProperty(KiltProperty.VERBOSE.getKey()));
    }

    if (this.verbose) {
      Configurator.setLevel(LogManager.getLogger("de.poiu.kilt").getName(), Level.DEBUG);
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  private Properties readConfigFile() {
    //TODO: Use more sophisticated KiltProperties
    final Properties props= new Properties();
    try (final FileInputStream fis= new FileInputStream(new File("kilt.properties"))) {
      props.load(fis);
    } catch (IOException ex) {
      LOGGER.log(Level.WARN, "Error reading kilt.properties.", ex);
    }
    return props;
  }

}
