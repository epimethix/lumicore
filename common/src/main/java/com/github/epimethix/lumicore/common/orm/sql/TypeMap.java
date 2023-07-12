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

import java.sql.Types;

import com.github.epimethix.lumicore.common.orm.model.Entity;

public interface TypeMap {

	boolean isMappableType(Class<?> type);
	/**
	 * resolves the SQL Type mapping that should be used for this type.
	 * 
	 * @param mappedType the java type
	 * @return the SQL Type
	 * @see Types
	 */
	int resolveType(Class<?> mappedType);

	int getReferencingType(Class<? extends Entity<?>> referencedEntity, String referencedFieldName);

	int getSQLType(int lumicoreType);

	int autoDetectType(Class<?> mappingType);
	
	int resolveType(Class<?> mappedType, String referencedFieldName);
}
