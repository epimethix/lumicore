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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

import com.github.epimethix.lumicore.common.orm.model.TreeEntity;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;

@SuppressWarnings("serial")
class TreeDataContextMenu extends JPopupMenu implements LabelsDisplayer, ActionListener {

	private final JMenuItem miNew;
	private final JMenuItem miDelete;
	private final JMenuItem miEdit;

//	private final JTree tree;
	private final EntityAccessController controller;
	private TreeEntity<?, ?> context;

	public TreeDataContextMenu(JTree tree, EntityAccessController controller) {
		this.miNew = new JMenuItem();
		this.miNew.addActionListener(this);
		add(miNew);
		this.miEdit = new JMenuItem();
		this.miEdit.addActionListener(this);
		add(miEdit);
		this.miDelete = new JMenuItem();
		this.miDelete.addActionListener(this);
		if (controller.isDeletable()) {
			add(miDelete);
		}
//		this.tree = tree;
		this.controller = controller;

	}

	public void show(Component invoker, int x, int y, TreeEntity<?, ?> context) {
		this.context = context;
		super.show(invoker, x, y);
	}

	@Override
	public void loadLabels() {
		miNew.setText(C.getLabel(C.TREE_MENU_NEW));
		miDelete.setText(C.getLabel(C.TREE_MENU_DELETE));
		miEdit.setText(C.getLabel(C.TREE_MENU_EDIT));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == miNew) {
//			Object selection = treeDataModel.getSelectedItem();
//			treeDataModel.newItem(selection);
			if(Objects.nonNull(context)) {
				controller.create(context);
			}
		} else if (e.getSource() == miEdit) {
//			Object x = treeDataModel.getSelectedItem();
			Object x = controller.getSelectedItem();
			if (Objects.nonNull(x)) {
//				treeDataModel.edit(x);
				controller.edit(x);
			}
		} else if (e.getSource() == miDelete) {
			Object x = controller.getSelectedItem();
			if (Objects.nonNull(x)) {
				controller.delete(x);
			}
		}
	}

}
