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
package com.github.epimethix.lumicore.swing;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.github.epimethix.lumicore.swing.util.LayoutUtils;

@SuppressWarnings("serial")
public final class StatusBar extends JPanel {

	private final JLabel[] statusMessage;

	public StatusBar() {
		this(1);
	}

	public StatusBar(int numberFields) {
		super(new GridLayout(1, numberFields));
		setBorder(LayoutUtils.createDefaultEmptyBorder());

		statusMessage = new JLabel[numberFields];
		for (int i = 0; i < numberFields; i++) {
			statusMessage[i] = new JLabel(" ");
			if (i > 0)  {
				statusMessage[i].setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.GRAY));
			}
			add(statusMessage[i]);
		}
	}

	public final void showMessage(String message) {
		showMessage(message, 0);
	}

	public final void showMessage(String message, int fieldIndex) {
		showMessage(message, fieldIndex, 0L);
	}

	public void showMessage(String message, int fieldIndex, long timeOut) {
		statusMessage[fieldIndex].setText(" " + message);
		if (fieldIndex == 0 && timeOut == 0) {
			HideWorker hw = new HideWorker(fieldIndex, 5_000L);
			hw.execute();
		} else if (timeOut > 0L) {
			HideWorker hw = new HideWorker(fieldIndex, timeOut);
			hw.execute();

		}
	}

	private class HideWorker extends SwingWorker<Void, Void> {

		private final int fieldIndex;
		private final long timeOut;

		public HideWorker(int fieldIndex, long timeOut) {
			this.fieldIndex = fieldIndex;
			this.timeOut = timeOut;
		}

		@Override
		protected Void doInBackground() throws Exception {
			Thread.sleep(timeOut);
			return null;
		}

		@Override
		protected void done() {
			statusMessage[fieldIndex].setText(" ");
		}

	}
}
