/*
 * Copyright 2022 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.swing.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.common.util.ClipBoardUtils;

@SuppressWarnings("serial")
class TextComponentPopupMenu extends JPopupMenu implements ActionListener, LabelsDisplayer {

	private final JTextComponent textComponent;

	private final JMenuItem miCut;
	private final JMenuItem miCopy;
	private final JMenuItem miPaste;

	public TextComponentPopupMenu(JTextComponent textComponent) {
		this.textComponent = textComponent;
		miCut = new JMenuItem();
		miCut.addActionListener(this);
		add(miCut);
		miCopy = new JMenuItem();
		miCopy.addActionListener(this);
		add(miCopy);
		miPaste = new JMenuItem();
		miPaste.addActionListener(this);
		add(miPaste);
	}

	public void setEnabled(boolean enabled) {
		miPaste.setEnabled(enabled);
		miCut.setEnabled(enabled);
	}

	public void setEditable(boolean editable) {
		setEnabled(editable);
	}

	@Override
	public void loadLabels() {
		miCut.setText(C.getLabel(C.CUT));
		miCopy.setText(C.getLabel(C.COPY));
		miPaste.setText(C.getLabel(C.PASTE));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == miCut) {
			String selection = textComponent.getSelectedText();
			if (Objects.nonNull(selection) && selection.length() > 0) {
				ClipBoardUtils.putString(selection);
				textComponent.replaceSelection("");
			}
		} else if (e.getSource() == miCopy) {
			String selection = textComponent.getSelectedText();
			if (Objects.nonNull(selection) && selection.length() > 0) {
				ClipBoardUtils.putString(selection);
			}
		} else if (e.getSource() == miPaste) {
			String clipString = ClipBoardUtils.getString();
			if (Objects.nonNull(clipString) && clipString.length() > 0) {
				textComponent.replaceSelection(clipString);
			}
		}

	}
}
