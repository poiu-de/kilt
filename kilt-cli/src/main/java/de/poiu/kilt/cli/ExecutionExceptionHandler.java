/*
 * Copyright (C) 2019 Marco Herrn
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

import picocli.CommandLine;


/**
 * A handler for exceptions that writes the exceptions message, in red if colors are available,
 * to stderr.
 * <p>
 * Additionally the usage help is printed to stderr.
 *
 * @author mherrn
 */
class ExecutionExceptionHandler implements CommandLine.IExecutionExceptionHandler {

  @Override
  public int handleExecutionException(final Exception ex, final CommandLine commandLine, final CommandLine.ParseResult parseResult) throws Exception {
    commandLine.getErr().println(
      CommandLine.Help.Ansi.AUTO.string(
        "@|bold,red " + ex.getMessage() + "|@"
      ));

    commandLine.usage(commandLine.getErr());

    return commandLine.getExitCodeExceptionMapper() != null
               ? commandLine.getExitCodeExceptionMapper().getExitCode(ex)
               : commandLine.getCommandSpec().exitCodeOnExecutionException();
  }
}
