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
package com.github.epimethix.lumicore.swing.dialog;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import com.github.epimethix.lumicore.common.ui.SplashScreen;
import com.github.epimethix.lumicore.swing.ImagePanel;

public abstract class SwingSplashScreen implements SplashScreen {

	private static SplashScreenWindow splashScreen;

	private static class SplashScreenWindow {

		private final JWindow window;

		private SplashScreenWindow(InputStream is, String label) {

			ImagePanel imgPanel = null;
			try {
				imgPanel = new ImagePanel(is);
			} catch (IOException e) {
				e.printStackTrace();
			}
//			imgPanel.setImage(is, true);

			window = new JWindow();

			JPanel content = new JPanel(new BorderLayout());
			content.add(imgPanel, BorderLayout.CENTER);
			if (Objects.nonNull(label) && !label.trim().isEmpty()) {
				JLabel lbInformation = new JLabel(label);
				content.add(lbInformation, BorderLayout.SOUTH);
			}
			window.setContentPane(content);
			window.pack();
			window.setLocationRelativeTo(null);
			
		}

		private void show() {
			window.setVisible(true);
		}

		private void hide() {
			window.setVisible(false);
			window.dispose();
		}
	}

	public SwingSplashScreen() {}

	protected final void showSplashScreen(final InputStream is, String label) {
		if (Objects.isNull(splashScreen)) {
			if (SwingUtilities.isEventDispatchThread()) {
				splashScreen = new SplashScreenWindow(is, label);
				splashScreen.show();
			} else {
				try {
					SwingUtilities.invokeAndWait(() -> {
						splashScreen = new SplashScreenWindow(is, label);
						splashScreen.show();
					});
				} catch (InvocationTargetException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public final void hideSplashScreen() {
		if (Objects.nonNull(splashScreen)) {
			splashScreen.hide();
			splashScreen = null;
		}
	}
}
