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
package com.github.epimethix.lumicore.swing.entityaccess;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.model.TreeEntity;
import com.github.epimethix.lumicore.common.swing.SwingInjector;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayerPool;
import com.github.epimethix.lumicore.swing.LumicoreSwing;
import com.github.epimethix.lumicore.swing.editor.EntityEditorPanel;
import com.github.epimethix.lumicore.swing.util.DialogUtils;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

public class EntityAccessController implements ActionListener, LabelsDisplayer, ChangeListener {
	private final SwingUI ui;

	private final EntityEditorPanel<? extends Entity<?>, ?> editor;
//	private final Repository<? extends Entity<?>, ?> repository;

	private final AbstractDataView tableDataView;
	private final AbstractDataView treeDataView;

	private DataModel dataModel;

	private final JToolBar toolBar;

	private final JButton btTbNew;
	private final JButton btTbSave;
	private final JButton btTbDelete;
	private final JButton btTbCancel;

	private final JPanel selectionButtons;
	private final JPanel closeButton;

	private final JButton btSelSelect;
	private final JButton btSelCancel;
	private final JButton btSelClearSelection;

	private final JButton btClose;

	private final JPanel accessView;

	private final CardLayout accessViewLayout;

	private final static String CARD_DATA = "DATA";
	private final static String CARD_EDITOR = "EDITOR";

	private String selectedView;

	private Answer dialogAnswer;

	private JDialog dialog;

	private final JTabbedPane tpDataViews;

//	public static final int ANSWER_CANCEL = 0;
//	public static final int ANSWER_CLEAR = 1;
//	public static final int ANSWER_SELECT = 2;

	EntityAccessController(SwingUI ui, Repository<?, ?> repository, EntityEditorPanel<?, ?> editor)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
//		this.repository = repository;
		this.editor = Objects.requireNonNull(editor);
		this.ui = ui;
		{
			Object item = editor.initItem();
			if (item instanceof Entity.EntityBuilder<?>) {
				item = ((Entity.EntityBuilder<?>) item).build();
			}
			tableDataView = new TableDataView(repository, this);
			if (item instanceof TreeEntity<?, ?>) {
				treeDataView = new TreeDataView(repository, this);
				this.dataModel = treeDataView.getModel();
			} else {
				this.dataModel = tableDataView.getModel();
				treeDataView = null;
			}
		}

		btTbNew = new JButton();
		btTbNew.addActionListener(this);

		btTbSave = new JButton();
		btTbSave.addActionListener(this);

		btTbDelete = new JButton();
		btTbDelete.addActionListener(this);

		btTbCancel = new JButton();
		btTbCancel.addActionListener(this);

		toolBar = new JToolBar();
		toolBar.setFloatable(false);

		toolBar.add(btTbNew);
		toolBar.add(btTbSave);
		if (editor.isDeletable()) {
			toolBar.add(btTbDelete);
		}
		toolBar.add(btTbCancel);

		btSelSelect = new JButton();
		btSelSelect.addActionListener(this);

		btSelCancel = new JButton();
		btSelCancel.addActionListener(this);

		btSelClearSelection = new JButton();
		btSelClearSelection.addActionListener(this);

		selectionButtons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		selectionButtons.add(btSelClearSelection);
		selectionButtons.add(btSelSelect);
		selectionButtons.add(btSelCancel);
		selectionButtons.setBorder(LayoutUtils.createMediumEmptyBorder());

		btClose = new JButton();
		btClose.addActionListener(this);

		closeButton = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		closeButton.add(btClose);
		closeButton.setBorder(LayoutUtils.createMediumEmptyBorder());

		accessViewLayout = new CardLayout();

		accessView = new JPanel(accessViewLayout);

		tpDataViews = new JTabbedPane();
		tpDataViews.addChangeListener(this);
		if (Objects.nonNull(treeDataView)) {
			tpDataViews.addTab("Tree", treeDataView);
			tpDataViews.addTab("Table", tableDataView);
			accessView.add(tpDataViews, CARD_DATA);
		} else {
			accessView.add(tableDataView, CARD_DATA);
		}
		accessView.add(LayoutUtils.initScrollPane(editor), CARD_EDITOR);

		showView(CARD_DATA);
	}

	private final void showView(String view) {
		if (view.equals(CARD_DATA)) {
			accessViewLayout.show(accessView, CARD_DATA);
			selectedView = CARD_DATA;
			btTbSave.setEnabled(false);
			btTbCancel.setEnabled(false);
		} else if (view.equals(CARD_EDITOR)) {
			accessViewLayout.show(accessView, CARD_EDITOR);
			selectedView = CARD_EDITOR;
			btTbSave.setEnabled(true);
			btTbCancel.setEnabled(true);
		}
	}

	@Override
	public void loadLabels() {
		btTbNew.setText(C.getLabel(C.ED_BTN_NEW));
		btTbSave.setText(C.getLabel(C.ED_BTN_SAVE));
		btTbDelete.setText(C.getLabel(C.ED_BTN_DELETE));
		btTbCancel.setText(C.getLabel(C.ED_BTN_CANCEL));
		btSelCancel.setText(C.getLabel(C.DLG_BTN_CANCEL));
		btSelClearSelection.setText(C.getLabel(C.DLG_BTN_CLEAR));
		btSelSelect.setText(C.getLabel(C.DLG_BTN_SELECT));
		btClose.setText(C.getLabel(C.ED_BTN_CLOSE));
	}

//	@Override
//	public void setOrientation(boolean rtl) {
//		dataView.applyComponentOrientation(
//				rtl ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
//	}

	public final JPanel getView() {
		JPanel view = new JPanel(new BorderLayout());
		view.add(toolBar, BorderLayout.NORTH);
		view.add(accessView, BorderLayout.CENTER);
		return view;
	}

	public final Answer showSelectionDialog(Component childComponent) {
		LabelsDisplayerPool.addLabelsDisplayers(this);
		JPanel view = getView();
		view.add(selectionButtons, BorderLayout.SOUTH);
		dialog = DialogUtils.initializeJDialog(childComponent, C.getLabel(C.DLG_SELECTION_TITLE), view, true);
		JMenuBar menuBar = new JMenuBar();
		JMenu langMenu = DialogUtils.getLanguageSelectionMenu(LumicoreSwing.getApplication());
		menuBar.add(langMenu);
		dialog.setJMenuBar(menuBar);
		dataModel.select(null);
		refresh();
		dialog.setVisible(true);
		LabelsDisplayerPool.removeLabelsDisplayers(this);
		return dialogAnswer;
	}

	public Answer showEditorDialog(Component childComponent) {
		LabelsDisplayerPool.addLabelsDisplayers(this);
		JPanel view = getView();
		view.add(closeButton, BorderLayout.SOUTH);
		dialog = DialogUtils.initializeJDialog(childComponent, C.getLabel(C.DLG_EDIT_TITLE), view, true);
		JMenuBar menuBar = new JMenuBar();
		JMenu langMenu = DialogUtils.getLanguageSelectionMenu(LumicoreSwing.getApplication());
		menuBar.add(langMenu);
		dialog.setJMenuBar(menuBar);
		dataModel.select(null);
		refresh();
		dialog.setVisible(true);
		LabelsDisplayerPool.removeLabelsDisplayers(this);
		return dialogAnswer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btClose) {
			if (editor.clear())
				hideDialog(Answer.CANCEL);
		} else if (selectedView.equals(CARD_EDITOR)) {
			if (e.getSource() == btTbNew) {
				editor.clear();
			} else if (e.getSource() == btTbSave) {
				editor.save(i -> {
					if (Objects.nonNull(i)) {
						dataModel.select(i);
						showView(CARD_DATA);
					}
				});
			} else if (e.getSource() == btTbDelete) {
				Entity<?> currentItem = editor.getCurrentItem();
				if (Objects.isNull(currentItem)) {
					ui.showErrorMessage(editor, C.DLG_MSG_ENTRY_NOT_SAVED_YET);
				} else if (Objects.nonNull(currentItem)) {
					editor.delete(currentItem.getId());
					showView(CARD_DATA);
				}
			} else if (e.getSource() == btTbCancel) {
				if (editor.clear()) {
					showView(CARD_DATA);
				}
			} else if (e.getSource() == btSelCancel) {
				hideDialog(Answer.CANCEL);
			} else if (e.getSource() == btSelClearSelection) {
				hideDialog(Answer.CLEAR);
			} else if (e.getSource() == btSelSelect) {
				editor.save(i -> {
					if (Objects.nonNull(i)) {
						dataModel.select(i);
						hideDialog(Answer.SELECT);
					}
				});
			}
		} else if (selectedView.equals(CARD_DATA)) {
			if (e.getSource() == btTbDelete) {
				Object itemToDelete = dataModel.select();
				if (Objects.nonNull(itemToDelete)) {
					delete(((Entity<?>) itemToDelete).getId());
				}
				dataModel.refresh();
			} else if (e.getSource() == btTbNew) {
				showView(CARD_EDITOR);
			} else if (e.getSource() == btSelCancel && Objects.nonNull(dialog)) {
				hideDialog(Answer.CANCEL);
			} else if (e.getSource() == btSelClearSelection) {
				dataModel.select(null);
				hideDialog(Answer.CLEAR);
			} else if (e.getSource() == btSelSelect) {
				Object selectedItem = dataModel.select();
				if (Objects.nonNull(selectedItem)) {
					hideDialog(Answer.SELECT);
				}
			}
		}
	}

	private void hideDialog(Answer answer) {
		if (Objects.nonNull(dialog)) {
			dialogAnswer = answer;
			dialog.setVisible(false);
			dialog.dispose();
			dialog = null;
		}
	}

	public Object getSelectedItem() {
		return dataModel.select();
	}

	public void edit(Object item) {
		editor.load(item);
		showView(CARD_EDITOR);
	}

	public void refresh() {
		dataModel.refresh();
	}

	public boolean isDeletable() {
		return editor.isDeletable();
	}

	public void create(TreeEntity<?, ?> context) {
		create();
		editor.setParent(context);
	}

	public void create() {
		editor.clear();
		showView(CARD_EDITOR);
	}

	public void delete(Object id) {
		editor.delete(id);
	}

	public Class<?> getEntityClass() {
		return editor.initItem().getClass();
	}

	public EntityEditorPanel<? extends Entity<?>, ?> getEditor() {
		return editor;
	}

	public void showDataView() {
		showView(CARD_DATA);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == tpDataViews) {
			switch (tpDataViews.getSelectedIndex()) {
			case 0:
				dataModel = treeDataView.getModel();
				break;
			case 1:
				dataModel = tableDataView.getModel();
				break;
			default:
				break;
			}
		}
	}
}
