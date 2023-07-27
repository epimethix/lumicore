package com.github.epimethix.lumicore.swing;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import com.github.epimethix.lumicore.common.CryptoDatabaseApplication;
import com.github.epimethix.lumicore.common.ui.AbstractApplication;
import com.github.epimethix.lumicore.common.ui.CryptoUI;
import com.github.epimethix.lumicore.swing.dialog.CryptoDialog.Mode;
import com.github.epimethix.lumicore.swing.dialog.SwingCryptoUI;

public abstract class AbstractSwingCryptoApplication extends AbstractApplication implements CryptoDatabaseApplication {

	private CryptoUI cryptoUI;

	public AbstractSwingCryptoApplication(Mode mode) {
		try {
			SwingUtilities.invokeAndWait(() -> {
				cryptoUI = new SwingCryptoUI(this, getClass(), mode);
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public CryptoUI getCryptoUI() {
		return cryptoUI;
	}

}
