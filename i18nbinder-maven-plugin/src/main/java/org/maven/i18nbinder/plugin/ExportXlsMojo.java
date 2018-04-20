/*******************************************************************************
 * Copyright 2012 Danny Kunz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.maven.i18nbinder.plugin;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.omnaest.i18nbinder.internal.XlsImExporter;


/**
 * Goal which executes the i18nBinder xls file generation
 *
 * @author <a href="mailto:awonderland6@googlemail.com">Danny Kunz</a>
 */
@Mojo(name = "export-xls")
public class ExportXlsMojo extends AbstractMojo {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * Location of the output directory root.
   */
  @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}", required = true)
  private File xlsOutputDirectory;

  /**
   * Location of the source i18n files.
   */
  @Parameter(property = "propertiesRootDirectory", defaultValue = "src/main/resources/i18n")
  private File propertiesRootDirectory;

  /**
   * Whether to give more verbose output.
   */
  @Parameter(property = "verbose", defaultValue = "false")
  private boolean verbose;

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
  @Parameter(property="i18nIncludes", defaultValue="**/*.properties")
  private String[] i18nIncludes;

  /**
   * The files to exclude from the list of resources bundles given in {@link #i18nIncludes}.
   * <p>
   * File globbing supported with the same semantics as for the <code>i18nIncludes</code>
   *
   * @see #i18nIncludes
   */
  @Parameter(property="i18nExcludes")
  private String[] i18nExcludes;

  @Parameter(property = "propertyFileEncoding")
  private String propertyFileEncoding;

  @Parameter(property = "xlsFileEncoding", defaultValue = "UTF-8")
  private String xlsFileEncoding;

  @Parameter(property = "xlsFileName", required= true, defaultValue = "i18n.xls")
  private String xlsFileName;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  @Override
  public void execute() throws MojoExecutionException {
    this.getLog().info("Exporting properties to XLS.");

    final Set<File> propertyFileSet = this.getIncludedPropertyFiles(this.propertiesRootDirectory);

    try {
      Files.createDirectories(this.xlsOutputDirectory.toPath());
      final File file = new File(this.xlsOutputDirectory, this.xlsFileName);

      XlsImExporter.exportXls(this.propertiesRootDirectory.toPath(),
                                propertyFileSet,
                                this.propertyFileEncoding,
                                file.toPath(),
                                this.xlsFileEncoding);
    } catch (IOException e) {
      throw new RuntimeException("Error exporting property files to XLS.", e);
    }

    this.getLog().info("...done");
  }


  private Set<File> getIncludedPropertyFiles(final File propertiesRootDirectory) {
    if (!propertiesRootDirectory.exists()) {
      this.getLog().warn("resource bundle directory " + propertiesRootDirectory + " does not exist. Nothing will be exported.");
      return ImmutableSet.of();
    }

    final Set<File> matchingFiles = new LinkedHashSet<>();

    final DirectoryScanner directoryScanner = new DirectoryScanner();
    directoryScanner.setIncludes(this.i18nIncludes);
    directoryScanner.setExcludes(this.i18nExcludes);
    directoryScanner.setBasedir(propertiesRootDirectory);
    directoryScanner.scan();

    final String[] fileNames = directoryScanner.getIncludedFiles();
    for (int i = 0; i < fileNames.length; i++) {
      if (this.verbose) {
        this.getLog().info("Including in XLS: " + fileNames[i]);
      }
      matchingFiles.add(new File(propertiesRootDirectory, fileNames[i]));
    }

    if (matchingFiles.isEmpty()) {
      this.getLog().warn("No resource bundles found. Nothing will be exported.");
    }

    return matchingFiles;
  }

}
