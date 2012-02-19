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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.jgui.ttscrape.Show;

/**
 * The <code>TitanTvPageParser</code> class parses the high level content
 * from the page then delegates to the GridParser for the listings content.
 * 
 * @author jguistwite
 */

public class TitanTvPageParser {
	private Logger logger = LoggerFactory.getLogger(TitanTvPageParser.class);

  private GridParser gridParser;
  
	public TitanTvPageParser() {
	  gridParser = new GridParser();
	}
	
	public List<Show> parsePage(HtmlPage page) {
		List<Show> rv = null;
		
		long start = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug("parsing page begin");
		}
		
		try {
			HtmlElement navSpan = page.getElementById("ctl00_Main_TVL_ctl00_Nav");
			HtmlElement dateSelect = navSpan.getElementById("ctl00_Main_TVL_ctl00_Nav_ctl00_Dates_Input");
			String dateString = dateSelect.getAttribute("value");
			SimpleDateFormat sdf = new SimpleDateFormat("E - MM/dd");
			Date gridDate = sdf.parse(dateString);
			Calendar gridCal = Calendar.getInstance();
			int currentYear = gridCal.get(Calendar.YEAR);
			gridCal.setTime(gridDate);
			gridCal.set(Calendar.YEAR, currentYear);
			gridCal.set(Calendar.HOUR_OF_DAY, 0);
			gridCal.set(Calendar.MINUTE, 0);
			gridCal.set(Calendar.SECOND, 0);
			gridCal.set(Calendar.MILLISECOND, 0);

			HtmlElement div = page.getElementById("ctl00_Main_TVL_ctl00_Grid");
			List<HtmlElement> tablesShouldBeOne = div.getElementsByAttribute("table", "class", "gridTable");
			HtmlTable tbl = (HtmlTable) tablesShouldBeOne.get(0);
			rv = gridParser.parseGrid(gridCal, tbl);
		}
		catch (IndexOutOfBoundsException e) {
      logger.error("huh - grid has no table", e);
		}
		catch (ElementNotFoundException e) {
      logger.error("huh", e);
		}
		catch (ParseException e) {
			logger.error("huh", e);
		}

		if (logger.isDebugEnabled()) {
		  long end = System.currentTimeMillis();
			logger.debug("parsing completed.  duration={}", (double)((end - start) / 1000d));
		}
		
		return rv;
	}
}
