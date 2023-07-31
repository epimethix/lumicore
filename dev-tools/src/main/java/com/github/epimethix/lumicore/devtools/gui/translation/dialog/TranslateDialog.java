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
package com.github.epimethix.lumicore.devtools.gui.translation.dialog;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingWorker;

import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.AnswerOption;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.devtools.DevTools;
import com.github.epimethix.lumicore.devtools.gui.D;
import com.github.epimethix.lumicore.devtools.gui.DevToolsGUIController;
import com.github.epimethix.lumicore.devtools.gui.translation.TranslationModel;
import com.github.epimethix.lumicore.ioc.annotation.Autowired;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;
import com.github.epimethix.lumicore.remoteai.Generator;
import com.github.epimethix.lumicore.remoteai.OpenAI;
import com.github.epimethix.lumicore.swing.dialog.AbstractDialog;
import com.github.epimethix.lumicore.swing.dialog.DialogUI;
import com.github.epimethix.lumicore.swing.util.GridBagUtils;

@SwingComponent
public final class TranslateDialog extends AbstractDialog {
	@SuppressWarnings("serial")
	private final static class TranslateDialogUI extends JPanel implements LabelsDisplayer {
		private final JPanel pnFromRadios;
		private final JPanel pnToCheckBoxes;
		private final JLabel lbFrom;
		private final JLabel lbTo;
		private JRadioButton[] radios;
		private JCheckBox[] checks;

		public TranslateDialogUI() {
			super(new GridBagLayout());
			pnFromRadios = new JPanel(new FlowLayout(FlowLayout.LEADING));
			pnToCheckBoxes = new JPanel(new FlowLayout(FlowLayout.LEADING));
			lbFrom = new JLabel();
			lbTo = new JLabel();

			GridBagConstraints c = GridBagUtils.initGridBagConstraints();
			GridBagUtils.addGridBagLine(this, c, lbFrom, pnFromRadios);
			GridBagUtils.addGridBagLine(this, c, lbTo, pnToCheckBoxes);
			GridBagUtils.finishGridBagForm(this, c, 2);

		}

		@Override
		public void loadLabels() {
			lbFrom.setText(D.getLabel(D.TRANSLATE_DIALOG_FROM));
			lbTo.setText(D.getLabel(D.TRANSLATE_DIALOG_TO));
		}

		public void load(TranslationModel model) {
			pnFromRadios.removeAll();
			pnToCheckBoxes.removeAll();
			String[] locales = model.getLoadedLocales();
			radios = new JRadioButton[locales.length];
			checks = new JCheckBox[locales.length];
			ButtonGroup bg = new ButtonGroup();
			for (int i = 0; i < locales.length; i++) {
//				final int index = i;
				radios[i] = new JRadioButton(locales[i]);
				bg.add(radios[i]);
				radios[i].addActionListener(e -> {
					JRadioButton radio = (JRadioButton) e.getSource();
					for (JCheckBox ck : checks) {
						if (ck.getText().equals(radio.getText())) {
							ck.setSelected(false);
						} else {
							ck.setSelected(true);
						}
					}
				});
				pnFromRadios.add(radios[i]);
				checks[i] = new JCheckBox(locales[i]);
				pnToCheckBoxes.add(checks[i]);
			}
			revalidate();
		}

		public String getLocaleToTranslateFrom() {
			for (JRadioButton rb : radios) {
				if (rb.isSelected()) {
					return rb.getText();
				}
			}
			return null;
		}

		public String[] getLocalesToTranslateTo(String from) {
			List<String> l = new ArrayList<>();
			for (JCheckBox check : checks) {
				if (check.isSelected() && !check.getText().equals(from)) {
					l.add(check.getText());
				}
			}
			return l.toArray(new String[] {});
		}
	}

	public final static class TranslationJob {
		private final String langFrom;
		private final String langTo;
		private final String localeTo;
		private final List<String> keys;
		private final List<String> labelsFrom;
		private final List<String> labelsTo;

		private TranslationJob(String localeFrom, String localeTo) {
			this.langFrom = new Locale(localeFrom).getDisplayLanguage(Locale.ENGLISH);
			this.langTo = new Locale(localeTo).getDisplayLanguage(Locale.ENGLISH);
			this.localeTo = localeTo;
			this.labelsFrom = new ArrayList<>();
			this.labelsTo = new ArrayList<>();
			this.keys = new ArrayList<>();
		}

		private void addLabel(String key, String label) {
			System.err.println("Key: " + key + " // Label: " + label);
			keys.add(key);
			labelsFrom.add(label);
		}

		public String getLocaleTo() {
			return localeTo;
		}

		public String getLabel(String key) {
			int index = keys.indexOf(key);
			return index > -1 ? labelsTo.get(index) : "";
		}

		public Set<String> keySet() {
			return new HashSet<>(keys);
		}

		private void appendList(StringBuilder query) {
			for (int i = 0; i < keys.size(); i++) {
				query.append(i + 1).append(". ").append(labelsFrom.get(i)).append("\n");
			}
		}

		public void putResult(String result) {
//			System.err.println(result);
			String[] lines = result.trim().split("\n");
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				System.err.println((i + 1) + "::" + line);
				String labelTo = line.substring(line.indexOf(".") + 1).trim();
				labelsTo.add(labelTo);
			}
		}
	}

	private final TranslateDialogUI ui;
	private final DialogUI dialogUI;

	private TranslationModel model;

	@Autowired
	private DevTools app;

	private final DevToolsGUIController guiController;

	public TranslateDialog(DevToolsGUIController guiController) {
		super(guiController.getFrame(), () -> D.getLabel(D.TRANSLATE_DIALOG_TITLE), ICON_EDIT, new TranslateDialogUI(),
				AnswerOption.OK_CANCEL);
		this.guiController = guiController;
		this.ui = (TranslateDialogUI) getUI();
		this.dialogUI = DialogUI.getDialogUI(this);
	}

	public final void showDialog(TranslationModel model) {
		this.model = model;
		this.ui.load(model);
		DialogUI.showDialogUI(dialogUI);
	}

	@Override
	public void onAnswer(Answer answer, JDialog parent) {
		if (answer == Answer.OK) {
			String from = ui.getLocaleToTranslateFrom();
			if (Objects.nonNull(from)) {
				String[] to = ui.getLocalesToTranslateTo(from);
				if (to.length > 0) {
//					List<String> toTranslate = new ArrayList<>();
					Set<String> keySet = model.keySet();
//					List<?>[] missingListArray = new List<?>[to.length];
					Map<String, List<String>> missingLocaleKeyMap = new HashMap<>();
					for (String key : keySet) {
						List<String> missing = model.getMissingLocales(to, key);
						for (int i = 0; i < to.length; i++) {
							if (missing.contains(to[i])) {
								List<String> missingKeys = missingLocaleKeyMap.get(to[i]);
								if (Objects.isNull(missingKeys)) {
									missingKeys = new ArrayList<>();
									missingLocaleKeyMap.put(to[i], missingKeys);
								}
//								System.err.println("Missing key discovered: " + key + " for locale " + to[i]);
								missingKeys.add(key);
							}
						}
					}
					if (!missingLocaleKeyMap.isEmpty()) {
						Optional<Generator> optGenerator = guiController.getGenerator();
						if (optGenerator.isPresent()) {
							Generator g = optGenerator.get();
//							final TranslationJob[] jobs = new TranslationJob[to.length];
							final List<TranslationJob> jobList = new ArrayList<>();
							for (int i = 0; i < to.length; i++) {
								List<String> l = missingLocaleKeyMap.get(to[i]);
								if (Objects.nonNull(l)) {
//									jobs[i] = new TranslationJob(from, to[i]);
									TranslationJob j = new TranslationJob(from, to[i]);
									int size = 0;
									boolean wasAdded = false;
									for (String key : l) {
										String fromLabel = model.getLabel(from, key);
										if (Objects.nonNull(fromLabel)) {
											wasAdded = false;
											j.addLabel(key, fromLabel);
											size++;
										}
										if (size == 10) {
											jobList.add(j);
											wasAdded = true;
											j = new TranslationJob(from, to[i]);
											size = 0;
										}
									}
									if (!wasAdded) {
										jobList.add(j);
									}
								}
							}
							SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
								private String result;
								private Exception e;

								@Override
								protected Void doInBackground() throws Exception {
									try {

										for (TranslationJob job : jobList) {
											if (Objects.nonNull(job)) {
												StringBuilder query = new StringBuilder();
												query.append(
														"Please repeat the following list of GUI labels and translate it from ")
														.append(job.langFrom).append(" to ").append(job.langTo)
														.append(".\n\n");
												job.appendList(query);
												String prompt = query.toString();
												System.out.println(prompt);
												result = g.textQuery(prompt,
														OpenAI.TextModel.TEXT_DAVINCI_003.toString(), 500, 0.3f);
												System.err.println(result);
												job.putResult(result);
											}
//											model.clear();
//											model.refresh();
										}
									} catch (Exception e) {
										result = e.getMessage();
										this.e = e;

									}
									return null;
								}

								@Override
								protected void done() {
									if (Objects.nonNull(e)) {
										System.err.println(result);
										e.printStackTrace();
									} else {
										for (TranslationJob job : jobList) {
											if (Objects.nonNull(job)) {
												model.update(job);
											}
										}
									}
									super.done();
								}

							};
							worker.execute();
							try {
								worker.get();
							} catch (InterruptedException | ExecutionException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			}
		}
		parent.setVisible(false);
	}

}
