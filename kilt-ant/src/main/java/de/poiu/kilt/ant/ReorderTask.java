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
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
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

  private boolean verbose= false;

  private String propertyFileEncoding;

  /** Reorder the key-value pairs alphabetically by the name of their keys. */
  private boolean byKey= false;

  /** Reorder the key-value pairs in the same order as the key-value pairs in this template file. */
  private File template= null;

  /** How to handle comments and empty lines in the .properties files. */
  private AttachCommentsTo attachCommentsTo= AttachCommentsTo.NEXT_PROPERTY;

  private final List<FileSet> fileSetList = new ArrayList<>();


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
    final Set<File> propertyFileSet = this.resolveFilesFromFileSetList(this.fileSetList);

    final KiltReformatter reformatter= new KiltReformatter();
    if (this.byKey) {
      reformatter.reorderByKey(propertyFileSet,
                               attachCommentsTo,
                               this.propertyFileEncoding != null ? Charset.forName(this.propertyFileEncoding) : UTF_8);
    } else {
      reformatter.reorderByTemplate(this.template,
                                    propertyFileSet,
                                    this.attachCommentsTo,
                                    this.propertyFileEncoding != null ? Charset.forName(this.propertyFileEncoding) : UTF_8);
    }

    this.log("...done");
  }


  /**
   * @see #resolveFilesFromFileSet(FileSet)
   * @param fileSetList
   * @return
   */
  protected Set<File> resolveFilesFromFileSetList(List<FileSet> fileSetList) {
    Set<File> retset = new LinkedHashSet<>();

    if (fileSetList != null) {
      fileSetList.forEach((fileSet) -> {
        retset.addAll(this.resolveFilesFromFileSet(fileSet));
      });
    }

    return retset;
  }


  /**
   * @see #resolveFilesFromFileSetList(List)
   * @param fileSet
   * @return
   */
  protected List<File> resolveFilesFromFileSet(FileSet fileSet) {
    List<File> retlist = new ArrayList<>();

    if (fileSet != null) {
      DirectoryScanner directoryScanner = fileSet.getDirectoryScanner();
      String[] includedFileNames = directoryScanner.getIncludedFiles();

      if (includedFileNames != null) {
        File basedir = directoryScanner.getBasedir();

        for (String fileNameUnnormalized : includedFileNames) {
          String fileName = fileNameUnnormalized.replaceAll(Pattern.quote("\\"), "/");

          File file = new File(basedir, fileName);
          if (file.exists()) {
            retlist.add(file);
          }
        }
      }

    }

    return retlist;
  }


  public void addFileset(FileSet fileset) {
    if (fileset != null) {
      this.fileSetList.add(fileset);
    }
  }


//  public void setFileEncoding(String fileEncoding) {
//    this.propertyFileEncoding = fileEncoding;
//    this.javaFileEncoding = fileEncoding;
//  }



  public void setPropertiesRootDirectory(String propertiesRootDirectory) {
    this.propertiesRootDirectory = propertiesRootDirectory;
  }


  public void setPropertyFileEncoding(String propertyFileEncoding) {
    this.propertyFileEncoding = propertyFileEncoding;
  }


  public void setByKey(boolean byKey) {
    this.byKey = byKey;
  }


  public void setTemplate(File template) {
    this.template = template;
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
    sb.append("i18nIncludes              = ").append(this.fileSetList).append("\n");
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
