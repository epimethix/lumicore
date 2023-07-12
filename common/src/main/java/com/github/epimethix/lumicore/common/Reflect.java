/*
 * Copyright 2021-2023 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.model.Log;
//import com.github.epimethix.lumicore.common.orm.model.MutableEntity;
//import com.github.epimethix.lumicore.common.orm.sqlite.Constraint;
//import com.github.epimethix.lumicore.common.orm.sqlite.SQLiteUtils;
//import com.github.epimethix.lumicore.common.orm.sqlite.Constraint.ForeignKeyConstraint;
//import com.github.epimethix.lumicore.common.orm.sqlite.Constraint.PrimaryKeyConstraint;
import com.github.epimethix.lumicore.common.swing.DBControl;
import com.github.epimethix.lumicore.common.swing.EntityEditor;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.ioc.annotation.Autowired;
import com.github.epimethix.lumicore.ioc.annotation.Component;
import com.github.epimethix.lumicore.ioc.annotation.InterceptAfterCall;
import com.github.epimethix.lumicore.ioc.annotation.InterceptAllowCaller;
import com.github.epimethix.lumicore.ioc.annotation.InterceptBeforeCall;
import com.github.epimethix.lumicore.ioc.annotation.Service;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;
import com.github.epimethix.lumicore.orm.annotation.database.SchemaSync;
import com.github.epimethix.lumicore.orm.annotation.database.SchemaMetadata;
import com.github.epimethix.lumicore.orm.annotation.entity.TableSync;
import com.github.epimethix.lumicore.orm.annotation.entity.Table;
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

public final class Reflect {

	/**
	 * Buffer for storing getters
	 */
	private final static Map<String, Method> GETTER_CACHE = new HashMap<>();
	/**
	 * Buffer for storing getters
	 */
	private final static Map<String, Method> SETTER_CACHE = new HashMap<>();
	/**
	 * The format uset to create all Getter/Setter method names
	 */
	private static final String FORMAT = "%s%s%s";
	/**
	 * String "get" used for getter method name
	 */
	private static final String GET = "get";
	/**
	 * String "is" used for boolean getter method name
	 */
	private static final String IS = "is";
	/**
	 * String "set" used for setter method name
	 */
	private static final String SET = "set";
	/**
	 * Wrapper and primitive type pairs
	 */
	// @formatter:off
	private static final Class<?>[][] WRAPPER_TYPES = new Class<?>[][] {
		{boolean.class, Boolean.class},
		{byte.class, 	Byte.class},
		{short.class, 	Short.class},
		{int.class, 	Integer.class},
		{long.class, 	Long.class},
		{char.class, 	Character.class},
		{float.class, 	Float.class},
		{double.class, 	Double.class}
	};
	// @formatter:on

	/**
	 * Compares two types based upon object identity and full names
	 * 
	 * @param type1 type 1
	 * @param type2 type 2
	 * 
	 * @return whether type 1 equals type 2 or false if any of the types is null.
	 */
	public static boolean typeEquals(Class<?> type1, Class<?> type2) {
		if (Objects.isNull(type1) || Objects.isNull(type2)) {
			return false;
		} else if (type1 == type2) {
			// as long as only one class loader is used the equals operator can be used to
			// determine equality
			return true;
		} else {
			// to determine equality in case of multiple class loaders the full name is
			// compared
			return type1.getName().equals(type2.getName());
		}
	}

	public static final boolean typeEqualsOneOf(Class<?> type, Class<?>... types) {
		for (int i = 0; i < types.length; i++) {
			if (typeEquals(type, types[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * tests whether type extends typeSuper non recursively
	 * 
	 * @param type      type
	 * @param typeSuper typeSuper
	 * 
	 * @return true if type directly extends typeSuper
	 */
	public static boolean typeExtends(Class<?> type, Class<?> typeSuper) {
		return typeExtends(type, typeSuper, false);
	}

	/**
	 * tests whether typeSuper is the super type of type. if recursive is true then
	 * {@code typeExtends(Class<?>, Class<?>)} tests whether type or any of its
	 * super types extends typeSuper.
	 * 
	 * @param type      the type to test
	 * @param typeSuper the super class to check
	 * @param recursive true for recursive check
	 * 
	 * @return true if type is (direct if recursive false) subclass of typeSuper.
	 */
	public static boolean typeExtends(Class<?> type, Class<?> typeSuper, boolean recursive) {
		if (typeEquals(type, Object.class)) {
			return false;
		} else if (typeEquals(type, typeSuper)) {
			return false;
		} else if (typeEquals(typeSuper, Object.class)) {
			if (recursive) {
				return true;
			} else {
				return typeEquals(type.getSuperclass(), Object.class);
			}
		}
		Class<?> super_ = type;
		while (Objects.nonNull(super_ = super_.getSuperclass())) {
			if (typeEquals(super_, typeSuper)) {
				return true;
			}
			if (!recursive) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Tests whether type implements typeInterface non recursively (directly).
	 * 
	 * @param type          the type to test
	 * @param typeInterface the interface to check.
	 * 
	 * @return true if type directly implements typeInterface.
	 */
	public static boolean typeImplements(Class<?> type, Class<?> typeInterface) {
		return typeImplements(type, typeInterface, false);
	}

	/**
	 * Tests whether type implements typeInterface. if recursive is true then the
	 * test expands to all super classes of type.
	 * 
	 * @param type          the type to test.
	 * @param typeInterface the interface to check for.
	 * @param recursive     true to expand the test to all super classes.
	 * 
	 * @return true if type implements (directly if recursive is false)
	 *         typeInterface.
	 */
	public static boolean typeImplements(Class<?> type, Class<?> typeInterface, boolean recursive) {
		if (typeEquals(type, Object.class)) {
			return false;
		}
		do {
			Class<?>[] interfaces = type.getInterfaces();
			for (Class<?> interface_ : interfaces) {
				if (typeEquals(interface_, typeInterface)) {
					return true;
				}
			}
			if (!recursive) {
				return false;
			}
		} while (Objects.nonNull(type = type.getSuperclass()));
		return false;
	}

	public static Class<? extends Entity<?>> getEntityClass(Class<?> repoClass) {
		ParameterizedType t = (ParameterizedType) repoClass.getGenericSuperclass();
		Type[] tp = t.getActualTypeArguments();
		Class<? extends Entity<?>> result = null;
		for (int i = 0; i < tp.length; i++) {
			Class<?> entityClass;
			try {
				entityClass = Class.forName(tp[i].getTypeName());
				if (Entity.class.isAssignableFrom(entityClass)) {
					@SuppressWarnings("unchecked")
					Class<? extends Entity<?>> result1 = (Class<? extends Entity<?>>) entityClass;
					result = result1;
					break;
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (Objects.isNull(result)) {
//			LOGGER.critical("Reflect::getEntityClass returned null for Repository class %s!",
//					repoClass.getSimpleName());
		}
		return result;
	}

	public static boolean isComponent(Class<?> cls) {
		if (cls.isAnnotationPresent(Component.class)) {
			return true;
		} else if (cls.isAnnotationPresent(Service.class)) {
			return true;
		} else if (Application.class.isAssignableFrom(cls)) {
			return true;
		} else if (DatabaseApplication.class.isAssignableFrom(cls)) {
			return true;
		} else if (Database.class.isAssignableFrom(cls)) {
			return true;
		} else if (Repository.class.isAssignableFrom(cls)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isSwingComponent(Class<?> cls) {
		if (cls.isAnnotationPresent(SwingComponent.class)) {
			return true;
		} else if (SwingUI.class.isAssignableFrom(cls)) {
			return true;
//		} else if (Editor.class.isAssignableFrom(cls)) {
//			return true;
		} else {
			return false;
		}
	}

	/**
	 * gets the getter Method corresponding to the supplied field name.
	 * 
	 * @param c         the entity class to get the getter from
	 * @param fieldName the field name
	 * 
	 * @return the getter method
	 * 
	 * @throws NoSuchMethodException if the getter is not implemented
	 * @throws SecurityException
	 */
	public static Method getGetter(Class<?> c, String fieldName)
			throws NoSuchMethodException, SecurityException {
		String key = c.getName().concat(".").concat(fieldName);
		Method getter = GETTER_CACHE.get(key);
		if (Objects.nonNull(getter)) {
			return getter;
		}
		String firstLetterUC = fieldName.substring(0, 1).toUpperCase();
		String restOfName;
		if (fieldName.length() > 1) {
			restOfName = fieldName.substring(1);
		} else {
			restOfName = "";
		}
		int tries = 4;
		String[] namesToSeek = { String.format(FORMAT, GET, firstLetterUC, restOfName),
				String.format(FORMAT, IS, firstLetterUC, restOfName), fieldName,
				String.format(FORMAT, GET, fieldName.substring(0, 1), restOfName) };
		for (int i = 0; i < tries; i++) {
			Exception e = null;
			try {
				switch (i) {
				case 0:
					getter = c.getMethod(namesToSeek[i]);
					break;
				case 1:
					getter = c.getMethod(namesToSeek[i]);
					break;
				case 2:
					getter = c.getMethod(namesToSeek[i]);
					break;
				case 3:
					if (fieldName.length() > 1 && Character.isUpperCase(fieldName.charAt(1))) {
						getter = c.getMethod(namesToSeek[i]);
					} else {
						e = new NoSuchMethodException();
					}
					break;
				}
			} catch (Exception e1) {
				e = e1;
			}
			if (Objects.nonNull(e)) {
				if (i == tries - 1) {
					if (e instanceof NoSuchMethodException) {
						throw (NoSuchMethodException) e;
					}
					if (e instanceof SecurityException) {
						throw (SecurityException) e;
					}
					throw new NoSuchMethodException();
				}
			} else if (Objects.nonNull(getter)) {
				break;
			}
		}
//		try {} catch (NoSuchMethodException | SecurityException e) {
//			try {
//				try {} catch (NoSuchMethodException | SecurityException e1) {}
//			} catch (NoSuchMethodException | SecurityException e1) {}
//		}
		if (Objects.nonNull(getter)) {
			GETTER_CACHE.put(key, getter);
			return getter;
		}
		throw new NoSuchMethodException();
	}

	/**
	 * gets the setter method corresponding to the supplied field name/type
	 * 
	 * @param entityClass the entity class to get the setter from.
	 * @param fieldName   the field name
	 * @param type        the field type
	 * @return the corresponding setter.
	 * 
	 * @throws NoSuchMethodException if the setter is not implemented.
	 * 
	 * @throws SecurityException
	 */
	public static Method getSetter(Class<?> entityClass, String fieldName, Class<?> type)
			throws NoSuchMethodException, SecurityException {
		String key = entityClass.getName().concat(".").concat(fieldName);
		Method setter = SETTER_CACHE.get(key);
		if (Objects.nonNull(setter)) {
			return setter;
		}
		String firstLetterUC = fieldName.substring(0, 1).toUpperCase();
		String restOfName;
		if (fieldName.length() > 1) {
			restOfName = fieldName.substring(1);
		} else {
			restOfName = "";
		}
		String methodName = String.format(FORMAT, SET, firstLetterUC, restOfName);
		if (fieldName.equals(Log.ENTRY_ID) && typeEquals(entityClass, Log.class)) {
			type = Object.class;
		}
		String methodName2 = String.format(FORMAT, SET, firstLetterUC.toLowerCase(), restOfName);
		List<String> namesToSeek = Arrays.asList(methodName, fieldName, methodName2);
		Class<?> cls = entityClass;
		outer: do {
			for (Method m : cls.getDeclaredMethods()) {
				if (namesToSeek.contains(m.getName()) && m.getParameterCount() == 1) {
					Class<?> consumingType = m.getParameterTypes()[0];
					if (typeEquals(consumingType, type)) {
						setter = m;
						break outer;
					} else if (!consumingType.isPrimitive() && type.isPrimitive()) {
						if (typeEquals(consumingType, getWrapperOf(type))) {
							setter = m;
							break outer;
						}
					} else if (consumingType.isPrimitive() && !type.isPrimitive()) {
						if (typeEquals(type, getWrapperOf(consumingType))) {
							setter = m;
							break outer;
						}
					}
				}
			}
		} while (!typeEquals(cls = cls.getSuperclass(), Object.class));
		if (Objects.nonNull(setter)) {
			SETTER_CACHE.put(key, setter);
			return setter;
		}
		throw new NoSuchMethodException(entityClass.getSimpleName() + "." + fieldName + ": setter not found!");
	}

	/**
	 * Tests if either one of the supplied {@code Class<?>} parameters is the
	 * wrapper class of the other primitive type parameter.
	 * 
	 * @param x a {@code Class<?>}
	 * @param y a {@code Class<?>}
	 * 
	 * @return true if the supplied parameters are a pair of primitive and its
	 *         wrapper class
	 */
	public static boolean typeIsWrapperOf(Class<?> x, Class<?> y) {
		if ((x.isPrimitive() && y.isPrimitive()) || (!x.isPrimitive() && !y.isPrimitive())) {
			return false;
		}
		Class<?> primitive;
		Class<?> wrapper;
		if (x.isPrimitive()) {
			primitive = x;
			wrapper = y;
		} else {
			primitive = y;
			wrapper = x;
		}
		for (int i = 0; i < WRAPPER_TYPES.length; i++) {
			if (typeEquals(WRAPPER_TYPES[i][0], primitive) && typeEquals(WRAPPER_TYPES[i][1], wrapper)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * gets the primitive corresponding to the supplied wrapper class.
	 * 
	 * @param wrapper the wrapper class.
	 * 
	 * @return the corresponding primitive if any or null if no wrapper class is
	 *         supplied.
	 * @throws IllegalArgumentException if the supplied class is a primitive class.
	 */
	public static Class<?> getPrimitiveOf(Class<?> wrapper) {
		if (wrapper.isPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Reflect.getPrimitiveOf: the supplied parameter must not be a primitive but is '%s'",
							wrapper.getSimpleName()));
		}
		for (int i = 0; i < WRAPPER_TYPES.length; i++) {
			if (typeEquals(wrapper, WRAPPER_TYPES[i][1])) {
				return WRAPPER_TYPES[i][0];
			}
		}
		return null;
	}

	/**
	 * gets the wrapper class of the supplied primitive.
	 * 
	 * @param primitive the primitive class
	 * 
	 * @return the corresponding wrapper class
	 * 
	 * @throws IllegalArgumentException if the supplied class is not a primitive or
	 *                                  the type is unsupported.
	 */
	public static Class<?> getWrapperOf(Class<?> primitive) {
		if (!primitive.isPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Reflect.getWrapperOf: the supplied parameter must be a primitive but is '%s'",
							primitive.getSimpleName()));
		}
		for (int i = 0; i < WRAPPER_TYPES.length; i++) {
			if (typeEquals(primitive, WRAPPER_TYPES[i][0])) {
				return WRAPPER_TYPES[i][1];
			}
		}
		throw new IllegalArgumentException(String
				.format("Reflect.getWrapperOf: operation failed / unsupported type: '%s'", primitive.getSimpleName()));
	}

	/**
	 * Tests for the presence of an annotation.
	 * 
	 * @param testClass       The class to test.
	 * @param annotationClass the annotation to look for.
	 * 
	 * @return true if the class testClass is annotated by the specified annotation;
	 *         false otherwise.
	 */
	public static boolean isAnnotationPresent(Class<?> testClass, Class<? extends Annotation> annotationClass) {
		return isAnnotationPresent(testClass, annotationClass, false);
	}

	/**
	 * Tests for the presence of an annotation.
	 * 
	 * @param testClass       The class to test.
	 * @param annotationClass the annotation to look for.
	 * @param recursive       true to expand the search to all super classes.
	 * 
	 * @return true if the class testClass (or any of its super classes if recursive
	 *         true) is annotated by the specified annotation; false otherwise.
	 */
	public static boolean isAnnotationPresent(Class<?> testClass, Class<? extends Annotation> annotationClass,
			boolean recursive) {
		do {
			if (testClass.isAnnotationPresent(annotationClass)) {
				return true;
			}
			if (!recursive) {
				return false;
			}
		} while (Objects.nonNull(testClass = testClass.getSuperclass()));
		return false;
	}

	/**
	 * Upcasts the given INTEGER (numberValue) to the specified INTEGER
	 * (targetType).
	 * <p>
	 * Supported types are Byte, Short, Integer and Long.
	 * 
	 * @param numberValue the value in its undesired type.
	 * @param targetType  the desired type to up-cast the value to.
	 * 
	 * @return the given value as the specified type. the unmodified value if the
	 *         operation is not necessary.
	 * 
	 * @throws IllegalArgumentException when
	 *                                  <ul>
	 *                                  <li>numberValue is null
	 *                                  <li>targetType is null
	 *                                  <li>numberValue is not instance of Number
	 *                                  <li>targetType is not subclass of Number
	 *                                  <li>other unsupported value type
	 *                                  <li>other unsupported target type
	 *                                  <li>to prevent downcast
	 *                                  </ul>
	 */
	public final static Object tryUpCastInteger(Object numberValue, Class<?> targetType) {
		if (Objects.isNull(numberValue)) {
			throw new IllegalArgumentException("Reflect.tryUpCastInteger: value may not be null");
		}
		if (Objects.isNull(targetType)) {
			throw new IllegalArgumentException("Reflect.tryUpCastInteger: target type may not be null");
		}
		if (!(numberValue instanceof Number)) {
			throw new IllegalArgumentException(
					String.format("Reflect.tryUpCastInteger: value should be a Number object but is a %s",
							numberValue.getClass().getSimpleName()));
		}
		if (!typeExtends(targetType, Number.class)) {
			throw new IllegalArgumentException(
					String.format("Reflect.tryUpCastInteger: target type should be a Number Class but is a %s",
							targetType.getClass().getSimpleName()));
		}
		Class<?> valueType = numberValue.getClass();
		if (typeEquals(valueType, targetType)) {
			return numberValue;
		}
		List<Class<? extends Number>> numberClasses = new ArrayList<>();
		numberClasses.add(Byte.class);
		numberClasses.add(Short.class);
		numberClasses.add(Integer.class);
		numberClasses.add(Long.class);

		int receivedIndex = -1;
		int targetIndex = -1;

		for (int i = 0; i < numberClasses.size(); i++) {
			if (typeEquals(numberClasses.get(i), targetType)) {
				targetIndex = i;
			} else if (typeEquals(numberClasses.get(i), valueType)) {
				receivedIndex = i;
			}
		}
		if (targetIndex < 0) {
			throw new IllegalArgumentException(
					String.format("Reflect.tryUpCastInteger: Unsupported target type: %s", targetType.getSimpleName()));
		}
		if (receivedIndex < 0) {
			throw new IllegalArgumentException(
					String.format("Reflect.tryUpCastInteger: Unsupported value type: %s", valueType.getSimpleName()));
		}
		if (targetIndex < receivedIndex) {
			throw new IllegalArgumentException(String.format(
					"Reflect.tryUpCastInteger: Downcasting is not allowed ( received: %s / target type: %s)",
					valueType.getSimpleName(), targetType.getSimpleName()));
		}
		Number n = (Number) numberValue;
		switch (targetIndex) {
		case 1:
			return n.shortValue();
		case 2:
			return n.intValue();
		case 3:
			return n.longValue();
		default:
			throw new IllegalArgumentException(
					String.format("Reflect.tryUpCastInteger: operation failed ( received: %s / target type: %s)",
							valueType.getSimpleName(), targetType.getSimpleName()));
		}
	}

//	/**
//	 * Gets a value from an object for the database
//	 * 
//	 * @param o         the object to extract the value of.
//	 * @param fieldName the field to get the value from.
//	 * 
//	 * @return the field value via getter method.
//	 * 
//	 * @throws NoSuchMethodException
//	 * @throws SecurityException
//	 * @throws IllegalAccessException
//	 * @throws IllegalArgumentException
//	 * @throws InvocationTargetException
//	 */
//	public static final Object getValue(Entity<?> o, String fieldName) throws NoSuchMethodException, SecurityException,
//			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//		return getValue(o, fieldName, getGetter(o.getClass(), fieldName));
//	}
//
//	public static final Object getValue(Entity<?> o, String fieldName, Method getter) throws NoSuchMethodException,
//			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//		Object result = getter.invoke(o);
//		if (Objects.nonNull(result)) {
//			if (isJsonType(result.getClass())) {
//				try {
//					result = JSON_MAPPER.writeValueAsString(result);
//				} catch (JsonProcessingException e) {
//					e.printStackTrace();
//					return null;
//				}
//			} else if (result instanceof char[]) {
//				result = SQLiteUtils.UTF16.charsToBytes((char[]) result, true);
//			} else if (result instanceof BigDecimal) {
//				int scale = getBigDecimalScale(o, fieldName);
//				result = Big.bigDecToBytes((BigDecimal) result, scale);
//			} else if (result instanceof BigInteger) {
//				result = ((BigInteger) result).toByteArray();
//			} else if (result instanceof Character) {
//				result = Character.getNumericValue((Character) result);
//			}
//		}
//		return result;
//	}
//
//	/**
//	 * Set a value from the database to an object.
//	 * 
//	 * @param o         the object to write the value into.
//	 * @param fieldName the field to set (via setter).
//	 * @param value     the value to set.
//	 * @param type      the field type to search for the setter method.
//	 * 
//	 * @throws NoSuchMethodException
//	 * @throws SecurityException
//	 * @throws IllegalAccessException
//	 * @throws InvocationTargetException
//	 */
//	public static final void setValue(Entity<?> o, String fieldName, Object value, Class<?> type)
//			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
//		setValue(o, fieldName, value, type, getSetter((Class<? extends Entity<?>>) o.getClass(), fieldName, type));
//	}
//
//	public static final void setValue(Entity<?> o, String fieldName, Object value, Class<?> type, Method setter)
//			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
//		if (Objects.isNull(value) && type.isPrimitive()) {
//			value = 0;
//		}
//
//		if (Objects.nonNull(value) && !Entity.class.isAssignableFrom(type)) {
//			/*
//			 * Convert the value from the database to the class field type if necessary
//			 */
//			if (typeEquals(type, Boolean.class) || typeEquals(type, boolean.class)) {
//				if (value instanceof Number) {
//					value = !((Number) value).equals(0);
//				}
//			} else if (typeEquals(type, char[].class) && value instanceof byte[]) {
//				value = SQLiteUtils.UTF16.bytesToChars((byte[]) value, true);
//			} else if (typeEquals(type, BigInteger.class)) {
//				if (typeEquals(value.getClass(), byte[].class)) {
//					value = new BigInteger((byte[]) value);
//				}
//			} else if (typeEquals(type, BigDecimal.class)) {
//				if (typeEquals(value.getClass(), byte[].class)) {
//					int scale = getBigDecimalScale(o, fieldName);
//					value = Big.bytesToBigDec((byte[]) value, scale);
//				}
//			} else if (value instanceof String) {
//				if (isJsonType(type)) {
//					try {
//						value = JSON_MAPPER.readValue((String) value, type);
//					} catch (JsonMappingException e) {
//						e.printStackTrace();
//					} catch (JsonProcessingException e) {
//						e.printStackTrace();
//					}
//				}
//			} else if (value instanceof Number && (typeEquals(type, Character.class) || typeEquals(type, char.class))) {
//				value = (char) ((Number) value).intValue();
//			}
//			// finally ensure data/type integrity
//			if (!typeEquals(value.getClass(), type) && !typeIsWrapperOf(value.getClass(), type)) {
//				// auto upcast because of issues with getting the current auto-generated id
//				// todo fix
//				if (typeExtends(type, Number.class) && typeExtends(value.getClass(), Number.class)) {
//					Object x = tryUpCastInteger(value, type);
//					if (Objects.nonNull(x)) {
//						value = x;
//					}
//				}
//			}
//		}
//		try {
//			setter.invoke(o, value);
//		} catch (IllegalArgumentException e) {
//			String errorMessageFormat = "Reflect.setValue: Illegal argument! %s.%s expected: %s / received: %s%n";
//			System.err.printf(errorMessageFormat, o.getClass().getSimpleName(), fieldName, type.getSimpleName(),
//					value.getClass().getSimpleName());
//			throw e;
//		}
//	}

	public static Class<?>[] getActualTypeArgumentsFromGenericInterface(Class<?> implementationClass,
			Class<?> genericInterface) {
		Class<?> cls = implementationClass;
		do {
			Type[] genericInterfaces = cls.getGenericInterfaces();
			for (Type genericIf : genericInterfaces) {
				if (genericIf.getTypeName().startsWith(genericInterface.getTypeName())) {
					ParameterizedType pt = (ParameterizedType) genericIf;
					Type[] actualTypes = pt.getActualTypeArguments();
					Class<?>[] types = new Class<?>[actualTypes.length];
//					System.out.println(Arrays.toString(actualTypes));
					for (int i = 0; i < actualTypes.length; i++) {
						try {
							types[i] = Class.forName(actualTypes[i].getTypeName());
						} catch (ClassNotFoundException e) {
//							continue outer;
//							e.printStackTrace();
							return null;
						}
					}
					return types;
				}
			}
		} while (Objects.nonNull(cls = cls.getSuperclass()));
//		throw new IllegalArgumentException(
//				String.format("generic interface '%s' not found in implementation class '%s'",
//						genericInterface.getSimpleName(), implementationClass.getSimpleName()));
		return null;
	}

	public static <T> Constructor<T> getEmptyConstructor(Class<T> cls) {
		Constructor<?>[] constructors = cls.getConstructors();
		for (Constructor<?> c : constructors) {
			if (c.getParameterCount() == 0) {
				@SuppressWarnings("unchecked")
				Constructor<T> ct = (Constructor<T>) c;
				return ct;
			}
		}
		return null;
	}

	public static Collection<Field> getFieldsAnnotatedWith(Class<?> cls, Class<? extends Annotation> annotation) {
		List<Field> fields = new ArrayList<>();
		Field[] fx = cls.getDeclaredFields();
		for (Field f : fx) {
			if (f.isAnnotationPresent(annotation)) {
				fields.add(f);
			}
		}
		return fields;
	}

	private static String getStaticString(Class<?> cls, String fieldName) {
		do {
			try {
				Field field = cls.getDeclaredField(fieldName);
				if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())
						&& typeEquals(field.getType(), String.class)) {
					return (String) field.get(null);
				}
			} catch (NoSuchFieldException e) {
				Class<?>[] interfaces = cls.getInterfaces();
				for (Class<?> interface0 : interfaces) {
					String string = getStaticString(interface0, fieldName);
					if (Objects.nonNull(string)) {
						return string;
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} while (Objects.nonNull(cls = cls.getSuperclass()));
		return null;
	}

	public static final Optional<Class<?>> getClass(String className) {
		Class<?> cls = null;
		try {
			cls = Class.forName(className);
		} catch (Exception e) {
//			System.err.printf("Reflect::getClass(String) failed: %s%n", e.getMessage());
		}
		return Optional.ofNullable(cls);
	}

	/**
	 * concatenates the specified arrays.
	 * <p>
	 * if {@code arrayN.length} is 0 (zero) then array1 is returned. if there are
	 * more than 0 (zero) elements in arrayN then array1 is not allowed to be null.
	 * values in arrayN may be null.
	 * 
	 * @param <T>    The array type
	 * @param array1 The first array
	 * @param arrayN the subsequent arrays
	 * @return one array containing all values from the specified arrays to
	 *         concatenate
	 */
	public static <T> T[] concatenateArrays(T[] array1, T[]... arrayN) {
		if (arrayN.length == 0) {
			return array1;
		} else {
			Objects.requireNonNull(array1);
//			List<T[]> tList = new ArrayList<>();
//			tList.add(array1);
//			for(T[] ta:arrayN) {
//				tList.add(ta);
//			}
//			T[] arrays = tList.toArray(i->t);
			int size;
			if (Objects.nonNull(array1)) {
				size = array1.length;
			} else {
				size = 0;
			}
			for (int i = 0; i < arrayN.length; i++) {
				if (Objects.nonNull(arrayN[i])) {
					size += arrayN[i].length;
				}
			}
			/*
			 * The component type can only be T since the value comes from the method
			 * Parameter T[]...
			 */
			@SuppressWarnings("unchecked")
			T[] result = (T[]) Array.newInstance(array1.getClass().getComponentType(), size);
			if (size > 0) {
				int offset = 0;
				for (int i = 0; i < arrayN.length + 1; i++) {
					T[] array;
					if (i == 0) {
						array = array1;
					} else {
						array = arrayN[i - 1];
					}
					if (Objects.nonNull(array) && array.length > 0) {
						int j = 0;
						for (; j < array.length; j++) {
							result[offset + j] = array[j];
						}
						offset += j;
					}
				}
			}
			return result;
		}
	}

	public static final Class<?> getCollectionTypeFromField(Field field) {
		if (Collection.class.isAssignableFrom(field.getType())) {
			Type collectionType = field.getGenericType();
			if (collectionType instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) collectionType;
				Type[] actualTypeArguments = pt.getActualTypeArguments();
				if (actualTypeArguments.length == 1) {
					return (Class<?>) actualTypeArguments[0];
				}
			}
		}
		return null;
	}

	public static boolean shouldProxyForInterception(Class<?> cls) {
		for (Method m : cls.getMethods()) {
			if (m.isAnnotationPresent(InterceptBeforeCall.class) || m.isAnnotationPresent(InterceptAfterCall.class)
					|| m.isAnnotationPresent(InterceptAllowCaller.class)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Utility class / cannot be instantiated
	 */
	private Reflect() {}

}
