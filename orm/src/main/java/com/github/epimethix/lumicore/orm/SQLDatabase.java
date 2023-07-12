/*
 * Copyright 2021-2022 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.orm;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.DatabaseApplication;
import com.github.epimethix.lumicore.common.Reflect;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.ManyToManyRepository;
import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.model.ManyToManyEntity;
import com.github.epimethix.lumicore.common.orm.query.QueryBuilderFactory;
import com.github.epimethix.lumicore.common.orm.sql.ConnectionController;
import com.github.epimethix.lumicore.common.orm.sql.ConnectionFactory;
import com.github.epimethix.lumicore.common.orm.sql.Dialect;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;
import com.github.epimethix.lumicore.orm.annotation.database.SchemaSync;
import com.github.epimethix.lumicore.orm.annotation.entity.ImplementationClass;
import com.github.epimethix.lumicore.orm.annotation.entity.TableSync;
import com.github.epimethix.lumicore.orm.model.AbstractManyToManyEntity;
import com.github.epimethix.lumicore.orm.model.Meta;
import com.github.epimethix.lumicore.properties.LumicoreProperties;

/**
 * AbstractDB implements the following tasks:
 * <ul>
 * <li>Connection management
 * <li>(Abstract)Repository management
 * <li>Database Metadata initialization, management and accessibility (table
 * meta_db / Model entity "Meta")
 * <ul>
 * <li>give the database an UUID on initial deployment
 * <li>track the database structure version
 * <li>track the entity structure versions
 * <li>provide tracking option for the required application version to implement
 * backwards incompatibility
 * <li>provide tracking option for database access/read/write time-stamps
 * <li>provide methods to save/read custom metadata parameters
 * </ul>
 * <li>Master table access
 * <li>Pragma access
 * <li>Alter table implementation, redefine table (niy)
 * <li>schema upgrade and/or synchronization
 * <li>utility methods for common r/w tasks
 * <li>script execution
 * <li>getActiveUser() and setActiveUser(String)
 * </ul>
 * 
 * When implementing AbstractDB follow this pattern:
 * 
 * <pre>
 * public class MyDB extends AbstractDB {
 * 	private final MyRepository myRepository;
 * 
 * 	public MyDB(File dbFile) throws ConfigurationException, SQLException {
 * 		super(dbFile);
 * 		myRepository = new MyRepository(this);
 * 		registerRepository(myRepository);
 * 
 * 		...
 * 
 * 		autoSyncSchema();
 * 
 * 		...or
 * 
 * 		upgradeSchema();
 * 	}
 * 
 * }
 * </pre>
 * 
 * This way ConfigurationException will only be thrown when instantiating the
 * database instance including all its repositories. When there is no
 * {@link ConfigurationException} thrown the database should be ready for
 * operation.
 * 
 * @author epimethix
 * 
 * @see ConfigurationException
 * @see SQLDatabase#autoSyncSchema()
 * @see SchemaSync
 * @see TableSync
 *
 */
public abstract class SQLDatabase implements Database {

	protected final static Logger LOGGER = Log.getLogger(Log.CHANNEL_ORM);

//	private final static String CONNECTION_ENCRYPTED_FORMAT = "jdbc:sqlite:file:%s";

	// @formatter:off
	// @formatter:on

//
//	protected final class Rename {
//		private final String oldName;
//		private final String newName;
//
//		public Rename(String oldName, String newName) {
//			this.oldName = oldName;
//			this.newName = newName;
//		}
//	}

	/**
	 * The metadata key "DB_ID"
	 */
	public final static String META_DB_ID = "DB_ID";
	/**
	 * The metadata key "STRUCTURE_VERSION"
	 */
	public static final String META_STRUCTURE_VERSION = "LUMICORE_STRUCTURE_VERSION";
	/**
	 * The metadata key "REQUIRED_APP_VERSION"
	 */
	public static final String META_REQUIRED_APP_VERSION = "LUMICORE_REQUIRED_APP_VERSION";
	/**
	 * The metadata key "CREATE_DATE"
	 */
	public final static String META_CREATE_DATE = "LUMICORE_CREATE_DATE";
	/**
	 * The metadata key "EDIT_DATE"
	 */
	public final static String META_EDIT_DATE = "LUMICORE_EDIT_DATE";
	/**
	 * The metadata key "ACCESS_DATE"
	 */
	public final static String META_ACCESS_DATE = "LUMICORE_ACCESS_DATE";
	/**
	 * The metadata key format "STRUCTURE_VERSION_%s"
	 */
	private final static String META_ENTITY_STRUCTURE_VERSION_FORMAT = META_STRUCTURE_VERSION.concat("_%s");
	/**
	 * The drop table statement format "DROP TABLE `%s`"
	 */
//	private final static String SQL_DROP_TABLE_FORMAT = SQLiteBuilder.newBuilder().dropTable("%s").getSQL();
	private static final String SEQUENCE_FORMAT = "SEQUENCE_%s";
	private static final String SEQUENCE_NEGATIVE_FORMAT = "SEQUENCE_NEGATIVE_%s";
	/*
	 * Object
	 */
	/**
	 * the SQLiteConnection
	 */
	protected final transient ConnectionController connectionController;
	/**
	 * the current connection or null if a connection must be opened
	 */
//	private transient Connection c;
	/**
	 * The databases metadata store
	 */
	private final MetaRepository metaRepository;
	/**
	 * The list of repositories to manage in creation order
	 */
	protected final List<Repository<?, ?>> repositories;
	/**
	 * For easy access this Map is synchronized to the list of repositories to
	 * manage
	 */
	private final Map<String, Repository<?, ?>> mappedRepositories;
	/**
	 * The database file
	 */
//	private final File databaseFile;
	/**
	 * The active user, null by default
	 */
	private String activeUser;
	/**
	 * the database application for version checking
	 */
	protected final DatabaseApplication databaseApplication;

	private final transient Dialect dialect;

	/**
	 * this method should be overridden when it is necessary to implement custom
	 * tasks that have to be done after initial deployment of the database
	 * <p>
	 * this method is called after {@link SQLDatabase#deploy()} has finished its
	 * work.
	 * <p>
	 * note that {@link SQLDatabase#autoSyncSchema()} calls
	 * {@link SQLDatabase#deploy()} if the database file does not exist yet.
	 * <p>
	 * by default the method is blank
	 * 
	 * @throws SQLException
	 */
	@Override
	public void postCreateAction() throws SQLException {};

	@Override
	public void beforeUpgrade(long fileStructureVersion) {}

	/**
	 * this method should be overridden when it is necessary to implement custom
	 * tasks when the database structure has changed.
	 * <p>
	 * upgrade is called by {@link SQLDatabase#autoSyncSchema()}
	 * <p>
	 * the upgrade is triggered by hiding the {@link Database#STRUCTURE_VERSION} in
	 * the implementing Database class and defining a number greater than zero or by
	 * setting the hiding field to a higher version number.
	 * 
	 * @see Database#STRUCTURE_VERSION
	 */
	@Override
	public void afterUpgrade(long fileStructureVersion) {}

	public SQLDatabase(DatabaseApplication databaseApplication) throws ConfigurationException {
		this.databaseApplication = databaseApplication;
		this.dialect = DialectFactory.createDialect(this, databaseApplication.createConnectionFactory(getClass()));
		this.connectionController = dialect;
		if (Objects.isNull(this.connectionController) || !this.connectionController.isConnectionWorking()) {
			throw new ConfigurationException(ConfigurationException.COULD_NOT_ESTABLISH_CONNECTION,
					getClass().getSimpleName());
		}
		repositories = new ArrayList<Repository<?, ?>>();
		mappedRepositories = new HashMap<String, Repository<?, ?>>();
		metaRepository = new MetaRepository(this);
		registerRepository(metaRepository);
		checkIntegrity();
	}

	public SQLDatabase(DatabaseApplication databaseApplication, ConnectionFactory connectionFactory)
			throws ConfigurationException {
		this.databaseApplication = databaseApplication;
		this.dialect = DialectFactory.createDialect(this, connectionFactory);
		this.connectionController = dialect;
		if (Objects.isNull(this.connectionController) || !this.connectionController.isConnectionWorking()) {
			throw new ConfigurationException(ConfigurationException.COULD_NOT_ESTABLISH_CONNECTION,
					getClass().getSimpleName());
		}
		repositories = new ArrayList<Repository<?, ?>>();
		mappedRepositories = new HashMap<String, Repository<?, ?>>();
		metaRepository = new MetaRepository(this);
		registerRepository(metaRepository);
		checkIntegrity();
	}

	private void checkIntegrity() throws ConfigurationException {
		if (connectionController.isDeployed()) {
			if (databaseApplication.getApplicationId() != 0
					&& databaseApplication.getApplicationId() != getApplicationId()) {
				throw new ConfigurationException(ConfigurationException.WRONG_APPLICATION_ID,
						databaseApplication.getApplicationId(), getApplicationId());
			}
			long runningAppVersion = databaseApplication.getApplicationVersion();
			long requiredAppVersion = getMetaRequiredAppVersion();
			if (runningAppVersion < requiredAppVersion) {
				throw new ConfigurationException(ConfigurationException.OUT_DATED_SOFTWARE_IN_USE, runningAppVersion,
						requiredAppVersion);
			}
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					SQLDatabase.this.connectionController.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}

//	/**
//	 * Override this method to configure the database connection.
//	 * <p>
//	 * The default configuration is:
//	 * <p>
//	 * 
//	 * <pre>
//	 * SQLiteConfig config = new SQLiteConfig();
//	 * config.enforceForeignKeys(true);
//	 * config.setTempStore(TempStore.MEMORY);
//	 * </pre>
//	 * 
//	 * @return the database connection configuration.
//	 */
//	protected SQLiteConfig sqliteConfig() {
//		SQLiteConfig config = new SQLiteConfig();
//		config.enforceForeignKeys(true);
//		config.setTempStore(TempStore.MEMORY);
//		return config;
//	}

//	/**
//	 * Gets the database configuration.
//	 * 
//	 * @return the database configuration {@code Properties}
//	 */
//	protected final Properties getDatabaseConfig() {
//		return dbConfig;
//	}

	@Override
	public DatabaseApplication getDatabaseApplication() {
		return this.databaseApplication;
	}

	@Override
	public QueryBuilderFactory getQueryBuilderFactory() {
		return dialect;
	}

	/**
	 * adds a repository to be managed
	 * 
	 * @param repository the repository to be managed
	 */
	private void addRepository(Repository<?, ?> repository) {
		repositories.add(repository);
		mappedRepositories.put(repository.getEntityName(), repository);
	}

	@Override
	public final void registerRepository(Repository<?, ?> repository) throws ConfigurationException {
		if (Objects.nonNull(mappedRepositories.get(repository.getEntityName()))) {
			throw new ConfigurationException(ConfigurationException.ENTITY_NAME_COLLISION, repository.getEntityName());
		}
		addRepository(repository);
		LogRepository<?, ?> logRepository = (LogRepository<?, ?>) repository.getLogRepository();
		if (Objects.nonNull(logRepository)) {
			addRepository(logRepository);
		}
	}

	@Override
	public Repository<?, ?> getRepository(Class<? extends Entity<?>> entityClass) {
		return getRepository(Entity.getEntityName(entityClass));
	}

	public Repository<?, ?> getRepository(String entityName) {
		return mappedRepositories.get(entityName);
	}

	@Override
	public ManyToManyRepository<?, ?, ?> getManyToManyRepository(Class<? extends Entity<?>> manyToManyEntity) {
		if (!ManyToManyEntity.class.isAssignableFrom(manyToManyEntity)) {
			throw new IllegalArgumentException("getManyToManyRepository(Class<? extends Entity<?>>) "
					+ "the Entity parameter must be assignable to ManyToManyEntity but it is not!");
		}
		return getManyToManyRepository(manyToManyEntity, null);
	}

	@Override
	public ManyToManyRepository<?, ?, ?> getManyToManyRepository(Class<? extends Entity<?>> entityClassA,
			Class<? extends Entity<?>> entityClassB) {
		int MTM_ENTITY = 1;
		int MTM_SUB_ENTITY = 2;
		int search;
		Objects.requireNonNull(entityClassA);
		if (AbstractManyToManyEntity.class.isAssignableFrom(entityClassA)) {
			search = MTM_ENTITY;
		} else {
			Objects.requireNonNull(entityClassB);
			search = MTM_SUB_ENTITY;
		}
		for (String key : mappedRepositories.keySet()) {
			Repository<?, ?> mappedRepository = mappedRepositories.get(key);
			if (ManyToManyRepository.class.isAssignableFrom(mappedRepository.getClass())) {
				if (search == MTM_ENTITY) {
					if (Reflect.typeEquals(mappedRepository.getEntityClass(), entityClassA)) {
						return (ManyToManyRepository<?, ?, ?>) mappedRepository;
					}
				} else {
					ManyToManyRepository<?, ?, ?> mtmRepo = (ManyToManyRepository<?, ?, ?>) mappedRepository;
					if ((Reflect.typeEquals(mtmRepo.getClassA(), entityClassA)
							&& Reflect.typeEquals(mtmRepo.getClassB(), entityClassB))
							|| (Reflect.typeEquals(mtmRepo.getClassB(), entityClassA)
									&& Reflect.typeEquals(mtmRepo.getClassA(), entityClassB))) {
						return mtmRepo;
					}
				}
			}
		}
		return null;
	}

	/*
	 * Connection management
	 */

	@Override
	public String getPath() {
		return connectionController.getPath();
	}

	@Override
	public final Connection getConnection() throws SQLException {
		return connectionController.getConnection();
	}

	@Override
	public boolean isConnectionWorking() {
//		try {
//			getPragmaSchemaVersion();
//		} catch (SQLException e) {
//			return false;
//		}
//		return true;
		return connectionController.isConnectionWorking();
	}

	@Override
	public void checkClose(boolean closeConnection) throws SQLException {
		connectionController.checkClose(closeConnection);
	}

	@Override
	public final void closeConnection() throws SQLException {
		connectionController.close();
	}

//	@Override
//	public void setKey(String key) throws SQLException {
////		connection.setKeyChacha20(key);
//	}
//
//	@Override
//	public void setKey(String key, SQLiteConfig config) throws SQLException {
//		connectionController.setKey(key, config.toProperties());
//	}

	/*
	 * Deploy database
	 */

//	/**
//	 * By annotating the implementing database class with DatabaseDefinition the
//	 * metadata initialization can be configured.
//	 * <p>
//	 * The following metadata records are created:
//	 * <p>
//	 * Set {@link DatabaseMetadataConfiguration#initializeTimeStamps()} to true to:
//	 * <p>
//	 * <ul>
//	 * <li>set {@link AbstractDB#META_CREATE_DATE},
//	 * <li>{@link AbstractDB#META_ACCESS_DATE} and
//	 * <li>{@link AbstractDB#META_EDIT_DATE} to {@link System#currentTimeMillis()}
//	 * </ul>
//	 * <p>
//	 * Set {@link DatabaseMetadataConfiguration#unique()} to true to:
//	 * <p>
//	 * <ul>
//	 * <li>set {@link AbstractDB#META_DB_ID} to {@link DB#nextUUID()}
//	 * </ul>
//	 * <p>
//	 * By default:
//	 * <p>
//	 * <ul>
//	 * <li>{@link AbstractDB#META_REQUIRED_APP_VERSION} is set to
//	 * {@link AbstractDatabaseApplication#getCurrentlyRequiredApplicationVersion()}
//	 * </ul>
//	 * this method is called by {@link DB#deploy()} when initially deploying the
//	 * database. this call happens before calling {@link DB#postCreateAction()}.
//	 * 
//	 * @throws SQLException
//	 * @throws InterruptedException
//	 * 
//	 * @see DatabaseMetadataConfiguration
//	 */
//	private final void initializeDatabaseMetadata() throws SQLException, InterruptedException {
//		if (Reflect.isDatabaseTimestamped(getClass())) {
//			String now = String.valueOf(System.currentTimeMillis());
//			metaRepository.save(META_CREATE_DATE, now, false);
//			metaRepository.save(META_ACCESS_DATE, now, false);
//			metaRepository.save(META_EDIT_DATE, now, false);
//		}
//		if (Reflect.isDatabaseUnique(getClass())) {
//			metaRepository.save(META_DB_ID, nextUUID(), false);
//		}
//		metaRepository.save(META_REQUIRED_APP_VERSION,
//				String.valueOf(databaseApplication.getCurrentlyRequiredApplicationVersion()), false);
//		setPragmaApplicationId(databaseApplication.getApplicationId());
//	}

	/**
	 * deploys a new entity in the schema
	 * <p>
	 * to complete any further tasks after the initial deployment of the table the
	 * method {@link Repository#postCreateAction()} can be implemented/overridden.
	 * 
	 * @param r the {@link Repository} instance managing the entity access.
	 * 
	 * @throws SQLException
	 * 
	 * @see {@link Repository#postCreateAction()}
	 */
	private final void deploy(Repository<?, ?> r) throws SQLException {
		if (Objects.isNull(r)) {
			throw new IllegalArgumentException("The repository to deploy was null!");
		}
//		executeUpdate(r.getCreateStatement(), false);
		r.create();
		setMetaEntityStructureVersion(r.getEntityName(), r.getEntityStructureVersion());
		r.postCreateAction();
		LOGGER.info("Table of Entity '%s' was deployed as V%d", r.getEntityName(), r.getEntityStructureVersion());
	}

	@Override
	public final void deploy() throws SQLException, ConfigurationException {
		try {
			for (Repository<?, ?> r : repositories) {
				deploy(r);
			}
			setMetaDatabaseStructureVersion(ORM.getDatabaseStructureVersion(getClass()));
			setMetaRequiredAppVersion(databaseApplication.getRequiredApplicationVersion());
//			setPragmaApplicationId(databaseApplication.getApplicationId());
//			initializeDatabaseMetadata();
			if (ORM.isDatabaseTimestamped(getClass())) {
				String now = String.valueOf(System.currentTimeMillis());
				metaRepository.save(META_CREATE_DATE, now);
				metaRepository.save(META_ACCESS_DATE, now);
				metaRepository.save(META_EDIT_DATE, now);
			}
			if (ORM.isDatabaseUnique(getClass())) {
				metaRepository.save(META_DB_ID, nextUUID());
			}
//			metaRepository.save(META_REQUIRED_APP_VERSION,
//					String.valueOf(databaseApplication.getCurrentlyRequiredApplicationVersion()), false);
			setApplicationId(databaseApplication.getApplicationId());
			postCreateAction();
			LOGGER.info("Database '%s' was deployed", getClass().getSimpleName());
		} finally {
			checkClose(LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
		}
	}

	/*
	 * Schema synchronization
	 */

	/**
	 * Lists all table names that are defined in Java
	 * 
	 * @return a list of all Java-defined table names
	 */
	private final List<String> listDefinedTableNames() {
		List<String> definedTables = new ArrayList<>();
		for (Repository<?, ?> r : repositories) {
			definedTables.add(r.getEntityName());
		}
		return definedTables;
	}

	/**
	 * Checks the synchronizity of the schema in the file system and the Java
	 * definition
	 * 
	 * @return the tables to check, deploy and delete
	 * @throws SQLException
	 */
//	private final SchemaSynchronizity checkSchemaSynchronizity() throws SQLException {
	public final SchemaSynchronizity checkSchemaSynchronizity() throws SQLException {
		List<String> existingTables = dialect.listDatabaseTableNames();
		List<String> definedTables = listDefinedTableNames();
		SchemaSynchronizity ss = new SchemaSynchronizity();

		for (String defined : definedTables) {
			if (existingTables.contains(defined)) {
				ss.tablesToCheck.add(defined);
			} else {
				ss.tablesToCreate.add(defined);
			}
//			for (String existing : existingTables) {
//				if (defined.equals(existing)) {
//					ss.tablesToCheck.add(defined);
//					continue checkDefined;
//				}
//			}
//			ss.tablesToCreate.add(defined);
		}

		for (String existing : existingTables) {
			if (definedTables.contains(existing)) {
				continue;
			}
//			for (String defined : definedTables) {
//				if (existing.equals(defined)) {
//					continue checkExisting;
//				}
//			}
			ss.tablesToDelete.add(existing);
		}
		return ss;
	}

	/**
	 * calls {@link Repository#upgrade(long)} on the specified repository if the
	 * current entity structure version in file is less than the entity structure
	 * version in the java definition.
	 * 
	 * upgrades the entity structure version after calling
	 * {@link Repository#upgrade(long)}.
	 * 
	 * @param repository the repository to upgrade if necessary.
	 * @throws SQLException
	 */
	@Override
	public final void upgradeEntity(Repository<?, ?> repository) throws SQLException {
		String entityName = repository.getEntityName();
		long repoFileStructureVersion = getMetaEntityStructureVersion(entityName);
		long repoJavaStructureVersion = repository.getEntityStructureVersion();
		if (repoFileStructureVersion < repoJavaStructureVersion) {
			repository.upgrade(repoFileStructureVersion);
			setMetaEntityStructureVersion(entityName, repoJavaStructureVersion);
			LOGGER.info("Entity '%s' was upgraded from V%d to V%d", repository.getEntityName(),
					repoFileStructureVersion, repoJavaStructureVersion);
		}
	}

//	/**
//	 * calls {@link DB#afterUpgrade(long)} if necessary
//	 */
//	protected final void upgradeDatabase() {
//		long dbFileStructureVersion = getMetaDatabaseStructureVersion();
//		long dbJavaStructureVersion = Reflect.getDatabaseStructureVersion(getClass());
//		if (dbFileStructureVersion < dbJavaStructureVersion) {
//			afterUpgrade(dbFileStructureVersion);
//			setMetaDatabaseStructureVersion(dbJavaStructureVersion);
//		}
//		long dbRequiredAppVersion = getMetaRequiredAppVersion();
//		if (dbRequiredAppVersion < databaseApplication.getCurrentlyRequiredApplicationVersion()) {
//			setMetaRequiredAppVersion(databaseApplication.getCurrentlyRequiredApplicationVersion());
//		}
//	}

//	/**
//	 * Upgrades the schema if any structure versions got higher.
//	 * <p>
//	 * <ol>
//	 * <li>{@link AbstractDatabase#upgradeEntity(Repository)} 1...n
//	 * </ol>
//	 * 
//	 * @throws SQLException
//	 */
//	public final void upgradeSchema() throws SQLException {
//		long dbFileStructureVersion = getMetaDatabaseStructureVersion();
//		long dbJavaStructureVersion = Reflect.getDatabaseStructureVersion(getClass());
//		if (dbFileStructureVersion < dbJavaStructureVersion) {
//			beforeUpgrade(getMetaDatabaseStructureVersion());
//		}
//		for (Repository<?, ?> r : repositories) {
//			upgradeEntity(r);
//		}
////		upgradeDatabase();
//		if (dbFileStructureVersion < dbJavaStructureVersion) {
//			afterUpgrade(dbFileStructureVersion);
//			setMetaDatabaseStructureVersion(dbJavaStructureVersion);
//		}
//		long dbRequiredAppVersion = getMetaRequiredAppVersion();
//		if (dbRequiredAppVersion < databaseApplication.getCurrentlyRequiredApplicationVersion()) {
//			setMetaRequiredAppVersion(databaseApplication.getCurrentlyRequiredApplicationVersion());
//		}
//	}

	/**
	 * executes an update statement
	 * 
	 * @param sqlUpdate the sql update statement
	 * @throws SQLException
	 */
	public final void executeUpdate(String sqlUpdate) throws SQLException {
		executeUpdate(sqlUpdate, LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
	}

	/**
	 * executes an update statement
	 * 
	 * @param sqlUpdate       the sql update statement
	 * @param closeConnection true to close the database connection
	 * @throws SQLException
	 */
	public final void executeUpdate(String sqlUpdate, boolean closeConnection) throws SQLException {
//		try {
		Connection c = getConnection();
		try (Statement st = c.createStatement()) {
			st.executeUpdate(sqlUpdate);
			LOGGER.trace("update '%s' was executed", sqlUpdate);
		} finally {
			try {
				checkClose(closeConnection);
			} catch (SQLException e) {
				LOGGER.error(e);
			}
		}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * gets the INTEGER value from the first field of first record of the specified
	 * query result.
	 * 
	 * @param sqlQuery the sql query
	 * @return the long value taken from the specified query
	 */
	public final long getInteger(String sqlQuery) {
		try {
			Connection c = getConnection();
			try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sqlQuery)) {
				if (rs.next()) {
					LOGGER.trace("Integer query '%s' executed", sqlQuery);
					return rs.getLong(1);
				}
			} finally {
				checkClose(LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
			}
		} catch (SQLException e) {
			LOGGER.error(e);
		}
		return 0L;
	}

	/*
	 * Meta methods
	 */

	/**
	 * Saves a value to the meta table overwriting any possibly existing value with
	 * the specified key.
	 * 
	 * @param key   the key to store the value under.
	 * @param value the value to be stored
	 * @return the saved meta entry or null if an Exception was thrown
	 */
	protected final Meta saveMeta(String key, String value) {
		return saveMeta(key, value, false);
	}

	/**
	 * Saves a value to the meta table overwriting any possibly existing value with
	 * the specified key.
	 * 
	 * @param key   the key to store the value under.
	 * @param value the value to be stored
	 * @return the saved meta entry or null if an Exception was thrown
	 */
	private final Meta saveMeta(String key, String value, boolean internal) {
		if (!internal) {
			if (key.toUpperCase().startsWith("LUMICORE_")) {
				throw new IllegalArgumentException(
						"illegal meta field name prefix! Do not prefix meta field names with \"LUMICORE_\".");
			}
		}
		try {
			Meta m = metaRepository.save(key, value);
			if (Objects.nonNull(m)) {
				LOGGER.info("Meta value was stored to '%s'", key);
			} else {
				LOGGER.critical("Storing Meta value to '%s' failed!", key);
			}
			return m;
		} catch (SQLException e) {
			LOGGER.error(e);
//		} catch (InterruptedException e) {
//			LOGGER.error(e);
		}
		return null;
	}

	/**
	 * Saves an INTEGER value to the meta table replacing any previously stored
	 * value with that key.
	 * 
	 * @param key   the key to store the value under
	 * @param value the value to store
	 */
	protected final void saveIntegerMeta(String key, long value) {
		saveIntegerMeta(key, value, false);
	}

	private final void saveIntegerMeta(String key, long value, boolean internal) {
		saveMeta(key, String.valueOf(value), internal);
	}

	/**
	 * saves the current time in millis under the specified key.
	 * 
	 * @param key the key to store the timestamp under
	 */
	protected final void timestampMeta(String key) {
		timestampMeta(key, false);
	}

	private final void timestampMeta(String key, boolean internal) {
		saveIntegerMeta(key, System.currentTimeMillis(), internal);
	}

	@Override
	public long next(String sequenceName) {
		synchronized (this) {
			String key = String.format(SEQUENCE_FORMAT, sequenceName);
			long sequenceId = getIntegerMeta(key);
			if (sequenceId == 0L) {
				sequenceId = 1L;
			}
			saveIntegerMeta(key, sequenceId + 1L, true);
			return sequenceId;
		}
	}

	@Override
	public long nextNegative(String sequenceName) {
		synchronized (this) {
			String key = String.format(SEQUENCE_NEGATIVE_FORMAT, sequenceName);
			long sequenceId = getIntegerMeta(key);
			if (sequenceId == 0L) {
				sequenceId = -1L;
			}
			saveIntegerMeta(key, sequenceId - 1L, true);
			return sequenceId;
		}
	}

	/**
	 * Gets a value from the meta table
	 * 
	 * @param key the key of the value
	 * @return the specified value if found or null otherwise
	 */
	protected final String getMeta(String key) {
		Meta m = null;
		try {
			m = metaRepository.selectById(key).orElse(null);
		} catch (SQLException e) {
			LOGGER.error(e);
//		} catch (InterruptedException e) {
//			LOGGER.error(e);
		}
		if (Objects.isNull(m)) {
			LOGGER.warn("%s::getMeta(%s) failed!", getClass().getSimpleName(), key);
			return null;
		}
		LOGGER.trace("Meta value of '%s' was loaded", key);
		return m.getValue();
	}

	/**
	 * Gets an INTEGER value from the meta table.
	 * 
	 * @param key the key of the INTEGER value
	 * 
	 * @return the INTEGER value stored with the specified key or 0L if the key was
	 *         not found.
	 */
	protected final long getIntegerMeta(String key) {
		String x = getMeta(key);
		long l = 0L;
		boolean wasSet = false;
		if (Objects.nonNull(x)) {
			try {
				l = Long.parseLong(x);
				wasSet = true;
			} catch (NumberFormatException e) {
				LOGGER.error(e);
			}
		}
		if (wasSet) {
			LOGGER.info("%s::getIntegerMeta(%s) was successful", getClass().getSimpleName(), key);
		} else {
			LOGGER.warn("%s::getIntegerMeta(%s) failed, returning default value!", getClass().getSimpleName(), key);
		}
		return l;
	}

	/**
	 * Gets the databases ID or null if there is none.
	 * 
	 * @return the database ID or null if the entry does not exist.
	 */
	public final String getMetaDatabaseID() {
		return getMeta(META_DB_ID);
	}

	/**
	 * Gets the database create date.
	 * 
	 * @return the database create date or 0L if the entry does not exist.
	 */
	public final long getMetaDatabaseCreateDate() {
		return getIntegerMeta(META_CREATE_DATE);
	}

	/**
	 * Timestamps the database meta entry "edit date"
	 */
	protected final void stampMetaDatabaseEditDate() {
		timestampMeta(META_EDIT_DATE, true);
	}

	/**
	 * Gets the meta entry "edit date"
	 * 
	 * @return the database "edit date" or 0L if the entry does not exist.
	 */
	public final long getMetaDatabaseEditDate() {
		return getIntegerMeta(META_EDIT_DATE);
	}

	/**
	 * Timestamps the database meta entry "access date"
	 */
	protected final void stampMetaDatabaseAccessDate() {
		timestampMeta(META_ACCESS_DATE, true);
	}

	/**
	 * Gets the meta entry "access date"
	 * 
	 * @return the database "access date" or 0L if the entry does not exist.
	 */
	public final long getMetaDatabaseAccessDate() {
		return getIntegerMeta(META_ACCESS_DATE);
	}

	/**
	 * Sets the database meta entry "required app version"
	 * 
	 * @param version the required application version to use the database file.
	 */
	public final void setMetaRequiredAppVersion(long version) {
		saveIntegerMeta(META_REQUIRED_APP_VERSION, version, true);
	}

	/**
	 * Gets the meta entry "required application version"
	 * 
	 * @return the required application version to open the database file or 0L if
	 *         the entry does not exist.
	 */
	public final long getMetaRequiredAppVersion() {
		return getIntegerMeta(META_REQUIRED_APP_VERSION);
	}

	/**
	 * sets the database structure version.
	 * 
	 * @param version the new database structure version
	 */
	public final void setMetaDatabaseStructureVersion(long version) {
		saveIntegerMeta(META_STRUCTURE_VERSION, version, true);
	}

	/**
	 * Gets the current database structure version
	 * 
	 * @return the database structure version
	 */
	public final long getMetaDatabaseStructureVersion() {
		return getIntegerMeta(META_STRUCTURE_VERSION);
	}

	/**
	 * Gets an entities structure version.
	 * 
	 * @param entityName the entity name to get the entity structure version of.
	 */
	public final long getMetaEntityStructureVersion(String entityName) {
		return getIntegerMeta(String.format(META_ENTITY_STRUCTURE_VERSION_FORMAT, entityName));
	}

	/**
	 * Sets the structure version meta entry for the specified entity.
	 * 
	 * @param entityName       the entity name
	 * @param structureVersion the new structure version
	 * @throws SQLException
	 */
	void setMetaEntityStructureVersion(String entityName, long structureVersion) {
		saveIntegerMeta(String.format(META_ENTITY_STRUCTURE_VERSION_FORMAT, entityName), structureVersion, true);
	}

	/*
	 * Execute scripts
	 */

	/**
	 * executes the SQLite script that is accessible by the argument pathToScript.
	 * <p>
	 * if auto commit is enabled, executeScript(String[]) will disable it for the
	 * execution of the given update statements. auto commit will be re-enabled
	 * after execution even if an Exception is thrown.
	 * <p>
	 * At the end of execution the connection will be closed.
	 * 
	 * @param pathToScript the file system path string to the script.
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	public void executeScript(String pathToScript) throws IOException, SQLException {
		executeScript(new File(pathToScript));
	}

	/**
	 * executes the SQLite script that is accessible by the argument pathToScript.
	 * <p>
	 * if auto commit is enabled, executeScript(String[]) will disable it for the
	 * execution of the given update statements. auto commit will be re-enabled
	 * after execution even if an Exception is thrown.
	 * 
	 * @param pathToScript    the file system path string to the script.
	 * @param closeConnection true if the connection should be closed at the end of
	 *                        execution.
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	public void executeScript(String pathToScript, boolean closeConnection) throws IOException, SQLException {
		executeScript(new File(pathToScript), closeConnection);
	}

	/**
	 * executes the SQLite script that is accessible by the argument script.
	 * <p>
	 * if auto commit is enabled, executeScript(String[]) will disable it for the
	 * execution of the given update statements. auto commit will be re-enabled
	 * after execution even if a SQLException is thrown.
	 * <p>
	 * At the end of execution the connection will be closed.
	 * 
	 * @param script the File representing the script.
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	public void executeScript(File script) throws IOException, SQLException {
		executeScript(JDBCUtils.readScript(script));
	}

	/**
	 * executes the SQLite script that is accessible by the argument script.
	 * <p>
	 * if auto commit is enabled, executeScript(String[]) will disable it for the
	 * execution of the given update statements. auto commit will be re-enabled
	 * after execution even if a SQLException is thrown.
	 * 
	 * @param script          the File representing the script.
	 * @param closeConnection true if the connection should be closed at the end of
	 *                        execution.
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	public void executeScript(File script, boolean closeConnection) throws IOException, SQLException {
		executeScript(JDBCUtils.readScript(script), closeConnection);
	}

	/**
	 * executes a SQLite script
	 * <p>
	 * if auto commit is enabled, executeScript(String[]) will disable it for the
	 * execution of the given update statements. auto commit will be re-enabled
	 * after execution even if a SQLException is thrown.
	 * <p>
	 * At the end of execution the connection will be closed.
	 * 
	 * @param script the update statements
	 * 
	 * @throws SQLException
	 */
	public void executeScript(String[] script) throws SQLException {
		executeScript(script, LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
	}

	/**
	 * executes a SQLite script
	 * <p>
	 * if auto commit is enabled, executeScript(String[]) will disable it for the
	 * execution of the given update statements. auto commit will be re-enabled
	 * after execution even if a SQLException is thrown.
	 * 
	 * @param script          the update statements
	 * @param closeConnection true if the connection should be closed at the end of
	 *                        execution.
	 * 
	 * @throws SQLException
	 */
	public void executeScript(String[] script, boolean closeConnection) throws SQLException {
		try {
			Connection c = getConnection();
			boolean isAutoCommit = c.getAutoCommit();
			try {
				if (isAutoCommit) {
					c.setAutoCommit(false);
				}
				for (String statement : script) {
					executeUpdate(statement, false);
				}
				c.commit();
				LOGGER.info("script of %d commands executed successfully!", script.length);
			} finally {
				if (isAutoCommit) {
					c.setAutoCommit(true);
				}
			}
		} finally {
			checkClose(closeConnection);
		}
	}

	/*
	 * User management
	 */

	@Override
	public String getActiveUser() {
		return activeUser;
	}

	@Override
	public void setActiveUser(String activeUser) {
		this.activeUser = activeUser;
	}

	@Override
	public String getSchemaName() {
		// TODO Implement getSchemaName
		return "main";
	}

	@Override
	public boolean dropTable(String tableName) throws SQLException {
		if (Arrays.asList(getDropTableWhiteList()).contains(tableName)) {
			executeUpdate(String.format("DROP TABLE %s", dialect.quoteIdentifier(tableName)));
		}
		return false;
	}

	@Override
	public void autoSyncSchema() throws SQLException, ConfigurationException {
		dialect.autoSyncSchema();
	}

	@Override
	public boolean isDeployed() {
		return connectionController.isDeployed();
	}

	@Override
	public List<Repository<?, ?>> getRepositories() {
		return new ArrayList<>(repositories);
	}

	@Override
	public Dialect getDialect() {
		return dialect;
	}

	@Override
	public int getApplicationId() {
		return dialect.getApplicationId();
	}

	@Override
	public void setApplicationId(int applicationId) {
		dialect.setApplicationId(applicationId);
	}

	@Override
	public Class<? extends Entity<?>> getEntityImplementationClass(Class<? extends Entity<?>> e) {
		Class<? extends Entity<?>> x = null;
		if (e.isAnnotationPresent(ImplementationClass.class)) {
			return (Class<? extends Entity<?>>) e.getAnnotation(ImplementationClass.class).value();
		}
		throw new RuntimeException("LazyEntity interface needs @ImplementationClass(Class<?>) annotation!");
	}
}
