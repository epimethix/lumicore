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
package com.github.epimethix.lumicore.common.orm.query;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.sqlite.Constraint;
import com.github.epimethix.lumicore.common.orm.sqlite.JoinOperator;

/**
 * This interface defines Query, QueryBuilder and CriteriaBuilder.
 * <p>
 * Each specific QueryBuilder has its own type of Query.
 * <p>
 * The interfaces Query and QueryBuilder have themselves no direct
 * implementations.
 * <p>
 * The implementation classes implement
 * <ul>
 * <li>CreateQuery and CreateBuilder
 * <li>CreateIndexQuery and CreateIndexBuilder
 * <li>InsertQuery and InsertBuilder
 * <li>SelectQuery and SelectBuilder and JoinBuilder
 * <li>UpdateQuery and UpdateBuilder
 * <li>DeleteQuery and DeleteBuilder
 * <li>CriteriaBuilder
 * </ul>
 * 
 * @author epimethix
 *
 */
public interface Query<Q extends Query<Q>> {

	/**
	 * Any Query may have an acting user.
	 * 
	 * @return the acting user if one was set
	 */
	Optional<String> getUser();

	/**
	 * The connection may be closed or left open explicitly.
	 * 
	 * @return true closes the connection after the operation
	 */
	Optional<Boolean> isCloseConnection();

	/**
	 * get the raw query String.
	 * 
	 * @return the query
	 */
	String getQueryString();

	/**
	 * Get a QueryBuilder with the values of this Query.
	 * 
	 * @return the builder
	 */
	QueryBuilder<Q> builder();

	/**
	 * The QueryBuilder interface is extended by the specific query operation
	 * interfaces
	 */
	public interface QueryBuilder<Q extends Query<Q>> {

		QueryBuilder<Q> withUser(String user);

		QueryBuilder<Q> withCloseConnection(boolean closeConnection);

		Q build();
	}

	public interface CreateQuery extends Query<CreateQuery> {
		CreateBuilder builder();
	}

	/**
	 * The CreateBuilder implmentation should have a constructor that receives the
	 * schema name, the entity class and the field definitions.
	 */
	public interface CreateBuilder extends QueryBuilder<CreateQuery> {
		CreateBuilder temp();

		CreateBuilder ifNotExists();

		CreateBuilder withConstraints(Constraint... constraints);

		CreateBuilder strict();

		CreateBuilder withoutRowid();

		SelectBuilder select(String schemaName, Class<? extends Entity<?>> e, String... fields);

		SelectBuilder select(Class<? extends Entity<?>> e, String... fields);

//		CreateQuery build();
	}

	public interface CreateIndexQuery extends Query<CreateIndexQuery> {

		Object[] getCriteriumValues();

		CreateIndexBuilder builder();
	}

	/**
	 * The CreateIndexBuilder implementation should have a constructor that receives
	 * the schema name, the entity class, the index name and the column names to
	 * index.
	 */
	public interface CreateIndexBuilder extends QueryBuilder<CreateIndexQuery> {
		CreateIndexBuilder unique();

		CreateIndexBuilder ifNotExists();

		/**
		 * Synonym for {@link #withCriteria(String, Class)}
		 */
		default CriteriaBuilder<CreateIndexBuilder, CreateIndexQuery> where(String schemaName, Class<? extends Entity<?>> e) {
			return withCriteria(schemaName, e);
		}

		CriteriaBuilder<CreateIndexBuilder, CreateIndexQuery> withCriteria(String schemaName, Class<? extends Entity<?>> e);

//		CreateIndexQuery build();
	}

	public interface InsertQuery extends Query<InsertQuery> {
		String[] getFields();

		List<? extends Entity<?>> getRecords();

		InsertQuery withRecords(List<? extends Entity<?>> records);

		InsertBuilder builder();
	}

	/**
	 * The InsertBuilder implementation should have a constructor that receives the
	 * schema name, the entity class and the columns to set.
	 */
	public interface InsertBuilder extends QueryBuilder<InsertQuery> {
		default InsertBuilder values(Entity<?> record) {
			return values(Arrays.asList(record));
		}

		InsertBuilder values(List<? extends Entity<?>> records);

		SelectBuilder select(String schemaName, Class<? extends Entity<?>> entity, String... fields);

//		InsertQuery build();
	}

	public interface SelectQuery extends Query<SelectQuery> {
		Object[] getCriteriumValues();

		SelectBuilder builder();

		SelectQuery withCriteriumValues(Object... values);

		SelectQuery atPage(long page);
	}

	/**
	 * SelectBuilder implementation should have a constructor that receives schema
	 * name, Entity class and the fields to select.
	 */
	public interface SelectBuilder extends QueryBuilder<SelectQuery> {

		/**
		 * The constructor of the implementation class should receive the select
		 * builder, the local Table and the referenced table names and a list of join
		 * operators.
		 */
		public interface JoinBuilder {
			default SelectBuilder on(String foreignKey, String referencedField) {
				return on(new String[] { foreignKey }, new String[] { referencedField });
			}

			SelectBuilder on(String[] foreignKeys, String[] referencedFields);

			SelectBuilder using(String... columns);
		}

		SelectBuilder distinct();

		/**
		 * Synonym for {@link #select(String, Class, String...)} with "main" as schema
		 * name
		 */
		default SelectBuilder select(Class<? extends Entity<?>> e, String... fields) {
			return select("main", e, fields);
		}

		SelectBuilder select(String schemaName, Class<? extends Entity<?>> e, String... fields);

		SelectBuilder select(String alias, String... fields);

		JoinBuilder join(String localSchemaName, Class<? extends Entity<?>> localTable, String otherSchemaName,
				Class<? extends Entity<?>> otherTable, String[] generatedAlias, JoinOperator... joinOperators);

		/**
		 * Synonym for {@link #withCriteria(String, Class)}
		 */
		default CriteriaBuilder<SelectBuilder, SelectQuery> where(String schemaName, Class<? extends Entity<?>> e) {
			return withCriteria(schemaName, e);
		}

		/**
		 * Synonym for {@link #withCriteria(String, Class)}
		 */
		default CriteriaBuilder<SelectBuilder, SelectQuery> having(String schemaName, Class<? extends Entity<?>> e) {
			return withCriteria(schemaName, e);
		}

		CriteriaBuilder<SelectBuilder, SelectQuery> withCriteria(String schemaName, Class<? extends Entity<?>> e);

		SelectBuilder groupBy(String schemaName, Class<? extends Entity<?>> e, String field, String... fields);

		default SelectBuilder orderBy(String schemaName, Class<? extends Entity<?>> e, String field) {
			return orderByAsc(schemaName, e, field);
		}

		SelectBuilder orderByAsc(String schemaName, Class<? extends Entity<?>> e, String fields);

		SelectBuilder orderByDesc(String schemaName, Class<? extends Entity<?>> e, String fields);

		SelectBuilder nullsFirst();

		SelectBuilder nullsLast();

		SelectBuilder clearLimit();

		SelectBuilder limit(long limit);

		SelectBuilder offset(long offset);

		SelectBuilder page(long page);

//		SelectQuery build();

		default CriteriaBuilder<SelectBuilder, SelectQuery> withCriteria(Repository<?, ?> r) {
			return withCriteria(r.getSchemaName(), r.getEntityClass());
		}

		default SelectBuilder selectCount(Repository<?, ?> r, String field) {
			return selectCount(r.getSchemaName(), r.getEntityClass(), field);
		}

		SelectBuilder selectCount(String schemaName, Class<? extends Entity<?>> e, String field);

		default SelectBuilder selectSum(Repository<?, ?> r, String field) {
			return selectSum(r.getSchemaName(), r.getEntityClass(), field);
		}

		SelectBuilder selectSum(String schemaName, Class<? extends Entity<?>> e, String field);

		default SelectBuilder selectMin(Repository<?, ?> r, String field) {
			return selectMin(r.getSchemaName(), r.getEntityClass(), field);
		}

		SelectBuilder selectMin(String schemaName, Class<? extends Entity<?>> e, String field);

		default SelectBuilder selectMax(Repository<?, ?> r, String field) {
			return selectMax(r.getSchemaName(), r.getEntityClass(), field);
		}

		SelectBuilder selectMax(String schemaName, Class<? extends Entity<?>> e, String field);

		default SelectBuilder selectAverage(Repository<?, ?> r, String field) {
			return selectAverage(r.getSchemaName(), r.getEntityClass(), field);
		}

		SelectBuilder selectAverage(String schemaName, Class<? extends Entity<?>> e, String field);
	}

	public interface UpdateQuery extends Query<UpdateQuery> {
		Object[] getSetValues();

		Object[] getCriteriumValues();

		String[] getFields();

		UpdateBuilder builder();

		UpdateQuery withValues(Object... values);

		UpdateQuery withCriteriumValues(Object... values);

		String[] getCriteriumFields();

	}

	/**
	 * UpdateBuilder implementation should have a constructor that receives the
	 * entity to update
	 */
	public interface UpdateBuilder extends QueryBuilder<UpdateQuery> {

		UpdateBuilder set(String field, Object value);

		/**
		 * Synonym for {@link #withCriteria(String, Class)}
		 */
		default CriteriaBuilder<UpdateBuilder, UpdateQuery> where(String schemaName, Class<? extends Entity<?>> e) {
			return withCriteria(schemaName, e);
		}

		default CriteriaBuilder<UpdateBuilder, UpdateQuery> where(Repository<?, ?> r) {
			return withCriteria(r.getSchemaName(), r.getEntityClass());
		}

		default CriteriaBuilder<UpdateBuilder, UpdateQuery> withCriteria(Repository<?, ?> r) {
			return withCriteria(r.getSchemaName(), r.getEntityClass());
		}

		CriteriaBuilder<UpdateBuilder, UpdateQuery> withCriteria(String schemaName, Class<? extends Entity<?>> e);

//		UpdateQuery build();
	}

	public interface DeleteQuery extends Query<DeleteQuery> {
		Object[] getCriteriumValues();

		DeleteQuery withCriteriumValues(Object... values);

		DeleteBuilder builder();
	}

	public interface DeleteBuilder extends QueryBuilder<DeleteQuery> {
		/**
		 * Synonym for {@link #withCriteria(String, Class)}
		 */
		default CriteriaBuilder<DeleteBuilder, DeleteQuery> where(String schemaName, Class<? extends Entity<?>> e) {
			return withCriteria(schemaName, e);
		}

		default CriteriaBuilder<DeleteBuilder, DeleteQuery> where(Repository<?, ?> r) {
			return withCriteria(r.getSchemaName(), r.getEntityClass());
		}

		default CriteriaBuilder<DeleteBuilder, DeleteQuery> withCriteria(Repository<?, ?> r) {
			return withCriteria(r.getSchemaName(), r.getEntityClass());
		}

		CriteriaBuilder<DeleteBuilder, DeleteQuery> withCriteria(String schemaName, Class<? extends Entity<?>> e);

//		DeleteQuery build();
	}

	/**
	 * CriteriaBuilder implementation should have a constructor that receives the
	 * parent QueryBuilder. that way the parent builder should be returned in the
	 * CriteriaBuilder.leave() method.
	 */
	public interface CriteriaBuilder<T extends QueryBuilder<Q>, Q extends Query<Q>> {

		CriteriaBuilder<T, Q> openBracket();

		CriteriaBuilder<T, Q> closeBracket();

		CriteriaBuilder<T, Q> and();

		CriteriaBuilder<T, Q> or();

		CriteriaBuilder<T, Q> not();

		CriteriaBuilder<T, Q> withAlias(String alias);

		CriteriaBuilder<T, Q> in(String field, List<Object> values);

		CriteriaBuilder<T, Q> between(String field, Number start, Number end);

		CriteriaBuilder<T, Q> isNull(String field);

		CriteriaBuilder<T, Q> isNotNull(String field);

		CriteriaBuilder<T, Q> isZero(String field);

		CriteriaBuilder<T, Q> isNotZero(String field);

		CriteriaBuilder<T, Q> equals(String field, String value);

		CriteriaBuilder<T, Q> equals(String field, Number value);

		CriteriaBuilder<T, Q> equals(String field, Object value);

		/**
		 * Synonym for {@link #matches(String, String, char)} with backslash (\) as
		 * escape character
		 */
		default CriteriaBuilder<T, Q> matches(String field, String value) {
			return matches(field, value, '\\');
		}

		CriteriaBuilder<T, Q> matches(String field, String value, char escape);

		CriteriaBuilder<T, Q> lessThan(String field, Number value);

		CriteriaBuilder<T, Q> lessThanEquals(String field, Number value);

		CriteriaBuilder<T, Q> greaterThan(String field, Number value);

		CriteriaBuilder<T, Q> greaterThanEquals(String field, Number value);

		String buildCriteria();

		List<Object> getCriteriumValues();

		boolean isEmpty();

		CriteriaBuilder<T, Q> clear();

		T leave();
		
		default Q build() {
			return leave().build();
		}

		String[] getCriteriumFields();
	}
}
