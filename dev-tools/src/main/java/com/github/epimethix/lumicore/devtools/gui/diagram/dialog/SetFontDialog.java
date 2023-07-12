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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.AnswerOption;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.devtools.gui.D;
import com.github.epimethix.lumicore.devtools.gui.GUIController;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.Diagram;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;
import com.github.epimethix.lumicore.swing.dialog.Dialog;
import com.github.epimethix.lumicore.swing.dialog.DialogUI;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

@SwingComponent
public class SetFontDialog implements Dialog, LabelsDisplayer {
	private final JLabel lbFontSize;
	private final JComboBox<String> cbFontSize;
	private final JPanel pnEditor;
	
	private Diagram diagram;

	private final GUIController guiController;
	
	private final DialogUI dialogUI;
	
	public SetFontDialog(GUIController guiController) {
		this.guiController = guiController;
		lbFontSize = new JLabel();
		cbFontSize = new JComboBox<>(new String[] { "10", "12", "14", "16", "18", "20", "22", "24" });
		cbFontSize.setEditable(true);
//		cbFontSize.addActionListener(this);
		cbFontSize.addActionListener(e -> {
			try {
				int fontSize = Integer.parseInt(cbFontSize.getSelectedItem().toString());
				diagram.setFontSize(fontSize);
			} catch (NumberFormatException e2) {
				cbFontSize.setSelectedItem(String.valueOf(diagram.getFontSize()));
			}
		});

		pnEditor = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = LayoutUtils.createDefaultMargin();
		pnEditor.add(lbFontSize, c);
		c.gridx++;
		c.weightx = 1.0;
		pnEditor.add(cbFontSize, c);
		dialogUI = DialogUI.getDialogUI(this);
	}
	
	public final void showSetFontDialog(Diagram diagram) {
		this.diagram = diagram;
		cbFontSize.setSelectedItem(String.valueOf(diagram.getFontSize()));
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
		return D.getLabel(D.FONT_SIZE);
	}

	@Override
	public Answer[] getAnswerOptions() {
		return AnswerOption.OK_CANCEL.getAnswers();
	}

	@Override
	public Answer getDefaultAnswer() {
		return Answer.CANCEL;
	}

	@Override
	public void onAnswer(Answer answer, JDialog parent) {
		if(answer == Answer.OK) {
			try {
				int fontSize = Integer.parseInt(cbFontSize.getSelectedItem().toString());
				diagram.setFontSize(fontSize);
			} catch (NumberFormatException e2) {
				cbFontSize.setSelectedItem(String.valueOf(diagram.getFontSize()));
			}
		}
		parent.setVisible(false);
	}

	@Override
	public void loadLabels() {
		lbFontSize.setText(D.getLabel(D.FONT_SIZE));
	}

}
