/*
 *  Accounting - Lumicore example application
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
package com.github.epimethix.accounting.gui;

import java.awt.Component;
import java.awt.ComponentOrientation;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.github.epimethix.accounting.Accounting;
import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.ioc.annotation.Autowired;
import com.github.epimethix.lumicore.ioc.annotation.PostConstruct;
import com.github.epimethix.lumicore.swing.AbstractSwingUI;
import com.github.epimethix.lumicore.swing.util.DialogUtils;

public class ExampleMainController extends AbstractSwingUI implements SwingUI, LabelsDisplayer {
//	private final static Logger LOGGER = Log.getLogger();

	private final JFrame frm;

	@Autowired
	private ContentPane contentPane;

	private final Application application;

	@Autowired
	private ExamleEntityAccessBrowser examleEntityAccessBrowser;

	public ExampleMainController(Accounting application) {
		this.application = application;
		this.frm = new JFrame(application.getApplicationName());
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@PostConstruct
	public void init() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(DialogUtils.getLanguageSelectionMenu(application));
		frm.setJMenuBar(menuBar);
		frm.setContentPane(examleEntityAccessBrowser);
	}

	@Override
	public final void showUI() {
		frm.pack();
		frm.setLocationRelativeTo(null);
		this.frm.setVisible(true);
	}

	@Override
	protected Component getMainFrame() {
		return frm;
	}

	@Override
	public void loadLabels() {}

	@Override
	public void setOrientation(boolean rtl) {
		frm.applyComponentOrientation(rtl ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
	}
}
