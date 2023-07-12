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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Objects;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.AnswerOption;
import com.github.epimethix.lumicore.common.ui.labels.displayer.IgnoreLabels;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.devtools.ClassPathIndex;
import com.github.epimethix.lumicore.devtools.gui.D;
import com.github.epimethix.lumicore.devtools.gui.GUIController;
import com.github.epimethix.lumicore.devtools.gui.ListComboBoxModel;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.Diagram;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramEntity;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;
import com.github.epimethix.lumicore.swing.dialog.Dialog;
import com.github.epimethix.lumicore.swing.dialog.DialogUI;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

@SwingComponent
public class AddEntityDialog implements Dialog, LabelsDisplayer {

	private final JPanel pnEditor;
	private final JList<String> lsFullNames;

	private final GUIController uic;
	private final DefaultListModel<String> model;
	@IgnoreLabels
	private Diagram diagram;

	private final DialogUI dui;
	private final JLabel lbClassName;
	private final JComboBox<String> cbSimpleName;

	public AddEntityDialog(GUIController uic) {
		this.uic = uic;
		lbClassName = new JLabel();
		cbSimpleName = new JComboBox<String>();
		model = new DefaultListModel<>();
		cbSimpleName.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyReleased(KeyEvent e) {
				model.clear();
				model.addAll(ClassPathIndex.resolveNames(cbSimpleName.getSelectedItem().toString()));
			}

			@Override
			public void keyPressed(KeyEvent e) {}
		});
		cbSimpleName.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.clear();
				if (Objects.nonNull(cbSimpleName.getSelectedItem())) {
					model.addAll(ClassPathIndex.resolveNames(cbSimpleName.getSelectedItem().toString()));
				}
			}
		});
		lsFullNames = new JList<>(model);

		pnEditor = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.insets = LayoutUtils.createDefaultMargin();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.weighty = 0.0;

		pnEditor.add(lbClassName, c);
		c.gridx++;
		pnEditor.add(cbSimpleName, c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		pnEditor.add(LayoutUtils.initScrollPane(lsFullNames), c);

		dui = DialogUI.getDialogUI(this);
	}
	
	private boolean initialized;

	public final void showDialog(Diagram diagram) {
		this.diagram = diagram;
		if(!initialized) {
			cbSimpleName.setModel(new ListComboBoxModel<String>(ClassPathIndex.getSimpleNames()));
			AutoCompleteDecorator.decorate(cbSimpleName);
			initialized = true;
		}
		DialogUI.showDialogUI(dui);
	}

	@Override
	public Component getUI() {
		return pnEditor;
	}

	@Override
	public void clear() {
		model.clear();
		cbSimpleName.getEditor().setItem("");
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getTitle() {
		return D.getLabel(D.DIALOG_TITLE_ADD_ENTITY);
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
		if (answer.equals(Answer.OK)) {
			int selectedIndex = lsFullNames.getSelectedIndex();
			if (selectedIndex != -1) {
				String className = model.get(selectedIndex);
				DiagramEntity de = null;
				try {
					de = uic.getDiagramController().getDiagramEntity(className);
					if (Objects.nonNull(de)) {
						diagram.addEntity(className, de);
						parent.setVisible(false);
						return;
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				uic.showErrorMessage(parent, D.ERROR_MSG_COULD_NOT_LOAD_CLASS, className);
			} else {
				uic.showErrorMessage(parent, D.ERROR_MSG_SELECT_CLASS);
			}
		} else {
			parent.setVisible(false);
		}
	}

	@Override
	public void loadLabels() {
		lbClassName.setText(D.getLabel(D.CLASS_NAME));
	}

	@Override
	public Component getParent() {
		return uic.getFrame();
	}

}
