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
package com.github.epimethix.lumicore.swing;

import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;
import com.github.epimethix.lumicore.properties.Theme;
import com.github.epimethix.lumicore.swing.util.DialogUtils;

public abstract class AbstractSwingUI implements SwingUI, WindowListener {
	private final Application application;

	public AbstractSwingUI(Application application) {
		this.application = application;
	}

	public void showErrorMessage(String key, Object... args) {
		showErrorMessage(null, key, args);
	}

	@Override
	public void showErrorMessage(Component c, String key, Object... args) {
		DialogUtils.showErrorMessage(Objects.nonNull(c) ? SwingUtilities.getRoot(c) : getMainFrame(),
				LabelsManagerPool.getLabel(key, args));
	}

	public void showPlainMessage(String key, Object... args) {
		showPlainMessage(null, key, args);
	}

	@Override
	public void showPlainMessage(Component c, String key, Object... args) {
		DialogUtils.showPlainMessage(Objects.nonNull(c) ? SwingUtilities.getRoot(c) : getMainFrame(),
				LabelsManagerPool.getLabel(key, args));
	}

	public void showInfoMessage(String key, Object... args) {
		showInfoMessage(null, key, args);
	}

	@Override
	public void showInfoMessage(Component c, String key, Object... args) {
		DialogUtils.showInfoMessage(Objects.nonNull(c) ? SwingUtilities.getRoot(c) : getMainFrame(),
				LabelsManagerPool.getLabel(key, args));
	}

	public void showWarningMessage(String key, Object... args) {
		showWarningMessage(null, key, args);
	}

	@Override
	public void showWarningMessage(Component c, String key, Object... args) {
		DialogUtils.showWarningMessage(Objects.nonNull(c) ? SwingUtilities.getRoot(c) : getMainFrame(),
				LabelsManagerPool.getLabel(key, args));
	}

	public Answer showOkCancelDialog(String key, Object... args) {
		return showOkCancelDialog(null, key, args);
	}

	@Override
	public Answer showOkCancelDialog(Component c, String key, Object... args) {
		return DialogUtils.showConfirmDialog(Objects.nonNull(c) ? SwingUtilities.getRoot(c) : getMainFrame(),
				JOptionPane.OK_CANCEL_OPTION, LabelsManagerPool.getLabel(key, args)) == JOptionPane.OK_OPTION
						? Answer.OK
						: Answer.CANCEL;
	}

	public Answer showYesNoDialog(String key, Object... args) {
		return showYesNoDialog(null, key, args);
	}

	@Override
	public Answer showYesNoDialog(Component c, String key, Object... args) {
		int answer = DialogUtils.showConfirmDialog(Objects.nonNull(c) ? SwingUtilities.getRoot(c) : getMainFrame(),
				JOptionPane.YES_NO_OPTION, LabelsManagerPool.getLabel(key, args));
		if (answer == JOptionPane.YES_OPTION) {
			return Answer.YES;
		}
		return Answer.NO;
	}

	public Answer showYesNoCancelDialog(String key, Object... args) {
		return showYesNoCancelDialog(null, key, args);
	}

	@Override
	public Answer showYesNoCancelDialog(Component c, String key, Object... args) {
		int answer = DialogUtils.showConfirmDialog(Objects.nonNull(c) ? SwingUtilities.getRoot(c) : getMainFrame(),
				JOptionPane.YES_NO_CANCEL_OPTION, LabelsManagerPool.getLabel(key, args));
		if (answer == JOptionPane.YES_OPTION) {
			return Answer.YES;
		}
		if (answer == JOptionPane.NO_OPTION) {
			return Answer.NO;
		}
		return Answer.CANCEL;
	}

	@Override
	public Theme getTheme() {
		return application.getTheme();
	}

	@Override
	public void setTheme(Theme t) {
		application.setTheme(t);
		setupTheme();
	}

	@Override
	public void setupTheme() {
		Theme t = getTheme();
		if (t == Theme.DARK || t == Theme.DEFAULT) {
			FlatDarkLaf.setup();
		} else {
			FlatLightLaf.setup();
		}
		SwingUtilities.updateComponentTreeUI(getMainFrame());
	}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public abstract void showUI();

	protected abstract Component getMainFrame();

}
