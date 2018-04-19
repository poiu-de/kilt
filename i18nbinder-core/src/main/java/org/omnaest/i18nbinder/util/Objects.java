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
package org.omnaest.i18nbinder.util;


/**
 * Helper methods additional to {@link java.util.Objects}.
 *
 * @author mherrn
 */
public class Objects {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * Checks that the given string is not empty. This
   * method is designed primarily for doing parameter validation in methods
   * and constructors, as demonstrated below:
   * <blockquote><pre>
   * public Foo(String bar) {
   *     this.bar = Objects.requireNonEmpty(bar);
   * }
   * </pre></blockquote>
   *
   * @param s the object reference to check for emptiness
   * @return {@code s} if not empty
   * @throws IllegalArgumentException if {@code s} is empty
   */
  public static String requireNonEmpty(final String s) {
    if (s.isEmpty()) {
      throw new IllegalArgumentException("Empty String is not allowed");
    }
    return s;
  }


  /**
   * Checks that the given string is not empty. This
   * method is designed primarily for doing parameter validation in methods
   * and constructors, as demonstrated below:
   * <blockquote><pre>
   * public Foo(String bar, String baz) {
   *     this.bar = Objects.requireNonEmpty(bar, "bar must not be empty");
   *     this.baz = Objects.requireNonEmpty(baz, "baz must not be empty");
   * }
   * </pre></blockquote>
   *
   * @param s the object reference to check for emptiness
   * @param message detail message to be used in the event that an {@code
   *                IllegalArgumentException} is thrown
   * @return {@code s} if not empty
   * @throws IllegalArgumentException if {@code s} is empty
   */
  public static String requireNonEmpty(final String s, final String message) {
    if (s.isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return s;
  }


  /**
   * Checks that the given string is not all whitespace or empty. This
   * method is designed primarily for doing parameter validation in methods
   * and constructors, as demonstrated below:
   * <blockquote><pre>
   * public Foo(String bar) {
   *     this.bar = Objects.requireNonWhitespace(bar);
   * }
   * </pre></blockquote>
   *
   * @param s the object reference to check for containing only whitespace
   * @return {@code s} if not only whitespace
   * @throws IllegalArgumentException if {@code s} is only whitespace
   */
  public static String requireNonWhitespace(final String s) {
    if (s.trim().isEmpty()) {
      throw new IllegalArgumentException("Empty String or String of only whitespace is not allowed");
    }
    return s;
  }


  /**
   * Checks that the given string is not all whitespace or empty. This
   * method is designed primarily for doing parameter validation in methods
   * and constructors, as demonstrated below:
   * <blockquote><pre>
   * public Foo(String bar) {
   *     this.bar = Objects.requireNonWhitespace(bar, "bar must not be all whitespace or empty");
   *     this.baz = Objects.requireNonWhitespace(baz, "baz must not be all whitespace or empty");
   * }
   * </pre></blockquote>
   *
   * @param s the object reference to check for containing only whitespace
   * @param message detail message to be used in the event that an {@code
   *                IllegalArgumentException} is thrown
   * @return {@code s} if not only whitespace
   * @throws IllegalArgumentException if {@code s} is only whitespace
   */
  public static String requireNonWhitespace(final String s, final String message) {
    if (s.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return s;
  }
}
