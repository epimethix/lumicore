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

public interface DBControl<T> extends LabelsDisplayer {
	public static final int LABEL_TOP = 1;
	public static final int LABEL_LEFT = 2;

	public String getFieldName();

	public void setEnabled(boolean enabled);

	public void setEditable(boolean editable);

	public boolean isValid();

	public void clear();

	public void setValue(T value);

	public T getValue();

	public JComponent getControl();

	public JComponent getLabel();

	public T getInitialValue();

	public void setInitialValue(T initialValue);

	public boolean isRequired();

	public boolean isEmpty();

	default public boolean hasChanges() {
		T val = getValue();
		if (val instanceof BigDecimal) {
			return ((BigDecimal) val).compareTo((BigDecimal) getInitialValue()) != 0;
		} else {
			return !Objects.equals(getValue(), getInitialValue());
		}
	}

	default public void clearChanges() {
		setInitialValue(getValue());
	}

	public void requestFocus();

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


	default public void onSelect(Consumer<T> selectAction) {};
}
