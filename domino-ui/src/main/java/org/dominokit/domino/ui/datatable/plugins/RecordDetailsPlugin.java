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
package org.dominokit.domino.ui.datatable.plugins;

import static java.util.Objects.nonNull;
import static org.jboss.elemento.Elements.*;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableRowElement;
import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.datatable.*;
import org.dominokit.domino.ui.datatable.events.ExpandRecordEvent;
import org.dominokit.domino.ui.datatable.events.TableEvent;
import org.dominokit.domino.ui.icons.BaseIcon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.utils.DominoElement;
import org.dominokit.domino.ui.utils.ElementUtil;
import org.jboss.elemento.IsElement;

/**
 * This plugin add the capability to expand a row in the table to display more information about its
 * record beneath the row itself
 *
 * @param <T> the type of the data table records
 */
public class RecordDetailsPlugin<T> implements DataTablePlugin<T> {

  public static final String DATA_TABLE_DETAILS_CM = "data-table-details-cm";
  private final BaseIcon<?> collapseIcon;
  private final BaseIcon<?> expandIcon;
  private HTMLDivElement element = div().element();
  private HTMLTableCellElement td = td().css(DataTableStyles.DETAILS_TD).add(element).element();
  private HTMLTableRowElement tr = tr().css(DataTableStyles.DETAILS_TR).add(td).element();

  private final CellRenderer<T> cellRenderer;
  private DetailsButtonElement buttonElement;
  private DataTable<T> dataTable;

  /**
   * Creates an instance with custom renderer and default expand/collapse icons
   *
   * @param cellRenderer the {@link CellRenderer}
   */
  public RecordDetailsPlugin(CellRenderer<T> cellRenderer) {
    this(cellRenderer, Icons.ALL.fullscreen_exit(), Icons.ALL.fullscreen());
  }

  /**
   * Creates an instance with custom renderer and expand/collapse icons
   *
   * @param cellRenderer the {@link CellRenderer}
   * @param collapseIcon {@link BaseIcon}
   * @param expandIcon {@link BaseIcon}
   */
  public RecordDetailsPlugin(
      CellRenderer<T> cellRenderer, BaseIcon<?> collapseIcon, BaseIcon<?> expandIcon) {
    this.cellRenderer = cellRenderer;
    this.collapseIcon = collapseIcon;
    this.expandIcon = expandIcon;
  }

  /** {@inheritDoc} */
  @Override
  public void onBeforeAddHeaders(DataTable<T> dataTable) {
    this.dataTable = dataTable;
    ColumnConfig<T> column =
        ColumnConfig.<T>create(DATA_TABLE_DETAILS_CM)
            .setSortable(false)
            .setWidth("60px")
            .setFixed(true)
            .setCellRenderer(
                cell -> {
                  applyStyles(cell);
                  DetailsButtonElement<T> detailsButtonElement =
                      new DetailsButtonElement<>(
                          expandIcon, collapseIcon, RecordDetailsPlugin.this, cell);
                  cell.getTableRow().addMetaObject(detailsButtonElement);
                  applyStyles(cell);
                  return detailsButtonElement.element();
                })
            .setHeaderElement(
                columnTitle ->
                    Button.create(expandIcon.copy())
                        .linkify()
                        .disable()
                        .setCssProperty("padding", "0px")
                        .setHeight("24px")
                        .element())
            .asHeader()
            .textAlign("center");
    setupColumn(column);

    dataTable.getTableConfig().insertColumnFirst(column);
  }

  /** {@inheritDoc} */
  @Override
  public void handleEvent(TableEvent event) {
    if (ExpandRecordEvent.EXPAND_RECORD.equals(event.getType())) {
      expandRow((ExpandRecordEvent<T>) event);
    }
  }

  private void expandRow(ExpandRecordEvent<T> event) {
    DetailsButtonElement<T> detailsButtonElement =
        event.getTableRow().getMetaObject(DataTableStyles.RECORD_DETAILS_BUTTON);
    setExpanded(detailsButtonElement);
  }

  /** @return the root {@link HTMLDivElement} of this component */
  public HTMLDivElement getElement() {
    return element;
  }

  /** @return the {@link HTMLTableCellElement} that contains the records details */
  public HTMLTableCellElement getTd() {
    return td;
  }

  /** @return the {@link HTMLTableRowElement} that contains the records details */
  public HTMLTableRowElement getTr() {
    return tr;
  }

  public void applyStyles(CellRenderer.CellInfo<T> cellInfo) {}

  /**
   * A hook method to customize the column of the expand/collapse icons
   *
   * @param column {@link ColumnConfig}
   */
  public void setupColumn(ColumnConfig<T> column) {}

  private static class DetailsButtonElement<T>
      implements IsElement<HTMLElement>, TableRow.RowMetaObject {
    private final DominoElement<HTMLDivElement> element;
    private final CellRenderer.CellInfo<T> cellInfo;
    private final BaseIcon<?> expandIcon;
    private final BaseIcon<?> collapseIcon;
    private RecordDetailsPlugin<?> recordDetailsPlugin;

    public DetailsButtonElement(
        BaseIcon<?> expandIcon,
        BaseIcon<?> collapseIcon,
        RecordDetailsPlugin<?> recordDetailsPlugin,
        CellRenderer.CellInfo<T> cellInfo) {
      this.expandIcon = expandIcon.copy();
      this.collapseIcon = collapseIcon.copy();
      this.recordDetailsPlugin = recordDetailsPlugin;
      this.cellInfo = cellInfo;
      this.element = DominoElement.div();
      this.element
          .appendChild(this.expandIcon.clickable())
          .appendChild(this.collapseIcon.clickable().hide());
      element
          .setCssProperty("padding", "0px")
          .setHeight("27px")
          .setPaddingLeft("2px")
          .setPaddingRight("2px");

      this.expandIcon.addClickListener(evt -> expand());
      this.collapseIcon.addClickListener(evt -> collapse());
    }

    public CellRenderer.CellInfo<T> getCellInfo() {
      return cellInfo;
    }

    public void expand() {
      expandIcon.hide();
      collapseIcon.show();
      recordDetailsPlugin.setExpanded(this);
    }

    public void collapse() {
      expandIcon.show();
      collapseIcon.hide();
      recordDetailsPlugin.clear();
    }

    @Override
    public HTMLElement element() {
      return element.element();
    }

    @Override
    public String getKey() {
      return DataTableStyles.RECORD_DETAILS_BUTTON;
    }
  }

  private void clear() {
    tr.remove();
    ElementUtil.clear(element);
    this.buttonElement = null;
  }

  private void setExpanded(DetailsButtonElement buttonElement) {
    if (nonNull(this.buttonElement)) {
      this.buttonElement.collapse();
      clear();
    }
    this.buttonElement = buttonElement;
    ElementUtil.contentBuilder(td)
        .attr("colspan", dataTable.getTableConfig().getColumns().size() + "");
    element.appendChild(cellRenderer.asElement(buttonElement.getCellInfo()));
    dataTable
        .bodyElement()
        .element()
        .insertBefore(tr, buttonElement.getCellInfo().getTableRow().element().nextSibling);
  }
}
