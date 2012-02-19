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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgui.ttscrape.ClientNotifier;

/**
 * The <code>MyWebSocketHandler</code> 
 * 
 * @author jguistwite
 */

public class MyWebSocketHandler extends WebSocketHandler implements ClientNotifier {

  private Logger logger = LoggerFactory.getLogger(MyWebSocketHandler.class);
  
  private final Set<ClientWebSocket> webSockets = new CopyOnWriteArraySet<ClientWebSocket>();

  private String uriPrefix = "/wsock";
  
  @PostConstruct
  public void init() {
  }
  
  public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
    logger.trace("got new web socket connection request {} {}", request, protocol);
    return new ClientWebSocket();
  }

  /*
  @Override
  public String checkOrigin(HttpServletRequest request, String host, String origin) {
    String rv = super.checkOrigin(request, host, origin);
    logger.debug("checkOrigin(" + host + "," + origin + "=" + rv);
    return rv;
  }

  @Override
  protected void doStart() throws Exception {
    logger.debug("doStart()");
    super.doStart();
  }

  @Override
  protected void doStop() throws Exception {
    logger.debug("doStop()");
    super.doStop();
  }
  */
  
  /**
   * Override the handle method to short circuit processing of the request if it doesn't
   * match our target.  If we don't process the request, it is passed onto the next Jetty
   * handler.
   */
  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    //logger.debug("handle {}", target);
    if (target.startsWith(uriPrefix)) {
      super.handle(target, baseRequest, request, response);
      baseRequest.setHandled(true);
    }
  }

  private class ClientWebSocket implements WebSocket.OnTextMessage {

    private Connection connection;

    public void onOpen(Connection connection) {
      //logger.debug("on open");
      this.connection = connection;
      webSockets.add(this);
    }

    /**
     * When a message is received from a client, broadcast it to
     * the other clients.  Not sure what I'll use this for but
     * it remains from the chat server that I used as a basis for this.
     */
    public void onMessage(String data) {
      //logger.debug("on message {}", data);
      for (ClientWebSocket webSocket : webSockets) {
        try {
          webSocket.connection.sendMessage(data);
        }
        catch (IOException ex) {
          logger.warn("exception sending to client " + webSocket, ex);
          webSocket.connection.disconnect();
          // TODO: might get a concurrent modification exception
          webSockets.remove(webSocket);
        }
      }
    }

    public void onClose(int closeCode, String message) {
      //logger.debug("on close");
      webSockets.remove(this);
    }
  }
  
  
  
  
  
  /**
   * Notify the web clients of some event.
   * @param message message to send to the client.
   */
  public void notifyClient(String message) {
    for (ClientWebSocket webSocket : webSockets) {
      try {
        webSocket.connection.sendMessage(message);
      }
      catch (IOException ex) {
        webSocket.connection.disconnect();
        webSockets.remove(webSocket);
        logger.warn("failed to notify web socket client", ex);
      }
    }    
  }

  /**
   * Convenience method to inform the web clients of some action turning the
   * values provided into a JSON request message.
   * @param action action to be performed on the client
   * @param arg1Key first argument key
   * @param arg1Value first argument value
   */
  public void notifyClient(String action, String arg1Key, String arg1Value) {
    HashMap<String, String> data = new HashMap<String, String>();
    data.put("action", action);
    data.put(arg1Key, arg1Value);
    notifyClient(data);
  }

  /**
   * Convenience method to inform the web clients of some action turning the
   * values provided into a JSON request message.
   * @param action action to be performed on the client
   * @param arg1Key first argument key
   * @param arg1Value first argument value
   * @param arg2Key second argument key
   * @param arg2Value second argument value
   */
  public void notifyClient(String action, String arg1Key, String arg1Value, String arg2Key, String arg2Value) {
    HashMap<String, String> data = new HashMap<String, String>();
    data.put("action", action);
    data.put(arg1Key, arg1Value);
    data.put(arg2Key, arg2Value);
    notifyClient(data);
  }

  /**
   * Convenience method to inform the web clients of some action turning the
   * map of key value pairs provided into a JSON object to be sent to the client.
   * @param data values for JSON request.
   */
  public void notifyClient(Map<String, String> data) {
    try {
      JSONObject obj = new JSONObject();
      for (String s : data.keySet()) {
        obj.put(s, data.get(s));
      }
      notifyClient(obj.toString());
    }
    catch (JSONException ex) {
      logger.error("failed to notify clients", ex);
    }
  }

  public String getUriPrefix() {
    return uriPrefix;
  }

  public void setUriPrefix(String uriPrefix) {
    this.uriPrefix = uriPrefix;
  }
}
