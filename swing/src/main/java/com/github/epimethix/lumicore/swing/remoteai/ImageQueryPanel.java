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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.remoteai.Generator;
import com.github.epimethix.lumicore.remoteai.OpenAI.Resolution;
import com.github.epimethix.lumicore.swing.util.GridBagUtils;

@SuppressWarnings("serial")
public class ImageQueryPanel extends JPanel implements ActionListener, PropertyChangeListener, LabelsDisplayer {

	private final JPanel pnImages;
	private final JTextArea taPrompt;
	private final JButton btGenerate;
	private final JButton btSave;
	private final JProgressBar pbProgress;

	private final List<String> imageURLs;
	private final JComboBox<Resolution> cbResolution;
	private final BufferedImage[] images;
	private final JCheckBox[] imageSelection;
	private final JFrame parent;
	private final int nImages;

	private final Generator generator;

	private final Supplier<File> outputDir;
	private String prompt;
	private boolean hasChanges;

	public ImageQueryPanel(JFrame parent, int width, int height, Generator generator, Supplier<File> outputDir) {
		super(new BorderLayout());
		this.parent = parent;
		this.nImages = width * height;
		this.generator = generator;
		this.outputDir = outputDir;
		pnImages = new JPanel(new GridLayout(height, width));
		taPrompt = new JTextArea(3, 0);
//		taPrompt.getActionMap().put(parent, null);
		taPrompt.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),
				"ctrlEnterAction");
		taPrompt.getActionMap().put("ctrlEnterAction", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				generate();
			}
		});
		cbResolution = new JComboBox<>(new DefaultComboBoxModel<Resolution>() {
			@Override
			public int getSize() {
				return 3;
			}
			@Override
			public Resolution getElementAt(int index) {
				switch (index) {
				case 0:
					return Resolution.RES256X256;
				case 1:
					return Resolution.RES512X512;
				default:
					return Resolution.RES1024X1024;
				}
			}
		});
		cbResolution.setSelectedIndex(1);
		cbResolution.setEditable(false);
		btGenerate = new JButton("Generate!");
		btGenerate.addActionListener(this);
		btSave = new JButton("Save...");
		btSave.addActionListener(this);
		imageURLs = new ArrayList<>();
		images = new BufferedImage[4];
		pbProgress = new JProgressBar();
		JPanel pnEditor = new JPanel(new GridBagLayout());
		GridBagConstraints c = GridBagUtils.initGridBagConstraints();
//		c.gridx = c.gridy = 0;
//		c.fill = GridBagConstraints.HORIZONTAL;
		pnEditor.add(btGenerate, c);
		c.gridx++;
		pnEditor.add(btSave, c);
		c.gridx++;
		pnEditor.add(cbResolution);
		c.weightx = 1.0d;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 4;
		pnEditor.add(new JScrollPane(taPrompt), c);
		c.gridy++;
		pnEditor.add(pbProgress, c);
		add(pnEditor, BorderLayout.NORTH);
		add(new JScrollPane(pnImages), BorderLayout.CENTER);
		imageSelection = new JCheckBox[nImages];
		for (int i = 0; i < imageSelection.length; i++) {
			imageSelection[i] = new JCheckBox();
		}
//		setBorder(UIUtils.createMediumEmptyBorder());
	}

	private void generate() {
		if (hasChanges) {
			boolean abortGenerate = JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(parent,
					"The current images are not saved yet and will be lost if you continue!", "Unsaved Changes",
					JOptionPane.OK_CANCEL_OPTION);
			if (abortGenerate) {
				return;
			}
		}
		if (btSave.isEnabled()) {
			btSave.setEnabled(false);
			btGenerate.setEnabled(false);
		}
		pbProgress.setIndeterminate(true);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				try {
					ImageQueryPanel.this.prompt = taPrompt.getText();
					imageURLs.clear();
					imageURLs.addAll(generator.imageQuery(ImageQueryPanel.this.prompt,
							((Resolution) cbResolution.getSelectedItem()).toString(), nImages));
					Arrays.fill(images, null);
					firePropertyChange("progress", -1, 0);
					for (int i = 0; i < images.length;) {
						try {
							URL url = new URL(imageURLs.get(i));
							System.out.println(imageURLs.get(i));
							try (InputStream is = url.openStream()) {
								images[i] = ImageIO.read(is);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						setProgress(100 / nImages * ++i);
					}
					setProgress(100);
				} catch (UnknownHostException e1) {
					// client system is offline
					firePropertyChange("progress", -1, 0);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				return null;
			}

			@Override
			protected void done() {
				hasChanges = true;
				removePropertyChangeListener(ImageQueryPanel.this);
				refreshUI();
				super.done();
			}
		};
		worker.addPropertyChangeListener(this);
		worker.execute();
	}

	private void refreshUI() {
		pnImages.removeAll();
		for (int i = 0; i < images.length; i++) {
			if (Objects.nonNull(images[i])) {
				imageSelection[i].setSelected(true);
				JLabel lbImage = new JLabel(new ImageIcon(images[i]));
				JPanel pnImage = new JPanel(new BorderLayout());
				pnImage.add(imageSelection[i], BorderLayout.LINE_START);
				pnImage.add(lbImage, BorderLayout.CENTER);
				pnImages.add(pnImage);
			} else {
				imageSelection[i].setSelected(false);
				pnImages.add(new JLabel("Failed to load image!"));
			}
		}
		if (!btSave.isEnabled()) {
			btSave.setEnabled(true);
			btGenerate.setEnabled(true);
		}
		parent.revalidate();
		parent.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btGenerate) {
			generate();
		} else if (e.getSource() == btSave) {
			JTextField tfWorkName = new JTextField(30);
			tfWorkName.setText(prompt);
			JLabel lbMessageText = new JLabel("Please enter work name:");
			JPanel pnMessage = new JPanel(new BorderLayout());
			pnMessage.add(lbMessageText, BorderLayout.NORTH);
			pnMessage.add(tfWorkName, BorderLayout.CENTER);
			int answer = JOptionPane.showConfirmDialog(parent, pnMessage, "Select work Name",
					JOptionPane.OK_CANCEL_OPTION);
			if (answer == JOptionPane.OK_OPTION) {
				String workName = tfWorkName.getText();
				if (workName.length() == 0) {
					workName = UUID.randomUUID().toString();
				}
				File parentDir = new File(outputDir.get(), workName);
				String fileNameFormat = "%03d.png";
				int num = 1;
				if (!parentDir.exists()) {
					parentDir.mkdirs();
				}
				for (int i = 0; i < images.length; i++) {
					File out;
					while ((out = new File(parentDir, String.format(fileNameFormat, num++))).exists()) {}
					try {
						ImageIO.write(images[i], "png", out);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				hasChanges = false;
			}
		}
	}

//	public final void setOutputDir(File outputDir) {
//		this.outputDir = outputDir;
//	}

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
	}
}
