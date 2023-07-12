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
package com.github.epimethix.lumicore.swing.remoteai;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.remoteai.Generator;
import com.github.epimethix.lumicore.remoteai.TextQuery;
import com.github.epimethix.lumicore.swing.util.DialogUtils;
import com.github.epimethix.lumicore.swing.util.GridBagUtils;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

@SuppressWarnings("serial")
public class TextQueryPanel extends JPanel implements ActionListener, PropertyChangeListener, LabelsDisplayer {
	private final JFrame parent;
	private final Generator generator;
	private Supplier<File> outputDir;

	private final JTextArea taPrompt;
	private RSyntaxTextArea taResponse;
//	private final JPanel pnResponse;
	private final JButton btGenerate;
	private final JButton btSave;
	private final JButton btLoad;
	private final JButton btSaveDocument;
	private final JComboBox<String> cbModel;
	private final JComboBox<String> cbOutputFormat;
	private final JSlider slTemperature;
	private final JLabel lbTemperature;
	private final JSlider slMaxTokens;
	private final JLabel lbMaxTokens;
	private final JCheckBox ckReferences;
	private final JCheckBox ckCodeExamples;
	private final JProgressBar pbProgress;

	private TextQuery textQuery;
	private boolean hasChanges;
	private static File currentDirectory;
	private static String extension;
	private static final Map<String, String> typeToExtensionMap;
	private static final String[] typeKeys;

	static {
		List<String> typeKeysList = new ArrayList<>();
		typeToExtensionMap = new HashMap<>();
		typeToExtensionMap.put("Plain", ".txt");
		typeKeysList.add("Plain");
		typeToExtensionMap.put("Markdown", ".md");
		typeKeysList.add("Markdown");
		typeToExtensionMap.put("HTML", ".html");
		typeKeysList.add("HTML");
		typeToExtensionMap.put("Java", ".java");
		typeKeysList.add("Java");
		typeToExtensionMap.put("C++", ".cpp");
		typeKeysList.add("C++");
		typeToExtensionMap.put("Python", ".py");
		typeKeysList.add("Python");
		typeKeys = typeKeysList.toArray(new String[] {});
	}

	public TextQueryPanel(JFrame parent, Generator generator) {
		this(parent, generator, () -> {
			return DialogUtils.showOpenDialog(parent, currentDirectory, extension).orElseGet(null);
		});
	}

	public TextQueryPanel(JFrame parent, Generator generator, Supplier<File> outputDir) {
		super(new BorderLayout());
		this.parent = parent;
		this.generator = generator;
		this.outputDir = outputDir;

		taPrompt = new JTextArea();
		taPrompt.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),
				"ctrlEnterAction");
		taPrompt.getActionMap().put("ctrlEnterAction", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				generate();
			}
		});
		taResponse = new RSyntaxTextArea();
		taResponse.setLineWrap(true);
//		pnResponse = new JPanel(new BorderLayout());
		btGenerate = new JButton();
		btGenerate.addActionListener(this);
		btSave = new JButton();
		btSave.addActionListener(this);
		btLoad = new JButton();
		btLoad.addActionListener(this);
		btSaveDocument = new JButton();
		btSaveDocument.addActionListener(this);
		cbModel = new JComboBox<String>(generator.getTextModels());
		cbModel.setEditable(false);
		cbModel.setSelectedIndex(0);
		slTemperature = new JSlider(1, 100);
		slTemperature.setValue(70);
		lbTemperature = new JLabel(String.format("Temperature: %.2f", (slTemperature.getValue() / 100.0)));
		slTemperature.addChangeListener(e -> {
			lbTemperature.setText(String.format("Temperature: %.2f", (slTemperature.getValue() / 100.0)));
		});
		slMaxTokens = new JSlider(1, generator.getMaxTokensMax(cbModel.getSelectedItem().toString()));
		lbMaxTokens = new JLabel(String.format("Max Tokens: %,d", slMaxTokens.getValue()));
		slMaxTokens.addChangeListener(e -> {
			lbMaxTokens.setText(String.format("Max Tokens: %,d", slMaxTokens.getValue()));
		});
		cbOutputFormat = new JComboBox<String>(typeKeys);
		ckReferences = new JCheckBox("References");
		ckCodeExamples = new JCheckBox("Code Examples");
		pbProgress = new JProgressBar();

		JPanel pnEditor = new JPanel(new GridBagLayout());
		GridBagConstraints c = GridBagUtils.initGridBagConstraints();

		pnEditor.add(btGenerate, c);
		c.gridx++;
		pnEditor.add(cbModel, c);
		c.gridx++;
		pnEditor.add(lbTemperature, c);
		c.gridx++;
		pnEditor.add(slTemperature, c);
		c.gridx++;
		pnEditor.add(lbMaxTokens, c);
		c.gridx++;
		pnEditor.add(slMaxTokens, c);
		int width = c.gridx + 2;
		c.gridx = 0;
		c.gridy++;

		pnEditor.add(btSave, c);
		c.gridx++;
		pnEditor.add(btSaveDocument, c);
		c.gridx++;
		pnEditor.add(cbOutputFormat, c);
		c.gridx++;
		pnEditor.add(ckCodeExamples, c);
		c.gridx++;
		pnEditor.add(ckReferences, c);
		c.gridx++;
		c.weightx = 1.0d;
		c.gridwidth = width - c.gridx;
		pnEditor.add(new JPanel(), c);
		c.gridwidth = width;
		c.gridx = 0;
		c.gridy++;
		pnEditor.add(pbProgress, c);

		add(pnEditor, BorderLayout.NORTH);

		JSplitPane spText = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		spText.setDividerLocation(250);
		spText.add(LayoutUtils.initScrollPane(taPrompt));
		spText.add(LayoutUtils.initScrollPane(taResponse));
		add(spText, BorderLayout.CENTER);

//		generator.textQuery(prompt, model, maxTokens, temperature);
	}

	private String getPrompt() {
		StringBuilder b = new StringBuilder().append(taPrompt.getText());
		if (!"Plain".equals(cbOutputFormat.getSelectedItem().toString())) {
			b.append("\n\n").append("please output ").append(cbOutputFormat.getSelectedItem().toString()).append(".");
		}
		if (ckReferences.isSelected()) {
			b.append("\n\n").append("please provide links to documentation of what you are describing.")
					.append("make sure you provide only working weblinks.");
		}
		if (ckCodeExamples.isSelected()) {
			b.append("\n\n").append("please provide code examples.");
		}
		return b.toString();
	}

	private void generate() {
		if (hasChanges) {
			boolean abortGenerate = JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(parent,
					"The current response is not saved yet and will be lost if you continue!", "Unsaved Changes",
					JOptionPane.OK_CANCEL_OPTION);
			if (abortGenerate) {
				return;
			}
		}

		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			private final String model = cbModel.getSelectedItem().toString();
			private final int maxTokens = slMaxTokens.getValue();
			private final float temperature = (float) (slTemperature.getValue() / 100.0f);
			private final String prompt = getPrompt();
			private final String outputFormat = cbOutputFormat.getSelectedItem().toString();
			private String response;
			{
				pbProgress.setIndeterminate(true);
				btSave.setEnabled(false);
				btSaveDocument.setEnabled(false);
				btGenerate.setEnabled(false);
			}

			@Override
			protected Void doInBackground() throws Exception {
				if (taPrompt.getText().trim().length() > 0) {
					try {
						response = generator.textQuery(prompt, model, maxTokens, temperature).trim();
//						System.out.println(response);
					} catch (Exception e) {
						response = e.getMessage();
						System.err.println(response);
					}
					textQuery = new TextQuery(prompt, "", outputFormat, model, maxTokens, temperature, response);
				}
				firePropertyChange("progress", 0, 100);
				return null;
			}

			protected void done() {
				if ("Markdown".equals(outputFormat)) {
					taResponse.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
				} else if ("Java".equals(outputFormat)) {
					taResponse.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
				} else if ("C++".equals(outputFormat)) {
					taResponse.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
				} else if ("C".equals(outputFormat)) {
					taResponse.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
				} else {
					taResponse.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
				}
				taResponse.setText(response);
				btSave.setEnabled(true);
				btSaveDocument.setEnabled(true);
				btGenerate.setEnabled(true);
				hasChanges = true;
				removePropertyChangeListener(TextQueryPanel.this);
			}
		};
		worker.addPropertyChangeListener(this);
		worker.execute();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btGenerate) {
			generate();
		} else if (e.getSource() == btSave) {
			JTextField tfFileName = new JTextField(30);
			JLabel lbMessageText = new JLabel("Please enter file name:");
			JPanel pnMessage = new JPanel(new BorderLayout());
			pnMessage.add(lbMessageText, BorderLayout.NORTH);
			pnMessage.add(tfFileName, BorderLayout.CENTER);
			int answer = JOptionPane.showConfirmDialog(parent, pnMessage, "Select File Name",
					JOptionPane.OK_CANCEL_OPTION);
			if (answer == JOptionPane.OK_OPTION) {
				if (Objects.nonNull(textQuery)) {
					String fileName = tfFileName.getText();
					if (fileName.length() == 0) {
						fileName = UUID.randomUUID().toString();
					}
					String extension;
					if (!fileName.endsWith(extension = ".gpt.json")) {
						fileName += extension;
					}
					File outputFile = new File(outputDir.get(), fileName);
					ObjectMapper om = new ObjectMapper();
					try {
						om.writerWithDefaultPrettyPrinter().writeValue(outputFile, textQuery);
						hasChanges = false;
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		} else if (e.getSource() == btSaveDocument) {
			JTextField tfFileName = new JTextField(30);
			JLabel lbMessageText = new JLabel("Please enter file name:");
			JPanel pnMessage = new JPanel(new BorderLayout());
			pnMessage.add(lbMessageText, BorderLayout.NORTH);
			pnMessage.add(tfFileName, BorderLayout.CENTER);
			int answer = JOptionPane.showConfirmDialog(parent, pnMessage, "Select File Name",
					JOptionPane.OK_CANCEL_OPTION);
			if (answer == JOptionPane.OK_OPTION) {
				if (Objects.nonNull(textQuery)) {
					String fileName = tfFileName.getText().trim();
					if (fileName.length() == 0) {
						fileName = UUID.randomUUID().toString();
					}
					String extension;
					if (!fileName.endsWith(extension = typeToExtensionMap.get(textQuery.getOutputFormat()))) {
						fileName += extension;
					}
					File outputFile = new File(outputDir.get(), fileName);
					try {
						Files.writeString(outputFile.toPath(), taResponse.getText());
						hasChanges = false;
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress".equals(evt.getPropertyName())) {
			int value = (int) evt.getNewValue();
			if (pbProgress.isIndeterminate()) {
				pbProgress.setIndeterminate(false);
			}
			pbProgress.setValue(value);
		}
	}

	@Override
	public void loadLabels() {
		btGenerate.setText(C.getLabel(C.BUTTON_GENERATE));
		btSave.setText(C.getLabel(C.BUTTON_SAVE));
		btLoad.setText(C.getLabel(C.BUTTON_OPEN));
		btSaveDocument.setText(C.getLabel(C.BUTTON_SAVE_DOCUMENT));
		ckCodeExamples.setText(C.getLabel(C.CHECKBOX_CODE_EXAMPLES));
		ckReferences.setText(C.getLabel(C.CHECKBOX_REFERENCES));
	}
}
