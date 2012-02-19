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

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

/**
 * The <code>SciFi</code> class writes shows with
 * the scifi genre to a show writer.
 * 
 * @author jguistwite
 */

@Component
public class SciFi implements ShowPostProcessor {

	private static final String WRITER_NAME = "scifi";
	
	@Resource
	private ShowWriter writer;
	
	@Override
	public void beginSequence() {
    writer.establishWriter(WRITER_NAME, false);
	}

	@Override
	public void endSequence() {
    writer.closeWriter(WRITER_NAME);
	}

	@Override
	public void postProcess(Show s) {
		if ("SciFi".equals(s.getCategory())) {
      writer.writeShow(WRITER_NAME, s);
		}
	}

}
