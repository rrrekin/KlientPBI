/*
 * Copyright 2014 Michał Rudewicz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.prv.rrrekin.pbi.models;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pl.prv.rrrekin.pbi.PbiBookEntry;

/**
 * <p>
 * SearchResultModel - Swing table model for search results.</p>
 *
 * @author Michał Rudewicz
 * @version $Id: $Id
 */
public class SearchResultModel implements TableModel {

    private List<PbiBookEntry> backingList;
    private ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();
    private static final ResourceBundle guiTexts = ResourceBundle.getBundle("pl/prv/rrrekin/pbi/gui");
    private Log logger = LogFactory.getLog(this.getClass());

    /**
     * <p>
     * Constructor for SearchResultModel.</p>
     *
     * @param backingList a {@link java.util.List} object.
     */
    public SearchResultModel(List<PbiBookEntry> backingList) {
        this.backingList = backingList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
        if (backingList == null) {
            return 0;
        } else {
            return backingList.size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return guiTexts.getString("AUTHOR");
        } else if (columnIndex == 1) {
            return guiTexts.getString("TITLE");
        } else {
            return guiTexts.getString("ID");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if(columnIndex==2)return Integer.class;
        return String.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PbiBookEntry entry = backingList.get(rowIndex);
        if (columnIndex == 0) {
            return entry.getAuthor();
        } else if(columnIndex == 1){
            return entry.getTitle();
        } else {
            return entry.getId();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    /**
     * <p>
     * Getter for the field <code>backingList</code>.</p>
     *
     * @return the backingList
     */
    public List<PbiBookEntry> getBackingList() {
        return backingList;
    }

    /**
     * <p>
     * Setter for the field <code>backingList</code>.</p>
     *
     * @param backingList the backingList to set
     */
    public void setBackingList(List<PbiBookEntry> backingList) {
        this.backingList = backingList;

        tableChanged();
    }

    /**
     * <p>
     * Notify all listeners that table contents has been changed.</p>
     */
    public void tableChanged() {
        for (TableModelListener l : listeners) {
            logger.debug("Notifying " + l.toString());
            l.tableChanged(new TableModelEvent(this));
        }
    }

}
