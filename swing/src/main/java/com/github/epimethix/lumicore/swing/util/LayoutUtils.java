/*
 * Copyright 2022-2023 epimethix@protonmail.com
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;

import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;

public final class LayoutUtils {

	public final static String DEFAULT_MARGIN_KEY = "swing.default-margin";
	public final static int DEFAULT_MARGIN;
	public final static String MEDIUM_MARGIN_KEY = "swing.medium-margin";
	public final static int MEDIUM_MARGIN;
	public final static String LARGE_MARGIN_KEY = "swing.large-margin";
	public final static int LARGE_MARGIN;
	public final static String DEFAULT_TEXT_MARGIN_KEY = "swing.default-text-margin";
	public final static int DEFAULT_TEXT_MARGIN;
	public final static String DEFAULT_SCROLL_INCREMENT_KEY = "swing.default-scroll-increment";
	public final static int DEFAULT_SCROLL_INCREMENT;

	public final static String DEFAULT_DIALOG_WIDTH_KEY = "swing.default-dialog-width";
	public final static int DEFAULT_DIALOG_WIDTH;

	public final static String DEFAULT_FONT_SIZE_KEY = "swing.default-font-size";
	public final static int DEFAULT_FONT_SIZE;

	public final static String SMALL_FONT_SIZE_KEY = "swing.small-font-size";
	public final static int SMALL_FONT_SIZE;

	public final static String LARGE_FONT_SIZE_KEY = "swing.large-font-size";
	public final static int LARGE_FONT_SIZE;

	public final static Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

	static {
		int defMrg = 5;
		int mediumMargin = 10;
		int largeMargin = 15;
		int defTxtMrg = 3;
		int defScrInc = 20;
		int defDialogWidth = getDialogWidth("25%");
		int defFontSize = 12;
		int smallFontSize = 10;
		int largeFontSize = 16;
		try {
			ResourceBundle rb = ResourceBundle.getBundle("lumicore");
			if (Objects.nonNull(rb)) {
				if (rb.containsKey(DEFAULT_MARGIN_KEY)) {
					try {
						defMrg = Integer.parseInt(rb.getString(DEFAULT_MARGIN_KEY));
					} catch (NumberFormatException e) {}
				}
				if (rb.containsKey(MEDIUM_MARGIN_KEY)) {
					try {
						mediumMargin = Integer.parseInt(rb.getString(MEDIUM_MARGIN_KEY));
					} catch (NumberFormatException e) {}
				}
				if (rb.containsKey(LARGE_MARGIN_KEY)) {
					try {
						largeMargin = Integer.parseInt(rb.getString(LARGE_MARGIN_KEY));
					} catch (NumberFormatException e) {}
				}
				if (rb.containsKey(DEFAULT_TEXT_MARGIN_KEY)) {
					try {
						defTxtMrg = Integer.parseInt(rb.getString(DEFAULT_TEXT_MARGIN_KEY));
					} catch (NumberFormatException e) {}
				}
				if (rb.containsKey(DEFAULT_SCROLL_INCREMENT_KEY)) {
					try {
						defScrInc = Integer.parseInt(rb.getString(DEFAULT_SCROLL_INCREMENT_KEY));
					} catch (NumberFormatException e) {}
				}
				if (rb.containsKey(DEFAULT_DIALOG_WIDTH_KEY)) {
					try {
						defDialogWidth = getDialogWidth(rb.getString(DEFAULT_DIALOG_WIDTH_KEY));
					} catch (NumberFormatException e) {}
				}
				if (rb.containsKey(DEFAULT_FONT_SIZE_KEY)) {
					try {
						defFontSize = Integer.parseInt(rb.getString(DEFAULT_FONT_SIZE_KEY));
					} catch (NumberFormatException e) {}
				}
				if (rb.containsKey(SMALL_FONT_SIZE_KEY)) {
					try {
						smallFontSize = Integer.parseInt(rb.getString(SMALL_FONT_SIZE_KEY));
					} catch (NumberFormatException e) {}
				}
				if (rb.containsKey(LARGE_FONT_SIZE_KEY)) {
					try {
						largeFontSize = Integer.parseInt(rb.getString(LARGE_FONT_SIZE_KEY));
					} catch (NumberFormatException e) {}
				}
			}
		} catch (MissingResourceException e) {}
		DEFAULT_MARGIN = defMrg;
		MEDIUM_MARGIN = mediumMargin;
		LARGE_MARGIN = largeMargin;
		DEFAULT_TEXT_MARGIN = defTxtMrg;
		DEFAULT_SCROLL_INCREMENT = defScrInc;
		DEFAULT_DIALOG_WIDTH = defDialogWidth;
		DEFAULT_FONT_SIZE = defFontSize;
		SMALL_FONT_SIZE = smallFontSize;
		LARGE_FONT_SIZE = largeFontSize;

		setDefaultFont(defFontSize, smallFontSize);
	}

	private static int getDialogWidth(String property) throws NumberFormatException {
		int result = -1;
		if (property.endsWith("%")) {
			int percent = Integer.parseInt(property.substring(0, property.length() - 1));
			double percentDbl = percent / 100.0;
			result = (int) (SCREEN_SIZE.width * percentDbl);
		} else {
			result = Integer.parseInt(property);
		}
		return result;
	}
	
	/*
	 * Margins / Borders
	 */

	public static Insets createDefaultMargin() {
		return new Insets(DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN);
	}

	public static Insets createDefaultTextMargin() {
		return new Insets(DEFAULT_TEXT_MARGIN, DEFAULT_TEXT_MARGIN, DEFAULT_TEXT_MARGIN, DEFAULT_TEXT_MARGIN);
	}

	public static Border createDefaultEmptyBorder() {
		return BorderFactory.createEmptyBorder(DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_TEXT_MARGIN, DEFAULT_MARGIN);
	}

	public static Border createDefaultEmptyTextBorder() {
		return BorderFactory.createEmptyBorder(DEFAULT_TEXT_MARGIN, DEFAULT_TEXT_MARGIN, DEFAULT_TEXT_MARGIN,
				DEFAULT_TEXT_MARGIN);
	}

	public static Border createMediumEmptyBorder() {
		return BorderFactory.createEmptyBorder(MEDIUM_MARGIN, MEDIUM_MARGIN, MEDIUM_MARGIN, MEDIUM_MARGIN);
	}

	public static Insets createDefaultEmptyMargin() {
		return new Insets(0, 0, 0, 0);
	}

	public static Insets createDefaultLeftMargin() {
		return new Insets(0, DEFAULT_MARGIN, 0, 0);
	}

	public static Insets createDefaultRightMargin() {
		return new Insets(0, 0, 0, DEFAULT_MARGIN);
	}

	public static Insets createDefaultBottomLeftMargin() {
		return new Insets(0, DEFAULT_MARGIN, DEFAULT_MARGIN, 0);
	}

	public static Insets createDefaultBottomRightMargin() {
		return new Insets(0, 0, DEFAULT_MARGIN, DEFAULT_MARGIN);
	}

	public static Insets createDefaultBottomMargin() {
		return new Insets(0, 0, DEFAULT_MARGIN, 0);
	}

	public static Insets createDefaultTopRightMargin() {
		return new Insets(DEFAULT_MARGIN, 0, 0, DEFAULT_MARGIN);
	}

	public static Insets createDefaultTopMargin() {
		return new Insets(DEFAULT_MARGIN, 0, 0, 0);
	}

	private static final void setDefaultFont(int defFontSize, int smallFontSize) {
		List<Integer> fontSizes = getFontSizes();
		Map<Integer, Integer> fontOldToNewMap = new HashMap<>();
		// TODO check cases
		if (fontSizes.size() == 1) {
			fontOldToNewMap.put(fontSizes.get(0), defFontSize);
		} else if (fontSizes.size() == 2) {
			fontOldToNewMap.put(fontSizes.get(0), smallFontSize);
			fontOldToNewMap.put(fontSizes.get(1), defFontSize);
//		} else if(fontSizes.size() == 3) {
		}
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				FontUIResource f = (FontUIResource) value;
				int fontSize = fontOldToNewMap.get(f.getSize());
				FontUIResource f2 = new FontUIResource(f.getName(), f.getStyle(), fontSize);
				UIManager.put(key, f2);
			}
		}
	}

	public static final List<Integer> getFontSizes() {
		List<Integer> fontSizes = new ArrayList<>();
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				FontUIResource f = (FontUIResource) value;
				if (!fontSizes.contains(f.getSize())) {
					fontSizes.add(f.getSize());
				}
			}
		}
		Collections.sort(fontSizes);
		return fontSizes;
	}

	public static final void printUIManagerDefaults() {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				FontUIResource f = (FontUIResource) value;
				System.out.print(f.getSize() + "\t");
			}
			System.out.print(key.toString() + "::");
			System.out.println(Objects.nonNull(value) ? value.toString() : "null");
		}
	}
	
	/*
	 * Initialize / Build Components
	 */

	public static JScrollPane initScrollPane(Component view) {
		JScrollPane sp = new JScrollPane(view);
		sp.getVerticalScrollBar().setUnitIncrement(DEFAULT_SCROLL_INCREMENT);
		sp.getHorizontalScrollBar().setUnitIncrement(DEFAULT_SCROLL_INCREMENT);
		return sp;
	}

	public static JLabel getTitleLabel(String labelKey, Object... args) {
		JLabel l = new JLabel(LabelsManagerPool.getLabel(labelKey, args));
		Font lFont = l.getFont();
		l.setFont(new Font(lFont.getName(), Font.BOLD, LARGE_FONT_SIZE));
		return l;
	}

	public static JLabel getTitleLabel() {
		JLabel lbTitle = new JLabel();
		Font currentFont = lbTitle.getFont();
		lbTitle.setFont(new Font(currentFont.getName(), Font.BOLD, LARGE_FONT_SIZE));
		return lbTitle;
	}

	public static void touch() {}

	private LayoutUtils() {}
}
