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
package org.omnaest.i18nbinder.grouping;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.omnaest.utils.propertyfile.PropertyFile;
import org.omnaest.utils.propertyfile.content.PropertyFileContent;
import org.omnaest.utils.propertyfile.content.PropertyMap;
import org.omnaest.utils.propertyfile.content.element.Property;

/**
 * Adapter to allow easy access to properties of all files which are part of a {@link FileGroup}.
 * 
 * @author Omnaest
 */
public class FileGroupToPropertiesAdapter
{
  /* ********************************************** Constants ********************************************** */
  public static final String LINE_SEPARATOR             = System.getProperty( "line.separator" );
  public static final String MULTILINE_VALUES_SEPARATOR = "\\" + LINE_SEPARATOR;
  
  /* ********************************************** Variables ********************************************** */
  protected FileGroup        fileGroup                  = null;
  private PropertyFileCache  propertyFileCache          = new PropertyFileCache();
  private String             fileEncoding               = "utf-8";
  
  /* ********************************************** Classes/Interfaces ********************************************** */
  /**
   * Container for a {@link Property} and its respective {@link PropertyFile}.
   * 
   * @author Omnaest
   */
  protected static class PropertyFileAndProperty
  {
    /* ********************************************** Variables ********************************************** */
    
    protected PropertyFile propertyFile = null;
    protected Property     property     = null;
    
    /* ********************************************** Methods ********************************************** */
    public PropertyFile getPropertyFile()
    {
      return this.propertyFile;
    }
    
    public void setPropertyFile( PropertyFile propertyFile )
    {
      this.propertyFile = propertyFile;
    }
    
    public Property getProperty()
    {
      return this.property;
    }
    
    public void setProperty( Property property )
    {
      this.property = property;
    }
    
  }
  
  private static class PropertyFileCache
  {
    private Map<File, PropertyFile> cache = new WeakHashMap<File, PropertyFile>();
    
    public PropertyFile get( File file )
    {
      return this.cache.get( file );
    }
    
    public void put( PropertyFile propertyFile )
    {
      this.cache.put( propertyFile.getFile(), propertyFile );
    }
  }
  
  /* ********************************************** Methods ********************************************** */
  
  public FileGroupToPropertiesAdapter( FileGroup fileGroup )
  {
    super();
    this.fileGroup = fileGroup;
  }
  
  public FileGroup getFileGroup()
  {
    return this.fileGroup;
  }
  
  public void setFileGroup( FileGroup fileGroup )
  {
    this.fileGroup = fileGroup;
  }
  
  /**
   * Resolves a {@link PropertyFile} for the given group token using the {@link FileGroup}.
   * 
   * @param groupToken
   * @param fileEncoding
   * @return
   */
  protected PropertyFile resolvePropertyFile( String groupToken, String fileEncoding )
  {
    //
    PropertyFile retval = null;
    
    //
    if ( groupToken != null )
    {
      //
      Map<String, File> groupTokenToFileMap = this.fileGroup.getGroupTokenToFileMap();
      
      //
      File file = groupTokenToFileMap.get( groupToken );
      if ( file != null )
      {
        //
        retval = new PropertyFile( file );
        retval.setFileEncoding( fileEncoding );
      }
    }
    
    //
    return retval;
  }
  
  /**
   * Resolves and loads a {@link PropertyFile}
   * 
   * @see #resolvePropertyFile(String)
   * @param groupToken
   * @return
   */
  protected PropertyFile resolveAndLoadPropertyFile( String groupToken )
  {
    //
    PropertyFile propertyFile = this.resolvePropertyFile( groupToken, this.fileEncoding );
    
    //
    if ( propertyFile != null )
    {
      //
      PropertyFile propertyFileFromCache = this.propertyFileCache.get( propertyFile.getFile() );
      if ( propertyFileFromCache != null )
      {
        propertyFile = propertyFileFromCache;
      }
      else
      {
        propertyFile.load();
        this.propertyFileCache.put( propertyFile );
      }
    }
    
    //
    return propertyFile;
  }
  
  /**
   * @see PropertyFileAndProperty
   * @param propertyKey
   * @param groupToken
   * @return
   */
  protected PropertyFileAndProperty resolvePropertyFileAndProperty( String propertyKey, String groupToken )
  {
    //
    PropertyFileAndProperty retval = null;
    
    //
    if ( propertyKey != null && groupToken != null )
    {
      //
      PropertyFile propertyFile = this.resolveAndLoadPropertyFile( groupToken );
      if ( propertyFile != null )
      {
        //
        PropertyFileContent propertyFileContent = propertyFile.getPropertyFileContent();
        PropertyMap propertyMap = propertyFileContent.getPropertyMap();
        
        //
        Property property = propertyMap.get( propertyKey );
        if ( property != null )
        {
          //
          retval = new PropertyFileAndProperty();
          retval.setProperty( property );
          retval.setPropertyFile( propertyFile );
        }
      }
    }
    
    //
    return retval;
  }
  
  public void writePropertyValue( String propertyKey, String groupToken, String value )
  {
    //
    if ( propertyKey != null && groupToken != null && value != null )
    {
      //
      PropertyFileAndProperty propertyFileAndProperty = this.resolvePropertyFileAndProperty( propertyKey, groupToken );
      if ( propertyFileAndProperty != null )
      {
        //
        Property property = propertyFileAndProperty.getProperty();
        if ( property != null )
        {
          //
          String[] valueTokens = value.split( Pattern.quote( MULTILINE_VALUES_SEPARATOR ) );
          
          //
          List<String> valueList = property.getValueList();
          valueList.clear();
          valueList.addAll( Arrays.asList( valueTokens ) );
          
          //
          propertyFileAndProperty.getPropertyFile().store();
        }
      }
    }
  }
  
  public String resolvePropertyValue( String propertyKey, String groupToken )
  {
    //
    String retval = null;
    
    //
    if ( propertyKey != null && groupToken != null )
    {
      //
      PropertyFileAndProperty propertyFileAndProperty = this.resolvePropertyFileAndProperty( propertyKey, groupToken );
      if ( propertyFileAndProperty != null )
      {
        //
        Property property = propertyFileAndProperty.getProperty();
        if ( property != null )
        {
          //
          retval = StringUtils.join( property.getValueList(), MULTILINE_VALUES_SEPARATOR );
        }
      }
    }
    
    //
    return retval;
  }
  
  /**
   * Determines the tokens of the {@link FileGroup}.
   * 
   * @return
   */
  public List<String> determineGroupTokenList()
  {
    return new ArrayList<String>( this.fileGroup.getGroupTokenToFileMap().keySet() );
  }
  
  /**
   * Determines all property keys which are occurring at least in one of the files of the {@link FileGroup}.
   * 
   * @return
   */
  public Set<String> determinePropertyKeySet()
  {
    //
    Set<String> retset = new HashSet<String>();
    
    //
    for ( String groupToken : this.determineGroupTokenList() )
    {
      //
      PropertyFile propertyFile = this.resolveAndLoadPropertyFile( groupToken );
      if ( propertyFile != null )
      {
        //
        PropertyFileContent propertyFileContent = propertyFile.getPropertyFileContent();
        PropertyMap propertyMap = propertyFileContent.getPropertyMap();
        
        //
        retset.addAll( propertyMap.keySet() );
      }
    }
    
    //
    return retset;
  }
  
  public void setFileEncoding( String fileEncoding )
  {
    this.fileEncoding = fileEncoding;
  }
}
