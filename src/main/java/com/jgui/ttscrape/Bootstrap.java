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

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.jgui.ttscrape.htmlunit.FetchController;

/**
 * The <code>Bootstrap</code> class initializes the spring container
 * and either reads the previously loaded shows, fetches a new version
 * of the shows or waits for user input from the web client.
 * 
 * @author Jim Guistwite
 * 
 */
public class Bootstrap {
  private Logger logger = LoggerFactory.getLogger(Bootstrap.class);

  private boolean readMode = false;
  private boolean fetchMode = false;

  /**
   * Look for "read" or "fetch" on the command line. 
   */
  public static void main(String[] args) {
    Bootstrap b = new Bootstrap();
    if (args.length != 0) {
      b.readMode = "read".equalsIgnoreCase(args[0]);
      b.fetchMode = "fetch".equalsIgnoreCase(args[0]);
    }
    b.run();
  }

  public Bootstrap() {
  }

  public void run() {
    if (logger.isTraceEnabled()) {
      logger.trace("initializing container");
    }
    
    URL url = Bootstrap.class.getResource("/applicationContext.xml");
    ApplicationContext ctx = new FileSystemXmlApplicationContext(url.toExternalForm());
    FetchController c = ctx.getBean(FetchController.class);
    if (c != null) {
      //c.setWriteShows(true);
      //c.setWriteContent(true);
      if (c.getFetcher().init()) {
        if (readMode) {
          c.read();
        }
        else if (fetchMode) {
          c.fetch();
        }
      }
    }
    else {
      logger.error("unable to find fetch controller");
    }
  }
}
