/*
 * Copyright 2022 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.swing.entityaccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;

@SuppressWarnings("serial")
final class TableDataModel extends AbstractTableModel implements DataModel, LabelsDisplayer {
	private List<?> data;
	private JTable table;
//	private final EntityAccessController controller;
	private String title;

	private Entity<?> selectedItem;

	private final Repository<?, ?> repository;

	TableDataModel(Repository<?, ?> repository) {
		data = new ArrayList<>();
		this.repository = repository;
//		this.controller = controller;
	}

	public void setTable(JTable table) {
		this.table = table;
	}

	Object getElementAt(int index) {
		return data.get(index);
	}

	@Override
	public Object select() {
		return selectedItem;
	}

	@Override
	public void select(Object item) {
		selectedItem = (Entity<?>) item;
	}

	@Override
	public void filterBy(Filter[] filters) {
		// TODO Auto-generated method stub
	}

	@Override
	public void refresh() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			private List<?> data = Collections.emptyList();

			@Override
			protected Void doInBackground() throws Exception {
				try {
					data = repository.selectAll();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void done() {
				TableDataModel.this.data = data;
				fireTableDataChanged();
				int selectedIndex = data.indexOf(selectedItem);
				if (selectedIndex > -1) {
					table.setRowSelectionInterval(selectedIndex, selectedIndex);
				}
			}
		};
		worker.execute();
	}

	@Override
	public void loadLabels() {
		title = C.getLabel(C.DATA_TABLE_TITLE);
		if (Objects.nonNull(table)) {
			table.getColumnModel().getColumn(0).setHeaderValue(title);
		}
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex).toString();
	}

	@Override
	public String getColumnName(int column) {
		return title;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
}
