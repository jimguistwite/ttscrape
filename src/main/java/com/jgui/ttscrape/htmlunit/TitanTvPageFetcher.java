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

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class TitanTvPageFetcher {
  private Logger logger = LoggerFactory.getLogger(TitanTvPageFetcher.class);
  private WebClient webClient;
  private boolean writeContent;

  private String login;
  private String password;

  public TitanTvPageFetcher() {

  }

  /**
   * Login to the site and hang onto that initial page.
   */
  public boolean init() {
    boolean rv = true;

    Cache cache = new Cache();
    webClient = new WebClient(BrowserVersion.FIREFOX_8);
    webClient.setThrowExceptionOnScriptError(false);
    webClient.setCache(cache);

    CookieManager cm = new CookieManager();
    webClient.setCookieManager(cm);

    final SilentCssErrorHandler eh = new SilentCssErrorHandler();
    webClient.setCssErrorHandler(eh);
    webClient.setAjaxController(new NicelyResynchronizingAjaxController());
   
    // extended version of htmlunit that allows certain URLs to be blocked.
    try {
      Method m = webClient.getClass().getMethod("addBlockedScript", URL.class);
      m.invoke(webClient, new URL("http://setjam.com/static/widget/v1/compressed/all.js"));
      m.invoke(webClient, new URL("http://www.google-analytics.com"));
      m.invoke(webClient, new URL("http://pixel.quantserve.com"));
      m.invoke(webClient, new URL("http://serve.directdigitalllc.com/serve.php"));
      m.invoke(webClient, new URL("http://ad.doubleclick.net"));
      m.invoke(webClient, new URL("http://d7.zedo.com"));
      m.invoke(webClient, new URL("http://d3.zedo.com"));
      m.invoke(webClient, new URL("http://pagead2.googlesyndication.com"));
      m.invoke(webClient, new URL("http://s0.2mdn.net"));
      m.invoke(webClient, new URL("http://edge.quantserve.com"));
      m.invoke(webClient, new URL("http://ad.doubleclick.net"));
    }
    catch (NoSuchMethodException ex) {
      logger.warn("no javascript blocking installed due to htmlunit version");
    }
    catch (Exception ex) {
      logger.error("failed", ex);
    }

    return rv;
  }

  public HtmlPage login() {
    HtmlPage page;
    try {
      logger.debug("fetching login.aspx page");
      WebRequest req = new WebRequest(new URL("http://ww2.titantv.com/account/login.aspx"));
      req.setAdditionalHeader("Accept-Encoding", "gzip, deflate");
      page = webClient.getPage(req);
      if (writeContent) {
        FileWriter writer = new FileWriter("login.xml");
        writer.write(page.asXml());
        writer.close();
      }
      logger.debug("done fetching login.aspx page");

      HtmlForm form = page.getFormByName("aspnetForm");
      HtmlInput usernameField = form.getInputByName("ctl00$ctl00$Main$content$li$ctl00$UserName");
      HtmlInput passwordField = form.getInputByName("ctl00$ctl00$Main$content$li$ctl00$Password");
      usernameField.setValueAttribute(login);
      passwordField.setValueAttribute(password);

      HtmlSubmitInput button = form.getInputByName("ctl00$ctl00$Main$content$li$ctl00$LoginButton");

      logger.debug("posting login.aspx page");
      HtmlPage loginResultPage = button.click();
      if (writeContent) {
        FileWriter writer = new FileWriter("loginresults.xml");
        writer.write(loginResultPage.asXml());
        writer.close();
      }
      // logger.debug("done posting login.aspx page into loginresults.xml");
      return loginResultPage;
    }
    catch (FailingHttpStatusCodeException e) {
      logger.error("initial login post failed", e);
    }
    catch (MalformedURLException e) {
      logger.error("malformed url??", e);
    }
    catch (IOException e) {
      logger.error("ioexception on login request", e);
    }
    return null;
  }

  public HtmlPage next(HtmlPage current) throws IOException {
    HtmlPage rv = null;

    if (current == null) {
      logger.error("current is null");
      return null;
    }

    HtmlElement grid = current.getElementById("ctl00_Main_TVL_ctl00_Grid");

    // long now = System.currentTimeMillis();
    List<HtmlElement> navCells = grid.getElementsByAttribute("td", "class", "gridHeaderNavRightCell");
    if ((navCells != null) && (navCells.size() != 0)) {
      long newnow = System.currentTimeMillis();
      // logger.debug("time to find nav button with getElements is {}", (newnow
      // - now));
      HtmlElement cell = navCells.get(0);
      //logger.debug("clicking on {} ", cell.asXml());
      cell.click();
      long after = System.currentTimeMillis();
      logger.debug("time to fetch next results {}", (double) (after - newnow) / 1000d);

      // click can return content from a different window, so ignore the result
      // instead, loop though and find the top window which should be the original
      List<WebWindow> windows = webClient.getWebWindows();
      for(WebWindow ww: windows) {
        if (ww instanceof TopLevelWindow) {
          rv = (HtmlPage)ww.getEnclosedPage();
          logger.debug("using window page with URL: {}", ww.getEnclosedPage().getUrl());
        }
      }

      if (writeContent) {
        FileWriter writer = new FileWriter("results.xml");
        writer.write(rv.asXml());
        writer.close();
      }

      //re-get the grid element
      grid = rv.getElementById("ctl00_Main_TVL_ctl00_Grid");
      List<HtmlElement> ghcNow = grid.getElementsByAttribute("span", "class", "ghcNow");
      if ((ghcNow != null) && (ghcNow.size() != 0)) {
        logger.debug("grid time is now {}", ghcNow.get(0).asText());
      }
      
    }

    return rv;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isWriteContent() {
    return writeContent;
  }

  public void setWriteContent(boolean writeContent) {
    this.writeContent = writeContent;
  }
}
