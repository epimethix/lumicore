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
package com.github.epimethix.lumicore.swing.dialog.wizard;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;

public class DefaultWizardPage implements WizardPage {

	private final JComponent page;
	private final Supplier<String> title;
	private final Icon icon;
	private final Function<JDialog, Boolean> isValid;

	public DefaultWizardPage(JComponent page, Supplier<String> title, Icon icon, Function<JDialog, Boolean> isValid) {
		super();
		this.page = page;
		this.title = title;
		this.icon = icon;
		this.isValid = isValid;
	}

	@Override
	public JComponent getPage() {
		return page;
	}

	@Override
	public Supplier<String> getTitle() {
		return title;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public Function<JDialog, Boolean> isValid() {
		return isValid;
	}

}
