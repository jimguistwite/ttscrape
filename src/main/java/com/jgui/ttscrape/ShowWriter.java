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


/**
 * Instances of the <code>ShowWriter</code> API are called by
 * the post processors when shows of interest are identified.  
 * 
 * @author jguistwite
 */

public interface ShowWriter {
  
  /**
   * Establish a show writer with a name and settings.
   * 
   * @param writerName name of the writer (e.g. sports)
   * @param includeSubtitle if true, writer should include the sub-title in the output.
   */
  public void establishWriter(String writerName, boolean includeSubtitle);

  /**
   * Write a show to a named show writer.
   * @param writerName show writer name (e.g. sports)
   * @param s show to be written.
   */
  public void writeShow(String writerName, Show s);

  /**
   * Processing of shows is complete and the writer should close
   * any resources it has open.
   * @param writerName name of the writer to be closed.
   */
  public void closeWriter(String writerName);
}

