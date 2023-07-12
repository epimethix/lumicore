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
package com.github.epimethix.lumicore.swing.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayerPool;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;
import com.github.epimethix.lumicore.swing.dialog.AnswerButtonPanel;
import com.github.epimethix.lumicore.swing.dialog.AbstractAnswerListener;

public final class DialogUtils {

	public static void showErrorMessage(Component parent, String msg) {
		JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void showPlainMessage(Component parent, String msg) {
		JOptionPane.showMessageDialog(parent, msg, "Message", JOptionPane.PLAIN_MESSAGE);
	}

	public static void showInfoMessage(Component parent, String msg) {
		JOptionPane.showMessageDialog(parent, msg, "Message", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void showWarningMessage(Component parent, String msg) {
		JOptionPane.showMessageDialog(parent, msg, "Warning", JOptionPane.WARNING_MESSAGE);
	}

	public static int showConfirmDialog(Component parent, int choices, String msg) {
		int answer;
		answer = JOptionPane.showConfirmDialog(parent, msg, "Question", choices);
		return answer;
	}

	public static Optional<File> showOpenDialog(Component parent, File currentDirectory, String extension) {
		JFileChooser fc = new JFileChooser();
		if (Objects.nonNull(extension)) {
			extension = extension.startsWith(".") ? extension : "." + extension;
			final String ext = extension;
			fc.setFileFilter(new FileFilter() {

				@Override
				public String getDescription() {
					return ext.substring(1);
				}

				@Override
				public boolean accept(File f) {
					return f.getName().endsWith(ext) || f.isDirectory();
				}
			});
		}
		if (Objects.isNull(currentDirectory) || !currentDirectory.exists()) {
			fc.setCurrentDirectory(new File(System.getProperty("user.home")));
		} else {
			fc.setCurrentDirectory(currentDirectory);
		}
		boolean open = JFileChooser.APPROVE_OPTION == fc.showOpenDialog(parent);
		if (open) {
			return Optional.of(fc.getSelectedFile());
		} else {
			return Optional.empty();
		}
	}

	public static final Answer showDialog(Component c, String titleKey, JComponent message,
			BiConsumer<Answer, JDialog> onAnswer, Answer defaultAnswer, Answer... options) {
//		JPanel pnButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		final Answer[] answer = new Answer[] { defaultAnswer };
		JDialog dialog = initializeJDialog(c, titleKey, null, true);
		AnswerButtonPanel pnButtons = new AnswerButtonPanel(new AbstractAnswerListener(dialog) {
			@Override
			public void onAnswer(Answer a, JDialog dialog) {
				answer[0] = a;
				if (Objects.nonNull(onAnswer)) {
					onAnswer.accept(a, dialog);
				} else {
					dialog.setVisible(false);
				}
			}
		}, options);
//		AnswerButtonPanel pnButtons = new AnswerButtonPanel((a, d) -> {
//			answer[0] = a;
//		}, options);
		JPanel pnView = new JPanel(new BorderLayout());
		pnView.add(message, BorderLayout.CENTER);
		pnView.add(pnButtons, BorderLayout.SOUTH);
		pnView.setBorder(LayoutUtils.createDefaultEmptyBorder());
		dialog.setContentPane(pnView);
		dialog.pack();
		dialog.setMinimumSize(dialog.getSize());
		dialog.setLocationRelativeTo(dialog.getParent());
		dialog.setVisible(true);
		return answer[0];
	}

	public static final void registerHideOnEscape(final JDialog d) {
		registerEscapeAction(d, (e) -> d.setVisible(false));
	}

	public static final void registerHideOnEscape(final JDialog d, Predicate<ActionEvent> condition) {
		registerEscapeAction(d, (e) -> {
			if (condition.test(e)) {
				d.setVisible(false);
			}
		});
	}

	public static final void registerEscapeAction(final JDialog d, ActionListener al) {
		d.getRootPane().registerKeyboardAction(al, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	public static final JDialog initializeJDialog(Component childComponent, String title, JComponent view,
			boolean modal) {
		return initializeJDialog(childComponent, title, view, modal, null);
	}

	public static final JDialog initializeJDialog(Component c, String title, JComponent view,
			boolean modal, Predicate<ActionEvent> escapeCondition) {
		JDialog dialog;
		Component parent = c;
		if (Objects.nonNull(c)) {
			if (parent instanceof Window) {
				dialog = new JDialog((Window) parent);
			} else if (parent instanceof Frame) {
				dialog = new JDialog((Frame) parent);
			} else if (parent instanceof Dialog) {
				dialog = new JDialog((Dialog) parent);
			} else {
				parent = SwingUtilities.getRoot(c);
				if (parent instanceof Window) {
					dialog = new JDialog((Window) parent);
				} else if (parent instanceof Frame) {
					dialog = new JDialog((Frame) parent);
				} else if (parent instanceof Dialog) {
					dialog = new JDialog((Dialog) parent);
				} else {
					dialog = new JDialog();
					System.err.println("Could not identify parent window/frame/dialog");
				}
			}
		} else {
			dialog = new JDialog();
		}
		dialog.setTitle(title);
		dialog.setModal(modal);
		if (Objects.nonNull(view)) {
			view.setBorder(LayoutUtils.createDefaultEmptyBorder());
			dialog.setContentPane(view);
			dialog.pack();
			dialog.setMinimumSize(dialog.getSize());
			dialog.setLocationRelativeTo(parent);
		}
		if (Objects.nonNull(escapeCondition)) {
			registerHideOnEscape(dialog, escapeCondition);
		} else {
			registerHideOnEscape(dialog);
		}
		return dialog;
	}

	public static Optional<File> showSaveDialog(Component parent, File currentDirectory, String extension) {
//		JFileChooser fc = new JFileChooser();
//		fc.setCurrentDirectory(currentDirectory);
//		File selectedFile = null;
//		while (selectedFile.exists()) {
//			int answer = fc.showSaveDialog(parent);
//			if (answer == JFileChooser.APPROVE_OPTION) {
//				answer = JOptionPane.NO_OPTION;
//				selectedFile = fc.getSelectedFile();
//				if (selectedFile.exists()) {
//					answer = JOptionPane.showConfirmDialog(parent, "Overwrite?", "Question",
//							JOptionPane.YES_NO_CANCEL_OPTION);
//					if (answer == JOptionPane.CANCEL_OPTION) {
//						return Optional.empty();
//					} else if (answer == JOptionPane.YES_OPTION) {
//						break;
//					}
//				}
//			} else {
//				return Optional.empty();
//			}
//		}
//		return Optional.ofNullable(selectedFile);
		File selectedFile = null;
		boolean overwrite = false;
		FileFilter ff = null;
		if (Objects.nonNull(extension)) {
			extension = extension.startsWith(".") ? extension : "." + extension;
			final String ext = extension;
			ff = new FileFilter() {

				@Override
				public String getDescription() {
					return ext.substring(1);
				}

				@Override
				public boolean accept(File f) {
					return f.getName().endsWith(ext);
				}
			};
		}
		do {
			JFileChooser fc = new JFileChooser();
			if (Objects.nonNull(extension)) {
				fc.setFileFilter(ff);
			}
			fc.setCurrentDirectory(currentDirectory);
			int answer = fc.showSaveDialog(parent);
			if (answer == JFileChooser.CANCEL_OPTION) {
				return Optional.empty();
			} else if (answer == JFileChooser.APPROVE_OPTION) {
				selectedFile = fc.getSelectedFile();
				if (Objects.nonNull(extension)) {
					if (Objects.nonNull(selectedFile) && !selectedFile.getName().endsWith(extension)) {
						selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + extension);
					}
				}
				if (Objects.nonNull(selectedFile) && selectedFile.exists()) {
					String title = C.getLabel(C.DIALOG_TITLE_FILE_EXISTS);
					String message = C.getLabel(C.DIALOG_MESSAGE_FILE_EXISTS);
					answer = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
					if (answer == JOptionPane.YES_OPTION) {
						overwrite = true;
					}
				}
			} else {
				return Optional.empty();
			}
		} while (!overwrite && (Objects.isNull(selectedFile) || selectedFile.exists()));
		return Optional.ofNullable(selectedFile);
	}

	public static JMenu getLanguageSelectionMenu(final Application application) {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new RuntimeException(
					"Labels.getLanguageSelectionMenu() must be called from the Event Dispatch Thread");
		}
		JMenu menu = new JMenu(C.getLabel(C.MENU_LANGUAGES));
		List<Locale> locales = LabelsManagerPool.getAvailableLocales();
		Collections.sort(locales, (a, b) -> a.getDisplayLanguage().compareTo(b.getDisplayLanguage()));
//		System.err.println(locales.toString());
		for (Locale locale : locales) {
			JMenuItem mi = new JMenuItem(locale.getDisplayLanguage(locale));
			mi.addActionListener(event -> LabelsManagerPool.setLocale(application, locale));
			menu.add(mi);
		}
		LabelsDisplayer ld = () -> menu.setText(C.getLabel(C.MENU_LANGUAGES));
		LabelsDisplayerPool.addLabelsDisplayer(ld);
		return menu;
	}

	private DialogUtils() {}
}
