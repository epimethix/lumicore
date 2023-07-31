/*
 * Copyright 2023 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.devtools.gui.diagram.dialog;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.AnswerOption;
import com.github.epimethix.lumicore.common.ui.labels.displayer.IgnoreLabels;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.devtools.gui.D;
import com.github.epimethix.lumicore.devtools.gui.DevToolsGUIController;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.Diagram;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;
import com.github.epimethix.lumicore.swing.dialog.Dialog;
import com.github.epimethix.lumicore.swing.dialog.DialogUI;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

@SwingComponent
public class ClassSelectorDialog implements ActionListener, LabelsDisplayer, Dialog {
	private final static class UIClass implements Comparable<UIClass> {
		private final String className;
		private final String simpleName;
		private final String packageName;

		public UIClass(String className) {
			this.className = className;
			if (className.contains(".")) {
				simpleName = className.substring(className.lastIndexOf(".") + 1);
			} else {
				simpleName = className;
			}
			if (className.contains(".")) {
				packageName = className.substring(0, className.lastIndexOf('.'));
			} else {
				packageName = "";
			}
		}

//		public String getClassName() {
//			return className;
//		}

		public String getPackageName() {
			return packageName;
		}

		@Override
		public String toString() {
			return simpleName;
		}

		@Override
		public int compareTo(UIClass o) {
			return className.compareTo(o.className);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((className == null) ? 0 : className.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UIClass other = (UIClass) obj;
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			return true;
		}
	} // End of class UIClass

	private static final class UIClassModel implements ListModel<UIClass> {

		private List<UIClass> data = new ArrayList<>();

		private final List<ListDataListener> listeners = new ArrayList<>();

		@Override
		public int getSize() {
			return data.size();
		}

		@Override
		public UIClass getElementAt(int index) {
			return data.get(index);
		}

		public UIClass removeElementAt(int index) {
			return data.remove(index);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}

		public void setData(List<UIClass> data) {
			this.data = new ArrayList<>(data);
			refresh();
		}

		private void fireDataChanged() {
			ListDataEvent lde = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
			for (ListDataListener l : listeners) {
				l.contentsChanged(lde);
			}
		}

		public void refresh() {
			Collections.sort(data);
			fireDataChanged();
		}

		public void removeAll(List<UIClass> uiClasses) {
			data.removeAll(uiClasses);
		}
	} // End of class UIClassModel

	public static final class UIPackage implements Comparable<UIPackage> {
		private final File sourcesDirectory;
		private final File packageDirectory;
		private final String packageName;

		/**
		 * @param sourcesDirectory
		 * @param packageDirectory
		 */
		public UIPackage(File sourcesDirectory, File packageDirectory) {
			this.sourcesDirectory = sourcesDirectory;
			this.packageDirectory = packageDirectory;
			if (sourcesDirectory.getPath().equals(packageDirectory.getPath())) {
				packageName = "Default Package";
			} else if ('\\' == File.separatorChar) {
				packageName = packageDirectory.getPath().substring(sourcesDirectory.getPath().length() + 1)
						.replaceAll("[\\\\]", ".");
			} else {
				packageName = packageDirectory.getPath().substring(sourcesDirectory.getPath().length() + 1)
						.replaceAll("[/]", ".");
			}
		}

		@Override
		public int compareTo(UIPackage o) {
			return packageDirectory.getPath().compareTo(o.packageDirectory.getPath());
		}

		@Override
		public String toString() {
			return packageName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((packageDirectory == null) ? 0 : packageDirectory.hashCode());
			result = prime * result + ((sourcesDirectory == null) ? 0 : sourcesDirectory.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UIPackage other = (UIPackage) obj;
			if (packageDirectory == null) {
				if (other.packageDirectory != null)
					return false;
			} else if (!packageDirectory.equals(other.packageDirectory))
				return false;
			if (sourcesDirectory == null) {
				if (other.sourcesDirectory != null)
					return false;
			} else if (!sourcesDirectory.equals(other.sourcesDirectory))
				return false;
			return true;
		}

		public String getPackageName() {
			return packageName;
		}
	} // End of class UIPackage

	private final DefaultComboBoxModel<UIPackage> cmPackages;
	private final JComboBox<UIPackage> cbPackages;
	private final UIClassModel lmPackageClasses;
	@IgnoreLabels
	private final JList<UIClass> lsPackageClasses;
	@IgnoreLabels
	private final UIClassModel lmSelectedClasses;
	private final JList<UIClass> lsSelectedClasses;

	private final JLabel lbClasses;
	private final JLabel lbSelected;
	private final JButton btSelect;
	private final JButton btSelectAll;
	private final JButton btUnSelect;
	private final JButton btUnSelectAll;

	private final JPanel pnEditor;

	private final DialogUI dialogUI;

	private final Component parent;

	private Diagram diagram;

	public ClassSelectorDialog(DevToolsGUIController guiController) {
		this.parent = guiController.getFrame();
		cmPackages = new DefaultComboBoxModel<>();
		cbPackages = new JComboBox<>(cmPackages);
		cbPackages.addActionListener(this);
		lmPackageClasses = new UIClassModel();
		lsPackageClasses = new JList<>(lmPackageClasses);
		lsPackageClasses.setBorder(LayoutUtils.createDefaultEmptyBorder());
		lmSelectedClasses = new UIClassModel();
		lsSelectedClasses = new JList<>(lmSelectedClasses);
		lsSelectedClasses.setBorder(LayoutUtils.createDefaultEmptyBorder());
		lbClasses = new JLabel();
		lbSelected = new JLabel();
		btSelect = new JButton();
		btSelect.addActionListener(this);
		btSelectAll = new JButton();
		btSelectAll.addActionListener(this);
		btUnSelect = new JButton();
		btUnSelect.addActionListener(this);
		btUnSelectAll = new JButton();
		btUnSelectAll.addActionListener(this);
		JPanel pnSelectionButtons = new JPanel(new GridLayout(4, 1));
		pnSelectionButtons.add(btSelectAll);
		pnSelectionButtons.add(btSelect);
		pnSelectionButtons.add(btUnSelect);
		pnSelectionButtons.add(btUnSelectAll);
		pnEditor = new JPanel(new GridBagLayout());
		pnEditor.setBorder(LayoutUtils.createDefaultEmptyBorder());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.weightx = c.weighty = 0.0;
		c.insets = LayoutUtils.createDefaultMargin();
		c.fill = GridBagConstraints.HORIZONTAL;
		pnEditor.add(lbClasses, c);
		c.gridx += 2;
		pnEditor.add(lbSelected, c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		pnEditor.add(cbPackages, c);
		c.gridy++;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = c.weighty = 1.0;
		pnEditor.add(LayoutUtils.initScrollPane(lsPackageClasses), c);
		c.gridx++;
		c.fill = GridBagConstraints.NONE;
		c.weightx = c.weighty = 0.0;
		pnEditor.add(pnSelectionButtons, c);
		c.gridx++;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = c.weighty = 1.0;
		pnEditor.add(LayoutUtils.initScrollPane(lsSelectedClasses), c);

		this.dialogUI = DialogUI.getDialogUI(this);
	}

	public String[] getPackageNames(File codeDirectory) {
		List<UIPackage> uiPackages = listPackages(codeDirectory);
		String[] packageNames = new String[uiPackages.size()];
		int i = 0;
		for (UIPackage p : uiPackages) {
			packageNames[i++] = p.toString();
		}
//		Arrays.sort(packageNames);
		return packageNames;
	}

	public static List<UIPackage> listPackages(File codeDirectory) {
		List<UIPackage> packages = new ArrayList<>();
		loadPackages(codeDirectory, codeDirectory, packages);
		Collections.sort(packages);
		return packages;
	}

	public static void loadPackages(File codeDirectory, File subDirectory, List<UIPackage> packages) {
		File[] javaFiles = subDirectory.listFiles(f -> f.isFile() && f.getName().endsWith(".java"));
		if (Objects.nonNull(javaFiles) && javaFiles.length > 0) {
			packages.add(new UIPackage(codeDirectory, subDirectory));
		}
		File[] dirs = subDirectory.listFiles(f -> f.isDirectory());
		if (Objects.nonNull(dirs) && dirs.length > 0) {
			for (File dir : dirs) {
				loadPackages(codeDirectory, dir, packages);
			}
		}
	}

	private List<UIClass> loadClasses(UIPackage pkg) {
		File[] javaFiles = pkg.packageDirectory.listFiles(f -> f.isFile() && f.getName().endsWith(".java"));
		String packageName = pkg.toString();
		List<UIClass> uiClasses = new ArrayList<>();
		for (File javaFile : javaFiles) {
			if (packageName.length() > 0) {
				uiClasses.add(new UIClass(
						String.format("%s.%s", packageName, javaFile.getName().replaceAll("[.]java$", ""))));
			} else {
				uiClasses.add(new UIClass(javaFile.getName().replaceAll("[.]java$", "")));
			}
		}
		return uiClasses;
	}

	public final int showDialog(File codeDirectory, Diagram diagram) {
		cmPackages.removeAllElements();
		cmPackages.addAll(listPackages(codeDirectory));
		if (cmPackages.getSize() > 0) {
			cbPackages.setSelectedIndex(0);
		}
		this.diagram = diagram;
		setSelectedClasses(diagram.getClassNames());
		DialogUI.showDialogUI(dialogUI);
		return 0;
	}

	public final void setSelectedClasses(List<String> classes) {
		List<UIClass> uiClasses = new ArrayList<>();
		for (String className : classes) {
			uiClasses.add(new UIClass(className));
		}
		lmSelectedClasses.setData(uiClasses);
	}

	public final List<String> getSelectedClasses() {
		List<String> list = new ArrayList<>();
		for (UIClass uic : lmSelectedClasses.data) {
			list.add(uic.className);
		}
		return list;
	}

	@Override
	public void loadLabels() {
		btSelect.setText(D.getLabel(D.BUTTON_SELECT));
		btSelectAll.setText(D.getLabel(D.BUTTON_SELECT_ALL));
		btUnSelect.setText(D.getLabel(D.BUTTON_UN_SELECT));
		btUnSelectAll.setText(D.getLabel(D.BUTTON_UN_SELECT_ALL));
		lbClasses.setText(D.getLabel(D.LABEL_CLASS_SELECTOR_CLASSES));
		lbSelected.setText(D.getLabel(D.LABEL_CLASS_SELECTOR_SELECTED));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btSelect) {
			int[] indices = lsPackageClasses.getSelectedIndices();
			select(indices);
		} else if (e.getSource() == btSelectAll) {
			int[] allIndices = new int[lmPackageClasses.getSize()];
			for (int i = 0; i < allIndices.length; i++) {
				allIndices[i] = i;
			}
			select(allIndices);
		} else if (e.getSource() == btUnSelect) {
			int[] indices = lsSelectedClasses.getSelectedIndices();
			deSelect(indices);
		} else if (e.getSource() == btUnSelectAll) {
			int[] allIndices = new int[lmSelectedClasses.getSize()];
			for (int i = 0; i < allIndices.length; i++) {
				allIndices[i] = i;
			}
			deSelect(allIndices);
		} else if (e.getSource() == cbPackages) {
			loadPackageClasses();
		}
	}

	private void loadPackageClasses() {
		UIPackage packageName = (UIPackage) cbPackages.getSelectedItem();
		if (Objects.nonNull(packageName)) {
			List<UIClass> packageClasses = loadClasses(packageName);
			lmPackageClasses.setData(packageClasses);
			hideSelectedInSourceList();
			lmPackageClasses.refresh();
			lsPackageClasses.setSelectedIndices(new int[] {});
		}
	}

	/**
	 * After loading the package classes, the classes that are selected in the
	 * target list are hidden from the source list.
	 */
	private void hideSelectedInSourceList() {
		for (UIClass uic : lmSelectedClasses.data) {
			if (lmPackageClasses.data.contains(uic)) {
				lmPackageClasses.data.remove(uic);
			}
		}
	}

	private void select(int[] indices) {
		if (Objects.nonNull(indices) && indices.length > 0) {
			List<UIClass> uiClasses = new ArrayList<>();
			for (int i : indices) {
				uiClasses.add(lmPackageClasses.getElementAt(i));
			}
			lmPackageClasses.removeAll(uiClasses);
			lmSelectedClasses.data.addAll(uiClasses);
			lmSelectedClasses.refresh();
			lmPackageClasses.refresh();
			lsPackageClasses.setSelectedIndices(new int[] {});
		}
	}

	private void deSelect(int[] indices) {
		if (Objects.nonNull(indices) && indices.length > 0) {
			List<UIClass> uiClasses = new ArrayList<>();
			for (int i : indices) {
				uiClasses.add(lmSelectedClasses.getElementAt(i));
			}
			for (int i = indices.length - 1; i > -1; i--) {
				lmSelectedClasses.removeElementAt(indices[i]);
			}
			for (UIClass uic : uiClasses) {
				String pkgName = uic.getPackageName();
				if (pkgName.equals(cbPackages.getSelectedItem().toString())) {
					lmPackageClasses.data.add(uic);
				}
			}
			lmSelectedClasses.refresh();
			lmPackageClasses.refresh();
			lsSelectedClasses.setSelectedIndices(new int[] {});
		}
	}

	@Override
	public Component getParent() {
		return parent;
	}

	@Override
	public Component getUI() {
		return pnEditor;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getTitle() {
		return D.getLabel(D.LABEL_CLASS_SELECTOR_TITLE);
	}

	@Override
	public Answer[] getAnswerOptions() {
		return AnswerOption.SAVE_CANCEL.getAnswers();
	}

	@Override
	public Answer getDefaultAnswer() {
		return Answer.CANCEL;
	}

	@Override
	public void onAnswer(Answer answer, JDialog parent) {
		if (answer == Answer.SAVE) {
			diagram.setClasses(getSelectedClasses());
		}
		parent.setVisible(false);
	}
}
