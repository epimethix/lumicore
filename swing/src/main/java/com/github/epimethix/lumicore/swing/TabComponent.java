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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public final class TabComponent extends JPanel implements ActionListener, MouseListener {

	private final JLabel labelChanged;
	private final JLabel label;
	private final JButton button;
	private final Color colorDefault;
	private final Color colorHighlighted;
	private final Consumer<TabComponent> closeConsumer;

	public TabComponent(String label, Consumer<TabComponent> closeConsumer) {
		super(new GridBagLayout());
		this.closeConsumer = closeConsumer;
		labelChanged = new JLabel("*");
		labelChanged.setVisible(false);
		setOpaque(false);
		this.label = new JLabel(label);
		button = new JButton("x");
		button.setOpaque(false);
		button.setBorder(BorderFactory.createEtchedBorder());
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.addActionListener(this);
		button.addMouseListener(this);
		colorDefault = button.getForeground();
		colorHighlighted = Color.RED;
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		add(labelChanged, gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		add(this.label, gbc);
		gbc.weightx = 0;
		gbc.gridx++;
		gbc.insets = new Insets(0, 7, 0, 0);
		add(button, gbc);
	}

	public void setLabel(String label) {
		this.label.setText(label);
	}

	public void setChanged(boolean changed) {
		labelChanged.setVisible(changed);
		revalidate();
	}

	/*
	 * MouseListener: JButton
	 */
	
	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (e.getSource() == button) {
			button.setForeground(colorHighlighted);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (e.getSource() == button) {
			button.setForeground(colorDefault);
		}
	}

	/*
	 * ActionListener: JButton
	 */

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == button) {
			closeConsumer.accept(this);
		}
	}
}
