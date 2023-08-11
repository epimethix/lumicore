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
import java.awt.CardLayout;
import java.awt.ComponentOrientation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.github.epimethix.lumicore.common.ui.labels.displayer.IgnoreLabels;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;
import com.github.epimethix.lumicore.swing.util.TreeUtils;

@SuppressWarnings("serial")
public abstract class AbstractEntityAccessBrowser extends JPanel implements LabelsDisplayer, TreeSelectionListener {
	private final static class UIClass implements LabelsDisplayer {
		private final Class<?> cls;
		private final String labelId;
		private String label;

		private UIClass(Class<?> cls, String labelId) {
			this.cls = cls;
			this.labelId = labelId;
		}

		@Override
		public String toString() {
			return label;
		}

		@Override
		public void loadLabels() {
			label = LabelsManagerPool.getLabel(labelId);
		}
	}

//	private final Map<Class<? extends Entity<?>>, Boolean> isInitializedMap = new HashMap<>();
	private final List<LabelsDisplayer> labelsDisplayers;
	private final JPanel view;
	private final CardLayout cardLayout;
	private final JTree menuTree;
	private final JScrollPane menuTreeScrollPane;
	private final JSplitPane splitPane;
	private final Map<Class<?>, EntityAccessController> entityAccessControllerMap = new HashMap<>();
	@IgnoreLabels
	protected EntityAccessController currentView;
	private DefaultMutableTreeNode currentNode;
	private boolean ignoreNextTreeSelectionEvent;

	protected abstract void buildMenuTree(DefaultMutableTreeNode rootNode);

	public AbstractEntityAccessBrowser() {
		super(new BorderLayout());
		this.view = new JPanel(cardLayout = new CardLayout());
		labelsDisplayers = new ArrayList<>();
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
		menuTree = new JTree(rootNode);
		menuTree.setRootVisible(false);
		menuTree.addTreeSelectionListener(this);
		menuTree.setBorder(LayoutUtils.createDefaultEmptyBorder());
		buildMenuTree(rootNode);
		if(Objects.nonNull(currentView)) {
			currentView.refresh();
		}
		splitPane = new JSplitPane();
		menuTreeScrollPane = LayoutUtils.initScrollPane(menuTree);
		splitPane.setLeftComponent(menuTreeScrollPane);
		splitPane.setRightComponent(view);
		add(splitPane, BorderLayout.CENTER);
//		UIUtils.expandAll(new TreePath(rootNode), menuTree);
	}

	protected DefaultMutableTreeNode addNode(String labelId) {
		return addNode(labelId, (DefaultMutableTreeNode) menuTree.getModel().getRoot());
	}

	protected DefaultMutableTreeNode addNode(String labelId, DefaultMutableTreeNode parentNode) {
		UIClass uiClass = new UIClass(null, labelId);
		labelsDisplayers.add(uiClass);
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(uiClass);
		parentNode.add(newNode);
		return newNode;
	}

	protected void addEntityAccessView(String labelId, Class<?> entityClass) {
		addEntityAccessView(labelId, entityClass, (DefaultMutableTreeNode) menuTree.getModel().getRoot());
	}

	protected void addEntityAccessView(String labelId, Class<?> entityClass, DefaultMutableTreeNode parentNode) {
		EntityAccessController eav = EntityAccessControllerFactory.getEntityAccessController(entityClass);
		view.add(eav.getView(), entityClass.getName());
		UIClass uiClass = new UIClass(entityClass, labelId);
		labelsDisplayers.add(uiClass);
		labelsDisplayers.add(eav);
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(uiClass);
		parentNode.add(newNode);
		entityAccessControllerMap.put(entityClass, eav);
		if(Objects.isNull(currentView)) {
			currentView = eav;
			currentNode = newNode;
		}
	}

	@Override
	public void loadLabels() {
		((DefaultTreeModel) menuTree.getModel()).reload();
		TreeUtils.expandAll(new TreePath(menuTree.getModel().getRoot()), menuTree);
		menuTree.revalidate();
	}

	@Override
	public void setOrientation(boolean rtl) {
		if(rtl) {
			applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
//			splitPane.setResizeWeight(0.01);
			splitPane.setRightComponent(menuTreeScrollPane);
			splitPane.setLeftComponent(view);
		} else {
			applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
//			splitPane.setResizeWeight(0.01);
			splitPane.setLeftComponent(menuTreeScrollPane);
			splitPane.setRightComponent(view);
		}
//		splitPane.setResizeWeight(0.0);
		splitPane.setEnabled(true);
	}
	
	public boolean clear() {
		if(Objects.nonNull(currentView)) {
			boolean b = currentView.getEditor().clear();
			if(b) {
				currentView.showDataView();
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		if(ignoreNextTreeSelectionEvent) {
			ignoreNextTreeSelectionEvent = false;
			return;
		}
		if(Objects.nonNull(currentView)) {
			if(!currentView.getEditor().clear()) {
				System.err.println(e.getSource().toString());
				ignoreNextTreeSelectionEvent = true;
				menuTree.setSelectionPath(new TreePath(currentNode));
				return;
			}
			currentView.showDataView();
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
		Object userObject = node.getUserObject();
		if (userObject instanceof UIClass) {
			Class<?> entityClass = ((UIClass) userObject).cls;
			if (Objects.nonNull(entityClass)) {
				cardLayout.show(view, entityClass.getName());
				EntityAccessController eav = entityAccessControllerMap.get(entityClass);
				currentNode = node;
				currentView = eav;
				if(Objects.nonNull(eav)) {
					eav.refresh();
				}
			}
		}
	}

	public boolean hasChanges() {
		if(Objects.nonNull(currentView)) {
			return currentView.getEditor().hasChanges();
		}
		return false;
	}
}
