/*
 *  RemoteAI UI - Lumicore example application
 *  Copyright (C) 2023  epimethix@protonmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.epimethix.lumicore.remoteaiui.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.ioc.annotation.Autowired;
import com.github.epimethix.lumicore.ioc.annotation.PostConstruct;
import com.github.epimethix.lumicore.remoteaiui.RemoteAIUI;
import com.github.epimethix.lumicore.remoteaiui.service.GeneratorService;
import com.github.epimethix.lumicore.remoteaiui.ui.dialog.Setup;
import com.github.epimethix.lumicore.swing.AbstractSwingUI;
import com.github.epimethix.lumicore.swing.remoteai.AIChatPanel;
import com.github.epimethix.lumicore.swing.remoteai.ImageQueryPanel;
import com.github.epimethix.lumicore.swing.remoteai.TextQueryPanel;
import com.github.epimethix.lumicore.swing.util.DialogUtils;

public final class GUIController extends AbstractSwingUI implements SwingUI, LabelsDisplayer{

	private RemoteAIUI application;

	private final JFrame frame;

	private final JMenuItem miReRunSetup;
	private final JMenuItem miExit;

	private final JMenu muFile;

	private ImageQueryPanel imageQueryPanel;

	private TextQueryPanel textQueryPanel;

	private AIChatPanel aiChatPanel;
	
	@Autowired
	private GeneratorService generatorService;


	public GUIController(RemoteAIUI application) {
		super(application);
		this.application = application;
		this.frame = new JFrame();
		miReRunSetup = new JMenuItem();
		miExit = new JMenuItem();
		muFile = new JMenu();
	}

	private void exit() {
		System.exit(0);
	}

	/**
	 * Run / Re-Run Setup
	 * 
	 * @return true if the setup was completed successfully.
	 */
	private boolean runSetup() {
		boolean success = Setup.show(application, this);
		if (success)
			try {
				generatorService.initGenerator(application.getApiKey().get());
			} catch (IllegalAccessException e) {
				success = false;
			}
		return success;
	}

	@PostConstruct
	public final void init() {
		JMenuBar menuBar = new JMenuBar();
		miReRunSetup.addActionListener(e -> runSetup());
		muFile.add(miReRunSetup);
		miExit.addActionListener(e -> exit());
		muFile.add(miExit);
		menuBar.add(muFile);
		menuBar.add(DialogUtils.getLanguageSelectionMenu(application));
		menuBar.add(DialogUtils.getThemeSeletionMenu(this));
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(this);
		imageQueryPanel = new ImageQueryPanel(frame, 2, 2, generatorService.getGenerator(),
				() -> new File(application.getImageOutputDirectory().get()));
//		imageQueryPanel = new Image
		textQueryPanel = new TextQueryPanel(frame, generatorService.getGenerator(),
				() -> new File(application.getTextOutputDirectory().get()));
		aiChatPanel = new AIChatPanel(frame, generatorService.getGenerator(),
				() -> new File(application.getTextOutputDirectory().get()));
		JTabbedPane tpTabbedPane = new JTabbedPane();
		tpTabbedPane.insertTab("Completion", null, textQueryPanel, "", 0);
		tpTabbedPane.insertTab("Image", null, imageQueryPanel, "", 1);
		tpTabbedPane.insertTab("Chat", null, aiChatPanel, "", 2);
		
		frame.setContentPane(tpTabbedPane);
	}

	@Override
	public void showUI() {
		if (!generatorService.isGeneratorInitialized()) {
			if (!runSetup()) {
				System.exit(1);
			}
		}
		frame.pack();
		frame.setMinimumSize(frame.getSize());
		frame.setSize(new Dimension(1200, 800));
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		exit();
	}

	@Override
	public void loadLabels() {
		frame.setTitle(application.getApplicationName());
		muFile.setText(L.getLabel(L.MENU_FILE));
		miExit.setText(L.getLabel(L.MENU_EXIT));
		miReRunSetup.setText(L.getLabel(L.MENU_RE_RUN_SETUP));
	}

	@Override
	public Component getMainFrame() {
		return frame;
	}
}
