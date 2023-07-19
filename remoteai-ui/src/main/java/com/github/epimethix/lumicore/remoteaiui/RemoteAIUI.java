/*
 *  RemoteAI UI - Lumicore example application
 *  Copyright (C) 2023  epimethix@protonmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.epimethix.lumicore.remoteaiui;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Optional;

import javax.swing.SwingUtilities;

import org.sqlite.mc.SQLiteMCChacha20Config;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.CryptoDatabaseApplication;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.sql.ConnectionFactory;
import com.github.epimethix.lumicore.common.ui.CryptoUI;
import com.github.epimethix.lumicore.ioc.Lumicore;
import com.github.epimethix.lumicore.ioc.annotation.Autowired;
import com.github.epimethix.lumicore.orm.sqlite.SQLiteUtils;
import com.github.epimethix.lumicore.properties.PropertiesFile;
import com.github.epimethix.lumicore.remoteaiui.db.AppDB;
import com.github.epimethix.lumicore.remoteaiui.service.GeneratorServiceImpl;
import com.github.epimethix.lumicore.remoteaiui.ui.GUIController;
import com.github.epimethix.lumicore.stackutil.AccessCheck;
import com.github.epimethix.lumicore.swing.dialog.CryptoDialog.Mode;
import com.github.epimethix.lumicore.swing.dialog.SwingCryptoUI;

public final class RemoteAIUI implements CryptoDatabaseApplication {

	private static final AppProperties PROPERTIES = new AppProperties();

	public static void main(String[] args) {
		try {
			Lumicore.startApplication(RemoteAIUI.class, args, PropertiesFile.getProperties(PROPERTIES.getFile().getPath()));
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Autowired
	private AppDB db;

	private CryptoUI cryptoUI;

	public RemoteAIUI() {
		try {
			SwingUtilities.invokeAndWait(() -> {
				cryptoUI = new SwingCryptoUI(this, GUIController.class, Mode.KEY);
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getApplicationName() {
		return "Remote AI UI";
	}

	@Override
	public Locale getDefaultLocale() {
		return PROPERTIES.getDefaultLocale();
	}

	@Override
	public void setDefaultLocale(Locale locale) {
		PROPERTIES.setDefaultLocale(locale);
	}

	@Override
	public File getNestingDirectory() {
		return AppFiles.NESTING_DIR;
	}

	@Override
	public long getApplicationVersion() {
		return 1;
	}

	@Override
	public int getApplicationId() {
		return 1938565738;
	}

	@Override
	public ConnectionFactory createConnectionFactory(Class<? extends Database> dbClass) {
		return SQLiteUtils.connectToFile(AppFiles.DATABASE_FILE)
				.userAuthenticate(SQLiteMCChacha20Config.getDefault().toProperties(), this);
	}

//	@Override
//	public SQLConnection initializeConnection(Database database) {
//		return LumiSQLiteConnection.Builder.newBuilder(AppFiles.DATABASE_FILE)
//				.userAuthenticate(SQLiteMCChacha20Config.getDefault(), this);
//	}

	public Optional<String> getApiKey() throws IllegalAccessException {
		try {
			AccessCheck.allowCaller(false, GUIController.class.getName().concat("::runSetup"),
					GeneratorServiceImpl.class.getName().concat("::init"));
		} catch (IllegalAccessException e) {
			System.err.println(e.getMessage());
			throw e;
		}
		return db.getApiKey();
	}

	public void setApiKey(String text) {
		db.setApiKey(text);
	}

	public Optional<String> getTextOutputDirectory() {
		return db.getTextOutputDirectory();
	}

	public void setTextOutputDirectory(String path) {
		db.setTextOutputDirectory(path);
	}

	public Optional<String> getImageOutputDirectory() {
		return db.getImageOutputDirectory();
	}

	public void setImageOutputDirectory(String path) {
		db.setImageOutputDirectory(path);
	}

	@Override
	public CryptoUI getCryptoUI() {
		return cryptoUI;
	}
}
