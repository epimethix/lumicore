# lumicore :: ioc

## Components

There is the concept of managed "components". 

Components are the classes that get automatically instatiated by the IOC algorithm. Also they are injected with their dependencies.

Components are classes that are annotated with the `@Component` or `@Service` annotations; these annotations are synonyms and can be used interchangeably.

Swing UI Components can be annotated with `@SwingComponent`. Those components will be initialized on the EventDispatchThread.

Also there are implicit components:

- implementation of the `Application` interface.
- implementations of the `Database` interface.
- implementations of the `Repository` interface

```java
@Component
public class ComponentDemo {
	// mark dependencies for injection after instantiation
	@Autowired
	private OtherComponent dependency

	// the @PostConstruct method must return void and be without parameters
	@PostConstruct
	public void init() {
		// initialization tasks after dependency injection
	}
}
```

Bidirectional dependencies are supported but rather discouraged since either one of the `Component`s will not be injected at the time the other `Component`s post construct method may be called.

## Starting the framework / The `Application` interface

The application interface has some variants:

- `Application` for a non-database application
- `DatabaseApplication` for database applications
- `CryptoDatabaseApplication` for database applications with user obtained credentials

The `Application` implementation should reside in the base package of the project. That way the class scanners can include all project files.

Starting the framework is done using the static initialization method:

```java
public static void main(String[] args) {
	try {
		Lumicore.startApplication(MyApplication.class, args);
	} catch(ConfigurationException e) {
		e.printStackTrace();
	}
}
```

## `ConfigurationException`

This exception is thrown only during initialization of the application and is supposed to aid discovering inconsistencies in the application configuration.
