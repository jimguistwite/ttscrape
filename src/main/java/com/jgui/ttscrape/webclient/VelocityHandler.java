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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.ResourceTool;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The <code>VelocityHandler</code>
 * 
 * @author jguistwite
 */

public class VelocityHandler extends AbstractHandler {

  private Logger logger = LoggerFactory.getLogger(VelocityHandler.class);

  class InvocationInfo {
    public InvocationInfo(Method method, boolean isVelocityMethod, boolean isJsonMethod) {
      this.method = method;
      this.isVelocityMethod = isVelocityMethod;
      this.isJsonMethod = isJsonMethod;
    }

    Method method;
    boolean isVelocityMethod;
    boolean isJsonMethod;
  }

  /**
   * This allows the default mapping from URL suffix to method name to be
   * overridden so that, for example, the "admin/status" URL could go to
   * something other than the status method of the delegate.
   */
  private Map<String, InvocationInfo> urlMethodNameMap;

  private VelocityEngine engine;

  private Object delegate;

  /**
   * Called by the Spring container after construction, this method initializes
   * the velocity engine for web administration of the PCEF. Templates must be
   * placed in a directory called "webapp" at the same level as the where the
   * application configuration files are located (e.g. configdir/../webapp).
   */
  @PostConstruct
  public void init() {
    try {
      BasicConfigurator.configure();
      engine = new VelocityEngine();
      Properties p = new Properties();
      p.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
      p.put("runtime.log.logsystem.log4j.logger", VelocityHandler.class.getName());
      p.put("resource.loader", "file");
      p.put("file.resource.loader.path", "webapp");
      p.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
      p.put("file.resource.loader.cache", "true");
      p.put("file.resource.loader.modificationCheckInterval", "2");
      engine.init(p);
      logger.trace("velocity engine initialized");
    }
    catch (Exception ex) {
      logger.error("failed to initialize velocity engine");
    }

    if (urlMethodNameMap == null) {
      urlMethodNameMap = new HashMap<String, InvocationInfo>();
    }

    // scan the delegate for Spring RequestMapping annotations.
    for (Method m : delegate.getClass().getMethods()) {
      RequestMapping mapping = m.getAnnotation(RequestMapping.class);
      if (mapping != null) {
        String[] values = mapping.value();
        for (String s : values) {
          boolean isVelocity = false;
          boolean isJson = false;
          Class<?>[] args = m.getParameterTypes();
          for (Class<?> k : args) {
            if (k == VelocityContext.class) {
              isVelocity = true;
            }
            else if (k == JSONObject.class) {
              isJson = true;
            }
          }
          urlMethodNameMap.put(s, new InvocationInfo(m, isVelocity, isJson));
        }
      }
    }
  }

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse rsp)
      throws IOException, ServletException {

    String prefix = "/ttscrape";
    HttpURI uri = baseRequest.getUri();
    String uriPath = uri.getPath();
    if (uriPath.startsWith(prefix)) {
      // exclude it.
      uriPath = uriPath.substring(prefix.length());
    }
    if (uriPath.startsWith("/")) {
      uriPath = uriPath.substring(1);
    }
    logger.debug("request for " + uriPath);

    String velocityFile = null;
    try {

      InvocationInfo ii = urlMethodNameMap.get(uriPath);
      if (ii == null) {
        logger.error("no mapping for {}", uriPath);
        return;
      }

      if (ii.isVelocityMethod) {
        VelocityContext context = new VelocityContext();
        initVelocityContext(context);
        Object rv = ii.method.invoke(delegate, req, rsp, context);
        // if the result is a string (ie non-null), use that string
        // as the velocity template file that is to be used.
        // if null is returned, then the method has already generated the
        // necessary response so we don't need to do anything.
        if (rv instanceof String) {
          velocityFile = rv.toString();
        }
        // if the template file was provided by the controller method,
        // dispatch to it.
        if (velocityFile != null) {
          Template template = engine.getTemplate(velocityFile);
          StringWriter writer = new StringWriter();
          template.merge(context, writer);
          String s = writer.toString();
          byte[] bytes = s.getBytes("UTF-8");
          rsp.setStatus(200);
          rsp.setContentLength(bytes.length);
          rsp.getOutputStream().write(bytes);
        }
      }
      else if (ii.isJsonMethod) {
        InputStream is = req.getInputStream();
        String input = IOUtils.toString(is);
        logger.debug("got " + input);
        JSONObject jreq = new JSONObject(input);
        JSONObject jrsp = (JSONObject) ii.method.invoke(delegate, jreq, req, rsp);
        if (jrsp != null) {
          String result = jrsp.toString();
          byte[] bytes = result.getBytes("UTF-8");
          rsp.setContentType("text/json");
          rsp.setContentLength(bytes.length);
          rsp.setStatus(200);
          rsp.getOutputStream().write(bytes);
        }
      }
      else {
        JSONObject jrsp = (JSONObject) ii.method.invoke(delegate, req, rsp);
        if (jrsp != null) {
          String result = jrsp.toString();
          byte[] bytes = result.getBytes("UTF-8");
          rsp.setContentType("text/json");
          rsp.setContentLength(bytes.length);
          rsp.setStatus(200);
          rsp.getOutputStream().write(bytes);
        }
      }

      baseRequest.setHandled(true);
    }
    catch (Exception ex) {
      logger.error("failed to process message", ex);
      try {
        rsp.setStatus(500);
      }
      catch (Exception ex2) {
        logger.error("failed to send response headers and close", ex2);
      }
    }
  }

  /**
   * Initialize the velocity context for a request.
   */
  protected void initVelocityContext(VelocityContext context) {
    context.put("datetool", new DateTool());
    context.put("numtool", new NumberTool());

    ResourceTool rt = new ResourceTool();
    HashMap<String, String> args = new HashMap<String, String>();
    args.put(ResourceTool.BUNDLES_KEY, "TT");
    rt.configure(args);
    context.put("restool", rt);

    // see if the delegate cares to do anything.
    try {
      Method m = delegate.getClass().getMethod("initVelocityContext", VelocityContext.class);
      m.invoke(delegate, context);
    }
    catch (Exception ex) {

    }

  }

  public Object getDelegate() {
    return delegate;
  }

  public void setDelegate(Object delegate) {
    this.delegate = delegate;
  }
}
