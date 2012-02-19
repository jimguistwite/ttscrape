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
 * The <code>MovieRatingProcessor</code> creates show writer instances
 * for "star3" and "star4" to output movies and shows with a titantv
 * rating of 3.5 or 4. 
 * 
 * @author jguistwite
 */

@Component
public class MovieRatingProcessor implements ShowPostProcessor {

  @Resource
  private ShowWriter writer;
  private static final String WRITER3 = "star3";
  private static final String WRITER4 = "star4";

  @Override
  public void beginSequence() {
    writer.establishWriter(WRITER3, false);
    writer.establishWriter(WRITER4, false);
  }

  @Override
  public void endSequence() {
    writer.closeWriter(WRITER3);
    writer.closeWriter(WRITER4);
  }

  @Override
  public void postProcess(Show s) {
    if (s.getStars() >= 4) {
      writer.writeShow(WRITER4, s);
    }

    if (s.getStars() == 3.5) {
      writer.writeShow(WRITER3, s);
    }
  }

}
