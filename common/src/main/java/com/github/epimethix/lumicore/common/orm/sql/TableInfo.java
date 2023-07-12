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
package com.github.epimethix.lumicore.common.orm.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TableInfo {
	private final int cid;
	private final String name;
	private final String type;
	private final boolean notNull;
	private final String defaultValue;
	private final boolean pk;

	public TableInfo(ResultSet rs) throws SQLException {
		this.cid = rs.getInt(1);
		this.name = rs.getString(2);
		this.type = rs.getString(3);
		this.notNull = rs.getInt(4) != 0;
		this.defaultValue = rs.getString(5);
		this.pk = rs.getInt(6) != 0;
	}

	public int getCid() {
		return cid;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isNotNull() {
		return notNull;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isPk() {
		return pk;
	}

	/*
	public boolean typeEquals(int type) {

		if (this.type.equals("TEXT")) {
			if (pk) {
				return type == Definition.TYPE_TEXT_PK || type == Definition.TYPE_TEXT_PK_UUID
						|| type == Definition.TYPE_TEXT;
			} else {
				return type == Definition.TYPE_TEXT;
			}
		} else if (this.type.equals("INTEGER")) {
			if (pk) {
				return type == Definition.TYPE_INTEGER_PK || type == Definition.TYPE_INTEGER_PK_AI
						|| type == Definition.TYPE_INTEGER;
			} else {
				return type == Definition.TYPE_INTEGER;
			}
		} else if (this.type.equals("REAL")) {
			if (pk) {
				return type == Definition.TYPE_REAL_PK || type == Definition.TYPE_REAL;
			} else {
				return type == Definition.TYPE_REAL;
			}
		} else if (this.type.equals("BLOB")) {
			return type == Definition.TYPE_BLOB;
		}
		return false;
	}
	 */
	@Override
	public String toString() {
		return String.format("(%s | %s | PK:%d)", name, type, pk ? 1 : 0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cid;
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (notNull ? 1231 : 1237);
		result = prime * result + (pk ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TableInfo other = (TableInfo) obj;
		if (cid != other.cid)
			return false;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (notNull != other.notNull)
			return false;
		if (pk != other.pk)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
