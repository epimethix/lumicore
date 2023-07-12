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
package com.github.epimethix.lumicore.common.orm;

import java.sql.Connection;
import java.sql.SQLException;

import org.sqlite.SQLiteConfig;

import com.github.epimethix.lumicore.common.orm.query.QueryBuilderFactory;
/**
 * replaced by ConnectionFactory
 *
 */
@Deprecated
public interface SQLConnection {

	boolean isConnectionWorking();

	void close() throws SQLException;

	boolean isDeployed();

	Connection getConnection() throws SQLException;

	void checkClose(boolean closeConnection) throws SQLException;

	void setKey(String key, SQLiteConfig config) throws SQLException;

	String getPath();

//	QueryBuilderFactory getQueryBuilderFactory();
}
