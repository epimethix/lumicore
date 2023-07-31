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
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.AnswerOption;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.devtools.gui.D;
import com.github.epimethix.lumicore.devtools.gui.DevToolsGUIController;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.Diagram;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramType;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;
import com.github.epimethix.lumicore.swing.dialog.Dialog;
import com.github.epimethix.lumicore.swing.dialog.DialogUI;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

@SwingComponent
public class NewDiagramDialog implements Dialog, LabelsDisplayer {

//	public Optional<Diagram> newDiagram() {
//		try {
//			String title = D.getLabel(Key.DIALOG_TITLE_NEW_DIAGRAM);
//			final JDialog dialog = UIUtils.initializeJDialog(guiController.getFrame(), title, pnEditor, true);
//			class Answer {
//				final static String OK = "OK";
//				final static String CANCEL = "CANCEL";
//				String value = CANCEL;
//
//			}
//			final Answer answer = new Answer();
//			ActionListener actionListener = e -> {
//				answer.value = e.getActionCommand();
//				dialog.setVisible(false);
//			};
//			btOk.addActionListener(actionListener);
//			btOk.setActionCommand(Answer.OK);
//			btCancel.addActionListener(actionListener);
//			btCancel.setActionCommand(Answer.CANCEL);
//			dialog.setVisible(true);
//			if (answer.value.equals(Answer.OK)) {
//
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	private final DevToolsGUIController guiController;

	private final DialogUI dialogUI;

	private final JPanel pnEditor;

	private Diagram result;

	private final JTextField tfPath;

	private final JRadioButton rbUML;
	private final JRadioButton rbERD;

	public NewDiagramDialog(DevToolsGUIController guiController) {
		this.guiController = guiController;
		tfPath = new JTextField();
		JButton btFileChooser = new JButton();

		rbUML = new JRadioButton("", true);
		rbERD = new JRadioButton(D.getLabel(D.CREATE_DIAGRAM_ERD));
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(rbUML);
		buttonGroup.add(rbERD);
		JPanel pnType = new JPanel(new FlowLayout(FlowLayout.LEADING));
		pnType.add(rbUML);
		pnType.add(rbERD);

//		JButton btOk = new JButton(D.getLabel(Key.BUTTON_SAVE));
//		JButton btCancel = new JButton(D.getLabel(Key.BUTTON_CANCEL));
//		JPanel pnButtons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
//		pnButtons.add(btCancel);
//		pnButtons.add(btOk);

		pnEditor = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.insets = LayoutUtils.createDefaultMargin();
		pnEditor.add(tfPath, c);
		c.gridx++;
		c.weightx = 0.0;
		pnEditor.add(btFileChooser, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		pnEditor.add(pnType, c);
//		c.gridy++;
//		c.weighty = 1.0;
//		pnEditor.add(pnButtons, c);

		dialogUI = DialogUI.getDialogUI(this);
		btFileChooser.addActionListener((e) -> {
			Optional<File> result = guiController.showSaveDialog(dialogUI.getDialog(), ".json");
			if (result.isPresent()) {
				File selectedFile = result.get();
				tfPath.setText(selectedFile.getPath());
			}
		});

	}

	public Optional<Diagram> showNewDiagramDialog() {
		result = null;
		DialogUI.showDialogUI(dialogUI);
		return Optional.ofNullable(result);
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
		return D.getLabel(D.DIALOG_TITLE_NEW_DIAGRAM);
	}

	@Override
	public Answer[] getAnswerOptions() {
		return AnswerOption.SAVE_CANCEL.getAnswers();
	}

	@Override
	public Answer getDefaultAnswer() {
		return Answer.CANCEL;
	}

	@Override
	public void onAnswer(Answer answer, JDialog parent) {
		if (answer == Answer.SAVE) {

			File selection = new File(tfPath.getText());
			if (!selection.getName().endsWith(".json")) {
				selection = new File(selection.getParent(), selection.getName() + ".json");
			}
			DiagramType type;
			if (rbUML.isSelected()) {
				type = DiagramType.UML;
			} else {
				type = DiagramType.ERD;
			}
			try {
				Diagram diagram = new Diagram(guiController.getDiagramController(), selection, type);
				diagram.persist();
				result = diagram;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		parent.setVisible(false);
	}

	@Override
	public void loadLabels() {
		rbUML.setText(D.getLabel(D.CREATE_DIAGRAM_UML));
		rbERD.setText(D.getLabel(D.CREATE_DIAGRAM_ERD));
	}

}
