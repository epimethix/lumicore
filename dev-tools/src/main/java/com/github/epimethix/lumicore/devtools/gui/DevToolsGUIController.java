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
package com.github.epimethix.lumicore.devtools.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.common.LumicoreBuildConfig;
import com.github.epimethix.lumicore.common.swing.Document;
import com.github.epimethix.lumicore.common.swing.FileDocument;
import com.github.epimethix.lumicore.common.swing.MutableDocument;
import com.github.epimethix.lumicore.common.swing.MutableFileDocument;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayerPool;
import com.github.epimethix.lumicore.devtools.DevTools;
import com.github.epimethix.lumicore.devtools.DevToolsFiles;
import com.github.epimethix.lumicore.devtools.gui.diagram.DiagramController;
import com.github.epimethix.lumicore.devtools.gui.diagram.DiagramView;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.Diagram;
import com.github.epimethix.lumicore.devtools.gui.translation.TranslationController;
import com.github.epimethix.lumicore.devtools.gui.translation.TranslationModel;
import com.github.epimethix.lumicore.devtools.gui.translation.TranslationView;
import com.github.epimethix.lumicore.ioc.annotation.Autowired;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;
import com.github.epimethix.lumicore.remoteai.Generator;
import com.github.epimethix.lumicore.swing.AbstractSwingUI;
import com.github.epimethix.lumicore.swing.StatusBar;
import com.github.epimethix.lumicore.swing.TabComponent;
import com.github.epimethix.lumicore.swing.control.LTextField;
import com.github.epimethix.lumicore.swing.util.DialogUtils;

public class DevToolsGUIController extends AbstractSwingUI
		implements SwingUI, ActionListener, WindowListener, LabelsDisplayer, ChangeListener {
	private static final Logger LOGGER = Log.getLogger();
	private final JFrame frm;
	private final JMenu muFile;
	private final JMenuItem miNewDiagram;
	private final JMenuItem miOpenDiagram;
	private final JMenuItem miOpenBundle;
	private final JMenuItem miOpenI18N;
	private final JMenuItem miSaveDocument;
	private final JMenuItem miSaveDocumentAs;
	private final JMenuItem miExportAsImage;
	private final JMenuItem miExit;
	private final JTabbedPane tabbedPane;
//	private final LabelsPane labelsPane;
//	private final DevToolsProperties devToolsProperties;

	private final StatusBar statusBar;

	@Autowired
	private DiagramController diagramController;

	@Autowired
	private TranslationController translationController;

//	@Autowired
	private DevTools application;

	private Document currentView;

	private final Map<TabComponent, Document> openDocuments;

	public DevToolsGUIController(DevTools devTools) {
		super(devTools);
		this.application = devTools;
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(this);
		statusBar = new StatusBar();
//		devToolsProperties = DevToolsProperties.getProperties();

		openDocuments = new HashMap<>();

		frm = new JFrame();

		frm.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frm.addWindowListener(this);

		muFile = new JMenu();
//		JMenu muDiagram = new JMenu(D.getLabel(Key.MENU_ERD));
		miNewDiagram = new JMenuItem();
		miNewDiagram.addActionListener(this);
		muFile.add(miNewDiagram);
		miOpenDiagram = new JMenuItem();
		miOpenDiagram.addActionListener(this);
		muFile.add(miOpenDiagram);
		miOpenBundle = new JMenuItem();
		miOpenBundle.addActionListener(this);
		muFile.add(miOpenBundle);
		miOpenI18N = new JMenuItem();
		miOpenI18N.addActionListener(this);
		muFile.add(miOpenI18N);
		miSaveDocument = new JMenuItem();
		miSaveDocument.addActionListener(this);
		muFile.add(miSaveDocument);
		miSaveDocumentAs = new JMenuItem();
		miSaveDocumentAs.addActionListener(this);
		muFile.add(miSaveDocumentAs);
		miExportAsImage = new JMenuItem();
		miExportAsImage.addActionListener(this);
		muFile.add(miExportAsImage);

		miExit = new JMenuItem();
		miExit.addActionListener(this);
		muFile.add(miExit);

//		muFile.add(muDiagram);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(muFile);
		menuBar.add(DialogUtils.getLanguageSelectionMenu(devTools));

		frm.setJMenuBar(menuBar);
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		frm.setContentPane(contentPane);
	}

	/*
	 * Getters
	 */

	public DiagramController getDiagramController() {
		return diagramController;
	}

	public JFrame getFrame() {
		return frm;
	}

//	public DevToolsProperties getDevToolsProperties() {
//		return devToolsProperties;
//	}

	/*
	 * Open / Show
	 */

	private void show() {
//		frm.pack();
		frm.setSize(500, 500);
		frm.setLocationRelativeTo(null);
		frm.setVisible(true);
	}

	public void showStatus(String status) {
		statusBar.showMessage(status);
	}

	public void openCodeTab(File file, String code) {
		openTab(new CodeView(file, code));
	}

	public DiagramView openDiagramTab(Diagram diagram) {
		DiagramView diagramView = new DiagramView(diagramController, diagram);
		openTab(diagramView);
		return diagramView;
	}

	public TranslationView openTranslationTab(TranslationModel tm) {
		TranslationView translationView = new TranslationView(tm, translationController);
		openTab(translationView);
		return translationView;
	}

	private void openTab(FileDocument view) {
		String name = view.getDocumentFile().getPath();
		int x = tabbedPane.indexOfTab(name);
		if (x == -1) {
			LOGGER.trace("%n%s", LabelsDisplayerPool.addLabelsDisplayers(view));
			TabComponent tabComponent = new TabComponent(view.getDocumentName(), this::closeTab);
			tabbedPane.addTab(name, (Component) view);
			x = tabbedPane.indexOfTab(name);
			tabbedPane.setTabComponentAt(x, tabComponent);
			tabbedPane.setSelectedIndex(x);
			currentView = view;
			openDocuments.put(tabComponent, view);
		} else if (view instanceof CodeView) {
			TabComponent tabComponent = (TabComponent) tabbedPane.getTabComponentAt(x);
			CodeView newCodeView = (CodeView) view;
			CodeView currentCodeView = (CodeView) openDocuments.get(tabComponent);
			currentCodeView.refresh(newCodeView);
			tabbedPane.setSelectedIndex(x);
		} else {
			tabbedPane.setSelectedIndex(x);
		}
	}

	/*
	 * Close / Exit
	 */

	public void closeTab() {
		closeTab(tabbedPane.getSelectedIndex());
	}

	public void closeTab(int index) {
		closeTab((TabComponent) tabbedPane.getTabComponentAt(index));
	}

	public void closeTab(TabComponent tabComponent) {
		int x = tabbedPane.indexOfTabComponent(tabComponent);
		if (x > -1) {
			Document document = openDocuments.get(tabComponent);
			if (document instanceof MutableDocument) {
				if (((MutableDocument) document).hasChanges()) {
					if (!showUnsavedChangesDialog(document)) {
						return;
					}
				}
			}
//			Document openDocument = openDocuments.get(tabComponent);
			LOGGER.trace("%n%s", LabelsDisplayerPool.removeLabelsDisplayers(document));
			tabbedPane.remove(x);
			openDocuments.remove(tabComponent);
			x = tabbedPane.getSelectedIndex();
			if (x > -1) {
				tabComponent = (TabComponent) tabbedPane.getTabComponentAt(x);
				currentView = openDocuments.get(tabComponent);
			} else {
				currentView = null;
			}
		}
	}

	private void exit() {
		List<Document> changedDocuments = new ArrayList<>();
		for (TabComponent tc : openDocuments.keySet()) {
			Document d = openDocuments.get(tc);
			if(d instanceof MutableDocument) {
				if (((MutableDocument)d).hasChanges()) {
					changedDocuments.add(d);
				}
			}
		}
		if (changedDocuments.size() == 1) {
			if (!showUnsavedChangesDialog(changedDocuments.get(0))) {
				return;
			}
		} else if (changedDocuments.size() > 1) {
			if (!showUnsavedChangesDialog(changedDocuments)) {
				return;
			}
		}
		System.exit(0);
	}

	/*
	 * Dialogs
	 */

	private boolean showUnsavedChangesDialog(List<Document> documents) {
		List<JCheckBox> checkBoxes = new ArrayList<>();
		JPanel pnDocuments = new JPanel(new GridLayout(documents.size(), 1));
		for (int i = 0; i < documents.size(); i++) {
			JCheckBox ck = new JCheckBox(documents.get(i).getDocumentName());
			ck.setSelected(true);
			pnDocuments.add(ck);
			checkBoxes.add(ck);
		}
		JPanel pnMessage = new JPanel(new BorderLayout());
		pnMessage.add(pnDocuments, BorderLayout.CENTER);
		pnMessage.add(new JLabel(D.getLabel(D.DOCUMENTS_HAVE_UNSAVED_CHANGES, documents.size())), BorderLayout.NORTH);
		int answer = JOptionPane.showConfirmDialog(frm, pnMessage, "Question", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (answer == JOptionPane.CANCEL_OPTION) {
			return false;
		} else if (answer == JOptionPane.YES_OPTION) {
			for (int i = 0; i < documents.size(); i++) {
				if (checkBoxes.get(i).isSelected()) {
					((MutableDocument)documents.get(i)).persist();
				}
			}
		}
		return true;
	}

	private boolean showUnsavedChangesDialog(Document document) {
		Answer answer = showYesNoCancelDialog(frm, D.DOCUMENT_HAS_UNSAVED_CHANGES, document.getDocumentName());
		if (answer == Answer.CANCEL) {
			return false;
		} else if (answer == Answer.YES) {
			((MutableDocument)document).persist();
		}
		return true;
	}

	public Optional<File> showSaveDialog(String extension) {
		return showSaveDialog(frm, extension);
	}

	public Optional<File> showSaveDialog(Component parent, String extension) {
		return DialogUtils.showSaveDialog(parent, new File(DevToolsFiles.RESOURCES_RELATIVE).getAbsoluteFile(),
				extension);
	}

	public Optional<File> showOpenDialog(String extension) {
		return showOpenDialog(frm, extension);
	}

	public Optional<File> showOpenDialog(Component parent, String extension) {
		return DialogUtils.showOpenDialog(parent, new File(DevToolsFiles.RESOURCES_RELATIVE).getAbsoluteFile(),
				extension);

	}

	public Optional<Generator> getGenerator() {
		Generator generator = null;
		if (application.isGeneratorConfigured()) {
			generator = application.getGenerator();
		} else {
			JLabel lbMessage = new JLabel(D.getLabel(D.MESSAGE_ENTER_GENERATOR_API_KEY));
			LTextField tfKey = new LTextField(50);
			JPanel pnEditor = new JPanel(new FlowLayout());
			pnEditor.add(lbMessage);
			pnEditor.add(tfKey);
			boolean proceed = JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(frm, pnEditor,
					D.getLabel(D.MESSAGE_ENTER_GENERATOR_API_KEY_TITLE), JOptionPane.OK_CANCEL_OPTION);
			if (proceed) {
				generator = application.setGeneratorApiKey(tfKey.getText());
			}
		}
		return Optional.ofNullable(generator);
	}

	public Application getApplication() {
		return application;
	}

	/*
	 * User Interface
	 */

	@Override
	public void showUI() {
		show();
	}

	@Override
	protected Component getMainFrame() {
		return frm;
	}

	/*
	 * LabelsDisplayer
	 */

	@Override
	public void loadLabels() {
		frm.setTitle(D.getLabel(D.APP_NAME, LumicoreBuildConfig.VERSION));
		muFile.setText(D.getLabel(D.MENU_FILE));
		miNewDiagram.setText(D.getLabel(D.MENU_DIAGRAM_NEW));
		miOpenDiagram.setText(D.getLabel(D.MENU_DIAGRAM_OPEN));
		miOpenBundle.setText(D.getLabel(D.MENU_BUNDLE_OPEN));
		;
		miSaveDocument.setText(D.getLabel(D.MENU_SAVE));
		miSaveDocumentAs.setText(D.getLabel(D.MENU_SAVE_AS));
		miExportAsImage.setText(D.getLabel(D.MENU_EXPORT_AS));
		miExit.setText(D.getLabel(D.MENU_EXIT));
		miOpenI18N.setText(D.getLabel(D.OPEN_I18N));

	}

	@Override
	public void setOrientation(boolean rtl) {
		frm.applyComponentOrientation(rtl ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
	}

	/*
	 * ChangeListener (JTabbedPane)
	 */

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == tabbedPane) {
			int selectedIndex = tabbedPane.getSelectedIndex();
			if (selectedIndex > -1) {
				TabComponent tabComponent = (TabComponent) tabbedPane.getTabComponentAt(selectedIndex);
				currentView = openDocuments.get(tabComponent);
			} else {
				currentView = null;
			}
		}
	}

	/*
	 * ActionListener (JMenu)
	 */

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == miExit) {
			exit();
		} else if (e.getSource() == miNewDiagram) {
			diagramController.newDiagram();
		} else if (e.getSource() == miOpenDiagram) {
			diagramController.openDiagram();
		} else if (e.getSource() == miOpenBundle) {
			translationController.openBundle();
		} else if (e.getSource() == miOpenI18N) {
			translationController.openI18N();
		} else if (e.getSource() == miSaveDocument) {
			if (Objects.nonNull(currentView)) {
				if(currentView instanceof MutableDocument) {
				((MutableDocument)currentView).persist();
				}
			}
		} else if (e.getSource() == miSaveDocumentAs) {
			if (Objects.nonNull(currentView) && currentView instanceof MutableFileDocument) {
				Optional<File> result = showSaveDialog(".json");
				if (result.isEmpty()) {
					return;
				}
				File selectedFile = result.get();
				((MutableFileDocument)currentView).persist(selectedFile);
//				currentView.persist(selectedFile);
				for (TabComponent tabComponent : openDocuments.keySet()) {
					if (openDocuments.get(tabComponent).equals(currentView)) {
						tabComponent.setLabel(currentView.getDocumentName());
						break;
					}
				}
			}
		} else if (e.getSource() == miExportAsImage) {
			if (Objects.nonNull(currentView) && currentView instanceof DiagramView) {
				DiagramView dv = (DiagramView) currentView;
				try {
					dv.exportAsImage();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	/*
	 * WindowListener (JFrame)
	 */

	@Override
	public void windowClosing(WindowEvent e) {
		exit();
	}

	public File getSourcesDirectory() {
		return application.getSourcesDirectory();
	}
}
