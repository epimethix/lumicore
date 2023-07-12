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

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;

import com.github.epimethix.lumicore.swing.dialog.wizard.WizardUI.Trail;

public interface Wizard {

	JComponent getPage(int pageIndex);

	boolean validatePage(int pageIndex, JDialog parent);

	default boolean isPageRequired(int pageIndex) {
		return true;
	}

	default int getPagesToSkipFrom(int pageIndex) {
		throw new RuntimeException("when Wizard.isPageRequired() returns anything but true "
				+ "Wizard.getPagesToSkipFrom(int) must be implemented in tandem.");
	}

	default Trail[] getTrails() {
		return new Trail[0];
	}

	default int getTrailChoice(int splitPage) {
		throw new RuntimeException("when Wizard.getTrails() returns a non empty array "
				+ "Wizard.getTrailChoice(int) must be implemented in tandem.");
	}

	default boolean isFinishEnabled(int pageIndex) {
		return pageIndex + 1 == getPageCount();
	}

	Icon getPageIcon(int pageIndex);

	String getPageTitle(int pageIndex);

	String getWizardTitle();

	int getPageCount();

	void finish(boolean[] skipped);

	void cancel();

	void clear();
}
