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
package org.dominokit.domino.ui.spin;

import elemental2.dom.DOMRect;
import elemental2.dom.HTMLDivElement;
import org.dominokit.domino.ui.icons.BaseIcon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.utils.SwipeUtil;

/**
 * A component provides horizontal spin
 *
 * @param <T> the type of the object inside the spin
 */
public class HSpinSelect<T> extends SpinSelect<T, HSpinSelect<T>> {

  /**
   * Creates new instance
   *
   * @param <T> the type of the object inside the spin
   * @return new instance
   */
  public static <T> HSpinSelect<T> create() {
    return new HSpinSelect<>();
  }

  /**
   * Creates new instance with back/forward icons
   *
   * @param backIcon the back {@link BaseIcon}
   * @param forwardIcon the forward {@link BaseIcon}
   * @param <T> the type of the object inside the spin
   * @return new instance
   */
  public static <T> HSpinSelect<T> create(BaseIcon<?> backIcon, BaseIcon<?> forwardIcon) {
    return new HSpinSelect<>(backIcon, forwardIcon);
  }

  public HSpinSelect() {
    this(Icons.ALL.keyboard_arrow_left(), Icons.ALL.keyboard_arrow_right());
  }

  public HSpinSelect(BaseIcon<?> backIcon, BaseIcon<?> forwardIcon) {
    super(backIcon, forwardIcon);
    SwipeUtil.addSwipeListener(SwipeUtil.SwipeDirection.RIGHT, main.element(), evt -> moveBack());
    SwipeUtil.addSwipeListener(SwipeUtil.SwipeDirection.LEFT, main.element(), evt -> moveForward());
  }

  @Override
  protected void fixElementsWidth() {
    DOMRect boundingClientRect = main.getBoundingClientRect();
    double totalWidth = boundingClientRect.width * items.size();
    contentPanel.setWidth(100 * items.size() + "%");

    items.forEach(
        spinItem -> spinItem.setWidth(((boundingClientRect.width / totalWidth) * 100) + "%"));
  }

  @Override
  protected void setTransformProperty(double offset) {
    contentPanel.setCssProperty("transform", "translate3d(-" + offset + "%, 0px, 0px)");
  }

  @Override
  protected String getStyle() {
    return SpinStyles.H_SPIN;
  }

  /** {@inheritDoc} */
  @Override
  public HTMLDivElement element() {
    return element.element();
  }
}
