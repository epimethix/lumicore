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
package com.github.epimethix.lumicore.orm;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.ManyToManyRepository;
import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectQuery;
import com.github.epimethix.lumicore.orm.model.AbstractManyToManyEntity;
import com.github.epimethix.lumicore.orm.model.LazyCollectionProxy;

public abstract class AbstractManyToManyRepository<E extends AbstractManyToManyEntity<A, B>, A extends Entity<?>, B extends Entity<?>>
		extends SQLRepository<E, Long> implements ManyToManyRepository<E, A, B> {

	private final Repository<A, ?> repoA;
	private final Class<A> classA;
	private final Class<?> mappingTypeA;
	private final Field fieldA;
	private final String fieldNameA;
	private final Repository<B, ?> repoB;
	private final Class<B> classB;
	private final Class<?> mappingTypeB;
	private final Field fieldB;
	private final String fieldNameB;

	private final SelectQuery selectByA;
	private final SelectQuery selectByB;

	public AbstractManyToManyRepository(Database db, Class<E> entityClass, Field fieldA, Class<A> entityA, Field fieldB,
			Class<B> entityB) throws ConfigurationException {
//	public AbstractManyToManyRepository(Database db, Class<E> entityClass, Class<A> entityA, Class<?> mappingTypeA,
//			String fieldA, Class<B> entityB, Class<?> mappingTypeB, String fieldB) throws ConfigurationException {
//		Class<A> entityA, Class<?> mappingTypeA, String fieldA
		super(db, entityClass, Long.class);
		this.classA = entityA;
		this.repoA = (Repository<A, ?>) DB.getRepository(classA);
		this.mappingTypeA = fieldA.getType();
		this.fieldNameA = ORM.getFieldName(fieldA);
		this.fieldA = fieldA;
		this.fieldB = fieldB;
		this.classB = entityB;
		this.repoB = (Repository<B, ?>) DB.getRepository(classB);
		this.mappingTypeB = fieldB.getType();
		this.fieldNameB = ORM.getFieldName(fieldB);
		String referencedFieldNameA = ORM.getReferencedFieldName(fieldA);
		String referencedFieldNameB = ORM.getReferencedFieldName(fieldB);

		SelectBuilder selectByABuilder = DB.getQueryBuilderFactory().select(this, Entity.ID, fieldNameB);
		repoB.joinEntity(selectByABuilder, DB.getSchemaName(), getEntityClass(), fieldNameB, referencedFieldNameB);
		selectByABuilder.withCriteria(this).equals(fieldNameA, "");
		selectByA = selectByABuilder.build();

		SelectBuilder selectByBBuilder = DB.getQueryBuilderFactory().select(this, Entity.ID, fieldNameA);
		repoA.joinEntity(selectByBBuilder, DB.getSchemaName(), getEntityClass(), fieldNameA, referencedFieldNameA);
		selectByBBuilder.withCriteria(this).equals(fieldNameB, "");
		selectByB = selectByBBuilder.build();

	}

//	public <T extends Entity<?>, U> List<U> listByT(T t, String user, boolean closeConnection) throws SQLException {
//		List<U> result = new ArrayList<>();
//		if (Reflect2.typeEquals(t.getClass(), classA)) {
//			for (E e : listBy(t.getId(), mappingTypeA, user, closeConnection, fieldA)) {
//				result.add((U) e.getB());
//			}
//		} else if (Reflect2.typeEquals(t.getClass(), classB)) {
//			for (E e : listBy(t.getId(), mappingTypeB, user, closeConnection, fieldA)) {
//				result.add((U) e.getA());
//			}
//		} else if (Reflect2.typeEquals(t.getClass(), getEntityClass())) {
////			result.addAll(result);
//		}
//		return result;
//	}

	@Override
	public List<B> listByA(Entity<?> a, String user, boolean closeConnection) throws SQLException {
		return selectMany(selectByA.withCriteriumValues(a.getId()), classB, repoB);
	}

	@Override
	public List<A> listByB(Entity<?> b, String user, boolean closeConnection) throws SQLException {
		return selectMany(selectByB.withCriteriumValues(b.getId()), classA, repoA);
	}

	private <E extends Entity<?>> List<E> selectMany(SelectQuery q, Class<E> e, Repository<E, ?> otherRepository)
			throws SQLException {
		List<E> l = new ArrayList<E>();
		String sql = q.getQueryString();
		try {
			Connection c = DB.getConnection();
			logQuery(sql, Arrays.asList(q.getCriteriumValues()));
			try (PreparedStatement ps = c.prepareStatement(sql)) {
				fillPreparedStatementAutoType(ps, q.getCriteriumValues());
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						E x = otherRepository.initializeRecord(rs, new int[] { 3 });
						l.add(x);
					}
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException ex) {
			ex.printStackTrace();
		} catch (SQLException ex) {
			LOGGER.error(sql);
			LOGGER.error(ex);
			throw ex;
		} finally {
			checkClose(q);
		}
		return l;
	}

	@Override
	public void saveByA(Entity<?> a, List<? extends Entity<?>> bs, String user, boolean closeConnection)
			throws SQLException {
		saveByT(a, bs, mappingTypeA, fieldNameA, user, closeConnection);
	}

	@Override
	public void saveByB(Entity<?> b, List<? extends Entity<?>> as, String user, boolean closeConnection)
			throws SQLException {
		saveByT(b, as, mappingTypeB, fieldNameB, user, closeConnection);
	}

	@SuppressWarnings("unchecked")
	private <T extends Entity<?>, U extends Entity<?>> void saveByT(T t, List<U> us, Class<?> mappingType,
			String fieldName, String user, boolean closeConnection) throws SQLException {
		try {

//	        Field integerListField = testClass.getDeclaredField("integerList");
//	        ParameterizedType integerListType = (ParameterizedType) integerListField.getGenericType();
//	        Class<?> integerListClass = (Class<?>) integerListType.getActualTypeArguments()[0];
//	        System.out.println(integerListClass); // class java.lang.Integer

//			Class<?> classU = (Class<?>) ((ParameterizedType) us.getClass().getGenericSuperclass())
//					.getActualTypeArguments()[0];
			Set<E> newState = null;
			if (t.getClass() == classA) {
				newState = us.stream().map(u -> {
					E e = newRecord();
					e.setA((A) t);
					e.setB((B) u);
					return e;
				}).collect(Collectors.toSet());
			} else if (t.getClass() == classB) {
				newState = us.stream().map(u -> {
					E e = newRecord();
					e.setA((A) u);
					e.setB((B) t);
					return e;
				}).collect(Collectors.toSet());
			} else {
				System.err.println("ManyToMany Error");
				return;
			}
//			Set<E> oldState = new HashSet<>(listBy(t.getId(), mappingType, user, false, fieldName));
			Set<E> oldState = new HashSet<>(select(b -> b.withCriteria(this).equals(fieldName, t.getId()).leave()));
			if (newState.equals(oldState)) {
				return;
			}
			Set<E> toDelete = new HashSet<>(oldState);
			toDelete.removeAll(newState);
			Set<E> toSave = new HashSet<>(newState);
			toSave.removeAll(oldState);
			// TODO replace with deleteAll
			toDelete.stream().forEach(e -> {
				try {
					deleteById(e.getId());
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			});
			// TODO replace with saveAll
//			toSave.stream().forEach(e -> {
//				try {
//					save(e, QueryParameters.newBuilder(getDB()).userName(user).closeConnection(closeConnection)
//							.build());
//				} catch (SQLException | InterruptedException e1) {
//					e1.printStackTrace();
//				}
//			});
			save(new ArrayList<>(toSave));
		} finally {
			checkClose(closeConnection);
		}
	}

	@Override
	public E saveDirect(Entity<?> o) throws SQLException {
		return save((E) o).orElse(null);
	}

	@Override
	public Class<?> getClassA() {
		return classA;
	}

	@Override
	public Class<?> getTypeA() {
		return mappingTypeA;
	}

	@Override
	public String getFieldNameA() {
		return fieldNameA;
	}

	@Override
	public Class<?> getClassB() {
		return classB;
	}

	@Override
	public Class<?> getTypeB() {
		return mappingTypeB;
	}

	@Override
	public String getFieldNameB() {
		return fieldNameB;
	}
}
