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
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>TextualShowWriter</code>
 * 
 * @author jguistwite
 */

public class TextualShowWriter implements ShowWriter {

  private Logger logger = LoggerFactory.getLogger(TextualShowWriter.class);

  private HashMap<String, Writer> writers;

  class Writer {
    public FileWriter writer;
    public boolean includeSubtitle;

    public Writer(String name, boolean includeSubtitle) throws IOException {
      File f = new File("results/" + name + ".txt");
      f.getParentFile().mkdirs();
      writer = new FileWriter(f);
      this.includeSubtitle = includeSubtitle;
    }
  }

  public TextualShowWriter() {
    writers = new HashMap<String, Writer>();
  }

  @Override
  public void establishWriter(String writerName, boolean includeSubtitle) {
    try {
      Writer writer = new Writer(writerName, includeSubtitle);
      writers.put(writerName, writer);
    }
    catch (IOException ex) {
      logger.error("failed to establish writer", ex);
    }
  }

  @Override
  public void closeWriter(String writerName) {
    try {
      Writer w = writers.get(writerName);
      if (w != null) {
        w.writer.close();
      }
    }
    catch (IOException ex) {

    }
  }

  @Override
  public void writeShow(String writerName, Show s) {
    try {
      Writer w = writers.get(writerName);
      if (w == null) {
        logger.error("writer {} not found", writerName);
      }
      else {
        StringBuffer sb = new StringBuffer();
        int titlePad = 35;
        String str = s.getTitle();
        if (s.getStars() > 0) {
          str = str + " (" + s.getStars() + ")";
        }
        if (w.includeSubtitle && (s.getSubtitle() != null)) {
          str = str + " " + s.getSubtitle();
          titlePad += 10;
        }
        sb.append(StringUtils.rightPad(str, titlePad));
        sb.append("\t");
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        sb.append(StringUtils.rightPad(df.format(s.getStartTime()), 20));
        sb.append("\t");
        sb.append(s.getChannelName() + " " + s.getChannelNumber());
        w.writer.write(sb.toString());
        w.writer.write("\n");
        if (w.includeSubtitle) {
          // write an extra newline to separate
          w.writer.write("\n");
        }
        w.writer.flush();
      }
    }
    catch (IOException ex) {
      logger.error("failed to write", ex);
    }
  }

}
