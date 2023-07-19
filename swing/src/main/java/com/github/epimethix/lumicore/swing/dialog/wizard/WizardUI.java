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
package com.github.epimethix.lumicore.swing.dialog.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayerPool;
import com.github.epimethix.lumicore.swing.LumicoreSwing;
import com.github.epimethix.lumicore.swing.dialog.AnswerButtonPanel;
import com.github.epimethix.lumicore.swing.dialog.DialogController;
import com.github.epimethix.lumicore.swing.dialog.AbstractAnswerListener;
import com.github.epimethix.lumicore.swing.util.DialogUtils;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

public final class WizardUI extends AbstractAnswerListener implements LabelsDisplayer {

	public static final class Trail {
		private final int split;
		private final int start;
		private final int end;
		private final int join;

		/**
		 * This class defines a trail in the Wizard.
		 * 
		 * @param split the index of the page after which the wizard flow splits into
		 *              different trails.
		 * @param start the index of the first page of the trail.
		 * @param end   the index of the last page of the trail.
		 * @param join  the index of the page that rejoins the different trails. -1 if
		 *              the last page of the trail should also be the finish page. this
		 *              value may not be within the specified start and end.
		 */
		public Trail(int split, int start, int end, int join) {
			this.split = split;
			this.start = start;
			this.end = end;
			if (join >= start && join <= end) {
				throw new IndexOutOfBoundsException("'join >= start && join <=end' may not be true but it is!");
			}
			this.join = join;
		}
	}

	private final static BiConsumer<Wizard, Component> runDisposableWizard = (w, parent) -> {
		WizardUI wizardUI = new WizardUI(w, parent);
		LabelsDisplayerPool.addLabelsDisplayers(wizardUI);
		wizardUI.showUI();
		LabelsDisplayerPool.removeLabelsDisplayers(wizardUI);
		if (wizardUI.finished) {
			w.finish(wizardUI.skipped);
		} else {
			w.cancel();
		}
		wizardUI.getDialog().dispose();
	};

	private final static Consumer<WizardUI> runReusableWizard = (wizardUI) -> {
		wizardUI.showUI();
		if (wizardUI.finished) {
			wizardUI.wizard.finish(wizardUI.skipped);
		} else {
			wizardUI.wizard.cancel();
		}
	};

	public static final void runWizard(Wizard w) {
		runWizard(w, null);
	}

	public static final void runWizard(Wizard w, Component parent) {
		if (SwingUtilities.isEventDispatchThread()) {
			runDisposableWizard.accept(w, parent);
		} else {
			try {
				SwingUtilities.invokeAndWait(() -> {
					runDisposableWizard.accept(w, parent);
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static final void runWizardUI(WizardUI wizardUI) {
		if (SwingUtilities.isEventDispatchThread()) {
			runReusableWizard.accept(wizardUI);
		} else {
			try {
				SwingUtilities.invokeAndWait(() -> runReusableWizard.accept(wizardUI));
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static final WizardUI getWizardUI(Wizard w, Component parent) {
		WizardUI wizardUI = new WizardUI(w, parent);
		LabelsDisplayerPool.addLabelsDisplayers(wizardUI);
		return wizardUI;
	}

//	private final JButton btNext;
//	private final JButton btPrevious;
//	private final JButton btSkip;
//	private final JButton btCancel;

	private final AnswerButtonPanel answerButtonPanel;

	private final Wizard wizard;

	private final JPanel pnCards;
	private final CardLayout cardLayout;

	private final JLabel lbIcon;
	private final JLabel lbTitle;
	private final String[] titles;

//	private final JDialog dialog;
	private final DialogController dialogController;

	private final int pageCount;

	private final Component parent;

	private final Map<Integer, Integer> skipBackMap;

	private final boolean[] skipped;

	private final Trail[] trails;

	private final Set<Integer> splitPages;

	private Trail currentTrail;

	private int currentPage;

	private boolean finished;

	private WizardUI(Wizard w, Component parent) {
		this.wizard = w;
		skipBackMap = new HashMap<>();
		skipped = new boolean[w.getPageCount()];
		trails = w.getTrails();
		splitPages = new HashSet<>();
		if (Objects.nonNull(trails)) {
			for (int i = 0; i < trails.length; i++) {
				splitPages.add(trails[i].split);
			}
		}

		lbIcon = new JLabel();
		lbIcon.setBorder(LayoutUtils.createMediumEmptyBorder());
		lbTitle = LayoutUtils.getTitleLabel();
		lbTitle.setBorder(LayoutUtils.createMediumEmptyBorder());
		titles = new String[w.getPageCount()];

		cardLayout = new CardLayout();
		pnCards = new JPanel(cardLayout);
		pnCards.setBorder(LayoutUtils.createMediumEmptyBorder());
		pageCount = wizard.getPageCount();
		boolean allRequired = true;
		for (int i = 0; i < pageCount; i++) {
			pnCards.add(wizard.getPage(i), String.valueOf(i));
			if (!wizard.isPageRequired(i)) {
				allRequired = false;
			}
		}
		JDialog dlg = null;
		if (Objects.nonNull(parent)) {
			for (int i = 0; i < 2; i++) {
				if (parent instanceof JFrame) {
					dlg = new JDialog((JFrame) parent);
					break;
				} else if (parent instanceof JWindow) {
					dlg = new JDialog((JWindow) parent);
					break;
				} else if (parent instanceof JDialog) {
					dlg = new JDialog((JDialog) parent);
					break;
				} else {
					parent = SwingUtilities.getRoot(parent);
				}
			}
		}
		JDialog dialog;
		if (Objects.nonNull(dlg)) {
			dialog = dlg;
			this.parent = parent;
		} else {
			dialog = new JDialog();
			this.parent = null;
		}
		setDialog(dlg);
		dialogController = new DialogController(dialog);
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(DialogUtils.getLanguageSelectionMenu(LumicoreSwing.getApplication()));
		dialog.setJMenuBar(menuBar);
		if (allRequired) {
			answerButtonPanel = new AnswerButtonPanel(this, Answer.CANCEL, Answer.PREVIOUS, Answer.NEXT, Answer.FINISH);
		} else {
			answerButtonPanel = new AnswerButtonPanel(this, Answer.CANCEL, Answer.PREVIOUS, Answer.SKIP, Answer.NEXT,
					Answer.FINISH);
		}
		JPanel pnContent = new JPanel(new BorderLayout());
		pnContent.add(pnCards, BorderLayout.CENTER);
		pnContent.add(answerButtonPanel, BorderLayout.SOUTH);
		JPanel pnTitle = new JPanel(new BorderLayout());
		JPanel pnSouth = new JPanel(new BorderLayout());
		pnSouth.add(lbTitle, BorderLayout.SOUTH);
		pnTitle.add(lbIcon, BorderLayout.WEST);
		pnTitle.add(pnSouth, BorderLayout.CENTER);
		pnContent.add(pnTitle, BorderLayout.NORTH);
		dialog.setModal(true);
		dialog.setContentPane(pnContent);
		dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
	}

	private void showUI() {
		currentPage = 0;
		finished = false;
		Arrays.fill(skipped, true);
		wizard.clear();
		setStep(0, 0);
		dialogController.setVisible(true);
		System.err.println("setVisible(true) returned");
	}

	private void step(int inc) {
		if (inc == -1) {
			if (skipBackMap.containsKey(currentPage)) {
				inc = skipBackMap.get(currentPage) * -1;
				skipBackMap.remove(currentPage);
			}
		}
		if (splitPages.contains(currentPage)) {
			int trailId = wizard.getTrailChoice(currentPage);
			if (trailId > -1 && trailId < trails.length) {
				currentTrail = trails[trailId];
				if (currentPage < currentTrail.start) {
					inc = currentTrail.start - currentPage;
				}
				currentPage = currentTrail.start;
			} else {
				System.err.printf("%s.getTrailChoice(int): Index (%d) out of Bounds! trails.length=%d%n",
						wizard.getClass().getSimpleName(), trailId, trails.length);
				currentPage += inc;
			}
		} else if (Objects.nonNull(currentTrail) && currentPage == currentTrail.end) {
			if (currentPage < currentTrail.join) {
				inc = currentTrail.join - currentPage;
			}
			currentPage = currentTrail.join;
			currentTrail = null;
		} else {
			currentPage += inc;
		}

		if (inc > 1) {
			skipBackMap.put(currentPage, inc);
		}
		setStep(currentPage, inc);
	}

	private void setStep(int currentPage, int inc) {
		if (this.currentPage != currentPage) {
			this.currentPage = currentPage;
		}
		if (currentPage == 0) {
			answerButtonPanel.setEnabled(Answer.PREVIOUS, false);
//			btPrevious.setEnabled(false);
//			if (inc == 0) {
//				btNext.setText(A.getLabel(A.BUTTON_NEXT));
//			}
//		} else if (currentPage > 0 && inc > 0 && !btPrevious.isEnabled()) {
		} else if (currentPage > 0 && inc > 0 && !answerButtonPanel.isEnabled(Answer.PREVIOUS)) {
//			btPrevious.setEnabled(true);
			answerButtonPanel.setEnabled(Answer.PREVIOUS, true);
		}
//		if (currentPage + 1 == pageCount) {
//			btNext.setText(A.getLabel(A.BUTTON_FINISH));
//		} else if (currentPage + 1 <= pageCount - 1 && inc < 0) {
//			btNext.setText(A.getLabel(A.BUTTON_NEXT));
//		}

		if (wizard.isPageRequired(currentPage) && answerButtonPanel.isEnabled(Answer.SKIP)) {
			answerButtonPanel.setEnabled(Answer.SKIP, false);
//			btSkip.setEnabled(false);
		} else if (!wizard.isPageRequired(currentPage) && !answerButtonPanel.isEnabled(Answer.SKIP)) {
			answerButtonPanel.setEnabled(Answer.SKIP, true);
//			btSkip.setEnabled(true);
		}
		if (Objects.nonNull(wizard.getPageIcon(currentPage))) {
			lbIcon.setIcon(wizard.getPageIcon(currentPage));
		}

		answerButtonPanel.setEnabled(Answer.NEXT, currentPage + 1 < wizard.getPageCount());
		answerButtonPanel.setEnabled(Answer.FINISH, wizard.isFinishEnabled(currentPage));
		lbTitle.setText(titles[currentPage]);
		cardLayout.show(pnCards, String.valueOf(currentPage));
	}

	public JDialog getDialog() {
		return dialogController.getDialog();
	}

	public boolean wasPageSkipped(int pageIndex) {
		return skipped[pageIndex];
	}

	@Override
	public void loadLabels() {
		getDialog().setTitle(wizard.getWizardTitle());
		for (int i = 0; i < pageCount; i++) {
			titles[i] = wizard.getPageTitle(i);
		}
		lbTitle.setText(titles[currentPage]);
	}

	@Override
	public void onAnswer(Answer answer, JDialog dialog) {
		if (answer == Answer.NEXT) {
			if (wizard.validatePage(currentPage, dialog)) {
				skipped[currentPage] = false;
				step(1);
			}
		} else if (answer == Answer.FINISH) {
			skipped[currentPage] = false;
			finished = true;
			System.err.println("setVisible(false) called");
			dialogController.setVisible(false);
//			dialog.setVisible(false);
//			System.err.println("setVisible(false) returned");
		} else if (answer == Answer.PREVIOUS) {
			step(-1);
		} else if (answer == Answer.SKIP) {
			skipped[currentPage] = true;
			step(wizard.getPagesToSkipFrom(currentPage));
		} else if (answer == Answer.CANCEL) {
			dialog.setVisible(false);
			Arrays.fill(skipped, true);
		}
	}
}
