/*
 * Copyright 2021 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.orm.sqlite;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.TempStore;
import org.sqlite.mc.SQLiteMCChacha20Config;
import org.sqlite.mc.SQLiteMCConfig;
import org.sqlite.mc.SQLiteMCRC4Config;
import org.sqlite.mc.SQLiteMCSqlCipherConfig;
import org.sqlite.mc.SQLiteMCWxAES128Config;
import org.sqlite.mc.SQLiteMCWxAES256Config;

import com.github.epimethix.lumicore.common.CryptoDatabaseApplication;
import com.github.epimethix.lumicore.common.orm.SQLConnection;
import com.github.epimethix.lumicore.properties.LumicoreProperties;

/**
 * Builder to create a {@link LumiSQLiteConnection}.
 * 
 * @author epimethix
 * 
 * @see Builder#build()
 * @see Builder#build(SQLiteConfig)
 * @see Builder#buildWithChacha20(String)
 *
 */
@Deprecated
public class LumiSQLiteConnection implements SQLConnection {

	/**
	 * Connection management object
	 * 
	 * @author epimethix
	 *
	 */
//	public static class SQLiteConnection {
	public static final int LOCAL_FILE = 1;
	public static final int MEMORY = 2;
	public static final int RESOURCE = 3;
	public static final int RESOURCE_JAR = 4;

	private boolean isEncrypted;
	private String connectionString;
	private Properties properties;
	private SQLiteConfig configuration;
	private Connection c;
	private File databaseFile;
//	private final QueryBuilderFactory queryBuilderFactory;

	private LumiSQLiteConnection(File databaseFile, String connectionString, SQLiteConfig cfg) {
		this.databaseFile = databaseFile;
		this.connectionString = connectionString;
		this.configuration = cfg;
		this.properties = cfg.toProperties();
		isEncrypted = cfg instanceof SQLiteMCConfig;
//		queryBuilderFactory = new SQLiteQueryBuilderFactory();
	}

//	private boolean init;

	@Override
	public Connection getConnection() throws SQLException {
		if (Objects.isNull(c) || c.isClosed()) {
			c = DriverManager.getConnection(connectionString, properties);
//			if (!init) {
//				init = true;
//				DatabaseMetaData md = c.getMetaData();
//				System.err.println(md.getCatalogTerm());
//				System.err.println(md.getSchemaTerm());
//				System.err.println(md.getDatabaseProductName());
//				System.err.println(md.getDriverName());
//				System.err.println(md.getCatalogSeparator());
//				System.err.println(md.getIdentifierQuoteString());
//
//				ResultSet rs = md.getSchemas();
//				while (rs.next()) {
//					System.err.println(rs.getString(1) + ", " + rs.getString(2));
//				}
//				rs = md.getTables(null, null, null, new String[] { "TABLE" });
//				while (rs.next()) {
//					String tableName = rs.getString(3);
//					System.err.println("## " + tableName);
//					ResultSet rs2 = md.getColumns(null, null, tableName, null);
//					while (rs2.next()) {
////						Each column description has the following columns:
////
//
//						System.err.print("TABLE_CAT:");
//						System.err.print(rs2.getString(1));
////							TABLE_CAT String => table catalog (may be null)
//						System.err.print("--TABLE_SCHEM:");
//						System.err.print(rs2.getString(2));
////							TABLE_SCHEM String => table schema (may be null)
//						System.err.print("--TABLE_NAME");
//						System.err.print(rs2.getString(3));
////							TABLE_NAME String => table name
//						System.err.print("--COLUMN_NAME:");
//						System.err.print(rs2.getString(4));
////							COLUMN_NAME String => column name
//						System.err.print("--DATA_TYPE:");
//						System.err.print(rs2.getInt(5) + "::");
////							DATA_TYPE int => SQL type from java.sql.Types
//						System.err.print(ORM.getTypeName(rs2.getInt(5)));
//						System.err.print("--TYPE_NAME:");
//						System.err.print(rs2.getString(6));
////							TYPE_NAME String => Data source dependent type name, for a UDT the type name is fully qualified
//						System.err.print("--COLUMN_SIZE:");
//						System.err.print(rs2.getInt(7));
////							COLUMN_SIZE int => column size.
//						System.err.print("--BUFFER_LENGTH:");
//						System.err.print(rs2.getObject(8));
////							BUFFER_LENGTH is not used.
//						System.err.print("--DECIMAL_DIGITS:");
//						System.err.print(rs2.getInt(9));
////							DECIMAL_DIGITS int => the number of fractional digits. Null is returned for data types where DECIMAL_DIGITS is not applicable.
//						System.err.print("--NUM_PREC_RADIX:");
//						System.err.print(rs2.getInt(10));
////							NUM_PREC_RADIX int => Radix (typically either 10 or 2)
//						System.err.print("--NULLABLE:");
//						System.err.print(rs2.getInt(11));
////							NULLABLE int => is NULL allowed.
////							columnNoNulls - might not allow NULL values
////							columnNullable - definitely allows NULL values
////							columnNullableUnknown - nullability unknown
//						System.err.print("--REMARKS:");
//						System.err.print(rs2.getString(12));
////							REMARKS String => comment describing column (may be null)
//						System.err.print("--COLUMN_DEF:");
//						System.err.print(rs2.getString(13));
////							COLUMN_DEF String => default value for the column, which should be interpreted as a string when the value is enclosed in single quotes (may be null)
//						System.err.print("--SQL_DATA_TYPE:");
//						System.err.print(rs2.getInt(14));
////							SQL_DATA_TYPE int => unused
//						System.err.print("--SQL_DATETIME_SUB:");
//						System.err.print(rs2.getInt(15));
////							SQL_DATETIME_SUB int => unused
//						System.err.print("--CHAR_OCTET_LENGTH:");
//						System.err.print(rs2.getInt(16));
////							CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in the column
//						System.err.print("--ORDINAL_POSITION:");
//						System.err.print(rs2.getInt(17));
////							ORDINAL_POSITION int => index of column in table (starting at 1)
//						System.err.print("--IS_NULLABLE:");
//						System.err.print(rs2.getInt(18));
////							IS_NULLABLE String => ISO rules are used to determine the nullability for a column.
////							YES --- if the column can include NULLs
////							NO --- if the column cannot include NULLs
////							empty string --- if the nullability for the column is unknown
//						System.err.print("--SCOPE_CATALOG:");
//						System.err.print(rs2.getString(19));
////							SCOPE_CATALOG String => catalog of table that is the scope of a reference attribute (null if DATA_TYPE isn't REF)
//						System.err.print("--SCOPE_SCHEMA:");
//						System.err.print(rs2.getString(20));
////							SCOPE_SCHEMA String => schema of table that is the scope of a reference attribute (null if the DATA_TYPE isn't REF)
//						System.err.print("--SCOPE_TABLE:");
//						System.err.print(rs2.getString(21));
////							SCOPE_TABLE String => table name that this the scope of a reference attribute (null if the DATA_TYPE isn't REF)
//						System.err.print("--SOURCE_DATA_TYPE:");
//						System.err.print(rs2.getInt(22));
////							SOURCE_DATA_TYPE short => source type of a distinct type or user-generated Ref type, SQL type from java.sql.Types (null if DATA_TYPE isn't DISTINCT or user-generated REF)
//						System.err.print("--IS_AUTOINCREMENT:");
//						System.err.print(rs2.getString(23));
////							IS_AUTOINCREMENT String => Indicates whether this column is auto incremented
////							YES --- if the column is auto incremented
////							NO --- if the column is not auto incremented
////							empty string --- if it cannot be determined whether the column is auto incremented
//						System.err.print("--IS_GENERATEDCOLUMN:");
//						System.err.print(rs2.getString(24));
////							IS_GENERATEDCOLUMN String => Indicates whether this is a generated column
////							YES --- if this a generated column
////							NO --- if this not a generated column
////							empty string --- if it cannot be determined whether this is a generated column
//						System.err.println();
//					}
//				}
//				rs = md.getImportedKeys(null, null, null);
//			}
		}
//		System.exit(0);
		return c;
	}

	@Override
	public final boolean isConnectionWorking() {
		return isConnectionWorking(LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
	}

	@Override
	public boolean isDeployed() {
		// Objects.nonNull(databaseFile) && databaseFile.exists() &&
		// databaseFile.length() > 0
		if (Objects.nonNull(databaseFile)) {
			return databaseFile.exists() && databaseFile.length() > 0;
		}
		return true;
	}

	public final boolean isConnectionWorking(boolean closeConnection) {
//			if (Objects.nonNull(databaseFile) && !databaseFile.exists()) {
//				return true;
//			}
		try {
			Connection c = getConnection();
			try (Statement st = c.createStatement()) {
				st.execute(Pragma.SQL_GET_SCHEMA_VERSION);
				return true;
			} finally {
				try {
					checkClose(closeConnection);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
//				e.printStackTrace();
		}
		return false;
	}

	@Override
	public void close() throws SQLException {
		if (Objects.nonNull(c)) {
			if (!c.isClosed()) {
				c.close();
			}
			c = null;
		}
	}

	@Override
	public void checkClose(boolean closeConnection) throws SQLException {
		if (closeConnection) {
			close();
		}
	}

	/**
	 * set a (new) encryption key
	 * 
	 * @param key the encryption key to set. null or empty string disables
	 *            encryption.
	 * 
	 * @throws SQLException
	 */
	@Override
	public void setKey(String key, SQLiteConfig config) throws SQLException {
		Builder cb = Builder.newBuilder();
//			cb.Builder.append(currentConnection.connectionString);
		Object p = properties.get("foreign_keys");
		if (Objects.nonNull(p) && p.toString().equals("true")) {
			cb.withForeignKeysEnabled();
		}
		p = properties.get("temp_store");
		if (Objects.nonNull(p) && p.toString().equals("MEMORY")) {
			cb.withTempStoreInMemory();
		}
		p = properties.get("open_mode");
		if (Objects.nonNull(p) && p.toString().equals("65")) {
			cb.withReadOnlyAccess();
		}
		if (config instanceof SQLiteMCConfig) {
			((SQLiteMCConfig) config).withKey(key);
		}
		Connection conn = getConnection();
		try (Statement st = conn.createStatement()) {
			if (Objects.nonNull(key) && !key.isEmpty()) {
				if (isEncrypted) {
					// rekey
					st.executeUpdate(Pragma.setStringPragma(Pragma.PRAGMA_REKEY, key));
					// return cb.buildWithChacha20(key);
//						return cb.build(config);
				} else {
					// key
					st.executeUpdate(Pragma.setStringPragma(Pragma.PRAGMA_KEY, key));
//						return cb.buildWithChacha20(key);
//						return cb.build(config);
				}
			} else if (isEncrypted) {
				// key=''
				st.executeUpdate(Pragma.setStringPragma(Pragma.PRAGMA_REKEY, ""));
//					return cb.build();
//				} else {
			}
//				return cb.build(config);
			LumiSQLiteConnection sc = cb.build(config);
//				this.connectionString = sc.connectionString;
			this.configuration = config;
			this.properties = sc.properties;
			this.isEncrypted = sc.isEncrypted;
//			} catch (SQLException e) {
//				e.printStackTrace();
		} finally {
			checkClose(LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
		}
	}

	@Override
	public String getPath() {
		if (connectionString.contains("?")) {
			return connectionString.substring(0, connectionString.indexOf("?"));
		} else {
			return connectionString;
		}
	}

//	@Override
//	public QueryBuilderFactory getQueryBuilderFactory() {
//		return queryBuilderFactory;
//	}

	public SQLiteConfig getConfiguration() {
		return configuration;
	}

//		public void printConfig() {
//			for (Entry<Object, Object> val : properties.entrySet()) {
//				System.out.printf("%s :: %s%n", val.getKey(), val.getValue().toString());
//			}
//		}
//	}// end of class SQLiteConnection
	public static class Builder {
		/**
		 * The sqlite connection String to connect to a file
		 */
		private final static String CONNECTION_TO_FILE = "jdbc:sqlite:file:";
		/**
		 * The sqlite connection String to connect to memory
		 */
		private final static String CONNECTION_TO_MEMORY = "jdbc:sqlite::memory:";
		/**
		 * The sqlite connection String to connect to a resource
		 */
		private final static String CONNECTION_TO_RESOURCE = "jdbc:sqlite::resource:";
		/**
		 * The sqlite connection String to connect to a jar
		 */
		private final static String CONNECTION_TO_RESOURCE_JAR = "jdbc:sqlite::resource:jar:";
		/**
		 * flag to indicate if backslashes should be sanitized from file paths
		 */
		private final static boolean HEAL_SEPARATOR = FileSystems.getDefault().getSeparator().equals("\\");

		/**
		 * the database file in case of file connection
		 */
		private File databaseFile;
		/**
		 * flag to enable foreign keys
		 */
		private boolean withForeignKeysEnabled;
		/**
		 * flag to set TempStore to memory
		 */
		private boolean withTempStoreInMemory;
		/**
		 * flag to enable read only access
		 */
		private boolean withReadOnlyAccess;

		/**
		 * Creates a new Builder
		 * 
		 * @return a new Builder
		 */
		public static final Builder newBuilder() {
			return new Builder();
		}

		/**
		 * Creates a new Builder and sets the connection string to the specified file.
		 * <p>
		 * On systems with backslash (\) as file path separator all backslashes will be
		 * replaced by forward slashes.
		 * 
		 * @return a new Builder
		 */
		public static Builder newBuilder(File databaseFile) {
			return new Builder().connectToFile(databaseFile);
		}

		private StringBuilder Builder;

		private Builder() {
			Builder = new StringBuilder();
		}

		/**
		 * 
		 * @param check
		 * @return this builder
		 */
		private String healBackSlashes(String check) {
			if (HEAL_SEPARATOR) {// && check.charAt(2) == '\\') {
				return check.replace('\\', '/');
			} else {
				return check;
			}
		}

		/**
		 * 
		 * On systems with backslash (\) as file path separator all backslashes will be
		 * replaced by forward slashes.
		 * 
		 * @param databaseFile
		 * @return this builder
		 */
		public Builder connectToFile(File databaseFile) {
			this.databaseFile = databaseFile;
			if (!databaseFile.getParentFile().exists()) {
				databaseFile.getParentFile().mkdirs();
			}
			Builder.append(CONNECTION_TO_FILE).append(healBackSlashes(databaseFile.getPath()));
			return this;
		}

		/**
		 * 
		 * On systems with backslash (\) as file path separator all backslashes will be
		 * replaced by forward slashes.
		 * 
		 * @param databasePath
		 * @return this builder
		 */
		public Builder connectToFile(String databasePath) {
//		this.databaseFile = new File(databasePath);
//		Builder.append(CONNECTION_TO_FILE).append(healBackSlashes(databasePath));
			return connectToFile(new File(databasePath));
		}

		/**
		 * 
		 * @return this builder
		 */
		public Builder connectToMemory() {
			Builder.append(CONNECTION_TO_MEMORY);
			return this;
		}

		/**
		 * 
		 * @param resourcePath
		 * @return this builder
		 */
		public Builder connectToResource(String resourcePath) {
			Builder.append(CONNECTION_TO_RESOURCE).append(resourcePath);
			return this;
		}

		/**
		 * Connect to a database inside a jar file.
		 * 
		 * @param resourceJarPath the path to the db. example:
		 *                        "/home/x/archive.jar!sample.db"
		 * 
		 * @return this builder
		 */
		public Builder connectToResourceJar(String resourceJarPath) {
			Builder.append(CONNECTION_TO_RESOURCE_JAR).append(resourceJarPath);
			return this;
		}

		/**
		 * enable foreign keys
		 * 
		 * @return this builder
		 */
		public Builder withForeignKeysEnabled() {
			withForeignKeysEnabled = true;
			return this;
		}

		/**
		 * set temp store to memory
		 * 
		 * @return this builder
		 */
		public Builder withTempStoreInMemory() {
			withTempStoreInMemory = true;
			return this;
		}

		/**
		 * Connect using read-only access
		 * 
		 * @return this builder
		 */
		public Builder withReadOnlyAccess() {
			withReadOnlyAccess = true;
			return this;
		}

		/**
		 * read the configuration from the builder functions
		 * 
		 * @param cfg the configuration to put the builder config into
		 */
		private void readConfig(SQLiteConfig cfg) {
			if (withForeignKeysEnabled) {
				cfg.enforceForeignKeys(true);
			}
			if (withTempStoreInMemory) {
				cfg.setTempStore(TempStore.MEMORY);
			}
			if (withReadOnlyAccess) {
				cfg.setReadOnly(true);
			}
		}

		/**
		 * builds the connection without encryption
		 * 
		 * @return the {@link LumiSQLiteConnection}
		 */
		public final LumiSQLiteConnection build() {
			SQLiteConfig cfg = new SQLiteConfig();
			readConfig(cfg);
			return new LumiSQLiteConnection(databaseFile, Builder.toString(), cfg);
		}

		/**
		 * Build the {@link LumiSQLiteConnection} using the specified configuration.
		 * 
		 * @param cfg
		 * @return the builder Result is a {@link LumiSQLiteConnection} object
		 * 
		 * @see SQLiteMCSqlCipherConfig
		 * @see SQLiteMCChacha20Config
		 * @see SQLiteMCWxAES256Config
		 * @see SQLiteMCWxAES128Config
		 * @see SQLiteMCRC4Config
		 */
		public final LumiSQLiteConnection build(SQLiteConfig cfg) {
			readConfig(cfg);
			return new LumiSQLiteConnection(databaseFile, Builder.toString(), cfg);
		}

		/**
		 * Build the {@link LumiSQLiteConnection} using chacha20 encryption with the
		 * specified key
		 * 
		 * @param key the key to use for encryption
		 * 
		 * @return the built SQLiteConnection
		 */
		public final LumiSQLiteConnection buildWithChacha20(String key) {
			SQLiteMCChacha20Config cfg = SQLiteMCChacha20Config.getDefault();
			cfg.withKey(key);
			readConfig(cfg);
			return new LumiSQLiteConnection(databaseFile, Builder.toString(), cfg);
		}

		public LumiSQLiteConnection userAuthenticate(SQLiteConfig cfg, CryptoDatabaseApplication application) {
			if (cfg instanceof SQLiteMCConfig) {
				RunUnlockDB ru = new RunUnlockDB((SQLiteMCConfig) cfg, application);
				try {
					SwingUtilities.invokeAndWait(ru);
					return ru.connection;
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return build(cfg);
		}

		private class RunUnlockDB implements Runnable {
			private final SQLiteMCConfig cfg;
			private LumiSQLiteConnection connection;
			private final CryptoDatabaseApplication application;

			public RunUnlockDB(SQLiteMCConfig cfg, CryptoDatabaseApplication application) {
				this.cfg = cfg;
				this.application = application;
			}

			@Override
			public void run() {
//				while (true) {
//					File dbFile = Builder.this.databaseFile;
//					Optional<char[]> secretOpt;
//					if (Objects.nonNull(dbFile) && (!dbFile.exists() || dbFile.length() == 0)) {
////					pwConfirm = new JPasswordField();
//						secretOpt = application.getCryptoUI().setupSecret();
//					} else {
//						secretOpt = application.getCryptoUI().getSecret();
//					}
//					if (secretOpt.isPresent()) {
//						char[] secretArray = secretOpt.get();
//						cfg.withKey(new String(secretArray));
//						Arrays.fill(secretArray, (char) 0);
//						LumiSQLiteConnection connection = build(cfg);
//						if (connection.isConnectionWorking()) {
//							this.connection = connection;
//							break;
//						} else {
//							continue;
//						}
//					} else {
//						break;
//					}
//				}
//				if (Objects.isNull(this.connection)) {
//					System.err.println("Could not obtain password, exiting...");
//					System.exit(-10);
//				}
			}
		}
	} // End of class Builder
}