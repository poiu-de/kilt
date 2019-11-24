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
package de.poiu.kilt.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.MatchPattern;


/**
 *
 * @author mherrn
 */
public class PathUtils {
  private static final Logger LOGGER= LogManager.getLogger();


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  private final Path         root;
  private final List<String> i18nIncludes;
  private final List<MatchPattern> i18nIncludePatterns;
  private final List<String> i18nExcludes;
  private final List<MatchPattern> i18nExcludePatterns;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  public PathUtils(final Path root, final String[] i18nIncludes) {
    this(root, Arrays.asList(i18nIncludes));
  }


  public PathUtils(final Path root, final List<String> i18nIncludes) {
    this(root, i18nIncludes, Collections.EMPTY_LIST);
  }


  public PathUtils(final Path root, final String[] i18nIncludes, final String[] i18nExcludes) {
    this(root, Arrays.asList(i18nIncludes), Arrays.asList(i18nExcludes));
  }


  public PathUtils(final Path root, final List<String> i18nIncludes, final List<String> i18nExcludes) {
    this.root= root.toAbsolutePath().normalize();
    this.i18nIncludes= new ArrayList<>(i18nIncludes);
    this.i18nIncludePatterns= this.toMatchPatterns(i18nIncludes);
    this.i18nExcludes= new ArrayList<>(i18nExcludes);
    this.i18nExcludePatterns= this.toMatchPatterns(i18nExcludes);
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  private List<MatchPattern> toMatchPatterns(final List<String> filePatterns) {
    final List<MatchPattern> matchPatterns= new ArrayList<>(filePatterns.size());

    for (final String filePattern : filePatterns) {
      // we resolve the pattern here agains the root path, even though the pattern doesn't have to
      // be a real path. This allows specifying the pattern as relative to the root or as an absolute
      // path (even paths that are not below the root).
      final String absolutePattern= this.root.resolve(filePattern).normalize().toString();
      final MatchPattern matchPattern= MatchPattern.fromString(absolutePattern);
      matchPatterns.add(matchPattern);
    }

    return matchPatterns;
  }


  public boolean matches(final Path path) {
    final Path canonicalPath= this.root.resolve(path).toAbsolutePath().normalize();

    // if the path matches an exclude pattern, we can return false
    for (final MatchPattern matchPattern : this.i18nExcludePatterns) {
      // FIXME: How to decide whether to be case sensitive or not? Can we ask the filesystem?
      //        No builtin way in Java. We would need to write a file to check: https://stackoverflow.com/a/58349517/572645
      if (matchPattern.matchPath(canonicalPath.toString(), true)) {
        return false;
      }
    }

    // if the path matches an include pattern, we can return true
    for (final MatchPattern matchPattern : this.i18nIncludePatterns) {
      if (matchPattern.matchPath(canonicalPath.toString(), true)) {
        return true;
      }
    }

    // if there was no match, it obviously didn't match
    return false;
  }


  public Set<File> findMatchingFiles() {
    if (!root.toFile().isDirectory()) {
      LOGGER.log(Level.INFO, "Property root directory {} does not exist.", root);
      return Collections.EMPTY_SET;
    }

    try (final Stream<Path> paths= Files.walk(root, FileVisitOption.FOLLOW_LINKS)) {
      return paths
        // only regular files
        .filter(path -> path.toFile().isFile())

        // only files matching our include and not matching our exclude patterns
        .filter(this::matches)

        .map(path -> path.toFile())
        .collect(Collectors.toSet()
        );
    } catch (IOException ex) {
      throw new RuntimeException("Error finding included files in root directory '"+root.toString()+"'.", ex);
    }
  }
}
