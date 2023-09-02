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

import static com.github.epimethix.lumicore.common.Reflect.getCollectionTypeFromField;
import static com.github.epimethix.lumicore.common.Reflect.getGetter;
import static com.github.epimethix.lumicore.common.Reflect.getSetter;
import static com.github.epimethix.lumicore.common.Reflect.isAnnotationPresent;
import static com.github.epimethix.lumicore.common.Reflect.typeEquals;
import static com.github.epimethix.lumicore.common.Reflect.typeImplements;
import static com.github.epimethix.lumicore.common.orm.model.Entity.getEntityName;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.Reflect;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.IntegerEnum;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.model.MutableEntity;
import com.github.epimethix.lumicore.common.orm.sql.TypeMap;
import com.github.epimethix.lumicore.common.orm.sqlite.Constraint;
import com.github.epimethix.lumicore.common.orm.sqlite.Constraint.ForeignKeyConstraint;
import com.github.epimethix.lumicore.common.orm.sqlite.Constraint.PrimaryKeyConstraint;
import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.orm.annotation.database.SchemaSync;
import com.github.epimethix.lumicore.orm.annotation.database.SchemaMetadata;
import com.github.epimethix.lumicore.orm.annotation.entity.ImplementationClass;
import com.github.epimethix.lumicore.orm.annotation.entity.Table;
import com.github.epimethix.lumicore.orm.annotation.entity.TableSync;
import com.github.epimethix.lumicore.orm.annotation.field.BigDecimalScale;
import com.github.epimethix.lumicore.orm.annotation.field.Column;
import com.github.epimethix.lumicore.orm.annotation.field.JoinTable;
import com.github.epimethix.lumicore.orm.annotation.field.ManyToMany;
import com.github.epimethix.lumicore.orm.annotation.field.ManyToOne;
import com.github.epimethix.lumicore.orm.annotation.field.OneToMany;
import com.github.epimethix.lumicore.orm.annotation.field.OneToOne;
import com.github.epimethix.lumicore.orm.annotation.field.PrimaryKey;
import com.github.epimethix.lumicore.orm.annotation.field.Unique;
import com.github.epimethix.lumicore.orm.annotation.type.JsonType;
import com.github.epimethix.lumicore.properties.LumicoreProperties;

public final class ORM {

	private static final String STRUCTURE_VERSION = "STRUCTURE_VERSION";

	@FunctionalInterface
	public static interface TransformOperation {
		Object transform(Object in) throws Exception;
	}

	public static class Transform {
		public TransformOperation dbToJava;
		public TransformOperation javaToDb;

		private Transform() {
			// no-op by default
			dbToJava = javaToDb = in -> in;
		}
	}

	/**
	 * A map of the type names of Type
	 * 
	 * @see java.sql.Types
	 */
	private static final Map<Integer, String> TYPE_NAMES = ORM.initializeJavaSqlTypeNames();

	private static <T extends Enum<T>> Transform getEnumTransform(Class<?> mappingType) {
		Transform transform = new Transform();
		transform.javaToDb = in -> in.toString();
		transform.dbToJava = in -> Enum.valueOf((Class<T>) mappingType, (String) in);
		return transform;
	}

	public static Transform getTransform(Field field, final ObjectMapper jsonMapper) {
		final Class<?> mappingType = field.getType();
		Transform transform = new Transform();
		if (ORM.isJsonType(mappingType)) {
			transform.javaToDb = in -> jsonMapper.writeValueAsString(in);
			transform.dbToJava = in -> jsonMapper.readValue((String) in, mappingType);
		} else if (Reflect.typeEquals(mappingType, char[].class)) {
			transform.javaToDb = in -> UTF16.charsToBytes((char[]) in, false);
			transform.dbToJava = in -> UTF16.bytesToChars((byte[]) in, true);
		} else if (Reflect.typeEquals(mappingType, BigDecimal.class)) {
			final int bigDecScale = ORM.getBigDecimalScale(field);
			transform.javaToDb = in -> Big.bigDecToBytes((BigDecimal) in, bigDecScale);
			transform.dbToJava = in -> Big.bytesToBigDec((byte[]) in, bigDecScale);
		} else if (Reflect.typeEquals(mappingType, BigInteger.class)) {
			transform.javaToDb = in -> ((BigInteger) in).toByteArray();
			transform.dbToJava = in -> new BigInteger((byte[]) in);
		} else if (Reflect.typeEqualsOneOf(mappingType, char.class, Character.class)) {
			transform.javaToDb = in -> Character.getNumericValue((Character) in);
			transform.dbToJava = in -> new BigInteger((byte[]) in);
		} else if (Reflect.typeEqualsOneOf(mappingType, boolean.class, Boolean.class)) {
			transform.javaToDb = in -> (Boolean) in ? 1 : 0;
			transform.dbToJava = in -> ((Number) in).intValue() != 0;
		} else if (Reflect.typeEquals(mappingType, Instant.class)) {
			transform.javaToDb = in -> ((Instant) in).toEpochMilli();
			transform.dbToJava = in -> Instant.ofEpochMilli(((Number) in).longValue());
		} else if (Reflect.typeEquals(mappingType, LocalDate.class)) {
			transform.javaToDb = in -> ((LocalDate) in).toEpochDay();
			transform.dbToJava = in -> LocalDate.ofEpochDay(((Number) in).longValue());
		} else if (Reflect.typeEquals(mappingType, LocalTime.class)) {
			transform.javaToDb = in -> ((Integer) (((LocalTime) in).toSecondOfDay())).longValue();
			transform.dbToJava = in -> LocalTime.ofSecondOfDay(((Number) in).longValue());
		} else if (Reflect.typeEquals(mappingType, LocalDateTime.class)) {
			transform.javaToDb = in -> (((LocalDateTime) in).atZone(LumicoreProperties.APPLICATION_TIME_ZONE)
					.toInstant().toEpochMilli());
			transform.dbToJava = in -> LocalDateTime.ofInstant(Instant.ofEpochMilli(((Number) in).longValue()),
					LumicoreProperties.APPLICATION_TIME_ZONE);
		} else if (Reflect.typeEquals(mappingType, StringBuffer.class)) {
			transform.javaToDb = in -> ((StringBuffer) in).toString();
			transform.dbToJava = in -> new StringBuffer((String) in);
		} else if (Reflect.typeEquals(mappingType, StringBuilder.class)) {
			transform.javaToDb = in -> ((StringBuilder) in).toString();
			transform.dbToJava = in -> new StringBuilder((String) in);
		} else if (Reflect.typeEquals(mappingType, File.class)) {
			transform.javaToDb = in -> ((File) in).getPath();
			transform.dbToJava = in -> new File((String) in);
		} else if (Reflect.typeEquals(mappingType, Path.class)) {
			transform.javaToDb = in -> ((Path) in).toString();
			transform.dbToJava = in -> Paths.get((String) in);
		} else if (mappingType.isEnum() && IntegerEnum.class.isAssignableFrom(mappingType)) {
			/*
			 * mappingType.isEnum() == true here
			 */
			@SuppressWarnings("unchecked")
			Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) mappingType;
			transform.javaToDb = in -> ((IntegerEnum) in).toInteger();
			transform.dbToJava = in -> IntegerEnum.ofInteger(((Number) in).longValue(), enumClass);
		} else if (mappingType.isEnum()) {
			transform = getEnumTransform(mappingType);
		}
//		SQLConnectionFactory
		return transform;
	}

	/**
	 * Used to transform a joined entity value into its id/joined column value.
	 * 
	 * @param getter
	 * @return
	 */
	public final static Transform getTransform(final Method getter) {
		Transform transform = new Transform();
		transform.javaToDb = in -> {
			if (Objects.isNull(in))
				return null;
			if (getter.getReturnType() == in.getClass())
				return in;
			return getter.invoke(in);
		};
		return transform;
	}

	/**
	 * Gets the {@link java.sql.Types} type name as String.
	 * 
	 * @param type the {@link java.sql.Types} type.
	 * 
	 * @return The type name String
	 * 
	 * @see java.sql.Types
	 */
	public final static String getTypeName(int type) {
		return TYPE_NAMES.get(type);
	}

	public static Field[] selectEntityFields(Class<?> entityClass) {
		return selectEntityFields(entityClass, false);
	}

	public static Field[] selectEntityFields(Class<?> entityClass, boolean recursive) {
		Field[] fields = entityClass.getDeclaredFields();

		boolean explicitDeclarationNeeded = false;

		if (entityClass.isAnnotationPresent(Table.class)) {
			Table ed = entityClass.getAnnotation(Table.class);
			if (ed.strategy() == Table.STRATEGY_EXPLICIT_DEFINITION) {
				explicitDeclarationNeeded = true;
			}
		}

		List<Field> result = new ArrayList<>();
		do {
			for (Field field : fields) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				} else if (field.isAnnotationPresent(ManyToMany.class)) {
					continue;
				} else if (field.isAnnotationPresent(OneToMany.class)) {
					continue;
				} else if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)
						|| field.isAnnotationPresent(Column.class) || !explicitDeclarationNeeded) {
					result.add(field);
				}
			}
			if (!recursive) {
				break;
			}
		} while (!typeEquals((entityClass = entityClass.getSuperclass()), Object.class));
		return result.toArray(new Field[result.size()]);
	}

	public static Field[] selectJoinTableFields(Class<?> entityClass) {
		return selectJoinTableFields(entityClass, false);
	}

	public static Field[] selectJoinTableFields(Class<?> entityClass, boolean recursive) {
		List<Field> result = new ArrayList<>();
		do {
			Field[] fields = entityClass.getDeclaredFields();
			for (Field field : fields) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				} else if (field.isAnnotationPresent(ManyToMany.class) && field.isAnnotationPresent(JoinTable.class)) {
					result.add(field);
				}
			}
			if (!recursive) {
				break;
			}
		} while (!typeEquals((entityClass = entityClass.getSuperclass()), Object.class));
		return result.toArray(new Field[result.size()]);
	}

	public static String getFieldName(Field f) {
		if (f.isAnnotationPresent(Column.class)) {
			Column fd = f.getAnnotation(Column.class);
			if (!fd.name().trim().isEmpty()) {
				return fd.name();
			}
		}
		if (f.isAnnotationPresent(OneToOne.class)) {
			OneToOne oto = f.getAnnotation(OneToOne.class);
			if (!oto.fieldName().isBlank()) {
				return oto.fieldName();
			}
		}
		if (f.isAnnotationPresent(ManyToOne.class)) {
			ManyToOne mto = f.getAnnotation(ManyToOne.class);
			if (!mto.fieldName().isBlank()) {
				return mto.fieldName();
			}
		}
//		if (Entity.class.isAssignableFrom(f.getType())) {
//			String name1 = f.getName();
//			String name2 = getReferencedFieldName(f);
//			return concatenateNames(name1, name2);
//		}
		return f.getName();
	}

	public static Field getReferencedField(Class<? extends Entity<?>> referencedEntity, String referencedField) {
		if (referencedEntity.isInterface()) {
			if (referencedEntity.isAnnotationPresent(ImplementationClass.class)) {
				referencedEntity = (Class<? extends Entity<?>>) referencedEntity
						.getAnnotation(ImplementationClass.class).value();
			} else {

				throw new RuntimeException(
						String.format("Could not get referenced implementation class for lazy entity '%s'!",
								getEntityName(referencedEntity)));
			}
		}
		Class<?> cls = referencedEntity;
		do {
			Field[] fields = cls.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				if (getFieldName(fields[i]).equals(referencedField)) {
					return fields[i];
				}
			}
		} while (Objects.nonNull(cls = cls.getSuperclass()));
		throw new RuntimeException(String.format("Could not get referenced field %s.%s!",
				getEntityName(referencedEntity), referencedField));
//		return null;
	}

	public static String concatenateNames(String name1, String name2) {
		Objects.requireNonNull(name1);
		Objects.requireNonNull(name2);
		if (name1.length() == 0) {
			throw new IllegalArgumentException("concatenateNames: name1 must not be empty!");
		}
		if (name2.length() == 0) {
			throw new IllegalArgumentException("concatenateNames: name2 must not be empty!");
		}
		if (name2.length() == 1) {
			return String.format("%s%s", name1, name2.toUpperCase());
		} else {
			return String.format("%s%s%s", name1, name2.substring(0, 1).toUpperCase(), name2.substring(1));
		}
	}

//	public static String getDefinedFieldName(Field f) {
//		if (f.isAnnotationPresent(FieldDefinition.class)) {
//			FieldDefinition fd = f.getAnnotation(FieldDefinition.class);
//			if (!fd.name().isBlank()) {
//				return fd.name();
//			}
//		}
//		if (f.isAnnotationPresent(OneToOne.class)) {
//			OneToOne oto = f.getAnnotation(OneToOne.class);
//			if (!oto.fieldName().isBlank()) {
//				return oto.fieldName();
//			}
//		}
//		if (f.isAnnotationPresent(ManyToOne.class)) {
//			ManyToOne mto = f.getAnnotation(ManyToOne.class);
//			if (!mto.fieldName().isBlank()) {
//				return mto.fieldName();
//			}
//		}
//		return null;
//	}

	public static String getReferencedFieldName(Field field) {
		String foreignKey = "";
		if (Entity.class.isAssignableFrom(field.getType())) {
			if (field.isAnnotationPresent(ManyToOne.class)) {
				foreignKey = field.getAnnotation(ManyToOne.class).referencedFieldName();
			} else if (field.isAnnotationPresent(OneToOne.class)) {
				foreignKey = field.getAnnotation(OneToOne.class).referencedFieldName();
			}
			if (foreignKey.trim().isEmpty()) {
				try {
					foreignKey = (String) field.getType().getField("ID").get(null);
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException
						| ClassCastException e) {
					e.printStackTrace();
					throw new RuntimeException(String.format("Entity[%s] is missing \"public static final String ID\"",
							field.getType().getSimpleName()));
				}
			}
		}
		return foreignKey;
	}

	/**
	 * Gets the value of the public static long STRUCTURE_VERSION of the specified
	 * database class.
	 * 
	 * @param dbClass the database class
	 * 
	 * @return the value of DB.STRUCTURE_VERSION (or its hider).
	 * 
	 * @see Database#STRUCTURE_VERSION
	 */
	public static final long getDatabaseStructureVersion(Class<? extends Database> dbClass) {
		try {
			Field f = dbClass.getField(STRUCTURE_VERSION);
			return f.getLong(null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return 0L;
	}

	/**
	 * Gets the entities specified structure version (0 (Zero) by default as
	 * specified in the {@link Entity} interface)
	 * 
	 * @param entityClass the entity class to examine.
	 * 
	 * @return the entities structure version (public static long STRUCTURE_VERSION)
	 *         or its hider
	 * 
	 * @see Entity#STRUCTURE_VERSION
	 */
	public static final long getEntityStructureVersion(Class<? extends Entity<?>> entityClass) {
		try {
			Field f = entityClass.getField(STRUCTURE_VERSION);
			return f.getLong(null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return 0L;
	}
//
//	/**
//	 * Gets the key size of a composite key class.
//	 * 
//	 * @param compositeKeyClass a class that extends CompositeKey
//	 * 
//	 * @return the key size (size &gt; 1) or -1 if there is any problem
//	 * 
//	 * @see CompositeKey#KEY_SIZE
//	 * 
//	 * @see DualKey#KEY_SIZE
//	 * @see TripleKey#KEY_SIZE
//	 * @see QuadrupleKey#KEY_SIZE
//	 * @see QuintupleKey#KEY_SIZE
//	 */
//	public static int getKeySize(Class<?> compositeKeyClass) {
//		try {
//			Field f = compositeKeyClass.getField("KEY_SIZE");
//			return f.getInt(null);
//		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {}
//		return -1;
//	}

//	public static ManagedRepositories getManagedRepositories(Class<? extends AbstractDatabaseApplication> appClass) {
//		ManagedRepositories mr = appClass.getAnnotation(ManagedRepositories.class);
//		return mr;
//	}

	/**
	 * Checks if the database is configured to create an UUID for the file on
	 * initial deployment.
	 * 
	 * @param databaseClass the database class
	 * 
	 * @return true if an UUID should be generated on initial deployment.
	 */
	public static boolean isDatabaseUnique(Class<? extends Database> databaseClass) {
		SchemaMetadata dd = databaseClass.getAnnotation(SchemaMetadata.class);
		if (Objects.nonNull(dd)) {
			return dd.unique();
		} else {
			return false;
		}
	}

	/**
	 * Checks if the database is configured to create time-stamps for the file on
	 * initial deployment.
	 * 
	 * @param databaseClass the database class
	 * 
	 * @return true if time-stamps should be created on initial deployment.
	 */
	public static boolean isDatabaseTimestamped(Class<? extends Database> databaseClass) {
		SchemaMetadata dd = databaseClass.getAnnotation(SchemaMetadata.class);
		if (Objects.nonNull(dd)) {
			return dd.initializeTimeStamps();
		} else {
			return false;
		}
	}

	/**
	 * Gets the database class's &#64;{@link SchemaSync} annotation if there is one
	 * 
	 * @param dbClass the class extending AbstractDB
	 * 
	 * @return the AutoSyncDatabase annotation if any, null otherwise
	 * 
	 * @see SchemaSync
	 */
	public static final SchemaSync getDatabaseAutoSyncConfig(Class<? extends Database> dbClass) {
		return dbClass.getAnnotation(SchemaSync.class);
	}

	/**
	 * Gets the AutoSyncEntity annotation if any.
	 * 
	 * @param entityClass the entity class
	 * 
	 * @return the AutoSyncEntity annotation or null if there is none
	 * 
	 * @see TableSync
	 */
	public static final TableSync getEntityAutoSyncConfig(Class<? extends Entity<?>> entityClass) {
		return entityClass.getAnnotation(TableSync.class);
	}

	/**
	 * Tests if the supplied entityClass is configured to soft-delete
	 * 
	 * @param entityClass the entity class to test
	 * 
	 * @return true if soft delete is enabled
	 */
	public static boolean isSoftDeleteEnabled(Class<? extends Entity<?>> entityClass) {
		Table ed = entityClass.getAnnotation(Table.class);
		if (Objects.nonNull(ed)) {
			return ed.enableSoftDelete();
		} else {
			return false;
		}
	}

	/**
	 * Tests whether the given type is annotated with &#64;{@link JsonType}
	 * 
	 * @param type the type to test
	 * 
	 * @return true if json type
	 */
	public static boolean isJsonType(Class<?> type) {
		return isAnnotationPresent(type, JsonType.class, true);
	}

	/**
	 * gets the powerOfTen() of the annotated &#64;{@link BigDecimalScale}
	 * 
	 * @param fieldName the BigDecimal field to examine.
	 * 
	 * @return the annotated &#64;{@link BigDecimalScale} if present;
	 *         {@code SQLUtil.DEFAULT_SCALE} as configured otherwise.
	 */
	public static int getBigDecimalScale(Object o, String fieldName) {
		try {
			return getBigDecimalScale(o.getClass().getDeclaredField(fieldName));
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return LumicoreProperties.DEFAULT_BIGDECIMAL_SCALE;
	}

	public static int getBigDecimalScale(Field field) {
		BigDecimalScale bds;
		bds = field.getAnnotation(BigDecimalScale.class);
		if (Objects.nonNull(bds)) {
			return bds.powerOfTen();
		}
		return LumicoreProperties.DEFAULT_BIGDECIMAL_SCALE;
	}

//	public static int getEntityIdType(Class<?> entityClass) {
//		Type[] genericInterfaces = entityClass.getGenericInterfaces();
//		for (int i = 0; i < genericInterfaces.length; i++) {
//			try {
//				if (genericInterfaces[i].getTypeName().startsWith(Entity.class.getName())) {
//					ParameterizedType pt = (ParameterizedType) genericInterfaces[i];
////					System.out.println(pt.getActualTypeArguments()[0].getTypeName());
//					Class<?> idType = Class.forName(pt.getActualTypeArguments()[0].getTypeName());
//					return resolveType(idType);
//				}
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
//		return 0;
//	}

	public static boolean entityDependsOn(Class<? extends Entity<?>> a, Class<? extends Entity<?>> b) {
		Field[] fields = a.getDeclaredFields();
		for (Field f : fields) {
//			if(f.getType().getSimpleName().equals("AccountManager")) {
//				System.err.println("HALT");
//			}
			if (f.isAnnotationPresent(ManyToOne.class)
					|| (f.isAnnotationPresent(OneToOne.class) && !f.getAnnotation(OneToOne.class).lazy())) {
				if (f.getType().isInterface()) {
					Class<? extends Entity<?>> ic = getImplementationClass(f.getType());
					if (typeEquals(ic, b)) {
						return true;
					}
				} else if (typeEquals(f.getType(), b)) {
					return true;
				}
			} else if (f.isAnnotationPresent(ManyToMany.class) && f.isAnnotationPresent(JoinTable.class)) {
				if (typeEquals(f.getType(), b)) {
					return true;
				}
			} else if (Modifier.isStatic(f.getModifiers()) && typeEquals(ForeignKeyConstraint.class, f.getType())) {
				try {
					if (!f.canAccess(null)) {
						f.setAccessible(true);
					}
					ForeignKeyConstraint fkc = (ForeignKeyConstraint) f.get(null);
					if (Objects.nonNull(fkc)) {
						if (fkc.getForeignEntity().equals(getEntityName(b))) {
							return true;
						}
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	private static Class<? extends Entity<?>> getImplementationClass(Class<?> type) {
		return (Class<? extends Entity<?>>) type.getAnnotation(ImplementationClass.class).value();
	}

	/**
	 * Checks if the supplied entity class implements all required getters/setters.
	 * 
	 * @param entityClass the entity class to test.
	 * @param fieldNames  all entity field names.
	 * @param fieldTypes  all entity field mapping type classes
	 * 
	 * @throws ConfigurationException if the empty constructor is missing.
	 * @throws ConfigurationException if an unsupported field type is used.
	 * @throws ConfigurationException if there is any getter missing.
	 * @throws ConfigurationException if there is any setter missing.
	 */
	public static void checkEntityIntegrity(Class<? extends Entity<?>> entityClass, List<String> fieldNames,
			List<Class<?>> fieldTypes, TypeMap typeMap) throws ConfigurationException {
		Iterator<Class<?>> iTypes = fieldTypes.iterator();
		outer0: for (String fieldName : fieldNames) {
			Class<?> type = iTypes.next();
			boolean isMappable = false;
			if (Entity.class.isAssignableFrom(type)) {
//				try {
				String referencedFieldName = getReferencedFieldName(getField(entityClass, fieldName));
				/*
				 * Checked: type is assignable to Entity
				 */
				@SuppressWarnings("unchecked")
				Field referencedField = getReferencedField((Class<? extends Entity<?>>) type, referencedFieldName);
				isMappable = typeMap.isMappableType(referencedField.getType());
//				} catch (NoSuchFieldException | SecurityException e) {
//					e.printStackTrace();
//				}
			} else {
				isMappable = typeMap.isMappableType(type);
			}
			if (!isMappable) {
				throw new ConfigurationException(ConfigurationException.UNSUPPORTED_TYPE_USED,
						getEntityName(entityClass), fieldName, type.getSimpleName());
			}
			try {
				getGetter(entityClass, fieldName);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new ConfigurationException(ConfigurationException.ENTITY_GETTER_MISSING,
						getEntityName(entityClass), fieldName);
			}
			if (MutableEntity.class.isAssignableFrom(entityClass)) {
				try {
					getSetter(entityClass, fieldName, type);
				} catch (NoSuchMethodException | SecurityException e) {
					throw new ConfigurationException(ConfigurationException.ENTITY_SETTER_MISSING,
							getEntityName(entityClass), fieldName);
				}
			} else {
				Class<?>[] members = entityClass.getDeclaredClasses();
				for (Class<?> m : members) {
					if (m.getSimpleName().equals("Builder")) {
						try {
							getSetter(m, fieldName, type);
							continue outer0;
						} catch (NoSuchMethodException | SecurityException e) {
							break;
						}
					}
				}
				throw new ConfigurationException(ConfigurationException.ENTITY_SETTER_MISSING,
						getEntityName(entityClass) + ".Builder", fieldName);
			}
		}
		Class<?> cls = entityClass;
		do {
			Field[] ff = cls.getDeclaredFields();
			outer: for (Field f : ff) {
				String fieldName = f.getName();
				if (f.isAnnotationPresent(OneToMany.class) || f.isAnnotationPresent(ManyToMany.class)) {
					if (Modifier.isStatic(f.getModifiers())) {
						throw new ConfigurationException(ConfigurationException.MTM_FIELD_IS_STATIC,
								getEntityName(entityClass), fieldName);
					} else if (!List.class.isAssignableFrom(f.getType())) {
						throw new ConfigurationException(ConfigurationException.MTM_COLLECTION_TYPE_IS_NOT_SUPPORTED,
								getEntityName(entityClass), fieldName);
					} else {
						Class<?> collectionType = getCollectionTypeFromField(f);
						if (Objects.isNull(collectionType) || !Entity.class.isAssignableFrom(collectionType)) {
							throw new ConfigurationException(
									ConfigurationException.MTM_COLLECTION_TYPE_IS_NOT_ASSIGNABLE_TO_ENTITY,
									getEntityName(entityClass), fieldName);
						} else {
							Class<?> type = f.getType();
							try {
								getGetter(entityClass, fieldName);
							} catch (NoSuchMethodException | SecurityException e) {
								throw new ConfigurationException(ConfigurationException.ENTITY_GETTER_MISSING,
										getEntityName(entityClass), fieldName);
							}
							if (MutableEntity.class.isAssignableFrom(entityClass)) {
								try {
									getSetter(entityClass, fieldName, type);
								} catch (NoSuchMethodException | SecurityException e) {
									throw new ConfigurationException(ConfigurationException.ENTITY_SETTER_MISSING,
											getEntityName(entityClass), fieldName);
								}
							} else {
								Class<?>[] members = entityClass.getDeclaredClasses();
								for (Class<?> m : members) {
									if (m.getSimpleName().equals("Builder")) {
										try {
											getSetter(m, fieldName, type);
											continue outer;
										} catch (NoSuchMethodException | SecurityException e) {
											break;
										}
									}
								}
								throw new ConfigurationException(ConfigurationException.ENTITY_SETTER_MISSING,
										getEntityName(entityClass) + ".Builder", fieldName);
							}
						}
					}
				}
			}

		} while (!Reflect.typeEquals(cls = cls.getSuperclass(), Object.class));
	}

	private static Field getField(Class<? extends Entity<?>> entityClass, String fieldName) {
		// TODO Auto-generated method stub
		// entityClass.getDeclaredField(fieldName)
		Class<?> testClass = entityClass;
		for (;;) {
			try {
				Field f = null;
				f = testClass.getDeclaredField(fieldName);
				return f;
			} catch (NoSuchFieldException | SecurityException e) {
				testClass = testClass.getSuperclass();
				if (Reflect.typeEquals(testClass, Object.class)) {
					break;
				}
			}
		}
		throw new RuntimeException(String.format("Field not found: %s.%s", getEntityName(entityClass), fieldName));
	}

	/**
	 * scans the given entity class and all super classes for Constraint fields
	 * 
	 * @param entityClass the entity class to scan
	 * @param constraints the constraints list to populate
	 * @param entityName  the entity name for exception clarification
	 * 
	 * @return the PrimaryKeyConstraint if one was found, null otherwise
	 * 
	 * @throws ConfigurationException if any unexpected problem occurs
	 * 
	 * @see Constraint
	 */
	public static PrimaryKeyConstraint scanForConstraints(Class<?> entityClass, List<Constraint> constraints,
			String entityName) throws ConfigurationException {
		PrimaryKeyConstraint pkc = null;
		List<String> pkcAnnotated = new ArrayList<String>();
		do {
			Field[] declaredFields = entityClass.getDeclaredFields();
			for (Field field : declaredFields) {

				Class<?> type = field.getType();
				if (field.isAnnotationPresent(PrimaryKey.class) || field.getName().equals(Entity.ID)) {
					pkcAnnotated.add(getFieldName(field));
				} else if (field.isAnnotationPresent(Unique.class)) {
					Unique u = field.getAnnotation(Unique.class);
					String fieldName = getFieldName(field);
					boolean selfIsIncludedInAnnotation = false;
					for (int i = 0; i < u.include().length; i++) {
						if (u.include()[i].equals(fieldName)) {
							selfIsIncludedInAnnotation = true;
							break;
						}
					}
					if (selfIsIncludedInAnnotation) {
						constraints.add(Constraint.uniqueConstraint(u.include()));
					} else {
						String[] uniqueFields = new String[u.include().length + 1];
						uniqueFields[0] = fieldName;
						for (int i = 1; i < uniqueFields.length; i++) {
							uniqueFields[i] = u.include()[i - 1];
						}
						constraints.add(Constraint.uniqueConstraint(uniqueFields));
					}
				}
				if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
					if (Entity.class.isAssignableFrom(field.getType())) {
						String key = getFieldName(field);
						String foreignKey = getReferencedFieldName(field);
						/*
						 * Checked: field type is assignable to Entity
						 */
						@SuppressWarnings("unchecked")
						Class<? extends Entity<?>> foreignEntityClass = (Class<? extends Entity<?>>) field.getType();
						constraints.add(Constraint.foreignKeyConstraint(key, foreignEntityClass, foreignKey));
					}
				}
				if (Modifier.isStatic(field.getModifiers())
						&& (typeImplements(type, Constraint.class) || typeEquals(type, Constraint.class))) {
					Object constraint = null;
					try {
						if (!field.canAccess(null)) {
							field.setAccessible(true);
						}
						constraint = field.get(null);
						if (Objects.nonNull(constraint)) {
							constraints.add((Constraint) constraint);
							if (typeEquals(type, PrimaryKeyConstraint.class) && Objects.isNull(pkc)) {
								pkc = (PrimaryKeyConstraint) constraint;
							}
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
					if (Objects.isNull(constraint)) {
						throw new ConfigurationException(ConfigurationException.COULD_NOT_READ_CONSTRAINT, entityName,
								field.getName());
					}
				}
			}
		} while (Objects.nonNull(entityClass = entityClass.getSuperclass()));
		if (Objects.isNull(pkc) && !pkcAnnotated.isEmpty()) {
			String[] pkcAnnotatedArray = pkcAnnotated.toArray(new String[pkcAnnotated.size()]);
			pkc = Constraint.primaryKeyConstraint(pkcAnnotatedArray);
		}
		return pkc;
	}

	/**
	 * initializes a {@code Map<Integer, String>} containing the type names from
	 * {@link java.sql.Types}.
	 * 
	 * @return a {@code Map<Integer, String>} containing the type names from
	 *         {@link java.sql.Types}.
	 */
	public static Map<Integer, String> initializeJavaSqlTypeNames() {
		Map<Integer, String> typeNames = new HashMap<Integer, String>();
		for (Field f : Types.class.getFields()) {
			try {
				typeNames.put((Integer) f.get(null), f.getName());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return typeNames;
	}

	public static Object initEntityFromParameterizedType(Class<?> repoClass) {
		Class<?> type = null;
		ParameterizedType pt = (ParameterizedType) repoClass.getGenericSuperclass();
		Type[] actualTypeArgs = pt.getActualTypeArguments();
		int counter = 0;
		for (Type actualType : actualTypeArgs) {
			try {
				type = Class.forName(actualType.getTypeName());
				if (Entity.class.isAssignableFrom(type)) {
					break;
				} else {
					type = null;
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			if (++counter == 2) {
				System.err.println("initEntityFromParameterizedType: second round");
			}
		}
		if (Objects.isNull(type)) {
			throw new RuntimeException(
					String.format("[%s]: Entity type was not found in type arguments", repoClass.getSimpleName()));
		}
		Constructor<?>[] constructors = type.getConstructors();
		Constructor<?> constructor = null;
		for (Constructor<?> c : constructors) {
			if (c.getParameterCount() == 0) {
				constructor = c;
				break;
			}
		}
		if (Objects.isNull(constructor)) {
			throw new RuntimeException(
					String.format("Entity class [%s] needs an empty constructor!", type.getSimpleName()));
		}
		Object returnObject = null;
		try {
			returnObject = constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
//			throw e;
		}
		return returnObject;
	}

	public static void sortEntityClasses(List<Class<? extends Entity<?>>> entityClasses) throws ConfigurationException {
		Collections.sort(entityClasses, (a, b) -> {
			String nameA = getEntityName(a);
			String nameB = getEntityName(b);
			return nameA.toLowerCase().compareTo(nameB.toLowerCase());
		});
		// list to track the number of move operations to detect circular references
		List<Integer> movedList = new ArrayList<Integer>();
		for (int i = 0; i < entityClasses.size(); i++) {
			movedList.add(Integer.valueOf(0));
		}
		for (int i = entityClasses.size() - 2; i > -1; i--) {
			Class<? extends Entity<?>> x = entityClasses.get(i);
			for (int j = i + 1; j < entityClasses.size(); j++) {
				Class<? extends Entity<?>> y = entityClasses.get(j);
				if (entityDependsOn(x, y)) {
					int moved = movedList.get(j) + 1;
					if (moved > entityClasses.size()) {
						for (int k = 0; k < entityClasses.size(); k++) {
							System.err.printf("[%s] was moved %d times%n", entityClasses.get(k).getSimpleName(),
									movedList.get(k));
						}
						throw new ConfigurationException(ConfigurationException.CIRCULAR_FOREIGN_KEY_CONSTRUCT_DETECTED,
								getEntityName(entityClasses.get(j)));
					}
					Class<? extends Entity<?>> tmp = entityClasses.get(j);
					entityClasses.remove(j);
					movedList.remove(j);
					entityClasses.add(i, tmp);
					movedList.add(i, moved);
					i++;
				}
			}
		}
		int putPosition = 0;
		String name;
		for (int i = 0; i < entityClasses.size(); i++) {
			name = getEntityName(entityClasses.get(i));
			if (name.startsWith("lumicore_")) {
				Class<? extends Entity<?>> tmp = entityClasses.get(i);
				entityClasses.remove(i);
				entityClasses.add(putPosition++, tmp);
			}
		}
//		name = getEntityName(Meta.class);
		name = "lumicore_meta";
		for (int i = 0; i < entityClasses.size(); i++) {
			if (name.equals(getEntityName(entityClasses.get(i)))) {
				if (i > 0) {
					Class<? extends Entity<?>> tmp = entityClasses.get(i);
					entityClasses.remove(i);
					entityClasses.add(0, tmp);
					break;
				}
			}
		}
	}

	public static String getParentFieldName(Class<?> cls) {
		do {
			try {
				Field f = cls.getDeclaredField("parent");
				return getFieldName(f);
			} catch (NoSuchFieldException | SecurityException e) {}
		} while (Objects.nonNull(cls = cls.getSuperclass()));
		return null;
	}

	public static void setValue(Object item, DBControl<?> control) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		setValue(item, control, null);
	}

	public static void setValue(Object item, DBControl<?> control, Function<Object, Object> transformation)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		/*
		 * Checked: item is Entity
		 */
		@SuppressWarnings("unchecked")
		Field fSet = getReferencedField((Class<? extends Entity<?>>) item.getClass(), control.getFieldName());
//		Field fSet = referencedField;
		@SuppressWarnings("unchecked")
		Method setter = getSetter((Class<? extends Entity<?>>) item.getClass(), fSet.getName(), fSet.getType());
		// TODO check unsafe operation: setter.invoke(item, control.getValue());
		try {
			if (Objects.nonNull(transformation)) {
				setter.invoke(item, transformation.apply(control.getValue()));
			} else {

				setter.invoke(item, control.getValue());
			}
		} catch (IllegalArgumentException e) {
			System.err.println(item.getClass().getSimpleName() + "." + fSet.getName() + " [" + fSet.getType()
					+ "] cannot be set to [" + control.getValue().getClass().getName() + "] " + e.getMessage()
					+ " / item class " + item.getClass().getName() + " / setter class "
					+ setter.getDeclaringClass().getName() + " / setter name " + setter.getName());
			throw e;
		}
	}

	public static <T> void getValue(Entity<?> item, DBControl<T> control) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		getValue(item, control, null);
	}

	public static <T> void getValue(Entity<?> item, DBControl<T> control, Function<Object, Object> transformation)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		/*
		 * Checked: item is Entity
		 */
		@SuppressWarnings("unchecked")
		Field fGet = getReferencedField((Class<? extends Entity<?>>) item.getClass(), control.getFieldName());
//		Field fGet = referencedField;
		@SuppressWarnings("unchecked")
		Method getter = Reflect.getGetter((Class<? extends Entity<?>>) item.getClass(), fGet.getName());
		// TODO check unsafe operation: control.setValue((T) getter.invoke(item));
		if (Objects.nonNull(transformation)) {
			control.setValue((T) transformation.apply(getter.invoke(item)));
		} else {
			control.setValue((T) getter.invoke(item));
		}
	}

	/**
	 * Utility class for handling BigDecimal values
	 */
	public final static class Big {
		/**
		 * Converts the given byte[] to BigDecimal using
		 * {@link LumicoreProperties#DEFAULT_BIGDECIMAL_SCALE}.
		 * 
		 * @param bytes the byte[] representation of a BigDecimal (BigInteger) value.
		 * 
		 * @return the BigDecimal value represented by the given byte[]:
		 *         {@code new BigDecimal(new BigInteger(bytes), scale)}
		 * 
		 * @see LumicoreProperties#DEFAULT_BIGDECIMAL_SCALE
		 */
		public static final BigDecimal bytesToBigDec(byte[] bytes) {
			return bytesToBigDec(bytes, LumicoreProperties.DEFAULT_BIGDECIMAL_SCALE);
		}

		/**
		 * Converts the given byte[] to BigDecimal.
		 * 
		 * @param bytes the byte[] representation of a BigDecimal (BigInteger) value.
		 * @param scale the scale to use.
		 * 
		 * @return the BigDecimal value represented by the given byte[]:
		 *         {@code new BigDecimal(new BigInteger(bytes), scale)}
		 */
		public static final BigDecimal bytesToBigDec(byte[] bytes, int scale) {
			return new BigDecimal(new BigInteger(bytes), scale);
		}

		/**
		 * Converts the given BigDecimal to bytes using
		 * {@link LumicoreProperties#DEFAULT_BIGDECIMAL_SCALE}.
		 * 
		 * @param bd the BigDecimal value.
		 * 
		 * @return the byte[] representation of the given value.
		 * 
		 * @see LumicoreProperties#DEFAULT_BIGDECIMAL_SCALE
		 */
		public static final byte[] bigDecToBytes(BigDecimal bd) {
			return bigDecToBytes(bd, LumicoreProperties.DEFAULT_BIGDECIMAL_SCALE);
		}

		/**
		 * Converts the given BigDecimal to bytes using
		 * {@link LumicoreProperties#DEFAULT_BIGDECIMAL_SCALE}.
		 * 
		 * @param bd the BigDecimal value.
		 * 
		 * @return the byte[] representation of the given value.
		 * 
		 * @see LumicoreProperties#DEFAULT_BIGDECIMAL_SCALE
		 */
		public static final byte[] bigDecToBytes(BigDecimal bd, int scale) {
			return bd.scaleByPowerOfTen(scale).toBigInteger().toByteArray();
		}

		/**
		 * Utility class - non-instantiable
		 */
		private Big() {}
	}

	/**
	 * Utility Class for byte[] to char[] to byte[] conversion using UTF16
	 */
	public final static class UTF16 {
		/**
		 * Convert a char[] to a UTF16 byte[]
		 * 
		 * @param chars the char[] to convert
		 * @param clear true to clear the incoming char[] after conversion
		 * 
		 * @return the UTF16 byte[] representation of the given char[]
		 */
		public static final byte[] charsToBytes(char[] chars, boolean clear) {
			try {
				CharBuffer cb = CharBuffer.wrap(chars);
				ByteBuffer bb = StandardCharsets.UTF_16.encode(cb);
				try {
					byte[] bytes = new byte[bb.remaining()];
					bb.get(bytes);
//					LOGGER.trace("c2b :: len-in:{} :: len-out:{} (+2 Bytes BOM)", chars.length, bytes.length);
					return bytes;
				} finally {
					Arrays.fill(bb.array(), (byte) 0);
				}
			} finally {
				if (clear) {
					Arrays.fill(chars, (char) 0);
				}
			}
		}

		/**
		 * Convert an UTF16 byte[] to a char[]
		 * 
		 * @param bytes the UTF16 bytes
		 * @param clear true to clear the incoming byte[] after conversion
		 * 
		 * @return the char[] represented by the supplied UTF16 byte[]
		 */
		public static final char[] bytesToChars(byte[] bytes, boolean clear) {
			try {
				// /2: 2 bytes per UTF16 char, -1: BOM
				char[] chars = new char[bytes.length / 2 - 1];
				StandardCharsets.UTF_16.newDecoder().decode(ByteBuffer.wrap(bytes), CharBuffer.wrap(chars), true);
//				LOGGER.trace("b2c :: len-in:{} :: len-out:{}", bytes.length, chars.length);
				return chars;
			} finally {
				if (clear) {
					Arrays.fill(bytes, (byte) 0);
				}
			}
		}

		/**
		 * Utility class - non-instatiable
		 */
		private UTF16() {}
	}

	private ORM() {}

	public static boolean isEager(Field field) {
		if (field.isAnnotationPresent(OneToOne.class)) {
			return !field.getAnnotation(OneToOne.class).lazy();
		}
		if (field.isAnnotationPresent(ManyToOne.class)) {
			return !field.getAnnotation(ManyToOne.class).lazy();
		}
		if (field.isAnnotationPresent(OneToMany.class)) {
			return !field.getAnnotation(OneToMany.class).lazy();
		}
		if (field.isAnnotationPresent(ManyToMany.class)) {
			return !field.getAnnotation(ManyToMany.class).lazy();
		}
		return false;
	};
}
