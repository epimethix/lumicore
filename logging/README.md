# lumicore :: Logging

logging is just for fun yet another implementation of a logging system.

obtaining a logger instance is simple:

```java
Logger LOGGER = Log.getLogger();
```

Loggers be Channeled:

```java
Logger LOGGER = Log.getLogger("my-channel-name");
```

to configure the logging system the abstract class `LoggerConfiguration` can be extended.

Also the logger configuration must be declared in `lumicore.properties`. [see lumicore/properties for more on framework configuration](../../../blob/main/properties/README.md)

```properties
#############
#  LOGGING  #
#############
# Logger configuration
logger-configuration=com.example.app.ExampleLoggerConfiguration
# Console output of auto-wiring process.
# use value 'on' for output and 'off' or any other value for no output.
ioc-verbose=no
# Console output when querying the databases. values 'on' / 'off'
orm-verbose=no
# Swing console output
swing-verbose=no
```

Java Configuration:

```java
public class ExampleLoggerConfiguration extends LoggerConfiguration {
	private final static String LUMICORE_FRAMEWORK_PACKAGE_NAME = "com.github.epimethix.lumicore";
	private final static String APPLICATION_PACKAGE_NAME = Accounting.class.getPackageName();
	private List<AbstractLogTarget> logTargets;

	public ExampleLoggerConfiguration() {}

	@Override
	public List<AbstractLogTarget> createLogTargets() {
		if (Objects.isNull(logTargets)) {
			/*
			 * error.log: capture ALL messages from ALL channels of and above threshold
			 * CRITICAL (+ ERROR).
			 */
			FileLogTarget fltError = new FileLogTarget(AppFiles.ERROR_LOG_FILE, true);
			fltError.captureResponsibility("*?*");
			fltError.setThreshold(Log.CRITICAL);
			/*
			 * diagnostics.log: capture messages from within the app starting from TRACE
			 * including WARN at most. (excluding CRITICAL and ERROR)
			 */
			FileLogTarget fltAppDiagnostics = new FileLogTarget(AppFiles.DIAGNOSTICS_LOG_FILE, true);
			fltAppDiagnostics.captureResponsibility("com.github.epimethix.lumicoreexample");
			fltAppDiagnostics.setThreshold(Log.TRACE);
			fltAppDiagnostics.setCeiling(Log.WARN);
			/*
			 * framework_diagnostics.log
			 */
			FileLogTarget fltFrameworkDiagnostics = new FileLogTarget(AppFiles.FRAMEWORK_DIAGNOSTICS_LOG_FILE, true);
			// base package of all framework classes
			fltFrameworkDiagnostics.captureResponsibility("com.github.epimethix.lumicore.");
			// all channels
			fltFrameworkDiagnostics.captureResponsibility("?*");
			fltFrameworkDiagnostics.setThreshold(Log.TRACE);
			fltFrameworkDiagnostics.setCeiling(Log.WARN);
			/*
			 * channel_orm.log: capture ALL messages from the channel "lumicore-orm"
			 */
			FileLogTarget fltChannelORM = new FileLogTarget(AppFiles.CHANNEL_ORM_LOG_FILE, true);
			fltChannelORM.captureResponsibility("*?" + Log.CHANNEL_ORM);
			/*
			 * return log targets
			 */
			logTargets = Collections.unmodifiableList(
					Arrays.asList(fltError, fltAppDiagnostics, fltFrameworkDiagnostics, fltChannelORM));
		}
		return logTargets;
	}

	@Override
	public void configureDefaultConsoleLogTarget(AbstractLogTarget defaultConsoleLogTarget) {
		defaultConsoleLogTarget.setThreshold(Log.TRACE);
		defaultConsoleLogTarget.captureResponsibility("?"+Log.CHANNEL_ORM);
		if (System.currentTimeMillis() > 0) {
			return;
		}
		/*
		 * by default defaultConsoleLogTarget has the responsibility "*". this code
		 * reduces the responsibilities of the default console log target to the user
		 * project.
		 */
		defaultConsoleLogTarget.removeResponsibility("*");
		defaultConsoleLogTarget.captureResponsibility("com.github.epimethix.lumicoreexample");
	}

	@Override
	public void configureLogger(Logger logger, Class<?> user, String channel) {
		/*
		 * This method is only dead code for example purposes.
		 */
		if (System.currentTimeMillis() > 0) {
			return;
		}
		/*
		 * Set specific to current profile
		 */
		if (Profile.isProfileActive(Profile.DEBUGGING)) {
			logger.setThreshold(Log.TRACE);
		} else if (Profile.getActiveProfile() < Profile.PRODUCTION) {
			logger.setThreshold(Log.WARN);
		} else {
			logger.setThreshold(Log.CRITICAL);
		}
		/*
		 * select lumicore framework loggers
		 */
		if (user.getName().startsWith(LUMICORE_FRAMEWORK_PACKAGE_NAME)) {
			logger.setThreshold(Log.WARN);
		}
		/*
		 * select application loggers
		 */
		if (user.getName().startsWith(APPLICATION_PACKAGE_NAME)) {
			logger.setThreshold(Log.INFO);
		}
		/*
		 * globally set a threshold:
		 */
		logger.setThreshold(Log.WARN);
		/*
		 * set a threshold for a package
		 */
		if (user.getName().startsWith("package.name")) {
			logger.setThreshold(Log.INFO);
		}
		/*
		 * set a threshold for a channel
		 */
		if (Objects.nonNull(channel) && channel.equals("my-channel")) {
			logger.setThreshold(Log.TRACE);
		}
		/*
		 * set a channel name based on the type of user class
		 */
		if (Reflect.isComponent(user)) {
			logger.setChannelName("channel-components");
		} else if (Reflect.isSwingComponent(user)) {
			logger.setChannelName("channel-swing-components");
		}
		/*
		 * set a channel name for a package
		 */
		if (user.getPackageName().startsWith("some.package.name")) {
			logger.setChannelName("some-channel-name");
		} else if (user.getPackageName().startsWith("some.other.package.name")) {
			logger.setChannelName("some-other-channel-name");
		}
		/*
		 * change framework settings
		 */
		/*
		 * silence specific user
		 */
		if (SQLRepository.class.getName().equals(user.getName())) {
			logger.setThreshold(Log.SILENT);
		}
		/*
		 * set specific threshold to framework channel
		 */
		String ormChannelName = Log.CHANNEL_ORM;
		if (Objects.nonNull(channel) && ormChannelName.equals(channel)) {
			logger.setThreshold(Log.WARN);
		}
	}
}
```