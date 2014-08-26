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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Michał Rudewicz
 */
public class EnumComboBoxModel<T extends Enum<T>> implements ComboBoxModel<T> {

    private T selection;
    private final  T[] selections;

    @SuppressWarnings("unchecked")
    public EnumComboBoxModel(T selection) {
        this.selection = selection;
        selections=((Class<T>)selection.getClass()).getEnumConstants();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setSelectedItem(Object anItem) {
        try {
            selection = (T) anItem;
        } catch (ClassCastException ex) {
        }
    }

    @Override
    public Object getSelectedItem() {
        return selection;
    }

    @Override
    public int getSize() {
        return selections.length;
    }

    @Override
    public T getElementAt(int index) {
        return selections[index];
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }

}
