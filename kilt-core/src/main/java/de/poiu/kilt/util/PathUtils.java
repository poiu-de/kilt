/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.poiu.kilt.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author mherrn
 */
public class PathUtils {
  private static final Logger LOGGER= LogManager.getLogger();

  private static List<PathMatcher> toPathMatchers(final String absRoot, final String... filePatterns) {
    return toPathMatchers(absRoot, Arrays.asList(filePatterns));
  }


  private static List<PathMatcher> toPathMatchers(final String absRoot, final List<String> filePatterns) {
    // Build two matchers for each i18nInclude:
    //   - the given pattern literally
    //   - the given pattern prepended with the propertyRootDirectory
    return filePatterns.stream()
      .flatMap(i18nInclude -> Stream.of(
        FileSystems.getDefault().getPathMatcher("glob:" + i18nInclude),
        FileSystems.getDefault().getPathMatcher("glob:" + absRoot + "/" + i18nInclude))
      )
      .collect(Collectors.toList())
      ;
  }


  public static Set<File> getIncludedPropertyFiles(final Path root, final String[] i18nIncludes, final String[] i18nExcludes) {
    return getIncludedPropertyFiles(root, Arrays.asList(i18nIncludes), Arrays.asList(i18nExcludes));
  }


  public static Set<File> getIncludedPropertyFiles(final Path root, final List<String> i18nIncludes, final List<String> i18nExcludes) {
    if (!root.toFile().isDirectory()) {
      LOGGER.log(Level.INFO, "Property root directory {} does not exist.", root);
      return Collections.EMPTY_SET;
    }

    final String absRoot;
    try {
      absRoot= root.toFile().getAbsoluteFile().getCanonicalPath();
    } catch (IOException ex) {
      throw new RuntimeException("Properties root directory '"+root.toString()+"' cannot be found.", ex);
    }

    final List<PathMatcher> pathMatchers= toPathMatchers(absRoot, i18nIncludes);
    final List<PathMatcher> pathUnmatchers= toPathMatchers(absRoot, i18nExcludes);

    try (final Stream<Path> paths= Files.walk(root)) {
      return paths
        // only regular files
        .filter(path -> path.toFile().isFile())

        // only files for which at least one matcher matches
        .filter(path -> {
          return pathMatchers.stream()
            .anyMatch((pathMatcher) -> (pathMatcher.matches(path)));
        })

        // only files for which no unmatcher matches
        .filter(path -> {
          return pathUnmatchers.stream()
            .noneMatch((pathUnmatcher) -> (pathUnmatcher.matches(path)));
        })

        .map(path -> path.toFile())
        .collect(Collectors.toSet()
        );
    } catch (IOException ex) {
      throw new RuntimeException("Error finding included files in root directory '"+root.toString()+"'.", ex);
    }
  }
}
