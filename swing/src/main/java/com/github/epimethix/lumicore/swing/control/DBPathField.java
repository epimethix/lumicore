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
package com.github.epimethix.lumicore.swing.control;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.apache.commons.lang3.SystemUtils;

import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayerPool;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;
import com.github.epimethix.lumicore.swing.dialog.DirectoryChooserDialog;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

public class DBPathField implements DBControl<Path>, ActionListener {

	public enum Selector {
		FILE, DIRECTORY
	}

	private final SwingUI ui;
	private final Selector selector;
	private final String labelKey;
	private final String fieldName;
	private final boolean required;
	private final Path parentDir;

	private final JLabel label;
	private final JPanel control;

	private final JButton btOpenSelector;

	private final JTextField tfPath;

	private Path selectedPath;

	private Path initialValue;

	private final Border defaultTextFieldBorder;

	public DBPathField(SwingUI ui, String labelKey, String fieldName, boolean required, Selector selector,
			Path parent) {
		this.ui = ui;
		this.selector = selector;
		this.labelKey = labelKey;
		this.fieldName = fieldName;
		this.required = required;
		this.parentDir = parent;
		label = new JLabel();
		control = new JPanel(new GridBagLayout());
		btOpenSelector = new JButton();
		btOpenSelector.addActionListener(this);
		tfPath = new JTextField();
		tfPath.setEditable(false);
		defaultTextFieldBorder = tfPath.getBorder();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		control.add(tfPath, c);
		c.insets = LayoutUtils.createDefaultLeftMargin();
		c.gridx++;
		c.weightx = 0.0;
		control.add(btOpenSelector, c);

	}

	@Override
	public void loadLabels() {
		label.setText(LabelsManagerPool.getLabel(labelKey));
		btOpenSelector.setText(C.getLabel(C.BUTTON_CHOOSE));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btOpenSelector) {
			if (selector == Selector.DIRECTORY) {
				DirectoryChooserDialog dcd;
				Supplier<String> title = () -> Objects.nonNull(labelKey) && labelKey.trim().length() > 0
						? C.getLabel(C.DIR_CHOOSER_DIALOG_TITLE_FOR, LabelsManagerPool.getLabel(labelKey))
						: C.getLabel(C.DIR_CHOOSER_DIALOG_TITLE);
				if (Objects.nonNull(parentDir)) {
					String parentDirStr = parentDir.toString();
					dcd = new DirectoryChooserDialog(SwingUtilities.getRoot(btOpenSelector), title, parentDirStr,
							parentDirStr);
				} else if (!tfPath.getText().trim().isEmpty()) {
					String selectedDir = tfPath.getText().trim();
					dcd = new DirectoryChooserDialog(SwingUtilities.getRoot(btOpenSelector), title, null, selectedDir);
				} else {
					String selectedDir = SystemUtils.getUserHome().getPath();
					dcd = new DirectoryChooserDialog(SwingUtilities.getRoot(btOpenSelector), title, null, selectedDir);
				}
				LabelsDisplayerPool.loadLabels(dcd);
				Optional<String> answer = dcd.showDialog();
				LabelsDisplayerPool.removeLabelsDisplayers(dcd);
				if (answer.isPresent()) {
					String dir = answer.get();
					selectedPath = Path.of(dir);
					tfPath.setText(dir);
					tfPath.setBorder(defaultTextFieldBorder);
				}
			} else {
				JFileChooser fc = new JFileChooser();
				if (Objects.nonNull(parentDir)) {
					fc.setCurrentDirectory(parentDir.toFile());
				}
				int answer = fc.showOpenDialog(SwingUtilities.getRoot(btOpenSelector));
				if (answer == JFileChooser.APPROVE_OPTION) {
					File selection = fc.getSelectedFile();
					tfPath.setText(selection.getPath());
					selectedPath = selection.toPath();
					tfPath.setBorder(defaultTextFieldBorder);
				}
			}
		}
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public void setEnabled(boolean enabled) {
		btOpenSelector.setEnabled(enabled);
	}

	@Override
	public void setEditable(boolean editable) {
		btOpenSelector.setEnabled(editable);
	}

	@Override
	public boolean isValid() {
		if (isEmpty()) {
			if (required) {
				ui.showErrorMessage(control, C.FIELD_MAY_NOT_BE_EMPTY_ERROR, label.getText());
				tfPath.setBorder(BorderFactory.createLineBorder(Color.RED));
				return false;
			} else {
				return true;
			}
		}
		if (Objects.nonNull(parentDir) && !selectedPath.startsWith(parentDir)) {
			ui.showErrorMessage(control, C.PATH_FIELD_ERROR_WRONG_PARENT, label.getText(), parentDir.toString());
			tfPath.setBorder(BorderFactory.createLineBorder(Color.RED));
			return false;
		}
		return true;
	}

	@Override
	public void clear() {
		setValue(null);
	}

	@Override
	public void setValue(Path value) {
		selectedPath = value;
		initialValue = value;
		if (Objects.nonNull(value)) {
			tfPath.setText(value.toString());
		} else {
			tfPath.setText("");
		}
		tfPath.setBorder(defaultTextFieldBorder);
	}

	@Override
	public Path getValue() {
		return selectedPath;
	}

	@Override
	public JComponent getControl() {
		return control;
	}

	@Override
	public JComponent getLabel() {
		return label;
	}

	@Override
	public Path getInitialValue() {
		return initialValue;
	}

	@Override
	public void setInitialValue(Path initialValue) {
		this.initialValue = initialValue;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isEmpty() {
		return Objects.isNull(selectedPath);
	}

	@Override
	public void requestFocus() {
		btOpenSelector.requestFocusInWindow();
	}

}
