# lumicore :: Properties

This project is the home of two classes.

1. PropertiesFile
1. LumicoreProperties

## 1) `PropertiesFile`

This class tries to emulate the way `ResourceBundle`s are obtained through `ResourceBundle.getBundle(bundleName)`

### Usage

```java
	PropertiesFile p = PropertiesFile.getProperties("my-props");
	if (p.containsKey("my-key")) {
		system.err.println(p.getProperty("my-key"));
	}
	p.setProperty("my-key", "new value");
	// no need to call store, setProperty called it already
	// p.store();
```

## 2) `LumicoreProperties`

This class tries to obtain the framework configuration properties from `lumicore.properties` which has to be created in the application project.

```properties
########################
# lumicore.properties #
########################

# As a demonstration all values in this file are the default values.
# [boolean] toggles
# to enable a boolean field use a positive value.
# positive values are 'yes', 'on', 'true', 'open', '1'
# all other values are interpreted as false

#################
#  APPLICATION  #
#################

# Application Time Zone
# [String] zone offset value
# must be a value that is applicable for parsing through ZoneId.of(String)
# default value is ZoneId.systemDefault()
# uncomment next line to specify an application time zone...
# application-time-zone=+06:00

##############
#  DATABASE  #
##############

# Default BigDecimal Scale
# [int] n digits
# The global number of digits after the comma
# to be stored when using BigDecimal.
default-bigdecimal-scale=4

# Connection Policy
# [boolean] toggle
# To never close the database connection automatically
# use the value 'open' or any other positive value, 'close' will 
# automatically close the connection after each database operation.
connection-policy=open

# Default Query Limit
# [long] n
# limits the number of returned records
default-query-limit=100

#####################
#  USER MANAGEMENT  #
#####################

# the number of hashing iterations used by the default
# password hashing algorithm.
hashing-iterations=64000
# The hashing key length.
hashing-key-length=128

#############
#  LOGGING  #
#############

# Logger configuration
logger-configuration=com.github.epimethix.accounting.ExampleLoggerConfiguration
# Console output of auto-wiring process.
# use value 'on' for output and 'off' or any other value for no output.
ioc-verbose=no
# Console output when querying the databases. values 'on' / 'off'
orm-verbose=no
# Swing console output
swing-verbose=no

#############
#   SWING   #
#############

swing.default-margin=5
swing.default-text-margin=3
swing.medium-margin=10
swing.large-margin=15
swing.default-scroll-increment=20
swing.default-dialog-width=25%
swing.default-font-size=12
swing.small-font-size=10
swing.large-font-size=14
```