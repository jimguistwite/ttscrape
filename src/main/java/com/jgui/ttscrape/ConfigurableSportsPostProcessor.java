/*
 * Copyright 2012 Jim Guistwite
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgui.ttscrape;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

/**
 * The <code>ConfigurableSportsPostProcessor</code> processes
 * shows writing those to the show write that match the contains
 * list provided.
 * 
 * @author jguistwite
 */

public class ConfigurableSportsPostProcessor implements ShowPostProcessor {
  
  private static final String WRITER_NAME = "sports";
  
  @Resource
  private ShowWriter writer;

  private String category;
  
  private List<String> contains;
  
  @Override
  public void beginSequence() {
    writer.establishWriter(WRITER_NAME, true);
  }

  @Override
  public void endSequence() {
    writer.closeWriter(WRITER_NAME);
  }

  /**
   * Write the show to the show writer if it matches.
   */
  @Override
  public void postProcess(Show s) {
    // make sure the category matches.
    if (StringUtils.equals(category, s.getCategory())) {
      String subtitle = s.getSubtitle();
      
      // for each entry in the contains list,
      // see if the subtitle contains the string
      // (or doesn't if first char is bang char)
      boolean writeIt = false;
      for(String str: contains) {
        if (str.charAt(0) == '!') {
          String s1 = str.substring(1);
          if (StringUtils.contains(subtitle, s1)) {
            writeIt = false;
            break;
          }
        }
        else {
          if (StringUtils.contains(subtitle, str)) {
            writeIt = true;
          }
        }
      }
      if (writeIt) {
        writer.writeShow(WRITER_NAME, s);
      }
    }
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public List<String> getContains() {
    return contains;
  }

  public void setContains(List<String> contains) {
    this.contains = contains;
  }

}
