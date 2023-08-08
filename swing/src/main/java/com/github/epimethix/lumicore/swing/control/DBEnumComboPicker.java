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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;

import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;

public class DBEnumComboPicker<T extends Enum<T>> implements DBControl<T>, ActionListener {

	private final SwingUI ui;
	private final String fieldName;
	private final String labelKey;
//	private final Class<T> enumClass;
	private final boolean required;
	private final Function<T, String> constantToString;

	private final T[] enumConstants;
	private int initiallySelectedIndex;
	private int selectedIndex;

	private final JLabel label;
	private final JComboBox<String> control;
	private final Border defaultComboboxBorder;
	private Consumer<T> selectAction;

	public DBEnumComboPicker(SwingUI ui, String labelKey, String fieldName, boolean required, Class<T> enumClass) {
		this(ui, labelKey, fieldName, required, enumClass, t -> t.toString());
	}

	public DBEnumComboPicker(SwingUI ui, String labelKey, String fieldName, boolean required, Class<T> enumClass,
			Function<T, String> constantToString) {
		this.ui = ui;
		this.fieldName = fieldName;
		this.labelKey = labelKey;
//		this.enumClass = enumClass;
		this.required = required;
		this.constantToString = constantToString;
		enumConstants = enumClass.getEnumConstants();
		initiallySelectedIndex = -1;
		selectedIndex = -1;
		label = new JLabel();
		control = new JComboBox<>();
		control.addActionListener(this);
		defaultComboboxBorder = control.getBorder();
	}

	private int indexOf(T t) {
		if (Objects.nonNull(t)) {
			for (int i = 0; i < enumConstants.length; i++) {
				if (enumConstants[i].equals(t)) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public void loadLabels() {
		label.setText(LabelsManagerPool.getLabel(labelKey));
		if (Objects.nonNull(constantToString)) {
			List<String> enumLabels = new ArrayList<>();
			for (int i = 0; i < enumConstants.length; i++) {
				enumLabels.add(constantToString.apply(enumConstants[i]));
			}
			((DefaultComboBoxModel<String>) control.getModel()).removeAllElements();
			((DefaultComboBoxModel<String>) control.getModel()).addAll(enumLabels);
		} else {
			List<String> enumLabels = new ArrayList<>();
			for (int i = 0; i < enumConstants.length; i++) {
				enumLabels.add(enumConstants[i].name());
			}
			((DefaultComboBoxModel<String>) control.getModel()).removeAllElements();
			((DefaultComboBoxModel<String>) control.getModel()).addAll(enumLabels);
			
		}
	}

	@Override
	public String getFieldName() {
		return fieldName;
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
	public boolean isValid() {
		if (isEmpty() && required) {
			ui.showErrorMessage(control, C.FIELD_MAY_NOT_BE_EMPTY_ERROR, label.getText());
			control.setBorder(BorderFactory.createLineBorder(Color.RED));
			return false;
		}
		return true;
	}

	@Override
	public void clear() {
		setValue(null);
	}

	@Override
	public void setValue(T value) {
		selectedIndex = indexOf(value);
		initiallySelectedIndex = selectedIndex;
		control.setSelectedIndex(selectedIndex);
		control.setBorder(defaultComboboxBorder);
	}

	@Override
	public T getValue() {
		if (selectedIndex == -1) {
			return null;
		} else {
			return enumConstants[selectedIndex];
		}
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
	public T getInitialValue() {
		if (initiallySelectedIndex == -1) {
			return null;
		} else {
			return enumConstants[initiallySelectedIndex];
		}
	}

	@Override
	public void setInitialValue(T initialValue) {
		initiallySelectedIndex = indexOf(initialValue);
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isEmpty() {
		return selectedIndex == -1;
	}

	@Override
	public void onSelect(Consumer<T> selectAction) {
		this.selectAction = selectAction;
	}

	@Override
	public void requestFocus() {}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox<?>) {
			selectedIndex = control.getSelectedIndex();
			if (selectedIndex > -1) {
				control.setBorder(defaultComboboxBorder);
				if(Objects.nonNull(selectAction)) {
					selectAction.accept(getValue());
				}
			}
		}
	}
}
