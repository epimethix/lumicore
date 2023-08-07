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
package com.github.epimethix.lumicore.common.swing;

import java.awt.Component;

import com.github.epimethix.lumicore.common.UserInterface;
import com.github.epimethix.lumicore.common.ui.Answer;

public interface SwingUI extends UserInterface {

	void showErrorMessage(Component c, String key, Object... args);

	void showPlainMessage(Component c, String key, Object... args);

	void showInfoMessage(Component c, String key, Object... args);

	void showWarningMessage(Component c, String key, Object... args);

	Answer showOkCancelDialog(Component c, String key, Object... args);

	Answer showYesNoDialog(Component c, String key, Object... args);

	Answer showYesNoCancelDialog(Component c, String key, Object... args);

	Answer showInputDialog(Component c, Component input, String key, Object... args);
}
