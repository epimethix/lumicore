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

import static com.github.epimethix.lumicore.swing.util.GridBagUtils.addGridBagLine;
import static com.github.epimethix.lumicore.swing.util.GridBagUtils.finishGridBagForm;
import static com.github.epimethix.lumicore.swing.util.GridBagUtils.initGridBagConstraints;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.AnswerOption;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.CryptoUI.Credentials;
import com.github.epimethix.lumicore.common.ui.DefaultCredentials;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayerPool;

public class CryptoDialog {
	public enum Mode {
		KEY, NAME_AND_KEY;
	}

	/*
	 * Set up Secret
	 */
	@SuppressWarnings("serial")
	private final static class SetupSecretDialogUI extends JPanel implements LabelsDisplayer {
		private final JLabel lbUser;
		private final JTextField tfUser;
		private final JLabel lbEnterNewSecret1;
		private final JLabel lbEnterNewSecret2;
		private final JPasswordField pwSetupSecret1;
		private final JPasswordField pwSetupSecret2;

		public SetupSecretDialogUI(Mode m) {
			super(new GridBagLayout());
			lbUser = new JLabel();
			tfUser = new JTextField();
			lbEnterNewSecret1 = new JLabel();
			lbEnterNewSecret2 = new JLabel();
			pwSetupSecret1 = new JPasswordField(30);
			pwSetupSecret2 = new JPasswordField(30);
			GridBagConstraints c = initGridBagConstraints();
			if (m == Mode.NAME_AND_KEY) {
				addGridBagLine(this, c, lbUser, tfUser);
			}
			addGridBagLine(this, c, lbEnterNewSecret1, pwSetupSecret1);
			addGridBagLine(this, c, lbEnterNewSecret2, pwSetupSecret2);
			finishGridBagForm(this, c, 2);
		}

		@Override
		public void loadLabels() {
			lbEnterNewSecret1.setText(C.getLabel(C.CRYPTO_ENTER_NEW_SECRET));
			lbEnterNewSecret2.setText(C.getLabel(C.CRYPTO_ENTER_NEW_SECRET_AGAIN));
			lbUser.setText(C.getLabel(C.CRYPTO_USER_NAME));
		}
	}

	@SuppressWarnings("serial")
	private final static class ResetSecretDialogUI extends JPanel implements LabelsDisplayer {
		private final JLabel lbUser;
		private final JTextField tfUser;
		private final JLabel lbEnterOldSecret;
		private final JLabel lbEnterNewSecret1;
		private final JLabel lbEnterNewSecret2;
		private final JPasswordField pwOldSecret;
		private final JPasswordField pwNewSecret1;
		private final JPasswordField pwNewSecret2;

		public ResetSecretDialogUI(Mode m) {
			super(new GridBagLayout());
			lbUser = new JLabel();
			tfUser = new JTextField();
			lbEnterOldSecret = new JLabel();
			lbEnterNewSecret1 = new JLabel();
			lbEnterNewSecret2 = new JLabel();
			pwOldSecret = new JPasswordField(30);
			pwNewSecret1 = new JPasswordField(30);
			pwNewSecret2 = new JPasswordField(30);
			GridBagConstraints c = initGridBagConstraints();
			if (m == Mode.NAME_AND_KEY) {
				addGridBagLine(this, c, lbUser, tfUser);
			}
			addGridBagLine(this, c, lbEnterOldSecret, pwOldSecret);
			addGridBagLine(this, c, lbEnterNewSecret1, pwNewSecret1);
			addGridBagLine(this, c, lbEnterNewSecret2, pwNewSecret2);
			finishGridBagForm(this, c, 2);
		}

		@Override
		public void loadLabels() {
			lbEnterOldSecret.setText(C.getLabel(C.CRYPTO_ENTER_OLD_SECRET));
			lbEnterNewSecret1.setText(C.getLabel(C.CRYPTO_ENTER_NEW_SECRET));
			lbEnterNewSecret2.setText(C.getLabel(C.CRYPTO_ENTER_NEW_SECRET_AGAIN));
			lbUser.setText(C.getLabel(C.CRYPTO_USER_NAME));
		}
	}

	@SuppressWarnings("serial")
	private final class GetSecretDialogUI extends JPanel implements LabelsDisplayer {
		private final JLabel lbUser;
		private final JTextField tfUser;
		private final JLabel lbEnterSecret;
		private final JPasswordField pwSecret;

		public GetSecretDialogUI(Mode m) {
			super(new GridBagLayout());
			lbUser = new JLabel();
			tfUser = new JTextField();
			lbEnterSecret = new JLabel();
			pwSecret = new JPasswordField(30);

			GridBagConstraints c = initGridBagConstraints();
			if (m == Mode.NAME_AND_KEY) {
				addGridBagLine(this, c, lbUser, tfUser);
			}
			addGridBagLine(this, c, lbEnterSecret, pwSecret);
			finishGridBagForm(this, c, 2);
		}

		@Override
		public void loadLabels() {
			lbEnterSecret.setText(C.getLabel(C.CRYPTO_ENTER_SECRET));
			lbUser.setText(C.getLabel(C.CRYPTO_USER_NAME));
		}
	}

	private char[] oldSecret;
	private Credentials result;

	/*
	 * Setup Secret
	 */
	private final DialogUI setupSecretUI;
	private final SetupSecretDialogUI setupSecretDialogUI;

	/*
	 * Reset Secret
	 */
	private final DialogUI resetSecretUI;
	private final ResetSecretDialogUI resetSecretDialogUI;

	/*
	 * Get Secret
	 */
	private final DialogUI getSecretUI;
	private final GetSecretDialogUI getSecretDialogUI;

	public CryptoDialog(Application application, Mode mode) {
		this(application, null, mode);
	}

	public CryptoDialog(Application application) {
		this(application, null, null);
	}

	public CryptoDialog(Application application, Component parent) {
		this(application, parent, null);
	}

	public CryptoDialog(Application application, Component parent, Mode mode) {
//	}
//	public CryptoDialog() {
		this.setupSecretDialogUI = new SetupSecretDialogUI(mode);
		this.setupSecretUI = DialogUI
				.getDialogUI(new AbstractDialog(parent, () -> C.getLabel(C.CRYPTO_TITLE_SET_UP_SECRET),
						AbstractDialog.ICON_SECRET, setupSecretDialogUI, AnswerOption.OK_CANCEL) {
					{
						setupSecretDialogUI.pwSetupSecret1.addActionListener(this);
						setupSecretDialogUI.pwSetupSecret2.addActionListener(this);
					}

					@Override
					public void actionPerformed(ActionEvent e) {
						onAnswer(Answer.OK, setupSecretUI.getDialog());
					}

					@Override
					public void onAnswer(Answer answer, JDialog parent) {
						if (answer == Answer.OK) {
//							char[] csec1 = setupSecretDialogUI.pwSetupSecret1.getPassword();
//							csec2 = setupSecretDialogUI.pwSetupSecret2.getPassword();
							char[] sec1 = setupSecretDialogUI.pwSetupSecret1.getPassword();
							char[] sec2 = setupSecretDialogUI.pwSetupSecret2.getPassword();
							try {
								if (Arrays.equals(sec1, sec2)) {
									if (mode == Mode.NAME_AND_KEY) {
										result = new DefaultCredentials(setupSecretDialogUI.tfUser.getText(),
												Arrays.copyOf(sec1, sec1.length));
									} else {
										result = new DefaultCredentials(null, Arrays.copyOf(sec1, sec1.length));
									}
								} else {
									JOptionPane.showMessageDialog(parent,
											C.getLabel(C.ERROR_MESSAGE_SECRETS_DONT_MATCH),
											C.getLabel(C.ERROR_MESSAGE_SECRETS_DONT_MATCH_TITLE),
											JOptionPane.ERROR_MESSAGE);
									return;
								}
							} finally {
								Arrays.fill(sec1, (char) 0);
								Arrays.fill(sec2, (char) 0);
								setupSecretDialogUI.pwSetupSecret1.setText("");
								setupSecretDialogUI.pwSetupSecret2.setText("");
							}
						}
						parent.setVisible(false);
					}
				}, application);
		this.resetSecretDialogUI = new ResetSecretDialogUI(mode);
		this.resetSecretUI = DialogUI
				.getDialogUI(new AbstractDialog(parent, () -> C.getLabel(C.CRYPTO_TITLE_RESET_SECRET),
						AbstractDialog.ICON_SECRET, resetSecretDialogUI, AnswerOption.OK_CANCEL) {
					{
						resetSecretDialogUI.pwNewSecret1.addActionListener(this);
						resetSecretDialogUI.pwNewSecret2.addActionListener(this);
						resetSecretDialogUI.pwOldSecret.addActionListener(this);
					}

					@Override
					public void actionPerformed(ActionEvent e) {
						onAnswer(Answer.OK, resetSecretUI.getDialog());
					}

					@Override
					public void onAnswer(Answer answer, JDialog parent) {
						if (answer == Answer.OK) {
							char[] sec = resetSecretDialogUI.pwOldSecret.getPassword();
							char[] sec1 = resetSecretDialogUI.pwNewSecret1.getPassword();
							char[] sec2 = resetSecretDialogUI.pwNewSecret2.getPassword();
							try {
								if (!Arrays.equals(sec, oldSecret)) {
									JOptionPane.showMessageDialog(parent, C.getLabel(C.ERROR_MESSAGE_SECRET_INVALID),
											C.getLabel(C.ERROR_MESSAGE_SECRET_INVALID_TITLE),
											JOptionPane.ERROR_MESSAGE);
									return;
								}
								if (Arrays.equals(sec1, sec2)) {

									if (mode == Mode.NAME_AND_KEY) {
										result = new DefaultCredentials(resetSecretDialogUI.tfUser.getText(),
												Arrays.copyOf(sec1, sec1.length));
									} else {
										result = new DefaultCredentials(null, Arrays.copyOf(sec1, sec1.length));
									}
//									result = Arrays.copyOf(sec1, sec1.length);
								} else {
									JOptionPane.showMessageDialog(parent,
											C.getLabel(C.ERROR_MESSAGE_SECRETS_DONT_MATCH),
											C.getLabel(C.ERROR_MESSAGE_SECRETS_DONT_MATCH_TITLE),
											JOptionPane.ERROR_MESSAGE);
									return;
								}
							} finally {
								Arrays.fill(sec, (char) 0);
								Arrays.fill(sec1, (char) 0);
								Arrays.fill(sec2, (char) 0);
								resetSecretDialogUI.pwOldSecret.setText("");
								resetSecretDialogUI.pwNewSecret1.setText("");
								resetSecretDialogUI.pwNewSecret2.setText("");
							}
						}
						parent.setVisible(false);
					}
				}, application);
		this.getSecretDialogUI = new GetSecretDialogUI(mode);
		this.getSecretUI = DialogUI.getDialogUI(new AbstractDialog(parent, () -> C.getLabel(C.CRYPTO_TITLE_GET_SECRET),
				AbstractDialog.ICON_SECRET, getSecretDialogUI, AnswerOption.OK_CANCEL) {
			{
				getSecretDialogUI.pwSecret.addActionListener(this);
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				onAnswer(Answer.OK, getSecretUI.getDialog());
			}

			@Override
			public void onAnswer(Answer answer, JDialog parent) {
				if (answer == Answer.OK) {
					char[] sec = getSecretDialogUI.pwSecret.getPassword();
					try {
						if (mode == Mode.NAME_AND_KEY) {
							result = new DefaultCredentials(getSecretDialogUI.tfUser.getText(),
									Arrays.copyOf(sec, sec.length));
						} else {
							result = new DefaultCredentials(null, Arrays.copyOf(sec, sec.length));
						}
//						result = Arrays.copyOf(sec, sec.length);
					} finally {
						Arrays.fill(sec, (char) 0);
						getSecretDialogUI.pwSecret.setText("");
					}
				}
				parent.setVisible(false);
			}
		}, application);
		LabelsDisplayerPool.addLabelsDisplayers(this);
	}

	public void setParent(Component c) {
//		setupSecretUI.getDialog().getPar
	}

	public final Optional<Credentials> setupSecret() {
		this.result = null;
		DialogUI.showDialogUI(setupSecretUI);
		Credentials result = this.result;
		this.result = null;
		return Optional.ofNullable(result);
	}

	public Optional<Credentials> resetSecret(char[] oldSecret) {
		try {
			this.oldSecret = oldSecret;
			this.result = null;
			DialogUI.showDialogUI(resetSecretUI);
			Credentials result = this.result;
			this.result = null;
			return Optional.ofNullable(result);
		} finally {
			Arrays.fill(oldSecret, (char) 0);
		}
	}

	public Optional<Credentials> getSecret() {
		this.result = null;
		DialogUI.showDialogUI(getSecretUI);
		Credentials result = this.result;
		this.result = null;
		return Optional.ofNullable(result);
	}
}
