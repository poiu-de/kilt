# i18n-binder
Create an XLS sheet from Java localization resource bundles and vice versa.

The i18n Binder provides an Apache Ant task and an Apache Maven Plugin which allows you to
 * map i18n properties from multiple property files to an Microsoft Excel XLS file
 * write changed properties within the XLS file back to the original source files
 * create a Java source code facade based on the property files and their folder structure

In the first step the locales are separated from the absolute file path by a declared regualar expression pattern. This results in a as many columns as locales are matched within the XLS file. As rows all property keys per file are listed.

In the second step the properies changed within the XLS file are merged back into the original files. This is done with minimal impact on the property files which means comments and blank line are maintained as they are.

## Features

 * Supports file encoding like UTF-8
 * Allows minimal invasive changes (comments, blank line are maintained)
 * Easy to integrate in build tools like Apache Ant and Apache Maven
 * Pretty good overview over property values for different locales
 * Easier translation changes
 * Validation of existence of property keys over all locales
 * Generation of a Java source code facade



