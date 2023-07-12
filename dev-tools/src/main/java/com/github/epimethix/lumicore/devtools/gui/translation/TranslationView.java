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
package com.github.epimethix.lumicore.devtools.gui.translation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.github.epimethix.lumicore.common.swing.MutableFileDocument;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.devtools.gui.D;
import com.github.epimethix.lumicore.devtools.gui.translation.TranslationModel.LabelTextField;
import com.github.epimethix.lumicore.swing.util.GridBagUtils;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

//@SwingComponent
@SuppressWarnings("serial")
public class TranslationView extends JPanel implements MutableFileDocument, LabelsDisplayer, ActionListener {

//	private final JButton btOpen;
	private final JButton btSave;
	private final JButton btReload;
	private final JButton btNew;
	private final JButton btAdd;
	private final JButton btTranslate;
//	private final JComboBox<String> cbLabelsManagers;
	private final JCheckBox ckShowAll;

	private final JPanel pnEditor;
	private final JLabel lbTitleKey;

	private final TranslationModel model;

	private final TranslationController translationController;
	private boolean empty;

	public TranslationView(TranslationModel model, TranslationController translationController) {
		super(new BorderLayout());
		this.model = model;
		this.translationController = translationController;
//		btOpen = new JButton();
//		btOpen.addActionListener(this);
		empty = true;
		btSave = new JButton();
		btSave.addActionListener(this);
		btReload = new JButton();
		btReload.addActionListener(this);
		btNew = new JButton();
		btNew.addActionListener(this);
		btAdd = new JButton();
		btAdd.addActionListener(this);
		btTranslate = new JButton();
		btTranslate.addActionListener(this);
//		cbLabelsManagers = new JComboBox<>();
		ckShowAll = new JCheckBox();
		ckShowAll.addActionListener((e) -> refreshUI());
		lbTitleKey = LayoutUtils.getTitleLabel();
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
//		toolBar.add(btOpen);
		toolBar.add(btSave);
		toolBar.add(btReload);
		toolBar.add(btNew);
		toolBar.add(btAdd);
		toolBar.add(btTranslate);
		toolBar.add(ckShowAll);

		pnEditor = new JPanel(new GridBagLayout());

		add(toolBar, BorderLayout.NORTH);
		add(LayoutUtils.initScrollPane(pnEditor), BorderLayout.CENTER);

		refreshUI();
	}

	private void refreshUI() {
		pnEditor.removeAll();
//		model.clear();
		GridBagConstraints c = GridBagUtils.initGridBagConstraints();
		pnEditor.add(lbTitleKey, c);
		c.gridx++;
		String[] locales = model.getLoadedLocales();
		empty = true;
		int formWidth = 2;
		List<String> keys = new ArrayList<>(model.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			if (ckShowAll.isSelected() || !model.isComplete(key)) {
				if (empty) {
					empty = false;
					for (String locale : locales) {
						JButton removeLocale = new JButton("-");
						removeLocale.addActionListener(e -> {
							model.setLoaded(locale, false);
							refreshUI();
						});
						JPanel pnLocale = new JPanel(new FlowLayout(FlowLayout.LEADING));
						pnLocale.setBorder(LayoutUtils.createMediumEmptyBorder());
						JLabel lbLocale = new JLabel(locale);
						lbLocale.setBorder(LayoutUtils.createMediumEmptyBorder());
						pnLocale.add(lbLocale);
						pnLocale.add(removeLocale);
						pnEditor.add(pnLocale);
						c.gridx++;
					}
					formWidth = c.gridx + 1;
					c.gridy++;
					c.gridx = 0;
				}
				pnEditor.add(new JLabel(key), c);
				c.gridx++;
				for (int i = 0; i < locales.length; i++) {
					LabelTextField editor = model.getEditor(locales[i], key);
					pnEditor.add(LayoutUtils.initScrollPane(editor), c);
					c.gridx++;
				}
				c.gridy++;
				c.gridx = 0;
			}
		}

		if (empty) {
			c.gridx = 0;
			c.gridy++;
			lbTitleKey.setText(D.getLabel(D.TRANSLATION_MESSAGE_NOTHING_TO_DO));
		} else {
			lbTitleKey.setText(D.getLabel(D.TITLE_KEY));
		}

		GridBagUtils.finishGridBagForm(pnEditor, c, formWidth);

		pnEditor.revalidate();
		pnEditor.repaint();
	}

	@Override
	public boolean persist() {
		try {
			if (model.isNew()) {
				File parent = model.getParentFile();
				File modelFile = new File(parent, String.format("src/dev/resources/%s.i18n", model.getName()));
				if (modelFile.exists()) {
					boolean proceed = translationController.shouldOverwriteExistingI18NDialog(modelFile.getName());
					if (!proceed) {
						return false;
					}
				} else if (!modelFile.getParentFile().exists()) {
					modelFile.getParentFile().mkdirs();
				}
				model.persist(modelFile);
			} else {
				model.persist();
			}
			model.clear();
			refreshUI();
			return true;
		} catch (StreamWriteException e) {
			e.printStackTrace();
		} catch (DatabindException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean persist(File file) {
		try {
			model.persist(file);
			model.clear();
			refreshUI();
			return true;
		} catch (StreamWriteException e) {
			e.printStackTrace();
		} catch (DatabindException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		;
		return false;
	}

	@Override
	public File getDocumentFile() {
		return new File(model.getName());
	}

	@Override
	public boolean hasChanges() {
		return model.hasChanges();
	}

	@Override
	public String getDocumentName() {
		return model.getName();
	}

	@Override
	public void loadLabels() {
		btAdd.setText(C.getLabel(C.BUTTON_ADD));
		btNew.setText(C.getLabel(C.BUTTON_NEW));
//		btOpen.setText(A.getLabel(A.BUTTON_OPEN));
		btReload.setText(C.getLabel(C.BUTTON_RELOAD));
		btSave.setText(C.getLabel(C.BUTTON_SAVE));
		btTranslate.setText(D.getLabel(D.BUTTON_TRANSLATE));
		if (empty) {
			lbTitleKey.setText(D.getLabel(D.TRANSLATION_MESSAGE_NOTHING_TO_DO));
		} else {
			lbTitleKey.setText(D.getLabel(D.TITLE_KEY));
		}
		ckShowAll.setText(D.getLabel(D.CHECK_BOX_SHOW_ALL));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btSave) {
			persist();
		} else if (e.getSource() == btReload) {
			try {
				model.refresh();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			refreshUI();
		} else if (e.getSource() == btTranslate) {
			translationController.showTranslateDialog(model);
		} else if (e.getSource() == btNew) {
			translationController.showNewLocaleDialog(model);
			refreshUI();
		} else if (e.getSource() == btAdd) {
			translationController.showAddLocalesToView(model);
			refreshUI();
		}
	}

}
