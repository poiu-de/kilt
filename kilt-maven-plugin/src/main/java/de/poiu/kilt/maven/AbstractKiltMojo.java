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

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;


/**
 *
 * @author mherrn
 */
public abstract class AbstractKiltMojo extends AbstractMojo {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * The location of the source i18n resource bundle files.
   */
  @Parameter(property="propertiesRootDirectory", defaultValue="src/main/resources/", required=true)
  File propertiesRootDirectory;


  /**
   * Whether to give more verbose output.
   */
  @Parameter(property="verbose", defaultValue="false")
  boolean verbose;


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
   *
   * @see #i18nExcludes
   */
  @Parameter(property="i18nIncludes", defaultValue="**/*.properties")
  String[] i18nIncludes;


  /**
   * The files to exclude from the list of resources bundles given in {@link #i18nIncludes}.
   * <p>
   * File globbing is supported with the same semantics as for the <code>i18nIncludes</code>
   *
   * @see #i18nIncludes
   */
  @Parameter(property="i18nExcludes")
  String[] i18nExcludes;


  /**
   * The encoding of the properties files.
   */
  @Parameter(property = "propertyFileEncoding")
  String propertyFileEncoding;


  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject project;
}
