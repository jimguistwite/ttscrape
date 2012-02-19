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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.jgui.ttscrape.Show;

/**
 * The <code>GridParser</code> class parses a table of listings. It's been
 * stable for since late 2009 but is subject to major changes when the
 * titantv.com site is overhauled. Note: the Chrome browser developer tools aids
 * greatly when inspecting the DOM.
 * 
 * @author jguistwite
 */

public class GridParser {

  private Logger logger = LoggerFactory.getLogger(GridParser.class);

  private static Map<String, String> categoryMap = new HashMap<String, String>();

  static {
    // category is the 2nd part of the table cell class
    categoryMap.put("c1b", "Action");
    categoryMap.put("c2b", "Children");
    categoryMap.put("c3b", "Comedy");
    categoryMap.put("c4b", "Drama");
    categoryMap.put("c5b", "Documentary");
    categoryMap.put("c6b", "Educational");
    categoryMap.put("c7b", "Game");
    categoryMap.put("c8b", "How-To");
    categoryMap.put("c9b", "Music");
    categoryMap.put("c10b", "Nature");
    categoryMap.put("c11b", "News");
    categoryMap.put("c12b", "Reality");
    categoryMap.put("c13b", "Religion");
    categoryMap.put("c14b", "Soap");
    categoryMap.put("c15b", "SciFi");
    categoryMap.put("c16b", "Special");
    categoryMap.put("c17b", "Sports");
    categoryMap.put("c18b", "Talk");
    categoryMap.put("c19b", "Travel");

    categoryMap.put("cb", "Other");
  }

  private static ArrayList<String> knownRatings = new ArrayList<String>();
  static {
    String[] ratings = { "G", "PG", "PG-13", "R", "NR" };
    for (String s : ratings) {
      knownRatings.add(s);
    }
  }

  /**
   * Parse the table of shows.
   * 
   * @param gridStartDate
   *          times on the html page are relative to this date.
   * @param tbl
   *          html markup to be parsed
   * @return collection of shows generated from markup
   */
  public List<Show> parseGrid(Calendar gridStartDate, HtmlTable tbl) {
    ArrayList<Show> shows = new ArrayList<Show>();

    // this first row should be a row with times that sync
    // up with the table cells.
    // use this row to determine the start of the grid.

    Date gridStartTime = null;
    for (HtmlTableRow row : tbl.getRows()) {
      if (gridStartTime == null) {
        gridStartTime = determineGridStartTime(row, gridStartDate);
      }
      String rowClass = row.getAttribute("class");
      if ("gridRow".equals(rowClass)) {
        shows.addAll(parseListingRow(gridStartTime, row));
      }
    }
    return shows;
  }

  /**
   * Parse a row in the table into a sequence of shows.
   * 
   * @param gridStartTime
   *          the start time of the first show in the column.
   * @param row
   *          row to be parsed.
   * @return collection of shows generated for this row.
   */
  private List<Show> parseListingRow(Date gridStartTime, HtmlTableRow row) {
    ArrayList<Show> rv = new ArrayList<Show>();

    try {
      String currentChannel = null;
      String currentChannelName = null;
      // String currentNetwork = null;

      Calendar timeAcrossGrid = Calendar.getInstance();
      timeAcrossGrid.setTime(gridStartTime);

      for (HtmlTableCell cell : row.getCells()) {
        String cellClass = cell.getAttribute("class");

        Show show = null;

        // gLSC class cell is the small cell before the shows start
        // (continuation cell)
        if ("gLSC".equals(cellClass)) {
          continue;
        }

        // gRSC is the blank cell at the end of a row.
        if ("gRSC".equals(cellClass)) {
          continue;
        }

        if ("gridCallSignCell".equals(cellClass)) {
          // extract grid call sign text span cell.
          for (HtmlElement el : cell.getChildElements()) {
            if ("gridCallSignText".equals(el.getAttribute("class"))) {
              currentChannelName = el.asText();
              break;
            }
            // else if ("gridNetworkText".equals(el.getAttribute("class"))) {
            // currentNetwork = el.asText();
            // }
          }
        }
        else if ("gridChannelCell".equals(cellClass)) {
          currentChannel = cell.asText();
        }

        // gELC class cell is a continuation of a previous listing.
        // gEBC class cell is a continuation that fills the entire timespan.
        // cannot compute end time yet
        else if ((cellClass.startsWith("gELC ")) || (cellClass.startsWith("gEBC "))) {
          show = new Show();
          show.setStartTime(timeAcrossGrid.getTime());
          show.setChannelName(currentChannelName);
          show.setChannelNumber(currentChannel);

          // colspan is +1 from the show's remaining time for gELC
          int dur = Integer.parseInt(cell.getAttribute("colspan"));
          timeAcrossGrid.add(Calendar.MINUTE, (dur - 1));
          show.setDuration(dur - 1);

          try {
            // show should have a "Continued from hh:mm a
            NodeList spanList = cell.getElementsByTagName("span");
            for (int j = 0; j < spanList.getLength(); j++) {
              HtmlSpan span = (HtmlSpan) spanList.item(j);
              String title = span.getAttribute("title");
              if ((title != null) && (title.startsWith("Continued from "))) {
                String[] splits = title.split(" ");
                String tm = splits[2];
                String ampm = splits[3];
                Calendar gridStartCal = Calendar.getInstance();
                gridStartCal.setTime(gridStartTime);
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                Date dt = sdf.parse(tm + " " + ampm);
                Calendar cal2 = Calendar.getInstance();
                cal2.setTime(dt);

                Calendar cal4 = Calendar.getInstance();
                cal4.setTimeInMillis(gridStartCal.getTimeInMillis());
                cal4.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
                cal4.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));

                // cal4 represents the time from the continued from except it
                // might be in the future
                // since it doesn't take into account the day boundary. Deal
                // with that.
                long diff;
                if (cal4.after(gridStartCal)) {
                  // must have cross the day - show continues from previous day.
                  diff = cal4.getTimeInMillis() - gridStartCal.getTimeInMillis();
                }
                else {
                  diff = gridStartCal.getTimeInMillis() - cal4.getTimeInMillis();
                }
                Calendar cal3 = Calendar.getInstance();
                cal3.setTime(show.getStartTime());
                cal3.add(Calendar.MILLISECOND, (int) (diff * -1));
                show.setStartTime(cal3.getTime());

                show.setDuration(show.getDuration() + (int) (diff / (1000 * 60)));
              }
            }
          }
          catch (Exception ex) {
            logger.error("failed to parse continuation", ex);
          }
        }

        // a gERC cell continues into the next time period so it's
        // colspan is one larger than the duration we're aware of.

        else if ((cellClass.startsWith("gC ")) || (cellClass.startsWith("gERC "))) {
          show = new Show();
          show.setStartTime(timeAcrossGrid.getTime());
          show.setChannelName(currentChannelName);
          show.setChannelNumber(currentChannel);

          int dur = Integer.parseInt(cell.getAttribute("colspan"));
          if (cellClass.startsWith("gC ")) {
            timeAcrossGrid.add(Calendar.MINUTE, (dur));
            show.setDuration(dur);
          }
          else {
            // must be gERC so subtract from the duration
            timeAcrossGrid.add(Calendar.MINUTE, (dur - 1));
            show.setDuration(dur - 1);
          }
        }

        // at this point, the show variable should be assigned
        // a value if we have a show to process. If null,
        // we can continue to the next cell.

        if (show == null) {
          continue;
        }

        parseShowContent(show, cell);

        try {
          String[] classItems = cellClass.split(" ");
          String cat = categoryMap.get(classItems[1]);
          if (cat == null) {
            cat = "Other";
          }
          show.setCategory(cat);
        }
        catch (Exception ex) {
          logger.error("could not get category from class", ex);
        }

        String data = cell.getAttribute("data");
        if (data != null) {
          int idx1 = data.indexOf("EID:");
          if (idx1 > 0) {
            String eid;
            int idx2 = data.indexOf(",", idx1);
            if (idx2 < 0) {
              eid = data.substring(idx1 + 4);
            }
            else {
              eid = data.substring(idx1 + 4, idx2);
            }
            show.setDetailKey(Integer.parseInt(eid));
            show.setId(Integer.parseInt(eid));
          }
        }

        if (isValid(show)) {
          rv.add(show);
        }
      }

    }
    catch (Exception ex) {
      logger.error("failed to process row {}", row, ex);
    }
    return rv;
  }

  private void parseShowContent(Show show, HtmlElement parent) {
    for (HtmlElement el : parent.getChildElements()) {
      if (el instanceof HtmlDivision) {
        parseShowContent(show, el);
      }
      else if (el instanceof HtmlSpan) {
        HtmlSpan span = (HtmlSpan) el;
        String spanClass = span.getAttribute("class");
        if ("tn".equals(span.getId())) {
          show.setTitle(span.getTextContent());
        }
        else if ((spanClass != null) && (spanClass.startsWith("cdt "))) {
          // this contains details about the show.
          String txt = span.asText();
          if (StringUtils.isNotEmpty(txt)) {
            txt = txt.replace('\n', ' ');
            txt = StringUtils.strip(txt, " \t,");
            if (txt.startsWith("(")) {
              parseShowSummary(show, txt);
            }
            else {
              show.setSubtitle(txt);
            }
          }
          parseShowContent(show, el);
        }
        else if ((spanClass != null) && (spanClass.startsWith("hdSymbolTxt"))) {
          show.setHd(true);
        }
        else if (!StringUtils.isEmpty(spanClass)) {
          logger.trace("unexpected span class {} with cell text {}", spanClass, span.asText());
        }
      }
    }
  }

  private void parseShowSummary(Show show, String txt) {
    int i1 = txt.indexOf('(');
    if (i1 >= 0) {
      int i2 = txt.indexOf(')', i1);
      if (i2 > 0) {
        String flags = txt.substring(i1 + 1, i2);
        parseShowSummary(show, flags);
        show.setSubtitle(StringUtils.trim(txt.substring(i2 + 1)));
        return;
      }
    }

    String[] fields = txt.split(",");
    for (String field : fields) {
      field = field.trim();
      if (StringUtils.isNotEmpty(field)) {
        // look for star rating.
        if (StringUtils.containsOnly(field, "*+")) {
          show.setStars((float) (StringUtils.countMatches(field, "*") + 0.5 * StringUtils.countMatches(field, "+")));
        }
        if (StringUtils.containsOnly(field, "0123456789")) {
          try {
            show.setYear(Integer.parseInt(field));
          }
          catch (NumberFormatException e) {
            logger.error("failed to parse year: {}", field);
          }
        }
        if ("New".equals(field)) {
          show.setNewFlag(true);
        }
        if (knownRatings.contains(field)) {
          show.setRating(field);
        }
      }
    }
  }

  private Date determineGridStartTime(HtmlTableRow row, Calendar gridCal) {
    logger.debug("determine grid start time using date {}", gridCal.getTime());
    List<HtmlElement> cells = row.getElementsByAttribute("td", "class", "gridHeaderTimeCell");
    for (HtmlElement cell : cells) {
      String timeText = cell.asText();
      int idx = timeText.indexOf(':');
      String fmt;
      if (idx < 0) {
        fmt = "h a";
      }
      else {
        fmt = "h:mm a";
      }
      SimpleDateFormat sdf = new SimpleDateFormat(fmt);
      try {
        Date dt = sdf.parse(timeText);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.set(Calendar.DAY_OF_MONTH, gridCal.get(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.MONTH, gridCal.get(Calendar.MONTH));
        cal.set(Calendar.YEAR, gridCal.get(Calendar.YEAR));
        Date result = cal.getTime();
        logger.debug("grid start determined to be {}", result);
        return result;
      }
      catch (Exception ex) {
        logger.error("fault to parse time", ex);
      }
    }
    return null;
  }

  private boolean isValid(Show show) {
    return (!StringUtils.isEmpty(show.getTitle()) && (show.getStartTime() != null) && (show.getTitle().length() < 128) && (show
        .getDuration() > 0));
  }

}
