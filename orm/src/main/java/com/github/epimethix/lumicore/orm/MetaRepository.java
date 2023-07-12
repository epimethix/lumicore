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

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.query.QueryParameters;
import com.github.epimethix.lumicore.orm.model.Meta;
import com.github.epimethix.lumicore.properties.LumicoreProperties;

/**
 * The implementation class to manage the {@link Entity} {@link Meta}.
 * <p>
 * This repository is managed by AbstractDB that implements metadata access.
 * 
 * @author epimethix
 * 
 *         TODO return optionals not null
 *
 */
public final class MetaRepository extends SQLRepository<Meta, String> {
//	private final transient DB db;

	public MetaRepository(Database db) throws ConfigurationException {
		super(db, Meta.class, String.class);
//		this.db = db;
	}

	Meta save(String key, String value) throws SQLException {
		return save(new Meta(key, value)).orElse(null);
	}
}
