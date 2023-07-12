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
package com.github.epimethix.lumicore.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.swing.LumicoreSwingImpl;
import com.github.epimethix.lumicore.swing.util.DialogUtils;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

public class DialogUI implements LabelsDisplayer {

	public static final void showDialogUI(DialogUI dui) {
		if (SwingUtilities.isEventDispatchThread()) {
			dui.showDialog();
		} else {
			try {
				SwingUtilities.invokeAndWait(() -> dui.showDialog());
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static final DialogUI getDialogUI(Dialog d, Application application) {
		return new DialogUI(d, application);
	}
	
	public static final DialogUI getDialogUI(Dialog d) {
		return new DialogUI(d, LumicoreSwingImpl.getApplication());
	}

	private final Dialog dialog;
	private final JDialog jDialog;
	private final JLabel lbTitle;
	private final JLabel lbIcon;

	private final Answer defaultAnswer;

	private final AnswerButtonPanel buttonPanel;

	private DialogUI(Dialog d, Application application) {
		this.dialog = d;
		this.defaultAnswer = d.getDefaultAnswer();
		this.jDialog = DialogUtils.initializeJDialog(d.getParent(), d.getTitle(), null, true);
		buttonPanel = new AnswerButtonPanel(new AbstractAnswerListener(jDialog) {
			@Override
			public void onAnswer(Answer answer, JDialog d) {
				DialogUI.this.dialog.onAnswer(answer, d);
			}
		}, dialog.getAnswerOptions());
		lbTitle = LayoutUtils.getTitleLabel();
		lbIcon = new JLabel();
		JPanel pnTitle = new JPanel(new BorderLayout());
		pnTitle.add(lbTitle, BorderLayout.CENTER);
		if (Objects.nonNull(dialog.getIcon())) {
			lbIcon.setIcon(dialog.getIcon());
			pnTitle.add(lbIcon, BorderLayout.LINE_START);
		}
		JPanel view = new JPanel(new BorderLayout());
		view.setBorder(LayoutUtils.createMediumEmptyBorder());
		view.add(pnTitle, BorderLayout.NORTH);
		view.add(dialog.getUI(), BorderLayout.CENTER);
		view.add(buttonPanel, BorderLayout.SOUTH);
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(DialogUtils.getLanguageSelectionMenu(application));
		jDialog.setJMenuBar(menuBar);
		jDialog.setContentPane(view);
		jDialog.pack();
		jDialog.setMinimumSize(jDialog.getSize());
		jDialog.setLocationRelativeTo(jDialog.getParent());
		jDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		jDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				DialogUI.this.dialog.onAnswer(defaultAnswer, jDialog);
			}
		});
		DialogUtils.registerEscapeAction(jDialog, e -> DialogUI.this.dialog.onAnswer(defaultAnswer, jDialog));
	}

	private final void showDialog() {
		dialog.clear();
		jDialog.setMinimumSize(new Dimension(0, 0));
		jDialog.pack();
		jDialog.setMinimumSize(jDialog.getSize());
		jDialog.setVisible(true);
	}

	public JDialog getDialog() {
		return jDialog;
	}

	@Override
	public void loadLabels() {
		jDialog.setTitle(dialog.getTitle());
		lbTitle.setText(dialog.getTitle());
		Icon i = dialog.getIcon();
		if (Objects.nonNull(i)) {
			lbIcon.setIcon(i);
		}
	}
}
