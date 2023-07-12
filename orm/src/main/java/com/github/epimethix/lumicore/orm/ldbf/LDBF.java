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
package com.github.epimethix.lumicore.orm.ldbf;

import java.util.List;

/**
 * LDBF stands for "Lumicore DataBase Format". An LDBF instance contains all
 * relevant schema information for an sqlite database.
 * 
 * @author epimethix
 *
 */
public class LDBF {

	private String schemaName;
	private boolean singleton;
	private String databaseClass;
	private String modelPackage;
	private String repositoryPackage;
	private List<String> tables;

	public LDBF() {}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public boolean isSingleton() {
		return singleton;
	}

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public String getDatabaseClass() {
		return databaseClass;
	}

	public void setDatabaseClass(String databaseClass) {
		this.databaseClass = databaseClass;
	}

	public String getModelPackage() {
		return modelPackage;
	}

	public void setModelPackage(String modelPackage) {
		this.modelPackage = modelPackage;
	}

	public String getRepositoryPackage() {
		return repositoryPackage;
	}

	public void setRepositoryPackage(String repositoryPackage) {
		this.repositoryPackage = repositoryPackage;
	}

	public List<String> getTables() {
		return tables;
	}

	public void setTables(List<String> tables) {
		this.tables = tables;
	}
}
