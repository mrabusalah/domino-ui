/*
 * Copyright © 2019 Dominokit
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
package org.dominokit.domino.ui.forms;

import java.util.function.Function;
import org.dominokit.domino.ui.utils.DominoUIConfig;

/** A component that has an input to take/provide Integer value */
public class IntegerBox extends NumberBox<IntegerBox, Integer> {

  /** @return a new instance without a label */
  public static IntegerBox create() {
    return new IntegerBox();
  }

  /**
   * @param label String
   * @return new instance with a label
   */
  public static IntegerBox create(String label) {
    return new IntegerBox(label);
  }

  /** Create instance without a label */
  public IntegerBox() {
    this("");
  }

  /**
   * Create an instance with a label
   *
   * @param label String
   */
  public IntegerBox(String label) {
    super(label);
  }

  /** {@inheritDoc} */
  @Override
  protected void clearValue() {
    value(0);
  }

  /** {@inheritDoc} */
  @Override
  protected Function<String, Integer> defaultValueParser() {
    return DominoUIConfig.INSTANCE.getNumberParsers().integerParser(this);
  }

  /** {@inheritDoc} */
  @Override
  protected Integer defaultMaxValue() {
    return Integer.MAX_VALUE;
  }

  /** {@inheritDoc} */
  @Override
  protected Integer defaultMinValue() {
    return Integer.MIN_VALUE;
  }

  /** {@inheritDoc} */
  @Override
  protected boolean isExceedMaxValue(Integer maxValue, Integer value) {
    return value > maxValue;
  }

  /** {@inheritDoc} */
  @Override
  protected boolean isLowerThanMinValue(Integer minValue, Integer value) {
    return value < minValue;
  }
}
