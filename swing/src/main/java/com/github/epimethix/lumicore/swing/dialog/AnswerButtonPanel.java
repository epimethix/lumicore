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

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.AnswerOption;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;

@SuppressWarnings("serial")
public final class AnswerButtonPanel extends JPanel implements LabelsDisplayer {

	private final Map<String, JButton> buttonMap = new HashMap<>();

	public AnswerButtonPanel(AbstractAnswerListener answerListener, AnswerOption answerOption) {
		this(answerListener, answerOption.getAnswers());
	}

	public AnswerButtonPanel(AbstractAnswerListener answerListener, Answer... options) {
		super(new FlowLayout(FlowLayout.TRAILING));
		if (options.length > 0) {
			for (Answer a : options) {
				addButton(a, answerListener);
			}
		} else {
			addButton(Answer.OK, answerListener);
		}
	}

	private final void addButton(Answer answer, ActionListener actionListener) {
		JButton bt = new JButton();
		bt.setActionCommand(answer.toString());
		bt.addActionListener(actionListener);
		buttonMap.put(answer.getLabelKey(), bt);
		add(bt);
	}
	
	public final void setEnabled(Answer answer, boolean enabled) {
		JButton button = buttonMap.get(answer.getLabelKey());
		if(Objects.nonNull(button)) {
			button.setEnabled(enabled);
		}
	}

	@Override
	public void loadLabels() {
		for (String key : buttonMap.keySet()) {
			JButton bt = buttonMap.get(key);
			bt.setText(C.getLabel(key));
		}
	}

	public boolean isEnabled(Answer answer) {
		JButton button = buttonMap.get(answer.getLabelKey());
		if(Objects.nonNull(button)) {
			return button.isEnabled();
		}
		return false;
	}
}
