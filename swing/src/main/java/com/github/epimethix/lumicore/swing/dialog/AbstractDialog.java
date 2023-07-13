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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.AnswerOption;

public abstract class AbstractDialog implements Dialog, ActionListener {

	public final static Icon ICON_INFO = new ImageIcon(AbstractDialog.class.getResource("/img/info.png"));
	public final static Icon ICON_EDIT = new ImageIcon(AbstractDialog.class.getResource("/img/edit.png"));
	public final static Icon ICON_WARNING = new ImageIcon(AbstractDialog.class.getResource("/img/warn.png"));
	public final static Icon ICON_QUESTION = new ImageIcon(AbstractDialog.class.getResource("/img/question.png"));
	public final static Icon ICON_SECRET = new ImageIcon(AbstractDialog.class.getResource("/img/secret.png"));
	public final static Icon ICON_ERROR = new ImageIcon(AbstractDialog.class.getResource("/img/error.png"));

	private final Component parent;
	private final Supplier<String> title;
	private final Icon icon;
	private final Component messageUI;
	private final Answer[] answerOptions;

	public AbstractDialog(Component parent, Supplier<String> title, Icon icon, Object message,
			AnswerOption answerOptions) {
		this(parent, title, icon, message, answerOptions.getAnswers());
	}

	public AbstractDialog(Component parent, Supplier<String> title, Icon icon, Object message, Answer... answers) {
		this.parent = parent;
		this.title = title;
		this.icon = icon;
		if (answers.length > 0) {
			this.answerOptions = answers;
		} else {
			this.answerOptions = new Answer[] { Answer.OK };
		}
		if (message instanceof Component) {
			messageUI = (Component) message;
		} else if (message instanceof JComponent) {
			messageUI = (JComponent) message;
		} else if (message instanceof String) {
			messageUI = new JLabel((String) message);
		} else {
			throw new IllegalArgumentException("message must be String or Component!");
		}
	}

	@Override
	public Component getParent() {
		return parent;
	}

	@Override
	public Component getUI() {
		return messageUI;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getTitle() {
		return title.get();
	}

	@Override
	public Answer[] getAnswerOptions() {
		return Arrays.copyOf(answerOptions, answerOptions.length);
	}

	@Override
	public void actionPerformed(ActionEvent e) {};
	
	@Override
	public abstract void onAnswer(Answer answer, JDialog parent);
}
