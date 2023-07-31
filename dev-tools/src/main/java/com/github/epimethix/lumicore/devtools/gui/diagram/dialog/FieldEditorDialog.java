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
package com.github.epimethix.lumicore.devtools.gui.diagram.dialog;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.AnswerOption;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.devtools.ClassPathIndex;
import com.github.epimethix.lumicore.devtools.gui.D;
import com.github.epimethix.lumicore.devtools.gui.DevToolsGUIController;
import com.github.epimethix.lumicore.devtools.gui.ListComboBoxModel;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramEntity;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramType;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.EntitySource;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;
import com.github.epimethix.lumicore.sourceutil.JavaSource.FieldSource;
import com.github.epimethix.lumicore.swing.dialog.Dialog;
import com.github.epimethix.lumicore.swing.dialog.DialogUI;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

@SwingComponent
public class FieldEditorDialog implements Dialog, LabelsDisplayer {

	@SuppressWarnings("serial")
	public static final class FieldEditorUI extends JPanel {

		private final class FieldsTableModel extends AbstractTableModel {

			private final class UMLModelField {
				String identifier;
				String visibility;
				String[] modifiers;
				String type;
				String[] typeParameters;
				int arrayDepth;
				String value;
			}

			private final class ERDModelField {
				String identifier;
				String type;
				boolean notNull;
			}

			private DiagramType diagramType;
			
			private List<FieldSource> fieldSources;

			private FieldsTableModel() {
				
			}

			@Override
			public int getRowCount() {
				return 0;
			}

			@Override
			public int getColumnCount() {
				return 0;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return null;
			}

			private void loadEntity(EntitySource entitySource) {
				fireTableDataChanged();
			}
		}

		private final FieldsTableModel fieldsTableModel;
		private final JTable fieldsTable;

		public FieldEditorUI() {
			fieldsTableModel = new FieldsTableModel();
			fieldsTable = new JTable(fieldsTableModel);
		}

		public void loadEntity(EntitySource entitySource) {
			fieldsTableModel.loadEntity(entitySource);
		}
	}

	private final DevToolsGUIController guiController;
	private DiagramEntity diagramEntity;
	private final JComboBox<String> visibility;
	private final JCheckBox ckStatic;
	private final JCheckBox ckFinal;
	private final JCheckBox ckTransient;
	private final JCheckBox ckVolatile;
	private final DialogUI dialogUI;
	private final JLabel lbName;
	private final JTextField tfName;
	private final JLabel lbValue;
	private final JTextField tfValue;
	private final JLabel lbType;
	private final JComboBox<String> tfType;
	private final JPanel pnEditor;
	private boolean isInitialized;

	public static FieldEditorUI createFieldEditorUI() {
		return new FieldEditorUI();
	}

	public FieldEditorDialog(DevToolsGUIController guiController) {
		this.guiController = guiController;
		visibility = new JComboBox<>(new String[] { "package", "private", "protected", "public" });
		ckStatic = new JCheckBox("static");
		ckFinal = new JCheckBox("final");
		ckTransient = new JCheckBox("transient");
		ckVolatile = new JCheckBox("volatile");
		JPanel pnModifiers = new JPanel(new FlowLayout(FlowLayout.LEADING));
		pnModifiers.add(visibility);
		pnModifiers.add(ckStatic);
		pnModifiers.add(ckFinal);
		pnModifiers.add(ckTransient);
		pnModifiers.add(ckVolatile);
		lbType = new JLabel();
		tfType = new JComboBox<String>();
//		tfType.setEditable(true);
		tfType.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyReleased(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {}
		});
		lbName = new JLabel();
		tfName = new JTextField();
		lbValue = new JLabel();
		tfValue = new JTextField();
//		JPanel pnButtons = initializeButtons();
		pnEditor = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.insets = LayoutUtils.createDefaultMargin();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 4;
		c.weightx = 0.0;
		pnEditor.add(pnModifiers, c);
		c.gridy++;
		c.gridwidth = 1;
		pnEditor.add(lbType, c);
		c.gridx++;
		c.weightx = 1.0;
		c.gridwidth = 3;
		pnEditor.add(tfType, c);
		c.gridx = 0;
		c.gridy++;
		c.weightx = 0.0;
		c.gridwidth = 1;
		pnEditor.add(lbName, c);
		c.gridx++;
//		c.gridy++;
		c.weightx = 1.0;
		c.gridwidth = 3;
		pnEditor.add(tfName, c);
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0.0;
		c.gridwidth = 1;
		pnEditor.add(lbValue, c);
		c.gridx++;
		c.weightx = 1.0;
		c.gridwidth = 3;
		pnEditor.add(tfValue, c);
		dialogUI = DialogUI.getDialogUI(this);
//	if (visibility.length() > 0) {
//		modifiers.add(visibility);
//	}
	}

	public void showAddFieldDialog(DiagramEntity diagramEntity) {
		this.diagramEntity = diagramEntity;
		if (!isInitialized) {
			tfType.setModel(new ListComboBoxModel<String>(ClassPathIndex.getSimpleNames()));
			AutoCompleteDecorator.decorate(tfType);
			isInitialized = true;
		}
		dialogUI.getDialog()
				.setTitle(D.getLabel(D.DIALOG_TITLE_ADD_FIELD, diagramEntity.getSimpleName()));
		DialogUI.showDialogUI(dialogUI);
	}

	@Override
	public Component getParent() {
		return guiController.getFrame();
	}

	@Override
	public Component getUI() {
		return pnEditor;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getTitle() {
		return "";
	}

	@Override
	public Answer[] getAnswerOptions() {
		return AnswerOption.OK_CANCEL.getAnswers();
	}

	@Override
	public void onAnswer(Answer answer, JDialog parent) {
		if (answer == Answer.OK) {
			FieldSource.Builder builder = FieldSource.Builder.newField((String) tfType.getSelectedItem(),
					tfName.getText());
			int visibilityIndex = visibility.getSelectedIndex();
			switch (visibilityIndex) {
			case 1:
				builder.setPrivate();
				break;
			case 2:
				builder.setProtected();
				break;
			case 3:
				builder.setPublic();
				break;
			}
			if (ckStatic.isSelected()) {
				builder.setStatic();
			}
			if (ckFinal.isSelected()) {
				builder.setFinal();
			}
			if (ckTransient.isSelected()) {
				builder.setTransient();
			}
			if (ckVolatile.isSelected()) {
				builder.setVolatile();
			}
			String value = tfValue.getText();
			if (value.trim().length() > 0) {
				builder.setValue(value);
			}
			FieldSource fieldSource = builder.build();
			((EntitySource) diagramEntity).insertField(fieldSource);
		}
		parent.setVisible(false);
	}

	@Override
	public void loadLabels() {
		lbName.setText(D.getLabel(D.FIELD_IDENTIFIER));
		lbValue.setText(D.getLabel(D.FIELD_VALUE));
		lbType.setText(D.getLabel(D.FIELD_TYPE_NAME));
	}

}
