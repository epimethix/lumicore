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

import java.sql.SQLException;
import java.util.List;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.model.Log;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectQuery;
import com.github.epimethix.lumicore.orm.model.Key.CompositeKey;

public final class LogRepository<E extends Entity<ID>, ID> extends SQLRepository<Log, Long> {

	final static long STRUCTURE_VERSION = 1;

	private final Class<E> ENTITY_CLASS;

	private final SelectQuery logsByItemId;

	public LogRepository(Database db, Class<E> entityClass, Class<ID> entryIdClass) throws ConfigurationException {
		super(db, Log.class, Long.class, null, entryIdClass, Entity.getEntityName(entityClass));
		this.ENTITY_CLASS = entityClass;
		logsByItemId = DEFAULT_SELECT_QUERY.builder().withCriteria(this).equals(Log.ENTRY_ID, "").leave().build();
	}

	final Class<?> getLoggingEntityClass() {
		return ENTITY_CLASS;
	}

	void log(ID id, char event, String user) throws SQLException {
		Log am = new Log();
		if(id instanceof CompositeKey) {
			am.setEntryId(id.toString());
		} else {
			am.setEntryId(id);
		}
		am.setEvent(event);
		am.setUser(user);
		am.setTimestamp(System.currentTimeMillis());
		save(am);
	}

	List<Log> listLogByItemId(ID id) throws SQLException, InterruptedException {
//		if(id instanceof CompositeKey) {
//			return listBy(id.toString(), String.class, Log.ENTRY_ID);
//		} else {
//			return listBy(id, id.getClass(), Log.ENTRY_ID);
//		}
		return select(logsByItemId.withCriteriumValues(id));
	}

	@Override
	public void upgrade(long structureVersion) throws SQLException {}
}
