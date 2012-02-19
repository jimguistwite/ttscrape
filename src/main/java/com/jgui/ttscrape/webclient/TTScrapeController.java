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
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jgui.ttscrape.IgnoreListFilter;
import com.jgui.ttscrape.Show;
import com.jgui.ttscrape.htmlunit.FetchController;

/**
 * The <code>TTScrapeController</code> 
 * 
 * @author jguistwite
 */

public class TTScrapeController {
  Logger logger = LoggerFactory.getLogger(TTScrapeController.class);
  
  @Resource
  private WebShowWriter writer;
  
  @Resource
  private IgnoreListFilter ignoreListFilter;
  
  @Resource
  private FetchController fetchController;
  
  private int port;
  
  @RequestMapping("status")
  public String status(HttpServletRequest req, HttpServletResponse rsp, VelocityContext ctx) {
    Map<String,ArrayList<Show>> map = writer.getShowListMap();

    ctx.put("showListMap", map);
    ctx.put("port", port);
    ctx.put("controller", fetchController);
    /*
    for (String beanName : beans) {
      Object bean = beanFactory.getBean(beanName);
      ctx.put(beanName, bean);
    }
    */
    return "status.vhtml";
  }

  @RequestMapping("ignore")
  public JSONObject ignore(HttpServletRequest req, HttpServletResponse rsp) throws JSONException {
    JSONObject rv = null;
    logger.debug("ignore request received");

    String showId = req.getParameter("id");
    Show s = writer.getShowMap().get(Integer.parseInt(showId));
    if (s == null) {
      logger.error("failed to find show with id {}", showId);
      rv = createJsonResponse(rsp, "error", "Show not found.");
    }
    else {
      ignoreListFilter.addTitleToIgnore(s.getTitle());
      rv = createJsonResponse(rsp, "success", "");
    }
    
    return rv;
  }

  @RequestMapping("runRead")
  public JSONObject runread(HttpServletRequest req, HttpServletResponse rsp) throws JSONException {
    Thread t = new Thread(new Runnable() {
      public void run() {
        fetchController.read();
      }
    });
    t.start();
    return createJsonResponse(rsp, "success", "");
  }

  @RequestMapping("runFetch")
  public JSONObject runfetch(HttpServletRequest req, HttpServletResponse rsp) throws JSONException {
    Thread t = new Thread(new Runnable() {
      public void run() {
        fetchController.fetch();
      }
    });
    t.start();
    return createJsonResponse(rsp, "success", "");
  }

  private JSONObject createJsonResponse(HttpServletResponse rsp, String status, String error) throws JSONException {
    JSONObject jsonRsp = new JSONObject();
    jsonRsp.put("status", status);
    jsonRsp.put("error", error);
    return jsonRsp;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }
}

