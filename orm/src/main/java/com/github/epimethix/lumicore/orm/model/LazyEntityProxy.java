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
import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;

public class LazyEntityProxy implements InvocationHandler{
	private final Object id;
	private final Repository repository;
	
	private Entity entity;
	
	
	public LazyEntityProxy(Object id, Repository repository) {
		this.id = id;
		this.repository = repository;
	}

	private Entity getEntity() {
		if(Objects.isNull(entity)) {
			System.err.println("loading lazy");
			try {
				entity = (Entity) repository.selectById(id).orElse(null);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return entity;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Entity e = getEntity();
		if(Objects.nonNull(e)) {
			return method.invoke(e, args);
		}
		throw new SQLException("Entity could not lazy load!");
	}
}
