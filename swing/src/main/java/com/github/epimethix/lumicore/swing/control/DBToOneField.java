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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.model.TreeTop;
import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;
import com.github.epimethix.lumicore.swing.entityaccess.EntityAccessController;
import com.github.epimethix.lumicore.swing.entityaccess.EntityAccessControllerFactory;

public class DBToOneField<E extends Entity<?>> implements DBControl<E>, ActionListener {

	private final Class<E> entityClass;
	private final String fieldName;
	private final JButton control;
	private final JLabel label;
	private final boolean required;
	private final String labelKey;

	private final Border defaultButtonBorder;

	private final SwingUI ui;

	private E value;
	private E initialValue;

//	public DBToOneField(SwingUI ui, String labelKey, String fieldName, Class<E> entityClass) {
//		this(ui, labelKey, fieldName, false, entityClass);
//	}

	public DBToOneField(SwingUI ui, String labelKey, String fieldName, boolean required, Class<E> entityClass) {
		this.entityClass = entityClass;
		this.ui = ui;
		this.fieldName = fieldName;
		this.required = required;
		this.labelKey = labelKey;
		this.label = new JLabel();
		this.control = new JButton();
		defaultButtonBorder = control.getBorder();
		this.control.addActionListener(this);
	}

	private void refreshControlLabel() {
		if (Objects.nonNull(value)) {
			control.setText(value.toString());
		} else {
			control.setText(C.getLabel(C.FIELD_SELECTION_IS_NULL));
		}
	}

	@Override
	public void loadLabels() {
		label.setText(LabelsManagerPool.getLabel(labelKey));
		refreshControlLabel();
	}

	@Override
	public void setOrientation(boolean rtl) {
		if (rtl) {
			label.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			control.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		} else {
			label.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			control.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
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
		control.setEnabled(editable);
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
	public void setValue(E value) {
		setInitialValue(value);
		this.value = value;
		refreshControlLabel();
		control.setBorder(defaultButtonBorder);
	}

	@Override
	public E getValue() {
		if (value instanceof TreeTop) {
			return null;
		}
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
	public E getInitialValue() {
		return initialValue;
	}

	@Override
	public void setInitialValue(E initialValue) {
		this.initialValue = initialValue;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isEmpty() {
		return Objects.isNull(value);
	}

	@Override
	public void requestFocus() {
		control.requestFocusInWindow();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == control) {
			EntityAccessController eac = EntityAccessControllerFactory.getEntityAccessController(entityClass);
			int answer = eac.showSelectionDialog(control);
			if (answer == EntityAccessController.ANSWER_CANCEL) {
				return;
			} else if (answer == EntityAccessController.ANSWER_CLEAR) {
				this.value = null;
			} else if (answer == EntityAccessController.ANSWER_SELECT) {
				/*
				 * The EntityAccessController obtained with the Class<E> should return an
				 * selected item object of type E.
				 */
				@SuppressWarnings("unchecked")
				E selection = (E) eac.getSelectedItem();
				this.value = selection;
				control.setBorder(defaultButtonBorder);
			}
			refreshControlLabel();
		}
	}
}
