/*
 * Copyright 2022-2023 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.common.swing;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;

/**
 * This interface defines a database control (user input control) for automated
 * use in database entity editors.
 * 
 * @author epimethix
 *
 * @param <T> the field type this control manages
 * 
 */
public interface DBControl<T> extends LabelsDisplayer {
	/**
	 * Constant to indicate that the controls label should be displayed above the
	 * control (using grid width = 2).
	 */
	public static final int LABEL_TOP = 1;
	/**
	 * Constant to indicate that the controls label should be displayed left to the
	 * control.
	 */
	public static final int LABEL_LEFT = 2;

	/**
	 * Gets the field name of the table column this value is for.
	 * 
	 * @return the table field name.
	 */
	public String getFieldName();

	/**
	 * Sets the control dis/enabled.
	 * 
	 * @param enabled true to enable, false to disable the control.
	 */
	public void setEnabled(boolean enabled);

	/**
	 * Sets the control un/editable.
	 * 
	 * @param editable false to deactivate the control and block user input /
	 *                 manipulation, true to enable editing (default).
	 */
	public void setEditable(boolean editable);

	/**
	 * Tests if the user input is valid.
	 * 
	 * @return true if the input is valid, false if the input is not valid.
	 */
	public boolean isValid();

	/**
	 * Clears (empties or sets a default value to) this control.
	 */
	public void clear();

	/**
	 * Sets the field value when loading a record from the database. The
	 * implementing class should call {@link #setInitialValue(Object)} using the
	 * input argument value.
	 * 
	 * @param value the value to load.
	 */
	public void setValue(T value);

	/**
	 * Gets the user input to be set to the entity {@code Object}.
	 * 
	 * @return the field value.
	 */
	public T getValue();

	/**
	 * Gets the input control {@code Component}.
	 * 
	 * @return the user input {@code Control}
	 */
	public JComponent getControl();

	/**
	 * Gets the label for this control.
	 * 
	 * @return the label
	 */
	public JComponent getLabel();

	/**
	 * Gets the initial value.
	 * 
	 * @return either the default/empty value after clearing or the value as
	 *         persisted after loading.
	 */
	public T getInitialValue();

	/**
	 * Sets the initial value. The implementation class should call this method from
	 * {@link #setValue(Object)}!
	 * 
	 * @param initialValue the initial value
	 */
	public void setInitialValue(T initialValue);

	/**
	 * Checks if the field is required.
	 * 
	 * @return true if the field may not be empty.
	 */
	public boolean isRequired();

	/**
	 * Tests if the input control has any content.
	 * 
	 * @return true if there was no user input after clearing.
	 */
	public boolean isEmpty();

	/**
	 * Tests for changes by comparing inital and current value.
	 * 
	 * @return true if the current value is unequal to the initial value.
	 */
	default public boolean hasChanges() {
		T val = getValue();
		if (val instanceof BigDecimal) {
			return ((BigDecimal) val).compareTo((BigDecimal) getInitialValue()) != 0;
		} else {
			return !Objects.equals(getValue(), getInitialValue());
		}
	}

	/**
	 * Invalidates changes by setting the initial value to the current value.
	 */
	default public void clearChanges() {
		setInitialValue(getValue());
	}

	/**
	 * The implementation class should make its (first) control request the focus
	 * here.
	 */
	public void requestFocus();

	/**
	 * Gets the labels text.
	 * 
	 * @return the control labels text.
	 */
	public default String getLabelText() {
		JComponent label = getLabel();
		if (label instanceof JTextComponent) {
			JTextComponent tc = (JTextComponent) label;
			return tc.getText();
		} else if (label instanceof JLabel) {
			return ((JLabel) getLabel()).getText();
		}
		System.err.println(getClass().getSimpleName() + " should override DBControl.getLabelText()!!!");
		return "#ERROR: Could not resolve label text#";
	}

	/**
	 * Put a custom action when a value is selected (NOOP by default)
	 * 
	 * @param selectAction a custom action to perform when a value is selected by
	 *                     the user.
	 */
	default public void onSelect(Consumer<T> selectAction) {}
}
