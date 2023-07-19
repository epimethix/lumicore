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

import java.util.Objects;

import javax.swing.JTextArea;
import javax.swing.text.Document;

import com.github.epimethix.lumicore.swing.util.LayoutUtils;

/**
 * <code>JTextAreaWithPopUp</code> is a <code>JTextArea</code> that offers the
 * customary text editing functions cut, copy and paste via a
 * <code>JPopupMenu</code>, triggered by right clicking.
 */
@SuppressWarnings("serial")
public class LTextArea extends JTextArea {

	private final TextComponentPopupMenu popupMenu;

	/**
	 * From <code>JTextArea</code> Javadoc: Constructs a new <code>JTextArea</code>.
	 * A default model is created, the initial string is null, and the rows/columns
	 * is set to 0.
	 * 
	 * @see JTextArea
	 */
	public LTextArea() {
		this(null, null, 0, 0);
	}

	/**
	 * From <code>JTextArea</code> Javadoc: Constructs a new <code>JTextArea</code>
	 * with the given document model, and defaults for all of the other arguments
	 * (null, 0, 0).
	 * 
	 * @param doc The model to use
	 * @see JTextArea
	 */
	public LTextArea(Document doc) {
		this(doc, null, 0, 0);
	}

	/**
	 * From <code>JTextArea</code> Javadoc: Constructs a new <code>JTextArea</code>
	 * with the specified text displayed. A default model is created and
	 * rows/columns are set to 0.
	 * 
	 * @param text the text to be displayed, or null
	 * @see JTextArea
	 */
	public LTextArea(String text) {
		this(null, text, 0, 0);
	}

	/**
	 * From <code>JTextArea</code> Javadoc: Constructs a new empty TextArea with the
	 * specified number of rows and columns. A default model is created, and the
	 * initial string is null.
	 * 
	 * @param rows    the number of rows &gt;= 0
	 * @param columns the number of columns &gt;= 0
	 * @exception IllegalArgumentException if the rows or columns arguments are
	 *                                     negative.
	 * @see JTextArea
	 */
	public LTextArea(int rows, int columns) {
		this(null, null, rows, columns);
	}

	/**
	 * From <code>JTextArea</code> Javadoc: Constructs a new TextArea with the
	 * specified text and number of rows and columns. A default model is created.
	 *
	 * @param text    the text to be displayed, or null
	 * @param rows    the number of rows &gt;= 0
	 * @param columns the number of columns &gt;= 0
	 * @exception IllegalArgumentException if the rows or columns arguments are
	 *                                     negative.
	 * @see JTextArea
	 */
	public LTextArea(String text, int rows, int columns) {
		this(null, text, rows, columns);
	}

	/**
	 * From <code>JTextArea</code> Javadoc: Constructs a new JTextArea with the
	 * specified number of rows and columns, and the given model. All of the
	 * constructors feed through this constructor.
	 *
	 * @param doc     the model to use, or create a default one if null
	 * @param text    the text to be displayed, null if none
	 * @param rows    the number of rows &gt;= 0
	 * @param columns the number of columns &gt;= 0
	 * @exception IllegalArgumentException if the rows or columns arguments are
	 *                                     negative.
	 * @see JTextArea
	 */
	public LTextArea(Document doc, String text, int rows, int columns) {
		super(doc, text, rows, columns);
		this.popupMenu = new TextComponentPopupMenu(this);
		setComponentPopupMenu(popupMenu);
		setLineWrap(true);
		setWrapStyleWord(true);
		setMargin(LayoutUtils.createDefaultMargin());
	}

	@Override
	public void setEditable(boolean b) {
		if (Objects.nonNull(popupMenu)) {
			popupMenu.setEditable(b);
		}
		super.setEditable(b);
	}

	@Override
	public void setEnabled(boolean enabled) {
		popupMenu.setEnabled(enabled);
		super.setEnabled(enabled);
	}
}
