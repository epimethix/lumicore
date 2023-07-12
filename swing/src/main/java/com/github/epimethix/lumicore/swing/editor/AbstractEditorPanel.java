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
package com.github.epimethix.lumicore.swing.editor;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.InvocationTargetException;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.model.TreeEntity;
import com.github.epimethix.lumicore.common.orm.query.QueryParameters;
import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.common.swing.EntityEditor;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.swing.control.DBBigDecimalField;
import com.github.epimethix.lumicore.swing.control.DBBooleanField;
import com.github.epimethix.lumicore.swing.control.DBDateField;
import com.github.epimethix.lumicore.swing.control.DBDateTimeField;
import com.github.epimethix.lumicore.swing.control.DBEnumComboPicker;
import com.github.epimethix.lumicore.swing.control.DBEnumRadioPicker;
import com.github.epimethix.lumicore.swing.control.DBIntegerField;
import com.github.epimethix.lumicore.swing.control.DBPathField;
import com.github.epimethix.lumicore.swing.control.DBTextArea;
import com.github.epimethix.lumicore.swing.control.DBTextField;
import com.github.epimethix.lumicore.swing.control.DBToManyField;
import com.github.epimethix.lumicore.swing.control.DBToOneField;
import com.github.epimethix.lumicore.swing.control.DBPathField.Selector;
import com.github.epimethix.lumicore.swing.editor.EditorLayoutController.ControlTransform;
import com.github.epimethix.lumicore.swing.editor.EditorLayoutController.LayoutIncrement;
import com.github.epimethix.lumicore.swing.util.DialogUtils;
import com.github.epimethix.lumicore.swing.util.GridBagUtils;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

@SuppressWarnings({ "serial" })
public abstract class AbstractEditorPanel<E extends Entity<ID>, ID> extends JPanel
		implements ComponentListener, EntityEditor<E>, LabelsDisplayer {
	/**
	 * The editor events are: INIT, SHOW, AFTER_SAVE, AFTER_CLEAR, AFTER_DELETE,
	 * AFTER_LOAD, AFTER_HIDE
	 *
	 */
	protected static enum EditorEvent {
		INIT, SHOW, AFTER_SAVE, AFTER_CLEAR, AFTER_DELETE, AFTER_LOAD, AFTER_HIDE;
	}

	/**
	 * Override this method to receive editor events.
	 * 
	 * @param event the event
	 * @see EditorEvent
	 */
	protected void editorEvent(EditorEvent event) {}

	/**
	 * current item in editor, is null when new
	 */
	private E currentItem;

	private boolean isLoaded;

	private final SwingUI ui;

	private final Repository<E, ID> repository;

	private final EditorLayoutController<E> layoutManager;

	protected AbstractEditorPanel(SwingUI ui, Repository<E, ID> repository) {
		this(ui, repository, DBControl.LABEL_LEFT);
	}

	public AbstractEditorPanel(SwingUI ui, Repository<E, ID> repository, int labelPosition) {
		super(new GridBagLayout());
		this.repository = repository;
		this.ui = ui;
		this.layoutManager = new EditorLayoutController<E>(this, GridBagUtils.initGridBagConstraints(), labelPosition);
		addComponentListener(this);
		setBorder(LayoutUtils.createDefaultEmptyBorder());
	}

	/**
	 * Gets the current Ts repository
	 * 
	 * @return the current Ts repository
	 */
	protected Repository<E, ID> getRepository() {
		return repository;
	}

	public E getCurrentItem() {
		return currentItem;
	}

	protected boolean isNew() {
		return Objects.isNull(currentItem);
	}

	/*
	 * Editor
	 */

	@Override
	public E initItem() {
		Object o = repository.newRecord();
		if(o instanceof Entity.EntityBuilder<?>) {
			return (E) ((Entity.EntityBuilder) o).build();
		}
		return (E) o;
	}

	@Override
	public Comparator<E> getComparator() {
		return (a, b) -> a.toString().compareTo(b.toString());
	}

	@Override
	public void load(Object item) {
		if (clear()) {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					currentItem = repository.selectById(((E) item).getId()).orElse(null);
					return null;
				}

				protected void done() {
					if (Objects.nonNull(currentItem)) {
						layoutManager.getValues(currentItem);
						editorEvent(EditorEvent.AFTER_LOAD);
					}
				}
			};
			worker.execute();
		}
	}

	@Override
	public Optional<SwingWorker<E, Void>> save(Consumer<E> onDone) {
		for (DBControl<?> c : layoutManager.getControls()) {
			if (!c.isValid()) {
				return Optional.empty();
			}
		}
		if (Objects.isNull(currentItem)) {
			currentItem = initItem();
		}
		currentItem = layoutManager.setValues(currentItem);
		SwingWorker<E, Void> worker = new SwingWorker<E, Void>() {
			private Exception e;
			private E saved = null;

			@Override
			protected E doInBackground() throws Exception {
				try {
					saved = getRepository()
							.save(currentItem)
							.orElse(null);
				} catch (SQLException e) {
					this.e = e;
				}
				return saved;
			}

			@Override
			protected void done() {
				if (Objects.nonNull(saved)) {
//					isNew = false;
					currentItem = saved;
					clearChanges();
					editorEvent(EditorEvent.AFTER_SAVE);
					clearControls();
				}
				if (Objects.nonNull(e)) {
					ui.showErrorMessage(AbstractEditorPanel.this, e.getMessage());
				}
				onDone.accept(saved);
			}
		};
		worker.execute();
		return Optional.of(worker);
	}

	@Override
	public boolean clear() {
		if (hasChanges()) {
			Answer ans = ui.showYesNoCancelDialog(this, C.DLG_MSG_EDITOR_HAS_UNSAVED_CHANGES);
			if (ans == Answer.CANCEL) {
				return false;
			} else if (ans == Answer.YES) {
				Optional<SwingWorker<E, Void>> w = save(i -> {});
				if (w.isEmpty()) {
					return false;
				}
			}
		}
		clearControls();
		editorEvent(EditorEvent.AFTER_CLEAR);
		return true;
	}

	private void clearControls() {
		layoutManager.clearControls();
		currentItem = null;
	}

	public void clearChanges() {
		layoutManager.clearChanges();
	}

	@Override
	public boolean hasChanges() {
		return layoutManager.hasChanges();
	}

	/**
	 * to modify deleting behavior this method should be overridden.
	 * 
	 * @return false
	 */
	@Override
	public boolean isDeletable() {
		return false;
	}

//	@Override
	public void delete(Object id) {
		// TODO call ui message
		if (isDeletable()) {
			boolean proceed = JOptionPane.YES_OPTION == DialogUtils.showConfirmDialog(this, JOptionPane.YES_NO_OPTION,
					C.getLabel(C.DLG_MSG_PROCEED_DELETING_ENTRY));
			if (proceed) {
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
					private boolean success = false;
					private Exception e;

					@Override
					protected Void doInBackground() throws Exception {
						try {
							success = getRepository().deleteById((ID) id).isPresent();
						} catch (SQLException e2) {
							e = e2;
						}
						return null;
					}

					@Override
					protected void done() {
						if (Objects.nonNull(e)) {
							ui.showErrorMessage(AbstractEditorPanel.this, C.DLG_MSG_COULD_NOT_DELETE_ITEM_FORMAT,
									e.getMessage());
						}
						if (success) {
							clearControls();
							editorEvent(EditorEvent.AFTER_DELETE);
						}
					}
				};
				worker.execute();

			}
		} else {
			ui.showErrorMessage(this, C.DLG_MSG_ENTRY_CAN_NOT_BE_DELETED);
		}

	}

	@Override
	public void setParent(Object parent) {
		for (DBControl<?> c : layoutManager.getControls()) {
			if (TreeEntity.PARENT.equals(c.getFieldName())) {
				try {
					c.getClass().getMethod("setValue", Object.class).invoke(c, parent);
				} catch (NoSuchMethodException | SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

//		Class<?> entityClass = initItem().getClass();
//		Field[] fields = entityClass.getDeclaredFields();
//		Field parentField = null;
//		for (Field f : fields) {
//			if (f.getName().equals(TreeEntity.PARENT)) {
//				parentField = f;
//				break;
//			}
//		}
//		if (Objects.nonNull(parentField)) {
//			String fieldName = Reflect.getReferencedFieldName(parentField);
//
//		}
	}

	/*
	 * Add DB Controls
	 */

	protected DBDateField addDateField(String labelKey, String fieldName, boolean required, String formatString) {
		return addDateField(labelKey, fieldName, required, formatString, null);
	}

	protected DBDateField addDateField(String labelKey, String fieldName, boolean required, String formatString,
			ControlTransform transform) {
		DBDateField dbDF = new DBDateField(ui, labelKey, fieldName, required, formatString);
		addControl(dbDF, transform);
		return dbDF;
	}

	protected DBDateTimeField addDateTimeField(String labelKey, String fieldName, boolean required,
			String formatString) {
		return addDateTimeField(labelKey, fieldName, required, formatString, null);
	}

	protected DBDateTimeField addDateTimeField(String labelKey, String fieldName, boolean required, String formatString,
			ControlTransform transform) {
		DBDateTimeField dbDTF = new DBDateTimeField(ui, labelKey, fieldName, required, formatString);
		addControl(dbDTF, transform);
		return dbDTF;
	}

	protected <T extends Enum<T>> DBEnumComboPicker<T> addEnumComboPicker(String labelKey, String fieldName,
			boolean required, Class<T> enumClass, Function<T, String> constantToString) {
		return addEnumComboPicker(labelKey, fieldName, required, enumClass, constantToString, null);
	}

	protected <T extends Enum<T>> DBEnumComboPicker<T> addEnumComboPicker(String labelKey, String fieldName,
			boolean required, Class<T> enumClass, Function<T, String> constantToString, ControlTransform transform) {
		DBEnumComboPicker<T> dbECP = new DBEnumComboPicker<>(ui, labelKey, fieldName, required, enumClass,
				constantToString);
		addControl(dbECP, transform);
		return dbECP;
	}

	protected <T extends Enum<T>> DBEnumRadioPicker<T> addEnumRadioPicker(String labelKey, String fieldName,
			boolean required, Class<T> enumClass, Function<T, String> constantToString, int cols) {
		return addEnumRadioPicker(labelKey, fieldName, required, enumClass, constantToString, cols, null);
	}

	protected <T extends Enum<T>> DBEnumRadioPicker<T> addEnumRadioPicker(String labelKey, String fieldName,
			boolean required, Class<T> enumClass, Function<T, String> constantToString, int cols,
			ControlTransform transform) {
		DBEnumRadioPicker<T> dbERP = new DBEnumRadioPicker<>(ui, labelKey, fieldName, required, enumClass,
				constantToString, cols);
		addControl(dbERP, transform);
		return dbERP;
	}

	protected DBIntegerField addIntegerField(String labelKey, String fieldName, boolean required) {
		return addIntegerField(labelKey, fieldName, required, null);
	}

	protected DBIntegerField addIntegerField(String labelKey, String fieldName, boolean required,
			ControlTransform transform) {
		DBIntegerField dbIF = new DBIntegerField(ui, labelKey, fieldName, required);
		addControl(dbIF, transform);
		return dbIF;
	}

	public DBBooleanField addBooleanField(String labelKey, String fieldName, boolean required) {
		return addBooleanField(labelKey, fieldName, required, null);
	}
	public DBBooleanField addBooleanField(String labelKey, String fieldName, boolean required,
			ControlTransform transform) {
		DBBooleanField f = new DBBooleanField(ui, labelKey, fieldName, required);
		addControl(f, transform);
		return f;
	}

	protected DBBigDecimalField addBigDecimalField(String labelKey, String fieldName, int decimalPlacesAfterComma,
			RoundingMode roundingMode, boolean required) {
		return addBigDecimalField(labelKey, fieldName, decimalPlacesAfterComma, roundingMode, required, null);
	}

	protected DBBigDecimalField addBigDecimalField(String labelKey, String fieldName, int decimalPlacesAfterComma,
			RoundingMode roundingMode, boolean required, ControlTransform transform) {
		DBBigDecimalField dbIF = new DBBigDecimalField(ui, labelKey, fieldName, decimalPlacesAfterComma, roundingMode,
				required);
		addControl(dbIF, transform);
		return dbIF;
	}

	protected DBPathField addFilePathField(String labelKey, String fieldName, boolean required, Path parent) {
		return addFilePathField(labelKey, fieldName, required, parent, null);
	}

	protected DBPathField addFilePathField(String labelKey, String fieldName, boolean required, Path parent,
			ControlTransform transform) {
		return addPathField(labelKey, fieldName, required, Selector.FILE, parent, transform);
	}

	protected DBPathField addDirectoryPathField(String labelKey, String fieldName, boolean required, Path parent) {
		return addDirectoryPathField(labelKey, fieldName, required, parent, null);
	}

	protected DBPathField addDirectoryPathField(String labelKey, String fieldName, boolean required, Path parent,
			ControlTransform transform) {
		return addPathField(labelKey, fieldName, required, Selector.DIRECTORY, parent, transform);
	}

	private DBPathField addPathField(String labelKey, String fieldName, boolean required, Selector selector,
			Path parent, ControlTransform transform) {
		DBPathField dbPF = new DBPathField(ui, labelKey, fieldName, required, selector, parent);
		addControl(dbPF, transform);
		return dbPF;
	}

	protected DBTextArea addTextArea(String labelKey, String fieldName, boolean required, int rows) {
		return addTextArea(labelKey, fieldName, required, rows, null);
	}

	protected DBTextArea addTextArea(String labelKey, String fieldName, boolean required, int rows,
			ControlTransform transform) {
		DBTextArea dbTA = new DBTextArea(ui, labelKey, fieldName, required, rows, 0);
		addControl(dbTA, transform);
		return dbTA;
	}

	protected DBTextField addTextField(String labelKey, String fieldName, boolean required) {
		return addTextField(labelKey, fieldName, required, null);
	}

	protected DBTextField addTextField(String labelKey, String fieldName, boolean required,
			ControlTransform transform) {
		DBTextField dbTF = new DBTextField(ui, labelKey, fieldName, required);
		addControl(dbTF, transform);
		return dbTF;
	}

	protected <J extends Entity<?>> DBToManyField<J> addToManyField(String labelKey, String fieldName, boolean required,
			Class<J> entityClass, Comparator<J> comparator) {
		return addToManyField(labelKey, fieldName, required, entityClass, comparator, null);
	}

	protected <J extends Entity<?>> DBToManyField<J> addToManyField(String labelKey, String fieldName, boolean required,
			Class<J> entityClass, Comparator<J> comparator, ControlTransform transform) {
		DBToManyField<J> dbTMF = new DBToManyField<>(ui, labelKey, fieldName, required, entityClass, comparator);
		addControl(dbTMF, transform);
		return dbTMF;
	}

	protected <J extends Entity<?>> DBToOneField<J> addToOneField(String labelKey, String fieldName, boolean required,
			Class<J> entityClass) {
		return addToOneField(labelKey, fieldName, required, entityClass, null);
	}

	protected <J extends Entity<?>> DBToOneField<J> addToOneField(String labelKey, String fieldName, boolean required,
			Class<J> entityClass, ControlTransform transform) {
		DBToOneField<J> dbTOF = new DBToOneField<>(ui, labelKey, fieldName, required, entityClass);
		addControl(dbTOF, transform);
		return dbTOF;
	}

	/*
	 * EditorLayoutController delegate methods
	 */

	protected void setLabelPositionLeft() {
		layoutManager.setLabelPositionLeft();
	}

	protected void setLabelPositionTop() {
		layoutManager.setLabelPositionTop();
	}

	protected void setNextLayoutIncrement(LayoutIncrement i) {
		layoutManager.setNextLayoutIncrement(i);
	}

	protected void addControl(DBControl<?> dbc) {
		layoutManager.addControl(dbc);
	}

	protected void addControl(DBControl<?> dbc, ControlTransform transform) {
		layoutManager.addControl(dbc, transform);
	}

	protected void addControl(DBControl<?> dbc, ControlTransform transform, LayoutIncrement i) {
		layoutManager.addControl(dbc, transform, i);
	}

	protected void addControl(DBControl<?> dbc, LayoutIncrement i) {
		layoutManager.addControl(dbc, i);
	}

	protected void addControl(DBControl<?> dbc, ControlTransform transform, LayoutIncrement i, int labelPosition) {
		layoutManager.addControl(dbc, transform, i, labelPosition);
	}

	protected void addComponent(Component component, LayoutIncrement i) {
		layoutManager.addComponent(component, i);
	}

	public void finishForm() {
		layoutManager.finishForm();
	}

	protected final void setReadOnly(String fieldName, String... fieldNames) {
		layoutManager.setReadOnly(fieldName, fieldNames);
	}

	protected GridBagConstraints getGridBagConstraints() {
		return layoutManager.getGridBagConstraints();
	}

	protected int getFormWidth() {
		return layoutManager.getFormWidth();
	}

	protected int getFormHeight() {
		return layoutManager.getFormHeight();
	}

//	public boolean isValid() {
//		return layoutManager.isValid();
//	}

	/*
	 * LabelsDisplayer
	 */

	@Override
	public void loadLabels() {}

	/*
	 * ComponentListener
	 */

	@Override
	public void componentResized(ComponentEvent e) {}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentShown(ComponentEvent e) {
		if (!isLoaded) {
			isLoaded = true;
			editorEvent(EditorEvent.INIT);
		}
		editorEvent(EditorEvent.SHOW);
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		editorEvent(EditorEvent.AFTER_HIDE);
	}
}
