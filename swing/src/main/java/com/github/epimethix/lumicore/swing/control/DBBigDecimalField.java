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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;

public class DBBigDecimalField implements DBControl<BigDecimal>, FocusListener {

	private final SwingUI ui;
	private final String labelkey;
	private final String fieldName;
	private final boolean required;
	private BigDecimal initialValue;

	private final JLabel label;
	private final JFormattedTextField formattedTextField;
	private final NumberFormat format;
	private Consumer<BigDecimal> selectAction;

	public DBBigDecimalField(SwingUI ui, String labelKey, String fieldName, int decimalPlacesAfterComma,
			RoundingMode roundingMode) {
		this(ui, labelKey, fieldName, decimalPlacesAfterComma, roundingMode, false);
	}

	public DBBigDecimalField(SwingUI ui, String labelKey, String fieldName, int decimalPlacesAfterComma,
			RoundingMode roundingMode, boolean required) {
		this.ui = ui;
		this.labelkey = labelKey;
		this.fieldName = fieldName;
		this.required = required;
		this.label = new JLabel();
//		this.decimalPlacesAfterComma = decimalPlacesAfterComma
		format = NumberFormat.getInstance();
		format.setRoundingMode(roundingMode);
		format.setMaximumFractionDigits(decimalPlacesAfterComma);
		format.setMinimumFractionDigits(decimalPlacesAfterComma);
		format.setGroupingUsed(false);
		formattedTextField = new JFormattedTextField(format);
		formattedTextField.addFocusListener(this);
		clear();
	}

	@Override
	public void loadLabels() {
		label.setText(LabelsManagerPool.getLabel(labelkey));
		formattedTextField.setLocale(Locale.getDefault());

	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public void setEnabled(boolean enabled) {
		label.setEnabled(enabled);
		formattedTextField.setEnabled(enabled);
	}

	@Override
	public void setEditable(boolean editable) {
		formattedTextField.setEditable(editable);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void clear() {
		setValue(null);
	}

	@Override
	public void setValue(BigDecimal value) {
		if (Objects.isNull(value)) {
			value = new BigDecimal("0.0");
		}
		setInitialValue(value);
		formattedTextField.setValue(value.doubleValue());
	}

	@Override
	public BigDecimal getValue() {
		try {
			Number n = format.parse(formattedTextField.getText());
			return new BigDecimal(n.doubleValue());
		} catch (Exception e) {
			System.err.println(formattedTextField.getText());
			e.printStackTrace();
		}
		return new BigDecimal("0.0");
	}

	@Override
	public JComponent getControl() {
		return formattedTextField;
	}

	@Override
	public JComponent getLabel() {
		return label;
	}

	@Override
	public BigDecimal getInitialValue() {
		return initialValue;
	}

	@Override
	public void setInitialValue(BigDecimal initialValue) {
		this.initialValue = initialValue;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isEmpty() {
		return getValue().compareTo(new BigDecimal("0.0")) == 0;
	}

	@Override
	public void requestFocus() {
		formattedTextField.requestFocusInWindow();
	}

	@Override
	public void onSelect(Consumer<BigDecimal> selectAction) {
		this.selectAction = selectAction;
	}

	@Override
	public void focusGained(FocusEvent e) {}

	@Override
	public void focusLost(FocusEvent e) {
		if (Objects.nonNull(selectAction)) {
			selectAction.accept(getValue());
		}
	}

	public void setBigDecimal(BigDecimal valuate) {
		formattedTextField.setText(
				String.format(String.format("%%.%df", format.getMaximumFractionDigits()), valuate.doubleValue()));
	}
}
