# lumicore :: IOC Annotations

## Components (apply to class)

### `@Component` & `@Service`

Classes annotated with these annotations are being picked up by the IOC framework to be instantiated and injected with their dependencies. [More about Components](../../../blob/main/ioc/README.md)

### `@SwingComponent`

Swing components are special because they are instantiated and injected on the `EventDispatchThread` and then they are being scanned for `LabelsDisplayer`s. [More about Swing and I18N](../../../blob/main/swing/README.md)

## Inside Components (apply to fields)

### `@Autowired` & `@Qualifier`

Fields of components and of some component type can be annotated with `@Autowired` to signal the IOC framework that these dependencies should be injected after instantiation.

If there is one interface with one implementation class the use of any qualifiers is not necessary. If an interface has multiple implementations, the field name can be used as a qualifier (case insensitively matching the implementation class simple name). If that is not an option then the `@Qualifier` annotation can be used to specify the implementation class.

## Inside Components (apply to initializer method)

### `@PostConstruct`

Initializer methods returning void and without parameters can be annotated with `@PostConstruct`. These methods will then be called after the injection of dependencies is done.

## Scanning (apply to `Application` class)

### `@ComponentScan`

Can be used to specify the packages that should be scanned for components.

### `@JarFileScan`

Can be used to specify additional jars to be scanned.

## Interception (TODO)

### `@Intercept`

### `@InterceptAfterCall`

### `@InterceptBeforeCall`

### `@InterceptAllowCaller`

### `@InterceptRequireRoles`


