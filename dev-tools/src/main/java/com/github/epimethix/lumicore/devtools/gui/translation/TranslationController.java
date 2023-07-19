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

import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.devtools.gui.D;
import com.github.epimethix.lumicore.devtools.gui.GUIController;
import com.github.epimethix.lumicore.devtools.gui.translation.dialog.TranslateDialog;
import com.github.epimethix.lumicore.devtools.service.ProjectService;
import com.github.epimethix.lumicore.ioc.annotation.Autowired;
import com.github.epimethix.lumicore.ioc.annotation.PostConstruct;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;
import com.github.epimethix.lumicore.sourceutil.ProjectSource;
import com.github.epimethix.lumicore.swing.control.LTextField;
import com.github.epimethix.lumicore.swing.util.DialogUtils;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

@SwingComponent
public class TranslationController {
	@Autowired
	private GUIController guiController;

	@Autowired
	private TranslateDialog translateDialog;

	@Autowired
	private ProjectService projectService;

	private final Map<String, TranslationModel> modelMap = new HashMap<>();

	public TranslationController() {}

	@PostConstruct
	public final void init() {
		Set<File> projects = projectService.getSiblingProjects();
		for (File project : projects) {
//			System.err.println("TranslationController::init() " + project.getName());
			List<TranslationModel> models = projectService.getTranslationModels(project);
			for (TranslationModel model : models) {
				modelMap.put(model.getName(), model);
			}
		}
	}

	public void newBundle() {

	}

	public void openBundle() {
		Optional<File> optFile = DialogUtils.showOpenDialog(guiController.getFrame(), new File("src/main/resources"),
				".properties");
		if (optFile.isPresent()) {
			File bundleFile = optFile.get();
			if (!bundleFile.getName().contains("_") || bundleFile.getName().startsWith("_")) {
				guiController.showErrorMessage(D.INVALID_BUNDLE_FILE_NAME, bundleFile.getName());

				return;
			}
			File parentDir = null;

			for (File project : projectService.getSiblingProjects()) {
				if (bundleFile.getPath().startsWith(project.getPath())) {
					parentDir = project;
					break;
				}
			}
			if (Objects.nonNull(parentDir)) {
				ProjectSource ps = projectService.loadProjectSource(parentDir);
				String basePackage = ps.getBasePackage();
				if (Objects.nonNull(parentDir)) {
					guiController.showPlainMessage(D.MESSAGE_SELECT_CONSTANTS_FILE);
					File packageFile = ProjectSource.getPackageFile(new File(parentDir, "src/main/java"), basePackage);
					optFile = DialogUtils.showOpenDialog(guiController.getFrame(), packageFile, ".java");

					if (optFile.isPresent()) {
						String bundleName = bundleFile.getName().substring(0, bundleFile.getName().indexOf("_"));
//				System.err.println(bundleName);
						File constantsFile = optFile.get();
						try {
							TranslationModel tm = new TranslationModel(parentDir, bundleName, constantsFile,
									bundleFile);
							guiController.openTranslationTab(tm);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

//	public void showSaveDialog(TranslationModel model) throws StreamWriteException, DatabindException, IOException {
//		Optional<File> optFile = DialogUtils.showSaveDialog(guiController.getFrame(),
//				new File("src/dev/resources").getAbsoluteFile(), ".i18n");
//		if (optFile.isPresent()) {
//			File file = optFile.get();
//			model.persist(file);
//		}
//	}

	public void openI18N() {
		List<String> knownBundles = new ArrayList<>(modelMap.keySet());
		Collections.sort(knownBundles);
		JList<String> lsBundles = new JList<>(knownBundles.toArray(new String[] {}));
		lsBundles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		boolean proceed = JOptionPane.showConfirmDialog(guiController.getFrame(), LayoutUtils.initScrollPane(lsBundles),
				"I18N", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
		int selectedIndex = lsBundles.getSelectedIndex();
		if (!proceed || selectedIndex == -1) {
			return;
		}
		TranslationModel model = modelMap.get(knownBundles.get(selectedIndex));
		guiController.openTranslationTab(model);
//		Optional<File> optFile = DialogUtils.showOpenDialog(guiController.getFrame(), new File("src/dev/resources"),
//				".i18n");
//		if (optFile.isPresent()) {
//			File f = optFile.get();
//			try {
//				TranslationModel model = new TranslationModel(f);
//				guiController.openTranslationTab(model);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}

	public void showTranslateDialog(TranslationModel model) {
		translateDialog.showDialog(model);
	}

	public boolean shouldOverwriteExistingI18NDialog(String name) {
		Answer a = guiController.showYesNoDialog(D.MESSAGE_I18N_ALREADY_EXISTS, name);
		return a == Answer.YES;
	}

	public void showNewLocaleDialog(TranslationModel model) {
		JLabel message = new JLabel(D.getLabel(D.MESSAGE_NEW_LOCALE));
		LTextField tfLocaleName = new LTextField(10);
		JPanel pnEditor = new JPanel(new FlowLayout());
		pnEditor.add(message);
		pnEditor.add(tfLocaleName);
		boolean proceed = JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(guiController.getFrame(), pnEditor,
				D.getLabel(D.MESSAGE_NEW_LOCALE_TITLE), JOptionPane.OK_CANCEL_OPTION);
		if (proceed) {
			model.createNewLocale(tfLocaleName.getText());
		}

	}

	public void showAddLocalesToView(TranslationModel model) {
		String[] hidden = model.getHiddenLocales();
		if (hidden.length > 0) {
			JPanel pnEditor = new JPanel(new FlowLayout());
			pnEditor.add(new JLabel(D.getLabel(D.MESSAGE_ADD_LOCALES)));
			JCheckBox[] checkBoxes = new JCheckBox[hidden.length];

			for (int i = 0; i < checkBoxes.length; i++) {
				checkBoxes[i] = new JCheckBox(hidden[i]);
				pnEditor.add(checkBoxes[i]);
			}
			boolean proceed = JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(guiController.getFrame(), pnEditor,
					D.getLabel(D.MESSAGE_ADD_LOCALES_TITLE), JOptionPane.OK_CANCEL_OPTION);
			if (proceed) {
				for (JCheckBox checkBox : checkBoxes) {
					if (checkBox.isSelected()) {
						model.setLoaded(checkBox.getText(), true);
					}
				}
			}
		}
	}

}
