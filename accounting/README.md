# lumicore :: Accounting

Example Application for minimal design requirements with database and gui editors.

## Dependencies

```groovy
dependencies {
    implementation project(':core')
    implementation project(':benchmark')
    implementation project(':ipc')
    implementation project(':logging')
    implementation project(':orm-sqlite')
    implementation project(':properties')
    implementation project(':swing')
    // Apache 2.0
	// https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
	implementation group: 'com.fasterxml.jackson.core', name: 'jackson-databind', version: '2.13.3'
}
```

