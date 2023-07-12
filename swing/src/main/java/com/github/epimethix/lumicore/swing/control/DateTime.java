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
package com.github.epimethix.lumicore.swing.control;

import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.JTextField;

final class DateTime {
	final static String NULL_DATE_STRING = "---";
	final static Pattern NULL_DATE_REGEX = Pattern.compile("^[-]+$");
	final static Pattern DIGIT_REGEX = Pattern.compile("^\\d$");
	final static Pattern PATTERN_LETTER_REGEX = Pattern.compile("^[A-Za-z]$");

	static void autoPutNonPatternLetter(KeyEvent e, JTextField textField, int[] nonPatternLettersIndexes,
			char[] nonPatternLetters, int formatLength) {
		if (e.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
			String input = textField.getText();
			int inputLength = input.length();
			if (inputLength > formatLength) {
				textField.setText(input.substring(0, formatLength));
			} else if (inputLength > 0) {
				int inputIndex = inputLength - 1;
				String inputChar = input.substring(inputIndex);
				if (DIGIT_REGEX.matcher(inputChar).matches()) {
					for (int i = 0; i < nonPatternLettersIndexes.length; i++) {
						if (nonPatternLettersIndexes[i] == inputLength) {
							input += nonPatternLetters[i];
							for (i++; i < nonPatternLettersIndexes.length; i++) {
								inputLength = input.length();
								if (nonPatternLettersIndexes[i] == inputLength) {
									input += nonPatternLetters[i];
								} else {
									break;
								}
							}
							textField.setText(input);
							textField.setSelectionEnd(input.length());
							textField.setSelectionStart(input.length());
							return;
						} else if (nonPatternLettersIndexes[i] > inputLength) {
							break;
						}
					}
				} else {
					int previousChar = 0;
					for (int i = 0; i < nonPatternLettersIndexes.length; i++) {
						if (nonPatternLettersIndexes[i] < inputIndex) {
							previousChar = nonPatternLettersIndexes[i];
						} else if (nonPatternLetters[i] == inputChar.charAt(0)) {
							String inputValue;
							if (previousChar > 0) {
								inputValue = input.substring(previousChar + 1, input.length() - 1);
							} else {
								inputValue = input.substring(0, input.length() - 1);
							}
							try {
								int value = Integer.parseInt(inputValue);
								int width;
								String start;
								if (previousChar == 0) {
									width = nonPatternLettersIndexes[i];
									start = "";
								} else {
									width = nonPatternLettersIndexes[i] - (previousChar + 1);
									start = input.substring(0, previousChar + 1);
								}
								String rebuilt = start
										.concat(String.format("%0".concat(String.valueOf(width)).concat("d"), value))
										.concat(inputChar);
								textField.setText(rebuilt);
								textField.setSelectionEnd(rebuilt.length());
								textField.setSelectionStart(rebuilt.length());
								return;
							} catch (NumberFormatException e2) {}
						} else {
							return;
						}
//						"0123456789"
//						   2  5			5-2-1=3
//						"dd.dd.dddd"
//						 0 2			2-0=2
//						"dd.dd.dddd"
//						"dd.dd.dddd"
					}
				}
			}
		}
	}

	private DateTime() {}
}
