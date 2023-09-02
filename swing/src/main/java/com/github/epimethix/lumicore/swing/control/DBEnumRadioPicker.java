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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.LabeledEnum;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;
import com.github.epimethix.lumicore.swing.util.GridBagUtils;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

public class DBEnumRadioPicker<T extends Enum<T>> implements DBControl<T>, ActionListener {

	private final SwingUI ui;
	private final String fieldName;
	private final String labelKey;
	private final boolean required;
	private final Function<T, String> constantToString;

	private T[] enumConstants;
	private JRadioButton[] radioButtons;
	private final ButtonGroup buttonGroup;
	private int initiallySelectedIndex;
	private int selectedIndex;

	private final JLabel label;
	private final JPanel control;
	private Consumer<T> selectAction;
	private final int cols;

	private Color currentBorderColor;

	public DBEnumRadioPicker(SwingUI ui, String labelKey, String fieldName, boolean required, Class<T> enumClass,
			int cols) {
		this(ui, labelKey, fieldName, required, enumClass,
				LabeledEnum.class.isAssignableFrom(enumClass) ? t -> ((LabeledEnum) t).screenName() : t -> t.name(),
				cols);
	}

	@SuppressWarnings("serial")
	public DBEnumRadioPicker(SwingUI ui, String labelKey, String fieldName, boolean required, Class<T> enumClass,
			Function<T, String> constantToString, int cols) {
		this.ui = ui;
		this.fieldName = fieldName;
		this.labelKey = labelKey;
		this.required = required;
		this.constantToString = constantToString;
		radioButtons = new JRadioButton[0];
		buttonGroup = new ButtonGroup();
		initiallySelectedIndex = -1;
		selectedIndex = -1;
		label = new JLabel();
		control = new JPanel(new BorderLayout()) {
			@Override
			public void updateUI() {
				super.updateUI();
				DBEnumRadioPicker.this.updateUI();
			}
		};
		currentBorderColor = control.getBackground();
		control.setBorder(BorderFactory.createLineBorder(currentBorderColor));
		this.cols = cols;
		buildControl(enumClass.getEnumConstants());
	}

	private void updateUI() {
		if (currentBorderColor != Color.RED) {
			if (Objects.nonNull(control)) {
				currentBorderColor = control.getBackground();
				updateBorder();
			}
		}
	}

	private void buildControl(T[] ts) {
		for (JRadioButton rb : radioButtons) {
			rb.removeActionListener(this);
			buttonGroup.remove(rb);
		}
		this.enumConstants = ts;
		radioButtons = new JRadioButton[ts.length];
		for (int i = 0; i < radioButtons.length; i++) {
			radioButtons[i] = new JRadioButton();
			radioButtons[i].addActionListener(this);
			buttonGroup.add(radioButtons[i]);
		}
		JPanel controlButtons = new JPanel(new GridBagLayout());
		GridBagConstraints c = GridBagUtils.initGridBagConstraints();
		Insets rightMargin = LayoutUtils.createDefaultRightMargin();
		Insets topMargin = LayoutUtils.createDefaultTopMargin();
		Insets topRightMargin = LayoutUtils.createDefaultTopRightMargin();
		Insets noMargin = LayoutUtils.createDefaultEmptyMargin();
		int col = 0;
		int row = 0;
		for (int i = 0; i < radioButtons.length; i++) {
			col++;
			if (col == cols || (row == 0 && col == radioButtons.length)) {
				if (row == 0) {
					c.insets = noMargin;
				} else {
					c.insets = topMargin;
				}
				col = 0;
				row++;
			} else if (row == 0) {
				c.insets = rightMargin;
			} else {
				c.insets = topRightMargin;
			}
			controlButtons.add(radioButtons[i], c);
			if (col == 0) {
				c.gridx = 0;
				c.gridy++;
			} else {
				c.gridx++;
			}
		}
		control.removeAll();
		control.add(controlButtons, BorderLayout.WEST);
	}

	private void updateBorder() {
		if (Objects.nonNull(control)) {
			control.setBorder(BorderFactory.createLineBorder(currentBorderColor));
		}
	}

	private int indexOf(JRadioButton rb) {
		if (Objects.nonNull(rb)) {
			for (int i = 0; i < radioButtons.length; i++) {
				if (radioButtons[i].equals(rb)) {
					return i;
				}
			}
		}
		return -1;
	}

	public void setVisibleEnumConstants(T... ts) {
		buildControl(ts);
	}

	public int indexOf(T t) {
		if (Objects.nonNull(t)) {
			for (int i = 0; i < enumConstants.length; i++) {
				if (enumConstants[i].equals(t)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 
	 * @param startIndex inclusive
	 * @param endIndex   exclusive
	 */
	public final void setEnabled(int startIndex, int endIndex, boolean enabled) {
		for (int i = startIndex; i < endIndex; i++) {
			radioButtons[i].setEnabled(enabled);
		}
	}

	public final void setEnabled(T t, boolean enabled) {
		radioButtons[indexOf(t)].setEnabled(enabled);
	}

	public final void setEnabled(int index, boolean enabled) {
		radioButtons[index].setEnabled(enabled);
	}

	public final void disablePrevious(T t) {
		disablePrevious(indexOf(t));
	}

	public final void disablePrevious(int i) {
		setEnabled(0, i, false);
		setEnabled(i, radioButtons.length, true);
	}

	public final void disableAfter(T t) {
		disableAfter(indexOf(t));
	}

	public final void disableAfter(int i) {
		setEnabled(0, i, true);
		setEnabled(i + 1, radioButtons.length, false);
	}

	@Override
	public void loadLabels() {
		label.setText(LabelsManagerPool.getLabel(labelKey));
		for (int i = 0; i < radioButtons.length; i++) {
			if (Objects.nonNull(constantToString)) {
				radioButtons[i].setText(constantToString.apply(enumConstants[i]));
			} else {
				radioButtons[i].setText(enumConstants[i].name());
			}
		}
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public void setEnabled(boolean enabled) {
		label.setEnabled(enabled);
		for (JRadioButton rb : radioButtons) {
			rb.setEnabled(enabled);
		}
	}

	@Override
	public void setEditable(boolean editable) {
		setEnabled(editable);
	}

	@Override
	public boolean isValid() {
		if (isEmpty() && required) {
			ui.showErrorMessage(control, C.FIELD_MAY_NOT_BE_EMPTY_ERROR, label.getText());
			currentBorderColor = Color.RED;
			updateBorder();
			requestFocus();
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
		if (selectedIndex == -1) {
			buttonGroup.clearSelection();
		} else {
			radioButtons[selectedIndex].setSelected(true);
		}
		currentBorderColor = control.getBackground();
		updateBorder();
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
	public void requestFocus() {
		control.requestFocusInWindow();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JRadioButton) {
			selectedIndex = indexOf((JRadioButton) e.getSource());
			if (selectedIndex > -1) {
				currentBorderColor = control.getBackground();
				updateBorder();
				if (Objects.nonNull(selectAction)) {
					selectAction.accept(getValue());
				}
			}
		}
	}
}
