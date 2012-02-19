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

package com.jgui.ttscrape.webclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jgui.ttscrape.Show;
import com.jgui.ttscrape.ShowWriter;
import com.jgui.ttscrape.TextualShowWriter;

/**
 * The <code>WebShowWriter</code> collects shows for
 * display via the html client.
 * 
 * @author jguistwite
 */

public class WebShowWriter implements ShowWriter {

  private Map<Integer,Show> showMap;
  private Map<String,ArrayList<Show>> showListMap;
  private TextualShowWriter other;
  
  public WebShowWriter() {
    showListMap = new HashMap<String,ArrayList<Show>>();
    showMap = new HashMap<Integer,Show>();
    other = new TextualShowWriter();
  }
  
  @Override
  public void establishWriter(String writerName, boolean includeSubtitle) {
    ArrayList<Show> lst = new ArrayList<Show>();
    showListMap.put(writerName, lst);
    other.establishWriter(writerName, includeSubtitle);
  }

  @Override
  public void writeShow(String writerName, Show s) {
    if (!showListMap.containsKey(writerName)) {
      establishWriter(writerName, false);
    }
    showListMap.get(writerName).add(s);
    other.writeShow(writerName, s);
    showMap.put(s.getId(), s);
  }

  public Map<Integer, Show> getShowMap() {
    return showMap;
  }
  
  @Override
  public void closeWriter(String writerName) {
    other.closeWriter(writerName);
  }

  public Map<String, ArrayList<Show>> getShowListMap() {
    return showListMap;
  }

  public void setShowListMap(Map<String, ArrayList<Show>> showListMap) {
    this.showListMap = showListMap;
  }

}

