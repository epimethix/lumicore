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

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.model.Log;
import com.github.epimethix.lumicore.common.orm.model.TreeEntity;
import com.github.epimethix.lumicore.common.orm.query.Query.CriteriaBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.DeleteBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.DeleteQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.InsertQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.UpdateBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.UpdateQuery;
import com.github.epimethix.lumicore.common.orm.sql.TableInfo;
import com.github.epimethix.lumicore.common.orm.sqlite.Definition;
import com.github.epimethix.lumicore.orm.annotation.field.ManyToMany;
//import com.github.epimethix.lumicore.common.orm.sqlite.SQLiteBuilder;
//import com.github.epimethix.lumicore.common.orm.sqlite.query.SQLiteQuery;
import com.github.epimethix.lumicore.properties.LumicoreProperties;

/**
 * This is the contract a Repository implementation has to fulfill.
 * <p>
 * The Repository class manages a certain {@code Entity<ID>}.
 * 
 * @author epimethix
 *
 * @param <E>  the {@code Entity<ID>} implementation class
 * @param <ID> the entities id class
 * 
 * @see Entity
 */
public interface Repository<E extends Entity<ID>, ID> {

	/*
	 * * * Schema Information
	 */

	/**
	 * To enable (auto)-dropping of columns this field must be hidden and filled
	 * with the column names that are allowed to be deleted.
	 * <p>
	 * <b>Default value is null!</b>
	 */
	public static String[] DROP_COLUMN_WHITE_LIST = null;

	/**
	 * Checks whether the specified column appears in
	 * {@link Repository#DROP_COLUMN_WHITE_LIST}.
	 * 
	 * @param columnName the column deletion candidate
	 * 
	 * @return true if the column name was found in the white-list
	 */
	boolean isColumnDroppable(String columnName);

	/**
	 * Gets the Database object that is used to manage this {@link Repository}
	 * 
	 * @return the Database object
	 */
	Database getDB();

	String getSchemaName();

	/**
	 * Gets the entities name
	 * 
	 * @return the entities name
	 */
	String getEntityName();

	/**
	 * Gets the managed Entity class
	 * 
	 * @return the managed Entity class
	 */
	Class<E> getEntityClass();

	/**
	 * Gets the entities STRUCTURE_VERSION
	 * 
	 * @return the entities STRUCTURE_VERSION
	 * 
	 * @see Entity#STRUCTURE_VERSION
	 */
	long getEntityStructureVersion();

	/**
	 * gets the field {@code Definition} of the field corresponding to the specified
	 * name.
	 * 
	 * @param fieldName the sql field name
	 * 
	 * @return the corresponding {@code Definition}
	 */
	Definition getDefinition(String fieldName);

	/*
	 * * * Deployment and Update
	 */

	/**
	 * Gets the entities create statement
	 * 
	 * @return the entities create statement
	 */
	@Deprecated
	String getCreateStatement();

	/**
	 * the {@link Repository#postCreateAction()} method is called after the entities
	 * table is initially deployed.
	 * 
	 * @throws SQLException
	 */
	void postCreateAction() throws SQLException;

	/**
	 * upgrades the table from its current structural state to the current
	 * definition versions state.
	 * 
	 * @param fileStructureVersion the old STRUCTURE_VERSION which the database is
	 *                             in now.
	 * 
	 * @throws SQLException
	 */
	void upgrade(long fileStructureVersion) throws SQLException;

	/**
	 * checks the entities java definitions synchronizity with the table as it is
	 * defined in the database file.
	 * 
	 * @param existingInDatabase the tableinfo as currently existing in the DB.
	 * 
	 * @return an EntitySynchronizity object containing the existing, the missing
	 *         and the deleted field names.
	 * 
	 * @throws SQLException
	 */
	EntitySynchronizity checkEntitySynchronizity(List<TableInfo> existingInDatabase) throws SQLException;

	/*
	 * * * Utility
	 */

	E getLazyEntityProxy(Object id);

	List<?> getLazyListProxy(Object one, String foreignKeyField);

	List<?> getLazyListProxy(Object one, String foreignKeyField, ManyToMany.Type via);

	/**
	 * initializes a record entity object from a result set at its currently focused
	 * 'line'
	 * 
	 * @param rs the result set
	 * 
	 * @return an object T based on the given result sets current position
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws SQLException
	 */
	public E initializeRecord(ResultSet rs)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, SQLException, Exception;

	public E initializeRecord(ResultSet rs, int[] index) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, SQLException;

	public E initializeRecord(ResultSet rs, int[] index, int self)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException,
			InstantiationException, NoSuchMethodException, SecurityException;

	<T> T newRecord();

	/**
	 * closes the database connection.
	 * 
	 * @return true if the connection was closed
	 * @throws SQLException
	 */
	default boolean close() throws SQLException {
		return checkClose(true);
	}

	/**
	 * checks whether the connection should be closed based on the applications
	 * connection policy.
	 * 
	 * @return true if the connection was closed
	 * @throws SQLException
	 */
	default boolean checkClose() throws SQLException {
		return checkClose(LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
	}

	/**
	 * checks whether the connection should be closed and closes it or not.
	 * 
	 * @param closeConnection true to close the connection, false to do nothing
	 * @return true if the connection was closed
	 * 
	 * @throws SQLException
	 */
	boolean checkClose(boolean closeConnection) throws SQLException;

	/**
	 * Checks two {@code Entity<ID>} items for equality based on their database
	 * field mapped content.
	 * 
	 * @param a the first item
	 * @param b the second item
	 * @return true if the database data of the specified items is equal, false
	 *         otherwise
	 */
	boolean contentEquals(E a, E b);

	/**
	 * copies the database content from the specified E into a new E.
	 * 
	 * @param e the E to duplicate
	 * @return a new E containing the same
	 */
	E copy(E e);

	/*
	 * * * NEW CRUD
	 */
//	List<E> query(Query q) throws SQLException;

	/*
	 * C
	 */
	void create() throws SQLException;

	List<E> insert(List<E> es);

	List<E> insert(InsertQuery q);

	/*
	 * R
	 */
	long getDefaultLimit();

	List<E> selectCriteria(Function<CriteriaBuilder<SelectBuilder>, CriteriaBuilder<SelectBuilder>> b)
			throws SQLException;

	default Optional<E> selectFirst(SelectQuery q) throws SQLException {
		List<E> result = select(q);
		if (result.size() == 0) {
			return Optional.empty();
		} else {
			return Optional.of(result.get(0));
		}
	}

	default Optional<E> selectFirst(Function<SelectBuilder, SelectBuilder> b) throws SQLException {
		List<E> es = select(b2 -> b.apply(b2.limit(1L)));
		if (es.size() > 0) {
			return Optional.of(es.get(0));
		}
		return Optional.empty();
	}

	List<E> select(Function<SelectBuilder, SelectBuilder> b) throws SQLException;

	<DTO> List<DTO> selectDTO(SelectQuery q, Class<DTO> dtoClass) throws SQLException;

	<T> List<T> distinct(String fieldName, Class<T> type) throws SQLException;

	List<E> select(SelectQuery q) throws SQLException;

	Optional<E> selectById(ID id) throws SQLException;

	List<E> selectAll() throws SQLException;

	List<E> selectAll(List<ID> ids) throws SQLException;

	List<E> selectByFK(String foreignKeyField, Object one) throws SQLException;

	/*
	 * U
	 */
	Optional<E> update(E e) throws SQLException;

	List<E> update(List<E> es) throws SQLException;

	List<E> update(Function<UpdateBuilder, UpdateBuilder> b) throws SQLException;

	List<E> update(UpdateQuery q) throws SQLException;

	/*
	 * D
	 */
	List<E> delete(Function<DeleteBuilder, DeleteBuilder> b) throws SQLException;

	List<E> delete(DeleteQuery q) throws SQLException;

	/*
	 * Aggregate Functions
	 */

	default long count() throws SQLException {
		return count(Entity.ID);
	}

	long count(String field) throws SQLException;

	long countDistinct(String field) throws SQLException;

	long min(String field) throws SQLException;

	long max(String field) throws SQLException;

	Number sum(String field) throws SQLException;

	Number average(String field) throws SQLException;

	/*
	 * * * OLD CRUD
	 */

	/**
	 * saves (inserts or updates) the given item.
	 * <p>
	 * the user name for audit logging is taken from
	 * {@link Database#getActiveUser()}
	 * <p>
	 * Metadata-stamping and audit-logging will be performed
	 * 
	 * @param item the item to save (insert or update)
	 * 
	 * @return the item as it was loaded from the database.
	 * 
	 * @throws SQLException
	 * 
	 * @see Database#getActiveUser()
	 */
	Optional<E> save(E item) throws SQLException;

	List<E> save(List<E> item) throws SQLException;

	/**
	 * Checks if a record with the given id exists.
	 * 
	 * @param id              the id to search for.
	 * 
	 * @return true a record with the specified id exists, false otherwise
	 * 
	 * @throws SQLException
	 */
	boolean exists(ID id) throws SQLException;

	/**
	 * deletes a record.
	 * <p>
	 * the user name for audit-logging will be taken from
	 * {@link Database#getActiveUser()}.
	 * <p>
	 * the connection will be closed after the deletion process.
	 * 
	 * @param id the id of the record to delete.
	 * 
	 * @return the deleted record if any or Optional.empty() if the id was not
	 *         found.
	 * 
	 * @throws SQLException
	 * 
	 * @see Database#getActiveUser()
	 */
//	@Deprecated
	Optional<E> deleteById(ID id) throws SQLException;

	/*
	 * * * Raw Query
	 */

	/*
	 * * * Lazy Load
	 */

//	default void load(String fieldName, E item) throws SQLException, InterruptedException {
//		load(fieldName, Arrays.asList(item));
//	}

//	default void load(String fieldName, List<E> items) throws SQLException, InterruptedException {
//		loadMany(fieldName, items, LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION, getDB().getActiveUser());
//	}
//
//	/**
//	 * 
//	 * @param fieldName
//	 * @param items           either the mutable entities or builders for immutable
//	 *                        entities
//	 * @param closeConnection
//	 * @param user
//	 * @throws SQLException
//	 */
//	void loadMany(String fieldName, List<E> items, boolean closeConnection, String user)
//			throws SQLException;

	/*
	 * * * Tree Entity
	 */

	<T extends TreeEntity<T, ID>> void resolve(T item);

	<T extends TreeEntity<T, ID>> void resolve(T item, int depth);

	List<?> childrenOf(TreeEntity<?, ?> parent) throws SQLException;

	List<?> foreignChildrenOf(TreeEntity<?, ?> parent) throws SQLException;

	void registerChildRepository(Repository<?, ?> repository);

	/*
	 * * * Joins
	 */

	default void joinEntity(SelectBuilder s, Repository<?, ?> r, String foreignKey, String referencedField) {
		joinEntity(s, r.getSchemaName(), r.getEntityClass(), foreignKey, referencedField);
	}

	void joinEntity(SelectBuilder s, String schemaName, Class<? extends Entity<?>> entity, String foreignKey,
			String referencedField);

	void joinEntity(SelectBuilder s, String schemaName, Class<? extends Entity<?>> entity, String foreignKey,
			String referencedField, int self);

	/*
	 * * * Logging
	 */

	/**
	 * Gets the repositories {@code LogRepository} if auditing is enabled.
	 * 
	 * @return the repositories {@code LogRepository} or null if auditing is not
	 *         enabled.
	 */
	Repository<?, ?> getLogRepository();

	/**
	 * Lists the auditing history of the specified record.
	 * 
	 * @param id the record id to get the history of actions of.
	 * 
	 * @return the log of actions associated with the specified id or null if
	 *         auditing is not enabled.
	 * 
	 * @throws SQLException
	 */
	List<Log> listHistory(ID id) throws SQLException;

//	Class<? extends Entity<?>> getEntityInterfaceClass();
}
