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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Set;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.*;


/**
 *
 * @author mherrn
 */
public class PathUtilsTest {

  @Rule
  public TemporaryFolder tmpFolder= new TemporaryFolder();


  @Test
  public void testGetIncludedPropertyFiles_IncludeRelativeToRoot() {
    // - preparation

    create(
      "root/",
      "root/config/test-config.properties",
      "root/config/test-config.txt",
      "root/i18n/test_de.properties",
      "root/i18n/test_de_AT.properties",
      "root/i18n/test_en.properties",
      "root/i18n/test.properties",
      "root/i18n/sub/some_de.properties",
      "root/i18n/sub/some_en.properties"
    );

    final Path root= FileSystems.getDefault().getPath(this.tmpFolder.getRoot().getPath(), "root/");
    final String[] includes= {
      "i18n/**.properties",
    };
    final String[] excludes= {
    };

    // - execution

    final Set<File> result= PathUtils.getIncludedPropertyFiles(root, includes, excludes);

    // - verification

    assertThat(result).containsExactlyInAnyOrder(
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test_de.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test_de_AT.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test_en.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/sub/some_de.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/sub/some_en.properties")
    );
  }

  @Test
  public void testGetIncludedPropertyFiles_IncludeIncludingRoot() {
    // - preparation

    create(
      "root/",
      "root/config/test-config.properties",
      "root/config/test-config.txt",
      "root/i18n/test_de.properties",
      "root/i18n/test_de_AT.properties",
      "root/i18n/test_en.properties",
      "root/i18n/test.properties",
      "root/i18n/sub/some_de.properties",
      "root/i18n/sub/some_en.properties"
    );

    final Path root= FileSystems.getDefault().getPath(this.tmpFolder.getRoot().getPath(), "root/");
    final String[] includes= {
      this.tmpFolder.getRoot().getPath() + "/root/" + "i18n/**.properties",
    };
    final String[] excludes= {
    };

    // - execution

    final Set<File> result= PathUtils.getIncludedPropertyFiles(root, includes, excludes);

    // - verification

    assertThat(result).containsExactlyInAnyOrder(
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test_de.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test_de_AT.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test_en.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/sub/some_de.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/sub/some_en.properties")
    );
  }

  @Ignore(value = "The globbing of PathMatcher is quite useless. "
    + "It doesn't correctly handles different ways to access the same path. "
    + "For example /root/../root/ doesn't match /root/. "
    + "Apparently it just converts the given glob string into a regex, which of course doesn't work.")
  @Test
  public void testGetIncludedPropertyFiles_IncludeIncludingDifferentRootPath() {
    // - preparation

    create(
      "root/",
      "root/config/test-config.properties",
      "root/config/test-config.txt",
      "root/i18n/test_de.properties",
      "root/i18n/test_de_AT.properties",
      "root/i18n/test_en.properties",
      "root/i18n/test.properties",
      "root/i18n/sub/some_de.properties",
      "root/i18n/sub/some_en.properties"
    );

    final Path root= FileSystems.getDefault().getPath(this.tmpFolder.getRoot().getPath(), "root/");
    final String[] includes= {
      this.tmpFolder.getRoot().getPath() + "/root/../root/" + "i18n/**.properties",
    };
    final String[] excludes= {
    };

    // - execution

    final Set<File> result= PathUtils.getIncludedPropertyFiles(root, includes, excludes);

    // - verification

    assertThat(result).containsExactlyInAnyOrder(
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test_de.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test_de_AT.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test_en.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/sub/some_de.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/sub/some_en.properties")
    );
  }

  @Test
  public void testGetIncludedPropertyFiles_InAndExclude() {
    // - preparation

    create(
      "root/",
      "root/config/test-config.properties",
      "root/config/test-config.txt",
      "root/i18n/test_de.properties",
      "root/i18n/test_de_AT.properties",
      "root/i18n/test_en.properties",
      "root/i18n/test.properties",
      "root/i18n/sub/some_de.properties",
      "root/i18n/sub/some_en.properties"
    );

    final Path root= FileSystems.getDefault().getPath(this.tmpFolder.getRoot().getPath(), "root/");
    final String[] includes= {
      "i18n/**.properties",
    };
    final String[] excludes= {
      "i18n/**_en.properties"
    };

    // - execution

    final Set<File> result= PathUtils.getIncludedPropertyFiles(root, includes, excludes);

    // - verification

    assertThat(result).containsExactlyInAnyOrder(
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test_de.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test_de_AT.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/test.properties"),
      new File(this.tmpFolder.getRoot().getPath() + "/" + "root/i18n/sub/some_de.properties")
    );
  }


  @Test
  public void testGetIncludedPropertyFiles_ExcludeOnly() {
    // - preparation

    create(
      "root/",
      "root/config/test-config.properties",
      "root/config/test-config.txt",
      "root/i18n/test_de.properties",
      "root/i18n/test_de_AT.properties",
      "root/i18n/test_en.properties",
      "root/i18n/test.properties",
      "root/i18n/sub/some_de.properties",
      "root/i18n/sub/some_en.properties"
    );

    final Path root= FileSystems.getDefault().getPath(this.tmpFolder.getRoot().getPath(), "root/");
    final String[] includes= {
    };
    final String[] excludes= {
      "i18n/**_en.properties"
    };

    // - execution

    final Set<File> result= PathUtils.getIncludedPropertyFiles(root, includes, excludes);

    // - verification

    assertThat(result).containsExactlyInAnyOrder(
    );
  }


  @Test
  public void testGetIncludedPropertyFiles_NonexistentRoot() {
    // - preparation

    create(
      "root/",
      "root/config/test-config.properties",
      "root/config/test-config.txt",
      "root/i18n/test_de.properties",
      "root/i18n/test_de_AT.properties",
      "root/i18n/test_en.properties",
      "root/i18n/test.properties",
      "root/i18n/sub/some_de.properties",
      "root/i18n/sub/some_en.properties"
    );

    final Path root= FileSystems.getDefault().getPath(this.tmpFolder.getRoot().getPath(), "non-existent/");
    final String[] includes= {
      "i18n/**.properties",
    };
    final String[] excludes= {
    };

    // - execution

    final Set<File> result= PathUtils.getIncludedPropertyFiles(root, includes, excludes);

    // - verification

    assertThat(result).containsExactlyInAnyOrder(
    );
  }


  /**
   * Creates the given files below the tmpFolder.
   *
   * Necessary subdirectories are created as well.
   *
   * If a string in the given list of files ends with a slash it is considered a directory.
   * Otherwise it is considered a file.
   *
   * @param files the list of files and directories to create
   */
  private void create(String... files) {
    for (final String file : files) {
      if (file.startsWith("/")) {
        throw new RuntimeException("Not absolute paths allowed: " + file);
      }

      if (file.startsWith("..")) {
        throw new RuntimeException("Not paths that traverse upwards allowed: " + file);
      }

      final Path p= FileSystems.getDefault().getPath(file);
      final File f= new File(this.tmpFolder.getRoot() + "/" + p.toString());

      if (file.endsWith("/")) {
        f.mkdirs();
      } else {
        f.getParentFile().mkdirs();
        try {
          f.createNewFile();
        } catch (IOException ex) {
          throw new RuntimeException("Error creating test file: " + f, ex);
        }
      }
    }
  }


}
