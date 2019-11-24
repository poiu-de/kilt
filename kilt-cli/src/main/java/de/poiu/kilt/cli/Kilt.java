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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;


/**
 *
 * @author mherrn
 */
@Command(name = "kilt",
         mixinStandardHelpOptions = true,
         versionProvider = ManifestVersionProvider.class,
         description= "@|bold K|@ilt @|bold I|@18n @|bold L|@10n @|bold T|@9n",
         subcommands = {
           HelpCommand.class,
           KiltExportXls.class,
           KiltImportXls.class,
           KiltCreateFacade.class,
           KiltReformat.class,
           KiltReorder.class,
         })
public class Kilt implements Runnable {

  private static final Logger LOGGER= LogManager.getLogger();

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void run() {
    CommandLine.usage(this, System.out);
  }

  public static void main(String[] args) {
    final int exitCode= new CommandLine(new Kilt())
      .setUsageHelpAutoWidth(true)
      .setParameterExceptionHandler(new ParameterExceptionHandler())
      .setExecutionExceptionHandler(new ExecutionExceptionHandler())
      .execute(args);

    System.exit(exitCode);
  }
}
