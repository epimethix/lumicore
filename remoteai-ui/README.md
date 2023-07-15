# lumicore :: RemoteAI UI

Example Application for user authenticated crypto db. also minimal db and AI example.

## Dependencies

```groovy
dependencies {
    implementation project(':core')
    implementation project(':benchmark')
    implementation project(':logging')
    implementation project(':orm-sqlite')
    implementation project(':properties')
    implementation project(':remoteai')
    implementation project(':stack-util')
    implementation project(':swing')

    // Apache 2.0
	// https://mvnrepository.com/artifact/io.github.willena/sqlite-jdbc
	implementation group: 'io.github.willena', name: 'sqlite-jdbc', version: '3.38.1'
}
```