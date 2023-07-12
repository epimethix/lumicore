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
package com.github.epimethix.lumicore.orm.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLiteMaster {

	private final String type;
	private final String name;
	private final String tbl_name;
	private final long rootpage;
	private final String sql;

	public SQLiteMaster(ResultSet rs) throws SQLException {
		this.type = rs.getString(1);
		this.name = rs.getString(2);
		this.tbl_name = rs.getString(3);
		this.rootpage = rs.getInt(4);
		this.sql = rs.getString(5);
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getTbl_name() {
		return tbl_name;
	}

	public long getRootpage() {
		return rootpage;
	}

	public String getSql() {
		return sql;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (rootpage ^ (rootpage >>> 32));
		result = prime * result + ((sql == null) ? 0 : sql.hashCode());
		result = prime * result + ((tbl_name == null) ? 0 : tbl_name.hashCode());
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
		SQLiteMaster other = (SQLiteMaster) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (rootpage != other.rootpage)
			return false;
		if (sql == null) {
			if (other.sql != null)
				return false;
		} else if (!sql.equals(other.sql))
			return false;
		if (tbl_name == null) {
			if (other.tbl_name != null)
				return false;
		} else if (!tbl_name.equals(other.tbl_name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
