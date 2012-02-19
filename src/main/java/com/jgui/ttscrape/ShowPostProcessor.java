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
 * The <code>ShowPostProcessor</code> interface provides a
 * API allowing beans to enhance the data or further process
 * a show before it is persisted.
 * 
 * @author jguistwite
 */

public interface ShowPostProcessor
{
  /**
   * Post process the show.
   * @param s show to be processed.
   */
	public void postProcess(Show s);
	
	/**
	 * Called to begin a sequence of shows.
	 */
	public void beginSequence();
	
	/**
	 * Called to end a sequence of shows.
	 */
	public void endSequence();
}
