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

package com.jgui.ttscrape.htmlunit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jgui.ttscrape.ClientNotifier;
import com.jgui.ttscrape.Show;
import com.jgui.ttscrape.ShowFilter;
import com.jgui.ttscrape.ShowPostProcessor;

/**
 * The <code>FetchController</code> class controls overall fetching and IO of shows.
 */

@Component
public class FetchController {
  private Logger logger = LoggerFactory.getLogger(FetchController.class);

  private double progress;
  private String mode;

  @Resource
  private ClientNotifier notifier;

  @Resource
  private TitanTvPageFetcher fetcher;

  private boolean writeShows;
  private boolean writeContent = false;

  @Autowired
  private List<ShowFilter> filters;

  @Autowired
  private List<ShowPostProcessor> postProcessors;

  public FetchController() {

  }

  @PostConstruct
  public void init() {
    mode = "Initialization";
  }

  /**
   * Read the previously loaded results.
   */
  public void read() {
    try {
      int total = 28;
      mode = "Read";

      ArrayList<Show> shows = new ArrayList<Show>();
      for (int i = 1; i <= total; i++) {
        File f = new File("results/shows" + i + ".txt");
        if (f.exists()) {
          FileReader showreader = new FileReader(f);
          List<String> lines = IOUtils.readLines(showreader);
          for (String s : lines) {
            Show show = Show.fromString(s);
            shows.add(show);
          }
        }
        progress = (double) i / (double) total;
        notifier.notifyClient("progress", "value", String.valueOf((int) (100.0 * ((double) i) / 28.0)), "mode", mode);
      }
      Collections.sort(shows, new Comparator<Show>() {
        @Override
        public int compare(Show arg0, Show arg1) {
          return arg0.getStartTime().compareTo(arg1.getStartTime());
        }
      });

      processShows(shows);
    }
    catch (IOException ex) {
      logger.error("failed to read", ex);
    }
  }

  public void fetch() {
    logger.debug("Fetch controller running");
    fetcher.setWriteContent(writeContent);
    
    try {
      if (postProcessors != null) {
        for (ShowPostProcessor spp : postProcessors) {
          spp.beginSequence();
        }
      }
      if (filters != null) {
        for (ShowFilter filter : filters) {
          filter.beginSequence();
        }
      }
      TitanTvPageParser parser = new TitanTvPageParser();

      int total = 28;
      progress = 1.0 / total;
      mode = "Fetch";
      notifier.notifyClient("progress", "value", String.valueOf((int) (100 * progress)), "mode", mode);

      // re-init.
      fetcher.init();
      
      HtmlPage currentPage = fetcher.login();
      if (currentPage == null) {
        logger.error("TT login failed");
        return;
      }
      if (isWriteContent()) {
        writeContent(1, currentPage);
      }
      mode = "Parse";
      notifier.notifyClient("progress", "value", String.valueOf((int) (100 * progress)), "mode", mode);
      List<Show> shows = parser.parsePage(currentPage);
      if (isWriteShows()) {
        writeShows(shows, "shows1.txt");
      }
      processShows(shows);

      // As of 11/21/09, this works. 'Pressing' the next button
      // updates the dom and the login results page now
      // includes the next set of listings.

      // At 4 fetches per day (6 hours each), we should do 28
      // fetches to look out a full week ahead.
      int start = 2;
      int end = 28;
      for (int i = start; i <= end; i++) {
        progress = (double) i / (double) total;
        mode = "Fetch";
        notifier.notifyClient("progress", "value", String.valueOf((int) (100 * progress)), "mode", mode);
        currentPage = fetcher.next(currentPage);
        if (isWriteContent()) {
          writeContent(i, currentPage);
        }
        mode = "Parse";
        notifier.notifyClient("progress", "value", String.valueOf((int) (100 * progress)), "mode", mode);
        shows = parser.parsePage(currentPage);
        if (isWriteShows()) {
          writeShows(shows, "shows" + i + ".txt");
        }
        processShows(shows);
      }
      notifier.notifyClient("progress", "value", String.valueOf(100), "mode", "Idle");
    }
    catch (Exception ex) {
      logger.error("failed fetch", ex);
    }
    finally {
      if (postProcessors != null) {
        for (ShowPostProcessor spp : postProcessors) {
          spp.endSequence();
        }
      }
      if (filters != null) {
        for (ShowFilter filter : filters) {
          filter.endSequence();
        }
      }
    }
  }

  private void writeContent(int i, HtmlPage currentPage) {
    try {
      FileWriter writer = new FileWriter("results/raw" + i + ".xml");
      writer.write(currentPage.asXml());
      writer.close();
    }
    catch (Exception ex) {
      logger.error("Failed to write raw content", ex);
    }
  }

  public void processShows(List<Show> shows) {
    for (Show s : shows) {
      boolean keeper = true;
      for (ShowFilter f : filters) {
        if (f.exclude(s)) {
          keeper = false;
          break;
        }
      }
      if (keeper) {
        for (ShowPostProcessor pp : postProcessors) {
          pp.postProcess(s);
        }
      }
    }
  }

  public TitanTvPageFetcher getFetcher() {
    return fetcher;
  }

  public void setFetcher(TitanTvPageFetcher fetcher) {
    this.fetcher = fetcher;
  }

  protected void writeShows(List<Show> shows, String filename) throws IOException {
    if (shows != null) {
      FileWriter showwriter = new FileWriter("results/" + filename);
      for (Show s : shows) {
        showwriter.write(s.toString());// s.format());
        showwriter.write("\n");
      }
      showwriter.close();
    }
  }

  public List<ShowPostProcessor> getPostProcessors() {
    return postProcessors;
  }

  public void setPostProcessors(List<ShowPostProcessor> postProcessors) {
    this.postProcessors = postProcessors;
  }

  public boolean isWriteShows() {
    return writeShows;
  }

  public void setWriteShows(boolean writeShows) {
    this.writeShows = writeShows;
  }

  public double getProgress() {
    return progress;
  }

  public int getProgressAsPercent() {
    return (int) (progress * 100.0);
  }

  public void setProgress(double progress) {
    this.progress = progress;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public List<ShowFilter> getFilters() {
    return filters;
  }

  public void setFilters(List<ShowFilter> filters) {
    this.filters = filters;
  }

  public boolean isWriteContent() {
    return writeContent;
  }

  public void setWriteContent(boolean writeContent) {
    this.writeContent = writeContent;
  }

}
