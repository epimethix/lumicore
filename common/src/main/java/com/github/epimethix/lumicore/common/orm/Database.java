/*
 * Copyright 2021-2023 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.common.orm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.sqlite.SQLiteConfig;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.DatabaseApplication;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.query.QueryBuilderFactory;
import com.github.epimethix.lumicore.common.orm.sql.Dialect;

/**
 * The interface Database represents a database object that primarily manages
 * connection settings and connection.
 * <p>
 * But it also prepares for database version tracking, user management and
 * retrieving SQLite specific database properties.
 * 
 * @author epimethix
 *
 */
public interface Database {

	/**
	 * Container for returning the state of synchronizity between java definition
	 * and actual database file contents (table level).
	 */
	public final static class SchemaSynchronizity {
		/**
		 * the list of tables that exist both in java definition and database file.
		 * <p>
		 * these tables must be further compared for definition identity
		 */
		public final List<String> tablesToCheck = new ArrayList<>();
		/**
		 * The list of tables that are defined in java but could not be found in the
		 * database file
		 */
		public final List<String> tablesToCreate = new ArrayList<>();
		/**
		 * The list of tables that were found in the database file but are not defined
		 * in the application
		 */
		public final List<String> tablesToDelete = new ArrayList<>();
	}
//	/**
//	 * this field can be hidden in the implementation to declare the database
//	 * structure version for upgrade triggering.
//	 */
	public static long STRUCTURE_VERSION = 0L;

//	default long getDatabaseStructureVersion() {
//		return 0L;
//	}

//	/**
//	 * This field should be hidden to enable table deletion by default.
//	 * <p>
//	 * The table names found in this String[] will be deleteable and auto-deletable
//	 * 
//	 * @see #dropTable(String)
//	 */
//	public static String[] DROP_TABLE_WHITE_LIST = null;

	default String[] getDropTableWhiteList() {
		return null;
	}

	/**
	 * Get a/the database connection
	 * 
	 * @return a/the database connection
	 * 
	 * @throws SQLException
	 */
	Connection getConnection() throws SQLException;

	/**
	 * Close the current connection
	 * 
	 * @throws SQLException
	 */
	void closeConnection() throws SQLException;

	/**
	 * Test the database configuration
	 * 
	 * @return true if a connection could be established
	 * 
	 * @throws SQLException
	 */
	boolean isConnectionWorking() throws SQLException;

	/**
	 * Deploy the database in case it does not exist yet
	 * 
	 * @throws SQLException
	 * @throws ConfigurationException
	 */
	void deploy() throws SQLException, ConfigurationException;

	/**
	 * this method is executed at the end of the deployment process
	 * 
	 * @throws SQLException
	 * @throws InterruptedException
	 * 
	 * @see Database#deploy()
	 */
	void postCreateAction() throws SQLException, InterruptedException;

	/**
	 * drops the given table if its name is found in
	 * {@link Database#getDropTableWhiteList()}.
	 * 
	 * @param tableName the table to drop
	 * 
	 * @throws SQLException
	 */
	boolean dropTable(String tableName) throws SQLException;

	/**
	 * Do work before the structure upgrade is executed
	 * 
	 * @param fileStructureVersion the current file structure version
	 */
	void beforeUpgrade(long fileStructureVersion);

	/**
	 * database level upgrades. upgrades from file (old) version to current program
	 * definition version.
	 * 
	 * @param fileStructureVersion the (old) database structure version as tracked
	 *                             in the database metadata or zero if it was not
	 *                             tracked yet.
	 */
	void afterUpgrade(long fileStructureVersion);

	/**
	 * Gets the active user name or null if no user is set/logged in
	 * 
	 * @return the active user name or null
	 */
	String getActiveUser();

	/**
	 * sets the active user name
	 * 
	 * @param activeUser the user name to set as logged in
	 */
	void setActiveUser(String activeUser);

//	/**
//	 * Set the/a new encryption key
//	 * 
//	 * @param key the encryption key
//	 * @throws SQLException 
//	 */
//	void setKey(String key) throws SQLException;

//	/**
//	 * Set the/a new encryption key
//	 * 
//	 * @param key the encryption key
//	 * @throws SQLException
//	 */
//	void setKey(String key, SQLiteConfig config) throws SQLException;

	/**
	 * generates a UUID.
	 * <p>
	 * By default UUID.randomUUID().toString() is returned.
	 * 
	 * @return a random UUID
	 */
	default String nextUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * get the table info for the specified table
	 * 
	 * @param tableName the entities table name
	 * 
	 * @return a {@code List<TableInfo>} holding a TableInfo for each field
	 */
//	List<TableInfo> tableInfo(String tableName);

	/**
	 * lists all entries of the sqlite_master table
	 * 
	 * @return a list of all entries of the sqlite_master table
	 */
//	List<SQLiteMaster> sqliteMaster();

	/**
	 * Gets a managed repository by its entity type
	 * 
	 * @param entityClass The managed entity
	 * @return the repository
	 */
	Repository<?, ?> getRepository(Class<? extends Entity<?>> entityClass);

	/**
	 * Seeks the {@code ManyToManyRepository} that manages the specified
	 * {@code ManyToManyEntity}.
	 * 
	 * @param manyToManyEntity the managed {@code ManyToManyEntity}
	 * @return the {@code ManyToManyRepository} that manages the specified
	 *         {@code ManyToManyEntity} or {@code null} if no such repository could
	 *         be found
	 * @throws IllegalArgumentException if the specified manyToManyEntity is not
	 *                                  assignable to {@code ManyToManyEntity}
	 */
	ManyToManyRepository<?, ?, ?> getManyToManyRepository(Class<? extends Entity<?>> manyToManyEntity);

	/**
	 * Seeks the {@link ManyToManyRepository} belonging to the specified entity
	 * class.
	 * 
	 * @param entityClassA the {@code ManyToManyEntity} of the repository to seek or
	 *                     class A
	 * @param entityClassB this field is nullable in case entityClassA is the
	 *                     managed {@code ManyToManyEntity}. If entityClassA is
	 *                     actually classA then this field should be filled with the
	 *                     according classB.
	 * @return The {@code ManyToManyRepository} for the specified classes or
	 *         {@code null} if no such repository could be found
	 */
	ManyToManyRepository<?, ?, ?> getManyToManyRepository(Class<? extends Entity<?>> entityClassA,
			Class<? extends Entity<?>> entityClassB);

	/**
	 * Gets the {@code DatabaseApplication} that manages this {@code Database}.
	 * 
	 * @return the {@code DatabaseApplication}
	 */
	DatabaseApplication getDatabaseApplication();

	/**
	 * Auto-synchronizes the complete database schema with the java database
	 * definition.
	 * 
	 * @throws SQLException
	 * @throws ConfigurationException
	 */
	void autoSyncSchema() throws SQLException, ConfigurationException;

	/**
	 * Registers a repository to be managed
	 * 
	 * @param repository the repository to register
	 * @throws ConfigurationException if the repository name is already registered
	 */
	void registerRepository(Repository<?, ?> repository) throws ConfigurationException;

	long next(String sequenceName);

	long nextNegative(String sequenceName);

	/**
	 * Gets the path to the database file.
	 * 
	 * @return the path to the database file
	 */
	String getPath();

	String getSchemaName();

	QueryBuilderFactory getQueryBuilderFactory();

	boolean isDeployed();

	long getMetaRequiredAppVersion();

	long getMetaDatabaseStructureVersion();

	SchemaSynchronizity checkSchemaSynchronizity() throws SQLException;

	List<Repository<?, ?>> getRepositories();

	void upgradeEntity(Repository<?, ?> repository) throws SQLException;

	void setMetaDatabaseStructureVersion(long dbJavaStructureVersion);

	void setMetaRequiredAppVersion(long requiredApplicationVersion);

	Dialect getDialect();

	int getApplicationId();

	void setApplicationId(int applicationId);

	/**
	 * Closes the connection if wanted
	 * 
	 * @param closeConnection true to close the connection
	 * @throws SQLException
	 */
	void checkClose(boolean closeConnection) throws SQLException;

	void executeUpdate(String sql, boolean closeConnection) throws SQLException;

	void executeUpdate(String sqlDisableLegacyAlterTable) throws SQLException;

	long getInteger(String sqlGetApplicationId);

	Class<? extends Entity<?>> getEntityImplementationClass(Class<? extends Entity<?>> e);
}
