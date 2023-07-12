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
package com.github.epimethix.lumicore.orm.model;

import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.model.MutableEntity;
import com.github.epimethix.lumicore.common.orm.sqlite.Definition;
import com.github.epimethix.lumicore.orm.annotation.entity.Table;
import com.github.epimethix.lumicore.orm.annotation.field.Column;

@Table(name = "lumicore_meta", withoutRowID = true, strategy = Table.STRATEGY_EXPLICIT_DEFINITION)
public final class Meta implements MutableEntity<String> {

	public static final long STRUCTURE_VERSION = 1;

	/**
	 * Column name: {@value #ID}
	 */
	public final static String ID = "id";
	@Column(name = ID, type = Definition.TYPE_TEXT_PK)
	private String id;

	/**
	 * Column name: {@value #VALUE}
	 */
	public final static String VALUE = "value";
	@Column(name = VALUE, type = Definition.TYPE_TEXT)
	private String value;

	public Meta() {}

	public Meta(String key, String value) {
		this.id = key;
		this.value = value;
	}

//	public final String getKey() {
//		return key;
//	}

//	public final void setKey(String key) {
//		this.key = key;
//	}

	public final String getValue() {
		return value;
	}

	public final void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Meta other = (Meta) obj;
		return Objects.equals(id, other.id) && Objects.equals(value, other.value);
	}
}
