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
import java.util.ArrayList;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.AnswerOption;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.devtools.fs.DiagramData;
import com.github.epimethix.lumicore.devtools.gui.D;
import com.github.epimethix.lumicore.devtools.gui.GUIController;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramEntityConfiguration;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;
import com.github.epimethix.lumicore.swing.dialog.Dialog;
import com.github.epimethix.lumicore.swing.dialog.DialogUI;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

@SwingComponent
public class PropertiesDialog implements Dialog, LabelsDisplayer {

	private final GUIController guiController;
	private final JCheckBox ckShowStaticFields;
	private final JCheckBox ckShowInstanceFields;
	private final JCheckBox ckShowConstructors;
	private final JCheckBox ckShowStaticMethods;
	private final JCheckBox ckShowInstanceMethods;
	private final JLabel lbMessage;
	private final DialogUI dialogUI;
	private Map<String, DiagramData> data;
	private ArrayList<String> keys;
	private final JPanel pnEditor;

	public PropertiesDialog(GUIController guiController) {
		this.guiController = guiController;
		ckShowStaticFields = new JCheckBox(D.getLabel(D.DIALOG_SHOW_STATIC_FIELDS));
		ckShowInstanceFields = new JCheckBox(D.getLabel(D.DIALOG_SHOW_INSTANCE_FIELDS));
		ckShowConstructors = new JCheckBox(D.getLabel(D.DIALOG_SHOW_CONSTRUCTORS));
		ckShowStaticMethods = new JCheckBox(D.getLabel(D.DIALOG_SHOW_STATIC_METHODS));
		ckShowInstanceMethods = new JCheckBox(D.getLabel(D.DIALOG_SHOW_INSTANCE_METHODS));
		lbMessage = new JLabel();
		pnEditor = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = LayoutUtils.createDefaultMargin();
		pnEditor.add(lbMessage, c);
		c.gridy++;
		pnEditor.add(ckShowStaticFields, c);
		c.gridy++;
		pnEditor.add(ckShowInstanceFields, c);
		c.gridy++;
		pnEditor.add(ckShowConstructors, c);
		c.gridy++;
		pnEditor.add(ckShowStaticMethods, c);
		c.gridy++;
		pnEditor.add(ckShowInstanceMethods, c);
		this.dialogUI = DialogUI.getDialogUI(this);
	}

	public final void showPropertiesDialog(Map<String, DiagramData> data) {
		this.data = data;
		this.keys = new ArrayList<>(data.keySet());
		if (keys.size() == 0) {
//			guiController.showInfoMessage(null, D.getLabel(Key.DIALOG_PROPERTIES_MESSAGE_NOTHING_TO_CONFIGURE, keys.get(0)));
			return;
		}
		DialogUI.showDialogUI(dialogUI);
	}

	public void clear() {
		if (keys.size() == 1) {
			lbMessage.setText(D.getLabel(D.DIALOG_PROPERTIES_MESSAGE, keys.get(0)));
			DiagramData dd = data.get(keys.get(0));
			DiagramEntityConfiguration conf = dd.getConfiguration();
			ckShowStaticFields.setSelected(conf.isShowStaticFields());
			ckShowInstanceFields.setSelected(conf.isShowInstanceFields());
			ckShowConstructors.setSelected(conf.isShowConstructors());
			ckShowStaticMethods.setSelected(conf.isShowStaticMethods());
			ckShowInstanceMethods.setSelected(conf.isShowInstanceMethods());
		} else {
			lbMessage.setText(D.getLabel(D.DIALOG_PROPERTIES_MESSAGE_ALL));
			boolean showStaticFields = false;
			boolean showInstanceFields = false;
			boolean showConstructors = false;
			boolean showStaticMethods = false;
			boolean showInstanceMethods = false;
			boolean global = true;
			boolean firstRound = true;
			for (String key : keys) {
				DiagramData item = data.get(key);
				DiagramEntityConfiguration dec = item.getConfiguration();
				if (firstRound) {
					showStaticFields = dec.isShowStaticFields();
					showInstanceFields = dec.isShowInstanceFields();
					showConstructors = dec.isShowConstructors();
					showStaticMethods = dec.isShowStaticMethods();
					showInstanceMethods = dec.isShowInstanceMethods();
					firstRound = false;
				} else {
					if (dec.isShowStaticFields() != showStaticFields) {
						global = false;
						break;
					}
					if (dec.isShowStaticMethods() != showStaticMethods) {
						global = false;
						break;
					}
					if (dec.isShowInstanceFields() != showInstanceFields) {
						global = false;
						break;
					}
					if (dec.isShowInstanceMethods() != showInstanceMethods) {
						global = false;
						break;
					}
					if (dec.isShowConstructors() != showConstructors) {
						global = false;
						break;
					}
				}
			}
			if (global) {
				ckShowStaticFields.setSelected(showStaticFields);
				ckShowInstanceFields.setSelected(showInstanceFields);
				ckShowConstructors.setSelected(showConstructors);
				ckShowStaticMethods.setSelected(showStaticMethods);
				ckShowInstanceMethods.setSelected(showInstanceMethods);
			} else {
				ckShowStaticFields.setSelected(false);
				ckShowInstanceFields.setSelected(false);
				ckShowConstructors.setSelected(false);
				ckShowStaticMethods.setSelected(false);
				ckShowInstanceMethods.setSelected(false);
			}
		}
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
		return D.getLabel(D.DIALOG_PROPERTIES_TITLE);
	}

	@Override
	public Answer[] getAnswerOptions() {
		return AnswerOption.OK_CANCEL.getAnswers();
	}

	@Override
	public void onAnswer(Answer answer, JDialog parent) {
		if (answer == Answer.OK) {
			for (String key : keys) {
				DiagramEntityConfiguration dec = data.get(key).getConfiguration();
				dec.setShowStaticFields(ckShowStaticFields.isSelected());
				dec.setShowInstanceFields(ckShowInstanceFields.isSelected());
				dec.setShowConstructors(ckShowConstructors.isSelected());
				dec.setShowStaticMethods(ckShowStaticMethods.isSelected());
				dec.setShowInstanceMethods(ckShowInstanceMethods.isSelected());
			}
		}
		parent.setVisible(false);
	}

	@Override
	public void loadLabels() {}
}
