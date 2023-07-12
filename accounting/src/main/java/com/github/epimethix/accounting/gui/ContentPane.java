/*
 *  Accounting - Lumicore example application
 *  Copyright (C) 2023  epimethix@protonmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.epimethix.accounting.gui;

import java.awt.Color;
import java.util.Objects;

import javax.swing.JPanel;

import com.github.epimethix.lumicore.ioc.annotation.PostConstruct;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;

@SwingComponent
public class ContentPane extends JPanel {

	private final ExampleMainController mainController;

	public ContentPane(ExampleMainController mainController) {

		Objects.requireNonNull(mainController);

		this.mainController = mainController;
	}

	@PostConstruct
	public void init() {
		setBackground(Color.cyan);
	}

}
