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

/**
 * The <code>JettyServer</code> 
 * 
 * @author jguistwite
 */

import java.util.Map;

import javax.annotation.PostConstruct;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* The <code>JettyServer</code> is an embedded Jetty server instance to handle
* traffic supporting the web services, RESTful services and web browser traffic.
* 
* @author jguistwite
*/

public class JettyServer {
 private Logger logger = LoggerFactory.getLogger(JettyServer.class);
 
 private int port;
 private String resourceBase;
 private Map<String,Handler> handlers;
 
 public JettyServer() {
   port = 8090; // default the port
 }
 
 @PostConstruct
 public void initServer() {
   
   logger.debug("jetty port is {}", port);
   
   Server server = new Server(port);

   // using the handlers defined in the Spring XML,
   // install the handlers into the Jetty server.
   HandlerList handlerList = new HandlerList();
   for(Map.Entry<String,Handler> hdef: handlers.entrySet()) {
     String key = hdef.getKey();
     if (key.startsWith("context:")) {
       String context = key.substring(8);
       ContextHandler h = new ContextHandler();
       h.setContextPath("/" + context);
       h.setClassLoader(Thread.currentThread().getContextClassLoader());
       h.setHandler(hdef.getValue());
       logger.debug("add handler {} for key {}", h, context);
       handlerList.addHandler(h);
     }
     else {
       handlerList.addHandler(hdef.getValue());
     }
   }
   
   
   // create a root handler
   ResourceHandler rootHandler = new ResourceHandler();
   rootHandler.setWelcomeFiles(new String[] {"index.html"});
   rootHandler.setDirectoriesListed(false);
   rootHandler.setResourceBase(resourceBase);
   ContextHandler h = new ContextHandler();
   h.setContextPath("/");
   h.setClassLoader(Thread.currentThread().getContextClassLoader());
   h.setHandler(rootHandler);
   handlerList.addHandler(h);
   
   //handlerList.addHandler(new DefaultHandler());
   server.setHandler(handlerList);

   try {
     server.start();
   }
   catch (Exception ex) {
     logger.error("failed to start jetty server.", ex);
   }
 }

 public int getPort() {
   return port;
 }

 public void setPort(int port) {
   this.port = port;
 }

 public Map<String, Handler> getHandlers() {
   return handlers;
 }

 public void setHandlers(Map<String, Handler> handlers) {
   this.handlers = handlers;
 }

 public String getResourceBase() {
   return resourceBase;
 }

 public void setResourceBase(String resourceBase) {
   this.resourceBase = resourceBase;
 }
 
}

