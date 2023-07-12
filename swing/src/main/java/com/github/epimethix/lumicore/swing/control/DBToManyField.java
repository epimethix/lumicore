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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.common.swing.SwingUI;

public class DBToManyField<E extends Entity<?>> implements DBControl<List<E>>, ActionListener {

	private final Class<E> entityClass;
	private final String fieldName;
	private final JButton control;
	private final JLabel label;
	private final boolean required;
	private final String labelKey;

	private final SwingUI ui;

	private Set<E> value;
	private Set<E> initialValue;

	public DBToManyField(SwingUI ui, String labelKey, String fieldName, boolean required, Class<E> entityClass,
			Comparator<E> comparator) {
		this.entityClass = entityClass;
		this.ui = ui;
		this.fieldName = fieldName;
		this.required = required;
		this.labelKey = labelKey;
		this.label = new JLabel();
		this.control = new JButton();
		this.control.addActionListener(this);
	}

	@Override
	public void loadLabels() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getFieldName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setValue(List<E> value) {
		this.value = new LinkedHashSet<>(value);
		this.initialValue = new LinkedHashSet<>(value);
	}

	@Override
	public List<E> getValue() {
		return new ArrayList<>(value);
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
	public List<E> getInitialValue() {
		return new ArrayList<>(initialValue);
	}

	@Override
	public void setInitialValue(List<E> initialValue) {
		this.initialValue = new LinkedHashSet<>(initialValue);
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isEmpty() {
		return Objects.isNull(value) || value.isEmpty();
	}

	@Override
	public void requestFocus() {
		control.requestFocusInWindow();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == control) {

		}
	}

}
