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
package com.github.epimethix.lumicore.remoteaiui.ui.dialog;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.Optional;

import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.remoteaiui.RemoteAIUI;
import com.github.epimethix.lumicore.remoteaiui.ui.L;
import com.github.epimethix.lumicore.swing.control.DBPathField;
import com.github.epimethix.lumicore.swing.control.DBPathField.Selector;
import com.github.epimethix.lumicore.swing.dialog.wizard.AbstractWizard;
import com.github.epimethix.lumicore.swing.dialog.wizard.WizardUI;

public final class Setup extends AbstractWizard {

	private static boolean finished;

	private static RemoteAIUI application;

	/**
	 * Show the setup Wizard.
	 * 
	 * @param application the Application
	 * @return true if the wizard was finished, false otherwise.
	 */
	public static boolean show(RemoteAIUI application, SwingUI ui) {
		Setup.application = application;
		Setup s = new Setup(ui);
		finished = false;
		WizardUI.runWizard(s);
		return finished;
	}

	private final JTextField tfApiKey;

	private final DBPathField pfImageOutputDir;

	private final DBPathField pfTextOutputDir;

	private final int indexOfPageApiKey;
	private final int indexOfPageTextOutputDir;
	private final int indexOfPageImageOutputDir;
	private final int indexOfPageFinish;

	private Setup(SwingUI ui) {
		super(() -> L.getLabel(L.DIALOG_SETUP_TITLE));
		/*
		 * Page 1
		 */
		tfApiKey = new JTextField();
		JPanel pnApiKey = new JPanel(new BorderLayout());
//		pnApiKey.setBorder(null);
		pnApiKey.add(tfApiKey, BorderLayout.NORTH);
		indexOfPageApiKey = addPage(pnApiKey, () -> L.getLabel(L.DIALOG_SETUP_API_KEY_TITLE), null, (d) -> {
			String apiKey = tfApiKey.getText().trim();
			if (apiKey.isEmpty()) {
				ui.showErrorMessage(d, L.DIALOG_SETUP_ERROR_MSG_API_KEY_IS_EMPTY);
				return false;
			}
			return true;
		});
		/*
		 * Page 2
		 */
		pfTextOutputDir = new DBPathField(ui, L.DIALOG_SETUP_TEXT_OUTPUT_DIR_TITLE, "", true, Selector.DIRECTORY, null);
		Optional<String> optOutDir = application.getTextOutputDirectory();
		if (optOutDir.isPresent()) {
			pfTextOutputDir.setValue(Path.of(optOutDir.get()));
		}
		JPanel pnTextOutputDir = new JPanel(new BorderLayout());
		pnTextOutputDir.add(pfTextOutputDir.getControl(), BorderLayout.NORTH);
		indexOfPageTextOutputDir = addPage(pnTextOutputDir, () -> L.getLabel(L.DIALOG_SETUP_TEXT_OUTPUT_DIR_TITLE),
				null, (d) -> pfTextOutputDir.isValid());
		/*
		 * Page 3
		 */
		pfImageOutputDir = new DBPathField(ui, L.DIALOG_SETUP_IMAGE_OUTPUT_DIR_TITLE, "", true, Selector.DIRECTORY,
				null);
		optOutDir = application.getImageOutputDirectory();
		if (optOutDir.isPresent()) {
			pfImageOutputDir.setValue(Path.of(optOutDir.get()));
		}
		JPanel pnImageOutputDir = new JPanel(new BorderLayout());
		pnImageOutputDir.add(pfImageOutputDir.getControl(), BorderLayout.NORTH);
		indexOfPageImageOutputDir = addPage(pnImageOutputDir, () -> L.getLabel(L.DIALOG_SETUP_IMAGE_OUTPUT_DIR_TITLE),
				null, (d) -> pfImageOutputDir.isValid());
		/*
		 * Page 4
		 */
		indexOfPageFinish = addPage(new JPanel(), () -> L.getLabel(L.DIALOG_SETUP_FINISHED_TITLE), null, (d) -> true);
	}

	@Override
	public void finish(boolean[] skipped) {
		finished = true;
		if (!skipped[indexOfPageApiKey])
			application.setApiKey(tfApiKey.getText());
		else
			System.err.println("API Key was skipped");
		if (!skipped[indexOfPageImageOutputDir])
			application.setImageOutputDirectory(pfImageOutputDir.getValue().toString());
		else
			System.err.println("Image Output Dir was skipped");
		if (!skipped[indexOfPageTextOutputDir])
			application.setTextOutputDirectory(pfTextOutputDir.getValue().toString());
		else
			System.err.println("Text Output Dir was skipped");
		if (skipped[indexOfPageFinish])
			System.err.println("finish was skipped");
	}

	@Override
	public void cancel() {}

	@Override
	public void clear() {}

}
