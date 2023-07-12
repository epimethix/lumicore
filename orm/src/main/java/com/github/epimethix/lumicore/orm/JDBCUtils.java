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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.sql.Dialect;
import com.github.epimethix.lumicore.common.orm.sql.TypeMap;

public class JDBCUtils {

	/**
	 * Reads the SQL Script file and turns it into a String[] holding the individual
	 * SQL statements.
	 * 
	 * @param pathToScript The path to the script file to be read.
	 * 
	 * @return a String[] containing the individual SQL statements found in the
	 *         file.
	 * @throws IOException if any IOException occurs
	 */
	public final static String[] readScript(String pathToScript) throws IOException {
		return readScript(new File(pathToScript));
	}

	/**
	 * Reads the SQL Script file and turns it into a String[] holding the individual
	 * SQL statements.
	 * 
	 * TODO: Ignore comments
	 * 
	 * @param script The script file to be read.
	 * 
	 * @return a String[] containing the individual SQL statements found in the file
	 *         or null if the file is empty.
	 * @throws IOException if any IOException occurs
	 */
	public final static String[] readScript(File script) throws IOException {
		try (InputStream is = new FileInputStream(script);
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr)) {
			StringBuffer sb = new StringBuffer();
			String line;
			while (Objects.nonNull(line = br.readLine().trim())) {
				if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ';') {
					sb.append(" ");
				}
				sb.append(line);
			}
			if (sb.length() > 0) {
				int x = 0;
				if (sb.charAt(sb.length() - 1) == ';') {
					x = 1;
				}
				return sb.subSequence(0, sb.length() - x).toString().split(";");
			}
		}
		throw new IOException("File error. is the file empty?");
	}

//	/**
//	 * Initialize a record using reflection.
//	 * 
//	 * @param <T>          the entity type.
//	 * @param rs           the ResultSet to get the values from.
//	 * @param entityClass  the entity class.
//	 * @param javaNames    the java field names.
//	 * @param mappingTypes the java field types.
//	 * 
//	 * @return the initialized entity object / record.
//	 * 
//	 * @throws InstantiationException
//	 * @throws IllegalAccessException
//	 * @throws IllegalArgumentException
//	 * @throws InvocationTargetException
//	 * @throws NoSuchMethodException
//	 * @throws SecurityException
//	 * @throws SQLException
//	 */
//	public static <T extends Entity<?>> T initializeRecord(ResultSet rs, Class<T> entityClass, String[] javaNames,
//			Class<?>[] mappingTypes) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
//			InvocationTargetException, NoSuchMethodException, SecurityException, SQLException {
//		return initializeRecord(rs, entityClass, javaNames, mappingTypes, 1);
//	}

//	public static <T extends Entity<?>> T initializeRecord(ResultSet rs, Class<T> entityClass, String[] javaNames,
//			Class<?>[] mappingTypes, int startIndex)
//			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
//			NoSuchMethodException, SecurityException, SQLException {
//		T t = entityClass.getDeclaredConstructor().newInstance();
//		for (int i = 0, iDB = startIndex; i < javaNames.length; i++, iDB++) {
//			if (Entity.class.isAssignableFrom(mappingTypes[i])) {
//				continue;
//			}
//			Reflect.setValue(t, javaNames[i], rs.getObject(iDB), mappingTypes[i]);
//		}
//		return t;
//	}

//	
//	/**
//	 * automatically fill in the prepared statement based upon the given arguments.
//	 * 
//	 * @param ps     the PreparedStatement to fill in
//	 * @param source the values to fill in
//	 * @param types  the types corresponding to the values
//	 * 
//	 * @return the next index to continue filling in the prepared statement
//	 * 
//	 * @throws SQLException
//	 */
//	public static int autoFill(PreparedStatement ps, List<?> source, List<?> types) throws SQLException {
//		return autoFill(ps, source, types, 1);
//	}
//
//	/**
//	 * automatically fill in the prepared statement based upon the given arguments.
//	 * 
//	 * @param ps            the PreparedStatement to fill in
//	 * @param source        the values to fill in
//	 * @param types         the types corresponding to the values
//	 * @param startingIndex the index to start filling the prepared statement from
//	 * 
//	 * @return the next index to continue filling in the prepared statement
//	 * 
//	 * @throws SQLException
//	 */
//	public static int autoFill(PreparedStatement ps, List<?> source, List<?> types, int startingIndex)
//			throws SQLException {
//		int iDB = startingIndex;
//		for (int i = 0; i < source.size(); i++) {
//			autoSetPreparedStatementParameter(ps, iDB++, source.get(i), (Class<?>) types.get(i));
//		}
//		return iDB;
//	}
//
	/**
	 * automatically fill in the prepared statement based upon the given arguments.
	 * 
	 * @param ps            the PreparedStatement to fill in
	 * @param source        the values to fill in, may not contain any null values
	 * @param startingIndex the index to start filling the prepared statement from
	 * 
	 * @return the next index to continue filling in the prepared statement
	 * 
	 * @throws SQLException
	 */
	public static int autoFill(TypeMap typeMap, PreparedStatement ps, Object[] source, int startingIndex)
			throws SQLException {
		int iDB = startingIndex;
		for (int i = 0; i < source.length; i++) {
			autoSetPreparedStatementParameter(typeMap, ps, iDB++, Objects.requireNonNull(source[i]),
					source[i].getClass());
		}
		return iDB;
	}

	/**
	 * Automatically set a PreparedStatement parameter
	 * 
	 * @param ps             the PreparedStatement
	 * @param parameterIndex the index of the parameter
	 * @param param          the value to set
	 * @param mappedType     the mapping type
	 * 
	 * @throws SQLException
	 */
	public static final void autoSetPreparedStatementParameter(TypeMap dialect, PreparedStatement ps,
			int parameterIndex, Object param, Class<?> mappedType) throws SQLException {
		autoSetPreparedStatementParameter(dialect, ps, parameterIndex, param, mappedType, null);
	}

	public static final void autoSetPreparedStatementParameter(TypeMap dialect, PreparedStatement ps,
			int parameterIndex, Object param, Class<?> mappedType, String referencedFieldName)
			throws SQLException {
		/*
		 * this always returns VARCHAR... int type =
		 * ps.getParameterMetaData().getParameterType(index);
		 */
		int type = dialect.resolveType(mappedType, referencedFieldName);

		if (Objects.isNull(param)) {
			ps.setNull(parameterIndex, type);
		} else {
			ps.setObject(parameterIndex, param, type);
		}
	}

	/**
	 * Retrieves the last auto-generated id from the given statement
	 * 
	 * @param s the statement that generated an id
	 * 
	 * @return the last generated id if it could be found or -1 otherwise
	 * 
	 * @throws SQLException
	 */
	public static final Long lastInsertIntegerId(Statement s) throws SQLException {
		long x = 0L;
		try (ResultSet rs = s.getGeneratedKeys()) {
			if (rs.next()) {
				x = rs.getLong(1);
			}
		}
		return x;
	}
	
	
}
