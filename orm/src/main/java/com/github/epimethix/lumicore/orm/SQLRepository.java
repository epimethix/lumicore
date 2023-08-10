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
package com.github.epimethix.lumicore.orm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.epimethix.lumicore.common.ApplicationUtils;
import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.Reflect;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.EntitySynchronizity;
import com.github.epimethix.lumicore.common.orm.ManyToManyRepository;
import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.model.Entity.EntityBuilder;
import com.github.epimethix.lumicore.common.orm.model.Log;
import com.github.epimethix.lumicore.common.orm.model.ManyToManyEntity;
import com.github.epimethix.lumicore.common.orm.model.MutableEntity;
import com.github.epimethix.lumicore.common.orm.model.TreeEntity;
import com.github.epimethix.lumicore.common.orm.query.Query.CreateBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.CreateIndexQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.CreateQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.CriteriaBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.DeleteBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.DeleteQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.InsertQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectQuery;
import com.github.epimethix.lumicore.common.orm.query.Query.UpdateBuilder;
import com.github.epimethix.lumicore.common.orm.query.Query.UpdateQuery;
import com.github.epimethix.lumicore.common.orm.query.QueryBuilderFactory;
import com.github.epimethix.lumicore.common.orm.sql.Dialect;
import com.github.epimethix.lumicore.common.orm.sql.TableInfo;
import com.github.epimethix.lumicore.common.orm.sqlite.Constraint;
import com.github.epimethix.lumicore.common.orm.sqlite.Constraint.PrimaryKeyConstraint;
import com.github.epimethix.lumicore.common.orm.sqlite.Definition;
import com.github.epimethix.lumicore.common.orm.sqlite.JoinOperator;
import com.github.epimethix.lumicore.logging.Logger;
import com.github.epimethix.lumicore.orm.ORM.Transform;
import com.github.epimethix.lumicore.orm.annotation.entity.Table;
import com.github.epimethix.lumicore.orm.annotation.field.AutoIncrement;
import com.github.epimethix.lumicore.orm.annotation.field.Column;
import com.github.epimethix.lumicore.orm.annotation.field.ManyToMany;
import com.github.epimethix.lumicore.orm.annotation.field.ManyToMany.Type;
import com.github.epimethix.lumicore.orm.annotation.field.ManyToOne;
import com.github.epimethix.lumicore.orm.annotation.field.NotNull;
import com.github.epimethix.lumicore.orm.annotation.field.ORMIgnore;
import com.github.epimethix.lumicore.orm.annotation.field.OneToMany;
import com.github.epimethix.lumicore.orm.annotation.field.OneToOne;
import com.github.epimethix.lumicore.orm.annotation.field.PrimaryKey;
import com.github.epimethix.lumicore.orm.annotation.field.Resolve;
import com.github.epimethix.lumicore.orm.annotation.type.DTOConstructor;
import com.github.epimethix.lumicore.orm.model.Key.CompositeKey;
import com.github.epimethix.lumicore.orm.model.LazyCollectionProxy;
import com.github.epimethix.lumicore.orm.model.LazyEntityProxy;
import com.github.epimethix.lumicore.orm.model.i.MetaEntity;
import com.github.epimethix.lumicore.properties.LumicoreProperties;

/**
 * {@code AbstractRepository<T, U>} is the main generator class of the package
 * com.github.epimethix.sqlite.lumicore. It automatically generates the create
 * statement and the CRUD+L access methods based upon an {@code Entity<U>}
 * implementation.
 * <p>
 * Special variants of {@code AbstractRepository<T, U>} are
 * {@code AbstractUserRepository<T extends User<U>, U>} and the implementation
 * class
 * {@code AuditMetaRepository<I extends Entity<J>, J> extends AbstractRepository<AuditMeta, Long>}.
 * <p>
 * When extending {@code AbstractMetaEntity<T>} the metadata fields will be set
 * automatically.
 * 
 * @author epimethix
 *
 * @param <E>  the entity class extending {@code Entity<U>}
 * @param <ID> the entities id/primary key type class
 * 
 * @see Entity
 * @see AbstractUserRepository
 */
public abstract class SQLRepository<E extends Entity<ID>, ID> implements Repository<E, ID> {

	/*
	 * * * Logging
	 */

	protected final Logger LOGGER = com.github.epimethix.lumicore.logging.Log
			.getLogger(com.github.epimethix.lumicore.logging.Log.CHANNEL_ORM);

	private final void logQuery(String sql) {
		logQuery(sql, Collections.emptyList());
	}

	protected final void logQuery(String sql, List<Object> values) {
		String msg = new StringBuilder(sql.replaceAll("[%]", "%%")).append(" ").append(values.stream()
				.map(o -> String.valueOf(o).replaceAll("[%]", "%%")).collect(Collectors.toList()).toString())
				.toString();
		LOGGER.trace(msg);
	}

	/*
	 * * * Class Fields
	 */

	/**
	 * Compares two TableInfo items by primary key and then by name
	 * 
	 * @see TableInfo
	 * @see FieldComparison#compare(boolean, boolean, String, String)
	 */
	private static final TableInfoComparator TABLE_INFO_COMPARATOR = new TableInfoComparator();

	/**
	 * The format string to concatenate the auto-generated auditing log entity name
	 */
	private final static String LOGGING_ENTITY_NAME_FORMAT = "%s_%s";

	/*
	 * * * Instance Fields
	 */

	/**
	 * Compares two FieldDefinitionContainers by primary key and then by name
	 * 
	 * @see FieldDefinitionContainer
	 * @see FieldComparison#compare(boolean, boolean, String, String)
	 */
	private final FieldDefinitionComparator FIELD_DEFINITION_COMPARATOR = new FieldDefinitionComparator();

	private final ObjectMapper JSON_MAPPER;

	/**
	 * indicates if soft delete (mark as deleted but keep in table) is enabled
	 */
	private final boolean SOFT_DELETE;

	/**
	 * The managed entity class
	 */
	private final Class<E> ENTITY_CLASS;
	private final Class<? extends Entity<ID>> ENTITY_INTERFACE_CLASS;
	private final Class<?>[] ENTITY_CLASS_INTERFACES;
	private final Class<? extends EntityBuilder<ID>> ENTITY_BUILDER_CLASS;
	private final Constructor<E> ENTITY_CLASS_CONSTRUCTOR;
	private final Constructor<? extends EntityBuilder<ID>> ENTITY_CLASS_BUILDER_CONSTRUCTOR;
	private final Constructor<? extends EntityBuilder<ID>> ENTITY_CLASS_BUILDER_COPY_CONSTRUCTOR;
	/**
	 * The managed entities id (primary key) class
	 */
	private final Class<ID> ID_CLASS;
	/**
	 * The audited entity id (primary key) class for AuditMetaRepository
	 * 
	 * @see LogRepository
	 */
	private final Class<?> LOGGING_ENTITY_ID_CLASS;
	/**
	 * The entity name as defined with EntityDefinition or the entity class' simple
	 * name by default
	 * 
	 * @see Table
	 */
	private final String ENTITY_NAME;

	private final String PARENT_FIELD_NAME;
	private final Class<?> PARENT_CLASS;
	/**
	 * indicates whether the primary key of the entity is auto-generated.
	 * 
	 * @see Definition#TYPE_INTEGER_PK
	 * @see Definition#TYPE_INTEGER_PK_AI
	 */
	private final boolean PK_IS_AUTO_GENERATED;
	/**
	 * indicates whether the primary key is a String UUID that must be generated on
	 * insert
	 */
	private final boolean PK_IS_UUID;
	/**
	 * The MappingDefinition arrays for the primary key definition(s)
	 */
	protected final MappingDefinition MAPPING_DEFINITION_PK;
	/**
	 * The MappingDefinition arrays for the non-primary key definition(s)
	 */
	protected final MappingDefinition MAPPING_DEFINITION_NON_PK;
	/**
	 * The MappingDefinition arrays for the definition(s) with the primary key
	 * leading.
	 */
	protected final MappingDefinition MAPPING_DEFINITION_PK_LEADING;

	private final Map<String, Integer> fieldIndexMap;

	private final JoinMapping[] JOIN_MAPPINGS;

	/**
	 * This Map can contain arrays
	 * <p>
	 * of length == 1 when mapped directly to the {@code ManyToManyEntity}
	 * <p>
	 * of length == 2 when mapped to the other {@code Entity}
	 */
	private final Map<String, ManyToMany.Type> manyToManyMappingType;
	private final Map<String, Class<?>[]> manyToManyMapping;
	private final Map<String, Class<?>> oneToManyMapping;
	private final List<String> eagerToMany;
	private final Map<String, ManyToManyMapping> manyToManyMap;
	private final Map<String, OneToManyMapping> oneToManyMap;

	private final int RESOLVE_DEPTH;

	private final List<Repository<?, ?>> childRepositories;
	/**
	 * the DB managing this Repository
	 */
	protected final Database DB;
	/**
	 * The audit-log repository if enabled, null otherwise
	 */
	private final LogRepository<E, ID> logRepository;

//	private final boolean LOG_LISTING;
	/**
	 * The lock for locking write sequences in this Repository
	 */
	private final ReentrantLock lock;
	/**
	 * Field definitions of this entity
	 */
	private final Map<String, Definition> definitions;
	/**
	 * Utility class instance for object oriented access.
	 * <p>
	 * supplies check for illegal names and check for field name collisions
	 */
	private final Naming NAMING;

	/*
	 * New Queries
	 */
	private final QueryBuilderFactory queryBuilderFactory;
	private final CreateQuery CREATE_TABLE_QUERY;
	private final List<CreateIndexQuery> CREATE_INDEX_QUERIES;
	protected final InsertQuery DEFAULT_INSERT_QUERY;
	protected final SelectQuery DEFAULT_SELECT_QUERY;
	protected final SelectQuery DEFAULT_SELECT_DISTINCT_QUERY;
	protected final SelectQuery DEFAULT_SELECT_QUERY_BY_ID;
	protected final SelectQuery DEFAULT_SELECT_QUERY_BY_PARENT;
	protected final SelectQuery DEFAULT_SELECT_QUERY_BY_TOP_PARENT;
	protected final SelectQuery DEFAULT_SELECT_QUERY_UNLIMITED;
	protected final DeleteQuery DEFAULT_DELETE_QUERY_UNCONDITIONAL;
	protected final DeleteQuery DEFAULT_DELETE_QUERY_BY_ID;
	private final Map<String, SelectQuery> FK_QUERIES;

	private final Map<Class<?>, Constructor<?>> DTO_CONSTRUCTORS = new HashMap<>();

	/**
	 * This constructor must be called when implementing
	 * {@code AbstractRepository<T, U>}.
	 * 
	 * @param db            the database implementation encapsulating this
	 *                      repository / not null
	 * @param entityClass   the entity class to manage / not null
	 * @param entityIdClass the entity id/primary key class / not null
	 * 
	 * @throws ConfigurationException if any configuration problem arises
	 */
	public SQLRepository(Database db, Class<E> entityClass, Class<ID> entityIdClass) throws ConfigurationException {
		this(db, entityClass, entityIdClass, null, null, null);
	}

	public SQLRepository(Database db, Class<E> entityClass, Class<ID> entityIdClass,
			Class<? extends Entity<ID>> entityInterfaceClass) throws ConfigurationException {
		this(db, entityClass, entityIdClass, entityInterfaceClass, null, null);
	}

	/**
	 * Custom constructor for logRepository, do not use!
	 */
	SQLRepository(Database db, Class<E> entityClass, Class<ID> entityIdClass,
			Class<? extends Entity<ID>> entityInterfaceClass, Class<?> loggingEntityIdClass, String loggingEntityName)
			throws ConfigurationException {
		if (Objects.isNull(db)) {
			throw new ConfigurationException(ConfigurationException.DATABASE_ARGUMENT_MUST_NOT_BE_NULL,
					getClass().getSimpleName());
		} else if (Objects.isNull(entityClass)) {
			throw new ConfigurationException(ConfigurationException.ENTITY_CLASS_ARGUMENT_MUST_NOT_BE_NULL,
					getClass().getSimpleName());
		} else if (Objects.isNull(entityIdClass)) {
			throw new ConfigurationException(ConfigurationException.ENTITY_ID_CLASS_ARGUMENT_MUST_NOT_BE_NULL,
					getClass().getSimpleName());
		}
		if (MetaEntity.class.isAssignableFrom(entityClass)) {
			if (!AbstractMutableMetaEntity.class.isAssignableFrom(entityClass)) {
				throw new ConfigurationException(ConfigurationException.CUSTOM_IMPLEMENTATION_OF_META_ENTITY_DETECTED,
						entityClass.getSimpleName());
			}
		}
		this.ENTITY_CLASS = entityClass;
		this.ENTITY_INTERFACE_CLASS = entityInterfaceClass;
		if (MutableEntity.class.isAssignableFrom(entityClass)) {
			this.ENTITY_CLASS_CONSTRUCTOR = Reflect.getEmptyConstructor(entityClass);
			if (Objects.isNull(this.ENTITY_CLASS_CONSTRUCTOR)) {
				throw new ConfigurationException(ConfigurationException.EMPTY_CONSTRUCTOR_MISSING,
						Entity.getEntityName(entityClass));
			}
			try {
				ENTITY_CLASS_CONSTRUCTOR.newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new ConfigurationException(ConfigurationException.ENTITY_CONSTRUCTOR_INACCESSIBLE,
						entityClass.getSimpleName());
			}
			ENTITY_CLASS_BUILDER_COPY_CONSTRUCTOR = null;
			ENTITY_CLASS_BUILDER_CONSTRUCTOR = null;
			this.ENTITY_BUILDER_CLASS = null;
		} else {
			this.ENTITY_CLASS_CONSTRUCTOR = null;
			Class<?>[] members = entityClass.getDeclaredClasses();
			Class<? extends EntityBuilder<ID>> builderClass = null;
			for (Class<?> cls : members) {
				if (cls.getSimpleName().equals("Builder")) {
					if (EntityBuilder.class.isAssignableFrom(cls)) {
						@SuppressWarnings("unchecked")
						Class<? extends EntityBuilder<ID>> builderClasss = (Class<? extends EntityBuilder<ID>>) cls;
						builderClass = builderClasss;
						break;
					}
				}
			}

			if (Objects.isNull(builderClass)) {
				throw new ConfigurationException(ConfigurationException.IMMUTABLE_BUILDER_NOT_FOUND,
						entityClass.getSimpleName());
			}
			this.ENTITY_BUILDER_CLASS = builderClass;
			ENTITY_CLASS_BUILDER_CONSTRUCTOR = Reflect.getEmptyConstructor(builderClass);
			if (Objects.isNull(ENTITY_CLASS_BUILDER_CONSTRUCTOR)) {
				throw new ConfigurationException(ConfigurationException.IMMUTABLE_BUILDER_NEEDS_AN_EMPTY_CONSTRUCTOR,
						entityClass.getSimpleName());
			}
			try {
				ENTITY_CLASS_BUILDER_COPY_CONSTRUCTOR = builderClass.getConstructor(entityClass);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new ConfigurationException(ConfigurationException.IMMUTABLE_BUILDER_NEEDS_A_COPY_CONSTRUCTOR,
						entityClass.getSimpleName());
			}
		}
		{
			List<Class<?>> interfaces = new ArrayList<>();
			Class<?> cls = ENTITY_CLASS;
			do {
				interfaces.addAll(Arrays.asList(cls.getInterfaces()));
			} while ((cls = cls.getSuperclass()) != Object.class && Objects.nonNull(cls));
			ENTITY_CLASS_INTERFACES = interfaces.toArray(new Class<?>[] {});
		}
		this.ID_CLASS = entityIdClass;
		this.LOGGING_ENTITY_ID_CLASS = loggingEntityIdClass;
		this.DB = db;
		this.lock = new ReentrantLock(true);
		this.definitions = new HashMap<>();
		this.SOFT_DELETE = ORM.isSoftDeleteEnabled(ENTITY_CLASS);
		this.NAMING = new Naming();
		this.JSON_MAPPER = new ObjectMapper();
		this.childRepositories = new ArrayList<>();
		this.manyToManyMapping = new HashMap<>();
		this.manyToManyMappingType = new HashMap<>();
		this.manyToManyMap = new HashMap<>();
		this.oneToManyMap = new HashMap<>();
		this.oneToManyMapping = new HashMap<>();
		this.eagerToMany = new ArrayList<>();

		if (this instanceof AbstractUserRepository<?, ?>
				&& !Reflect.typeExtends(ENTITY_CLASS, AbstractUser.class, true)) {
			// should not be possible to happen...
			throw new ConfigurationException(ConfigurationException.USER_ENTITY_SHOULD_EXTEND_USER_CLASS,
					getClass().getSimpleName());
		}
		if (SOFT_DELETE && !Reflect.typeExtends(ENTITY_CLASS, AbstractMutableMetaEntity.class, true)) {
			throw new ConfigurationException(ConfigurationException.SOFT_DELETE_REQUIRES_META_ENTITY,
					getClass().getSimpleName());
		}
		/**
		 * 1: Analyze Entity
		 */
		boolean withoutRowID = false;
		{
			String entityName = Entity.getEntityName(ENTITY_CLASS);
			boolean enableLogging = false;
//			boolean enableLoggingListing = false;
			Table entityDefinition = ENTITY_CLASS.getAnnotation(Table.class);
			if (Objects.nonNull(entityDefinition)) {
				withoutRowID = entityDefinition.withoutRowID();
				switch (entityDefinition.logging()) {
				case Table.ENABLE_LOGGING_CRUD:
					enableLogging = true;
					break;
//				case Table.ENABLE_LOGGING_CRUDL:
//					enableLogging = true;
////					enableLoggingListing = true;
//					break;
				}
			}
//			LOG_LISTING = enableLoggingListing;
			if (this instanceof LogRepository) {
				if (Objects.isNull(LOGGING_ENTITY_ID_CLASS) || Objects.isNull(loggingEntityName)
						|| loggingEntityName.isEmpty() || !Reflect.typeEquals(ENTITY_CLASS, Log.class)) {
					throw new ConfigurationException(ConfigurationException.WRONG_CONSTRUCTOR,
							getClass().getSimpleName());
				}
				entityName = String.format(LOGGING_ENTITY_NAME_FORMAT, entityName, loggingEntityName);
				logRepository = null;
			} else {
				if (NAMING.isIllegalTableName(entityName)) {
					throw new ConfigurationException(ConfigurationException.ILLEGAL_ENTITY_NAME, entityName);
				}
				if (Objects.nonNull(LOGGING_ENTITY_ID_CLASS) || Objects.nonNull(loggingEntityName)) {
					throw new ConfigurationException(ConfigurationException.WRONG_CONSTRUCTOR,
							getClass().getSimpleName());
				}
				if (enableLogging) {
					logRepository = new LogRepository<E, ID>(db, ENTITY_CLASS, ID_CLASS);
				} else {
					logRepository = null;
				}
			}
			ENTITY_NAME = entityName;
			if (TreeEntity.class.isAssignableFrom(ENTITY_CLASS)) {
				PARENT_FIELD_NAME = ORM.getParentFieldName(ENTITY_CLASS);
				PARENT_CLASS = ENTITY_CLASS;
			} else {
				PARENT_FIELD_NAME = null;
				PARENT_CLASS = null;
			}
		}
		/**
		 * 2: Analyze defined Constraints
		 */
		List<Constraint> constraints = new ArrayList<>();
		PrimaryKeyConstraint pkc = ORM.scanForConstraints(ENTITY_CLASS, constraints, ENTITY_NAME);

		/**
		 * 3: Analyze entity fields
		 */
		List<FieldDefinitionContainer> fieldDefinitionContainers = new ArrayList<>();
		int nPK;
		{
			nPK = scanEntity(ENTITY_CLASS, pkc, fieldDefinitionContainers);
			Collections.sort(fieldDefinitionContainers, FIELD_DEFINITION_COMPARATOR);
			List<String> fieldNames = new ArrayList<>();
			List<Class<?>> fieldTypes = new ArrayList<>();
			for (FieldDefinitionContainer fdc : fieldDefinitionContainers) {
				fieldNames.add(fdc.javaFieldName);
				fieldTypes.add(fdc.mappingType);
			}
			ORM.checkEntityIntegrity(ENTITY_CLASS, fieldNames, fieldTypes, DB.getDialect());
		}
		/**
		 * 4 Analyze joined entities
		 */
		List<JoinMapping> joinMappings = new ArrayList<JoinMapping>();
		for (int i = 0; i < fieldDefinitionContainers.size(); i++) {
			Class<?> mappingType = fieldDefinitionContainers.get(i).mappingType;
			if (Entity.class.isAssignableFrom(mappingType)) {
				Field field = fieldDefinitionContainers.get(i).field;
				/*
				 * Checked: mappingType is assignable to Entity
				 */
				@SuppressWarnings("unchecked")
				Class<? extends Entity<?>> eJoin = (Class<? extends Entity<?>>) mappingType;

				Repository<?, ?> repository;
				if (Reflect.typeEquals(ENTITY_CLASS, eJoin)) {
					repository = this;
				} else if (Objects.isNull(repository = DB.getRepository(eJoin))) {
					throw new ConfigurationException(ConfigurationException.REFERENCED_REPOSITORY_IS_MISSING,
							ENTITY_NAME, Entity.getEntityName(eJoin));

				}
				joinMappings.add(new JoinMapping(ENTITY_NAME, field, repository, i, DB.getDialect()));
			}
		}
		JOIN_MAPPINGS = joinMappings.toArray(new JoinMapping[0]);
		/**
		 * 5: initialize Join Tables
		 */
//		JOIN_TABLES = new ArrayList<>();
//		Field[] joinTableFields = Reflect.selectJoinTableFields(ENTITY_CLASS, true);
		/**
		 * 5: build mapping definition arrays and discover multiple auto-generated keys
		 */
		MAPPING_DEFINITION_PK = new MappingDefinition(nPK);
		String[] FLD_PK_JAVA_NAMES = MAPPING_DEFINITION_PK.javaNames;
		String[] FLD_PK_SQL_NAMES = MAPPING_DEFINITION_PK.sqlNames;
		Class<?>[] FLD_PK_MAPPING_TYPES = MAPPING_DEFINITION_PK.mappingTypes;
		int[] FLD_PK_LUMICORE_TYPES = MAPPING_DEFINITION_PK.lumicoreTypes;
		int[] FLD_PK_SQL_TYPES = MAPPING_DEFINITION_PK.sqlTypes;
		Transform[] FLD_PK_TRANSFORMS = MAPPING_DEFINITION_PK.transforms;

		MAPPING_DEFINITION_PK_LEADING = new MappingDefinition(fieldDefinitionContainers.size());
		String[] FLD_JAVA_NAMES_PK_LEADING = MAPPING_DEFINITION_PK_LEADING.javaNames;
		String[] FLD_SQL_NAMES_PK_LEADING = MAPPING_DEFINITION_PK_LEADING.sqlNames;
		Class<?>[] FLD_MAPPING_TYPES_PK_LEADING = MAPPING_DEFINITION_PK_LEADING.mappingTypes;
		int[] FLD_LUMICORE_TYPES_PK_LEADING = MAPPING_DEFINITION_PK_LEADING.lumicoreTypes;
		int[] FLD_SQL_TYPES_PK_LEADING = MAPPING_DEFINITION_PK_LEADING.sqlTypes;
		Transform[] FLD_TRANSFORMS_PK_LEADING = MAPPING_DEFINITION_PK_LEADING.transforms;

		MAPPING_DEFINITION_NON_PK = new MappingDefinition(fieldDefinitionContainers.size() - nPK);
		String[] FLD_NON_PK_JAVA_NAMES = MAPPING_DEFINITION_NON_PK.javaNames;
		String[] FLD_NON_PK_SQL_NAMES = MAPPING_DEFINITION_NON_PK.sqlNames;
		Class<?>[] FLD_NON_PK_MAPPING_TYPES = MAPPING_DEFINITION_NON_PK.mappingTypes;
		int[] FLD_NON_PK_LUMICORE_TYPES = MAPPING_DEFINITION_NON_PK.lumicoreTypes;
		int[] FLD_NON_PK_SQL_TYPES = MAPPING_DEFINITION_NON_PK.sqlTypes;
		Transform[] FLD_NON_PK_TRANSFORMS = MAPPING_DEFINITION_NON_PK.transforms;

		Definition[] definitionsArray = new Definition[fieldDefinitionContainers.size()];
		fieldIndexMap = new HashMap<>();
		{
			int counter = 0;
			int counterTrailing = 0;
			int counterJoins = 0;
			boolean pkIsAutoGen = false;
			boolean pkIsUUIDGen = false;
			int resolveDepth = Resolve.DEPTH_DEFAULT;
			int selfReferences = 0;
			for (FieldDefinitionContainer fdc : fieldDefinitionContainers) {
				Definition d = definitionsArray[counter] = fdc.toDefinition();
				definitions.put(d.getName(), d);
				fieldIndexMap.put(d.getName(), counter);
				if (Reflect.typeEquals(fdc.mappingType, ENTITY_CLASS)) {
					if (selfReferences > 0) {
						throw new ConfigurationException(ConfigurationException.MULTIPLE_SELF_REFERENCES, ENTITY_NAME);
					}
					selfReferences++;
					if (fdc.field.isAnnotationPresent(Resolve.class)) {
						Resolve r = fdc.field.getAnnotation(Resolve.class);
						resolveDepth = r.depth();
					}

				}
				if (counter == 0 && nPK == 1) {
					pkIsAutoGen = d.isAutoGenerated();
					pkIsUUIDGen = d.mustGenerateUUID();
				} else if (counter < nPK) {
					if (d.isAutoGenerated() || d.mustGenerateUUID()) {
						throw new ConfigurationException(
								ConfigurationException.MULTIPLE_PRIMARY_KEYS_CANNOT_BE_AUTOGENERATED, ENTITY_NAME,
								fdc.getSQLName());
					}
				}
				// @formatter:off
				if (counter < nPK) {
					FLD_PK_JAVA_NAMES[counter] = 
							FLD_JAVA_NAMES_PK_LEADING[counter] = 
							fdc.javaFieldName;
					FLD_PK_MAPPING_TYPES[counter] =
							FLD_MAPPING_TYPES_PK_LEADING[counter] = 
							fdc.mappingType;
					FLD_PK_SQL_NAMES[counter] =
							FLD_SQL_NAMES_PK_LEADING[counter] = 
							fdc.getSQLName();
					FLD_PK_LUMICORE_TYPES[counter] = 
							FLD_LUMICORE_TYPES_PK_LEADING[counter] = 
							d.getType();
//					if (Entity.class.isAssignableFrom(fdc.mappingType)) {
//						JoinMapping jm = JOIN_MAPPINGS[counterJoins++];
//						FLD_SQL_TYPES_PK_LEADING[counter] = jm.sqliteType;
//						FLD_TRANSFORMS_PK_LEADING[counter] = ORM.getTransform(jm.getter);
//					} else {
						FLD_PK_SQL_TYPES[counter] =
								FLD_SQL_TYPES_PK_LEADING[counter] = 
								DB.getDialect().resolveType(fdc.mappingType);
						FLD_PK_TRANSFORMS[counter] = 
								FLD_TRANSFORMS_PK_LEADING[counter] = 
								ORM.getTransform(fdc.field, JSON_MAPPER);
//					}
//					counter++;
				} else {
					FLD_NON_PK_MAPPING_TYPES[counterTrailing] =
							FLD_MAPPING_TYPES_PK_LEADING[counter] = 
							fdc.mappingType;
					FLD_NON_PK_JAVA_NAMES[counterTrailing] =
							FLD_JAVA_NAMES_PK_LEADING[counter] = 
							fdc.javaFieldName;
					FLD_NON_PK_SQL_NAMES[counterTrailing] =
							FLD_SQL_NAMES_PK_LEADING[counter] = 
							fdc.getSQLName();
					FLD_NON_PK_LUMICORE_TYPES[counterTrailing] =
							FLD_LUMICORE_TYPES_PK_LEADING[counter] = 
							d.getType();
					if (Entity.class.isAssignableFrom(fdc.mappingType)) {
						JoinMapping jm = JOIN_MAPPINGS[counterJoins++];
						FLD_NON_PK_SQL_TYPES[counterTrailing] = 
								FLD_SQL_TYPES_PK_LEADING[counter] = 
								jm.sqliteType;
						FLD_NON_PK_TRANSFORMS[counterTrailing] = 
								FLD_TRANSFORMS_PK_LEADING[counter] = 
								ORM.getTransform(jm.getter);
					} else {
						FLD_NON_PK_SQL_TYPES[counterTrailing] = 
								FLD_SQL_TYPES_PK_LEADING[counter] = 
								DB.getDialect().resolveType(fdc.mappingType);
						FLD_NON_PK_TRANSFORMS[counterTrailing] = 
								FLD_TRANSFORMS_PK_LEADING[counter] = 
								ORM.getTransform(fdc.field, JSON_MAPPER);
					}
//					FLD_LUMICORE_TYPES_PK_LEADING[counter] = d.getType();
//					FLD_SQL_TYPES_PK_LEADING[counter] = ORM.resolveType(fdc.mappingType);
					counterTrailing++;
				}
				counter++;
				// @formatter:on
			}

			PK_IS_AUTO_GENERATED = pkIsAutoGen;
			PK_IS_UUID = pkIsUUIDGen;
			MAPPING_DEFINITION_PK_LEADING.initializeMethods();
			MAPPING_DEFINITION_NON_PK.initializeMethods();
			MAPPING_DEFINITION_PK.initializeMethods();
			RESOLVE_DEPTH = resolveDepth;
		}

		/**
		 * 6: Build SQL
		 */

		/*
		 * NEW SQL
		 */

		queryBuilderFactory = db.getQueryBuilderFactory();
		CreateBuilder createBuilder = queryBuilderFactory.create(DB.getSchemaName(), ENTITY_CLASS, definitionsArray)
				.withConstraints(constraints.toArray(new Constraint[] {}));
		if (withoutRowID) {
			createBuilder.withoutRowid();
		}
		createBuilder.ifNotExists();
		CREATE_TABLE_QUERY = createBuilder.build();

		// TODO IMPLEMENT AND INITIALIZE TABLE INDICES
		CREATE_INDEX_QUERIES = Collections.emptyList();

		DEFAULT_INSERT_QUERY = queryBuilderFactory.insert(DB.getSchemaName(), ENTITY_CLASS, FLD_SQL_NAMES_PK_LEADING)
				.values(Arrays.asList((Entity<?>) null)).build();

		SelectBuilder selectBuilder = queryBuilderFactory.select(DB.getSchemaName(), ENTITY_CLASS,
				FLD_SQL_NAMES_PK_LEADING);

		if (SOFT_DELETE) {
			selectBuilder.withCriteria(this).isZero("deleted");
		}

		for (int i = 0; i < JOIN_MAPPINGS.length; i++) {
			JoinMapping jm = JOIN_MAPPINGS[i];
			if (jm.eager) {
				jm.repository.joinEntity(selectBuilder, this, jm.fieldName, jm.referencedFieldName);
			}
		}
		selectBuilder.limit(getDefaultLimit());
		DEFAULT_SELECT_QUERY = selectBuilder.build();
		DEFAULT_SELECT_QUERY_BY_ID = DEFAULT_SELECT_QUERY.builder().withCriteria(this).equals(Entity.ID, "").leave()
				.limit(1L).build();
		if (Objects.nonNull(PARENT_FIELD_NAME)) {
			DEFAULT_SELECT_QUERY_BY_TOP_PARENT = DEFAULT_SELECT_QUERY.builder().withCriteria(this)
					.isNull(PARENT_FIELD_NAME).leave().build();
			DEFAULT_SELECT_QUERY_BY_PARENT = DEFAULT_SELECT_QUERY.builder().withCriteria(this)
					.equals(PARENT_FIELD_NAME, "").leave().build();
		} else {
			DEFAULT_SELECT_QUERY_BY_TOP_PARENT = null;
			DEFAULT_SELECT_QUERY_BY_PARENT = null;
		}
		DEFAULT_SELECT_QUERY_UNLIMITED = DEFAULT_SELECT_QUERY.builder().clearLimit().build();
		DEFAULT_SELECT_DISTINCT_QUERY = queryBuilderFactory.select(this).distinct().build();
		DEFAULT_DELETE_QUERY_UNCONDITIONAL = queryBuilderFactory.delete(DB.getSchemaName(), ENTITY_CLASS).build();

		DEFAULT_DELETE_QUERY_BY_ID = DEFAULT_DELETE_QUERY_UNCONDITIONAL.builder()
				.where(DB.getSchemaName(), ENTITY_CLASS).equals(Entity.ID, "").leave().build();
		FK_QUERIES = new HashMap<>();
		for (JoinMapping j : JOIN_MAPPINGS) {
			Repository<?, ?> repo = j.repository;
			SelectBuilder b = db.getQueryBuilderFactory().select(repo, FLD_SQL_NAMES_PK_LEADING).withCriteria(this)
					.equals(j.fieldName, "").leave();
			for (JoinMapping jj : JOIN_MAPPINGS) {
				if (jj != j) {
					if (jj.eager) {
						jj.repository.joinEntity(b, this, jj.fieldName, jj.referencedFieldName);
					}
				}
			}
			FK_QUERIES.put(j.fieldName, DEFAULT_SELECT_QUERY);
		}

	}

	/**
	 * scans the given entity class for field definitions.
	 * <p>
	 * considering that this method uses reflection it should be moved to Reflect;
	 * <p>
	 * since it also uses a private inner class and a lot of constants of
	 * {@code AbstractRepository<T, U>} it will keep residing here for now.
	 * 
	 * @param entityClass               the entity class to scan
	 * @param pkc                       the PrimaryKeyConstraint if there is one or
	 *                                  null
	 * @param fieldDefinitionContainers the list to fill with the found field
	 *                                  definitions
	 * @param joinDefinitions
	 * 
	 * @return the number of primary key columns found
	 * 
	 * @throws ConfigurationException
	 */
	private int scanEntity(Class<?> entityClass, PrimaryKeyConstraint pkc,
			List<FieldDefinitionContainer> fieldDefinitionContainers) throws ConfigurationException {
		int nFoundPrimaryKeys = 0;
		List<String> names = new ArrayList<>();
		boolean firstRound = true;

		int strategy = Table.STRATEGY_DEFAULT;

		Table ed = entityClass.getAnnotation(Table.class);

		if (Objects.nonNull(ed)) {
			strategy = ed.strategy();
		}

		boolean explicitDeclarationNeeded = strategy == Table.STRATEGY_EXPLICIT_DEFINITION;

		do {
			Field[] declaredFields = entityClass.getDeclaredFields();
			for (Field field : declaredFields) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				if (field.isAnnotationPresent(ORMIgnore.class)) {
					continue;
				}
				Column fieldDefinition = field.getAnnotation(Column.class);
				OneToOne oneToOne = field.getAnnotation(OneToOne.class);
				ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
				OneToMany oneToMany = field.getAnnotation(OneToMany.class);
				ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
				Class<?> type = field.getType();
				Class<?> actualType = null;
				if (Objects.nonNull(LOGGING_ENTITY_ID_CLASS) && fieldDefinition.type() == Definition.TYPE_AUTO
						&& Reflect.typeEquals(type, Object.class)) {
					if (Reflect.typeExtends(LOGGING_ENTITY_ID_CLASS, CompositeKey.class)) {
						actualType = String.class;
					} else {
						actualType = LOGGING_ENTITY_ID_CLASS;
					}
				}
				FieldDefinitionContainer fdc = null;
				if (Objects.nonNull(fieldDefinition)) {
					if (Objects.nonNull(actualType)) {
						fdc = new FieldDefinitionContainer(field, actualType);
					} else {
						fdc = new FieldDefinitionContainer(field);
					}
				} else if (Objects.nonNull(oneToOne) && Entity.class.isAssignableFrom(type)) {
					if (!oneToOne.lazy()) {
						fdc = new FieldDefinitionContainer(field);
					}
				} else if (Objects.nonNull(manyToOne) && Entity.class.isAssignableFrom(type)) {
					fdc = new FieldDefinitionContainer(field);
				} else if (Objects.nonNull(manyToMany)) {
					boolean mapped = false;
					if (Collection.class.isAssignableFrom(type)) {
						Class<?> collectionType = Reflect.getCollectionTypeFromField(field);
						if (Objects.nonNull(collectionType) && Entity.class.isAssignableFrom(collectionType)) {
							String fieldName = field.getName();
							if (ManyToManyEntity.class.isAssignableFrom(collectionType)) {
								manyToManyMapping.put(fieldName, new Class<?>[] { collectionType });
							} else {
								manyToManyMapping.put(fieldName, new Class<?>[] { ENTITY_CLASS, collectionType });
							}
							manyToManyMappingType.put(fieldName, manyToMany.type());
							if (!manyToMany.lazy()) {
								eagerToMany.add(fieldName);
							}
							mapped = true;
						}
					}
					if (!mapped) {
						// TODO throw configuration exception
					}
					continue;
				} else if (Objects.nonNull(oneToMany)) {
					boolean mapped = false;
					if (Collection.class.isAssignableFrom(type)) {
						Class<?> collectionType = Reflect.getCollectionTypeFromField(field);
						if (Objects.nonNull(collectionType) && Entity.class.isAssignableFrom(collectionType)) {
							String fieldName = field.getName();
							oneToManyMapping.put(fieldName, collectionType);
							if (!oneToMany.lazy()) {
								eagerToMany.add(fieldName);
							}
							mapped = true;
						}
					}
					if (!mapped) {
						// TODO throw configuration exception
					}
					continue;
				} else if (explicitDeclarationNeeded) {
					continue;
				} else {
					if (DB.getDialect().isMappableType(type)) {
						fdc = new FieldDefinitionContainer(field);
					} else {
						throw new ConfigurationException(ConfigurationException.FIELD_TYPE_IS_NOT_MAPPABLE,
								entityClass.getSimpleName(), field.getName(), field.getType().getSimpleName());
					}
				}
				String sqlFieldName = fdc.getSQLName();
				if (NAMING.isIllegalFieldName(sqlFieldName)) {
					throw new ConfigurationException(ConfigurationException.ILLEGAL_FIELD_NAME, ENTITY_NAME,
							sqlFieldName);
				} else if (!firstRound && names.contains(sqlFieldName)) {
					/*
					 * do not skip duplicates in first round (outer loop) to discover inconsistent
					 * configuration / name collision.
					 * 
					 * in the 2nd to nTh round duplicate fields are being "hidden".
					 */
					continue;
				} else {
					names.add(sqlFieldName);
				}
				if (fdc.isPK() || (Objects.nonNull(pkc) && pkc.contains(sqlFieldName))) {
					nFoundPrimaryKeys++;
					if (!fdc.isPK()) {
						fdc.pk = true;
					}
				}
				fieldDefinitionContainers.add(fdc);
			}
			if (firstRound) {
				firstRound = false;
			}
//		} while (Objects.nonNull(entityClass = entityClass.getSuperclass()));
		} while (!Reflect.typeEquals((entityClass = entityClass.getSuperclass()), Object.class));
		String collisionField;
		if (Objects.nonNull(collisionField = NAMING.containsCollision(names))) {
			throw new ConfigurationException(ConfigurationException.FIELD_NAME_COLLISION, ENTITY_NAME, collisionField);
		}
		int nPK = 1;
		if (Reflect.typeExtends(ID_CLASS, CompositeKey.class)) {
//			TODO read key size from CompositeKey.class
//			nPK = Reflect.getKeySize(ID_CLASS);
			throw new RuntimeException("CompositeKey is not implemented yet, consider a unique key instead");
		}
		if (nPK != nFoundPrimaryKeys) {
			throw new ConfigurationException(ConfigurationException.PRIMARY_KEY_INCONSISTENCY, ENTITY_NAME,
					nFoundPrimaryKeys, nPK);
		}
		return nPK;
	}

	/*
	 * * * Schema Information
	 */

	@Override
	public boolean isColumnDroppable(String columnName) {
		if (Objects.nonNull(DROP_COLUMN_WHITE_LIST)) {
			for (String deletableColumn : DROP_COLUMN_WHITE_LIST) {
				if (deletableColumn.equals(columnName)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Database getDB() {
		return DB;
	}

	@Override
	public String getSchemaName() {
		return DB.getSchemaName();
	}

	@Override
	public String getEntityName() {
		return ENTITY_NAME;
	}

	@Override
	public Class<E> getEntityClass() {
		return ENTITY_CLASS;
	}

	@Override
	public long getEntityStructureVersion() {
		return ORM.getEntityStructureVersion(ENTITY_CLASS);
	}

	@Override
	public Definition getDefinition(String fieldName) {
		return definitions.get(fieldName);
	}

	/*
	 * * * Deployment and Update
	 */
	@Override
	public String getCreateStatement() {
		return CREATE_TABLE_QUERY.getQueryString();
	}

	@Override
	public void create() throws SQLException {
		DB.executeUpdate(CREATE_TABLE_QUERY.getQueryString());
		for (CreateIndexQuery ciq : CREATE_INDEX_QUERIES) {
			DB.executeUpdate(ciq.getQueryString());
			;
		}
	}

	@Override
	public void postCreateAction() throws SQLException {}

	@Override
	public void upgrade(long fileStructureVersion) throws SQLException {}

	/**
	 * checks the entities java definitions synchronizity with the table as it is
	 * defined in the database file.
	 * 
	 * @param tableInfo the tableinfo for this entity.
	 * 
	 * @return an EntitySynchronizity object containing the existing, the missing
	 *         and the deleted field names.
	 * 
	 */
	@Override
	public EntitySynchronizity checkEntitySynchronizity(List<TableInfo> tableInfo) {
		EntitySynchronizity es = new EntitySynchronizity();
		Collections.sort(tableInfo, TABLE_INFO_COMPARATOR);
		checkFields: for (String defName : MAPPING_DEFINITION_PK_LEADING.sqlNames) {
			for (TableInfo ti : tableInfo) {
				if (ti.getName().equals(defName)) {
					es.getFieldsToCheck().add(ti.getName());
					continue checkFields;
				}
			}
			es.getFieldsToCreate().add(defName);
		}
		checkToDelete: for (TableInfo ti : tableInfo) {
			String tiName = ti.getName();
			for (String defName : MAPPING_DEFINITION_PK_LEADING.sqlNames) {
				if (tiName.equals(defName)) {
					continue checkToDelete;
				}
			}
			es.getFieldsToDelete().add(tiName);
		}
		return es;
	}

	/*
	 * * * Utility
	 */

	@Override
	public E getLazyEntityProxy(Object id) {
		// System.err.println("lazyEntityTest >> Proxy.isProxyClass(ENTITY_CLASS):" +
		// Proxy.isProxyClass(ENTITY_CLASS));
		return (E) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), ENTITY_CLASS_INTERFACES,
				new LazyEntityProxy(id, this));
	}

	@Override
	public List<E> getLazyListProxy(Object one, String foreignKeyField) {
		return (List<E>) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[] { List.class },
				new LazyCollectionProxy(one, this, foreignKeyField));
	}

	@Override
	public List<?> getLazyListProxy(Object one, String foreignKeyField, ManyToMany.Type via) {
		return (List<E>) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[] { List.class },
				new LazyCollectionProxy(one, this, foreignKeyField, via));
	}

	@Override
	public final E initializeRecord(ResultSet rs)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, SQLException {
		return initializeRecord(rs, new int[] { 1 });
	}

	@Override
	public final E initializeRecord(ResultSet rs, int[] index) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, SQLException, InstantiationException, NoSuchMethodException, SecurityException {
		return initializeRecord(rs, index, RESOLVE_DEPTH, null, null);
	}

	@Override
	public final E initializeRecord(ResultSet rs, int[] index, int self)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException,
			InstantiationException, NoSuchMethodException, SecurityException {
		return initializeRecord(rs, index, self, null, null);
	}

	public final E initializeRecord(ResultSet rs, int[] index, int self, Entity<?> one, String foreignKeyField)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException,
			InstantiationException, NoSuchMethodException, SecurityException {
		E record = null;
		Object mutable;
		EntityBuilder<ID> builder = null;
		if (Objects.isNull(ENTITY_BUILDER_CLASS)) {
			record = ENTITY_CLASS_CONSTRUCTOR.newInstance();
			mutable = record;
		} else {
			builder = ENTITY_CLASS_BUILDER_CONSTRUCTOR.newInstance();
			mutable = builder;
		}
		int j = JOIN_MAPPINGS.length > 0 ? 0 : -1;
		boolean isNull = true;
		Object[] joinIds = new Object[JOIN_MAPPINGS.length];
//		if (ENTITY_CLASS.getSimpleName().equals("Transaction")) {
//			System.err.println("STOP");
//		} else {
//			System.err.println("--- " + ENTITY_CLASS.getSimpleName());
//		}
		for (int i = 0, iDB = index[0]; i < MAPPING_DEFINITION_PK_LEADING.javaNames.length; i++, iDB++) {
			if (j != -1 && j < JOIN_MAPPINGS.length && JOIN_MAPPINGS[j].mappingIndex == i) {
				joinIds[j] = rs.getObject(iDB);
				j++;
				continue;
			}
			Object value = rs.getObject(iDB);
			if (Objects.nonNull(value)) {
				setValue(mutable, value, MAPPING_DEFINITION_PK_LEADING.mappingTypes[i],
						MAPPING_DEFINITION_PK_LEADING.setters[i], MAPPING_DEFINITION_PK_LEADING.transforms[i]);
				if (isNull) {
					isNull = false;
				}
			}
		}
		index[0] += MAPPING_DEFINITION_PK_LEADING.javaNames.length;
		if (isNull) {
			return null;
		}
		int i = 0;
		for (JoinMapping jm : JOIN_MAPPINGS) {
			if (Reflect.typeEquals(ENTITY_CLASS, jm.referencedEntity)) {
				if (self == -1) {
					self = 0;
				} else if (self == 0) {
					continue;
				} else {
					self--;
				}
			}
			Object subRecord;
			if (jm.fieldName.equals(foreignKeyField)) {
				subRecord = one;
			} else if (jm.eager) {
				subRecord = jm.repository.initializeRecord(rs, index, self);
			} else {
				subRecord = jm.repository.getLazyEntityProxy(joinIds[i]);
			}
			if (Objects.nonNull(subRecord)) {
				MAPPING_DEFINITION_PK_LEADING.setters[jm.mappingIndex].invoke(mutable, subRecord);
			}
			i++;
		}
		if (Objects.nonNull(builder)) {
			record = (E) builder.build();
		}
		boolean rebuild = false;
		for (String key : manyToManyMapping.keySet()) {

			ManyToManyMapping m = getManyToManyMapping(key);
			List<?> res = null;
//			ManyToManyMapping m = manyToManyMap.get(key);
			if (eagerToMany.contains(key)) {
				switch (m.type) {
				case DIRECT:
					res = m.mtmRepository.selectByFK(m.manyFieldName, record);
					break;
				case VIA_A:
					res = m.mtmRepository.listByA(record);
					break;
				case VIA_B:
					res = m.mtmRepository.listByB(record);
					break;

				default:
					break;
				}
			} else {
				switch (m.type) {
				case DIRECT:
					res = m.mtmRepository.getLazyListProxy(record, m.manyFieldName);
					break;
				case VIA_A:
					res = m.mtmRepository.getLazyListProxy(record, m.manyFieldName, Type.VIA_A);
					break;
				case VIA_B:
					res = m.mtmRepository.getLazyListProxy(record, m.manyFieldName, Type.VIA_B);
					break;

				default:
					break;
				}
			}
			if (!rebuild) {
				rebuild = true;
			}
			m.setter.invoke(builder, res);
		}
		for (String key : oneToManyMapping.keySet()) {
			OneToManyMapping m = getOneToManyMapping(key);
			List<?> res;
			if (eagerToMany.contains(key)) {
				res = m.otmRepository.selectByFK(m.oneFieldName, record);
			} else {
				res = m.otmRepository.getLazyListProxy(record, m.oneFieldName);
			}
			m.setter.invoke(builder, res);
			if (!rebuild) {
				rebuild = true;
			}
		}
		if (rebuild) {
			record = (E) builder.build();
		}
		return record;
	}

	private static final Object getValue(Entity<?> o, Method getter, Transform transform)
			throws SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object value = null;
		value = getter.invoke(o);
		if (Objects.nonNull(value)) {
			try {
				value = transform.javaToDb.transform(value);
			} catch (Exception e) {
				throw new SQLException("Type transformation failed: " + e.getMessage(), e);
			}
		}
		return value;
	}

	private static final Object setValue(Object item, Object value, Class<?> type, Method setter, Transform transform)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
//		Class<?> type = setter.getParameters()[0].getType();
		if (Objects.isNull(value) && type.isPrimitive()) {
			value = 0;
		}
		if (Objects.nonNull(value)) {
			try {
				value = transform.dbToJava.transform(value);
			} catch (Exception e) {
//				e.printStackTrace();
				throw new SQLException("Type transformation failed: " + e.getMessage(), e);
			}
			// ensure data/type integrity
			if (!Reflect.typeEquals(value.getClass(), type) && !Reflect.typeIsWrapperOf(value.getClass(), type)) {
				// auto upcast because of issues with getting the current auto-generated id
				// TODO fix?
				if (Number.class.isAssignableFrom(type) && Number.class.isAssignableFrom(value.getClass())) {
					Object x = Reflect.tryUpCastInteger(value, type);
					if (Objects.nonNull(x)) {
						value = x;
					}
				}
			}
			try {
				setter.invoke(item, value);
			} catch (IllegalArgumentException e) {
				String errorMessageFormat = "Reflect.setValue: Illegal argument! %s.%s expected: %s / received: %s%n";
				System.err.printf(errorMessageFormat, item.getClass().getSimpleName(), setter.getName(),
						type.getSimpleName(), value.getClass().getSimpleName());
				System.err.println("item class: " + item.getClass() + " / setter type: " + setter.getDeclaringClass());
				throw e;
			}
		} else {
			setter.invoke(item, (Object) null);
		}
		return value;
	}

	private int fillPreparedStatementAutoType(PreparedStatement ps, Object[] values, int startingValue)
			throws SQLException {
		return JDBCUtils.autoFill(DB.getDialect(), ps, values, startingValue);

	}

	protected int fillPreparedStatementAutoType(PreparedStatement ps, Object[] values) throws SQLException {
		return fillPreparedStatementAutoType(ps, values, 1);
	}

	protected int fillPreparedStatement(PreparedStatement ps, Method[] getters, Transform[] transforms, int[] sqlTypes,
			E item) throws SQLException {
		return fillPreparedStatement(ps, getters, transforms, sqlTypes, item, 1);
	}

	protected int fillPreparedStatement(PreparedStatement ps, Method[] getters, Transform[] transforms, int[] sqlTypes,
			E item, int nextPos) throws SQLException {
		for (int i = 0; i < getters.length; i++, nextPos++) {
			Object arg;
			try {
				arg = getValue(item, getters[i], transforms[i]);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				LOGGER.error(e);
				throw new SQLException(String.format(
						"%s::fillPreparedStatement : An error occurred while trying to"
								+ "obtain a value through %s::%s",
						getClass().getSimpleName(), ENTITY_CLASS.getSimpleName(), getters[i].getName()));
			}
			if (arg == null) {
				ps.setNull(nextPos, sqlTypes[i]);
			} else {
				ps.setObject(nextPos, arg, sqlTypes[i]);
			}
		}
		return nextPos;
	}

	protected int fillPreparedStatement(PreparedStatement ps, Transform[] transforms, int[] sqlTypes, Object[] values)
			throws SQLException {
		return fillPreparedStatement(ps, transforms, sqlTypes, values, 1);
	}

	protected int fillPreparedStatement(PreparedStatement ps, Transform[] transforms, int[] sqlTypes, Object[] values,
			int nextPos) throws SQLException {
		for (int i = 0; i < values.length; i++, nextPos++) {
			Object arg;
			try {
				arg = transforms[i].javaToDb.transform(values[i]);
			} catch (Exception e) {
				LOGGER.error(e);
				continue;
			}
//			try {
//				arg = getValue(item, getters[i], transforms[i]);
//			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//				LOGGER.error(e);
//				throw new SQLException(String.format(
//						"%s::fillPreparedStatement : An error occurred while trying to"
//								+ "obtain a value through %s::%s",
//								getClass().getSimpleName(), ENTITY_CLASS.getSimpleName(), getters[i].getName()));
//			}
			if (arg == null) {
				ps.setNull(nextPos, sqlTypes[i]);
			} else {
				ps.setObject(nextPos, arg, sqlTypes[i]);
			}
		}
		return nextPos;
	}

	@Override
	public <T> T newRecord() {
		if (Objects.isNull(ENTITY_BUILDER_CLASS)) {
			try {
				return (T) ENTITY_CLASS_CONSTRUCTOR.newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				LOGGER.error(e);
			}
		} else {
			try {
				return (T) ENTITY_CLASS_BUILDER_CONSTRUCTOR.newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				LOGGER.error(e);
			}
		}
		return null;
	}

	@Override
	public final boolean checkClose(boolean closeConnection) throws SQLException {
		if (closeConnection) {
			DB.closeConnection();
			return true;
		}
		return false;
	}

	@Override
	public boolean contentEquals(E a, E b) {
		int size = MAPPING_DEFINITION_PK_LEADING.getters.length;
		for (int i = 0; i < size; i++) {
			Method getter = MAPPING_DEFINITION_PK_LEADING.getters[i];
			try {
				Object valA = getter.invoke(a);
				Object valB = getter.invoke(b);
				if (!Objects.equals(valA, valB)) {
					return false;
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				LOGGER.error(e);
				return false;
			}
		}
		return true;
	}

	@Override
	public E copy(E e) {
		E e2 = newRecord();
		int size = MAPPING_DEFINITION_PK_LEADING.getters.length;
		for (int i = 0; i < size; i++) {
			Method getter = MAPPING_DEFINITION_PK_LEADING.getters[i];
			Method setter = MAPPING_DEFINITION_PK_LEADING.setters[i];
			try {
				Object val = getter.invoke(e);
				setter.invoke(e2, val);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				LOGGER.error(ex);
			}
		}
		return e2;
	}

	/*
	 * * * NEW CRUD
	 */

	@Override
	public List<E> insert(List<E> es) {
		return insert(DEFAULT_INSERT_QUERY.withRecords(es));
	}

	@Override
	public List<E> insert(InsertQuery q) {

		return null;
	}

	@Override
	public long getDefaultLimit() {
		return 100L;
	}

	@Override
	public List<E> select(Function<SelectBuilder, SelectBuilder> b) throws SQLException {
		return select(b.apply(DEFAULT_SELECT_QUERY.builder()).build());
	}

	@Override
	public Optional<E> selectById(ID id) throws SQLException {
		List<E> x = select(DEFAULT_SELECT_QUERY_BY_ID.withCriteriumValues(id));
		return x.size() == 1 ? Optional.of(x.get(0)) : Optional.empty();
	}

	@Override
	public List<E> selectByFK(String foreignKeyField, Object one) throws SQLException {
		SelectQuery q = FK_QUERIES.get(foreignKeyField);
		List<E> l = new ArrayList<E>();
		String sql = q.getQueryString();
		try {
			Connection c = DB.getConnection();
			logQuery(sql, Arrays.asList(q.getCriteriumValues()));
			if (q.getCriteriumValues().length == 0) {
				try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
					while (rs.next()) {
						E x = initializeRecord(rs, new int[] { 1 }, -1, (Entity<?>) one, foreignKeyField);
						l.add(x);
					}
				}
			} else {
				try (PreparedStatement ps = c.prepareStatement(sql)) {
					fillPreparedStatementAutoType(ps, q.getCriteriumValues());
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							E x = initializeRecord(rs);
							l.add(x);
						}
					}
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			LOGGER.error(e);
		} catch (SQLException e) {
			LOGGER.error(sql);
			throw e;
		} finally {
			checkClose(q);
		}
		return l;

//		return null;
	}

	@Override
	public List<E> selectAll() throws SQLException {
		return select(DEFAULT_SELECT_QUERY_UNLIMITED);
	}

	@Override
	public List<E> selectAll(List<ID> ids) throws SQLException {
		return select(DEFAULT_SELECT_QUERY.builder().limit(ids.size()).withCriteria(this)
				.in(PARENT_FIELD_NAME, (List<Object>) ids).leave().build());
	}

	@Override
	public List<E> select(SelectQuery q) throws SQLException {
		List<E> l = new ArrayList<E>();
		String sql = q.getQueryString();
		LOGGER.trace(sql);
		try {
			Connection c = DB.getConnection();
			logQuery(sql, Arrays.asList(q.getCriteriumValues()));
			if (q.getCriteriumValues().length == 0) {
				try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
					while (rs.next()) {
						E x = initializeRecord(rs);
						l.add(x);
					}
				}
			} else {
				try (PreparedStatement ps = c.prepareStatement(sql)) {
					fillPreparedStatementAutoType(ps, q.getCriteriumValues());
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							E x = initializeRecord(rs);
							l.add(x);
						}
					}
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			LOGGER.error(e);
		} catch (SQLException e) {
			LOGGER.error(sql);
			LOGGER.error(e);
			throw e;
		} finally {
			checkClose(q);
		}
		return l;
	}

	protected void checkClose(SelectQuery q) throws SQLException {
		if (q.isCloseConnection().isPresent()) {
			if (q.isCloseConnection().get()) {
				checkClose(true);
			}
		} else if (LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION) {
			checkClose(true);
		}
	}

	@Override
	public List<E> selectCriteria(Function<CriteriaBuilder<SelectBuilder>, CriteriaBuilder<SelectBuilder>> b)
			throws SQLException {
		return select(b.apply(DEFAULT_SELECT_QUERY.builder().withCriteria(this)).leave().build());
	}

	@Override
	public <DTO> List<DTO> selectDTO(SelectQuery q, Class<DTO> dtoClass) throws SQLException {
		if (dtoClass == Long.class) {
			@SuppressWarnings("unchecked")
			List<DTO> res = (List<DTO>) selectLong(q);
			return res;
		}
		if (dtoClass == Double.class) {
			@SuppressWarnings("unchecked")
			List<DTO> res = (List<DTO>) selectDouble(q);
			return res;
		}
		if (dtoClass == String.class) {
			@SuppressWarnings("unchecked")
			List<DTO> res = (List<DTO>) selectText(q);
			return res;
		}
		if (dtoClass == Object.class) {
			@SuppressWarnings("unchecked")
			List<DTO> res = (List<DTO>) selectObject(q);
			return res;
		}
		Constructor<?> dtoConstructor = null;
		if (DTO_CONSTRUCTORS.containsKey(dtoClass)) {
			dtoConstructor = DTO_CONSTRUCTORS.get(dtoClass);
		} else {
			Constructor<?>[] allConstructors = dtoClass.getConstructors();
			if (allConstructors.length == 1) {
				dtoConstructor = allConstructors[0];
			} else {
				for (Constructor<?> c : allConstructors) {
					if (c.isAnnotationPresent(DTOConstructor.class)) {
						dtoConstructor = c;
						break;
					}
				}
			}
			if (Objects.nonNull(dtoConstructor)) {
				DTO_CONSTRUCTORS.put(dtoClass, dtoConstructor);
			} else {
				throw new IllegalArgumentException("Could not determine DTO Constructor!");
			}
		}
		List<DTO> result = new ArrayList<>();
		try {
			String sql = q.getQueryString();
			Connection c = DB.getConnection();
			logQuery(sql, Arrays.asList(q.getCriteriumValues()));
			if (q.getCriteriumValues().length == 0) {
				try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
					int width = rs.getMetaData().getColumnCount();
					if (width != dtoConstructor.getParameterCount()) {
						throw new SQLException(
								"DTO Constructor parameter count must be equal to the column count of the ResultSet!");
					}
					while (rs.next()) {
						Object[] args = new Object[dtoConstructor.getParameterCount()];
						for (int i = 0; i < args.length; i++) {
							args[i] = rs.getObject(i + 1);
						}
						/*
						 * dtoConstructor is obtained from Class<DTO>
						 */
						@SuppressWarnings("unchecked")
						DTO dto = (DTO) dtoConstructor.newInstance(args);
						result.add(dto);
					}
				}
			} else {
				try (PreparedStatement ps = c.prepareStatement(sql)) {
					fillPreparedStatementAutoType(ps, q.getCriteriumValues());
					try (ResultSet rs = ps.executeQuery()) {
						int width = rs.getMetaData().getColumnCount();
						if (width != dtoConstructor.getParameterCount()) {
							throw new SQLException(
									"DTO Constructor parameter count must be equal to the column count of the ResultSet!");
						}
						while (rs.next()) {
							Object[] args = new Object[dtoConstructor.getParameterCount()];
							for (int i = 0; i < args.length; i++) {
								args[i] = rs.getObject(i + 1);
							}
							/*
							 * dtoConstructor is obtained from Class<DTO>
							 */
							@SuppressWarnings("unchecked")
							DTO dto = (DTO) dtoConstructor.newInstance(args);
							result.add(dto);
						}
					}
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| SecurityException e) {
			LOGGER.error(e);
		} finally {
			checkClose(q);
		}
		return result;
	}

	private List<Long> selectLong(SelectQuery q) throws SQLException {
		List<Long> result = new ArrayList<>();
		String sql = q.getQueryString();
		try {
			Connection c = DB.getConnection();
			logQuery(sql, Arrays.asList(q.getCriteriumValues()));
			if (q.getCriteriumValues().length == 0) {
				try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
					while (rs.next()) {
						result.add(rs.getLong(1));
					}
				}
			} else {
				try (PreparedStatement ps = c.prepareStatement(sql)) {
					fillPreparedStatementAutoType(ps, q.getCriteriumValues());
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							result.add(rs.getLong(1));
						}
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.error(sql);
			throw e;
		} finally {
			checkClose(q);
		}
		return result;
	}

	private List<Double> selectDouble(SelectQuery q) throws SQLException {
		List<Double> result = new ArrayList<>();
		String sql = q.getQueryString();
		try {
			Connection c = DB.getConnection();
			logQuery(sql, Arrays.asList(q.getCriteriumValues()));
			if (q.getCriteriumValues().length == 0) {
				try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
					while (rs.next()) {
						result.add(rs.getDouble(1));
					}
				}
			} else {
				try (PreparedStatement ps = c.prepareStatement(sql)) {
					fillPreparedStatementAutoType(ps, q.getCriteriumValues());
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							result.add(rs.getDouble(1));
						}
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.error(sql);
			throw e;
		} finally {
			checkClose(q);
		}
		return result;
	}

	private List<String> selectText(SelectQuery q) throws SQLException {
		List<String> result = new ArrayList<>();
		String sql = q.getQueryString();
		try {
			Connection c = DB.getConnection();
			logQuery(sql, Arrays.asList(q.getCriteriumValues()));
			if (q.getCriteriumValues().length == 0) {
				try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
					while (rs.next()) {
						result.add(rs.getString(1));
					}
				}
			} else {
				try (PreparedStatement ps = c.prepareStatement(sql)) {
					fillPreparedStatementAutoType(ps, q.getCriteriumValues());
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							result.add(rs.getString(1));
						}
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.error(sql);
			throw e;
		} finally {
			checkClose(q);
		}
		return result;
	}

	private List<Object> selectObject(SelectQuery q) throws SQLException {
		List<Object> result = new ArrayList<>();
		String sql = q.getQueryString();
		try {
			Connection c = DB.getConnection();
			logQuery(sql, Arrays.asList(q.getCriteriumValues()));
			if (q.getCriteriumValues().length == 0) {
				try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
					while (rs.next()) {
						result.add(rs.getString(1));
					}
				}
			} else {
				try (PreparedStatement ps = c.prepareStatement(sql)) {
					fillPreparedStatementAutoType(ps, q.getCriteriumValues());
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							result.add(rs.getString(1));
						}
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.error(sql);
			throw e;
		} finally {
			checkClose(q);
		}
		return result;
	}

	@Override
	public <T> List<T> distinct(String fieldName, Class<T> type) throws SQLException {
		return selectDTO(DEFAULT_SELECT_DISTINCT_QUERY.builder().select(ENTITY_CLASS, fieldName).distinct().build(),
				type);
	}

	@Override
	public List<E> update(List<E> es) throws SQLException {
		List<E> es2 = new ArrayList<>();
		for (E e : es) {
			es2.add(update(e).get());
		}
		return es2;
	}

	@Override
	public List<E> update(Function<UpdateBuilder, UpdateBuilder> b) throws SQLException {
		return update(b.apply(DB.getQueryBuilderFactory().update(this)).build());
	}

	@Override
	public List<E> update(UpdateQuery q) throws SQLException {
		Connection c = DB.getConnection();
		int n = q.getSetValues().length;
		MappingDefinition delta = new MappingDefinition(n);
		String[] fields = q.getFields();
		for (int i = 0, j = 0; i < n && j < MAPPING_DEFINITION_NON_PK.getters.length; j++) {
			if (MAPPING_DEFINITION_NON_PK.sqlNames[j].equals(fields[i])) {
				delta.transforms[i] = MAPPING_DEFINITION_NON_PK.transforms[j];
				delta.sqlTypes[i] = MAPPING_DEFINITION_NON_PK.sqlTypes[j];
			}
		}
		UpdateQuery sqlUpdateDeltaQuery = q;
		String sqlUpdateDelta = sqlUpdateDeltaQuery.getQueryString();

		logQuery(sqlUpdateDelta);
		try (PreparedStatement ps = c.prepareStatement(sqlUpdateDelta)) {
			int nextPos = fillPreparedStatement(ps, delta.transforms, delta.sqlTypes, q.getSetValues());
			fillPreparedStatement(ps, MAPPING_DEFINITION_PK.transforms, MAPPING_DEFINITION_PK.sqlTypes,
					q.getCriteriumValues(), nextPos);
			ps.executeUpdate();
//				log(item.getId(), CRUD.U, user, logWrite);
		} catch (SQLException e) {
			LOGGER.error(sqlUpdateDelta);
			throw e;
		}
		return Collections.emptyList();
	}

	@Override
	public List<E> delete(Function<DeleteBuilder, DeleteBuilder> b) throws SQLException {
		return delete(b.apply(DEFAULT_DELETE_QUERY_UNCONDITIONAL.builder()).build());
	}

	@Override
	public List<E> delete(DeleteQuery q) throws SQLException {
		if (q.getCriteriumValues().length == 0) {
			DB.executeUpdate(q.getQueryString());
		} else {

			Connection c = DB.getConnection();
			try (PreparedStatement ps = c.prepareStatement(q.getQueryString())) {
				fillPreparedStatementAutoType(ps, q.getCriteriumValues());
				ps.execute();
			} finally {
				try {
					checkClose();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	public Optional<E> deleteById(ID id) throws SQLException {
		List<E> ls = delete(DEFAULT_DELETE_QUERY_BY_ID.withCriteriumValues(id));
		if (ls.size() == 1) {
			return Optional.of(ls.get(0));
		}
		return Optional.empty();
	}

	/*
	 * Aggregate Functions
	 */

	@Override
	public long count(String field) throws SQLException {
		List<Long> result = selectLong(queryBuilderFactory.select(this).selectCount(this, field).build());
		if (result.size() == 1) {
			return result.get(0).longValue();
		}
		return 0L;
	}

	@Override
	public long countDistinct(String field) throws SQLException {
		List<Long> result = selectLong(queryBuilderFactory.select(this).distinct().selectCount(this, field).build());
		if (result.size() == 1) {
			return result.get(0).longValue();
		}
		return 0L;
	}

	@Override
	public long min(String field) throws SQLException {
		List<Long> result = selectLong(queryBuilderFactory.select(this).distinct().selectMin(this, field).build());
		if (result.size() == 1) {
			return result.get(0).longValue();
		}
		return 0L;
	}

	@Override
	public long max(String field) throws SQLException {
		List<Long> result = selectLong(queryBuilderFactory.select(this).distinct().selectMax(this, field).build());
		if (result.size() == 1) {
			return result.get(0).longValue();
		}
		return 0L;
	}

	@Override
	public long sumInteger(String field) throws SQLException {
		List<Long> result = selectLong(queryBuilderFactory.select(this).distinct().selectSum(this, field).build());
		if (result.size() == 1) {
			return result.get(0).longValue();
		}
		return 0L;
	}

	@Override
	public double sumDouble(String field) throws SQLException {
		List<Double> result = selectDouble(queryBuilderFactory.select(this).distinct().selectSum(this, field).build());
		if (result.size() == 1) {
			return result.get(0).doubleValue();
		}
		return 0.0d;
	}

	@Override
	public double average(String field) throws SQLException {
		List<Double> result = selectDouble(
				queryBuilderFactory.select(this).distinct().selectAverage(this, field).build());
		if (result.size() == 1) {
			return result.get(0).doubleValue();
		}
		return 0.0d;
	}

	/*
	 * * * OLD CRUD
	 */

	/**
	 * lock write operations for this thread
	 * 
	 * @return true if the lock was acquired
	 * 
	 * @throws InterruptedException
	 */
	protected boolean lock() throws InterruptedException {
		return lock.tryLock() || lock.tryLock(5, TimeUnit.SECONDS);
	}

	/**
	 * releases the write lock
	 */
	protected void unlock() {
		lock.unlock();
	}

	private E setId(E item, ID id) {
		// TODO revise setId?
		Method setter = MAPPING_DEFINITION_PK.setters[0];
		if (Objects.nonNull(ENTITY_BUILDER_CLASS)) {
			try {
				EntityBuilder<ID> b = ENTITY_CLASS_BUILDER_COPY_CONSTRUCTOR.newInstance(item);
				b.setId(id);
				return (E) b.build();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				LOGGER.error(e);
			}
		} else {
			try {
				setter.invoke(item, id);
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
		return item;
	}

	/**
	 * saves (inserts or updates) the given item.
	 * 
	 * @param item            the item to save (insert or update)
	 * @param closeConnection true if the connection should be closed after the
	 *                        saving process.
	 * @param user            the user name to insert into the logs record.
	 * @param stampUpdateMeta true if the items update metadata field should be
	 *                        stamped.
	 * @param logWrite        true if the saving action should be logged in the
	 *                        audit log.
	 * 
	 * @return the item as it was loaded from the database.
	 * 
	 * @throws SQLException
	 */
	private E save(E item, boolean closeConnection, String user, boolean stampUpdateMeta, boolean logWrite)
			throws SQLException {
		try {
			if (lock()) {
				try {
					E existing = null;
					ID id = item.getId();
					boolean insert = false;
					if (Objects.isNull(id)) {
						insert = true;
					} else if (id instanceof Number && ((Number) id).longValue() == 0L) {
						insert = true;
					} else if (id instanceof String && ((String) id).trim().isEmpty()) {
						insert = true;
					} else {
						Optional<E> existingOpt = selectById(id);
						if (existingOpt.isPresent()) {
							existing = existingOpt.get();
						} else {
							insert = true;
						}
					}
					try {
						if (insert) {
							item = insert(item, user, closeConnection);
						} else {
							item = update(item, existing, user, closeConnection, stampUpdateMeta, logWrite);
						}
//					item = saveMany(item);
					} catch (SecurityException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						LOGGER.error(e);
					}
				} finally {
					unlock();
				}
			} else {}
		} catch (InterruptedException e) {
			LOGGER.error(e);
			throw new RuntimeException("");
		}
		return item;
	}

	@Override
	public List<E> save(List<E> item) throws SQLException {
		List<E> result = new ArrayList<>();
		for (int i = 0; i < item.size(); i++) {
			Optional<E> opt = save(item.get(i));
			if (opt.isPresent()) {
				result.add(opt.get());
			}
		}
		return result;
	}

	@Override
	public Optional<E> save(E item) throws SQLException {

		return Optional.ofNullable(
				save(item, LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION, DB.getActiveUser(), true, true));
	}

	private E saveMany(E item) throws SQLException {
		Set<String> keys;
		if ((keys = manyToManyMapping.keySet()).size() > 0) {
//			System.err.println(keys);
			for (String k : keys) {
				ManyToManyMapping m = getManyToManyMapping(k);
				try {
					@SuppressWarnings("unchecked")
					List<? extends Entity<?>> data = (List<? extends Entity<?>>) m.getter.invoke(item);
					if (Objects.nonNull(data)) {
						System.err.println("data " + m.type + " for " + ENTITY_NAME + "." + m.manyFieldName
								+ " contains " + data.size() + " items");
						if (data.size() > 0) {
							if (m.type == ManyToMany.Type.VIA_A) {
								m.mtmRepository.saveByA(item, data);
							} else if (m.type == ManyToMany.Type.VIA_B) {
								m.mtmRepository.saveByB(item, data);
							} else if (m.type == ManyToMany.Type.DIRECT) {
								data.stream().forEach(o -> {
									try {
										m.mtmRepository.saveDirect(o);
									} catch (SQLException e) {
										LOGGER.error(e);
									}
								});
							}
						}

					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					LOGGER.error(e);
				}
			}
		}
		return item;
	}

	public E insert(E item) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			SQLException, InterruptedException {
		return insert(item, getDB().getActiveUser(), LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION);
	}

	private E insert(E item, String user, boolean closeConnection)
			throws SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		boolean generateUUID = false;
		ID id = item.getId();
		if (Objects.nonNull(id) && id instanceof Number && ((Number) id).longValue() == 0L) {
			item = setId(item, null);
		} else if (PK_IS_UUID && ID_CLASS == String.class) {
			generateUUID = true;
		}
		if (generateUUID) {
			@SuppressWarnings("unchecked")
			ID nextUUID = (ID) DB.nextUUID();
			item = setId(item, nextUUID);
		}
		Connection c = DB.getConnection();
		stampMeta(item, CRUD.C, true);
		String sqlInsert = DEFAULT_INSERT_QUERY.getQueryString();
		logQuery(sqlInsert);
		try (PreparedStatement ps = c.prepareStatement(sqlInsert)) {
			fillPreparedStatement(ps, MAPPING_DEFINITION_PK_LEADING.getters, MAPPING_DEFINITION_PK_LEADING.transforms,
					MAPPING_DEFINITION_PK_LEADING.sqlTypes, item);
			ps.executeUpdate();
			if (PK_IS_AUTO_GENERATED) {
				Long l = JDBCUtils.lastInsertIntegerId(ps);
				item = setId(item, (ID) l);
			}
			log(item.getId(), CRUD.C, user, true);
		}
		saveMany(item);

		return item;
	}

	@Override
	public Optional<E> update(E item) throws SQLException {
		Optional<E> existingOpt = selectById(item.getId());
		if (existingOpt.isPresent()) {
			E existing = existingOpt.get();
			return Optional.ofNullable(update(item, existing, DB.getActiveUser(),
					LumicoreProperties.CLOSE_CONNECTION_AFTER_OPERATION, true, true));
		}
		throw new SQLException("Record to update not found");
	}

	private E update(E item, E existing, String user, boolean closeConnection, boolean stampUpdateMeta,
			boolean logWrite) throws SQLException {
		// update
		Connection c = DB.getConnection();
		Optional<MappingDefinition> deltaDefinitionOpt = getDelta(existing, item);
		if (deltaDefinitionOpt.isPresent()) {
			stampMeta(item, CRUD.U, stampUpdateMeta);
			MappingDefinition deltaDefinition = deltaDefinitionOpt.get();
			UpdateBuilder sqlUpdateDeltaBuilder = DB.getQueryBuilderFactory().update(this);
			{
				int i = 0;
				for (String dt : deltaDefinition.sqlNames) {
					try {
						sqlUpdateDeltaBuilder.set(dt, deltaDefinition.getters[i++].invoke(item));
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						LOGGER.error(e);
					}
				}
			}
			{
				int i = 0;
				CriteriaBuilder<UpdateBuilder> cb = sqlUpdateDeltaBuilder.withCriteria(this);
				for (String n : MAPPING_DEFINITION_PK.sqlNames) {
					if (i > 0) {
						cb.and();
					}
					try {
						cb.equals(n, MAPPING_DEFINITION_PK.getters[i].invoke(item));
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						LOGGER.error(e);
					}
					i++;
				}
			}
			UpdateQuery sqlUpdateDeltaQuery = sqlUpdateDeltaBuilder.build();
			String sqlUpdateDelta = sqlUpdateDeltaQuery.getQueryString();

			logQuery(sqlUpdateDelta);
			try (PreparedStatement ps = c.prepareStatement(sqlUpdateDelta)) {
				int nextPos = fillPreparedStatement(ps, deltaDefinition.getters, deltaDefinition.transforms,
						deltaDefinition.sqlTypes, item);
				fillPreparedStatement(ps, MAPPING_DEFINITION_PK.getters, MAPPING_DEFINITION_PK.transforms,
						MAPPING_DEFINITION_PK.sqlTypes, item, nextPos);
				ps.executeUpdate();
				log(item.getId(), CRUD.U, user, logWrite);
			} catch (SQLException e) {
				LOGGER.error(sqlUpdateDelta);
				throw e;
			}
		}
		saveMany(item);
		return item;
	};

	private Optional<MappingDefinition> getDelta(E existing, E item) {
		int size = MAPPING_DEFINITION_PK_LEADING.sqlNames.length;
		List<Integer> delta = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			if (!valueEquals(existing, item, MAPPING_DEFINITION_PK_LEADING.getters[i])) {
				delta.add(i);
			}
		}
		if (delta.size() == 0) {
			return Optional.empty();
		}
		MappingDefinition deltaDefinition = new MappingDefinition(delta.size());
		for (int i = 0; i < deltaDefinition.sqlNames.length; i++) {
			int deltaIndex = delta.get(i);
			deltaDefinition.lumicoreTypes[i] = MAPPING_DEFINITION_PK_LEADING.lumicoreTypes[deltaIndex];
			deltaDefinition.getters[i] = MAPPING_DEFINITION_PK_LEADING.getters[deltaIndex];
			deltaDefinition.javaNames[i] = MAPPING_DEFINITION_PK_LEADING.javaNames[deltaIndex];
			deltaDefinition.mappingTypes[i] = MAPPING_DEFINITION_PK_LEADING.mappingTypes[deltaIndex];
			deltaDefinition.setters[i] = MAPPING_DEFINITION_PK_LEADING.setters[deltaIndex];
			deltaDefinition.sqlNames[i] = MAPPING_DEFINITION_PK_LEADING.sqlNames[deltaIndex];
			deltaDefinition.sqlTypes[i] = MAPPING_DEFINITION_PK_LEADING.sqlTypes[deltaIndex];
			deltaDefinition.transforms[i] = MAPPING_DEFINITION_PK_LEADING.transforms[deltaIndex];
		}
		return Optional.of(deltaDefinition);
	}

	private boolean valueEquals(E a, E b, Method getter) {
		try {
			Object valA = getter.invoke(a);
			Object valB = getter.invoke(b);
			if (Objects.equals(valA, valB)) {
				return true;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.error(e);
		}
		return false;
	}

	@Override
	public boolean exists(ID id) throws SQLException {
		return selectById(id).isPresent();
	}

	/*
	 * * * Tree Entity
	 */

	@Override
	public <T extends TreeEntity<T, ID>> void resolve(T item) {
		// TODO void resolve(T item)
		resolve(item, -1);
	}

	@Override
	public <T extends TreeEntity<T, ID>> void resolve(T item, int depth) {
		// TODO void resolve(T item, int depth)
		throw new RuntimeException("NIY");
	}

//	public <T extends TreeEntity<T, ID>> 
	@Override
	public List<E> childrenOf(TreeEntity<?, ?> parent) throws SQLException {
		if (Objects.nonNull(PARENT_FIELD_NAME) && (Objects.isNull(parent) || Objects.isNull(parent.getId()))) {
			return select(DEFAULT_SELECT_QUERY_BY_TOP_PARENT);
		} else if (Objects.nonNull(PARENT_FIELD_NAME) && (parent.getClass() == PARENT_CLASS)) {
			return select(DEFAULT_SELECT_QUERY_BY_PARENT.withCriteriumValues(parent.getId()));
		}
		return Collections.emptyList();
	}

	@Override
	public List<?> foreignChildrenOf(TreeEntity<?, ?> parent) throws SQLException {
		List<TreeEntity<?, ?>> children = new ArrayList<TreeEntity<?, ?>>();
		for (Repository<?, ?> childRepo : childRepositories) {
			/*
			 * Checked: childRepositories only contains repositories that manage
			 * TreeEntity<?, ?> implementations.
			 */
			@SuppressWarnings("unchecked")
			List<TreeEntity<?, ?>> childrenOf = (List<TreeEntity<?, ?>>) childRepo.childrenOf(parent);
			children.addAll(childrenOf);
		}
		return children;
	}

	@Override
	public void registerChildRepository(Repository<?, ?> repository) {
		childRepositories.add(repository);
	}

	/*
	 * * * Joins
	 */

	@Override
	public void joinEntity(SelectBuilder s, String schemaName, Class<? extends Entity<?>> entity, String foreignKey,
			String referencedField) {
		joinEntity(s, schemaName, entity, foreignKey, referencedField, RESOLVE_DEPTH);
	}

	@Override
	public void joinEntity(SelectBuilder s, String schemaName, Class<? extends Entity<?>> entity, String foreignKey,
			String referencedField, int self) {
		if (entity.equals(ENTITY_CLASS)) {
			if (self == -1) {
				self = 0;
			} else if (self == 0) {
				return;
			} else {
				self--;
			}
		}
		String[] alias = new String[] { null };
		s.join(schemaName, entity, DB.getSchemaName(), ENTITY_CLASS, alias, JoinOperator.LEFT, JoinOperator.JOIN)
				.on(foreignKey, referencedField)
				.select(DB.getSchemaName(), ENTITY_CLASS, MAPPING_DEFINITION_PK_LEADING.sqlNames);

		for (int i = 0; i < JOIN_MAPPINGS.length; i++) {
			JoinMapping jm = JOIN_MAPPINGS[i];
			if (jm.eager) {
				jm.repository.joinEntity(s, schemaName, ENTITY_CLASS, jm.fieldName, jm.referencedFieldName, self);
			}
		}
	}

	private final ManyToManyMapping getManyToManyMapping(String fieldName) throws NullPointerException {
		ManyToManyMapping mtmMapping = manyToManyMap.get(fieldName);
		if (Objects.isNull(mtmMapping)) {
			ManyToMany.Type mtmType = manyToManyMappingType.get(fieldName);
			ManyToManyRepository<?, ?, ?> mtmRepo = null;
			Class<?>[] mapping = manyToManyMapping.get(fieldName);
			String manyFieldName = null;
			if (mapping.length == 1) {
				/*
				 * values are sanitized on input
				 */
				@SuppressWarnings("unchecked")
				Class<? extends Entity<?>> mtmEntity = (Class<? extends Entity<?>>) mapping[0];
				mtmRepo = DB.getManyToManyRepository(mtmEntity);
				mtmType = ManyToMany.Type.DIRECT;
			} else {
				/*
				 * values are sanitized on input
				 */
				@SuppressWarnings("unchecked")
				Class<? extends Entity<?>> mtmEntityA = (Class<? extends Entity<?>>) mapping[0];
				/*
				 * values are sanitized on input
				 */
				@SuppressWarnings("unchecked")
				Class<? extends Entity<?>> mtmEntityB = (Class<? extends Entity<?>>) mapping[1];
				mtmRepo = DB.getManyToManyRepository(mtmEntityA, mtmEntityB);
				if (ManyToMany.Type.AUTO.equals(mtmType)) {
					if (Reflect.typeEquals(mtmRepo.getClassA(), ENTITY_CLASS)) {
						mtmType = ManyToMany.Type.VIA_A;
					} else {
						mtmType = ManyToMany.Type.VIA_B;
					}
					if (Reflect.typeEquals(mtmRepo.getClassA(), ENTITY_CLASS)) {
						manyFieldName = mtmRepo.getFieldNameA();
					} else if (Reflect.typeEquals(mtmRepo.getClassB(), ENTITY_CLASS)) {
						manyFieldName = mtmRepo.getFieldNameB();
					}
				} else if (ManyToMany.Type.VIA_A.equals(mtmType)) {
					manyFieldName = mtmRepo.getFieldNameA();
				} else if (ManyToMany.Type.VIA_B.equals(mtmType)) {
					manyFieldName = mtmRepo.getFieldNameB();
				}
			}
//			String javaFieldName = Reflect.getReferencedField(ENTITY_CLASS, fieldName).getName();
			Method setter = null;
			Method getter = null;
			try {
				// TODO Support other collections than list
				if (Objects.isNull(ENTITY_BUILDER_CLASS)) {
					setter = Reflect.getSetter(ENTITY_CLASS, fieldName, List.class);
					getter = Reflect.getGetter(ENTITY_CLASS, fieldName);
				} else {
					setter = Reflect.getSetter(ENTITY_BUILDER_CLASS, fieldName, List.class);
					getter = Reflect.getGetter(ENTITY_CLASS, fieldName);
				}
			} catch (NoSuchMethodException | SecurityException e) {
				LOGGER.error(e);
			}
			mtmMapping = new ManyToManyMapping(manyFieldName, getter, setter, mtmRepo, mtmType);
			manyToManyMap.put(fieldName, mtmMapping);
		}
		return mtmMapping;
	}

	private OneToManyMapping getOneToManyMapping(String fieldName) throws NullPointerException {
		OneToManyMapping otmMapping = oneToManyMap.get(fieldName);
		if (Objects.isNull(otmMapping)) {
			Class<?> otherEntity = oneToManyMapping.get(fieldName);
			/*
			 * Values are sanitized on input
			 */
			@SuppressWarnings("unchecked")
			Repository<?, ?> otmRepo = DB.getRepository((Class<? extends Entity<?>>) otherEntity);
//			String oneFieldName;
//			if (ENTITY_NAME.length() > 1) {
//				oneFieldName = ENTITY_NAME.substring(0, 1).toLowerCase() + ENTITY_NAME.substring(1);
//			} else {
//				oneFieldName = ENTITY_NAME.toLowerCase();
//			}
			Class<?> thisEntity = ENTITY_CLASS;
			Field manyField = null;
			outer: do {
				Field[] fields = thisEntity.getDeclaredFields();
				for (Field f : fields) {
					if (f.isAnnotationPresent(OneToMany.class) && Reflect.typeEquals(f.getType(), List.class)) {
						Class<?> collectionType = Reflect.getCollectionTypeFromField(f);
						if (Objects.nonNull(collectionType) && Reflect.typeEquals(collectionType, otherEntity)) {
							manyField = f;
							break outer;
						}
					}
				}
			} while (!Reflect.typeEquals(thisEntity = thisEntity.getSuperclass(), Object.class));
			OneToMany otm = manyField.getAnnotation(OneToMany.class);
			String referencedFieldName = otm.referencedFieldName();
			String javaFieldName = manyField.getName();
			Method setter = null;
			try {
				// after passing the integrity check on startup this setter must exist
				if (Objects.isNull(ENTITY_BUILDER_CLASS)) {
					setter = Reflect.getGetter(ENTITY_CLASS, javaFieldName);
				} else {
					setter = Reflect.getGetter(ENTITY_BUILDER_CLASS, javaFieldName);
				}
			} catch (NoSuchMethodException | SecurityException e) {
				LOGGER.error(e);
			}
			// TODO Support other collections than list
			Method getter = null;
			try {
				// after passing the integrity check on startup this getter must exist
//				if (Objects.isNull(ENTITY_BUILDER_CLASS)) {
				getter = Reflect.getSetter(ENTITY_CLASS, javaFieldName, List.class);
//				} else {
//					getter
//				}
			} catch (NoSuchMethodException | SecurityException e) {
				LOGGER.error(e);
			}

			String oneFieldName = "";

			Class<?> cls = otherEntity;

			outer: do {
				Field[] fields = cls.getDeclaredFields();
				for (Field f : fields) {
					if (Reflect.typeEquals(f.getType(), ENTITY_CLASS)
							&& (referencedFieldName.length() > 0 ? ORM.getFieldName(f).equals(referencedFieldName)
									: true)) {
						oneFieldName = ORM.getFieldName(f);
						break outer;
					}
				}

			} while (!Reflect.typeEquals(cls = cls.getSuperclass(), Object.class));

			otmMapping = new OneToManyMapping(oneFieldName, getter, setter, otmRepo);
			oneToManyMap.put(fieldName, otmMapping);
		}
		return otmMapping;
	}

	/*
	 * * * Logging
	 */

	@Override
	public LogRepository<E, ID> getLogRepository() {
		return logRepository;
	}

	/**
	 * Logs an action in the log if logging is enabled.
	 * <p>
	 * This method has the following abort conditions:
	 * <ol>
	 * <li>the argument act is false
	 * <li>logging is not enabled
	 * </ol>
	 * 
	 * @param id     the record id this action was performed on
	 * @param action the action that was performed (one of the constants of
	 *               AbstractRepository.CRUDL)
	 * @param user   the user this action was performed by
	 * @param act    false to abort the logging operation
	 * 
	 * @throws SQLException
	 * @throws IllegalArgumentException if any other action char argument is used
	 *                                  than defined in AbstractRepository.CRUDL
	 *                                  (case insensitive)
	 */
	protected void log(ID id, char action, String user, boolean act) throws SQLException {
		if (!act || Objects.isNull(logRepository)) {
			return;
		}
		action = Character.toUpperCase(action);
		switch (action) {
		case CRUD.C:
		case CRUD.R:
		case CRUD.U:
		case CRUD.D:
			logRepository.log(id, action, user);
			break;
		default:
			throw new IllegalArgumentException(String.format(
					"%s >> AbstractRepository.logAudit: Action not recognized: '%s' should be either one of [C | R | U | D | L]",
					getClass().getSimpleName(), action));
		}
	}

	/**
	 * Lists the auditing history of the specified record.
	 * 
	 * @param id the record id to get the history of actions of.
	 * 
	 * @return the log of actions associated with the specified id or null if
	 *         auditing is not enabled.
	 * 
	 * @throws SQLException
	 */
	@Override
	public List<Log> listHistory(ID id) throws SQLException {
		if (Objects.nonNull(logRepository)) {
			try {
				return logRepository.listLogByItemId(id);
			} catch (InterruptedException e) {
				LOGGER.error(e);
			}
		}
		return Collections.emptyList();
	}

	/*
	 * * * Metadata
	 */

	/**
	 * stamps the items specified metadata field with the current system time
	 * <p>
	 * the method has the following abort conditions:
	 * <ol>
	 * <li>the argument act is false
	 * <li>the item doesn't implement the MetaEntity interface
	 * </ol>
	 * 
	 * @param item   the item to stamp
	 * @param action one of the char constants from the protected class
	 *               AbstractRepository.CRUDL
	 * @param act    false to abort the stamping
	 * 
	 * @return true if the operation was executed, false if any of the abort
	 *         conditions was true
	 * 
	 * @throws IllegalArgumentException if any other action char is used as action
	 *                                  argument than defined in (case insensitive)
	 *                                  AbstractRepository.CRUDL
	 * 
	 * @see System#currentTimeMillis()
	 */
	protected boolean stampMeta(E item, char action, boolean act) {
		if (!act || !(item instanceof AbstractMutableMetaEntity<?>)) {
			return false;
		}
		try {
			long t = System.currentTimeMillis();
			char x = Character.toUpperCase(action);
			switch (x) {
			case CRUD.C:
				((AbstractMutableMetaEntity<?>) item).setCreateDate(t);
				return true;
			case CRUD.R:
				((AbstractMutableMetaEntity<?>) item).setReadDate(t);
				return true;
			case CRUD.U:
				((AbstractMutableMetaEntity<?>) item).setUpdateDate(t);
				return true;
			case CRUD.D:
				((AbstractMutableMetaEntity<?>) item).setDeleteDate(t);
				return true;
			default:
				String errorMessage = "%s >> AbstractRepository.stampMeta: Action not recognized: '%s' "
						+ "should be either one of [C | R | U | D]";
				throw new IllegalArgumentException(String.format(errorMessage, getClass().getSimpleName(), action));
			}
		} catch (IllegalAccessException e) {}
		return false;
	}

//	@Override
//	public Class<? extends Entity<?>> getEntityInterfaceClass() {
//		return ENTITY_INTERFACE_CLASS;
//	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
//		String format = "%s%n";
		sb.append(ApplicationUtils.createBanner(getClass().getSimpleName())).append("\n");
		return sb.toString();
	}

	/*
	 * * * Inner classes
	 */

	/**
	 * Class containing constants for audit logging and metadata time stamping
	 */
	protected final static class CRUD {
		/**
		 * {@link CRUD#C} indicates a create action
		 */
		public static final char C = 'C';
		/**
		 * {@link CRUD#R} indicates a read action
		 */
		public static final char R = 'R';
		/**
		 * {@link CRUD#U} indicates an update action
		 */
		public static final char U = 'U';
		/**
		 * {@link CRUD#D} indicates a delete action
		 */
		public static final char D = 'D';

		private CRUD() {}
	} // end of class CRUD

	private static final class OneToManyMapping {
		private final String oneFieldName;
//		private final Method getter;
		private final Method setter;
		private final Repository<?, ?> otmRepository;

		private OneToManyMapping(String oneFieldName, Method getter, Method setter, Repository<?, ?> otmRepository) {
			super();
			this.oneFieldName = oneFieldName;
//			this.getter = getter;
			this.setter = setter;
			this.otmRepository = otmRepository;
		}
	}

	private static final class ManyToManyMapping {

		private final String manyFieldName;
//		private final String javaFieldName;
		private final Method getter;
		private final Method setter;
		private final ManyToManyRepository<?, ?, ?> mtmRepository;
		private final ManyToMany.Type type;

		private ManyToManyMapping(String manyFieldName, Method getter, Method setter,
				ManyToManyRepository<?, ?, ?> mtmRepository, ManyToMany.Type type) {
			super();
			this.manyFieldName = manyFieldName;
			this.getter = getter;
			this.setter = setter;
			this.mtmRepository = mtmRepository;
			this.type = type;
		}

	}

	private final static class JoinMapping {
		private final boolean eager;
//		private final Class<?> mappingType;
		private final int sqliteType;
		private final int lumicoreType;
		private final String fieldName;
		private final String referencedFieldName;
		private final Class<? extends Entity<?>> referencedEntity;
		private final Method getter;
//		private Method setter;
		private final int mappingIndex;
		private final Repository<?, ?> repository;

		private JoinMapping(String entityName, Field field, Repository<?, ?> repository, int mappingIndex,
				Dialect dialect) {
			if (!Entity.class.isAssignableFrom(field.getType())) {
				throw new IllegalArgumentException(String.format("the field type of %s.%s should implement Entity<T>!",
						entityName, field.getName()));
			}
			this.eager = ORM.isEager(field);
			this.fieldName = ORM.getFieldName(field);
			this.referencedFieldName = ORM.getReferencedFieldName(field);
			/*
			 * Checked: field type is assignable to Entity<?>
			 */
			@SuppressWarnings("unchecked")
			Class<? extends Entity<?>> e = (Class<? extends Entity<?>>) field.getType();
			this.referencedEntity = e;
			this.repository = repository;
			this.mappingIndex = mappingIndex;
//			Field referencedField = ORM.getReferencedField(referencedEntity, referencedFieldName);
//			this.mappingType = referencedField.getType();

//			this.sqliteType = rep.getDefinition(this.fieldName).getReferencingType();
			this.lumicoreType = dialect.getReferencingType(referencedEntity, referencedFieldName);
			this.sqliteType = dialect.getSQLType(lumicoreType);
			Method mGetter = null;
			try {
				mGetter = Reflect.getGetter(referencedEntity, referencedFieldName);
			} catch (NoSuchMethodException | SecurityException e1) {
				e1.printStackTrace();
			}
			this.getter = mGetter;
//			setter = Reflect.getSetter(referencedEntity, referencedFieldName, mappingType);
		}

	}

	/**
	 * MappingDefinition holds a specific list of names and types of the fields of
	 * an entity (all, pk or non-pk)
	 */
	public final class MappingDefinition {
		/**
		 * The field names as they are defined in the java class. used for reflection.
		 */
		private final String[] javaNames;

//		private final String[] joinedJavaNames;

		/**
		 * The field names as they are named in the database table. used for SQL
		 * statements.
		 */
		final String[] sqlNames;

//		private final String[] joinedSqlNames;
		/**
		 * The java mapping types
		 */
		private final Class<?>[] mappingTypes;

		private final int[] sqlTypes;

		private final int[] lumicoreTypes;

//		private final Class<?>[] joinedMappingTypes;

		private final Method[] getters;

		private final Method[] setters;

		private final Transform[] transforms;

		/**
		 * Construct a new MappingDefinition
		 * 
		 * @param n the number of fields in this list
		 */
		private MappingDefinition(int n) {
			javaNames = new String[n];
			sqlNames = new String[n];
			mappingTypes = new Class<?>[n];
			sqlTypes = new int[n];
			lumicoreTypes = new int[n];
			getters = new Method[n];
			setters = new Method[n];
			transforms = new Transform[n];
//			joinedJavaNames = new String[n];
//			joinedSqlNames = new String[n];
//			joinedMappingTypes = new Class<?>[n];
		}

		private final void initializeMethods() {
			for (int i = 0; i < javaNames.length; i++) {
				try {
					getters[i] = Reflect.getGetter(SQLRepository.this.ENTITY_CLASS, javaNames[i]);
					if (MutableEntity.class.isAssignableFrom(ENTITY_CLASS)) {
						setters[i] = Reflect.getSetter(SQLRepository.this.ENTITY_CLASS, javaNames[i], mappingTypes[i]);
					} else {
						setters[i] = Reflect.getSetter(SQLRepository.this.ENTITY_BUILDER_CLASS, javaNames[i],
								mappingTypes[i]);

					}
//					transforms[i] = getTransform(mappingTypes[i]);
				} catch (NoSuchMethodException | SecurityException e) {
					LOGGER.error(e);
					throw new RuntimeException(String.format("Getter/Setter not found for %s.%s",
							SQLRepository.this.ENTITY_NAME, javaNames[i]));
				}
			}
		}
	} // End of MappingDefinition

	/**
	 * Naming utility class
	 * <p>
	 * not static because it must be able to inspect AbstractRepository.this
	 */
	private final class Naming {
		// TODO check allowed characters in name
//		private final List<Character> allowedChars = null;
//		private final List<Character> allowedFirstChars = null;

		/**
		 * tests whether the given table name is illegal
		 * 
		 * @param name the table name
		 * 
		 * @return true if the table name is illegal
		 */
		private final boolean isIllegalTableName(String name) {
			return isIllegalName(name, true);
		}

		/**
		 * tests whether the given field name is illegal
		 * 
		 * @param name the field name to test
		 * 
		 * @return true if the given field name is illegal
		 */
		private final boolean isIllegalFieldName(String name) {
			return isIllegalName(name, false);
		}

		/**
		 * Tests whether the given name is illegal
		 * 
		 * @param name  the name to test
		 * @param table true if the name is a table name, false if it is a field name
		 * 
		 * @return true if the given name is illegal
		 */
		private final boolean isIllegalName(String name, boolean table) {
			if (table && SQLRepository.this instanceof MetaRepository) {
				return false;
			} else if (table && SQLRepository.this instanceof LogRepository) {
				return false;
			} else if (table) {
				if (name.startsWith("sqlite_")) {
					return true;
				} else if (name.startsWith("lumicore_")) {
					return true;
				}
			}
			return false;
		}

		/**
		 * tests if the given String[] contains duplicate names (collisions)
		 * <p>
		 * the list is being sorted by this method
		 * 
		 * @param names the names to test
		 * 
		 * @return null if no duplicate was found, the duplicate name otherwise
		 */
		private final String containsCollision(List<String> names) {
			Collections.sort(names);
			for (int i = 0; i < names.size() - 1; i++) {
				if (names.get(i).equals(names.get(i + 1))) {
					return names.get(i);
				}
			}
			return null;
		}
	} // end of class Naming

	/**
	 * Generalization class to compare two fields for sorting
	 * 
	 * @see FieldDefinitionComparator
	 * @see TableInfoComparator
	 */
	private final static class FieldComparison {
		/**
		 * Compares two database table fields. primarily orders by primary key (pk < non
		 * pk), then by field name.
		 * 
		 * @param fldAisPK true if field 1 is primary key
		 * @param fldBisPK true if field 2 is primary key
		 * @param nameA    the name of field 1
		 * @param nameB    the name of field 2
		 * 
		 * @return the comparison value for sorting with a {@code Comparator<?>}.
		 */
		private final static int compare(boolean fldAisPK, boolean fldBisPK, String nameA, String nameB) {
			int r = Integer.compare(fldAisPK ? 0 : 1, fldBisPK ? 0 : 1);
			if (r == 0) {
				r = nameA.toLowerCase().compareTo(nameB.toLowerCase());
			}
			return r;
		}

		private FieldComparison() {}
	} // End of class FieldComparison

	/**
	 * Comparator for sorting a {@code List<FieldDefinitionContainer>}
	 *
	 * @see FieldComparison#compare(boolean, boolean, String, String)
	 */
	private final class FieldDefinitionComparator implements Comparator<FieldDefinitionContainer> {
		@Override
		public int compare(FieldDefinitionContainer o1, FieldDefinitionContainer o2) {
			return FieldComparison.compare(o1.isPK(), o2.isPK(), o1.getSQLName(), o2.getSQLName());
		}
	} // End of class FieldDefinitionComparator

	/**
	 * Comparator for sorting a {@code List<TableInfo>}
	 *
	 * @see FieldComparison#compare(boolean, boolean, String, String)
	 */
	private final static class TableInfoComparator implements Comparator<TableInfo> {
		@Override
		public int compare(TableInfo o1, TableInfo o2) {
			return FieldComparison.compare(o1.isPk(), o2.isPk(), o1.getName(), o2.getName());
		}
	} // End of class TableInfoComparator

	/**
	 * Container for temporarily storing the results of the field scan.
	 */
	private final class FieldDefinitionContainer {
		private final String javaFieldName;
		private final String referencedFieldName;
		private final Field field;
//		private final Field referencedField;
		private final Class<?> mappingType;
		private final Column fieldDefinition;
		private final OneToOne oneToOne;
		private final ManyToOne manyToOne;
		private final PrimaryKey primaryKey;
		private final NotNull notNull;
		/**
		 * this boolean is false by default. it is the default value to return for the
		 * method {@link #isPK()} if the type of the field is not specifically defined
		 * as primary key. it is set true by the method
		 * {@link SQLRepository#scanEntity(Class, PrimaryKeyConstraint, List)} if the
		 * field is contained in a primary key constraint.
		 */
		private boolean pk;

//		private FieldDefinitionContainer(String javaFieldName, Class<?> mappingType) {
//			this(javaFieldName, mappingType, FieldDefinition.DEFAULT_DEFINITION, null, null);
//		}

		/**
		 * Construct a new {@link FieldDefinitionContainer}
		 * 
		 * @param field the java field
		 */
		private FieldDefinitionContainer(Field field) {
			this(field, field.getType());
		}

		public FieldDefinitionContainer(Field field, Class<?> actualType) {
			this.javaFieldName = field.getName();
			this.mappingType = actualType;
			this.fieldDefinition = field.getAnnotation(Column.class);
			this.oneToOne = field.getAnnotation(OneToOne.class);
			this.manyToOne = field.getAnnotation(ManyToOne.class);
			this.primaryKey = field.getAnnotation(PrimaryKey.class);
			this.notNull = field.getAnnotation(NotNull.class);
			this.field = field;
			if (Entity.class.isAssignableFrom(field.getType())) {
				this.referencedFieldName = ORM.getReferencedFieldName(field);
//				/*
//				 * Checked: field type is assignable to Entity<?>
//				 */
//				@SuppressWarnings("unchecked")
//				Class<? extends Entity<?>> e = (Class<? extends Entity<?>>) field.getType();
//				if (e.isInterface()) {
//					e = DB.getEntityImplementationClass(e);
//				}
//				this.referencedField = ORM.getReferencedField(e, referencedFieldName);
			} else {
//				this.referencedField = null;
				this.referencedFieldName = null;
			}
		}

		/**
		 * Gets the SQL field name
		 * 
		 * @return the defined field name from the @FieldDefinition annotation if set,
		 *         the java field name otherwise
		 */
		private final String getSQLName() {
			return ORM.getFieldName(field);
		}

		private int getType() throws ConfigurationException {
			if (Objects.nonNull(fieldDefinition) && fieldDefinition.type() != Definition.TYPE_AUTO) {
				return fieldDefinition.type();
			} else if (Entity.class.isAssignableFrom(mappingType)) {
				/*
				 * Checked: mapping type is assignable to Entity<?>
				 */
				@SuppressWarnings("unchecked")
				Class<? extends Entity<?>> e = (Class<? extends Entity<?>>) mappingType;
				return DB.getDialect().getReferencingType(e, referencedFieldName);
			} else {
				int type = DB.getDialect().autoDetectType(mappingType);
				if (type != Definition.TYPE_NOT_SUPPORTED) {
					if (Objects.nonNull(primaryKey)) {
						switch (type) {
						case Definition.TYPE_INTEGER:
							if (field.isAnnotationPresent(AutoIncrement.class)) {
								return Definition.TYPE_INTEGER_PK_AI;
							} else {
								return Definition.TYPE_INTEGER_PK;
							}
						case Definition.TYPE_TEXT:
							if (primaryKey.autoGenerated()) {
								return Definition.TYPE_TEXT_PK_UUID;
							} else {
								return Definition.TYPE_TEXT_PK;
							}
						case Definition.TYPE_REAL:
							return Definition.TYPE_REAL_PK;
//						case Definition.TYPE_BLOB:
//							return ???
						default:
							break;
						}
					}
					return type;
				}
			}
			throw new ConfigurationException(ConfigurationException.AUTO_DETECT_FIELD_TYPE_FAILED,
					getClass().getSimpleName(), javaFieldName);
		}

		private int getNullHandling() {
			if (Objects.nonNull(fieldDefinition)) {
				return fieldDefinition.nullHandling();
			} else if (Objects.nonNull(oneToOne)) {
				return oneToOne.nullHandling();
			} else if (Objects.nonNull(manyToOne)) {
				return manyToOne.nullHandling();
			} else if (Objects.nonNull(notNull)) {
				return Definition.NOT_NULL;
			} else {
				return Column.NULL_HANDLING_DEFAULT;
			}
		}

		private int getDefaultHandling() {
			if (Objects.nonNull(fieldDefinition)) {
				return fieldDefinition.defaultHandling();
			} else if (Objects.nonNull(oneToOne)) {
				return oneToOne.defaultHandling();
			} else if (Objects.nonNull(manyToOne)) {
				return manyToOne.defaultHandling();
			} else {
				return Column.DEFAULT_HANDLING_DEFAULT;
			}
		}

		private Object getDefaultValue() {
			if (Objects.nonNull(fieldDefinition)) {
				return fieldDefinition.defaultValue();
			} else if (Objects.nonNull(oneToOne)) {
				return oneToOne.defaultValue();
			} else if (Objects.nonNull(manyToOne)) {
				return manyToOne.defaultValue();
			} else {
				return Column.DEFAULT_VALUE_DEFAULT;
			}
		}

		private String getCheck() {
			if (Objects.nonNull(fieldDefinition)) {
				return fieldDefinition.check();
			} else if (Objects.nonNull(oneToOne)) {
				return oneToOne.check();
			} else if (Objects.nonNull(manyToOne)) {
				return manyToOne.check();
			} else {
				return Column.CHECK_CONSTRAINT_DEFAULT;
			}
		}

		/**
		 * Checks if this field is a primaryKey field
		 * 
		 * @return true if this field is a primary key field
		 */
		private final boolean isPK() {
			if (Objects.nonNull(fieldDefinition)) {
				switch (fieldDefinition.type()) {
				case Definition.TYPE_INTEGER_PK:
				case Definition.TYPE_INTEGER_PK_AI:
				case Definition.TYPE_REAL_PK:
				case Definition.TYPE_TEXT_PK:
				case Definition.TYPE_TEXT_PK_UUID:
					return true;
				}
			}
			if (Objects.nonNull(primaryKey)) {
				return true;
			}
			return pk;
		}

		/**
		 * initialize the Definition based upon the values of this
		 * FieldDefinitionContainer
		 * 
		 * @return the {@link Definition}
		 * 
		 * @throws ConfigurationException
		 * 
		 * @see {@link SQLiteBuilder#define(String, int, int, int, Object, String)}
		 */
		private Definition toDefinition() throws ConfigurationException {
			return Definition.define(getSQLName(), getType(), getNullHandling(), getDefaultHandling(),
					getDefaultValue(), getCheck());
		}
	} // End of class FieldDefinitionContainer
}
