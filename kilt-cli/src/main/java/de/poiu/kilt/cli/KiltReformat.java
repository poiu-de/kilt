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
@Command(name = "reformat",
         description= "Reformats Java i18n resource bundle files to a common format.",
         sortOptions = false)
public class KiltReformat extends AbstractKiltCommand implements Runnable {

  private static final Logger LOGGER= LogManager.getLogger();


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * The format string to use for formatting the key-value pairs.
   */
  @Option(names={"-f", "--format"},
          description= "The format string to use for formatting the key-value pairs. "
            + "(default: \"${DEFAULT-VALUE}\")")
  private String format= "<key> = <value>\\n";

  /**
   * Whether to reformat the keys and values themselves by removing insignificant whitespace and linebreaks.
   */
  @Option(names={"-k", "--reformatKeyAndValue"},
          description= "Whether to reformat the keys and values themselves by removing insignificant"
            + " whitespace and linebreaks."
            + " (default: ${DEFAULT-VALUE})")
  private boolean reformatKeysAndValues= false;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  public KiltReformat() {
    super();
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void run() {
    if (this.verbose) {
      printProperties();
    }

    final FileMatcher fileMatcher= new FileMatcher(this.propertiesRootDirectory, i18nIncludes, i18nExcludes);

    final KiltReformatter reformatter= new KiltReformatter();
    reformatter.reformat(fileMatcher, this.format, this.reformatKeysAndValues, super.propertyFileEncoding);
  }


  private void printProperties(){
    final StringBuilder sb= new StringBuilder();

    sb.append("verbose                   = ").append(this.verbose).append("\n");
    sb.append("propertiesRootDirectory   = ").append(this.propertiesRootDirectory).append("\n");
    sb.append("i18nIncludes              = ").append(Joiner.on(", ").join(this.i18nIncludes)).append("\n");
    sb.append("i18nExcludes              = ").append(Joiner.on(", ").join(this.i18nExcludes)).append("\n");
    sb.append("propertyFileEncoding      = ").append(this.propertyFileEncoding).append("\n");
    sb.append("format                    = ").append(this.format).append("\n");
    sb.append("reformatKeysAndValues     = ").append(this.reformatKeysAndValues).append("\n");

    System.out.println(sb.toString());
  }
}
