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
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>IgnoreListFilter</code> kicks out shows on the ignore list.
 * 
 * @author jguistwite
 */

public class IgnoreListFilter implements ShowFilter {
  private Logger logger = LoggerFactory.getLogger(IgnoreListFilter.class);

  private List<String> fileHeader;
  private List<String> titlesToIgnore;
  private File ignoreFile;

  @PostConstruct
  public void init() {
    try {
      fileHeader = new ArrayList<String>();
      titlesToIgnore = new ArrayList<String>();
      boolean headerComplete = false;
      for(String title: IOUtils.readLines(new FileReader(ignoreFile))) {
        if ((title.length() != 0) && (title.charAt(0) != '#')) {
          titlesToIgnore.add(title.trim());
          headerComplete = true;
        }
        else if (!headerComplete) {
          fileHeader.add(title);
        }
      }
    }
    catch (Exception ex) {
      logger.error("failed to read titles to ignore list", ex);
    }
  }

  @Override
  public boolean exclude(Show s) {
    return titlesToIgnore.contains(s.getTitle());
  }

  @Override
  public void beginSequence() {
    // nothing to do.
  }

  @Override
  public void endSequence() {
    // nothing to do.
  }

  /**
   * Called by the web controller, this adds a title to the ignore list and
   * persists the list.
   * 
   * @param title
   *          title to be ignored.
   */
  public void addTitleToIgnore(String title) {
    if (!titlesToIgnore.contains(title)) {
      titlesToIgnore.add(title);
      writeIgnoreFile();
    }
  }

  /**
   * Sort then write the file to disk.
   */
  private void writeIgnoreFile() {
    try {
      Collections.sort(titlesToIgnore);
      FileWriter writer = new FileWriter(ignoreFile);
      IOUtils.writeLines(fileHeader, null, writer);
      IOUtils.writeLines(titlesToIgnore, null, writer);
      writer.close();
    }
    catch (Exception ex) {
      logger.error("failed to write ignore file", ex);
    }
  }

  /**
   * Get the file to read and write the ignore list to/from. 
   * @return file previously assigned to this bean.
   */
  public File getIgnoreFile() {
    return ignoreFile;
  }

  /**
   * Set the file to read and write the ignore list from/to.
   * @param ignoreFile file instance
   */
  public void setIgnoreFile(File ignoreFile) {
    this.ignoreFile = ignoreFile;
  }
}
