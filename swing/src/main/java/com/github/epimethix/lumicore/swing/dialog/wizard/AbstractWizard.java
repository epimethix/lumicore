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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;

public abstract class AbstractWizard implements Wizard {

	private final Supplier<String> title;
	private final List<WizardPage> pages;
	
	private int size = 0;

	protected AbstractWizard(Supplier<String> title) {
		this.title = title;
		pages = new ArrayList<>();
	}

//	public AbstractWizard(Supplier<String> title2) {
//		// TODO Auto-generated constructor stub
//	}

	protected int addPage(JComponent pageUI, Supplier<String> title, Icon icon, Function<JDialog, Boolean> isValid) {
		return addPage(new DefaultWizardPage(Objects.requireNonNull(pageUI), Objects.requireNonNull(title),
				icon, Objects.requireNonNull(isValid)));

	}

	protected int addPage(WizardPage wizardPage) {
		pages.add(Objects.requireNonNull(wizardPage));
		return size++;
	}

	@Override
	public JComponent getPage(int pageIndex) {
		return pages.get(pageIndex).getPage();
	}

	@Override
	public boolean validatePage(int pageIndex, JDialog parent) {
		return pages.get(pageIndex).isValid().apply(parent);
	}

	@Override
	public Icon getPageIcon(int pageIndex) {
		return pages.get(pageIndex).getIcon();
	}

	@Override
	public String getPageTitle(int pageIndex) {
		return pages.get(pageIndex).getTitle().get();
	}

	@Override
	public String getWizardTitle() {
		return title.get();
	}

	@Override
	public int getPageCount() {
		return size;
	}
}
