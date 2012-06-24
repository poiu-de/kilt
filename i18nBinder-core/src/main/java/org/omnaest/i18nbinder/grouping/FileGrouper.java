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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omnaest.utils.pattern.MatchResultGroupReplacer;

/**
 * This class supports the grouping of {@link File} instances by their file names. <br>
 * <br>
 * The grouping is tried to be done to as many defined regular expression groups as possible. This implies the groups have the
 * same value. If the values are different only the first group is replaced.
 * 
 * @see #determineFileGroupList()
 * @author Omnaest
 */
public class FileGrouper
{
  /* ********************************************** Constants ********************************************** */
  private final String    GROUPING_PATTERN_DEFAULT_STRING                 = "(.*?_(\\w{2,3}_\\w{2,3}|\\w{2,3})|.*())\\.\\w*";
  private final Integer[] GROUPING_PATTERN_GROUPING_GROUP_INDEXES_DEFAULT = { 2, 3 };
  protected String        GROUPING_REPLACEMENT_TOKEN_DEFAULT              = "{group}";
  
  /* ********************************************** Variables ********************************************** */
  protected List<File>    fileList                                        = new ArrayList<File>();
  
  protected Pattern       groupingPattern                                 = Pattern.compile( GROUPING_PATTERN_DEFAULT_STRING );
  protected List<Integer> groupingPatternGroupingGroupIndexList           = Arrays.asList( GROUPING_PATTERN_GROUPING_GROUP_INDEXES_DEFAULT );
  
  protected String        groupingPatternReplacementToken                 = GROUPING_REPLACEMENT_TOKEN_DEFAULT;
  
  /* ********************************************** Classes/Interfaces ********************************************** */

  /**
   * Determines a {@link Map} with the file group identifier as key and the {@link FileGroup} instances as values. This grouping
   * is based on an investigation in the absolute file names. If they match the given grouping pattern the first group of the
   * pattern is replaced by an general token. If the resulting string from multiple files match now, they are grouped together.<br>
   * <br>
   * For example the files
   * <ul>
   * <li>admin_de_DE.properties</li>
   * <li>admin_en_US.properties</li>
   * </ul>
   * and the grouping pattern ".*?_(\\w{2,3}_\\w{2,3}|\\w{2,3})\\.\\w*" will result in a group called "admin_{group}.properties"
   * which holds references to these two files.
   * 
   * @see FileGrouper#setGroupingPatternString(String)
   * @see FileGroup
   */
  public Map<String, FileGroup> determineFileGroupIdentifierToFileGroupMap()
  {
    //
    Map<String, FileGroup> retmap = new HashMap<String, FileGroup>();
    
    //
    for ( File file : this.fileList )
    {
      //
      String absolutePath = file.getAbsolutePath();
      
      //
      Matcher matcher = this.groupingPattern.matcher( absolutePath );
      if ( matcher.matches() )
      {
        //
        List<Integer> groupingPatternGroupIndexMatchingList = new ArrayList<Integer>();
        String groupValueFirstMatching = null;
        {
          //
          for ( Integer iGroupingPatternGroupingGroupIndex : this.groupingPatternGroupingGroupIndexList )
          {
            // 
            if ( matcher.groupCount() >= iGroupingPatternGroupingGroupIndex.intValue() )
            {
              //
              String groupValue = matcher.group( iGroupingPatternGroupingGroupIndex );
              if ( groupValue != null && ( groupValueFirstMatching == null || groupValueFirstMatching.equals( groupValue ) ) )
              {
                groupingPatternGroupIndexMatchingList.add( iGroupingPatternGroupingGroupIndex );
                groupValueFirstMatching = groupValue;
              }
            }
          }
        }
        
        //
        if ( groupValueFirstMatching != null )
        {
          //
          MatchResultGroupReplacer matchResultGroupReplacer = new MatchResultGroupReplacer( matcher.toMatchResult() );
          Map<Integer, String> groupIndexToNewValueMap = new HashMap<Integer, String>();
          {
            for ( Integer groupIndex : groupingPatternGroupIndexMatchingList )
            {
              groupIndexToNewValueMap.put( groupIndex, this.groupingPatternReplacementToken );
            }
          }
          
          //
          String fileGroupIdentifier = matchResultGroupReplacer.replaceGroups( groupIndexToNewValueMap );
          String groupToken = groupValueFirstMatching;
          
          //                 
          if ( !retmap.containsKey( fileGroupIdentifier ) )
          {
            //
            FileGroup fileGroup = new FileGroup();
            fileGroup.setFileGroupIdentifier( fileGroupIdentifier );
            retmap.put( fileGroupIdentifier, fileGroup );
          }
          
          //
          {
            //
            FileGroup fileGroup = retmap.get( fileGroupIdentifier );
            
            //
            Map<String, File> groupTokenToFileMap = fileGroup.getGroupTokenToFileMap();
            groupTokenToFileMap.put( groupToken, file );
          }
          
        }
      }
    }
    
    //
    return retmap;
  }
  
  /**
   * Adds the given {@link File} to the {@link FileGrouper}.
   * 
   * @param e
   * @return
   */
  public boolean addFile( File e )
  {
    return this.fileList.add( e );
  }
  
  /**
   * Adds all given {@link File} instances to the {@link FileGrouper}.
   * 
   * @param fileCollection
   * @return
   */
  public boolean addAllFiles( Collection<? extends File> fileCollection )
  {
    return this.fileList.addAll( fileCollection );
  }
  
  /**
   * Clears the {@link File}s from the {@link FileGrouper}.
   */
  public void clearFiles()
  {
    this.fileList.clear();
  }
  
  public boolean containsFile( File file )
  {
    return this.fileList.contains( file );
  }
  
  /**
   * Removes the given {@link File} from the {@link FileGrouper}.
   * 
   * @param file
   * @return
   */
  public boolean remove( File file )
  {
    return this.fileList.remove( file );
  }
  
  /**
   * Removes all given {@link File} instances from the {@link FileGrouper}.
   * 
   * @param fileCollection
   * @return
   */
  public boolean removeAll( Collection<File> fileCollection )
  {
    return this.fileList.removeAll( fileCollection );
  }
  
  /**
   * Returns the number of files.
   * 
   * @return
   */
  public int size()
  {
    return this.fileList.size();
  }
  
  public String getGroupingPatternString()
  {
    return this.groupingPattern.pattern();
  }
  
  public void setGroupingPatternString( String groupingPatternString ) throws Exception
  {
    if ( groupingPatternString != null )
    {
      this.groupingPattern = Pattern.compile( groupingPatternString );
    }
  }
  
  public String getGroupingPatternReplacementToken()
  {
    return this.groupingPatternReplacementToken;
  }
  
  public void setGroupingPatternReplacementToken( String groupingPatternReplacementToken )
  {
    this.groupingPatternReplacementToken = groupingPatternReplacementToken;
  }
  
  public List<Integer> getGroupingPatternGroupingGroupIndexList()
  {
    return this.groupingPatternGroupingGroupIndexList;
  }
  
  public void setGroupingPatternGroupingGroupIndexList( List<Integer> groupingPatternGroupingGroupIndexList )
  {
    this.groupingPatternGroupingGroupIndexList = groupingPatternGroupingGroupIndexList;
  }
  
}
