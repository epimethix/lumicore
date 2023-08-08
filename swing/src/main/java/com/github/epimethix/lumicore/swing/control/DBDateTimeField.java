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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;
import com.github.epimethix.lumicore.swing.util.GridBagUtils;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

public class DBDateTimeField implements DBControl<LocalDateTime>, KeyListener, ActionListener, FocusListener {
	private final String fieldName;
	private final String labelKey;
	private final JLabel label;
	private final LTextField textField;
	private LocalDateTime initialValue;
	private final DateTimeFormatter formatter;
	private final String formatString;
	private final int[] nonPatternLettersIndexes;
	private final char[] nonPatternLetters;
	private final int formatLength;
	private final JButton btOpenPicker;
	private final JPanel control;
	private final boolean required;
	private final Border defaultTextFieldBorder;
	private final SwingUI ui;
	private Consumer<LocalDateTime> selectAction;

	public DBDateTimeField(SwingUI ui, String labelKey, String fieldName, boolean required, String formatString) {
		this.ui = ui;
		this.labelKey = labelKey;
		this.fieldName = fieldName;
		this.formatString = formatString;
		this.formatLength = formatString.length();
		formatter = DateTimeFormatter.ofPattern(formatString);
		List<Integer> nonPatternLettersIndexIntegers = new ArrayList<>();
		int size = formatString.length();
		for (int i = 0; i < size; i++) {
			String letter = formatString.substring(i, i + 1);
			if (!DateTime.PATTERN_LETTER_REGEX.matcher(letter).find()) {
				nonPatternLettersIndexIntegers.add(i);
			}
		}
		nonPatternLettersIndexes = new int[nonPatternLettersIndexIntegers.size()];
		nonPatternLetters = new char[nonPatternLettersIndexIntegers.size()];
		for (int i = 0; i < nonPatternLettersIndexes.length; i++) {
			int index = nonPatternLettersIndexIntegers.get(i);
			nonPatternLettersIndexes[i] = index;
			nonPatternLetters[i] = formatString.charAt(index);
		}
		textField = new LTextField();
		textField.addFocusListener(this);
		textField.addKeyListener(this);
		defaultTextFieldBorder = textField.getBorder();
		btOpenPicker = new JButton();
		btOpenPicker.addActionListener(this);
		control = new JPanel(new GridBagLayout());
		setEditable(true);
		label = new JLabel();
		this.required = required;
	}

	@Override
	public void loadLabels() {
		label.setText(LabelsManagerPool.getLabel(labelKey));
		btOpenPicker.setText(C.getLabel(C.BUTTON_CHOOSE));
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public void setEnabled(boolean enabled) {
		textField.setEnabled(enabled);
		btOpenPicker.setEnabled(enabled);
	}

	@Override
	public void setEditable(boolean editable) {
		control.removeAll();
		textField.setEditable(editable);
		GridBagConstraints c = GridBagUtils.initGridBagConstraints();
		if (editable) {
			c.insets = LayoutUtils.createDefaultRightMargin();
			c.weightx = 1.0;
			control.add(textField, c);
			c.gridx++;
			c.insets = LayoutUtils.createDefaultEmptyMargin();
			c.weightx = 0.0;
			control.add(btOpenPicker, c);
		} else {
			c.insets = LayoutUtils.createDefaultEmptyMargin();
			c.weightx = 1.0;
			control.add(textField, c);
		}
		control.revalidate();
	}

	@Override
	public boolean isValid() {
		LocalDateTime value = null;
		try {
			value = LocalDateTime.parse(textField.getText(), formatter);
		} catch (Exception e) {}
		if (Objects.nonNull(value)) {
			return true;
		} else if (DateTime.NULL_DATE_REGEX.matcher(textField.getText()).matches()) {
			if (required) {
				ui.showErrorMessage(control, C.FIELD_MAY_NOT_BE_EMPTY_ERROR, label.getText());
			} else {
				return true;
			}
		} else {
			ui.showErrorMessage(control, C.DATE_VALIDATION_ERROR, formatString, label.getText());
		}
		return false;
	}

	@Override
	public void clear() {
		setValue(null);
	}

	@Override
	public void setValue(LocalDateTime value) {
		this.initialValue = value;
		if (Objects.nonNull(value)) {
			textField.setText(value.format(formatter));
		} else {
			textField.setText("---");
		}
		textField.setBorder(defaultTextFieldBorder);
	}

	@Override
	public LocalDateTime getValue() {
		LocalDateTime value = null;
		try {
			value = LocalDateTime.parse(textField.getText(), formatter);
		} catch (Exception e) {}
		return value;
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
	public LocalDateTime getInitialValue() {
		return initialValue;
	}

	@Override
	public void setInitialValue(LocalDateTime initialValue) {
		this.initialValue = initialValue;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isEmpty() {
		return Objects.isNull(getValue());
	}

	@Override
	public void requestFocus() {
		textField.requestFocusInWindow();
	}

	@Override
	public void onSelect(Consumer<LocalDateTime> selectAction) {
		this.selectAction = selectAction;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getSource() == textField) {
			DateTime.autoPutNonPatternLetter(e, textField, nonPatternLettersIndexes, nonPatternLetters, formatLength);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btOpenPicker) {
			// TODO Open DatePicker
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (e.getSource() == textField) {
			textField.setBorder(defaultTextFieldBorder);
			textField.select(0, textField.getText().length());
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (e.getSource() == textField) {
			if (DateTime.NULL_DATE_REGEX.matcher(textField.getText()).matches()) {
				if (required) {
					textField.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			} else {
				LocalDateTime value = null;
				try {
					value = LocalDateTime.parse(textField.getText(), formatter);
				} catch (Exception e1) {}
				if (Objects.isNull(value)) {
					textField.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			}
			if(Objects.nonNull(selectAction)) {
				selectAction.accept(getValue());
			}
		}
	}
}
