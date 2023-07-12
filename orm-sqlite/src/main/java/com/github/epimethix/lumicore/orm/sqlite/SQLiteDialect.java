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
package com.github.epimethix.lumicore.orm.sqlite;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.Database.SchemaSynchronizity;
import com.github.epimethix.lumicore.common.orm.EntitySynchronizity;
import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.sql.ConnectionFactory;
import com.github.epimethix.lumicore.common.orm.sql.TableInfo;
import com.github.epimethix.lumicore.common.orm.sqlite.Constraint;
import com.github.epimethix.lumicore.common.orm.sqlite.Definition;
import com.github.epimethix.lumicore.common.orm.sqlite.Voc;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;
import com.github.epimethix.lumicore.orm.ORM;
import com.github.epimethix.lumicore.orm.annotation.database.SchemaSync;
import com.github.epimethix.lumicore.orm.annotation.entity.TableSync;
import com.github.epimethix.lumicore.orm.sql.SQLDialect;
import com.github.epimethix.lumicore.properties.LumicoreProperties;

public final class SQLiteDialect extends SQLDialect {

	private final Logger LOGGER = Log.getLogger("orm-sqlite");
//
//	protected final Database DB;
//
//	private final ConnectionFactory connectionFactory;

	public SQLiteDialect(Database db, ConnectionFactory connectionFactory) {
		super(db, connectionFactory);
	}

	protected final class Rename {
		private final String oldName;
		private final String newName;

		public Rename(String oldName, String newName) {
			this.oldName = oldName;
			this.newName = newName;
		}
	}

	@Override
	public final void autoSyncSchema() throws SQLException, ConfigurationException {
		if (!DB.isDeployed()) {
			DB.deploy();
		} else if (DB.getDatabaseApplication().getApplicationVersion() < DB.getMetaRequiredAppVersion()) {
			/*
			 * This is a redundant check for security reasons. AbstractDB should actually
			 * throw a ConfigurationException during its constructor call when instantiating
			 * the implementing class to prevent this condition.
			 * 
			 * This check is done to be "100%" sure that an out-dated version of the
			 * application cannot mess up the schema during auto-synchronization.
			 * 
			 * This check is done to enforce backwards incompatibility in case an out-dated
			 * application version tries to auto-synchronize the schema.
			 */
			throw new ConfigurationException(ConfigurationException.OUT_DATED_SOFTWARE_IN_USE,
					DB.getDatabaseApplication().getApplicationVersion(), DB.getMetaRequiredAppVersion());
		} else {
			/**
			 * Get the global configuration parameters
			 */
			boolean autoDeployNewTables;
			boolean globAutoDeployNewColumns;
			boolean autoDropTables;
//			boolean globAutoDropColumns;
//			boolean upgradeSchema;
			boolean globAutoRedefineEntity;

			SchemaSync asd = ORM.getDatabaseAutoSyncConfig(DB.getClass());

			if (Objects.nonNull(asd)) {
				autoDeployNewTables = asd.deployNewTables();
				globAutoDeployNewColumns = asd.deployNewColumns();
				autoDropTables = asd.dropTables();
//				globAutoDropColumns = asd.dropColumns();
//				upgradeSchema = asd.upgradeSchema();
				globAutoRedefineEntity = asd.redefineEntity();
			} else {
				autoDeployNewTables = SchemaSync.DEPLOY_NEW_TABLES_DEFAULT;
				globAutoDeployNewColumns = SchemaSync.DEPLOY_NEW_COLUMNS_DEFAULT;
				autoDropTables = SchemaSync.DROP_TABLES_DEFAULT;
//				globAutoDropColumns = AutoSyncDatabase.DROP_COLUMNS_DEFAULT;
//				upgradeSchema = AutoSyncDatabase.UPGRADE_SCHEMA_DEFAULT;
				globAutoRedefineEntity = SchemaSync.REDEFINE_ENTITY_DEFAULT;
			}
			/**
			 * check schema for new, existing and non existing (deletion candidates) table
			 * names
			 */
			SchemaSynchronizity ss = DB.checkSchemaSynchronizity();

			long dbFileStructureVersion = DB.getMetaDatabaseStructureVersion();
			long dbJavaStructureVersion = ORM.getDatabaseStructureVersion(DB.getClass());
			/**
			 * Check / Create Segment
			 */
//			if (upgradeSchema) {
			if (dbFileStructureVersion < dbJavaStructureVersion) {
				DB.beforeUpgrade(dbFileStructureVersion);
			}
//			}
			for (Repository<?, ?> repository : DB.getRepositories()) {
				if (ss.tablesToCheck.contains(repository.getEntityName())) {
//						if (upgradeSchema) {
					DB.upgradeEntity(repository);
//						}
					boolean autoDeployNewColumns;
//					boolean autodropColumns;
//						boolean upgradeEntity;
					boolean autoRedefineEntity;
					TableSync ase = ORM.getEntityAutoSyncConfig(repository.getEntityClass());
					if (Objects.nonNull(ase)) {
						autoDeployNewColumns = ase.deployNewColumns();
//						autodropColumns = ase.dropColumns();
//							upgradeEntity = ase.upgradeEntity();
						autoRedefineEntity = ase.redefineEntity();
					} else {
						autoDeployNewColumns = globAutoDeployNewColumns;
//						autodropColumns = globAutoDropColumns;
//							upgradeEntity = globUpgradeEntity;
						autoRedefineEntity = globAutoRedefineEntity;
					}
					/**
					 * Auto sync table
					 */
					Optional<SQLiteMaster> mOpt = sqliteMasterByName(repository.getEntityName());
					if (mOpt.isPresent()) {
						SQLiteMaster m = mOpt.get();
						boolean needsRedefinition = false;
						{
//							if (Objects.isNull(m)) {
//								System.out.println("h");
//							}
							String masterDef = m.getSql().substring(m.getSql().indexOf("(")).trim();
							String javaDef = repository.getCreateStatement()
									.substring(repository.getCreateStatement().indexOf("(")).trim();
							if (masterDef.equals(javaDef)) {
								// System.out.println("~~~ CONTINUE (" + ar.getEntityName() + ")");
								// System.out.println("Master: " + masterDef);
								// System.out.println("Java : " + javaDef);
								continue;
//								} else {
								// System.out.println("~~~ OUT OF SYNC? (" + ar.getEntityName() + ")");
								// System.out.println("Master: " + masterDef);
								// System.out.println("Java : " + javaDef);
							}
							String withoutRowId = Voc.WITHOUT_ROWID;
							masterDef = masterDef.toUpperCase();
							javaDef = javaDef.toUpperCase();
							if ((masterDef.endsWith(withoutRowId) && !javaDef.endsWith(withoutRowId))
									|| (!masterDef.endsWith(withoutRowId) && javaDef.endsWith(withoutRowId))) {
								needsRedefinition = true;
							}

						}
						EntitySynchronizity es = repository
								.checkEntitySynchronizity(tableInfo(repository.getEntityName()));
						if (!needsRedefinition) {
							Map<String, Definition> masterDefinitions = Definition.parseDefinitions(m.getSql());
							for (String fCheck : es.getFieldsToCheck()) {
								Definition dDefinition = (Definition) repository.getDefinition(fCheck);
								Definition dMaster = masterDefinitions.get(fCheck);
								if (!dDefinition.sqlEquals(dMaster)) {
									needsRedefinition = true;
									break;
								}
							}
						}
						if (!needsRedefinition) {
							System.err.println(m.getSql());
							List<Constraint> masterConstraints = Constraint.parseConstraints(m.getSql());
							List<Constraint> definitionConstraints = Constraint
									.parseConstraints(repository.getCreateStatement());
							if (masterConstraints.size() != definitionConstraints.size()) {
								needsRedefinition = true;
							} else if (masterConstraints.size() > 0) {
								for (Constraint c : definitionConstraints) {
									boolean found = false;
									for (Constraint c1 : masterConstraints) {
										if (c1.equals(c)) {
											found = true;
											break;
										}
									}
									if (!found) {
										needsRedefinition = true;
										break;
									}
								}
							}
						}
						if (!needsRedefinition) {
//						if (!es.getFieldsToDelete().isEmpty() && autodropColumns) {
							if (!es.getFieldsToDelete().isEmpty()) {
								needsRedefinition = true;
							}
						}
						if (!needsRedefinition) {
							if (autoDeployNewColumns) {
								for (String fCreate : es.getFieldsToCreate()) {
									alterTableAddColumn(repository.getEntityName(),
											(Definition) repository.getDefinition(fCreate));
									LOGGER.info("Added column: %s.%s", repository.getEntityName(), fCreate);
								}
							}
						}
						if (needsRedefinition && autoRedefineEntity) {
							redefineEntity(repository, es);
						}
					} else if (ss.tablesToCreate.contains(repository.getEntityName())) {
						if (autoDeployNewTables) {
							repository.create();
						}
					}
				}
				/**
				 * Delete Segment
				 */
				if (autoDropTables) {
					for (String delete : ss.tablesToDelete) {
						if (dropTable(delete)) {
							LOGGER.info("TABLE DROPPED: %s", delete);
						} else {
							LOGGER.info("Table was NOT dropped: %s", delete);
						}
					}
				}
//			if (upgradeSchema) {
				if (dbFileStructureVersion < dbJavaStructureVersion) {
					DB.afterUpgrade(dbFileStructureVersion);
					DB.setMetaDatabaseStructureVersion(dbJavaStructureVersion);
					LOGGER.info("database structure version was set to V%d", dbJavaStructureVersion);
				}
//			}
				long dbRequiredAppVersion = DB.getMetaRequiredAppVersion();
				if (dbRequiredAppVersion < DB.getDatabaseApplication().getRequiredApplicationVersion()) {
					DB.setMetaRequiredAppVersion(DB.getDatabaseApplication().getRequiredApplicationVersion());
					LOGGER.info("required application version V%d was set",
							DB.getDatabaseApplication().getRequiredApplicationVersion());
				}
			}
		}
	}

	/**
	 * Renames a table
	 * 
	 * @param tableName    the current table name.
	 * @param newTableName the new table name.
	 * @throws SQLException
	 */
	protected final void alterTableRenameTo(String tableName, String newTableName) throws SQLException {
		alterTableRenameTo(tableName, newTableName, LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
	}

	/**
	 * Renames a table
	 * 
	 * @param tableName       the current table name.
	 * @param newTableName    the new table name.
	 * @param closeConnection true to close the database connection
	 * @throws SQLException
	 */
	protected final void alterTableRenameTo(String tableName, String newTableName, boolean closeConnection)
			throws SQLException {
		String sql = AlterTable.sqlAlterTableRenameTo(tableName, newTableName);
		DB.executeUpdate(sql, closeConnection);
	}

	/**
	 * Adds a column to a table
	 * 
	 * @param tableName  the table name
	 * @param definition the field definition to add
	 * @throws SQLException
	 */
	protected final void alterTableAddColumn(String tableName, Definition definition) throws SQLException {
		alterTableAddColumn(tableName, definition, LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
	}

	/**
	 * Adds a column to a table
	 * 
	 * @param tableName       the table name
	 * @param definition      the field definition to add
	 * @param closeConnection true to close the database connection
	 * @throws SQLException
	 */
	protected final void alterTableAddColumn(String tableName, Definition definition, boolean closeConnection)
			throws SQLException {
		String sql = AlterTable.sqlAlterTableAddColumn(tableName, definition);
		DB.executeUpdate(sql, closeConnection);
	}

	protected void redefineEntity(Repository<?, ?> repository, EntitySynchronizity synchronizity) throws SQLException {
		redefineEntity(repository, synchronizity, null);
	}

	protected void redefineEntity(Repository<?, ?> repository, List<Rename> renames) throws SQLException {
		redefineEntity(repository, repository.checkEntitySynchronizity(tableInfo(repository.getEntityName())), renames);
	}

	protected void redefineEntity(Repository<?, ?> repository, EntitySynchronizity synchronizity, List<Rename> renames)
			throws SQLException {
		boolean toggleForeignKeys = isPragmaForeignKeysEnabled();
		String tableName = repository.getEntityName();
		String tableNameOld = tableName.concat("Old");
		try (Connection connection = DB.getConnection()) {
			if (toggleForeignKeys) {
				DB.executeUpdate(Pragma.SQL_DISABLE_FOREIGN_KEYS, false);
			}
			DB.executeUpdate(Pragma.SQL_ENABLE_LEGACY_ALTER_TABLE, false);
//			executeUpdate("BEGIN TRANSACTION", false);
			boolean toggleAutoCommit = connection.getAutoCommit();
			if (toggleAutoCommit) {
				connection.setAutoCommit(false);
			}
			try {
				// 1) rename old
				DB.executeUpdate(AlterTable.sqlAlterTableRenameTo(tableName, tableNameOld), false);
				// 2) deploy new (current) table
				DB.executeUpdate(repository.getCreateStatement(), false);
				// 3) consider renamed fields
				String[] newNames, oldNames;
				String[] ftc = synchronizity.getFieldsToCheck().toArray(new String[0]);
				if (Objects.nonNull(renames) && renames.size() > 0) {
					newNames = new String[ftc.length + renames.size()];
					oldNames = new String[newNames.length];
					for (int i = 0; i < newNames.length; i++) {
						if (i < ftc.length) {
							newNames[i] = oldNames[i] = ftc[i];
						} else {
							// TODO Test/Debug renaming fields
							Rename r = renames.get(i - ftc.length);
							newNames[i] = r.newName;
							oldNames[i] = r.oldName;
						}
					}
				} else {
					newNames = oldNames = ftc;
				}
				// 4) copy data to new table
				SQLiteBuilder b = new SQLiteBuilder();
				String sql = b.insertInto(tableName, newNames).selectFrom(tableNameOld, oldNames).getSQL();
				DB.executeUpdate(sql, false);
				// 5) drop old table
				dropTableWithoutWhiteListing(tableNameOld, false);
				connection.commit();
				LOGGER.info("Table '%s' was redefined", tableName);
			} catch (SQLException e) {
				LOGGER.error(e, "redefine entity '%s' failed!", tableName);
				connection.rollback();
				throw e;
			} finally {
				if (toggleAutoCommit) {
					connection.setAutoCommit(true);
				}
			}
		} finally {
			if (toggleForeignKeys) {
				DB.executeUpdate(Pragma.SQL_ENABLE_FOREIGN_KEYS, false);
			}
			DB.executeUpdate(Pragma.SQL_DISABLE_LEGACY_ALTER_TABLE);
		}
	}

	/**
	 * <b>Warning!!!</b>
	 * <p>
	 * <b>The white-listing protection of {@link Database#dropTable(String)} is
	 * bypassed by this method and does not apply!</b>
	 * <p>
	 * <b>This method actually tries to delete any specified table without checking
	 * any further!</b>
	 * 
	 * @param tableName the table to delete
	 * @throws SQLException
	 */
	protected void dropTableWithoutWhiteListing(String tableName, boolean closeConnection) throws SQLException {
		DB.executeUpdate(String.format("DROP TABLE %s", tableName), closeConnection);
	}

//	@Override
	public boolean dropTable(String tableName) throws SQLException {
		String[] dropTableWhiteList = DB.getDropTableWhiteList();
		if (Objects.nonNull(dropTableWhiteList)) {
			for (String deletableTable : dropTableWhiteList) {
				if (deletableTable.equals(tableName)) {
//					dropTableWithoutWhiteListing(tableName);
					DB.executeUpdate(String.format("DROP TABLE %s", tableName));
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Lists all non-system database tables (tablenames not starting with "sqlite_"
	 * 
	 * @return the list of all existing non-system database tables
	 * @throws SQLException
	 */
	@Override
	public final List<String> listDatabaseTableNames() throws SQLException {
		List<String> databaseTables = new ArrayList<>();
		Connection c = DB.getConnection();
		LOGGER.trace(Master.SQL_MASTER_SELECT_NAMES_OF_NON_SYSTEM_TABLES.replaceAll("[%]", "%%"));
		try (Statement st = c.createStatement();
				ResultSet rs = st.executeQuery(Master.SQL_MASTER_SELECT_NAMES_OF_NON_SYSTEM_TABLES)) {
			while (rs.next()) {
				databaseTables.add(rs.getString(1));
			}
		} finally {
			DB.checkClose(LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
		}
		return databaseTables;
	}

	/**
	 * Get the PRAGMA application id
	 * 
	 * @return the PRAGMA application id
	 */
	@Override
	public final int getApplicationId() {
		return (int) DB.getInteger(Pragma.SQL_GET_APPLICATION_ID);
	}

	/**
	 * Set the PRAGMA application id
	 */
	@Override
	public final void setApplicationId(int applicationId) {
		if (applicationId != 0) {
			try {
				DB.executeUpdate(Pragma.setApplicationId(applicationId));
			} catch (SQLException e) {
				LOGGER.error(e);
			}
		}
	}

	/**
	 * Get the PRAGMA Schema Version
	 * 
	 * @return the PRAGMA Schema Version
	 */
	public final long getPragmaSchemaVersion() {
		return DB.getInteger(Pragma.SQL_GET_SCHEMA_VERSION);
	}

	/**
	 * Get the PRAGMA User Version
	 * 
	 * @return the PRAGMA User Version
	 */
	public final long getPragmaUserVersion() {
		return DB.getInteger(Pragma.SQL_GET_USER_VERSION);
	}

	/**
	 * Set the PRAGMA User Version
	 */
	public final void setPragmaUserVersion(long version) {
		try {
			DB.executeUpdate(Pragma.setUserVersion(version));
		} catch (SQLException e) {
			LOGGER.error(e);
		}
	}

	/**
	 * Increment the PRAGMA User Version
	 */
	public final long incrementPragmaUserVersion() {
		long version = getPragmaUserVersion();
		setPragmaUserVersion(++version);
		return version;
	}

	/**
	 * Checks if PRAGMA FOREIGN KEYS is enabled.
	 * 
	 * @return true if PRAGMA FOREIGN KEYS is enabled.
	 */
	public final boolean isPragmaForeignKeysEnabled() {
		long i = DB.getInteger(Pragma.SQL_GET_FOREIGN_KEYS);
		return i != 0;
	}

	/**
	 * Enable or disable PRAGMA FOREIGN KEYS
	 * 
	 * @param enabled true to enable
	 */
	public final void setPragmaForeignKeysEnabled(boolean enabled) {
		try {
			if (enabled) {
				DB.executeUpdate(Pragma.SQL_ENABLE_FOREIGN_KEYS);
			} else {
				DB.executeUpdate(Pragma.SQL_DISABLE_FOREIGN_KEYS);
			}
		} catch (SQLException e) {
			LOGGER.error(e);
		}
	}

//	@Override
	public final List<TableInfo> tableInfo(String table) {
		List<TableInfo> l = new ArrayList<>();
		try {
			Connection c = DB.getConnection();
			String sql = Pragma.setTableInfo(table);
			LOGGER.trace(sql);
			try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
				while (rs.next()) {
					l.add(new TableInfo(rs));
				}
			} finally {
				DB.checkClose(LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
			}
		} catch (SQLException e) {
			LOGGER.error(e);
		}
		LOGGER.trace("Querying table info of '%s' returned %d results", table, l.size());
		return l;
	}

//	@Override
	public final List<SQLiteMaster> sqliteMaster() {
		List<SQLiteMaster> l = new ArrayList<>();
		Connection c;
		try {
			c = DB.getConnection();
			String sql = Master.SQL_MASTER_SELECT_ALL;
			LOGGER.trace(sql);
			try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
				while (rs.next()) {
					l.add(new SQLiteMaster(rs));
				}
			} finally {
				DB.checkClose(LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
			}
		} catch (SQLException e) {
			LOGGER.error(e);
		}
		LOGGER.trace("Master table query returned %d results", l.size());
		return l;
	}

	/**
	 * Gets the {@link SQLiteMaster} entry of the specified table.
	 * 
	 * @param tableName the table to get the master record of
	 * @return the {@link SQLiteMaster} entry of the specified table.
	 */
	public final Optional<SQLiteMaster> sqliteMasterByName(String tableName) {
		Connection c;
		try {
			c = DB.getConnection();
			String sql = Master.SQL_MASTER_SELECT_BY_NAME;
			LOGGER.trace(sql);
			try (PreparedStatement ps = c.prepareStatement(sql)) {
				ps.setString(1, tableName);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
//						LOGGER.trace("Query master table for table name '%s' was successful", tableName);
						return Optional.of(new SQLiteMaster(rs));
					} else {
						LOGGER.warn("Query master table for table name '%s' was NOT successful", tableName);
					}
				}
			} finally {
				DB.checkClose(LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
			}
		} catch (SQLException e) {
			LOGGER.error(e);
		}
		return Optional.empty();
	}

	@Override
	public int resolveType(Class<?> mappedType) {
		return SQLiteUtils.resolveType(mappedType);
	}

	@Override
	public Definition getDefinition(Field f) {
		return null;
	}

	@Override
	public boolean isMappableType(Class<?> type) {
		return SQLiteUtils.isMappableType(type);
	}

	@Override
	public int getReferencingType(Class<? extends Entity<?>> referencedEntity, String referencedFieldName) {
		return SQLiteUtils.getReferencingType(referencedEntity, referencedFieldName);
	}

	@Override
	public int autoDetectType(Class<?> mappingType) {
		return SQLiteUtils.autoDetectType(mappingType);
	}

	@Override
	public int resolveType(Class<?> mappedType, String referencedFieldName) {
		return SQLiteUtils.resolveType(mappedType, referencedFieldName);
	}

	@Override
	public int getSQLType(int lumicoreType) {
		return SQLiteUtils.getSQLType(lumicoreType);
	}
}
