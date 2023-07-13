# lumicore :: ORM

Object-Relational-Mapping is a core feature of lumicore.

A minimal database application must at least implement the `Database` interface. this automatically creates a database that contains a metadata table in form of a key/value map.

If there are more than one implementations of `Database` than one then the implementation classes need to be annotated with the `@Repositories` annotation.

Further entities can be defined by implementing the `Entity<ID>` interface. This defines an immutable entity which must define its `EntityBuilder<ID>` as inner class. To create mutable entities the interface `MutableEntity<ID>` can be implemented.

To enable lazy loading of related entities that are mapped by `@ManyToOne` or `@OneToOne` the entity must also be defined by an interface that can be proxied. the proxy interface must be annotated with the `@ImplementationClass(EntityImpl.class)` annotation. By default these mappings are loaded eagerly.

Collections mapped by `@OneToMany` or `@ManyToMany` can be lazy loaded without any other measures and are lazy loaded by default.

Each `Entity<ID>` needs a `Repository<E extends Entity<ID>, ID>`.

## 1) `Database`
```java
package com.example.app.db.main;

@Repositories("com.example.db.main.repository")
public class MyDB extends SQLDatabase {
	public MyDB(DatabaseApplication databaseApplication) throws ConfigurationException {
		super(databaseApplication);
	}
}

```

## 2) `Entity<ID>`

```java
package com.example.app.db.main.model;

public class MyEntity implements Entity<Long> {
	@PrimaryKey
	private final Long id;
	private final String name;
	private final String email;
	
	// needs all args constructor
	public MyEntity(Long id, String name, String email) {
		this.id = id;
		this.name = name;
		this.email = email;
	}
	
	// needs copy constructor
	public MyEntity(MyEntity e) {
		this.id = e.id;
		this.name = e.name;
		this.email = e.email;
	}
	
	@Override
	public Long getId() {
		return id;
	}
	
	public Long getName() {
		return name;
	}
	
	public Long getEmail() {
		return email;
	}
	
	public static final class Builder implements Entity.EntityBuilder<Long> {
	
		private Long id;
		private String name;
		private String email;
		
		// needs no-args-constructor
		public Builder() {}
		
		// needs copy constructor
		public Builder(MyEntity e) {
			this.id = e.id;
			this.name = e.name;
			this.email = e.email;
		}
		
		@Override
		public Builder<ID> setId(Long id) {
			this.id = id;
			return this;
		}
		
		public Builder<ID> setName(String name) {
			this.name = name;
			return this;
		}
		
		public Builder<ID> setEmail(String email) {
			this.email = email;
			return this;
		}

		@Override
		public MyEntity build() {
			return new MyEntity(id, name, email);
		}
	}
}

```

## 3) `Repository<E extends Entity<ID>, ID>`

```java
package com.example.app.db.main.repository;

public class MyEntityRepository extends SQLRepository<MyEntity, Long> {
	public MyEntityRepository(Database db) throws ConfigurationException {
		super(db, MyEntity.class, Long.class);
	}
	
	// custom queries go here
}
```

