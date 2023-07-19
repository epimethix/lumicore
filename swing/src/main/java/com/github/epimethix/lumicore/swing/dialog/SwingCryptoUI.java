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

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.common.ui.CryptoUI;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayerPool;
import com.github.epimethix.lumicore.swing.LumicoreSwing;
import com.github.epimethix.lumicore.swing.dialog.CryptoDialog.Mode;

public class SwingCryptoUI implements CryptoUI {

	private final CryptoDialog cryptoDialog;

	private final Supplier<Optional<Credentials>> setupSecret;

	private final Function<char[], Optional<Credentials>> resetSecret;

	private final Supplier<Optional<Credentials>> getSecret;
	
	

	public SwingCryptoUI(Application application, Class<?> guiControllerClass, Mode mode) {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new RuntimeException("SwingCryptoUI must be instantiated on the Event Dispatch Thread!");
		}

		LabelsDisplayerPool.registerUiPackage(LumicoreSwing.class.getPackageName());
		LabelsDisplayerPool.registerUiPackage(guiControllerClass.getPackageName());
		LabelsDisplayerPool.addLabelsDisplayers(this);
		cryptoDialog = new CryptoDialog(application, null, mode);
		setupSecret = () -> {
			return cryptoDialog.setupSecret();
		};
		resetSecret = (oldSecret) -> {
			return cryptoDialog.resetSecret(oldSecret);
		};
		getSecret = () -> {
			return cryptoDialog.getSecret();
		};
	}

	@Override
	public Optional<Credentials> setupSecret() {
		if (SwingUtilities.isEventDispatchThread()) {
			return setupSecret.get();
		} else {
			final Optional<?>[] sec = new Optional<?>[1];
			try {
				SwingUtilities.invokeAndWait(() -> {
					sec[0] = setupSecret.get();
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
			if (Objects.nonNull(sec[0])) {
				return (Optional<Credentials>) sec[0];
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Credentials> resetSecret(char[] oldPassword) {
		if (SwingUtilities.isEventDispatchThread()) {
			return resetSecret.apply(oldPassword);
		} else {
			final Optional<?>[] sec = new Optional<?>[1];
			try {
				SwingUtilities.invokeAndWait(() -> {
					sec[0] = resetSecret.apply(oldPassword);
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
			if (Objects.nonNull(sec[0])) {
				return (Optional<Credentials>) sec[0];
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Credentials> getSecret() {
		if (SwingUtilities.isEventDispatchThread()) {
			return getSecret.get();
		} else {
			final Optional<?>[] sec = new Optional<?>[1];
			try {
				SwingUtilities.invokeAndWait(() -> {
					sec[0] = getSecret.get();
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
			if (Objects.nonNull(sec[0])) {
				return (Optional<Credentials>) sec[0];
			}
		}
		return Optional.empty();
	}

}
