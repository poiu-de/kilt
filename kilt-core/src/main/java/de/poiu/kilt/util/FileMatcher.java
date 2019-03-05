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
package de.poiu.kilt.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import org.codehaus.plexus.util.DirectoryScanner;


/**
 *
 * @author mherrn
 */
public class FileMatcher {
//  public static boolean Set<File> findMatchingFiles(final Path baseDir, final FileFilter fileFilter) {
//    for (final String pattern : fileFilter.getInclusions()) {
//      final PathMatcher pathMatcher= baseDir.getFileSystem().getPathMatcher("glob:"+pattern);
//    }
//  }


  public static void main(String[] args) throws Exception {
    final Path absRoot= Paths.get("/tmp/base/i18n");
    final Path complicatedRoot= Paths.get("/usr/../tmp/base/i18n/");
    final PathMatcher pm= absRoot.getFileSystem().getPathMatcher("glob:**_de.properties");

    Files.walk(absRoot)
      .filter(p -> {
        return pm.matches(absRoot.relativize(p));
      })
      .forEach(p -> {
        System.out.println("____ "+p);
      });
//      .forEach(p -> {
//        System.out.println(p.toString()+" -> "+pm.matches(p));
//        System.out.println(p.toAbsolutePath().toString()+" -> "+pm.matches(p.toAbsolutePath()));
//        System.out.println(absRoot.relativize(p).toString()+" -> "+pm.matches(absRoot.relativize(p)));
//        System.out.println(p.getFileName().toString()+" -> "+pm.matches(p.getFileName()));
//      });


    System.out.println("-----------------");
    final DirectoryScanner directoryScanner = new DirectoryScanner();
    directoryScanner.setIncludes(new String[]{"**_de.properties"});
    directoryScanner.setBasedir(absRoot.toFile());
    directoryScanner.scan();

    final String[] fileNames = directoryScanner.getIncludedFiles();
    for (final String fln : fileNames) {
      System.out.println("> "+fln);
    }
  }
}
