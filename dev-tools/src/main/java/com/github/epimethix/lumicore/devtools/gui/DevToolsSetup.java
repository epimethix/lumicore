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
package com.github.epimethix.lumicore.devtools.gui;

import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.swing.dialog.wizard.AbstractWizard;
import com.github.epimethix.lumicore.swing.dialog.wizard.WizardUI;

public class DevToolsSetup extends AbstractWizard implements LabelsDisplayer {
	private static boolean finished;

	/**
	 * launches the dev tools setup
	 * 
	 * @return true if the setup was completed successfully
	 */
	public static final boolean launchSetup(Application application) {
		finished = false;
		WizardUI.runWizard(new DevToolsSetup());
		return finished;
	}

	private DevToolsSetup() {
		super(() -> D.getLabel(D.SETUP_TITLE));
	}

	@Override
	public void finish(boolean[] skipped) {
		finished = true;
	}

	@Override
	public void cancel() {}

	@Override
	public void clear() {}

	@Override
	public void loadLabels() {}

}
