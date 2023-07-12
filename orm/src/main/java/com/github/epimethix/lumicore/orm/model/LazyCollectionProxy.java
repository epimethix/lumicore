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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.ManyToManyRepository;
import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectQuery;
import com.github.epimethix.lumicore.orm.annotation.field.ManyToMany;
import com.github.epimethix.lumicore.orm.annotation.field.ManyToMany.Type;

public class LazyCollectionProxy implements InvocationHandler {
	private final Object one;
	private final Repository repository;
	private final String foreignKeyField;
//	private final SelectQuery selectByFK;
	private final ManyToMany.Type type;

	private List<? extends Entity> entities;

	public LazyCollectionProxy(Object one, Repository repository, String foreignKeyField) {
		this(one, repository, foreignKeyField, null);
	}

	public LazyCollectionProxy(Object one, Repository repository, String foreignKeyField, ManyToMany.Type type) {
		this.one = one;
		this.repository = repository;
		this.foreignKeyField = foreignKeyField;
		this.type = type;
	}

	private List<? extends Entity> getEntities() {
		if (Objects.isNull(entities)) {
			System.err.println("loading lazy");
			try {
				if (Objects.nonNull(type) && repository instanceof ManyToManyRepository<?, ?, ?>) {
					ManyToManyRepository<?, ?, ?> mtmRepo = (ManyToManyRepository<?, ?, ?>) repository;
					if(type == Type.DIRECT) {
						entities = repository.selectByFK(foreignKeyField, one);
					} else if(type == Type.VIA_A) {
						entities = mtmRepo.listByA((Entity<?>) one);
					} else if(type == Type.VIA_B) {
						entities = mtmRepo.listByB((Entity<?>) one);
					}
				} else {
					entities = repository.selectByFK(foreignKeyField, one);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return entities;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		List<? extends Entity> e = getEntities();
		if (Objects.nonNull(e)) {
			return method.invoke(e, args);
		}
		throw new SQLException("Entity could not lazy load!");
	}
}
