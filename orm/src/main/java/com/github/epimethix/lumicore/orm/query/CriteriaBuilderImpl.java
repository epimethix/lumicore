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
package com.github.epimethix.lumicore.orm.query;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.query.Criteria;
import com.github.epimethix.lumicore.common.orm.query.Criterium.Type;
import com.github.epimethix.lumicore.common.orm.query.DefaultCriteria;
import com.github.epimethix.lumicore.common.orm.query.DefaultCriterium;
import com.github.epimethix.lumicore.common.orm.query.Query;
import com.github.epimethix.lumicore.common.orm.query.Query.CriteriaBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.QueryBuilder;

final class CriteriaBuilderImpl<T extends QueryBuilder<Q>, Q extends Query<Q>> implements CriteriaBuilder<T, Q> {

	private final T queryBuilder;
	private final Criteria criteria;

	private String alias;

	CriteriaBuilderImpl(CriteriaBuilderImpl<T, Q> c, T parentBuilder) {
		this.queryBuilder = parentBuilder;
		this.criteria = new DefaultCriteria((DefaultCriteria) c.criteria);
	}

	public CriteriaBuilderImpl(T queryBuilder) {
		this.queryBuilder = queryBuilder;
		this.criteria = new DefaultCriteria();
	}

	@Override
	public CriteriaBuilder<T, Q> withAlias(String alias) {
		this.alias = alias;
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> openBracket() {
		criteria.addCriterium(new DefaultCriterium(Type.OPEN_BRACKET));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> closeBracket() {
		criteria.addCriterium(new DefaultCriterium(Type.CLOSE_BRACKET));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> and() {
		criteria.addCriterium(new DefaultCriterium(Type.AND));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> or() {
		criteria.addCriterium(new DefaultCriterium(Type.OR));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> not() {
		criteria.addCriterium(new DefaultCriterium(Type.NOT));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> in(String field, List<Object> values) {
		Objects.requireNonNull(values);
		values.stream().forEach(x -> Objects.requireNonNull(x));
		criteria.addCriterium(new DefaultCriterium(Type.IN, alias, field, values));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> between(String field, Number start, Number end) {
		criteria.addCriterium(new DefaultCriterium(Type.BETWEEN, alias, field,
				Arrays.asList(Objects.requireNonNull(start), Objects.requireNonNull(end))));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> isNull(String field) {
		criteria.addCriterium(new DefaultCriterium(Type.IS_NULL, alias, field));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> isNotNull(String field) {
		criteria.addCriterium(new DefaultCriterium(Type.IS_NOT_NULL, alias, field));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> isZero(String field) {
		criteria.addCriterium(new DefaultCriterium(Type.IS_ZERO, alias, field));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> isNotZero(String field) {
		criteria.addCriterium(new DefaultCriterium(Type.IS_NOT_ZERO, alias, field));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> equals(String field, Number value) {
		criteria.addCriterium(new DefaultCriterium(Type.EQUALS, alias, field, Objects.requireNonNull(value)));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> equals(String field, String value) {
		criteria.addCriterium(new DefaultCriterium(Type.EQUALS, alias, field, Objects.requireNonNull(value)));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> equals(String field, Object value) {
		criteria.addCriterium(new DefaultCriterium(Type.EQUALS, alias, field, Objects.requireNonNull(value)));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> matches(String field, String value, char escape) {
		criteria.addCriterium(new DefaultCriterium(Type.MATCHES, alias, field, Objects.requireNonNull(value), escape));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> lessThan(String field, Number value) {
		criteria.addCriterium(new DefaultCriterium(Type.LESS_THAN, alias, field, Objects.requireNonNull(value)));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> lessThanEquals(String field, Number value) {
		criteria.addCriterium(new DefaultCriterium(Type.LESS_THAN_EQUALS, alias, field, Objects.requireNonNull(value)));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> greaterThan(String field, Number value) {
		criteria.addCriterium(new DefaultCriterium(Type.GREATER_THAN, alias, field, Objects.requireNonNull(value)));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> greaterThanEquals(String field, Number value) {
		criteria.addCriterium(
				new DefaultCriterium(Type.GREATER_THAN_EQUALS, alias, field, Objects.requireNonNull(value)));
		return this;
	}

	@Override
	public CriteriaBuilder<T, Q> clear() {
		criteria.clear();
		return this;
	}

	@Override
	public String buildCriteria() {
		return criteria.getCriteria();
	}

	@Override
	public List<Object> getCriteriumValues() {
		return criteria.getCriteriumValues();
	}

	@Override
	public T leave() {
		return queryBuilder;
	}

	@Override
	public boolean isEmpty() {
		return criteria.isEmpty();
	}

	@Override
	public String[] getCriteriumFields() {
		return criteria.getCriteriumFields().toArray(new String[] {});
	}
}
