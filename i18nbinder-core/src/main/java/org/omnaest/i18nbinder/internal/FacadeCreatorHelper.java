/*******************************************************************************
 * Copyright 2011 Danny Kunz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.omnaest.i18nbinder.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.omnaest.i18nbinder.grouping.FileGroup;
import org.omnaest.i18nbinder.grouping.FileGroupToPropertiesAdapter;
import org.omnaest.i18nbinder.grouping.FileGrouper;
import org.omnaest.utils.structure.collection.list.ListUtils;
import org.omnaest.utils.structure.element.converter.ElementConverterElementToMapEntry;
import org.omnaest.utils.structure.element.filter.ElementFilterNotBlank;
import org.omnaest.utils.structure.hierarchy.TokenMonoHierarchy;
import org.omnaest.utils.structure.hierarchy.TokenMonoHierarchy.TokenElementPath;
import org.omnaest.utils.structure.map.SimpleEntry;

/**
 * Helper to create a i18n facade Java source code file based on property files
 * 
 * @author Omnaest
 */
public class FacadeCreatorHelper
{
  /* ********************************************** Constants ********************************************** */
  public static final String DEFAULT_JAVA_FACADE_FILENAME_I18N_FACADE = "I18nFacade";
  public static final String LINE_SEPARATOR                           = System.getProperty( "line.separator" );
  
  /* ********************************************** Methods ********************************************** */
  
  /**
   * @param propertyFileSet
   * @param localeFilter
   * @param fileNameLocaleGroupPattern
   * @param groupingPatternGroupingGroupIndexList
   * @param i18nFacadeName
   * @param externalizeTypes
   * @param propertyfileEncoding
   * @return
   */
  public static Map<String, String> createI18nInterfaceFacadeFromPropertyFiles( Set<File> propertyFileSet,
                                                                                LocaleFilter localeFilter,
                                                                                String fileNameLocaleGroupPattern,
                                                                                List<Integer> groupingPatternGroupingGroupIndexList,
                                                                                String baseNameInTargetPlattform,
                                                                                String baseFolderIgnoredPath,
                                                                                String packageName,
                                                                                String i18nFacadeName,
                                                                                boolean externalizeTypes,
                                                                                String propertyfileEncoding )
  {
    //
    final Map<String, String> retmap = new LinkedHashMap<String, String>();
    
    //
    if ( propertyFileSet != null )
    {
      //
      Map<String, FileGroup> fileGroupIdentifierToFileGroupMap;
      {
        FileGrouper fileGrouper = new FileGrouper();
        try
        {
          if ( fileNameLocaleGroupPattern != null )
          {
            fileGrouper.setGroupingPatternString( fileNameLocaleGroupPattern );
          }
          if ( groupingPatternGroupingGroupIndexList != null )
          {
            fileGrouper.setGroupingPatternGroupingGroupIndexList( groupingPatternGroupingGroupIndexList );
          }
        }
        catch ( Exception e )
        {
          ModifierHelper.logger.info( e.getMessage() );
        }
        fileGrouper.setGroupingPatternReplacementToken( "" );
        fileGrouper.addAllFiles( propertyFileSet );
        fileGroupIdentifierToFileGroupMap = fileGrouper.determineFileGroupIdentifierToFileGroupMap();
      }
      
      //
      List<FileGroupToPropertiesAdapter> fileGroupToPropertiesAdapterList = new ArrayList<FileGroupToPropertiesAdapter>();
      {
        //
        for ( String fileGroupIdentifier : fileGroupIdentifierToFileGroupMap.keySet() )
        {
          //
          FileGroup fileGroup = fileGroupIdentifierToFileGroupMap.get( fileGroupIdentifier );
          
          //
          FileGroupToPropertiesAdapter fileGroupToPropertiesAdapter = new FileGroupToPropertiesAdapter( fileGroup );
          fileGroupToPropertiesAdapter.setFileEncoding( propertyfileEncoding );
          
          //
          fileGroupToPropertiesAdapterList.add( fileGroupToPropertiesAdapter );
        }
        
        //
        Collections.sort( fileGroupToPropertiesAdapterList, new Comparator<FileGroupToPropertiesAdapter>()
        {
          @Override
          public int compare( FileGroupToPropertiesAdapter fileGroupToPropertiesAdapter1,
                              FileGroupToPropertiesAdapter fileGroupToPropertiesAdapter2 )
          {
            //
            String fileGroupIdentifier1 = fileGroupToPropertiesAdapter1.getFileGroup().getFileGroupIdentifier();
            String fileGroupIdentifier2 = fileGroupToPropertiesAdapter2.getFileGroup().getFileGroupIdentifier();
            
            //
            return fileGroupIdentifier1.compareTo( fileGroupIdentifier2 );
          }
        } );
      }
      
      //determine all locales but fix the order
      List<String> localeList = new ArrayList<String>();
      {
        //
        Set<String> localeSet = new HashSet<String>();
        for ( FileGroupToPropertiesAdapter fileGroupToPropertiesAdapter : fileGroupToPropertiesAdapterList )
        {
          localeSet.addAll( fileGroupToPropertiesAdapter.determineGroupTokenList() );
        }
        localeList.addAll( localeSet );
        
        //
        for ( String locale : localeSet )
        {
          if ( !localeFilter.isLocaleAccepted( locale ) )
          {
            localeList.remove( locale );
          }
        }
        
        //
        Collections.sort( localeList );
      }
      
      //facade source code
      {
        //
        TokenMonoHierarchy<String, PropertyKeyAndValues> TokenMonoHierarchy = new TokenMonoHierarchy<String, PropertyKeyAndValues>();
        
        //
        for ( FileGroupToPropertiesAdapter fileGroupToPropertiesAdapter : fileGroupToPropertiesAdapterList )
        {
          //
          String fileGroupIdentifier = fileGroupToPropertiesAdapter.getFileGroup().getFileGroupIdentifier();
          
          //
          List<String> tokenPathElementList = new ArrayList<String>();
          {
            //
            final String pathDelimiter = "[\\\\/]";
            
            //
            if ( StringUtils.isNotBlank( baseNameInTargetPlattform ) )
            {
              //
              String[] baseNameTokens = baseNameInTargetPlattform.split( pathDelimiter );
              
              //
              tokenPathElementList.addAll( Arrays.asList( baseNameTokens ) );
            }
            
            //
            String[] fileGroupIdentifierTokens = fileGroupIdentifier.replaceFirst( Pattern.quote( baseFolderIgnoredPath ), "" )
                                                                    .split( pathDelimiter );
            if ( fileGroupIdentifierTokens.length > 0 )
            {
              //
              String lastToken = fileGroupIdentifierTokens[fileGroupIdentifierTokens.length - 1];
              lastToken = lastToken.replaceAll( "\\.properties$", "" ).replaceAll( "_", "" );
              fileGroupIdentifierTokens[fileGroupIdentifierTokens.length - 1] = lastToken;
              
              //
              tokenPathElementList.addAll( Arrays.asList( fileGroupIdentifierTokens ) );
            }
            
            //
            tokenPathElementList = ListUtils.filter( tokenPathElementList, new ElementFilterNotBlank() );
            
          }
          
          //
          ModifierHelper.logger.info( "Processing: " + fileGroupIdentifier );
          
          //
          List<String> propertyKeyList = new ArrayList<String>( fileGroupToPropertiesAdapter.determinePropertyKeySet() );
          Collections.sort( propertyKeyList );
          for ( String propertyKey : propertyKeyList )
          {
            if ( propertyKey != null )
            {
              //
              PropertyKeyAndValues propertyKeyAndValues = new PropertyKeyAndValues();
              {
                //
                propertyKeyAndValues.propertyKey = propertyKey;
                
                //
                for ( String locale : localeList )
                {
                  //
                  String value = fileGroupToPropertiesAdapter.resolvePropertyValue( propertyKey, locale );
                  value = StringUtils.defaultString( value );
                  if ( StringUtils.isNotBlank( value ) )
                  {
                    propertyKeyAndValues.valueList.add( locale + "=" + value );
                  }
                }
              }
              
              //
              TokenElementPath<String> tokenElementPath = new TokenElementPath<String>( tokenPathElementList );
              TokenMonoHierarchy.addTokenElementPathWithValues( tokenElementPath, propertyKeyAndValues );
            }
          }
        }
        
        //
        final Map<String, StringBuilder> externalizedClassToContentMap = externalizeTypes ? new LinkedHashMap<String, StringBuilder>()
                                                                                         : null;
        retmap.put( packageName + "." + i18nFacadeName,
                    buildFacadeSource( TokenMonoHierarchy, packageName, i18nFacadeName, externalizedClassToContentMap ) );
        if ( externalizeTypes )
        {
          for ( String subClassName : externalizedClassToContentMap.keySet() )
          {
            //
            final StringBuilder stringBuilder = externalizedClassToContentMap.get( subClassName );
            retmap.put( subClassName, stringBuilder.toString() );
          }
        }
      }
    }
    
    //
    return retmap;
  }
  
  protected static class PropertyKeyAndValues
  {
    public String       propertyKey = null;
    public List<String> valueList   = new ArrayList<String>();
  }
  
  private static String buildFacadeSource( TokenMonoHierarchy<String, PropertyKeyAndValues> TokenMonoHierarchy,
                                           String packageName,
                                           String i18nFacadeName,
                                           Map<String, StringBuilder> externalizedClassToContentMap )
  {
    //
    StringBuilder retval = new StringBuilder();
    
    //
    TokenMonoHierarchy<String, PropertyKeyAndValues>.Navigator navigator = TokenMonoHierarchy.getNavigator();
    
    //
    final String className = i18nFacadeName;
    final boolean isSubClass = false;
    final String rootPackageName = packageName;
    buildFacadeSource( retval, className, isSubClass, navigator, externalizedClassToContentMap, i18nFacadeName, packageName,
                       rootPackageName );
    
    return retval.toString().replaceAll( "\n", LINE_SEPARATOR );
  }
  
  private static void buildFacadeSource( StringBuilder stringBuilder,
                                         String className,
                                         boolean isSubClass,
                                         TokenMonoHierarchy<String, PropertyKeyAndValues>.Navigator navigator,
                                         Map<String, StringBuilder> externalizedClassToContentMap,
                                         String i18nFacadeName,
                                         String packageName,
                                         String rootPackageName )
  {
    //
    final Map<String, String> subClassNameToTokenElementMap = new LinkedHashMap<String, String>();
    final Map<String, List<String>> propertyNameToExampleValueListMap = new LinkedHashMap<String, List<String>>();
    final Map<String, String> propertyNameToPropertyKeyMap = new HashMap<String, String>();
    final String baseName = StringUtils.join( navigator.determineTokenPathElementList(), "." );
    final boolean externalizeTypes = externalizedClassToContentMap != null;
    final boolean staticModifier = !externalizeTypes && isSubClass;
    
    //
    {
      //
      List<String> tokenElementOfChildrenList = navigator.getTokenElementOfChildrenList();
      
      subClassNameToTokenElementMap.putAll( ListUtils.toMap( tokenElementOfChildrenList,
                                                             new CamelCaseTokenElementToMapEntryConverter( className ) ) );
      
    }
    final boolean hasAtLeastOneSubclass = !subClassNameToTokenElementMap.isEmpty();
    {
      //
      if ( navigator.hasValues() )
      {
        //
        List<PropertyKeyAndValues> propertyKeyAndValuesList = navigator.getValues();
        for ( PropertyKeyAndValues propertyKeyAndValues : propertyKeyAndValuesList )
        {
          //
          String propertyKey = propertyKeyAndValues.propertyKey;
          
          //
          String propertyName = "";
          {
            //
            {
              //
              String[] tokens = propertyKey.split( "[^a-zA-Z0-9]" );
              for ( String token : tokens )
              {
                propertyName += StringUtils.capitalize( token );
              }
            }
          }
          
          //
          {
            //
            final String key = propertyName;
            final List<String> valueList = new ArrayList<String>( propertyKeyAndValues.valueList );
            {
              //
              final String defaultLocaleString = String.valueOf( Locale.getDefault() );
              final String defaultLocaleLanguageString = String.valueOf( Locale.getDefault().getLanguage() );
              Collections.sort( valueList, new Comparator<String>()
              {
                @Override
                public int compare( String o1, String o2 )
                {
                  //
                  int retval = 0;
                  
                  //
                  final String firstElement1 = org.omnaest.utils.structure.collection.list.ListUtils.firstElement( org.omnaest.utils.structure.collection.list.ListUtils.valueOf( StringUtils.split( o1,
                                                                                                                                                                                                     "=" ) ) );
                  final String firstElement2 = org.omnaest.utils.structure.collection.list.ListUtils.firstElement( org.omnaest.utils.structure.collection.list.ListUtils.valueOf( StringUtils.split( o2,
                                                                                                                                                                                                     "=" ) ) );
                  
                  //
                  if ( StringUtils.startsWith( firstElement1, defaultLocaleString ) )
                  {
                    retval--;
                  }
                  if ( StringUtils.startsWith( firstElement2, defaultLocaleString ) )
                  {
                    retval++;
                  }
                  if ( StringUtils.contains( firstElement1, defaultLocaleString ) )
                  {
                    retval--;
                  }
                  if ( StringUtils.contains( firstElement2, defaultLocaleString ) )
                  {
                    retval++;
                  }
                  if ( StringUtils.contains( firstElement1, defaultLocaleLanguageString ) )
                  {
                    retval--;
                  }
                  if ( StringUtils.contains( firstElement2, defaultLocaleLanguageString ) )
                  {
                    retval++;
                  }
                  
                  //
                  return retval;
                }
              } );
            }
            propertyNameToExampleValueListMap.put( key, valueList );
          }
          
          //
          {
            //
            propertyNameToPropertyKeyMap.put( propertyName, propertyKey );
          }
        }
      }
    }
    
    //
    boolean hasBaseName = StringUtils.isNotBlank( baseName );
    boolean hasProperties = !propertyNameToExampleValueListMap.keySet().isEmpty();
    
    SortedSet<String> importSet = new TreeSet<String>();
    int importOffset = 0;
    
    //imports
    if ( !isSubClass || externalizeTypes )
    {
      //
      stringBuilder.append( StringUtils.isNotBlank( packageName ) ? "package " + packageName + ";\n\n" : "" );
      importOffset = stringBuilder.length();
      
      //stringBuilder.append( "import java.util.MissingResourceException;\n" );
      importSet.add( "java.util.Locale" );
      importSet.add( "java.util.MissingResourceException" );
      importSet.add( "javax.annotation.Generated" );
      
      //
      if ( !isSubClass )
      {
        importSet.add( "java.util.LinkedHashMap" );
        importSet.add( "java.util.ResourceBundle" );
      }
      
      //
      if ( externalizeTypes )
      {
        //
        if ( hasProperties )
        {
          importSet.add( rootPackageName + "." + i18nFacadeName );
          importSet.add( rootPackageName + "." + i18nFacadeName + ".Translator" );
        }
        
        //
        if ( hasAtLeastOneSubclass )
        {
          for ( String subClassName : subClassNameToTokenElementMap.keySet() )
          {
            importSet.add( packageName + "." + StringUtils.lowerCase( className ) + "." + subClassName );
          }
        }
      }
    }
    
    //documentation
    stringBuilder.append( "/**\n" );
    stringBuilder.append( " * This is an automatically with i18nBinder generated facade class.<br><br>\n" );
    stringBuilder.append( " * To modify please adapt the underlying property files.<br><br>\n" );
    stringBuilder.append( " * If the facade class is instantiated with a given {@link Locale} using {@link #" + className
                          + "(Locale)} all non static methods will use this predefined {@link Locale} when invoked.<br><br>\n" );
    stringBuilder.append( " * The facade methods will silently ignore all {@link MissingResourceException}s by default. To alter this behavior see {@link #"
                          + className + "(Locale, boolean)}<br><br>\n" );
    stringBuilder.append( hasBaseName ? " * Resource base: <b>" + baseName + "</b>\n" : "" );
    
    //
    if ( hasProperties )
    {
      printJavaDocPropertiesExamplesForSubclassAndInstance( stringBuilder, propertyNameToExampleValueListMap,
                                                            propertyNameToPropertyKeyMap );
    }
    
    for ( String subClassName : subClassNameToTokenElementMap.keySet() )
    {
      stringBuilder.append( " * @see " + subClassName + "\n" );
    }
    
    if ( hasProperties )
    {
      stringBuilder.append( " * @see #translator()\n" );
      stringBuilder.append( " * @see #translator(Locale)\n" );
    }
    stringBuilder.append( " */ \n" );
    stringBuilder.append( "@Generated(value = \"http://code.google.com/p/i18n-binder/\", date = \""
                          + DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format( Calendar.getInstance() ) + "\")\n" );
    
    //class
    stringBuilder.append( "public " + ( staticModifier ? "static " : "" ) + "class " + className + " {\n" );
    {
      //vars
      {
        //
        if ( !propertyNameToExampleValueListMap.isEmpty() )
        {
          //
          stringBuilder.append( "  public final static String baseName = \"" + baseName + "\";\n" );
          stringBuilder.append( "  private final Locale locale;\n" );
          stringBuilder.append( "  private final boolean silentlyIgnoreMissingResourceException;\n" );
        }
        
        //
        for ( String subClassName : subClassNameToTokenElementMap.keySet() )
        {
          //
          stringBuilder.append( "  /** @see " + subClassName + " */\n" );
          stringBuilder.append( "  public final " + subClassName + " " + subClassName + ";\n" );
        }
        
        if ( !isSubClass )
        {
          stringBuilder.append( "   /** Static access helper for the underlying resource */\n" );
          stringBuilder.append( "   public static class Resource\n" );
          stringBuilder.append( "  {\n" );
          stringBuilder.append( "    /** Internally used {@link ResourceBasedTranslator}. Changing this implementation affects the behavior of the whole facade */\n" );
          stringBuilder.append( "    public static ResourceBasedTranslator resourceBasedTranslator = new ResourceBasedTranslator()\n" );
          stringBuilder.append( "    {\n" );
          stringBuilder.append( "      @Override\n" );
          stringBuilder.append( "      public String translate( String baseName, String key, Locale locale )\n" );
          stringBuilder.append( "      {\n" );
          stringBuilder.append( "        ResourceBundle resourceBundle = ResourceBundle.getBundle( baseName,locale );\n" );
          stringBuilder.append( "        return resourceBundle.getString( key );\n" );
          stringBuilder.append( "      }\n\n" );
          stringBuilder.append( "      @Override\n" );
          stringBuilder.append( "      public String[] resolveAllKeys( String baseName, Locale locale )\n" );
          stringBuilder.append( "      {\n" );
          stringBuilder.append( "        ResourceBundle resourceBundle = ResourceBundle.getBundle( baseName,locale );\n" );
          stringBuilder.append( "        return resourceBundle.keySet().toArray( new String[0] );\n" );
          stringBuilder.append( "      }\n" );
          stringBuilder.append( "    };\n\n" );
          stringBuilder.append( "  }\n" );
          
          stringBuilder.append( "  /** Defines which {@link ResourceBasedTranslator} the facade should use. This affects all available instances. */\n" );
          stringBuilder.append( "  public static void use( ResourceBasedTranslator resourceBasedTranslator )\n" );
          stringBuilder.append( "  {\n" );
          stringBuilder.append( "    " + i18nFacadeName + ".Resource.resourceBasedTranslator = resourceBasedTranslator;\n" );
          stringBuilder.append( "  }\n\n" );
          
        }
      }
      
      //helper classes
      {
        if ( !isSubClass )
        {
          importSet.add( "java.util.Map" );
          appendResourceBasedTranslatorInterface( stringBuilder );
          appendTranslatorHelper( stringBuilder, i18nFacadeName );
        }
      }
      
      //constructor
      {
        //
        stringBuilder.append( "\n" );
        stringBuilder.append( "  /**\n" );
        stringBuilder.append( "   * This {@link "
                              + className
                              + "} constructor will create a new instance which silently ignores any {@link MissingResourceException} \n" );
        stringBuilder.append( "   * @see " + className + "\n" );
        stringBuilder.append( "   * @param locale\n" );
        stringBuilder.append( "   */ \n" );
        stringBuilder.append( "  public " + className + "( Locale locale )\n" );
        stringBuilder.append( "  {\n" );
        {
          //
          stringBuilder.append( "    this(locale,true);\n" );
        }
        stringBuilder.append( "  }\n" );
        stringBuilder.append( "  \n" );
        
        //
        stringBuilder.append( "\n" );
        stringBuilder.append( "  /**\n" );
        stringBuilder.append( "   * @see " + className + "\n" );
        stringBuilder.append( "   * @param locale\n" );
        stringBuilder.append( "   * @param silentlyIgnoreMissingResourceException\n" );
        stringBuilder.append( "   */ \n" );
        stringBuilder.append( "  public " + className + "( Locale locale, boolean silentlyIgnoreMissingResourceException )\n" );
        stringBuilder.append( "  {\n" );
        {
          //
          stringBuilder.append( "    super();\n" );
          if ( !propertyNameToExampleValueListMap.isEmpty() )
          {
            stringBuilder.append( "    this.locale = locale;\n" );
            stringBuilder.append( "    this.silentlyIgnoreMissingResourceException = silentlyIgnoreMissingResourceException;\n" );
          }
          
          //
          for ( String subClassName : subClassNameToTokenElementMap.keySet() )
          {
            stringBuilder.append( "    this." + subClassName + " = new " + subClassName
                                  + "( locale, silentlyIgnoreMissingResourceException );\n" );
          }
        }
        stringBuilder.append( "  }\n" );
        stringBuilder.append( "  \n" );
      }
      
      //static subclasses
      {
        //
        for ( String subClassName : subClassNameToTokenElementMap.keySet() )
        {
          //
          final boolean subClassIsSubClass = true;
          final String subClassPackageName = !externalizeTypes ? packageName : packageName + "."
                                                                               + StringUtils.lowerCase( className );
          final StringBuilder subClassStringBuilder;
          {
            //
            if ( externalizeTypes )
            {
              subClassStringBuilder = new StringBuilder();
              externalizedClassToContentMap.put( subClassPackageName + "." + subClassName, subClassStringBuilder );
            }
            else
            {
              subClassStringBuilder = stringBuilder;
            }
          }
          buildFacadeSource( subClassStringBuilder, subClassName, subClassIsSubClass,
                             navigator.newNavigatorFork().navigateToChild( subClassNameToTokenElementMap.get( subClassName ) ),
                             externalizedClassToContentMap, i18nFacadeName, subClassPackageName, rootPackageName );
        }
      }
      
      //methods based on properties
      if ( hasProperties )
      {
        //
        for ( String propertyName : propertyNameToExampleValueListMap.keySet() )
        {
          //
          String propertyKey = propertyNameToPropertyKeyMap.get( propertyName );
          List<String> exampleValueList = propertyNameToExampleValueListMap.get( propertyName );
          
          //
          List<String> replacementTokensForExampleValuesNumericPlaceholders = determineReplacementTokensForExampleValues( exampleValueList,
                                                                                                                          "\\{\\d+\\}" );
          List<String> replacementTokensForExampleValuesArbitraryPlaceholders = determineReplacementTokensForExampleValues( exampleValueList,
                                                                                                                            "\\{\\w+\\}" );
          
          boolean containsNumericalReplacementToken = replacementTokensForExampleValuesNumericPlaceholders.size() > 0;
          boolean containsArbitraryReplacementToken = !containsNumericalReplacementToken
                                                      && replacementTokensForExampleValuesArbitraryPlaceholders.size() > 0;
          
          //
          {
            //
            stringBuilder.append( "  /**\n" );
            stringBuilder.append( "   * Similar to {@link #get" + propertyName + "()} for the given {@link Locale}.\n" );
            stringBuilder.append( "   * @see " + className + "\n" );
            stringBuilder.append( "   * @see #get" + propertyName + "()\n" );
            stringBuilder.append( "   * @param locale \n" );
            stringBuilder.append( "   */ \n" );
            stringBuilder.append( "  protected String get" + propertyName + "(Locale locale)\n" );
            stringBuilder.append( "  {\n" );
            stringBuilder.append( "    try\n" );
            stringBuilder.append( "    {\n" );
            stringBuilder.append( "      final String key = \"" + propertyKey + "\";\n" );
            stringBuilder.append( "      return " + i18nFacadeName
                                  + ".Resource.resourceBasedTranslator.translate( baseName, key, locale );\n" );
            stringBuilder.append( "    }\n" );
            stringBuilder.append( "    catch ( MissingResourceException e )\n" );
            stringBuilder.append( "    {\n" );
            stringBuilder.append( "      if (!this.silentlyIgnoreMissingResourceException)\n" );
            stringBuilder.append( "      {\n" );
            stringBuilder.append( "        throw e;\n" );
            stringBuilder.append( "      }\n" );
            stringBuilder.append( "      return null;\n" );
            stringBuilder.append( "    }\n" );
            stringBuilder.append( "  }\n\n" );
            
            //
            stringBuilder.append( "  /**\n" );
            stringBuilder.append( "   * Returns the value of the property key <b>" + propertyKey
                                  + "</b> for the predefined {@link Locale}.\n" );
            printJavaDocPlaceholders( stringBuilder, replacementTokensForExampleValuesArbitraryPlaceholders );
            printJavaDocValueExamples( stringBuilder, exampleValueList );
            stringBuilder.append( "   * @see " + className + "\n" );
            stringBuilder.append( "   */ \n" );
            stringBuilder.append( "  public String get" + propertyName + "()\n" );
            stringBuilder.append( "  {\n" );
            stringBuilder.append( "    return get" + propertyName + "( this.locale );\n" );
            stringBuilder.append( "  }\n\n" );
          }
          
          //
          if ( containsNumericalReplacementToken )
          {
            //
            stringBuilder.append( "  /**\n" );
            stringBuilder.append( "   * Similar to  {@link #get" + propertyName + "(Object[])} using the given {@link Locale}.\n" );
            stringBuilder.append( "   * @see " + className + "\n" );
            stringBuilder.append( "   * @see #get" + propertyName + "(String[])\n" );
            stringBuilder.append( "   * @param locale\n" );
            stringBuilder.append( "   * @param tokens\n" );
            stringBuilder.append( "   */ \n" );
            stringBuilder.append( "  public String get" + propertyName + "( Locale locale, Object... tokens )\n" );
            stringBuilder.append( "  {\n" );
            stringBuilder.append( "    String retval = get" + propertyName + "( locale );\n" );
            stringBuilder.append( "    for ( int ii = 0; ii < tokens.length; ii++ )\n" );
            stringBuilder.append( "    {\n" );
            stringBuilder.append( "      String token = tokens[ii] != null ? tokens[ii].toString() : null;\n" );
            stringBuilder.append( "      if ( token != null )\n" );
            stringBuilder.append( "      {\n" );
            stringBuilder.append( "        retval = retval.replaceAll( \"\\\\{\" + ii + \"\\\\}\", token );\n" );
            stringBuilder.append( "      }\n" );
            stringBuilder.append( "    }\n" );
            stringBuilder.append( "    return retval;\n" );
            stringBuilder.append( "  }\n\n" );
            
            //
            stringBuilder.append( "  /**\n" );
            stringBuilder.append( "   * Returns the value of the property key <b>"
                                  + propertyKey
                                  + "</b> for the predefined {@link Locale} with all {0},{1},... placeholders replaced by the given tokens in their order.<br><br>\n" );
            stringBuilder.append( "   * If there are not enough parameters existing placeholders will remain unreplaced.\n" );
            printJavaDocPlaceholders( stringBuilder, replacementTokensForExampleValuesNumericPlaceholders );
            printJavaDocValueExamples( stringBuilder, exampleValueList );
            stringBuilder.append( "   * @see " + className + "\n" );
            stringBuilder.append( "   * @see #get" + propertyName + "(Locale,Object[])\n" );
            stringBuilder.append( "   * @param tokens\n" );
            stringBuilder.append( "   */ \n" );
            stringBuilder.append( "  public String get" + propertyName + "( Object... tokens )\n" );
            stringBuilder.append( "  {\n" );
            stringBuilder.append( "    return get" + propertyName + "( this.locale, tokens );\n" );
            stringBuilder.append( "  }\n\n" );
            
          }
          
          //
          if ( containsArbitraryReplacementToken )
          {
            importSet.add( "java.util.Map" );
            //
            stringBuilder.append( "  /**\n" );
            stringBuilder.append( "   * Returns the value of the property key <b>"
                                  + propertyKey
                                  + "</b> for the given {@link Locale} with arbitrary placeholder tag like {example} replaced by the given values.<br>\n" );
            stringBuilder.append( "   * The given placeholderToReplacementMap needs the placeholder tag name and a value. E.g. for {example} the key \"example\" has to be set.\n" );
            printJavaDocPlaceholders( stringBuilder, replacementTokensForExampleValuesArbitraryPlaceholders );
            printJavaDocValueExamples( stringBuilder, exampleValueList );
            stringBuilder.append( "   * @see " + className + "\n" );
            stringBuilder.append( "   * @see #get" + propertyName + "(Map)\n" );
            stringBuilder.append( "   * @param locale\n" );
            stringBuilder.append( "   * @param placeholderToReplacementMap\n" );
            stringBuilder.append( "   */ \n" );
            stringBuilder.append( "  public String get" + propertyName
                                  + "( Locale locale, Map<String, String> placeholderToReplacementMap )\n" );
            stringBuilder.append( "  {\n" );
            stringBuilder.append( "    String retval = get" + propertyName + "( locale );\n" );
            stringBuilder.append( "    if ( placeholderToReplacementMap != null )\n" );
            stringBuilder.append( "    {\n" );
            stringBuilder.append( "      for ( String placeholder : placeholderToReplacementMap.keySet() )\n" );
            stringBuilder.append( "      {\n" );
            stringBuilder.append( "        if ( placeholder != null )\n" );
            stringBuilder.append( "        {\n" );
            stringBuilder.append( "          String token = placeholderToReplacementMap.get( placeholder );\n" );
            stringBuilder.append( "          retval = retval.replaceAll( \"\\\\{\" + placeholder + \"\\\\}\", token );\n" );
            stringBuilder.append( "        }\n" );
            stringBuilder.append( "      }\n" );
            stringBuilder.append( "    }\n" );
            stringBuilder.append( "    return retval;\n" );
            stringBuilder.append( "  }\n\n" );
            
            //
            stringBuilder.append( "  /**\n" );
            stringBuilder.append( "   * Similar to  {@link #get" + propertyName
                                  + "(Locale,Map)} using the predefined {@link Locale}.\n" );
            stringBuilder.append( "   * @see " + className + "\n" );
            stringBuilder.append( "   * @see #get" + propertyName + "(Locale,Map)\n" );
            stringBuilder.append( "   * @param placeholderToReplacementMap\n" );
            stringBuilder.append( "   */ \n" );
            stringBuilder.append( "  public String get" + propertyName + "( Map<String, String> placeholderToReplacementMap )\n" );
            stringBuilder.append( "  {\n" );
            stringBuilder.append( "    return get" + propertyName + "( this.locale, placeholderToReplacementMap );\n" );
            stringBuilder.append( "  }\n\n" );
            
          }
        }
        
        //fluid factory methods
        {
          //
          stringBuilder.append( "  /**\n" );
          stringBuilder.append( "   * Returns a new instance of {@link " + className
                                + "} which uses the given setting for the exception handling\n" );
          stringBuilder.append( "   * @see " + className + "\n" );
          stringBuilder.append( "   * @param silentlyIgnoreMissingResourceException \n" );
          stringBuilder.append( "   */ \n" );
          stringBuilder.append( "  public " + className
                                + " doSilentlyIgnoreMissingResourceException( boolean silentlyIgnoreMissingResourceException )\n" );
          stringBuilder.append( "  {\n" );
          stringBuilder.append( "    return new " + className + "( this.locale, silentlyIgnoreMissingResourceException );\n" );
          stringBuilder.append( "  }\n\n" );
          
          //
          stringBuilder.append( "  /**\n" );
          stringBuilder.append( "   * Returns a new instance of {@link " + className + "} which uses the given {@link Locale}\n" );
          stringBuilder.append( "   * @see " + className + "\n" );
          stringBuilder.append( "   * @param locale \n" );
          stringBuilder.append( "   */ \n" );
          stringBuilder.append( "  public " + className + " forLocale( Locale locale )\n" );
          stringBuilder.append( "  {\n" );
          stringBuilder.append( "    return new " + className + "( locale, this.silentlyIgnoreMissingResourceException );\n" );
          stringBuilder.append( "  }\n\n" );
          
        }
        
        //translator methods
        {
          //
          stringBuilder.append( "  /**\n" );
          stringBuilder.append( "   * Returns a new {@link Translator} instance using the given {@link Locale} and based on the {@value #baseName} i18n base\n" );
          stringBuilder.append( "   * @see " + className + "\n" );
          stringBuilder.append( "   * @see #translator()\n" );
          stringBuilder.append( "   * @see #translator(Locale)\n" );
          stringBuilder.append( "   * @return {@link Translator}" );
          stringBuilder.append( "   */ \n" );
          stringBuilder.append( "  public static Translator translator(Locale locale, boolean silentlyIgnoreMissingResourceException)\n" );
          stringBuilder.append( "  {\n" );
          stringBuilder.append( "    return new Translator( baseName, locale, silentlyIgnoreMissingResourceException );\n" );
          stringBuilder.append( "  }\n\n" );
          
          stringBuilder.append( "  /**\n" );
          stringBuilder.append( "   * Returns a new {@link Translator} instance using the given {@link Locale} and based on the {@value #baseName} i18n base\n" );
          stringBuilder.append( "   * @see " + className + "\n" );
          stringBuilder.append( "   * @see #translator()\n" );
          stringBuilder.append( "   * @see #translator(Locale,boolean)\n" );
          stringBuilder.append( "   * @return {@link Translator}" );
          stringBuilder.append( "   */ \n" );
          stringBuilder.append( "  public Translator translator(Locale locale)\n" );
          stringBuilder.append( "  {\n" );
          stringBuilder.append( "    return new Translator( baseName, locale, this.silentlyIgnoreMissingResourceException );\n" );
          stringBuilder.append( "  }\n\n" );
          
          stringBuilder.append( "  /**\n" );
          stringBuilder.append( "   * Returns a new {@link Translator} instance using the internal {@link Locale} and based on the {@value #baseName} i18n base\n" );
          stringBuilder.append( "   * @see " + className + "\n" );
          stringBuilder.append( "   * @see #translator(Locale)\n" );
          stringBuilder.append( "   * @see #translator(Locale,boolean)\n" );
          stringBuilder.append( "   * @return {@link Translator}" );
          stringBuilder.append( "   */ \n" );
          stringBuilder.append( "  public Translator translator()\n" );
          stringBuilder.append( "  {\n" );
          stringBuilder.append( "    return translator( this.locale );\n" );
          stringBuilder.append( "  }\n\n" );
        }
      }
    }
    
    //
    stringBuilder.append( "}\n\n" );
    
    StringBuilder importBuf = new StringBuilder();
    for ( String importDef : importSet )
    {
      importBuf.append( "import " + importDef + ";\n" );
    }
    stringBuilder.insert( importOffset, importBuf.toString() + "\n" );
  }
  
  private static void printJavaDocPropertiesExamplesForSubclassAndInstance( StringBuilder stringBuilder,
                                                                            final Map<String, List<String>> propertyNameToExampleValueListMap,
                                                                            final Map<String, String> propertyNameToPropertyKeyMap )
  {
    //
    stringBuilder.append( " * <br><br>\n" );
    stringBuilder.append( " * <h1>Examples:</h1>\n" );
    stringBuilder.append( " * <table border=\"1\">\n" );
    
    stringBuilder.append( " * <thead>\n" );
    stringBuilder.append( " * <tr>\n" );
    stringBuilder.append( " * <th>key</th>\n" );
    stringBuilder.append( " * <th>examples</th>\n" );
    stringBuilder.append( " * </tr>\n" );
    stringBuilder.append( " * </thead>\n" );
    
    stringBuilder.append( " * <tbody>\n" );
    for ( String propertyName : propertyNameToExampleValueListMap.keySet() )
    {
      //
      final int exampleSizeMax = 3;
      
      //
      final String propertyKey = propertyNameToPropertyKeyMap.get( propertyName );
      final List<String> exampleValueList = new ArrayList<String>( propertyNameToExampleValueListMap.get( propertyName ) );
      {
        while ( exampleValueList.size() > exampleSizeMax )
        {
          exampleValueList.remove( exampleValueList.size() - 1 );
        }
      }
      final Iterator<String> iteratorExampleValueList = exampleValueList.iterator();
      
      //
      final int exampleSize = exampleValueList.size();
      if ( exampleSize > 0 )
      {
        //
        stringBuilder.append( " * <tr>\n" );
        stringBuilder.append( " * <td rowspan=\"" + exampleSize + "\">" + propertyKey + "</td>\n" );
        stringBuilder.append( " * <td>" + iteratorExampleValueList.next() + "</td>\n" );
        stringBuilder.append( " * </tr>\n" );
        while ( iteratorExampleValueList.hasNext() )
        {
          //
          stringBuilder.append( " * <tr>\n" );
          stringBuilder.append( " * <td><small>" + iteratorExampleValueList.next() + "</small></td>\n" );
          stringBuilder.append( " * </tr>\n" );
        }
      }
    }
    stringBuilder.append( " * </tbody>\n" );
    stringBuilder.append( " * </table><br><br>\n" );
  }
  
  private static void appendResourceBasedTranslatorInterface( StringBuilder stringBuilder )
  {
    //
    stringBuilder.append( "\n" );
    stringBuilder.append( "  /**\n" );
    stringBuilder.append( "   * Basic interface which is used by the facade to resolve translated values for given keys<br>\n" );
    stringBuilder.append( "   * <br>\n" );
    stringBuilder.append( "   * Any implementation should be thread safe" );
    stringBuilder.append( "   */ \n" );
    stringBuilder.append( "  public static interface ResourceBasedTranslator {\n" );
    
    stringBuilder.append( "    /**\n" );
    stringBuilder.append( "     * Returns the translated value for the given key respecting the base name and the given {@link Locale}\n" );
    stringBuilder.append( "     * @param baseName\n" );
    stringBuilder.append( "     * @param key\n" );
    stringBuilder.append( "     * @param locale\n" );
    stringBuilder.append( "     * @return\n" );
    stringBuilder.append( "     */ \n" );
    stringBuilder.append( "    public String translate( String baseName, String key, Locale locale );\n" );
    
    stringBuilder.append( "    /**\n" );
    stringBuilder.append( "     * Returns all available keys for the given {@link Locale}\n" );
    stringBuilder.append( "     * @param baseName\n" );
    stringBuilder.append( "     * @param locale\n" );
    stringBuilder.append( "     * @return\n" );
    stringBuilder.append( "     */ \n" );
    stringBuilder.append( "    public String[] resolveAllKeys( String baseName, Locale locale );\n" );
    
    stringBuilder.append( "  }\n" );
    stringBuilder.append( "\n" );
    
  }
  
  private static void appendTranslatorHelper( StringBuilder stringBuilder, String I18nFacadeName )
  {
    stringBuilder.append( "\n" );
    stringBuilder.append( "  /**\n" );
    stringBuilder.append( "   * A {@link Translator} offers several methods to translate arbitrary keys into their i18n counterpart based on the initially\n" );
    stringBuilder.append( "   * given {@link Locale}.\n" );
    stringBuilder.append( "   * \n" );
    stringBuilder.append( "   * @see #translate(String)\n" );
    stringBuilder.append( "   * @see #translate(String[]) \n" );
    stringBuilder.append( "   * @see #allPropertyKeys() \n" );
    stringBuilder.append( "   */ \n" );
    stringBuilder.append( "  public static class Translator {\n" );
    
    //translator vars and constructor
    {
      stringBuilder.append( "\n" );
      stringBuilder.append( "    private final String baseName;\n" );
      stringBuilder.append( "    private final Locale locale;\n" );
      stringBuilder.append( "    private final boolean silentlyIgnoreMissingResourceException;\n" );
      stringBuilder.append( "\n" );
      
      stringBuilder.append( "    /**\n" );
      stringBuilder.append( "     * @see Translator\n" );
      stringBuilder.append( "     * @param baseName\n" );
      stringBuilder.append( "     * @param locale\n" );
      stringBuilder.append( "     */ \n" );
      stringBuilder.append( "    public Translator( String baseName, Locale locale )\n" );
      stringBuilder.append( "    {\n" );
      stringBuilder.append( "      this(baseName,locale,true);\n" );
      stringBuilder.append( "    }\n\n" );
      
      stringBuilder.append( "    /**\n" );
      stringBuilder.append( "     * @see Translator\n" );
      stringBuilder.append( "     * @param baseName\n" );
      stringBuilder.append( "     * @param locale\n" );
      stringBuilder.append( "     */ \n" );
      stringBuilder.append( "    public Translator( String baseName, Locale locale, boolean silentlyIgnoreMissingResourceException )\n" );
      stringBuilder.append( "    {\n" );
      stringBuilder.append( "      super();\n" );
      stringBuilder.append( "      this.baseName = baseName;\n" );
      stringBuilder.append( "      this.locale = locale;\n" );
      stringBuilder.append( "      this.silentlyIgnoreMissingResourceException = silentlyIgnoreMissingResourceException;\n" );
      stringBuilder.append( "    }\n\n" );
      
    }
    
    //translation map methods
    {
      //
      stringBuilder.append( "    /**\n" );
      stringBuilder.append( "     * Returns the translated property key for the given {@link Locale}\n" );
      stringBuilder.append( "     * @see Translator\n" );
      stringBuilder.append( "     * @see #translate(String)\n" );
      stringBuilder.append( "     * @see #translate(String[])\n" );
      stringBuilder.append( "     */ \n" );
      stringBuilder.append( "    public String translate(Locale locale, String key)\n" );
      stringBuilder.append( "    {\n" );
      stringBuilder.append( "      try\n" );
      stringBuilder.append( "      {\n" );
      stringBuilder.append( "        return " + I18nFacadeName
                            + ".Resource.resourceBasedTranslator.translate( this.baseName, key, locale );\n" );
      stringBuilder.append( "      }\n" );
      stringBuilder.append( "      catch ( MissingResourceException e )\n" );
      stringBuilder.append( "      {\n" );
      stringBuilder.append( "        if (!this.silentlyIgnoreMissingResourceException)\n" );
      stringBuilder.append( "        {\n" );
      stringBuilder.append( "          throw e;\n" );
      stringBuilder.append( "        }\n" );
      stringBuilder.append( "        return null;\n" );
      stringBuilder.append( "      }\n" );
      stringBuilder.append( "    }\n\n" );
      
      stringBuilder.append( "    /**\n" );
      stringBuilder.append( "     * Returns the translated property key for the predefined {@link Locale}\n" );
      stringBuilder.append( "     * @see Translator\n" );
      stringBuilder.append( "     * @see #translate(Locale, String)\n" );
      stringBuilder.append( "     * @see #translate(String[])\n" );
      stringBuilder.append( "     */ \n" );
      stringBuilder.append( "    public String translate( String key )\n" );
      stringBuilder.append( "    {\n" );
      stringBuilder.append( "      return translate( this.locale, key );\n" );
      stringBuilder.append( "    }\n\n" );
      
      //
      stringBuilder.append( "    /**\n" );
      stringBuilder.append( "     * Returns a translation {@link Map} with the given property keys and their respective values for the given {@link Locale}.\n" );
      stringBuilder.append( "     * @param keys \n" );
      stringBuilder.append( "     * @see Translator\n" );
      stringBuilder.append( "     * @see #allPropertyKeys()\n" );
      stringBuilder.append( "     * @see #translate(String)\n" );
      stringBuilder.append( "     */ \n" );
      stringBuilder.append( "    public Map<String, String> translate( Locale locale, String... keys )\n" );
      stringBuilder.append( "    {\n" );
      stringBuilder.append( "      Map<String, String> retmap = new LinkedHashMap<String, String>();\n" );
      stringBuilder.append( "      for ( String key : keys )\n" );
      stringBuilder.append( "      {\n" );
      stringBuilder.append( "        retmap.put( key, translate( locale, key ) );\n" );
      stringBuilder.append( "      }\n" );
      stringBuilder.append( "      return retmap;\n" );
      stringBuilder.append( "    }\n\n" );
      
      stringBuilder.append( "    /**\n" );
      stringBuilder.append( "     * Returns a translation {@link Map} with the given property keys and their respective values for the predefined {@link Locale}.\n" );
      stringBuilder.append( "     * @param keys \n" );
      stringBuilder.append( "     * @see Translator\n" );
      stringBuilder.append( "     * @see #allPropertyKeys()\n" );
      stringBuilder.append( "     * @see #translate(String)\n" );
      stringBuilder.append( "     */ \n" );
      stringBuilder.append( "    public Map<String, String> translate( String... keys )\n" );
      stringBuilder.append( "    {\n" );
      stringBuilder.append( "      return translate( this.locale, keys );\n" );
      stringBuilder.append( "    }\n\n" );
      
      //
      stringBuilder.append( "    /**\n" );
      stringBuilder.append( "     * Returns all available property keys for the given {@link Locale}. \n" );
      stringBuilder.append( "     * @see Translator\n" );
      stringBuilder.append( "     * @see #allPropertyKeys()\n" );
      stringBuilder.append( "     * @see #translate(String[])\n" );
      stringBuilder.append( "     */ \n" );
      stringBuilder.append( "    public String[] allPropertyKeys(Locale locale)\n" );
      stringBuilder.append( "    {\n" );
      stringBuilder.append( "      return " + I18nFacadeName
                            + ".Resource.resourceBasedTranslator.resolveAllKeys( this.baseName, locale );\n" );
      stringBuilder.append( "    }\n\n" );
      
      stringBuilder.append( "    /**\n" );
      stringBuilder.append( "     * Returns all available property keys for the predefined {@link Locale}. \n" );
      stringBuilder.append( "     * @see Translator\n" );
      stringBuilder.append( "     * @see #allPropertyKeys(Locale)\n" );
      stringBuilder.append( "     * @see #translate(String[])\n" );
      stringBuilder.append( "     */ \n" );
      stringBuilder.append( "    public String[] allPropertyKeys()\n" );
      stringBuilder.append( "    {\n" );
      stringBuilder.append( "      return allPropertyKeys( this.locale );\n" );
      stringBuilder.append( "    }\n\n" );
      
      //
      stringBuilder.append( "    /**\n" );
      stringBuilder.append( "     * Returns a translation {@link Map} for the predefined {@link Locale} including all available i18n keys resolved using \n" );
      stringBuilder.append( "     * {@link #allPropertyKeys()} and their respective translation values resolved using {@link #translate(String...)} \n" );
      stringBuilder.append( "     * @see Translator\n" );
      stringBuilder.append( "     * @see #allPropertyKeys(Locale)\n" );
      stringBuilder.append( "     * @see #translate(String[])\n" );
      stringBuilder.append( "     * @return {@link Map}\n" );
      stringBuilder.append( "     */ \n" );
      stringBuilder.append( "    public Map<String, String> translationMap()\n" );
      stringBuilder.append( "    {\n" );
      stringBuilder.append( "      return this.translate( this.allPropertyKeys() );\n" );
      stringBuilder.append( "    }\n\n" );
      
      stringBuilder.append( "    /**\n" );
      stringBuilder.append( "     * Similar to {@link #translationMap()} for the given {@link Locale} instead. \n" );
      stringBuilder.append( "     * @see Translator\n" );
      stringBuilder.append( "     * @see #allPropertyKeys(Locale)\n" );
      stringBuilder.append( "     * @see #translate(String[])\n" );
      stringBuilder.append( "     * @param locale\n" );
      stringBuilder.append( "     * @return {@link Map}\n" );
      stringBuilder.append( "     */ \n" );
      stringBuilder.append( "    public Map<String, String> translationMap( Locale locale )\n" );
      stringBuilder.append( "    {\n" );
      stringBuilder.append( "      return this.translate( locale, this.allPropertyKeys( locale ) );\n" );
      stringBuilder.append( "    }\n\n" );
    }
    
    //
    stringBuilder.append( "  }\n" );
    stringBuilder.append( "\n" );
  }
  
  private static void printJavaDocPlaceholders( StringBuilder stringBuilder,
                                                List<String> replacementTokensForExampleValuesPlaceholders )
  {
    stringBuilder.append( "   * <br><br>\n" );
    if ( !replacementTokensForExampleValuesPlaceholders.isEmpty() )
    {
      stringBuilder.append( "   * Placeholders:\n" );
      stringBuilder.append( "   * <ul>\n" );
      for ( String replacementToken : replacementTokensForExampleValuesPlaceholders )
      {
        stringBuilder.append( "   * <li><b>" + replacementToken + "</b></li>\n" );
      }
      stringBuilder.append( "   * </ul>\n" );
    }
  }
  
  /**
   * @param stringBuilder
   * @param exampleValueList
   */
  private static void printJavaDocValueExamples( StringBuilder stringBuilder, List<String> exampleValueList )
  {
    stringBuilder.append( "   * \n" );
    stringBuilder.append( "   * Examples:\n" );
    stringBuilder.append( "   * <ul>\n" );
    for ( String exampleValue : exampleValueList )
    {
      stringBuilder.append( "   * <li>" + exampleValue + "</li>\n" );
    }
    stringBuilder.append( "   * </ul>\n" );
  }
  
  /**
   * @param exampleValueList
   * @param regexTokenPattern
   * @return
   */
  private static List<String> determineReplacementTokensForExampleValues( List<String> exampleValueList, String regexTokenPattern )
  {
    //
    Set<String> retset = new LinkedHashSet<String>();
    
    //
    final Pattern pattern = Pattern.compile( regexTokenPattern );
    for ( String exampleValue : exampleValueList )
    {
      Matcher matcher = pattern.matcher( exampleValue );
      while ( matcher.find() )
      {
        retset.add( matcher.group() );
      }
    }
    
    //
    return new ArrayList<String>( retset );
  }
  
  protected static class CamelCaseTokenElementToMapEntryConverter implements
                                                                 ElementConverterElementToMapEntry<String, String, String>
  {
    private static final long serialVersionUID = 1L;
    
    /* ********************************************** Variables ********************************************** */
    public String             excludedkey      = null;
    
    /* ********************************************** Methods ********************************************** */
    public CamelCaseTokenElementToMapEntryConverter( String excludedkey )
    {
      super();
      this.excludedkey = excludedkey;
    }
    
    @Override
    public Entry<String, String> convert( String element )
    {
      //
      String key = "";
      String value = "";
      
      //
      if ( element != null )
      {
        //
        String[] tokens = element.split( "[^a-zA-Z0-9]" );
        for ( String token : tokens )
        {
          key += StringUtils.capitalize( token );
        }
        
        //
        key = StringUtils.isBlank( key ) ? "Root" : key;
        key = key.matches( "\\d+.*" ) ? "_" + key : key;
        key = StringUtils.equals( key, this.excludedkey ) ? key + "_" : key;
        
        //
        value = element;
      }
      
      //
      return new SimpleEntry<String, String>( key, value );
    }
    
  }
}
