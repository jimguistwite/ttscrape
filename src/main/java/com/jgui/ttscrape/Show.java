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

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * The <code>Show</code> class contains values parsed
 * from the web scraper. 
 * 
 * @author jguistwite
 */

@SuppressWarnings("serial")
public class Show implements Serializable {
  private int id;

  private String channelNumber;
  private String channelName;
  private String title;
  private String subtitle;
  private boolean hd;
  private boolean newFlag;
  private String category;
  private float stars;
  private Date startTime;
  private int duration;
  private String rating;
  private long detailKey;
  private int year;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getRating() {
    return rating;
  }

  public void setRating(String rating) {
    this.rating = rating;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public String getChannelNumber() {
    return channelNumber;
  }

  public void setChannelNumber(String channelNumber) {
    this.channelNumber = channelNumber;
  }

  public String getChannelName() {
    return channelName;
  }

  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean isHd() {
    return hd;
  }

  public void setHd(boolean hd) {
    this.hd = hd;
  }

  public boolean isNewFlag() {
    return newFlag;
  }

  public void setNewFlag(boolean newFlag) {
    this.newFlag = newFlag;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public float getStars() {
    return stars;
  }

  public void setStars(float stars) {
    this.stars = stars;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  /**
   * Format the show with the start time, title, channel name, number, category and rating.
   * @return string representing this show.
   */
  public String format() {
    StringBuffer sb = new StringBuffer();

    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    sb.append(StringUtils.rightPad(df.format(startTime), 20));
    sb.append(StringUtils.rightPad(title, 30));
    sb.append("\t");
    sb.append(channelName + " " + channelNumber);
    sb.append("\t");
    sb.append(category);
    sb.append("\t");
    sb.append(stars);

    return sb.toString();
  }

  /**
   * Return a ToStringBuilder.reflectionToString representation of this show.
   */
  public String toString() {
    String tmpSubtitle = subtitle;
    String tmpTitle = title;
    
    if (subtitle != null) {
      subtitle = StringUtils.replaceChars(subtitle, ',', ' ');
    }
    if (title != null) {
      title = StringUtils.replaceChars(title, ',', ' ');
    }
    String rv = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    subtitle = tmpSubtitle;
    title = tmpTitle;
    return rv;
  }

  /**
   * Parse the toString() result.
   * @param s toString() string to be parsed.
   * @return resulting show.
   */
  public static Show fromString(String s) {
    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d hh:mm:ss z yyyy");
    Show show = new Show();
    int idx = s.indexOf('[');
    if (idx >= 0) {
      s = s.substring(idx + 1);
    }
    s = StringUtils.removeEnd(s, "]");
    String[] fields = StringUtils.split(s, ',');
    for (String field : fields) {
      String[] keyval = StringUtils.split(field, '=');
      if (keyval.length == 2) {
        try {
          if (!"<null>".equals(keyval[1]) && (keyval[1] != null) && !"null".equals(keyval[1])) {
            if (keyval[0].equals("startTime")) {
              Date dt = sdf.parse(keyval[1]);
              BeanUtils.setProperty(show, keyval[0], dt);
            }
            else {
              BeanUtils.setProperty(show, keyval[0], keyval[1]);
            }
          }
        }
        catch (Exception ex) {
          System.err.println("failed to set " + keyval[0] + " to " + keyval[1] + " ex: " + ex.getMessage());
        }
      }
    }
    return show;
  }

  /*
   * public String getCastAndCredits() { return castAndCredits; }
   * 
   * public void setCastAndCredits(String castAndCredits) { this.castAndCredits
   * = castAndCredits;
   * 
   * if ((cast == null) && (castAndCredits != null)) { cast = new
   * ArrayList<String>(); String[] items = castAndCredits.split(","); for(String
   * s: items) { cast.add(s); } }
   * 
   * }
   */
  public long getDetailKey() {
    return detailKey;
  }

  public void setDetailKey(long detailKey) {
    this.detailKey = detailKey;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Show) {
      return ((Show) obj).getId() == id;
    }
    else {
      return false;
    }
  }

  public String getSubtitle() {
    return subtitle;
  }

  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }
}
