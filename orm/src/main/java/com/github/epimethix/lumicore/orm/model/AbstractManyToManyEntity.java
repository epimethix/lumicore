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

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.model.ManyToManyEntity;

public abstract class AbstractManyToManyEntity<A extends Entity<?>, B extends Entity<?>> extends AbstractMutableIntegerEntity
		implements ManyToManyEntity<A, B> {
	@Override
	public int hashCode() {
		A a = getA();
		B b = getB();
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null || a.getId() == null) ? 0 : a.getId().hashCode());
		result = prime * result + ((b == null || b.getId() == null) ? 0 : b.getId().hashCode());
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
		@SuppressWarnings("rawtypes")
		AbstractManyToManyEntity other = (AbstractManyToManyEntity) obj;
		A a = getA();
		if (a == null) {
			if (other.getA() != null)
				return false;
		} else {
			Object idA = a.getId();
			Object idA2 = other.getA().getId();
			if (!idA.equals(idA2)) {
				return false;
			}
		}
		B b = getB();
		if (b == null) {
			if (other.getB() != null)
				return false;
		} else {
			Object idB = b.getId();
			Object idB2 = other.getB().getId();
			if (!idB.equals(idB2)) {
				return false;
			}
		}
		return true;
	}
}
