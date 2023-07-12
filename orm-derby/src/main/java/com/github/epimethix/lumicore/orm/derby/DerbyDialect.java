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
package com.github.epimethix.lumicore.orm.derby;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.sql.ConnectionFactory;
import com.github.epimethix.lumicore.common.orm.sqlite.Definition;
import com.github.epimethix.lumicore.orm.sql.SQLDialect;

public class DerbyDialect extends SQLDialect{


	public DerbyDialect(Database db, ConnectionFactory connectionFactory) {
		super(db, connectionFactory);
	}
	
	/*
	 * * * Dialect
	 */
	
	@Override
	public Definition getDefinition(Field f) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void autoSyncSchema() throws SQLException, ConfigurationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> listDatabaseTableNames() throws SQLException {
		// TODO Auto-generated method stub
//		select st.tablename  from sys.systables st LEFT OUTER join sys.sysschemas ss on (st.schemaid = ss.schemaid) where ss.schemaname ='APP'
		return null;
	}

	@Override
	public int getApplicationId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setApplicationId(int applicationId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isMappableType(Class<?> type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int resolveType(Class<?> mappedType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getReferencingType(Class<? extends Entity<?>> referencedEntity, String referencedFieldName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSQLType(int lumicoreType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int autoDetectType(Class<?> mappingType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int resolveType(Class<?> mappedType, String referencedFieldName) {
		// TODO Auto-generated method stub
		return 0;
	}
}
