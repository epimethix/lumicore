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

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.orm.query.QueryBuilderFactory;
import com.github.epimethix.lumicore.common.orm.sqlite.Definition;

public interface Dialect extends QueryBuilderFactory, TypeMap, ConnectionController, QueryCompiler {
	
	Definition getDefinition(Field f);
	
	void autoSyncSchema() throws SQLException, ConfigurationException;

	/**
	 * Lists all non-system database tables
	 * 
	 * @return the list of all existing non-system database tables
	 * @throws SQLException
	 */
	List<String> listDatabaseTableNames() throws SQLException;

	/**
	 * Get the PRAGMA application id
	 * 
	 * @return the PRAGMA application id
	 */
	int getApplicationId();

	/**
	 * Set the PRAGMA application id
	 */
	void setApplicationId(int applicationId);
}
