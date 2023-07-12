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

import javax.swing.text.JTextComponent;

import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.C;

class TextComponentValidation {
	static final int UCASE = 1;
	static final int LCASE = 2;

	private final JTextComponent textComponent;
	private final SwingUI ui;
	private final boolean required;

	private int minChars;
	private int maxChars;
	private int textCase;

	TextComponentValidation(SwingUI ui, JTextComponent textComponent, boolean required) {
		this.ui = ui;
		this.textComponent = textComponent;
		this.required = required;
	}

//	int getMinChars() {
//		return minChars;
//	}

	void setMinChars(int minChars) {
		this.minChars = minChars;
	}

//	int getMaxChars() {
//		return maxChars;
//	}

	void setMaxChars(int maxChars) {
		this.maxChars = maxChars;
	}

//	int getTextCase() {
//		return textCase;
//	}

	void setTextCase(int textCase) {
		this.textCase = textCase;
	}

//	public static enum TextError {
//		NONE, NOT_EXACT_LENGTH, TOO_LONG, TOO_SHORT;
//	}

	boolean validate(String fieldName) {
		String testText = textComponent.getText().trim();
		int testLength = testText.length();
		if (required && testLength == 0) {
			ui.showErrorMessage(textComponent, C.FIELD_MAY_NOT_BE_EMPTY_ERROR, fieldName);
			return false;
		} else if (minChars > 0 && minChars == maxChars && testLength != minChars) {
			ui.showErrorMessage(textComponent, C.TEXT_VALIDATION_ERROR_EXACT_LENGTH, fieldName, minChars);
			return false;
		} else if (minChars > 0 && testLength < minChars) {
			ui.showErrorMessage(textComponent, C.TEXT_VALIDATION_ERROR_MIN_LENGTH, fieldName, minChars);
			return false;
		} else if (maxChars > 0 && testLength > maxChars) {
			ui.showErrorMessage(textComponent, C.TEXT_VALIDATION_ERROR_MAX_LENGTH, fieldName, maxChars);
			return false;
		}
		if (textCase == UCASE) {
			textComponent.setText(testText.toUpperCase());
		} else if (textCase == LCASE) {
			textComponent.setText(testText.toLowerCase());
		}
		return true;
	}

}
