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

import java.awt.GridBagConstraints;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class GridBagUtils {


	public static GridBagConstraints initGridBagConstraints() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = c.weighty = 0.0d;
		c.insets = LayoutUtils.createDefaultMargin();
		return c;
	}

	public static void addGridBagLine(JComponent panel, GridBagConstraints c, JLabel label, JComponent control) {
		panel.add(label, c);
		c.gridx++;
		c.weightx = 1.0d;
		panel.add(control, c);
		c.gridx--;
		c.gridy++;
		c.weightx = 0.0d;
	}

	public static void addGridBagLine(JComponent panel, GridBagConstraints c, JComponent control) {
		c.gridwidth = 2;
		c.weightx = 1.0d;
		panel.add(control, c);
		c.gridwidth = 1;
		c.gridy++;
		c.weightx = 0.0d;
	}

	public static void finishGridBagForm(JComponent panel, GridBagConstraints c, int gridwidth) {
		c.gridwidth = gridwidth;
		c.weightx = 1.0d;
		c.weighty = 1.0d;
		c.fill = GridBagConstraints.BOTH;
		panel.add(new JPanel(), c);
		c.gridwidth = 1;
		c.weightx = 0.0d;
		c.weighty = 0.0d;
		c.fill = GridBagConstraints.HORIZONTAL;
	}
	
	private GridBagUtils() {}
}
