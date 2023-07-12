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
package com.github.epimethix.lumicore.orm.sqlite;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.github.epimethix.lumicore.common.orm.IntegerEnum;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.sql.ConnectionFactory;
import com.github.epimethix.lumicore.common.orm.sqlite.Definition;
import com.github.epimethix.lumicore.orm.ORM;
import com.github.epimethix.lumicore.orm.annotation.entity.ImplementationClass;
import com.github.epimethix.lumicore.orm.annotation.field.BigDecimalScale;
import com.github.epimethix.lumicore.orm.annotation.field.Column;

/**
 * Utility class to generalize tasks in the context of database interactions
 * <p>
 * 
 * @author epimethix
 * 
 * @see BigDecimalScale
 *
 */
public final class SQLiteUtils {

	/**
	 * The sqlite connection String to connect to a file
	 */
	private final static String CONNECTION_TO_FILE = "jdbc:sqlite:file:";
	/**
	 * The sqlite connection String to connect to memory
	 */
	private final static String CONNECTION_TO_MEMORY = "jdbc:sqlite::memory:";
	/**
	 * The sqlite connection String to connect to a resource
	 */
	private final static String CONNECTION_TO_RESOURCE = "jdbc:sqlite::resource:";
	/**
	 * The sqlite connection String to connect to a jar
	 */
	private final static String CONNECTION_TO_RESOURCE_JAR = "jdbc:sqlite::resource:jar:";

	/**
	 * A map holding constants of {@link java.sql.Types} mapped to java types for
	 * SQLite
	 */
	private static final Map<Class<?>, Integer> TYPE_MAP = initializeTypeMap();

	private static final Map<Integer, Integer> LUMICORE_TO_SQL_TYPE_MAP = initLumicoreToSQLTypeMap();

	/**
	 * Initializes the {@code Map<Class<?>, Integer} to map all supported java field
	 * types to the {@link java.sql.Types}.
	 * <p>
	 * All types are mapped to either {@link Types#INTEGER}, {@link Types#REAL},
	 * {@link Types#VARCHAR} or {@link Types#BLOB}.
	 * <p>
	 * synchronize with {@link AbstractRepository#getTransform(Field)}.
	 * 
	 * @return the type mapping {@code Map<Class<?>, Integer>}
	 */
	private static Map<Class<?>, Integer> initializeTypeMap() {
		Map<Class<?>, Integer> map = new HashMap<Class<?>, Integer>();
		/*
		 * Types mapped to Types.INTEGER
		 */
		map.put(int.class, Types.INTEGER);
		map.put(Integer.class, Types.INTEGER);
		map.put(long.class, Types.INTEGER);
		map.put(Long.class, Types.INTEGER);
		map.put(boolean.class, Types.INTEGER);
		map.put(Boolean.class, Types.INTEGER);
		map.put(byte.class, Types.INTEGER);
		map.put(Byte.class, Types.INTEGER);
		map.put(short.class, Types.INTEGER);
		map.put(Short.class, Types.INTEGER);
		map.put(char.class, Types.INTEGER);
		map.put(Character.class, Types.INTEGER);
		/*
		 * time types
		 */
		map.put(Instant.class, Types.INTEGER);
		map.put(LocalDate.class, Types.INTEGER);
		map.put(LocalTime.class, Types.INTEGER);
		map.put(LocalDateTime.class, Types.INTEGER);
		/*
		 * Types mapped to Types.REAL
		 */
		map.put(double.class, Types.REAL);
		map.put(Double.class, Types.REAL);
		map.put(float.class, Types.REAL);
		map.put(Float.class, Types.REAL);
		/*
		 * Types mapped to Types.VARCHAR
		 */
		map.put(String.class, Types.VARCHAR);
		map.put(StringBuilder.class, Types.VARCHAR);
		map.put(StringBuffer.class, Types.VARCHAR);
		map.put(File.class, Types.VARCHAR);
		map.put(Path.class, Types.VARCHAR);
//		map.put(Enum.class, Types.VARCHAR);
		/*
		 * types mapped to Types.BLOB
		 */
		map.put(byte[].class, Types.BLOB);
		map.put(char[].class, Types.BLOB);
		map.put(BigInteger.class, Types.BLOB);
		map.put(BigDecimal.class, Types.BLOB);

		return map;
	}

	private final static Map<Integer, Integer> initLumicoreToSQLTypeMap() {
		Map<Integer, Integer> map = new HashMap<>();
		map.put(Definition.TYPE_BLOB, Types.BLOB);
		map.put(Definition.TYPE_INTEGER, Types.INTEGER);
		map.put(Definition.TYPE_INTEGER_PK, Types.INTEGER);
		map.put(Definition.TYPE_INTEGER_PK_AI, Types.INTEGER);
		map.put(Definition.TYPE_REAL, Types.REAL);
		map.put(Definition.TYPE_REAL_PK, Types.REAL);
		map.put(Definition.TYPE_TEXT, Types.VARCHAR);
		map.put(Definition.TYPE_TEXT_PK, Types.VARCHAR);
		map.put(Definition.TYPE_TEXT_PK_UUID, Types.VARCHAR);
		return map;
	}

	/**
	 * Get the SQL type from {@link java.sql.Types} based upon the given java type.
	 * 
	 * @param type the java type / may not be null!
	 * 
	 * @return either one of {@link Types#INTEGER}, {@link Types#REAL},
	 *         {@link Types#VARCHAR} or {@link Types#BLOB} if one could be resolved
	 * 
	 * @throws IllegalArgumentException if the type could not be resolved or the
	 *                                  supplied argument type is null
	 * 
	 * @see java.sql.Types
	 */
	public final static int resolveType(Class<?> type) {
		return resolveType(type, null);
	}

	public final static int resolveType(Class<?> type, String referencedField) {
		if (Objects.isNull(type)) {
			throw new IllegalArgumentException(String.format("ORM.resolveType: type may not be null!"));
		}
		Integer resolvedType = TYPE_MAP.get(type);
		if (Objects.nonNull(resolvedType)) {
			return resolvedType;
		} else if (ORM.isJsonType(type)) {
			return Types.VARCHAR;
		} else if (type.isEnum() && IntegerEnum.class.isAssignableFrom(type)) {
			return Types.INTEGER;
		} else if (type.isEnum()) {
			return Types.VARCHAR;
		} else if (Entity.class.isAssignableFrom(type)) {
			Objects.requireNonNull(referencedField);
			/*
			 * Checked: type is assignable to Entity
			 */
			@SuppressWarnings("unchecked")
			Class<Entity<?>> entityType = (Class<Entity<?>>) type;
			return TYPE_MAP.get(ORM.getReferencedField(entityType, referencedField).getType());
		}
		throw new IllegalArgumentException(
				String.format("Reflect.resolveType: '%s': Operation failed!", type.getSimpleName()));
	}

	public static int getReferencingType(Class<? extends Entity<?>> joinedType, String referencedFieldName) {
		Class<?> cls;
		if (joinedType.isAnnotationPresent(ImplementationClass.class)) {
			cls = joinedType.getAnnotation(ImplementationClass.class).value();
		} else {
			cls = joinedType;
		}
		do {
			Field[] fields = cls.getDeclaredFields();
			for (Field f : fields) {
				if (ORM.getFieldName(f).equals(referencedFieldName)) {
					Column fd = f.getAnnotation(Column.class);
					if (Objects.nonNull(fd) && fd.type() != Definition.TYPE_AUTO) {
						return Definition.getReferencingType(fd.type());
					} else {
						return autoDetectType(f.getType());
					}
				}
			}
		} while (Objects.nonNull(cls = cls.getSuperclass()));
		return Definition.TYPE_NOT_SUPPORTED;
	}

	public final static boolean isMappableType(Class<?> type) {
		return TYPE_MAP.containsKey(type) || type.isEnum() || ORM.isJsonType(type);
	}

	public static int getSQLType(int lumicoreType) {
//		System.out.println(lumicoreType);
//		System.out.println(Definition.TYPE_IN);
		Integer x = LUMICORE_TO_SQL_TYPE_MAP.get(lumicoreType);
//		if (Objects.isNull(x)) {
//			return 0;
//		}
		return x;
	}

	public static int getSQLType(Field field) {
		if (field.isAnnotationPresent(Column.class)) {
			Column fieldDefinition = field.getAnnotation(Column.class);
			if (fieldDefinition.type() != Definition.TYPE_AUTO) {
				return getSQLType(fieldDefinition.type());
			}
		} else if (Entity.class.isAssignableFrom(field.getType())) {
			String referencedFieldName = ORM.getReferencedFieldName(field);
			/*
			 * Checked: field type is assignable to Entity
			 */
			@SuppressWarnings("unchecked")
			Field referencedField = ORM.getReferencedField((Class<? extends Entity<?>>) field.getType(),
					referencedFieldName);
			return getSQLType(referencedField);
		}
		return resolveType(field.getType());
//		return getSQLType(Definition.autoDetectType(field.getType()));
	}

	/**
	 * Tries to automatically auto detect the field type based on its java mapping
	 * type.
	 * 
	 * @param mappingType the java mapping type.
	 * 
	 * @return the Definition.TYPE_... or -1 if the given type is not supported
	 */
	public static int autoDetectType(Class<?> mappingType) {
		try {
			int javaSqlType = resolveType(mappingType);
			switch (javaSqlType) {
			case Types.INTEGER:
				return Definition.TYPE_INTEGER;
			case Types.REAL:
				return Definition.TYPE_REAL;
			case Types.VARCHAR:
				return Definition.TYPE_TEXT;
			case Types.BLOB:
				return Definition.TYPE_BLOB;
			}
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
		}
		return Definition.TYPE_NOT_SUPPORTED;
	}
//	/**
//	 * automatically fill in the prepared statement based upon the given arguments.
//	 * 
//	 * @param ps       the PreparedStatement to fill in
//	 * @param sqlNames the names of the key fields
//	 * @param source   the key containing the values to fill in
//	 * 
//	 * @return the next index to continue filling in the prepared statement
//	 * 
//	 * @throws SQLException
//	 */
//	public static int autoFill(PreparedStatement ps, String[] sqlNames, CompositeKey source) throws SQLException {
//		return autoFill(ps, sqlNames, source, 1);
//	}

//	/**
//	 * automatically fill in the prepared statement based upon the given arguments.
//	 * 
//	 * @param ps            the PreparedStatement to fill in
//	 * @param sqlNames      the names of the key fields
//	 * @param source        the key containing the values to fill in
//	 * @param startingIndex the index to start filling the prepared statement from
//	 * 
//	 * @return the next index to continue filling in the prepared statement
//	 * 
//	 * @throws SQLException
//	 */
//	public static int autoFill(PreparedStatement ps, String[] sqlNames, CompositeKey source, int startingIndex)
//			throws SQLException {
//		int iDB = startingIndex;
//		for (int i = 0; i < sqlNames.length; i++, iDB++) {
//			autoSetPreparedStatementParameter(ps, iDB, source.get(sqlNames[i]), source.getType(sqlNames[i]));
//		}
//		return iDB;
//	}

//	/**
//	 * automatically fill in the prepared statement based upon the given arguments.
//	 * 
//	 * @param ps        the PreparedStatement to fill
//	 * @param javaNames the names of the object fields
//	 * @param types     the mapping types (java field types) of the object fields
//	 * @param source    the object to extract the values from
//	 * 
//	 * @return the next index to continue filling in the prepared statement
//	 * 
//	 * @throws NoSuchMethodException
//	 * @throws SecurityException
//	 * @throws IllegalAccessException
//	 * @throws IllegalArgumentException
//	 * @throws InvocationTargetException
//	 * @throws SQLException
//	 */
//	public static int autoFill(PreparedStatement ps, String[] javaNames, Class<?>[] types, Entity<?> source)
//			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
//			InvocationTargetException, SQLException {
//		return autoFill(ps, javaNames, types, source, 1);
//	}

//	/**
//	 * automatically fill in the prepared statement based upon the given arguments.
//	 * 
//	 * @param ps            the PreparedStatement to fill
//	 * @param javaNames     the names of the object fields
//	 * @param types         the mapping types (java field types) of the object
//	 *                      fields
//	 * @param source        the object to extract the values from
//	 * @param startingIndex the index to start filling the prepared statement from
//	 * 
//	 * @return the next index to continue filling in the prepared statement
//	 * 
//	 * @throws NoSuchMethodException
//	 * @throws SecurityException
//	 * @throws IllegalAccessException
//	 * @throws IllegalArgumentException
//	 * @throws InvocationTargetException
//	 * @throws SQLException
//	 */
//	public static int autoFill(PreparedStatement ps, String[] javaNames, Class<?>[] types, Entity<?> source,
//			int startingIndex) throws NoSuchMethodException, SecurityException, IllegalAccessException,
//			IllegalArgumentException, InvocationTargetException, SQLException {
//		int iDB = startingIndex;
//		for (int i = 0; i < javaNames.length; i++, iDB++) {
//			autoSetPreparedStatementParameter(ps, iDB, Reflect.getValue(source, javaNames[i]), types[i]);
//		}
//		return iDB;
//	}

	/**
	 * Utility class - non-instatiable
	 */
	private SQLiteUtils() {}

	public static String connectToFile(Path dbFile) {
		return CONNECTION_TO_FILE + dbFile.toString();
	}

	public static ConnectionFactory connectToFile(File dbFile, String key) {
		return new ConnectionFactory(CONNECTION_TO_FILE + dbFile.getPath(), "", key);
	}

	public static ConnectionFactory connectToFile(File dbFile) {
		return new ConnectionFactory(CONNECTION_TO_FILE + dbFile.getPath(), "", "");
	}
}
