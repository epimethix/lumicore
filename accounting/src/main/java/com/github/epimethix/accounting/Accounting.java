/*
 *  Accounting - Lumicore example application
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
package com.github.epimethix.accounting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import javax.swing.SwingUtilities;

import com.github.epimethix.accounting.gui.L;
import com.github.epimethix.lumicore.common.AbstractDatabaseApplication;
import com.github.epimethix.lumicore.common.ApplicationUtils;
import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.sql.ConnectionFactory;
import com.github.epimethix.lumicore.common.ui.SplashScreenController;
import com.github.epimethix.lumicore.ioc.Lumicore;
import com.github.epimethix.lumicore.ipc.FileSystemIPCController;
import com.github.epimethix.lumicore.ipc.IPCController;
import com.github.epimethix.lumicore.ipc.IPCController.Mode;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;
import com.github.epimethix.lumicore.orm.sqlite.SQLiteUtils;
import com.github.epimethix.lumicore.profile.Profile;
import com.github.epimethix.lumicore.properties.PropertiesFile;
import com.github.epimethix.lumicore.swing.dialog.DefaultSwingSplashScreen;
/**
 * Lumicore example application
 * 
 * @author epimethix
 *
 */
public class Accounting extends AbstractDatabaseApplication {
	private final static Logger LOGGER;
	private static final int APP_ID;
	private static final long CURRENTLY_REQ_APP_VERSION;
	private static final long APP_VERSION;
	private static final String APP_ID_KEY = "app-id";
	private static final String CURRENTLY_REQ_APP_VERSION_KEY = "required-app-version";
	private static final String APP_VERSION_KEY = "app-version";

	private static IPCController ipcController;

	static {
		/*
		 * Profile must be loaded before logger creation to enable logging in the main
		 * method?
		 */
		Lumicore.loadProfile(AppFiles.PROFILE_FILE);
		LOGGER = Log.getLogger();
		/*
		 * Loading some application.properties
		 */
		PropertiesFile applicationProperties = null;
		try {
			applicationProperties = PropertiesFile.getProperties("application");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (Objects.nonNull(applicationProperties)) {
			APP_ID = applicationProperties.getIntProperty(APP_ID_KEY, 0);
			CURRENTLY_REQ_APP_VERSION = applicationProperties.getLongProperty(CURRENTLY_REQ_APP_VERSION_KEY, 0L);
			APP_VERSION = applicationProperties.getLongProperty(APP_VERSION_KEY, 0L);
		} else {
			APP_ID = 0;
			CURRENTLY_REQ_APP_VERSION = 0L;
			APP_VERSION = 0L;
		}
	}

	public static void main(String[] args) throws IOException {
		/*
		 * Example args
		 */
		if (args.length == 0) {
			args = new String[] { "some", "arguments", "from", "main" };
		}
		/*
		 * Locking on AppFiles.LOCK_FILE to enforce single instance
		 */
		if (ApplicationUtils.lockSingleInstance(AppFiles.LOCK_FILE)) {
			try {
				SwingUtilities.invokeAndWait(() -> SplashScreenController.showSplashScreen(
						new DefaultSwingSplashScreen(Accounting.class, "/splash.png", "Hello Accounting")));
			} catch (InvocationTargetException | InterruptedException e1) {
				e1.printStackTrace();
			}
			;
			LOGGER.info("Hello Main");

			System.err.println("Example Application nesting directory: " + AppFiles.APP_DIR.getPath());

			ipcController = FileSystemIPCController.getIPCController(AppFiles.MESSAGES_DIR, Mode.RECEIVER);
			try {
				Lumicore.startApplication(Accounting.class, args);
			} catch (ConfigurationException e) {
				if (Profile.isProfileActive(Profile.PRODUCTION)) {
					e.printStackTrace();
				} else {
					LOGGER.error(e);
				}
				System.exit(-1);
			}
		} else {
			/*
			 * If locking on file for single application instance failed then the main args
			 * are passed to the currently running instance.
			 */
			ipcController = FileSystemIPCController.getIPCController(AppFiles.MESSAGES_DIR, Mode.SENDER);
			ipcController.putMessage(String.join(";", args));
			System.exit(0);
			// exit
		}
//		Runtime.getRuntime().addShutdownHook(new Thread() {
//			@Override
//			public void run() {
//				AppFiles.DB_FILE.delete();
//			}
//		});
	}

	/*
	 * Example settings file for storing the default locale <p> another demo of
	 * PropertiesFile
	 */
	private final ExampleSettings exampleSettings;

	public Accounting(String[] args) {
//		super(AppFiles.LOCK_FILE);
		LOGGER.info("Hello Application args=" + Arrays.asList(args).toString());
		ExampleSettings es = null;
		try {
			es = new ExampleSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ipcController.addMessageListener(this::openDocument);
		this.exampleSettings = es;
	}

	/**
	 * This method is used as {@code MessageListener} for applications instance that
	 * are started additionally. as a default behavior default the main args are passed.
	 * 
	 * @param args the args from the main method from another application instance
	 */
	public void openDocument(String args) {
		System.err.println("Open Document: " + args);
		// TODO load Document, show UI...
	}

	@Override
	public long getApplicationVersion() {
		return APP_VERSION;
	}

	@Override
	public long getRequiredApplicationVersion() {
		return CURRENTLY_REQ_APP_VERSION;
	}

	@Override
	public String getApplicationName() {
		return L.getLabel(L.APPLICATION_TITLE);
	}

	@Override
	public int getApplicationId() {
		return APP_ID;
	}

	@Override
	public Locale getDefaultLocale() {
		Locale defaultLocale = exampleSettings.getDefaultLocale();
		return defaultLocale;
	}

	@Override
	public void setDefaultLocale(Locale locale) {
		exampleSettings.setDefaultLocale(locale);
	}

	@Override
	public File getNestingDirectory() {
		return AppFiles.APP_DIR;
	}

	@Override
	public ConnectionFactory createConnectionFactory(Class<? extends Database> dbClass) {
		return SQLiteUtils.connectToFile(AppFiles.DB_FILE);
	}

//	@Override
//	public ConnectionParameters getConnectionParameters(Class<? extends Database> dbClass) {
//		// TODO Auto-generated method stub
//		return new ConnectionParameters(SQLiteUtils.connectToFile(AppFiles.DB_FILE), "", "");
//	}

//	@Override
//	public SQLConnection initializeConnection(Database database) {
//		Credentials credentials = null;
//		try {
//			credentials = Profile.loadCredentials(".");
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//			throw new NullPointerException("Could not initialize credentials");
//		}
//		// @formatter:off
//			return LumiSQLiteConnection.Builder
//					.newBuilder()
//					.connectToFile(AppFiles.DB_FILE)
//					.buildWithChacha20(credentials.getPassword());
////					.build();
//		// @formatter:on
//	}
}
