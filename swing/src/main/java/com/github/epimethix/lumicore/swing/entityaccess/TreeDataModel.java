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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.TreeEntity;
import com.github.epimethix.lumicore.common.orm.model.TreeTop;

@SuppressWarnings("serial")
class TreeDataModel extends DefaultTreeModel implements DataModel {
	private JTree tree;
	private final Repository<?, ?> repository;
	private final  Map<TreeEntity<?, ?>, DefaultMutableTreeNode> entityNodeMap;
	private TreeEntity<?, ?> selectedItem;
//	private TreePath selectedPath;

//	private EntityAccessController controller;

	public TreeDataModel(Repository<?, ?> repository) {
		super(new DefaultMutableTreeNode(new TreeTop()));
		this.repository = repository;
		this.entityNodeMap = new HashMap<>();
	}

	public void setTree(JTree tree) {
		this.tree = tree;
	}

	@Override
	public Object select() {
		return selectedItem;
	}

	@Override
	public void filterBy(Filter[] filters) {
		// TODO Auto-generated method stub
	}

	@Override
	public void refresh() {
		reload();
	}

	@Override
	public void reload() {
//		System.err.println("reload");
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getRoot();
		rootNode.removeAllChildren();
		entityNodeMap.clear();
		super.reload();
		final Object userObject = rootNode.getUserObject();
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			private List<?> rootChildren;

			@Override
			protected Void doInBackground() throws Exception {
				try {
					rootChildren = repository.childrenOf((TreeEntity<?, ?>) userObject);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void done() {
				int i =0 ;
				for (Object child : rootChildren) {
//					rootNode.add(new DefaultMutableTreeNode(child));
					DefaultMutableTreeNode node = new DefaultMutableTreeNode(child);
					entityNodeMap.put((TreeEntity<?, ?>) child, node);
					TreeDataModel.this.insertNodeInto(node, rootNode, i++);
				}
//				int[] indices = new int[rootChildren.size()];
//				for (int i = 0; i < indices.length; i++) {
//					indices[i] = i;
//				}
//				TreeDataModel.this.nodesWereInserted(rootNode, indices);
				SwingUtilities.invokeLater(() -> tree.expandPath(new TreePath(rootNode)));
			}
		};
		worker.execute();
//		super.reload();
	}

	@Override
	public void select(Object o) {
//		System.err.println("select " + (o == null ? "null" : o.toString()));
		this.selectedItem = (TreeEntity<?, ?>) o;
//		this.selectedPath = tree.getSelectionPath();
	}

//	private boolean skipNextPreload;

	public void preload(DefaultMutableTreeNode treeNode) {
//		System.err.print("preload: ");
//		System.out.println(treeNode.getUserObject().toString());
//		if (treeNode.getUserObject().toString().equals("Top")) {
//			System.out.println(treeNode.getUserObject().toString());
//		}
//		if (skipNextPreload) {
//			skipNextPreload = false;
//			return;
//		}
		Enumeration<TreeNode> parents = treeNode.children();
		Map<TreeEntity<?, ?>, TreeNode> nodeMap = new HashMap<>();
		List<TreeEntity<?, ?>> parentsList = new ArrayList<>();
		while (parents.hasMoreElements()) {
			TreeNode parent = parents.nextElement();
			if (parent.isLeaf()) {
				TreeEntity<?, ?> parentEntity = (TreeEntity<?, ?>) ((DefaultMutableTreeNode) parent).getUserObject();
				parentsList.add(parentEntity);
				nodeMap.put(parentEntity, parent);
			}
		}

		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			private Map<TreeEntity<?, ?>, List<?>> result = new HashMap<>();

			@Override
			protected Void doInBackground() throws Exception {
				for (TreeEntity<?, ?> parentEntity : parentsList) {
					List<?> children;
					try {
						children = repository.childrenOf(parentEntity);
						result.put(parentEntity, children);
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
				return null;
			}

			@Override
			protected void done() {
				for (TreeEntity<?, ?> parent : parentsList) {
					DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) nodeMap.get(parent);
					List<?> children = result.get(parent);
					if (Objects.nonNull(children) && Objects.nonNull(parentNode)) {
						int i =0 ;
						for (Object child : children) {
//							parentNode.add(new DefaultMutableTreeNode(child));
							TreeDataModel.this.insertNodeInto(new DefaultMutableTreeNode(child), parentNode, i++);
						}
					}
				}
			}
		};
		worker.execute();
	}
//	public  <T extends DBCascade> void setData(List<T> data) {
//		DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
//		root.removeAllChildren();
//		UIUtils.populateTree(root, data, null);
//		reload();
//		
//	}
}
