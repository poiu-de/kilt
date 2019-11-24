/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.poiu.kilt.cli;

import picocli.CommandLine;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.UnmatchedArgumentException;


/**
 * A handler for exceptions that writes the exceptions message, in red if colors are available,
 * to stderr.
 * <p>
 * Additionally the usage help is printed to stderr.
 *
 * @author mherrn
 */
public class ParameterExceptionHandler implements IParameterExceptionHandler {

  @Override
  public int handleParseException(ParameterException ex, String[] args) {
    final CommandLine commandLine = ex.getCommandLine();

    commandLine.getErr().println(
      CommandLine.Help.Ansi.AUTO.string(
        "@|bold,red " + ex.getMessage() + "|@"
      ));

    UnmatchedArgumentException.printSuggestions(ex, commandLine.getErr());

    commandLine.usage(commandLine.getErr());

    return commandLine.getExitCodeExceptionMapper() != null
               ? commandLine.getExitCodeExceptionMapper().getExitCode(ex)
               : commandLine.getCommandSpec().exitCodeOnInvalidInput();
  }
}
