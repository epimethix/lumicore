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
package com.github.epimethix.lumicore.common.orm.query;

import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.query.Query.CreateBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.CreateIndexBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.DeleteBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.InsertBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.UpdateBuilder;
import com.github.epimethix.lumicore.common.orm.sqlite.Definition;

public interface QueryBuilderFactory {

	CreateBuilder create(String schemaName, Class<? extends Entity<?>> e, Definition... definitions);

	default CreateBuilder create(Repository<?, ?> r, Definition... definitions) {
		return create(r.getSchemaName(), r.getEntityClass(), definitions);
	}
	
	CreateIndexBuilder createIndex(String schemaName, Class<? extends Entity<?>> e, String indexName, String... fields);

	default CreateIndexBuilder createIndex(Repository<?, ?> r, String indexName, String... fields) {
		return createIndex(r.getSchemaName(), r.getEntityClass(), indexName, fields);
	}

	InsertBuilder insert(String schemaName, Class<? extends Entity<?>> e, String... fields);

	default InsertBuilder insert(Repository<?, ?> r, String... fields) {
		return insert(r.getSchemaName(), r.getEntityClass(), fields);
	}

	SelectBuilder select(String schemaName, Class<? extends Entity<?>> e, String... selection);

	default SelectBuilder select(Repository<?, ?> r, String... selection) {
		return select(r.getSchemaName(), r.getEntityClass(), selection);
	}

	UpdateBuilder update(String schemaName, Class<? extends Entity<?>> e);

	default UpdateBuilder update(Repository<?, ?> r) {
		return update(r.getSchemaName(), r.getEntityClass());
	}

	DeleteBuilder delete(String schemaName, Class<? extends Entity<?>> e);

	default DeleteBuilder delete(Repository<?, ?> r) {
		return delete(r.getSchemaName(), r.getEntityClass());
	}
}
