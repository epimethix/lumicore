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
package com.github.epimethix.lumicore.common.orm.sql;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import javax.swing.SwingUtilities;

import com.github.epimethix.lumicore.common.CryptoDatabaseApplication;
import com.github.epimethix.lumicore.common.ui.CryptoUI.Credentials;

public class ConnectionFactory {

	private final String connectionURL;

	private final transient Properties properties;

	public ConnectionFactory(String connectionURL, Properties properties) {
		this.connectionURL = connectionURL;
		this.properties = properties;
	}

	public ConnectionFactory(String connectionString, String user, String password) {
		this.connectionURL = connectionString;
		properties = new Properties();
		properties.put("user", user);
		properties.put("password", password);
	}

	public String getConnectionString() {
		return connectionURL;
	}

	public Connection createConnection() throws SQLException {
		return DriverManager.getConnection(connectionURL, properties);
	}

	public void testConnection() throws SQLException {
		Connection c = createConnection();
		if (Objects.isNull(c)) {
			throw new SQLException("Connection Test Failed!");
		} else {
			c.getMetaData().getTables(null, null, "%", new String[] { "TABLE" });
			c.close();
		}
	}

	public boolean isDeployed() {
		File databaseFile = new File(connectionURL.substring(connectionURL.lastIndexOf(":") + 1));
		if (Objects.nonNull(databaseFile)) {
			return databaseFile.exists() && databaseFile.length() > 0;
		}
		return false;
	}

	public void setPassword(String password) {
		properties.put("password", password);
	}

	public void setUser(String user) {
		if (Objects.nonNull(user)) {
			properties.put("user", user);

		}

	}

	public ConnectionFactory userAuthenticate(Properties cfg, CryptoDatabaseApplication application) {
		RunUnlockDB ru = new RunUnlockDB(application);
		try {
			SwingUtilities.invokeAndWait(ru);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return this;
	}

	private class RunUnlockDB implements Runnable {
//		private final Properties cfg;
//		private Connection connection;
		private final CryptoDatabaseApplication application;

		public RunUnlockDB(CryptoDatabaseApplication application) {
//			this.cfg = cfg;
			this.application = application;
		}

		@Override
		public void run() {
			while (true) {
//				File dbFile = Builder.this.databaseFile;
				Optional<Credentials> secretOpt;
				if (!isDeployed()) {
//				pwConfirm = new JPasswordField();
					secretOpt = application.getCryptoUI().setupSecret();
				} else {
					secretOpt = application.getCryptoUI().getSecret();
				}
				if (secretOpt.isPresent()) {
					Credentials cred = secretOpt.get();
					/*
					 * ...flopsec...
					 */
					char[] secretArray = cred.getPassword();
					ConnectionFactory.this.setPassword(new String(secretArray));
					Arrays.fill(secretArray, (char) 0);
					ConnectionFactory.this.setUser(cred.getUser());
//					LumiSQLiteConnection connection = build(cfg);
					try {
						testConnection();
						break;
					} catch (SQLException e) {
						continue;
					}
				} else {
					break;
				}
			}
			try {
				testConnection();
			} catch (SQLException e) {
				System.err.println("Could not obtain password, exiting...");
				System.exit(-10);
			}
		}
	}
}
