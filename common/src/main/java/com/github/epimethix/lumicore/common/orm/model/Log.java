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
package com.github.epimethix.lumicore.common.orm.model;

/**
 * This entity is managed by AuditMetaRepository to log auditing events.
 * <p>
 * Parallel to each audited entity there one auditing table
 * 
 * @author epimethix
 *
 */
//@EntityDefinition(name = "lumicore_audit")
public class Log implements Entity<Long> {
//	@FieldDefinition(name = ID, type = Definition.TYPE_INTEGER_PK)
	private Long id;

	/**
	 * Column name: {@value #TIMESTAMP}
	 */
	public final static String TIMESTAMP = "timestamp";
//	@FieldDefinition(name = TIMESTAMP)
	private long timestamp;

	/**
	 * Column name: {@value #ENTRY_ID}
	 */
	public final static String ENTRY_ID = "entryId";
//	@FieldDefinition(name = ENTRY_ID)
	private Object entryId;

	/**
	 * Column name: {@value #EVENT}
	 */
	public final static String EVENT = "event";
//	@FieldDefinition(name = EVENT)
	private char event;

	/**
	 * Column name: {@value #USER}
	 */
	public final static String USER = "user";
//	@FieldDefinition(name = USER)
	private String user;

	public Log() {}

	@Override
	public Long getId() {
		return id;
	}

//	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Object getEntryId() {
		return entryId;
	}

	public <T> T getEntryId(Class<T> cls) {
		return cls.cast(entryId);
	}

	public void setEntryId(Object entryId) {
		this.entryId = entryId;
	}

	public char getEvent() {
		return event;
	}

	public void setEvent(char event) {
		this.event = event;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entryId == null) ? 0 : entryId.hashCode());
		result = prime * result + event;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		Log other = (Log) obj;
		if (entryId == null) {
			if (other.entryId != null)
				return false;
		} else if (!entryId.equals(other.entryId))
			return false;
		if (event != other.event)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}
}
