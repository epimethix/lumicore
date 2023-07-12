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
package com.github.epimethix.lumicore.swing.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class TreeUtils {
	public static void expandAll(TreePath parent, JTree tree) {
		TreeNode parentNode = (TreeNode) parent.getLastPathComponent();
		for (Enumeration<? extends TreeNode> nodes = parentNode.children(); nodes.hasMoreElements();) {
			TreeNode node = nodes.nextElement();
			TreePath tp = parent.pathByAddingChild(node);
			expandAll(tp, tree);
		}
		tree.expandPath(parent);
	}

	public static TreePath getTreePathFromNode(TreeNode node) {
		List<Object> nodes = new ArrayList<>();
		if (Objects.nonNull(node)) {
			nodes.add(node);
			while (Objects.nonNull(node = node.getParent())) {
				nodes.add(node);
			}
		}
		Collections.reverse(nodes);
		return new TreePath(nodes.toArray());
	}

	private TreeUtils() {}
}
