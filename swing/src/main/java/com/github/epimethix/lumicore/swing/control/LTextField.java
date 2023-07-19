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

import java.awt.Component;
import java.util.Objects;

import javax.swing.ComboBoxEditor;
import javax.swing.JTextField;
import javax.swing.text.Document;

import com.github.epimethix.lumicore.swing.util.LayoutUtils;

/**
 * <code>JTextFieldWithPopUp</code> is a <code>JTextField</code> that offers the
 * customary text editing functions cut, copy and paste via a
 * <code>JPopupMenu</code>, triggered by right clicking. It also implements
 * <code>ComboBoxEditor</code> for use in <code>JComboBox</code>.
 * 
 */
@SuppressWarnings("serial")
public class LTextField extends JTextField implements ComboBoxEditor {
	private final TextComponentPopupMenu popupMenu;

	/**
	 * From <code>JTextField</code> Javadoc: Constructs a new
	 * <code>JTextField</code>. A default model is created, the initial string is
	 * null, and the number of columns is set to 0.
	 * 
	 * @see JTextField
	 */
	public LTextField() {
		this(0);
	}

	/**
	 * From <code>JTextField</code> Javadoc: Constructs a new empty
	 * <code>JTextField</code> with the specified number of columns. A default model
	 * is created and the initial string is set to null.
	 * 
	 * @param columns The number of columns to use to calculate the preferred width;
	 *                if columns is set to zero, the preferred width will be
	 *                whatever naturally results from the component implementation.
	 * @see JTextField
	 */
	public LTextField(int columns) {
		this(null, columns);
	}

	/**
	 * From <code>JTextField</code> Javadoc: Constructs a new
	 * <code>JTextField</code> initialized with the specified text. A default model
	 * is created and the number of columns is 0.
	 * 
	 * @param text The text to be displayed, or <code>null</code>.
	 * @see JTextField
	 */
	public LTextField(String text) {
		this(text, 0);
	}

	/**
	 * From <code>JTextField</code> Javadoc: Constructs a new
	 * <code>JTextField</code> initialized with the specified text and columns. A
	 * default model is created.
	 * 
	 * @param text    The text to be displayed, or <code>null</code>.
	 * @param columns The number of columns to use to calculate the preferred width;
	 *                if columns is set to zero, the preferred width will be
	 *                whatever naturally results from the component implementation.
	 * @see JTextField
	 */
	public LTextField(String text, int columns) {
		this(null, text, columns);
	}

	/**
	 * From <code>JTextField</code> Javadoc: Constructs a new
	 * <code>JTextField</code> that uses the given text storage model and the given
	 * number of columns. This is the constructor through which the other
	 * constructors feed. If the document is <code>null</code>, a default model is
	 * created.
	 * 
	 * @param doc     The text storage to use; if this is <code>null</code>, a
	 *                default will be provided by calling the
	 *                <code>createDefaultModel</code> method.
	 * @param text    The text to be displayed, or <code>null</code>.
	 * @param columns The number of columns to use to calculate the preferred width;
	 *                if columns is set to zero, the preferred width will be
	 *                whatever naturally results from the component implementation.
	 * @see JTextField
	 */
	public LTextField(Document doc, String text, int columns) {
		super(doc, text, columns);
		this.popupMenu = new TextComponentPopupMenu(this);
		setComponentPopupMenu(popupMenu);
		setMargin(LayoutUtils.createDefaultTextMargin());
	}

	/**
	 * {@inheritDoc} Also the <code>JMenuItem</code>s cut and paste are enabled or
	 * disabled.
	 */
	@Override
	public void setEditable(boolean editable) {
		if (Objects.nonNull(popupMenu)) {
			popupMenu.setEditable(editable);
		}
		super.setEditable(editable);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * When <code>setEnabled(false)</code> the popup menu is disabled too.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		popupMenu.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	@Override
	public Component getEditorComponent() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param anObject Preferably a <code>String</code> value. If the
	 *                 <code>Object</code> passed is not an instance of
	 *                 <code>String</code> the <code>toString</code> method is used.
	 *                 If <code>null</code> is passed an empty <code>String</code>
	 *                 is set.
	 */
	@Override
	public void setItem(Object anObject) {
		if (Objects.isNull(anObject)) {
			setText("");
		} else if (anObject instanceof String) {
			setText((String) anObject);
		} else {
			setText(anObject.toString());
		}
	}

	@Override
	public String getItem() {
		return getText();
	}
}