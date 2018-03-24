Getting Started
---------------

## Prerequisites

 * Java SE 1.6+
 * Apache Ant 1.8.1+ or Apache Maven 3.0.3+
 
# Download

Download a release package from the releases tab and extract it.

## Include in Maven

If i18nbinder will be used as a maven plugin it can be included with the following maven coordinates:

```
        <groupId> org.omnaest.i18nbinder </groupId>
        <artifactId> i18nbinder-maven-plugin </artifactId>
        <version> 0.1.18-SNAPSHOT </version>
```

See an example under [Maven Plugin](./MavenPlugin.md).

## Usage

### Provide i18n directory

Provide a directory structure filled with property files you want to convert.

### Adapt i18nBinder.properties
Open the `i18nBinder.properties` within the root folder of the extracted package. Adapt the necessary properies there.

For example 

* the directory of the i18n files
* the regular expression pattern for the file grouping

As default the regular expression groups locales are supposed to be at the end of the filename, like e.g.:

C:\i18n_src\subfolder1\administration_de_DE.properties C:\i18n_src\subfolder1\administration_en_US.properties 

If your i18n directory has another structure like e.g.:

C:\i18n_src\de_DE\administration.properties C:\i18n_src\en_US\administration.properties

you have to change the grouping expression.

See the `i18nBinder.properties` file template for examples.

### Create XLS File

Now call

```
ant createXLSFile
```

to create the XLS file from the properties.

### Write properties back to the i18n directory files

If you have a XLS file you can write the changes back to the original property files.
This is done by

```
ant writeProperties
```

### Create a Java source code facade based on the directories and property files

Adapt the `i18nBinder.properties`:

```xml
javaFacadeFilename          = I18nFacade.java
packageName                 = ome.package.example
baseNameInTargetPlattform   = i18n
```

Note: the `baseNameInTargetPlattform` flag will add a folder structure to the facade. This is important if your target plattform will have additional folders before those you run the i18nBinder with.

the run:

```
ant createJavaFacade
```

See also [Java Facade](./JavaFacade.md)