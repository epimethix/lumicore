# lumicore :: Class Scan

this project is used by the IOC `Injector` implementation to search for configurations and components.

There are two different implementations of the `ClassScanner` interface: `ClassPathScanner` and `JarFileScanner`. Depending on whether the application is being executed from a jar file or not the appropriate Scanner is being used.

## Usage

```java
	ClassScanner scanner = ClassScannerFactory.createClassScanner(MyApplication.class);
	
	/*
	 * Search classes that meet the criterion specifiedClass.isAssignableFrom(classToCheck)
	 */
	Collection<Class<?>> result = scanner.searchClassesAssignableFrom(SpecifiedClassOrInterface.class);
	
	/*
	 * narrow or broaden the scope to scan using the specific package declaration.
	 */
	result = scanner.searchClassesAssignableFrom("some.package.name", SpecifiedClassOrInterface.class);
	
	/*
	 * Search classes that are annotated with a certain annotation
	 */
	result = scanner.searchClassesByAnnotation(SomeAnnotation.class);

	/*
	 * narrow or broaden the scope to scan using the specific package declaration.
	 */
	result = scanner.searchClassesByAnnotation("some.package.name", SomeAnnotation.class);
	
	/*
	 * Create a ClassScanner that only finds interfaces
	 */
	ClassScanner interfaceScanner = ClassScannerFactory.createClassScanner(MyApplication.class, cls->cls.isInterface());
	
	/*
	 * Search for lazy entity interfaces
	 */
	Collection<Class<?>> lazyEntityInterfaces = interfaceScanner.searchClassesByCriteria(cls->LazyEntity.class.isAssignableFrom(cls));
```