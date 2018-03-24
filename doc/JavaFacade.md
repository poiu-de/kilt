Java Facade
-----------

## General

The i18nBinder is able to generate Java source code files based on a *given set of property files* and their folder structure.

This means that the general modification source will be the source property files and not any Java code. The Java code will be just generated on top of it, to act as an typed wrapper.

## What is the advante of that approach?

The major advantage is the type awareness of the Java compiler. If any property is used but not anymore provided within the source property files the respective code is not generated anymore and the Java compiler complains, if there is still a reference to it.

Further the generated code has lots of content preview provided as Javadoc.

## How to generate the Java Facade using Apache Ant

See the [Getting Started Guide](./GettingStarted.md).

Basically you have to provide following information within the i18nBinder.properties: 

* Filename of the created Java facade source file. The facade is only created if createJavaFacade is set to true

```
javaFacadeFilename        = I18nFacade.java
packageName               = org.your.package.name
baseNameInTargetPlattform = i18n
```

If set to true the Java facade will generate all subtypes as new separated Java files instead of one single nested Java class

```
externalizeTypes           = true
```

|property|default|description|
|:-------|:------|:----------|
|javaFacadeFilename|I18nFacade.java|The root facade file name|
|packageName|       |The package name the facade should reside in|
|baseNameInTargetPlattform|i18n   |The root folder of the i18n files, therefore a Java root type will be generated having that given name|
|externalizeTypes|true   |If false a single Java file is generated, otherwise a whole package structure is created similar to the i18n folder structure|

## How to generate the Java Facade using Apache Maven

If your project is using Apache Maven 3.0.3+ you can add the i18nbinder-maven-plugin to let the Java facade being generated.
As default the facade will be generated into the project/target/generated-sources/i18nbinder folder.

A basic plugin configuration would look like:

```xml
        <plugin>
          <groupId>org.omnaest.i18nbinder</groupId>
          <artifactId>i18nbinder-maven-plugin</artifactId>
          <version>0.1.18</version>
          <configuration>
            <packageName>my.target.package</packageName>
          </configuration>
          <executions>
            <execution>
              <id>i18nbinder</id>
              <phase>generate-sources</phase>
              <goals>
                <goal>i18nbinder</goal>
              </goals>
              <inherited>false</inherited>
              <configuration>
            </configuration>
          </execution>
        </executions>
      </plugin>
```

A more advanced configuration would look like:
```xml
      <plugin>
        <groupId>org.omnaest.i18nbinder</groupId>
        <artifactId>i18nbinder-maven-plugin</artifactId>
        <version>0.1.18</version> 
        <configuration>
          <packageName>my.target.package</packageName>
          <baseNameInTargetPlattform>i18n</baseNameInTargetPlattform>
          <externalizeTypes>true</externalizeTypes>
          <propertiesRootDirectory>src/main/resources/i18n</propertiesRootDirectory>
          <i18nFacadeName>MyFacade</i18nFacadeName>
          <localeFilterRegex>de_DE|en_US</localeFilterRegex>
        </configuration>
        <executions>
          <execution>
            <id>i18nbinder</id>
            <phase>generate-sources</phase>
            <goals> 
              <goal>i18nbinder</goal>
            </goals> 
            <inherited>false</inherited>
            <configuration>
          </configuration>
        </execution>
      </executions>
    </plugin>

```

|parameter|default|example|description|
|:--------|:------|:------|:----------|
|packageName|       |org.omnaest.i18nbinder.test|name of the package the Java facade is located in| 
|baseNameInTargetPlattform|i18n   |i18nTranslation|name of the generated Java type for the root folder the i18n source files|
|i18nFacadeName|I18nFacade|PropertyFacade|The name of the generated Java facade|
|externalizeTypes|true   |true or false|If false only one single Java file is generated with nested types, otherwise a package folder structure is created similar to the i18n folders|
|propertiesRootDirectory|src/main/resources/i18n|src/main/resources/properties|The location root of the i18n properties files|
|localeFilterRegex|.|de_DE|en_US|Filter for specific locales. If there are more locales available as code should be generated for this filter can be used. It is a    regular expression which should include all locales|
|logResolvedPropertyFileNames|false  |true or false|If set to true the i18nBinder logs which files are resolved and written|

