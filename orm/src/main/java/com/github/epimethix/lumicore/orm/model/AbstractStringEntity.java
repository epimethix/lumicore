/*
 * Copyright 2022 epimethix@protonmail.com
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

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.orm.annotation.field.Column;
import com.github.epimethix.lumicore.orm.annotation.field.PrimaryKey;

public abstract class AbstractStringEntity implements Entity<String> {

	@PrimaryKey
	@Column(name = ID)
	private final String id;

	protected AbstractStringEntity(String id) {
		this.id = id;
	}
	
	@Override
	public String getId() {
		return id;
	}
	
//	@Override
//	public void setId(String id) {
//		this.id = id;
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		AbstractStringEntity other = (AbstractStringEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
