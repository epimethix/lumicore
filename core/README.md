# lumicore :: Core

This is a pure meta project. it can be used as a dependency to enable the lumicore core features:

## Exported Dependencies

```groovy
dependencies {
	api project(':common')
	api project(':ioc')
	api project(':ioc-annotations')
	api project(':profile')
}
```

## Additional Dependencies

To enable ORM also the specific database implementation must be linked.

```groovy
    implementation project(':orm-sqlite')
```

For `Properties` management [see](../../../blob/main/properties/README.md):

```groovy
    implementation project(':properties')
```

For creating measurements during startup [see](../../../blob/main/benchmark/README.md):

```groovy
    implementation project(':benchmark')
```

For Inter Process Communication [see](../../../blob/main/ipc/README.md):

```groovy
    implementation project(':ipc')
```

For logging [see](../../../blob/main/logging/README.md):

```groovy
    implementation project(':logging')
```

For Swing UI [see](../../../blob/main/swing/README.md):

```groovy
    implementation project(':swing')
```