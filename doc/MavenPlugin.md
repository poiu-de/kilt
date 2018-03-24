i18nbinder Maven Plugin
-----------------------

The i18nbinder is now available as a Apache Maven Plugin.

There are two goals which have to be invoked manually:                        
 * `i18nbinder:create-xls`     
 * `i18nbinder:write-properties`

They are available to create an xls file from the defined property files and to write the content of the xls file back to those.

And one goal which create the i18nfacade during compilation:

 * `i18nbinder:i18nbinder`

## Maven plugin example

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
 
Notes to the example configuration: 
 
  * the original *i18n files* are located in `src\main\resources\i18n` 
  * the *i18nFacade* will be generated into `\target\generated-sources\i18nbinder` 
  * the *i18n.xls* file will be generated into the project root 
  * the location of the *property files* are stored within the xls file itself with absolute path names and are written back to those