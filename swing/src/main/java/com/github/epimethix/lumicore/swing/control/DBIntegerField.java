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
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.text.NumberFormatter;

import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;
import com.github.epimethix.lumicore.swing.control.IntegerComponentValidation.IntegerError;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

public class DBIntegerField implements DBControl<Long>, FocusListener {
	private final JLabel label;
	private final JFormattedTextField control;
	private Long initialValue;
	private final String fieldName;
	private final boolean required;
	private final String labelKey;
	private final NumberFormatter numberFormatter;
	private final IntegerComponentValidation validation;
	private final SwingUI ui;
	private final Border defaultTextFieldBorder;
	private Consumer<Long> selectAction;

	public DBIntegerField(SwingUI ui, String labelKey, String fieldName) {
		this(ui, labelKey, fieldName, false);
	}

	public DBIntegerField(SwingUI ui, String labelKey, String fieldName, boolean required) {
		this.ui = ui;
		this.labelKey = labelKey;
		this.fieldName = fieldName;
		this.required = required;
		label = new JLabel();
		NumberFormat f = NumberFormat.getIntegerInstance();
		numberFormatter = new NumberFormatter(f);
		numberFormatter.setValueClass(Long.class);
		numberFormatter.setAllowsInvalid(false);
		validation = new IntegerComponentValidation();
		control = new JFormattedTextField(numberFormatter);
		control.setMargin(LayoutUtils.createDefaultTextMargin());
		control.addFocusListener(this);
		defaultTextFieldBorder = control.getBorder();
		clear();
	}

	public final void setGroupingUsed(boolean group) {
		((NumberFormat) numberFormatter.getFormat()).setGroupingUsed(group);
	}

	public boolean isAllowNull() {
		return validation.isAllowNull();
	}

	public void setAllowNull(boolean allowNull) {
		validation.setAllowNull(allowNull);
	}

	public boolean isAllowZero() {
		return validation.isAllowZero();
	}

	public void setAllowZero(boolean allowZero) {
		validation.setAllowZero(allowZero);
	}

	public boolean isAllowNegative() {
		return validation.isAllowNegative();
	}

	public void setAllowNegative(boolean allowNegative) {
		validation.setAllowNegative(allowNegative);
	}

	public boolean isAllowPositive() {
		return validation.isAllowPositive();
	}

	public void setAllowPositive(boolean allowPositive) {
		validation.setAllowPositive(allowPositive);
	}

	public Long getMaxValue() {
		return validation.getMaxValue();
	}

	public void setMaxValue(Long maxValue) {
		validation.setMaxValue(maxValue);
	}

	public Long getMinValue() {
		return validation.getMinValue();
	}

	public void setMinValue(Long minValue) {
		validation.setMinValue(minValue);
	}

	@Override
	public void loadLabels() {
		label.setText(LabelsManagerPool.getLabel(labelKey));
	}
//
//	@Override
//	public void setOrientation(boolean rtl) {
//		if (rtl) {
////			System.out.println("Integer field - setOrientation(rtl)");
//			label.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
//			control.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
//		} else {
////			System.out.println("Integer field - setOrientation(ltr)");
//			label.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
//			control.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
//		}
//	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public boolean isValid() {
		IntegerError e = validation.validate(getValue());
		if (!e.equals(IntegerError.NONE)) {
			switch (e) {
			case VALUE_IS_NULL:
			case VALUE_IS_ZERO:
				ui.showErrorMessage(control, C.INTEGER_ERROR_MESSAGE_VALUE_IS_EMPTY, label.getText());
				break;
			case VALUE_IS_NEGATIVE:
				ui.showErrorMessage(control, C.INTEGER_ERROR_MESSAGE_VALUE_IS_NEGATIVE, label.getText());
				break;
			case VALUE_IS_POSITIVE:
				ui.showErrorMessage(control, C.INTEGER_ERROR_MESSAGE_VALUE_IS_POSITIVE, label.getText());
				break;
			case VALUE_IS_GREATER_THAN_MAX:
				ui.showErrorMessage(control, C.INTEGER_ERROR_MESSAGE_VALUE_IS_GREATER_THAN_MAX, label.getText(),
						validation.getMaxValue());
				break;
			case VALUE_IS_LESS_THAN_MIN:
				ui.showErrorMessage(control, C.INTEGER_ERROR_MESSAGE_VALUE_IS_LESS_THAN_MIN, label.getText(),
						validation.getMinValue());
				break;
			}
			control.setBorder(BorderFactory.createLineBorder(Color.RED));
			return false;
		}
		return true;
	}

	@Override
	public Long getValue() {
		try {
			Long value = (Long) control.getValue();
			if ((Objects.nonNull(value) && value.equals(0L))) {
				if (validation.isAllowZero()) {
					return value;
				} else {
					return null;
				}
			} else {
				return value;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0L;
		}
	}

	@Override
	public void clear() {
		setValue(null);
	}

	@Override
	public void setValue(Long value) {
		setInitialValue(value);
		control.setValue(Objects.isNull(value) ? 0L : value);
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
	public Long getInitialValue() {
		return initialValue;
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public void setInitialValue(Long value) {
		if (Objects.isNull(value) && validation.isAllowZero()) {
			this.initialValue = 0L;
		} else {
			this.initialValue = value;
		}
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isEmpty() {
		Long value = getValue();
		if (Objects.isNull(value) || (value.equals(0L) && !validation.isAllowZero())) {
			return true;
		} else {
			return false;
		}
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
		control.setEditable(editable);
	}

	@Override
	public void onSelect(Consumer<Long> selectAction) {
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

		if(Objects.nonNull(selectAction)) {
			selectAction.accept(getValue());
		}
	}

	/**
	 * Calling this method is equivalent to mutation through user input.
	 * 
	 * @param i the value to set to the control directly
	 */
	public void setInteger(long i) {
		control.setValue(i);
	}

	public void addActionListener(ActionListener l) {
		control.addActionListener(l);
	}
}
