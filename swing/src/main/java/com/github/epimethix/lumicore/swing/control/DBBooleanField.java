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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;

import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

public class DBBooleanField implements DBControl<Boolean>, FocusListener, ActionListener {
	private final JLabel label;
	private final JCheckBox control;
	private Boolean initialValue;
	private final String fieldName;
	private final boolean required;
	private final String labelKey;
	private final SwingUI ui;
	private final Border defaultTextFieldBorder;
	private Consumer<Boolean> selectAction;

	public DBBooleanField(SwingUI ui, String labelKey, String fieldName) {
		this(ui, labelKey, fieldName, false);
	}

	public DBBooleanField(SwingUI ui, String labelKey, String fieldName, boolean required) {
		this.ui = ui;
		this.labelKey = labelKey;
		this.fieldName = fieldName;
		this.required = required;
		label = new JLabel();
		control = new JCheckBox();
		control.setMargin(LayoutUtils.createDefaultTextMargin());
		control.addFocusListener(this);
		defaultTextFieldBorder = control.getBorder();
		control.addActionListener(this);
		clear();
	}

	@Override
	public void loadLabels() {
		label.setText(LabelsManagerPool.getLabel(labelKey));
	}

	@Override
	public boolean isValid() {
		if (required && !control.isSelected()) {
			ui.showErrorMessage(control, C.BOOLEAN_FIELD_MUST_BE_CHECKED, label.getText());
			control.setBorder(BorderFactory.createLineBorder(Color.RED));
			return false;
		}
		return true;
	}

	@Override
	public Boolean getValue() {
		return control.isSelected();
	}

	@Override
	public void clear() {
		setValue(null);
	}

	@Override
	public void setValue(Boolean value) {
		setInitialValue(value);
		if (Objects.isNull(value) || value.equals(false)) {
			control.setSelected(false);
		} else {
			control.setSelected(true);
		}
		control.setBorder(defaultTextFieldBorder);
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
	public Boolean getInitialValue() {
		return initialValue;
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public void setInitialValue(Boolean value) {
		this.initialValue = Objects.isNull(value) ? Boolean.FALSE : value;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isEmpty() {
		return !control.isSelected();
	}

	@Override
	public void requestFocus() {
		control.requestFocusInWindow();
	}

	@Override
	public void setEnabled(boolean enabled) {
		label.setEnabled(enabled);
		control.setEnabled(enabled);
	}

	@Override
	public void setEditable(boolean editable) {
		control.setEnabled(editable);
	}

	@Override
	public void onSelect(Consumer<Boolean> selectAction) {
		this.selectAction = selectAction;
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (e.getSource() == control) {
			control.setBorder(defaultTextFieldBorder);
		}
	}

	@Override
	public void focusLost(FocusEvent e) {

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == control) {
			if(Objects.nonNull(selectAction)) {
				selectAction.accept(getValue());
			}
		}
	}
}
