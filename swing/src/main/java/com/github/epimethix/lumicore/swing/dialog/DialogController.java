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

import java.awt.Dimension;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public class DialogController {

	private final JDialog d;

	private final DialogControllerThread thread;

	private volatile String lastAction = null;

	public DialogController(JDialog d) {
		this.d = Objects.requireNonNull(d);
		thread = new DialogControllerThread();
		new Thread(thread).start();
	}

	public final synchronized void setVisible(boolean b) {
		if (!b) {
			lastAction = "HIDE";
			thread.latch.countDown();
		} else {
			if (Objects.isNull(lastAction)) {
				d.setMinimumSize(new Dimension(0, 0));
				d.pack();
				d.setMinimumSize(d.getSize());
				d.setLocationRelativeTo(d.getParent());
				lastAction = "SHOW";
				System.err.println("SHOWING");
				thread.latch.countDown();
				d.setVisible(true);
			} else {
				lastAction = "ABORT";
				thread.latch.countDown();
			}
		}
	}

	private final class DialogControllerThread implements Runnable {
		private CountDownLatch latch = new CountDownLatch(2);

		private volatile boolean isDisposed = false;

		@Override
		public void run() {
			do {
				do {
					try {
						latch.await();
						break;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} while (true);
				if ("HIDE".equals(lastAction)) {
					System.err.println("HIDING");
					SwingUtilities.invokeLater(() -> d.setVisible(false));
				} else {
					System.err.println(lastAction);
				}
				lastAction = null;
				latch = new CountDownLatch(2);
			} while (!isDisposed);
		}
	}

	public JDialog getDialog() {
		return d;
	}
}
