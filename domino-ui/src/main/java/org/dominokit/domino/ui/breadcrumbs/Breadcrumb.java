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
package org.dominokit.domino.ui.breadcrumbs;

import static java.util.Objects.nonNull;
import static org.jboss.elemento.Elements.ol;

import elemental2.dom.EventListener;
import elemental2.dom.HTMLOListElement;
import java.util.LinkedList;
import java.util.List;
import org.dominokit.domino.ui.icons.BaseIcon;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.utils.BaseDominoElement;
import org.dominokit.domino.ui.utils.DominoElement;
import org.dominokit.domino.ui.utils.HasBackground;

/**
 * Provides current location in a navigational hierarchy.
 *
 * <p>This component displays locations with the ability to navigate between them and highlighting
 * the current one, each location can have text and icon with a click listener that will be called
 * when user clicks on it.
 *
 * <p>Customize the component can be done by overwriting classes provided by {@link
 * BreadcrumbStyles}
 *
 * <p>For example:
 *
 * <pre>
 *     Breadcrumb.create()
 *               .appendChild(" Home ", (evt) -> {//do something when clicked})
 *               .appendChild(" Library ", (evt) -> {//do something when clicked})
 * </pre>
 *
 * @see BaseDominoElement
 * @see HasBackground
 * @see BreadcrumbItem
 */
public class Breadcrumb extends BaseDominoElement<HTMLOListElement, Breadcrumb>
    implements HasBackground<Breadcrumb> {

  private final DominoElement<HTMLOListElement> element =
      DominoElement.of(ol().css(BreadcrumbStyles.BREADCRUMB));
  private final List<BreadcrumbItem> items = new LinkedList<>();
  private BreadcrumbItem activeItem;
  private boolean removeTail = false;
  private Color activeColor;
  private Color activeBackground;
  private boolean allowNavigation = true;

  public Breadcrumb() {
    init(this);
  }

  /**
   * Creates new empty breadcrumb
   *
   * @return new breadcrumb instance
   */
  public static Breadcrumb create() {
    return new Breadcrumb();
  }

  /**
   * Adds new location with {@code text} and {@code onClick} listener
   *
   * @param text the label of the location
   * @param onClick {@link EventListener} that will be called when the location is clicked
   * @return same instance
   */
  public Breadcrumb appendChild(String text, EventListener onClick) {
    BreadcrumbItem item = BreadcrumbItem.create(text);
    addNewItem(item);
    item.addClickListener(onClick);
    return this;
  }

  /**
   * Adds new location with {@code text}, {@code icon} and {@code onClick} listener
   *
   * @param icon the {@link BaseIcon} of the location
   * @param text the label of the location
   * @param onClick {@link EventListener} that will be called when the location is clicked
   * @return same instance
   */
  public Breadcrumb appendChild(BaseIcon<?> icon, String text, EventListener onClick) {
    BreadcrumbItem item = BreadcrumbItem.create(icon, text);
    addNewItem(item);
    item.addClickListener(onClick);
    return this;
  }

  /**
   * Adds new location by providing {@link BreadcrumbItem}
   *
   * @param item the {@link BreadcrumbItem} location to be added
   * @return same instance
   */
  public Breadcrumb appendChild(BreadcrumbItem item) {
    addNewItem(item);
    return this;
  }

  /**
   * Sets if the breadcrumb supports navigation between its locations
   *
   * @param allowNavigation true to allow navigation, false otherwise
   * @return same instance
   */
  public Breadcrumb setAllowNavigation(boolean allowNavigation) {
    this.allowNavigation = allowNavigation;
    removeCss(BreadcrumbStyles.NAV_DISABLED);

    if (!allowNavigation) {
      addCss(BreadcrumbStyles.NAV_DISABLED);
    }
    return this;
  }

  /** @return true if the breadcrumb supports navigation, false otherwise */
  public boolean isAllowNavigation() {
    return allowNavigation;
  }

  private void addNewItem(BreadcrumbItem item) {
    items.add(item);
    setActiveItem(item);
    element.appendChild(item);
    DominoElement.of(item.getClickableElement())
        .addClickListener(
            e -> {
              if (allowNavigation) {
                setActiveItem(item);
              }
            });
  }

  private Breadcrumb setActiveItem(BreadcrumbItem item) {
    if (nonNull(activeItem)) activeItem.deActivate();
    item.activate();
    this.activeItem = item;
    item.activate();
    if (removeTail) {
      int index = items.indexOf(item) + 1;
      while (items.size() > index) {
        items.get(items.size() - 1).element().remove();
        items.remove(items.size() - 1);
      }
    }

    return this;
  }

  /**
   * If true, then selecting location will remove the tailing ones.
   *
   * <p>For example:
   *
   * <ul>
   *   <li>Given {@code removeTail} is true and having 4 locations as follows:
   *       <p><strong>A</strong> -> <strong>B</strong> -> <strong>C</strong> -> <strong>D</strong>
   *       <p>when selecting location <strong>B</strong>, then the new locations will be as follows:
   *       <p><strong>A</strong> -> <strong><u>B</u></strong>
   *   <li>Given {@code removeTail} is false and having 4 locations as follows:
   *       <p><strong>A</strong> -> <strong>B</strong> -> <strong>C</strong> -> <strong>D</strong>
   *       <p>when selecting location <strong>B</strong>, then the new locations will be as follows:
   *       <p><strong>A</strong> -> <strong><u>B</u></strong> -> <strong>C</strong> ->
   *       <strong>D</strong>
   * </ul>
   *
   * @param removeTail true for activating remove tail, false otherwise
   * @return same instance
   */
  public Breadcrumb setRemoveActiveTailItem(boolean removeTail) {
    this.removeTail = removeTail;
    return this;
  }

  /**
   * Sets the color of the breadcrumb
   *
   * @param color the new {@link Color}
   * @return same instance
   */
  public Breadcrumb setColor(Color color) {
    if (nonNull(this.activeColor)) element.removeCss(color.getStyle());
    this.activeColor = color;
    element.addCss(color.getStyle());

    return this;
  }

  /**
   * Positions the breadcrumb to the center of its parent
   *
   * @return same instance
   */
  public Breadcrumb alignCenter() {
    style().alignCenter();
    return this;
  }

  /**
   * Positions the breadcrumb to the right of its parent
   *
   * @return same instance
   */
  public Breadcrumb alignRight() {
    style().alignRight();
    return this;
  }

  /**
   * Removes all locations from the breadcrumb
   *
   * @return same instance
   */
  public Breadcrumb removeAll() {
    items.forEach(BaseDominoElement::remove);
    items.clear();
    this.activeItem = null;
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public HTMLOListElement element() {
    return element.element();
  }

  /** {@inheritDoc} */
  @Override
  public Breadcrumb setBackground(Color background) {
    if (nonNull(this.activeBackground)) element.removeCss(background.getBackground());
    this.activeBackground = background;
    element.addCss(background.getBackground());

    return this;
  }

  /** @return The active {@link BreadcrumbItem} location */
  public BreadcrumbItem getActiveItem() {
    return activeItem;
  }

  /** @return All {@link BreadcrumbItem} locations */
  public List<BreadcrumbItem> getItems() {
    return items;
  }
}
