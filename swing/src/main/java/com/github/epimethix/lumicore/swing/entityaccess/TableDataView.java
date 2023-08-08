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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

@SuppressWarnings("serial")
class TableDataView extends AbstractDataView implements MouseListener, KeyListener, ComponentListener, ListSelectionListener{

	private final TableDataModel dataTableModel;
	private final JTable table;
	private final EntityAccessController controller;

	public TableDataView(Repository<?, ?> repository, EntityAccessController controller) {
		super(new BorderLayout());
		this.controller = controller;
		dataTableModel = new TableDataModel(repository);
		table = new JTable(dataTableModel);
//		table.setIntercellSpacing(new Dimension(UIUtils.MEDIUM_MARGIN, UIUtils.MEDIUM_MARGIN));
//		table.setRowHeight(table.getRowHeight()+UIUtils.MEDIUM_MARGIN*2);
		dataTableModel.setTable(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);
		table.addMouseListener(this);
		table.addKeyListener(this);
		final String ENTER_ACTION = "ENTER_ACTION";

		KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enterStroke, ENTER_ACTION);
		AbstractAction enterAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				editSelectedItem();
				System.out.println("SELECT TO EDIT " + controller.getEntityClass().getSimpleName());
			}
		};
		table.getActionMap().put(enterStroke, enterAction);
		add(LayoutUtils.initScrollPane(table), BorderLayout.CENTER);
		addComponentListener(this);
	}

	@Override
	public DataModel getModel() {
		return dataTableModel;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if((int)e.getKeyChar()==KeyEvent.VK_ENTER) {
			editSelectedItem();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == table) {
			int selRow = table.getSelectedRow();
			if (e.getClickCount() == 2 && selRow != -1) {
				editSelectedItem();
			}
		}
	}

	private void editSelectedItem() {
		Object x = dataTableModel.select();
		if (Objects.nonNull(x)) {
			controller.edit(x);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void componentResized(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
		dataTableModel.refresh();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex < 0 || e.getValueIsAdjusting()) {
			return;
		}
//		System.out.println(e.toString());
		dataTableModel.select(dataTableModel.getElementAt(selectedIndex));
	}
}
