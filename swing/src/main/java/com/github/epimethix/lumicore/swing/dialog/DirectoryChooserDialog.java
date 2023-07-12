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
package com.github.epimethix.lumicore.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.AnswerOption;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

public class DirectoryChooserDialog extends AbstractDialog {
	@SuppressWarnings("serial")
	private static class UIFile extends File {
		public UIFile(String pathname) {
			super(pathname);
		}

		@Override
		public String toString() {
			return getName().trim().isEmpty() ? getPath() : getName();
		}
	}

	@SuppressWarnings("serial")
	private static class DirectoryChooserDialogUI extends JPanel
			implements LabelsDisplayer, ActionListener, TreeSelectionListener, TreeWillExpandListener {
		private final FileFilter dirFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (showHidden) {
					return pathname.isDirectory();
				} else {
					return pathname.isDirectory() && !pathname.getName().startsWith(".");
				}
			}
		};

		private final Comparator<File> fileComparator = new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			}
		};

		private final File parentDir;
		private final JTree tree;
		private final JTextField tfPath;
//		private final JPanel message;
		private final JCheckBox ckShowHidden;
		private boolean showHidden;

		public DirectoryChooserDialogUI(String parentDir, String selectedDir) {
			super(new BorderLayout());
			File[] roots = File.listRoots();
			UIFile rootFile;
			if (Objects.isNull(parentDir) && roots.length == 1) {
				rootFile = new UIFile("/");
			} else if (Objects.nonNull(parentDir)) {
				rootFile = new UIFile(parentDir);
			} else {
				rootFile = null;
			}
			this.tfPath = new JTextField();
			this.parentDir = Objects.isNull(parentDir) ? null : rootFile;
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootFile);
			ckShowHidden = new JCheckBox("show hidden");// TODO string
			ckShowHidden.addActionListener(this);
			JPopupMenu popUp = new JPopupMenu();
			popUp.add(ckShowHidden);
			tree = new JTree(rootNode);
			tree.setRootVisible(Objects.nonNull(rootFile));
			tree.setRootVisible(Objects.nonNull(popUp));
			tree.setComponentPopupMenu(popUp);
			tree.addTreeWillExpandListener(this);
			tree.addTreeSelectionListener(this);
			tree.setBorder(LayoutUtils.createDefaultEmptyBorder());
			tfPath.setMargin(LayoutUtils.createDefaultTextMargin());
			tfPath.setText(selectedDir);
			tfPath.setEditable(false);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//			message = new JPanel(new BorderLayout());
			setPreferredSize(new Dimension((int) (screenSize.width * 0.3), (int) (screenSize.height * 0.6)));
			add(LayoutUtils.initScrollPane(tree), BorderLayout.CENTER);
			add(tfPath, BorderLayout.SOUTH);
			reloadTree();
//			return null;
		}

		private void reloadTree() {
			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
			rootNode.removeAllChildren();
			((DefaultTreeModel) tree.getModel()).reload();
			File rootFile = (File) rootNode.getUserObject();
			File[] childrenFiles;
			if (Objects.nonNull(rootFile)) {
				childrenFiles = rootFile.listFiles(dirFilter);
			} else {
				childrenFiles = File.listRoots();
			}
			if (Objects.nonNull(childrenFiles)) {
				Arrays.sort(childrenFiles, fileComparator);
				for (File f : childrenFiles) {
					rootNode.add(new DefaultMutableTreeNode(new UIFile(f.getPath())));
				}
			}

			tree.expandPath(new TreePath(rootNode));
			String selectedDir = tfPath.getText();
			if (Objects.nonNull(selectedDir) && !selectedDir.trim().isEmpty()) {
				String pathSeparator = FileSystems.getDefault().getSeparator();
				String[] path = selectedDir.split(pathSeparator);
				DefaultMutableTreeNode currentNode = rootNode;
				int i = Objects.isNull(parentDir) && Objects.nonNull(rootFile) ? 1 : 0;

				for (; i < path.length; i++) {
					Enumeration<TreeNode> children = currentNode.children();
					while (children.hasMoreElements()) {
						DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
						if (((File) child.getUserObject()).getName().equals(path[i])) {
							TreePath selectionPath = new TreePath(child.getPath());
							tree.expandPath(selectionPath);
							if (i + 1 == path.length) {
//								SwingUtilities.invokeLater(() -> {
								tree.setSelectionPath(selectionPath);
								tree.scrollPathToVisible(selectionPath);
//								});
							}
							currentNode = child;
							break;
						}
					}
				}
			}
		}

		private String getValue() {
			if (Objects.nonNull(parentDir) && !parentDir.toString().trim().isEmpty()) {
				return parentDir.toPath().resolve(tfPath.getText()).toString();
			}
			if(!tfPath.getText().trim().isEmpty()) {
				return tfPath.getText().trim();
			}
			return null;
		}

		private void setValue(String path) {

			if (Objects.isNull(parentDir)) {
				tfPath.setText(path);
			} else {
				int relativeIndex = parentDir.getPath().length() + 1;
				if (relativeIndex > path.length() || !path.startsWith(parentDir.getPath())) {
					tfPath.setText("");
				} else {
					tfPath.setText(path.substring(relativeIndex));
				}
			}
		}

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode selection = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (Objects.nonNull(selection)) {
				setValue(((File) selection.getUserObject()).getPath());
			}
		}

		@Override
		public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
			Enumeration<TreeNode> nodes = ((DefaultMutableTreeNode) event.getPath().getLastPathComponent()).children();
			while (nodes.hasMoreElements()) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
				if (node.isLeaf()) {
					File f = (File) node.getUserObject();
					File[] children = f.listFiles(dirFilter);
					if (Objects.nonNull(children)) {
						Arrays.sort(children, fileComparator);
						for (File c : children) {
							node.add(new DefaultMutableTreeNode(new UIFile(c.getPath())));
						}
					}
				}
			}
		}

		@Override
		public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == ckShowHidden) {
				showHidden = ckShowHidden.isSelected();
				reloadTree();
			}
		}

		@Override
		public void loadLabels() {

		}
	}

	private final DirectoryChooserDialogUI dcdUI;
	private final DialogUI dialogUI;
	private String result;
//	private final SwingUI swingUI;

	public DirectoryChooserDialog(Component parent, Supplier<String> title, String parentDir, String selectedDir) {
		super(parent, title, ICON_EDIT, new DirectoryChooserDialogUI(parentDir, selectedDir),
				AnswerOption.NEW_OK_CANCEL);
		this.dcdUI = (DirectoryChooserDialogUI) getUI();
		this.dialogUI = DialogUI.getDialogUI(this);
//		this.swingUI = swingUI;
	}

	public Optional<String> showDialog() {
		result = null;
		DialogUI.showDialogUI(dialogUI);
		return Optional.ofNullable(result);
	}

	@Override
	public void onAnswer(Answer answer, JDialog parent) {
		if (answer == Answer.NEW) {
			TreePath selectedNode = dcdUI.tree.getSelectionPath();
			if (Objects.nonNull(selectedNode)) {
//				for (Object sel : selectedNode.getPath()) {
//					System.err.println(((DefaultMutableTreeNode) sel).getUserObject().getClass());
//				}
				UIFile uif = (UIFile) ((DefaultMutableTreeNode) selectedNode.getLastPathComponent()).getUserObject();
				JTextField tfDirName = new JTextField();
				JLabel lbDirName = new JLabel(C.getLabel(C.CREATE_DIR_MESSAGE));
				JPanel pnMessage = new JPanel(new BorderLayout());
				pnMessage.add(lbDirName, BorderLayout.CENTER);
				pnMessage.add(tfDirName, BorderLayout.SOUTH);
				boolean continueCreateDir = JOptionPane.showConfirmDialog(SwingUtilities.getRoot(parent), pnMessage,
						C.getLabel(C.CREATE_DIR_MESSAGE_TITLE),
						JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
				if (continueCreateDir) {
					String dirName = tfDirName.getText().trim();
					if (dirName.isEmpty()) {
						JOptionPane.showMessageDialog(parent, C.getLabel(C.CREATE_DIR_ERROR_EMPTY_NAME),
								C.getLabel(C.CREATE_DIR_ERROR_EMPTY_NAME_TITLE),
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					File newDir = new File(uif, dirName);
					if (newDir.exists()) {
						JOptionPane.showMessageDialog(parent, C.getLabel(C.CREATE_DIR_ALREADY_EXISTS),
								C.getLabel(C.CREATE_DIR_ALREADY_EXISTS_TITLE),
								JOptionPane.ERROR_MESSAGE);
						return;
					} else {
						if (!newDir.mkdir()) {
							// TODO
							System.err.println("Could not create dir");
						} else {
							dcdUI.setValue(newDir.getPath());
							dcdUI.reloadTree();
						}
					}
				}

			}
		} else {
			if (answer == Answer.OK) {
				result = dcdUI.getValue();
			} else {
				result = null;
			}
			parent.setVisible(false);
		}
	}
}
