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
package com.github.epimethix.lumicore.orm;

import java.util.HashMap;
import java.util.Map;

import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;

public final class EntityController {
	private static final Map<Class<? extends Entity<?>>, Repository<?, ?>> REPOSITORY_MAP;

	static {
		REPOSITORY_MAP = new HashMap<Class<? extends Entity<?>>, Repository<?, ?>>();
	}

	public static final void register(Class<? extends Entity<?>> entityClass, Repository<?, ?> repository) {
		REPOSITORY_MAP.put(entityClass, repository);
	}

	public static Repository<?, ?> getRepository(Class<? extends Entity<?>> entityClass) {
		return REPOSITORY_MAP.get(entityClass);
	}

	public static boolean containsKey(Class<? extends Entity<?>> entityClass) {
		return REPOSITORY_MAP.containsKey(entityClass);
	}

	public static <E extends Entity<?>> E copy(E o) {
		Class<E> entityClass = (Class<E>) o.getClass();
		Repository<E, ?> repository = (Repository<E, ?>) getRepository(entityClass);
		return repository.copy(o);
	}

	public static <E extends Entity<?>> boolean recordEquals(E o1, E o2) {
		if (o1.getClass() == o2.getClass()) {
			Class<E> entityClass = (Class<E>) o1.getClass();
			Repository<E, ?> repository = (Repository<E, ?>) getRepository(entityClass);
			return repository.contentEquals(o1, o2);
		}
		return false;
	}

	private EntityController() {}
}
