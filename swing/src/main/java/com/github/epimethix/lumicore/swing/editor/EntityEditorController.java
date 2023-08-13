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
package com.github.epimethix.lumicore.swing.editor;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.model.Entity.EntityBuilder;
import com.github.epimethix.lumicore.common.orm.model.MutableEntity;
import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.orm.ORM;
import com.github.epimethix.lumicore.swing.util.GridBagUtils;

/**
 * {@code EntityEditorController} manages the creation of editor layout while at
 * the same time managing the editor control (user input) values for reading and
 * writing records.
 * 
 * @author epimethix
 *
 * @param <E> the entity to control
 */
public class EntityEditorController<E extends Entity<?>> {
	/**
	 * specifies in which location the next element to be added should be placed.
	 * 
	 * @author epimethix
	 *
	 */
	public static enum LayoutIncrement {
		/**
		 * Increments the y cursor by the current grid height.
		 */
		DOWN,
		/**
		 * Increments the x cursor by the current grid width.
		 */
		RIGHT,
		/**
		 * If the current grid width is equal to one (1) then the x cursor is
		 * decremented by one (1), the y cursor is incremented by the current grid
		 * height.
		 */
		LINE_DOWN,
		/**
		 * sets the x cursor to zero (0) and the y cursor to the current form height
		 * (y-max + 1).
		 */
		NEXT_LINE,
		/**
		 * sets the x cursor to the current form width (x-max + 1) and the y cursor to
		 * zero (0).
		 */
		NEXT_COLUMN;
	}

	/**
	 * Class to define custom Object transformations between entity fields and gui
	 * controls.
	 */
	public final static class ControlTransform {
		private final Function<Object, Object> getValue;
		private final Function<Object, Object> setValue;

		/**
		 * Defines a custom Object transformation.
		 * 
		 * @param getValue transforms a value from entity field getter value to the
		 *                 control value
		 * @param setValue transforms a value from the control value to pass to the
		 *                 entity field setter
		 */
		public ControlTransform(Function<Object, Object> getValue, Function<Object, Object> setValue) {
			this.getValue = Objects.requireNonNull(getValue);
			this.setValue = Objects.requireNonNull(setValue);
		}
	}

	private final Map<String, ControlTransform> transforms = new HashMap<>();

	private final void addTransform(String fieldName, ControlTransform transform, String controlType) {
		if (Objects.nonNull(transform) && Objects.nonNull(fieldName)) {
			if (!fieldName.trim().isEmpty()) {
				transforms.put(fieldName, transform);
			} else {
				System.err.printf("Transform for %s was not added since no field name was specified!%n", controlType);
			}
		}
	}

	private final JComponent component;
	private final GridBagConstraints c;
	private final List<DBControl<?>> controls;
	private final List<DBControl<?>> writeControls;
	private int labelPosition;
	private int formWidth;
	private int formHeight;
	private LayoutIncrement nextLayoutIncrement;

	/**
	 * Creates a new {@code EntityEditorController} with label position
	 * {@link DBControl#LABEL_LEFT}.
	 * 
	 * @param component the editor component (must have a {@link GridBagLayout})
	 */
	public EntityEditorController(JComponent component) {
		this(component, GridBagUtils.initGridBagConstraints());
	}

	/**
	 * Creates a new {@code EntityEditorController} with label position
	 * {@link DBControl#LABEL_LEFT}.
	 * 
	 * @param component the editor component (must have a {@link GridBagLayout})
	 * @param c         the {@link GridBagConstraints}
	 */
	public EntityEditorController(JComponent component, GridBagConstraints c) {
		this(component, c, DBControl.LABEL_LEFT);
	}

	/**
	 * Creates a new {@code EntityEditorController}.
	 * 
	 * @param component     the editor component (must have a {@link GridBagLayout})
	 * @param c             the {@link GridBagConstraints}
	 * @param labelPosition {@link DBControl#LABEL_LEFT} or
	 *                      {@link DBControl#LABEL_TOP}
	 */
	public EntityEditorController(JComponent component, GridBagConstraints c, int labelPosition) {
		Objects.requireNonNull(component);
		if (!(component.getLayout() instanceof GridBagLayout)) {
			throw new IllegalArgumentException("component must have a GridBagLayout!");
		}
		this.component = component;
		this.c = Objects.requireNonNull(c);
		this.labelPosition = labelPosition;
		this.controls = new ArrayList<>();
		this.writeControls = new ArrayList<>();
	}

	/**
	 * Sets the specified field names controls {@code setEditable(false)}.
	 * <p>
	 * to set all controls writable again this method can be called without any arguments.
	 * 
	 * @param fieldNames the field names to set read only
	 */
	public final void setReadOnly(String... fieldNames) {
		List<DBControl<?>> roControls = new ArrayList<>(controls);
		roControls.removeAll(writeControls);
		for(DBControl<?> roc:roControls) {
			setControlWritable(roc);
		}
		for (String name : fieldNames) {
			setControlReadOnly(name);
		}
	}

	private final void setControlWritable(DBControl<?> c) {
		writeControls.add(c);
		c.setEditable(true);
	}
	
	private final void setControlReadOnly(String fieldName) {
		if (Objects.nonNull(fieldName) && !fieldName.trim().isEmpty()) {
			for (DBControl<?> c : controls) {
//				System.out.println(c.getFieldName());
				if (fieldName.equals(c.getFieldName())) {
					if (writeControls.remove(c)) {
//						System.err.println("removed from write list: " + fieldName);
					}
					c.setEditable(false);
					return;
				}
			}
			System.err.printf(
					"EditorLayoutController.setControlReadOnly failed:Control for field name '%s' not found!%n",
					fieldName);
		}
	}

	/**
	 * Set the item values from the control input.
	 * 
	 * @param item the item to filled with values
	 */
	public E setValues(E item) {
		Object mutable = item;
		if (!MutableEntity.class.isAssignableFrom(item.getClass())) {
			for (Class<?> m : item.getClass().getDeclaredClasses()) {
				if (m.getSimpleName().equals("Builder")) {
					try {
						mutable = m.getConstructor(item.getClass()).newInstance(item);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
		}
		for (DBControl<?> c : controls) {
			if (Objects.nonNull(c.getFieldName()) && !c.getFieldName().trim().isEmpty()) {
				try {
					ControlTransform transform = transforms.get(c.getFieldName());
					if (Objects.nonNull(transform)) {
						ORM.setValue(mutable, c, transform.setValue);
					} else {
						ORM.setValue(mutable, c);
					}
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		if (mutable instanceof EntityBuilder<?>) {
			item = (E) ((EntityBuilder<?>) mutable).build();
		}
		return item;
	}

	/**
	 * Get values from item and put them into the controls.
	 * 
	 * @param item the item to get the values from
	 */
	public void getValues(E item) {
		for (DBControl<?> c : controls) {
			if (Objects.nonNull(c.getFieldName()) && !c.getFieldName().trim().isEmpty()) {
				try {
					ControlTransform transform = transforms.get(c.getFieldName());
					if (Objects.nonNull(transform)) {
						ORM.getValue(item, c, transform.getValue);
					} else {
						ORM.getValue(item, c);
					}
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void setLabelPositionLeft() {
		this.labelPosition = DBControl.LABEL_LEFT;
	}

	public void setLabelPositionTop() {
		this.labelPosition = DBControl.LABEL_TOP;
	}

	public void setNextLayoutIncrement(LayoutIncrement i) {
		nextLayoutIncrement = i;
	}

	public JComponent getComponent() {
		return component;
	}

	public GridBagConstraints getGridBagConstraints() {
		return c;
	}

	/**
	 * This method must be called if the control is added to the editor component
	 * any other way than through the method {@code addControl}. This ensures that
	 * the {@code DBControl}s values are managed.
	 * 
	 * @param dbc the control to register.
	 */
	public void registerControl(DBControl<?> dbc) {
		controls.add(dbc);
		writeControls.add(dbc);
	}

	public void addControl(DBControl<?> dbc) {
		addControl(dbc, (ControlTransform) null);
	}

	public void addControl(DBControl<?> dbc, LayoutIncrement i) {
		addControl(dbc, null, i);
	}

	public void addControl(DBControl<?> dbc, ControlTransform transform) {
		addControl(dbc, transform, LayoutIncrement.LINE_DOWN);
	}

	public void addControl(DBControl<?> dbc, ControlTransform transform, LayoutIncrement i) {
		addControl(dbc, transform, i, labelPosition);
	}

	/**
	 * Adds the specified control (Label + user input).
	 * 
	 * @param dbc           the control to add.
	 * @param transform     the type transformation
	 * @param i             the {@code LayoutIncrement}
	 * @param labelPosition
	 */
	public void addControl(DBControl<?> dbc, ControlTransform transform, LayoutIncrement i, int labelPosition) {
		addTransform(dbc.getFieldName(), transform, dbc.getClass().getSimpleName());
		if (Objects.nonNull(nextLayoutIncrement)) {
			i = nextLayoutIncrement;
			nextLayoutIncrement = null;
		}
		registerControl(dbc);
		if (labelPosition == DBControl.LABEL_LEFT) {
			c.gridwidth = 1;
//			c.weightx = 0.0;
//			c.anchor=GridBagConstraints.PAGE_START;
			addComponent(dbc.getLabel(), LayoutIncrement.RIGHT);
			c.weightx = 1.0;
		} else {
			c.gridwidth = 2;
			addComponent(dbc.getLabel(), LayoutIncrement.DOWN);
//			c.weightx = 1.0;
//			addComponent(new JSeparator(), LayoutIncrement.LINE_DOWN);
		}
		addComponent(dbc.getControl(), i);
		if (labelPosition != DBControl.LABEL_LEFT) {
			c.gridwidth = 1;
		}
		c.weightx = 0.0;
	}

	/**
	 * Adds a title in the left column and a separator in the right column.
	 * 
	 * @param lbTitle the title label
	 */
	public void addTitle(JLabel lbTitle) {
		c.gridwidth = 1;
		addComponent(lbTitle, LayoutIncrement.RIGHT);
		c.weightx = 1.0;
		addComponent(new JSeparator(), LayoutIncrement.LINE_DOWN);
		c.weightx = 0.0;
	}

	public void addComponent(Component label, Component control, LayoutIncrement i) {
		if(labelPosition == DBControl.LABEL_LEFT) {
			c.gridwidth = 1;
			addComponent(label, LayoutIncrement.RIGHT);
			c.weightx = 1.0;
			addComponent(control, i);
		} else {
			c.gridwidth = 2;
			c.weightx = 1.0;
			addComponent(label, LayoutIncrement.DOWN);
			addComponent(control, i);
		}
		c.weightx = 0.0;
	}

	public void addComponent(Component component, LayoutIncrement i) {
		this.component.add(component, c);
		formWidth = Math.max(formWidth, c.gridx + c.gridwidth);
		formHeight = Math.max(formHeight, c.gridy + c.gridheight);
		switch (i) {
		case RIGHT:
			c.gridx += c.gridwidth;
			break;
		case DOWN:
			c.gridy += c.gridheight;
			break;
		case LINE_DOWN:
			if (c.gridwidth == 1) {
				c.gridx--;
			}
			c.gridy += c.gridheight;
			break;
		case NEXT_COLUMN:
			c.gridx = formWidth;
			c.gridy = 0;
			break;
		case NEXT_LINE:
			c.gridx = 0;
			c.gridy = formHeight;
			break;
		}
	}

	public void finishForm() {
		GridBagUtils.finishGridBagForm(component, c, formWidth);
	}

	public boolean hasChanges() {
		for (DBControl<?> c : controls) {
			if (c.hasChanges()) {
//				System.err.println(c.getFieldName() + " has changes");
				return true;
			}
		}
		return false;
	}

	public void clearControls() {
		for (DBControl<?> c : controls) {
			c.clear();
		}
	}

	public void clearChanges() {
		for (DBControl<?> c : controls) {
			c.clearChanges();
		}
	}

	public List<DBControl<?>> getControls() {
		return controls;
	}

	public int getFormWidth() {
		return formWidth;
	}

	public int getFormHeight() {
		return formHeight;
	}

	public boolean isValid() {
		for (DBControl<?> c : writeControls) {
			if (!c.isValid()) {
				return false;
			}
		}
		return true;
	}
}
