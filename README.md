# lumicore

![lumicore Logo](https://github.com/epimethix/lumicore/blob/main/core/src/main/resources/lumicore_256.png?raw=true)

## Introduction

Welcome to the lumicore framework.

There are the three main features that the framework supplies. 

### 1) IOC/DI

Inversion of Control / Dependency injection is realized through some annotations and Reflection. 

[Read more...](../../blob/main/ioc/README.md)

### 2) Optional: ORM

Object-Relational-Mapping is realized through some annotations and Reflection. At the moment only SQLite is supported but that may be subject to change.

[Read more...](../../blob/main/orm/README.md)

### 3) Optional: Swing UI

Swing is included in the IOC Concept. Swing `Component`s are initialized on the `EventDispatchThread` and are scanned for `LabelsDisplayer` implementations for dynamic I18N.

[Read more...](../../blob/main/swing/README.md)