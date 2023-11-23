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
package com.github.epimethix.lumicore.common.orm.sql;

import java.util.List;

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.query.Query;
import com.github.epimethix.lumicore.common.orm.query.Query.CreateIndexBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.CreateIndexQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.CriteriaBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.DeleteBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.DeleteQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.UpdateBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.UpdateQuery;
import com.github.epimethix.lumicore.common.orm.sqlite.Constraint;
import com.github.epimethix.lumicore.common.orm.sqlite.Definition;

public interface QueryCompiler {


	String compileCreate(boolean temp, boolean ifNotExists, String schemaName, String tableName,
			List<Definition> definitions, List<Constraint> constraints, boolean strict, boolean withoutRowid);

	String compileCreateIndex(boolean unique, boolean ifNotExists, String indexName, String schemaName,
			Class<? extends Entity<?>> e, String[] fields, CriteriaBuilder<CreateIndexBuilder, CreateIndexQuery> criteriaBuilder);
	
	String compileInsert(Object schemaName, Class<? extends Entity<?>> e, String[] fields,
			List<? extends Entity<?>> records);

	String compileSelect(Query prev, boolean distinct, StringBuilder selectionBuilder, String tableName, String alias,
			StringBuilder joinBuilder, StringBuilder groupByBuilder, CriteriaBuilder<SelectBuilder, SelectQuery> criteriaBuilder,
			StringBuilder orderByBuilder, String nulls, Long limit, Long defLimit, Long offset);

	String compileUpdate(String schemaName, Class<? extends Entity<?>> e, String[] fields,
			CriteriaBuilder<UpdateBuilder, UpdateQuery> criteriaBuilder);

	String compileDelete(Object schemaName, Class<? extends Entity<?>> e,
			CriteriaBuilder<DeleteBuilder, DeleteQuery> criteriaBuilder);
	
	String getQuotationChar();

	String quoteIdentifier(String identifier);
}
