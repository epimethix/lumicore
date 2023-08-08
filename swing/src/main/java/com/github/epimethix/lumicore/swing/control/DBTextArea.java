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
package com.github.epimethix.lumicore.swing.control;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

public class DBTextArea implements DBControl<String>, FocusListener {

	private final LTextArea control;

	private final JLabel label;

	private String initialValue;

	private final String fieldName;

	private final boolean required;

	private final JScrollPane scrollControl;

	private final String labelKey;

	private final TextComponentValidation textComponentValidation;

	private final Border defaultScrollPaneBorder;

	private Consumer<String> selectAction;

//	public DBTextArea(SwingUI ui, String labelKey, String fieldName) {
//		this(ui, labelKey, fieldName, false);
//	}
//
//	public DBTextArea(SwingUI ui, String labelKey, String fieldName, boolean required) {
//		this(ui, labelKey, fieldName, required, 5);
//	}
//
//	public DBTextArea(SwingUI ui, String labelKey, String fieldName, boolean required, int rows) {
//		this(ui, labelKey, fieldName, required, rows, 50);
//	}

	public DBTextArea(SwingUI ui, String labelKey, String fieldName, boolean required, int rows, int columns) {
		control = new LTextArea(rows, columns);
		control.addFocusListener(this);
		scrollControl = LayoutUtils.initScrollPane(control);
		defaultScrollPaneBorder = scrollControl.getBorder();
		this.required = required;
		label = new JLabel();
		textComponentValidation = new TextComponentValidation(ui, control, required);
		this.fieldName = fieldName;
		this.labelKey = labelKey;
		clear();
	}

	@Override
	public void loadLabels() {
		label.setText(LabelsManagerPool.getLabel(labelKey));
	}

	@Override
	public void setOrientation(boolean rtl) {
		if (rtl) {
			label.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			control.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			scrollControl.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		} else {
			label.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			control.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			scrollControl.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		}
	}

	public void setMinChars(int minChars) {
		textComponentValidation.setMinChars(minChars);
	}

	public void setMaxChars(int maxChars) {
		textComponentValidation.setMaxChars(maxChars);
	}

	public void setUCase() {
		textComponentValidation.setTextCase(TextComponentValidation.UCASE);
	}

	public void setLCase() {
		textComponentValidation.setTextCase(TextComponentValidation.LCASE);
	}

	@Override
	public boolean isValid() {
		boolean valid = textComponentValidation.validate(label.getText());
		if (!valid) {
			scrollControl.setBorder(BorderFactory.createLineBorder(Color.RED));
		}
		return valid;
	}

	@Override
	public void clear() {
		setValue(null);
	}

	@Override
	public void setValue(String value) {
		this.initialValue = value;
		control.setText(value);
		scrollControl.setBorder(defaultScrollPaneBorder);
	}

	@Override
	public String getValue() {
		String value = control.getText();
		if (value.trim().isEmpty()) {
			return null;
		} else {
			return value.trim();
		}
	}

	@Override
	public JComponent getControl() {
		return scrollControl;
	}

	@Override
	public JComponent getLabel() {
		return label;
	}

	@Override
	public String getInitialValue() {
		return initialValue;
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public void setInitialValue(String value) {
		this.initialValue = value;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isEmpty() {
		String value = getValue();
		if (Objects.isNull(value)) {
			return true;
		} else {
			return value.isEmpty();
		}
	}

	@Override
	public void requestFocus() {
		control.requestFocusInWindow();
	}

	@Override
	public void onSelect(Consumer<String> selectAction) {
		this.selectAction = selectAction;
	}

	@Override
	public void setEnabled(boolean enabled) {
		control.setEnabled(enabled);
	}

	@Override
	public void setEditable(boolean editable) {
		control.setEditable(editable);
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (e.getSource() == control) {
			scrollControl.setBorder(defaultScrollPaneBorder);
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		if(Objects.nonNull(selectAction)) {
			selectAction.accept(getValue());
		}
	}
}
