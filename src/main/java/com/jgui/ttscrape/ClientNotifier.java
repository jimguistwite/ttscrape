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

import java.util.Map;

/**
 * The <code>ClientNotifier</code> interface provides an API
 * for other components to use to notify a web client of some
 * event that in can process.
 * 
 * @author jguistwite
 */

public interface ClientNotifier {
  /**
   * Send a message to be displayed to the user in an alert dialog.
   * @param message text message to be displayed.
   */
  public void notifyClient(String message);
  
  /**
   * Send a generic collection of fields to be interpreted by the client.
   * @param data data to be serializes as a json message for consumption by the client.
   */
  public void notifyClient(Map<String,String> data);
  
  /**
   * Convenience method to send an action and argument pair to the client.
   * @param action action to help the client process the message
   * @param arg1Key key for the argument
   * @param arg1Value value of argument
   */
  public void notifyClient(String action, String arg1Key, String arg1Value);

  /**
   * Convenience method to send an action and two argument pairs to the client.
   * @param action action to help the client process the message
   * @param arg1Key key for the argument
   * @param arg1Value value of argument
   * @param arg2Key key for the argument
   * @param arg2Value value of argument
   */
  public void notifyClient(String action, String arg1Key, String arg1Value, String arg2Key, String arg2Value);
}

