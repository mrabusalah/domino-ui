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
package org.dominokit.domino.ui.datatable;

import static java.util.Objects.nonNull;
import static org.jboss.elemento.Elements.*;

import elemental2.dom.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dominokit.domino.ui.datatable.plugins.DataTablePlugin;
import org.dominokit.domino.ui.popover.Tooltip;
import org.dominokit.domino.ui.style.Style;
import org.dominokit.domino.ui.utils.DominoElement;
import org.dominokit.domino.ui.utils.HasMultiSelectionSupport;
import org.jboss.elemento.HtmlContentBuilder;

/**
 * This class is responsible of configuring the data table
 *
 * <pre>
 *     TableConfig&lt;Contact&gt; tableConfig = new TableConfig&lt;&gt;();
 * tableConfig
 *         .addColumn(ColumnConfig.<Contact&gt;create("id", "#")
 *                 .textAlign("right")
 *                 .asHeader()
 *                 .setCellRenderer(cell -&gt; TextNode.of(cell.getTableRow().getRecord().getIndex() + 1 + "")))
 *         .addColumn(ColumnConfig.<Contact&gt;create("firstName", "First name")
 *                 .setCellRenderer(cell -&gt; TextNode.of(cell.getTableRow().getRecord().getName())))
 *         .addColumn(ColumnConfig.<Contact&gt;create("email", "Email")
 *                 .setCellRenderer(cell -&gt; TextNode.of(cell.getTableRow().getRecord().getEmail())))
 *         .addColumn(ColumnConfig.<Contact&gt;create("phone", "Phone")
 *                 .setCellRenderer(cell -&gt; TextNode.of(cell.getTableRow().getRecord().getPhone())))
 *         .addColumn(ColumnConfig.<Contact&gt;create("badges", "Badges")
 *                 .setCellRenderer(cell -&gt; {
 *                     if (cell.getTableRow().getRecord().getAge() &lt; 35) {
 *                         return Badge.create("Young")
 *                                 .setBackground(ColorScheme.GREEN.color()).element();
 *                     }
 *                     return TextNode.of("");
 *                 }));
 * </pre>
 *
 * @param <T> the type of the data table records
 */
public class TableConfig<T> implements HasMultiSelectionSupport {

  private List<ColumnConfig<T>> columns = new LinkedList<>();
  private List<DataTablePlugin<T>> plugins = new LinkedList<>();
  private DataTable<T> dataTable;
  private boolean fixed = false;
  private String fixedDefaultColumnWidth = "100px";
  private String fixedBodyHeight = "400px";
  private boolean lazyLoad = true;
  private boolean multiSelect = true;
  private RowAppender<T> rowAppender =
      (dataTable, tableRow) -> dataTable.bodyElement().appendChild(tableRow.element());
  private DirtyRecordProvider<T> dirtyRecordProvider = original -> original;
  private SaveDirtyRecordHandler<T> saveDirtyRecordHandler = (originalRecord, dirtyRecord) -> {};

  /**
   * This method will draw the table columns header elements for all columns and append them to the
   * table head element
   *
   * @param dataTable the {@link DataTable} initialized with this configuration
   * @param thead the {@link DominoElement} of {@link HTMLTableSectionElement} that is the table
   *     header element
   */
  public void drawHeaders(DataTable<T> dataTable, DominoElement<HTMLTableSectionElement> thead) {
    this.dataTable = dataTable;
    HtmlContentBuilder<HTMLTableRowElement> tr = tr();
    thead.appendChild(tr.element());

    columns.forEach(
        columnConfig -> {
          // TODO replace with FlexLayout
          Node element = columnConfig.getHeaderElement().asElement(columnConfig.getTitle());
          columnConfig.contextMenu = div().style("width: 15px; display: none;").element();
          HtmlContentBuilder<HTMLDivElement> headerContent =
              div()
                  .style("display: flex;")
                  .add(div().style("width:100%").add(element))
                  .add(columnConfig.contextMenu);
          HtmlContentBuilder<HTMLTableCellElement> th =
              th().css(DataTableStyles.TABLE_CM_HEADER).add(headerContent.element());

          columnConfig.applyScreenMedia(th.element());

          tr.add(th);
          columnConfig.setHeadElement(th.element());
          if (dataTable.getTableConfig().isFixed() || columnConfig.isFixed()) {
            fixElementWidth(columnConfig, th.element());
          }

          if (columnConfig.isShowTooltip()) {
            Tooltip.create(th.element(), columnConfig.getTooltipNode());
          }
          columnConfig.applyHeaderStyle();
          columnConfig.addShowHideListener(DefaultColumnShowHideListener.of(th.element(), true));
          DominoElement.of(th).toggleDisplay(!columnConfig.isHidden());

          plugins.forEach(plugin -> plugin.onHeaderAdded(dataTable, columnConfig));
        });

    dataTable.tableElement().appendChild(thead);
  }

  private void fixElementWidth(ColumnConfig<T> column, HTMLElement element) {
    String fixedWidth = bestFitWidth(column);
    Style.of(element)
        .setWidth(fixedWidth)
        .setMinWidth(fixedWidth)
        .setMaxWidth(fixedWidth)
        .addCss(DataTableStyles.FIXED_WIDTH);
  }

  /**
   * Draw a record as a row in the data table, row information is obtained from the TableRow
   *
   * @param dataTable the {@link DataTable} initialized with this configuration
   * @param tableRow the {@link TableRow} we are adding to the table
   */
  public void drawRecord(DataTable<T> dataTable, TableRow<T> tableRow) {
    columns.forEach(
        columnConfig -> {
          HTMLTableCellElement cellElement;
          if (columnConfig.isHeader()) {
            cellElement = th().css("dt-th-cell").element();
          } else {
            cellElement = td().css("dt-td-cell").element();
          }

          if (dataTable.getTableConfig().isFixed() || columnConfig.isFixed()) {
            fixElementWidth(columnConfig, cellElement);
          }

          RowCell<T> rowCell =
              new RowCell<>(new CellRenderer.CellInfo<>(tableRow, cellElement), columnConfig);
          rowCell.updateCell();
          tableRow.addCell(rowCell);

          columnConfig.applyScreenMedia(cellElement);

          tableRow.element().appendChild(cellElement);
          columnConfig.applyCellStyle(cellElement);
          columnConfig.addShowHideListener(DefaultColumnShowHideListener.of(cellElement));
          DominoElement.of(cellElement).toggleDisplay(!columnConfig.isHidden());
        });
    rowAppender.appendRow(dataTable, tableRow);

    plugins.forEach(plugin -> plugin.onRowAdded(dataTable, tableRow));
  }

  /**
   * Adds a configuration for a column in the data table
   *
   * @param column {@link ColumnConfig}
   * @return same TableConfig instance
   */
  public TableConfig<T> addColumn(ColumnConfig<T> column) {
    this.columns.add(column);
    return this;
  }

  /**
   * Adds a configuration for a column in the data table as the first column over the existing
   * columns list
   *
   * @param column {@link ColumnConfig}
   * @return same TableConfig instance
   */
  public TableConfig<T> insertColumnFirst(ColumnConfig<T> column) {
    this.columns.add(0, column);
    return this;
  }

  /**
   * Adds a configuration for a column in the data table as the last column after the existing
   * columns list
   *
   * @param column {@link ColumnConfig}
   * @return same TableConfig instance
   */
  public TableConfig<T> insertColumnLast(ColumnConfig<T> column) {
    this.columns.add(this.columns.size() - 1, column);
    return this;
  }

  /**
   * Adds a new plugin to the data table
   *
   * @param plugin {@link DataTablePlugin}
   * @return same TableConfig instance
   */
  public TableConfig<T> addPlugin(DataTablePlugin<T> plugin) {
    this.plugins.add(plugin);
    return this;
  }

  /**
   * @return boolean, if true then this table will have a fixed width and wont change the columns
   *     width when resized, otherwise columns will stretch to match the table root element width
   */
  public boolean isFixed() {
    return fixed;
  }

  /**
   * @param fixed boolean, if true then this table will have a fixed width and wont change the
   *     columns width when resized, otherwise columns will stretch to match the table root element
   *     width
   * @return same TableConfig instance
   */
  public TableConfig<T> setFixed(boolean fixed) {
    this.fixed = fixed;
    return this;
  }

  /**
   * @return boolean, if true the table will only start loading the data from the data store if load
   *     is called manually, otherwise it will automatically load the data when it is initialized
   */
  public boolean isLazyLoad() {
    return lazyLoad;
  }

  /**
   * @param lazyLoad boolean, if true the table will only start loading the data from the data store
   *     if load is called manually, otherwise it will automatically load the data when it is
   *     initialized
   * @return same TableConfig instance
   */
  public TableConfig<T> setLazyLoad(boolean lazyLoad) {
    this.lazyLoad = lazyLoad;
    return this;
  }

  /**
   * @return String, the height of the data table body, this is the value we set with {@link
   *     #setFixedBodyHeight(String)} not the actual current table body height
   */
  public String getFixedBodyHeight() {
    return fixedBodyHeight;
  }

  /**
   * @param fixedBodyHeight boolean, if true the height of the table body will be fixed to the
   *     specified value and while adding records to the table if the total height of rows exceed
   *     this height scroll bars will show up, otherwise the table body will not fixed and will grow
   *     to match the rows height and wont show scrollbars
   * @return same TableConfig instance
   */
  public TableConfig<T> setFixedBodyHeight(String fixedBodyHeight) {
    this.fixedBodyHeight = fixedBodyHeight;
    return this;
  }

  /** @return String default value for a fixed column width */
  public String getFixedDefaultColumnWidth() {
    return fixedDefaultColumnWidth;
  }

  /**
   * @param fixedDefaultColumnWidth String default value to be used as width for the fixed width
   *     columns
   * @return same TableConfig instance
   */
  public TableConfig<T> setFixedDefaultColumnWidth(String fixedDefaultColumnWidth) {
    this.fixedDefaultColumnWidth = fixedDefaultColumnWidth;
    return this;
  }

  /**
   * @param columnConfig String value of preferred width to be used for a column from its width.
   *     min-width, max-width or default fixedDefaultColumnWidth
   * @return same TableConfig instance
   */
  String bestFitWidth(ColumnConfig<T> columnConfig) {
    if (nonNull(columnConfig.getWidth()) && !columnConfig.getWidth().isEmpty()) {
      return columnConfig.getWidth();
    } else if (nonNull(columnConfig.getMinWidth()) && !columnConfig.getMinWidth().isEmpty()) {
      return columnConfig.getMinWidth();
    } else if (nonNull(columnConfig.getMaxWidth()) && !columnConfig.getMaxWidth().isEmpty()) {
      return columnConfig.getMaxWidth();
    } else {
      return fixedDefaultColumnWidth;
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isMultiSelect() {
    return this.multiSelect;
  }

  /** {@inheritDoc} */
  @Override
  public void setMultiSelect(boolean multiSelect) {
    this.multiSelect = multiSelect;
  }

  /**
   * Change the default RowAppender for the data table
   *
   * @param rowAppender {@link RowAppender}
   */
  public void setRowAppender(RowAppender<T> rowAppender) {
    if (nonNull(rowAppender)) {
      this.rowAppender = rowAppender;
    }
  }

  /** @return the {@link List} of plugins added to the table */
  public List<DataTablePlugin<T>> getPlugins() {
    return plugins;
  }

  /**
   * Run the {@link DataTablePlugin#onBeforeAddHeaders(DataTable)} for all plugin added to the data
   * table
   *
   * @param dataTable the {@link DataTable} initialized with this configuration
   */
  void onBeforeHeaders(DataTable<T> dataTable) {
    plugins.forEach(plugin -> plugin.onBeforeAddHeaders(dataTable));
  }

  /**
   * Run the {@link DataTablePlugin#onAfterAddHeaders(DataTable)} for all plugin added to the data
   * table
   *
   * @param dataTable the {@link DataTable} initialized with this configuration
   */
  void onAfterHeaders(DataTable<T> dataTable) {
    plugins.forEach(plugin -> plugin.onAfterAddHeaders(dataTable));
  }

  /** @return a {@link List} of all {@link ColumnConfig} added to the table */
  public List<ColumnConfig<T>> getColumns() {
    return columns;
  }

  /** @return a {@link List} of all currently visible {@link ColumnConfig} of the table */
  public List<ColumnConfig<T>> getVisibleColumns() {
    return columns.stream().filter(column -> !column.isHidden()).collect(Collectors.toList());
  }

  /**
   * get a column config by the column name
   *
   * @param name String name of the column
   * @return the {@link ColumnConfig} if exists otherwise throw {@link ColumnNofFoundException}
   */
  public ColumnConfig<T> getColumnByName(String name) {
    Optional<ColumnConfig<T>> first =
        getColumns().stream()
            .filter(columnConfig -> columnConfig.getName().equals(name))
            .findFirst();
    if (first.isPresent()) {
      return first.get();
    } else {
      throw new ColumnNofFoundException(name);
    }
  }

  /** @return the {@link DataTable} initialized with this configuration */
  public DataTable<T> getDataTable() {
    return dataTable;
  }

  /**
   * sets the dirty record handlers for editable tables
   *
   * @param dirtyRecordProvider {@link DirtyRecordProvider}
   * @param saveDirtyRecordHandler {@link SaveDirtyRecordHandler}
   * @return same TableConfig istance
   */
  public TableConfig<T> setDirtyRecordHandlers(
      DirtyRecordProvider<T> dirtyRecordProvider,
      SaveDirtyRecordHandler<T> saveDirtyRecordHandler) {
    this.dirtyRecordProvider = dirtyRecordProvider;
    this.saveDirtyRecordHandler = saveDirtyRecordHandler;

    return this;
  }

  /** @return the {@link DirtyRecordProvider} */
  DirtyRecordProvider<T> getDirtyRecordProvider() {
    return dirtyRecordProvider;
  }

  /** @return the {@link SaveDirtyRecordHandler} */
  SaveDirtyRecordHandler<T> getSaveDirtyRecordHandler() {
    return saveDirtyRecordHandler;
  }

  /**
   * An interface to provide an alternative implementation of how rows should be appended to the
   * table
   *
   * <p>e.g
   *
   * <p>The {@link org.dominokit.domino.ui.datatable.plugins.GroupingPlugin} defines an appender
   * that appends a row into the appropriate group instead of appending row sequentially
   *
   * @param <T> the type of the row record
   */
  @FunctionalInterface
  public interface RowAppender<T> {
    /**
     * Appends a row to the data table
     *
     * @param dataTable the {@link DataTable}
     * @param tableRow the {@link TableRow} being appended
     */
    void appendRow(DataTable<T> dataTable, TableRow<T> tableRow);
  }

  /**
   * This exception is thrown when performing action that looks up a column by its name but the
   * column does not exist in the current {@link TableConfig}
   */
  public static class ColumnNofFoundException extends RuntimeException {
    public ColumnNofFoundException(String name) {
      super(name);
    }
  }
}
