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
package de.poiu.kilt.example;

import de.poiu.kilt.facade.I18n;
import i18n.generated.I18nMessages;
import java.util.Locale;


/**
 * An example application for using Kilt.
 *
 * @author mherrn
 */
public class ExampleApp {
  // Create a default facade accessor.
  private final I18n i18n= new I18n();
  // Create a facade accessor for only japanese.
  private final I18n i18n_ja= i18n.forLocale(Locale.JAPANESE);
  // Create a facade accessor for only russian.
  private final I18n i18n_ru= i18n.forLocale(Locale.forLanguageTag("ru"));


  /**
   * Runs the application.
   */
  public void run() {
    // Print the greeting in the systems locale
    System.out.println(i18n.get(I18nMessages.GREETING) + ", " + System.getProperty("user.name"));

    // Print texts in different locales
    System.out.println(i18n_ja.get(I18nMessages.I_SPEAK_LANG));
    System.out.println(i18n_ru.get(I18nMessages.I_SPEAK_LANG));

    // Use the facade accessor to access resource bundles without a generated enum facade.
    // You lose type safety here, of course. Typos can occur easily.
    System.out.println(i18n.get("i18n/nongenerated", "No_facade_for_this_bundle"));

    // Fallbacks work as expected. Since this resource is not translated to russian, we fall back to the default
    System.out.println(i18n_ru.get(I18nMessages.FALLBACK));

    // Access a non-translated resource. This will by default print :MISSING:i18n/messages#non-existent-key:MISSING:
    System.out.println("This key does not exist: " + i18n.get("i18n/messages", "non-existent-key"));

    // Print the goodbye in the systems locale
    System.out.println(i18n.get(I18nMessages.GOODBYE));
  }


  public static void main(String[] args) {
    final ExampleApp exampleApp= new ExampleApp();
    exampleApp.run();
  }
}
