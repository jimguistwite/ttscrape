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

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>LookOutFor</code> class examines shows to find
 * those with a title that matches a file containing shows to look for. 
 * 
 * @author jguistwite
 */

public class LookOutFor implements ShowPostProcessor {
	private Logger logger = LoggerFactory.getLogger(LookOutFor.class);

	private static final String WRITER_NAME = "lookout";
  
  @Resource
  private ShowWriter writer;
	
  private File titlesToFindFile;
  
  private List<String> titlesToFind;
  
  @PostConstruct
  public void init() {
    
    try {
      titlesToFind = new ArrayList<String>();
      for(String title:IOUtils.readLines(new FileReader(titlesToFindFile))) {
        if ((title.length() != 0) && (title.charAt(0) != '#')) {
          titlesToFind.add(title.trim());
        }
      }
    }
    catch (Exception ex) {
      logger.error("failed to read titles to ignore list", ex);
    }
  }
  
	
	@Override
	public void beginSequence() {
    writer.establishWriter(WRITER_NAME, true);	  
	}

	@Override
	public void endSequence() {
	  writer.closeWriter(WRITER_NAME);
	}

	@Override
	public void postProcess(Show s) {
	  for(String title: titlesToFind) {
	    boolean writeIt = false;
	    if (title.startsWith("contains:")) {
	      writeIt = (s.getTitle().contains(title.substring(9)));
	    }
	    else {
	      writeIt = title.equalsIgnoreCase(s.getTitle());
	    }
	    if (writeIt) {
	      writer.writeShow(WRITER_NAME, s);
	    }
	  }
	}

  public File getTitlesToFindFile() {
    return titlesToFindFile;
  }


  public void setTitlesToFindFile(File titlesToFindFile) {
    this.titlesToFindFile = titlesToFindFile;
  }

}
