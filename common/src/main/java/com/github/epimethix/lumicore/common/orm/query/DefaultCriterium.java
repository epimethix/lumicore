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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DefaultCriterium implements Criterium {

	private final Type type;
	private final String tableAlias;
	private final String field;
	private final List<Object> values;
	private char escape;

	public DefaultCriterium(Type type) {
		this(type, null, null, Collections.emptyList());
	}

	public DefaultCriterium(Type type, String tableAlias, String field) {
		this(type, tableAlias, field, Collections.emptyList());
	}

	public DefaultCriterium(Type type, String tableAlias, String field, Object value) {
		this(type, tableAlias, field, new ArrayList<>(Arrays.asList(value)));
	}

	public DefaultCriterium(Type type, String tableAlias, String field, List<Object> values) {
		this(type, tableAlias, field, values, '\\');
	}

	public DefaultCriterium(Type type, String tableAlias, String field, String value, char escape) {
		this(type, tableAlias, field, Arrays.asList(value), escape);
	}

	public DefaultCriterium(Type type, String tableAlias, String field, List<Object> values, char escape) {
		this.type = type;
		this.tableAlias = tableAlias;
		this.field = field;
		this.values = values;
		this.escape = escape;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getEntityAlias() {
		return tableAlias;
	}

	@Override
	public String getField() {
		return field;
	}

	@Override
	public List<Object> getValues() {
		return values;
	}

	@Override
	public String getCriterium() {
		switch (type) {
		case AND:
			return "AND ";
		case OR:
			return "OR ";
		case OPEN_BRACKET:
			return "( ";
		case CLOSE_BRACKET:
			return ") ";
		case NOT:
			return "NOT ";
		case IS_NULL:
			return String.format("%s.`%s` IS NULL ", tableAlias, field);
		case IS_NOT_NULL:
			return String.format("%s.`%s` IS NOT NULL ", tableAlias, field);
		case IS_ZERO:
			return String.format("%s.`%s` = 0 ", tableAlias, field);
		case IS_NOT_ZERO:
			return String.format("%s.`%s` != 0 ", tableAlias, field);
		case EQUALS:
			return String.format("%s.`%s` = ? ", tableAlias, field);
		case MATCHES:
			return String.format("%s.`%s` LIKE ? ESCAPE '%s'", tableAlias, field, String.valueOf(escape));
		case GREATER_THAN:
			return String.format("%s.`%s` > ? ", tableAlias, field);
		case GREATER_THAN_EQUALS:
			return String.format("%s.`%s` >= ? ", tableAlias, field);
		case LESS_THAN:
			return String.format("%s.`%s` < ? ", tableAlias, field);
		case LESS_THAN_EQUALS:
			return String.format("%s.`%s` <= ? ", tableAlias, field);
		case BETWEEN:
			return String.format("%s.`%s` BETWEEN ? AND ? ", tableAlias, field);
		case IN:
			if (values.size() > 1)
				return String.format("%s.`%s` IN (?%s)", tableAlias, field, ", ?".repeat(values.size() - 1));
			else if (values.size() == 1)
				return String.format("%s.`%s` IN (?)", tableAlias, field);
		default:
			System.err.printf("getting criterium for %s of type %s with values %s failed%n", field, type.name(),
					Objects.nonNull(values) ? values.toString() : "null");
			return "";
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(field, type, values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultCriterium other = (DefaultCriterium) obj;
		return Objects.equals(field, other.field) && type == other.type && Objects.equals(values, other.values);
	}
}
