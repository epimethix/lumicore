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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.TreeEntity;
import com.github.epimethix.lumicore.common.orm.model.TreeTop;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

@SuppressWarnings("serial")
public class TreeDataView extends AbstractDataView
		implements TreeWillExpandListener, TreeSelectionListener, MouseListener, ComponentListener {
	private final EntityAccessController controller;
//	private final Repository<?, ?> repository; 
	private final TreeDataModel treeDataModel;
	private final JTree tree;
	private final TreeDataContextMenu treeDataContextMenu;

	public TreeDataView(Repository<?, ?> repository, EntityAccessController controller) {
		super(new BorderLayout());
//		this.repository = repository;
		this.controller = controller;
		treeDataModel = new TreeDataModel(repository);
		tree = new JTree(treeDataModel);
		treeDataModel.setTree(tree);
		tree.addTreeWillExpandListener(this);
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(this);
		treeDataContextMenu = new TreeDataContextMenu(tree, controller);
//		tree.setComponentPopupMenu(treeDataContextMenu);
		tree.setBorder(LayoutUtils.createDefaultEmptyBorder());
		add(LayoutUtils.initScrollPane(tree), BorderLayout.CENTER);
		addComponentListener(this);
	}

	@Override
	public DataModel getModel() {
		return treeDataModel;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreeEntity<?, ?> selectedItem = (TreeEntity<?, ?>) ((DefaultMutableTreeNode) e.getPath().getLastPathComponent())
				.getUserObject();
		treeDataModel.select(selectedItem);
	}

	@Override
	public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
//		System.err.println("treeWillExpand " + event.getSource().toString());
		DefaultMutableTreeNode treeNode = ((DefaultMutableTreeNode) event.getPath().getLastPathComponent());
		treeDataModel.preload(treeNode);
	}

	@Override
	public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
//		System.err.println("treeWillCollapse " + event.getSource().toString());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == tree) {
			DefaultMutableTreeNode selection = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (e.getClickCount() == 2 && Objects.nonNull(selection)) {
				if (!(selection.getUserObject() instanceof TreeTop)) {
					controller.edit(selection.getUserObject());
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getSource() == tree) {
			if (SwingUtilities.isRightMouseButton(e)) {
				TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
				tree.setSelectionPath(treePath);
				if (Objects.nonNull(treePath)) {
					treeDataContextMenu.show(tree, e.getX(), e.getY(),
							(TreeEntity<?, ?>) ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject());
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void componentResized(ComponentEvent e) {}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentShown(ComponentEvent e) {
		treeDataModel.refresh();
	}

	@Override
	public void componentHidden(ComponentEvent e) {}
}
