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
package com.github.epimethix.lumicore.orm;

import java.lang.reflect.InvocationTargetException;

import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.sql.ConnectionFactory;
import com.github.epimethix.lumicore.common.orm.sql.Dialect;
import com.github.epimethix.lumicore.orm.sql.GenericSQLDialect;

public final class DialectFactory {

	public static Dialect createDialect(Database database, ConnectionFactory connectionFactory) {
		String conn = connectionFactory.getConnectionString();
		String dialectClass = "";
		if (conn.startsWith("jdbc:sqlite")) {
			dialectClass = "com.github.epimethix.lumicore.orm.sqlite.SQLiteDialect";
		} else if(conn.startsWith("jdbc:derby")){
			dialectClass = "com.github.epimethix.lumicore.orm.derby.DerbyDialect";
		}
		try {
			return (Dialect) Class.forName(dialectClass)
					.getConstructor(Database.class, ConnectionFactory.class)
					.newInstance(database, connectionFactory);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			e.printStackTrace();
		}
		return new GenericSQLDialect(database, connectionFactory);
	}

	private DialectFactory() {}

}
