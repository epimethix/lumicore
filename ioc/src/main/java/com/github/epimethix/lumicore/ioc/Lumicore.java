/*
 * Copyright 2022-2023 epimethix@protonmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.epimethix.lumicore.ioc;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;

//import static org.burningwave.core.assembler.StaticComponentContainer.Fields;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.SwingUtilities;

import com.github.epimethix.lumicore.benchmark.Benchmark;
import com.github.epimethix.lumicore.benchmark.Benchmark.Check;
import com.github.epimethix.lumicore.classscan.ClassScanner;
import com.github.epimethix.lumicore.classscan.ClassScannerFactory;
import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.common.ApplicationUtils;
import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.DatabaseApplication;
import com.github.epimethix.lumicore.common.LumicoreBuildConfig;
import com.github.epimethix.lumicore.common.Reflect;
import com.github.epimethix.lumicore.common.UserInterface;
import com.github.epimethix.lumicore.common.ioc.Injector;
import com.github.epimethix.lumicore.common.orm.DatabaseInjector;
import com.github.epimethix.lumicore.common.swing.SwingInjector;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.util.PrintStreamString;
import com.github.epimethix.lumicore.ioc.annotation.Autowired;
import com.github.epimethix.lumicore.ioc.annotation.Component;
import com.github.epimethix.lumicore.ioc.annotation.PostConstruct;
import com.github.epimethix.lumicore.ioc.annotation.Qualifier;
import com.github.epimethix.lumicore.ioc.annotation.Service;
import com.github.epimethix.lumicore.ioc.interception.InterceptionController;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;
import com.github.epimethix.lumicore.logging.LoggerConfiguration;
import com.github.epimethix.lumicore.profile.Profile;
import com.github.epimethix.lumicore.properties.PropertiesFile;
import com.github.epimethix.lumicore.stackutil.AccessCheck;

/**
 * The class {@code Lumicore} is the core of the lumicore-framework, its task
 * primarily is managing IOC when starting the user application.
 * <p>
 * This class is based on the tutorial
 * https://github.com/JJBRT/advanced-java-tutorials/blob/master/dependency_injection
 * 
 * @author JJBRT, epimethix
 *
 */
public class Lumicore implements Injector {

	private static Logger LOGGER = Log.getLogger();
	private static Logger iocLogger = Log.getLogger(Log.CHANNEL_IOC);
//	private static Logger ormLogger = Log.getLogger(Log.CHANNEL_ORM);

	private static Lumicore injector;
	private static DatabaseInjector databaseInjector;

	private static String applicationPackage;

	private static boolean headless;
	
	private final static  String sqlDatabaseInjector = "com.github.epimethix.lumicore.orm.SQLDatabaseInjector";
	private final static String swingInjector = "com.github.epimethix.lumicore.swing.LumicoreSwing";

	/**
	 * 
	 * @param applicationClass
	 * @param args             the arguments from the main method
	 * @throws ConfigurationException
	 */
	public static void startApplication(Class<? extends Application> applicationClass, String[] args)
			throws ConfigurationException {
		startApplication(applicationClass, args, null);
	}

//	public static void startApplication(Class<? extends Application> applicationClass, String[] args,
//			PropertiesFile properties) throws ConfigurationException {
//		startApplication(applicationClass, args, properties.getFile());
//	}

	/**
	 * Starts the framework using the specified {@code Application} class using the
	 * specified {@code Profile}.
	 * <p>
	 * the arguments that are recognized by the framework are:
	 * <p>
	 * <ul>
	 * <li>--headless to suppress the ui from getting started
	 * <li>--labels-controller=fully.qualified.name.to.X to load a specific labels
	 * controller
	 * </ul>
	 * 
	 * @param applicationClass the {@code Application} specification
	 * @param args             the arguments from the main method
	 * @param profileFile      the profile file
	 * @throws ConfigurationException if any problem arises during startup
	 * @see Application
	 * @see DatabaseApplication
	 * @see UserInterface
	 * @see SwingUI
	 * @see Profile
	 */
	public static void startApplication(Class<? extends Application> applicationClass, String[] args, PropertiesFile profileFile)
			throws ConfigurationException {
		try {
			synchronized (Lumicore.class) {
				if (Objects.isNull(injector)) {
					Check ckStartApplication = Benchmark.start(Lumicore.class, "startApplication");
					headless = Arrays.asList(args).contains("--headless");
					if (Objects.nonNull(profileFile)) {
						Check ckLoadProfile = Benchmark.start(Lumicore.class, "loadProfile");
						loadProfile(profileFile);
						ckLoadProfile.stop();
					}
					Check ckInstatiateInjector = Benchmark.start(Lumicore.class, "initialize injector");
//					StackTraceElement ste = ApplicationUtils.getCallerStackTraceElement();
//					logger.info("%s::%s %s", ste.getClassName(), ste.getMethodName(), ste.getClassLoaderName());
					applicationPackage = applicationClass.getPackageName();
					LOGGER.info(C.getLabel(C.STARTUP_MESSAGE, LumicoreBuildConfig.NAME, LumicoreBuildConfig.VERSION,
							Profile.getActiveProfileName()));
					injector = new Lumicore(applicationClass);
					ckInstatiateInjector.stop();
					injector.initializeFramework(applicationClass, args);
					ckStartApplication.stop();
					LOGGER.info("%n%s", PrintStreamString.toString(Benchmark::printBenchmarkResults));
				}
			}
//		} catch (ConfigurationException ce) {
//			throw ce;
		} catch (Exception e) {
			if (e instanceof InvocationTargetException) {
				InvocationTargetException ite = (InvocationTargetException) e;
				Throwable cause = ite.getCause();
				if (cause instanceof ConfigurationException) {
					throw (ConfigurationException) cause;
				}
			}
			LOGGER.error(e);
//			e.printStackTrace();
		}
	}

	/**
	 * For testing only: sets the framework variable to null to enable
	 * {@link #startApplication(Class, String[])} again.
	 * <p>
	 * This method has no effect if called when any other {@link Profile} than
	 * {@link Profile#TESTING} is active
	 */
	public static void killApplication() {
		if (Profile.isProfileActive(Profile.TESTING)) {
			injector = null;
		}
	}

	/**
	 * Gets the implementation of the specified service class.
	 * 
	 * @param <T>
	 * @param cls the implementation class
	 * @return
	 */
	public static <T> T getService(Class<T> cls) {
		try {
			return injector.getBeanInstance(cls);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static final <T> T initComponent(Class<T> cls) {
		T component = getService(cls);
		injector.autowire(cls, component);
		return component;
	}

//	private final Map<Class<?>, Class<?>> diMap;
//
//	private final Map<Class<?>, Map<String, Class<?>>> interfaceMap;
//
//	private final Map<Class<?>, Object> applicationScope;
//	
	private final ComponentContainer componentContainer;

	private final Map<Class<?>, Boolean> autowiredFlags;

	private final Map<Object, Boolean> instanceAutowiredFlags;

	private final ClassScanner scanner;

	private Application application;

	private Lumicore(Class<?> applicationClass) throws IOException {
//		diMap = new HashMap<>();
//		interfaceMap = new HashMap<>();
//		applicationScope = new HashMap<>();
		componentContainer = new ComponentContainer();
		autowiredFlags = new HashMap<>();
		instanceAutowiredFlags = new HashMap<>();
//		String scannerName;
		scanner = ClassScannerFactory.createClassScanner(applicationClass);
		
//		logger.info("initialized %s", scannerName);
	}

	private void initializeFramework(Class<?> applicationClass, String[] args) throws ConfigurationException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		/*
		 * Only the startApplication method may call initializeFramework
		 */
		AccessCheck.allowCaller(false, Lumicore.class.getName() + "::startApplication");
		/*
		 * Step 1: Configure Logging
		 */
		if (!Log.wasConfigured()) {
			Check ckConfigureLogging = Benchmark.start(Lumicore.class, "configure-logging");
			Collection<Class<?>> loggerConfigurations = scanner.searchClassesAssignableFrom(LoggerConfiguration.class);
			if (loggerConfigurations.size() > 0) {
				if (loggerConfigurations.size() > 1) {
					throw new ConfigurationException(
							ConfigurationException.THERE_SHOULD_BE_ZERO_OR_ONE_LOGGER_CONFIGURATIONS,
							ApplicationUtils.classNamesToString(loggerConfigurations));
				} else {
					/*
					 * Checked: loggerConfigurations are assignable to LoggerConfiguration
					 */
					@SuppressWarnings("unchecked")
					Class<? extends LoggerConfiguration> loggerConfiguration = (Class<? extends LoggerConfiguration>) loggerConfigurations
							.iterator().next();
					Constructor<?> emptyConstructor = Reflect.getEmptyConstructor(loggerConfiguration);
					if (Objects.isNull(emptyConstructor)) {
						throw new ConfigurationException(
								ConfigurationException.LOGGER_CONFIGURATION_NEEDS_EMPTY_CONSTRUCTOR,
								loggerConfiguration.getSimpleName());
					}
					LoggerConfiguration lc = (LoggerConfiguration) emptyConstructor.newInstance();
					Log.configure(lc);
				}
			}
			ckConfigureLogging.stop();
		}
		/*
		 * Step 2: Configure Interception
		 */
		Collection<?> interceptionControllers = scanner.searchClassesAssignableFrom(InterceptionController.class);
		if (interceptionControllers.size() > 0) {
//			InterceptionController ic;
//			TODO Initialize Interception Controllers
		}
		/*
		 * Search for component classes
		 */
		Collection<Class<?>> components = scanner.searchClassesByAnnotation(Component.class);
		addAllIfNotContained(components, scanner.searchClassesByAnnotation(Service.class));
		/*
		 * initialize (Database)Application instance
		 */
		Check ckInitializeApplication = Benchmark.start(Lumicore.class, "initialize-application-instance");
		Constructor<?> appConstructor = Reflect.getEmptyConstructor(applicationClass);
		boolean injectArgsInAppConstructor = false;
		if (Objects.isNull(appConstructor)) {
			try {
				appConstructor = applicationClass.getConstructor(String[].class);
				injectArgsInAppConstructor = true;
			} catch (Exception e) {
				throw new ConfigurationException(ConfigurationException.INITIALIZE_DB_APPLICATION_FAILED,
						applicationClass.getSimpleName());
			}

		}
		Application appInstance;
		if (DatabaseApplication.class.isAssignableFrom(applicationClass)) {
			/*
			 * Checked: applicationClass is assignable to DatabaseApplication
			 */
			@SuppressWarnings("unchecked")
			Class<? extends DatabaseApplication> dbApplicationClass = (Class<? extends DatabaseApplication>) applicationClass;
			if (injectArgsInAppConstructor) {
				appInstance = (DatabaseApplication) appConstructor.newInstance((Object) args);
			} else {
				appInstance = (DatabaseApplication) appConstructor.newInstance();
			}
			ckInitializeApplication.stop();
			registerComponent(applicationClass, appInstance, components);
			/*
			 * in case the application class implements DatabaseApplication databases are
			 * initialized
			 */
			Check ckInitializeDatabases = Benchmark.start(Lumicore.class, "initializeDatabases");
			Collection<Class<?>> dbcs = scanner.searchClassesAssignableFrom(DatabaseInjector.class);
			try {

				if (dbcs.size() == 0) {
					dbcs.add(Class.forName(sqlDatabaseInjector ));
				}

				if (dbcs.size() == 1) {
					Constructor<?>[] cs = dbcs.stream().findFirst().get().getConstructors();
//				DatabaseController databaseController;
					for (Constructor<?> constructor : cs) {
						if (constructor.getParameterCount() == 1
								&& Injector.class == (constructor.getParameterTypes()[0])) {
							databaseInjector = (DatabaseInjector) constructor.newInstance(this);
							databaseInjector.initializeDatabases(dbApplicationClass, (DatabaseApplication) appInstance,
									components);
						}
					}
				}
			} catch (ClassNotFoundException e) {
				LOGGER.error(e, "Could not initialize databases! is lumicore:orm present?");
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			ckInitializeDatabases.stop();
		} else {
			if (injectArgsInAppConstructor) {
				appInstance = (Application) appConstructor.newInstance((Object) args);
			} else {
				appInstance = (Application) appConstructor.newInstance();
			}
			ckInitializeApplication.stop();
			registerComponent(applicationClass, appInstance, components);
		}
		this.application = appInstance;
		autowire(components);
		/*
		 * Start UI
		 */
		if (!headless) {
			Collection<Class<?>> uiClasses = injector.searchClassesAssignableFrom(UserInterface.class);
			if (uiClasses.size() > 1) {
				String uiClassNames = ApplicationUtils.classNamesToString(uiClasses);
				throw new ConfigurationException(
						ConfigurationException.THERE_SHOULD_BE_ZERO_OR_ONE_SWING_UI_IMPLEMENTATIONS, uiClasses.size(),
						uiClassNames);
			} else if (uiClasses.size() == 1) {
				/*
				 * Checked: uiClasses only contains classes that are assignable to SwingUI
				 */
				@SuppressWarnings("unchecked")
				final Class<? extends UserInterface> uiControllerClass = (Class<? extends UserInterface>) uiClasses
						.iterator().next();

				if (SwingUI.class.isAssignableFrom(uiControllerClass)) {
					try {
						Class<?> swingStarter = Class.forName(swingInjector );
						SwingInjector swingStarterInstance = (SwingInjector) swingStarter
								.getConstructor(Injector.class, Class.class, String[].class)
								.newInstance(this, uiControllerClass, args);
						swingStarterInstance.initializeSwingUI(applicationClass, appInstance);
					} catch (ClassNotFoundException e) {
						throw new ConfigurationException(ConfigurationException.SWING_LIB_MISSING,
								uiControllerClass.getSimpleName(), LumicoreBuildConfig.VERSION);
//					} catch (NoSuchMethodException e) {
//						LOGGER.error(e);
//					} catch (SecurityException e) {
					} catch (Exception e) {
						LOGGER.error(e);
					}
				} else {
					UserInterface ui;
					try {
						ui = uiControllerClass.getConstructor().newInstance();
						ui.showUI();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						LOGGER.error(e);
					}
				}
			}
		}
		componentContainer.logInterfaceMap();
	}

	@Override
	public Application getApplication() {
		return application;
	}

	@Override
	public void registerComponent(Class<?> cls, Object clsInstance, Collection<Class<?>> components) {
		InstanceCounter.track(clsInstance);
		componentContainer.registerComponent(clsInstance);
		addIfNotContained(components, cls);
	}

	private void addAllIfNotContained(Collection<Class<?>> components, Collection<Class<?>> classes) {
		for (Class<?> cls : classes) {
			addIfNotContained(components, cls);
		}
	}

	private void addIfNotContained(Collection<Class<?>> components, Class<?> cls) {
		if (!components.contains(cls)) {
			components.add(cls);
		}
	}

//	private void loadLabels(Object editor, String packageName) {
//		Reflect.loadLabels(editor, packageName);
//	}
	@Override
	public void autowire(Collection<Class<?>> components) throws ConfigurationException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		componentContainer.buildDiMap(components);
		Check ckAutowire = Benchmark.start(Lumicore.class, "autowire");
		StringBuilder autoWireDiagnostics = new StringBuilder();
		for (Class<?> cls : components) {
			Object classInstance = getComponent(cls);
			if (Objects.isNull(classInstance)) {
				throw new ConfigurationException(ConfigurationException.INITIALIZE_COMPONENT_FAILED,
						cls.getSimpleName());
			}
			autoWireDiagnostics.append(autowire(cls, classInstance));
		}
		iocLogger.info("%n%s%n%s", ApplicationUtils.createBanner("autowire"), autoWireDiagnostics.toString());
		ckAutowire.stop();
	}

	@Override
	public Object getComponent(Class<?> cls)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object classInstance;
		if (IOC.isSingleton(cls) && componentContainer.applicationScopeContainsKey(cls)) {
			classInstance = componentContainer.get(cls);
		} else {
			classInstance = initializeComponent(cls);
		}
		return classInstance;
	}

	private Object initializeComponent(Class<?> cls)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//		ObjectA proxyObjectA = (ObjectA) Proxy.newProxyInstance(ObjectA.class.getClassLoader(),
//                new Class[]{ObjectA.class},
//                new MyInvocationHandler(objectA));

		Object classInstance = autoInstance(cls);
//		if (Reflect.shouldProxyForInterception(cls)) {
//			classInstance = cls.cast(Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[] { cls },
//					new InterceptionProxy(classInstance, null)));
//		}
		if (Objects.nonNull(classInstance)) {
			InstanceCounter.track(classInstance);
			if (IOC.isSingleton(cls)) {
//				applicationScope.put(cls, classInstance);
				componentContainer.registerComponent(classInstance);
			}
		}
		return classInstance;
	}

	@Override
	public <T> T autoInstance(Class<T> cls)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		{
			Constructor<?> constructor = Reflect.getEmptyConstructor(cls);
			if (Objects.nonNull(constructor)) {
				Object classInstance = constructor.newInstance();
				return (T) classInstance;
			}
		}
		Constructor<?>[] constructors = cls.getConstructors();
		Constructor<?> constructor = null;

		if (constructors.length == 1) {
			constructor = constructors[0];
		} else {
			for (Constructor<?> c : constructors) {
				if (c.isAnnotationPresent(Autowired.class)) {
					if (Objects.isNull(constructor)) {
						constructor = c;
					} else {
						throw new RuntimeException(
								String.format("%s: only zero or one constructors can be annotated with @Autowired",
										cls.getSimpleName()));
					}
				}
			}
		}
		if (Objects.isNull(constructor)) {
			throw new NullPointerException(cls.getSimpleName() + ": could not obtain constructor");
		}
		return (T) autoInstance(constructor);
	}

	private Object autoInstance(Constructor<?> constructor)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Parameter[] parameters = constructor.getParameters();
		Object[] arguments = new Object[parameters.length];
		boolean includeSwing = SwingUtilities.isEventDispatchThread();
		for (int i = 0; i < parameters.length; i++) {
			if (Reflect.isComponent(parameters[i].getType())
					|| (includeSwing && Reflect.isSwingComponent(parameters[i].getType()))) {
				String name = parameters[i].getName();
				Qualifier q = parameters[i].getAnnotation(Qualifier.class);
				String qualifier = Objects.isNull(q) ? null : q.value();
				Object beanInstance = getBeanInstance(parameters[i].getType(), name, qualifier);
				// TODO Pre-Wire?!/Wait? (WTF)
//				autowire(parameters[i].getType(), beanInstance, "p".concat("\t"));
				arguments[i] = beanInstance;
			} else {
				return null;
			}
		}
		return constructor.newInstance(arguments);
	}

	private int autoWireCounter = 1;
	private int autoSetCounter = 1;

	@Override
	public String autowire(Class<?> cls, Object classInstance) {
		StringBuilder diagnostics = new StringBuilder();
		autowire(cls, classInstance, "", diagnostics);
		return diagnostics.toString();
	}

	private void autowire(Class<?> cls, Object classInstance, String prefix, StringBuilder diagnostics) {
		boolean isSingleton = IOC.isSingleton(cls);
		if (isSingleton) {
			if (autowiredFlags.containsKey(cls)) {
//			System.out.printf("%s%s was already auto wired%n", prefix, cls.getSimpleName());
				return;
			} else {
				autowiredFlags.put(cls, false);
			}
		} else {
			if (instanceAutowiredFlags.containsKey(classInstance)) {
				return;
			} else {
				instanceAutowiredFlags.put(classInstance, false);
			}
		}
//		iocLogger.info(String.format("%s@ [%s]: autowire #%03d%n", prefix, cls.getSimpleName(), autoWireCounter++));
		diagnostics.append(String.format("%s@ [%s]: autowire #%03d%n", prefix, cls.getSimpleName(), autoWireCounter++));
//		Collection<Field> fields = Fields.findAllAndMakeThemAccessible(FieldCriteria.forEntireClassHierarchy()
//				.allThoseThatMatch(field -> field.isAnnotationPresent(Autowired.class)), cls);
		Collection<Field> fields = Reflect.getFieldsAnnotatedWith(cls, Autowired.class);
		for (Field field : fields) {
			String qualifier = field.isAnnotationPresent(Qualifier.class) ? field.getAnnotation(Qualifier.class).value()
					: null;
			try {
				Object fieldInstance = getBeanInstance(field.getType(), field.getName(), qualifier);
				try {
					field.setAccessible(true);
					field.set(classInstance, fieldInstance);
					diagnostics.append(String.format("%s= [%s].[%s] was set: assignment #%03d%n", prefix,
							cls.getSimpleName(), field.getName(), autoSetCounter++));
					autowire(fieldInstance.getClass(), fieldInstance, prefix.concat("  "), diagnostics);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				System.err.println("autowire(" + cls.getSimpleName() + "." + field.getName() + "): "
						+ e.getClass().getSimpleName() + " - " + e.getMessage());
				throw e;
			}
//			Fields.setDirect(classInstance, field, fieldInstance);
		}
		try {
			Method[] methods = cls.getDeclaredMethods();
			for (Method m : methods) {
				if (m.isAnnotationPresent(PostConstruct.class) && m.getParameterCount() == 0
						&& !Modifier.isStatic(m.getModifiers())) {
					m.setAccessible(true);
					m.invoke(classInstance);
					diagnostics.append(String.format("%s$ [%s].[%s()]: Postconstruct method was called%n", prefix,
							cls.getSimpleName(), m.getName()));
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		if (isSingleton) {
			autowiredFlags.put(cls, true);
		} else {
			instanceAutowiredFlags.put(classInstance, true);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getBeanInstance(Class<T> interfaceClass) throws InstantiationException, IllegalAccessException {
		return (T) getBeanInstance(interfaceClass, interfaceClass.getSimpleName(), null);
	}

	private Object getBeanInstance(Class<?> type, String name, String qualifier) {
		Class<?> implementationClass = componentContainer.getImplementationClass(type, name, qualifier);
		{
			boolean isSwingComponent = Reflect.isSwingComponent(implementationClass);
			boolean isComponent = Reflect.isComponent(implementationClass);
			if (isSwingComponent) {
				if (!SwingUtilities.isEventDispatchThread()) {
					throw new IllegalArgumentException(String.format(
							"Cannot access or instantiate @SwingComponent[%s] on a non event dispatch thread!",
							implementationClass.getSimpleName()));
				}
			} else if (!isComponent) {
				throw new RuntimeException(
						String.format("%s is not a managed component class!", implementationClass.getSimpleName()));
			}
		}
		synchronized (componentContainer) {
			try {
				return getComponent(implementationClass);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			throw new RuntimeException(String.format("Could not load bean [%s]", type.getSimpleName()));
		}
	}

	@Override
	public void putImplementation(Class<?> implementationClass) {
//		diMap.put(implementationClass, implementationClass);
		componentContainer.buildDiMap(Arrays.asList(implementationClass));
	}

	@Override
	public final void putInterfaceImplementation(Class<?> interface0, Class<?> implementationClass) {
//		String qualifier;
//		if (implementationClass.isAnnotationPresent(Qualifier.class)) {
//			qualifier = implementationClass.getAnnotation(Qualifier.class).value().toLowerCase();
//		} else {
//			qualifier = implementationClass.getSimpleName().toLowerCase();
//		}
//		Map<String, Class<?>> ifMap = interfaceMap.get(interface0);
//		if (Objects.isNull(ifMap)) {
//			ifMap = new HashMap<>();
//			ifMap.put(qualifier, implementationClass);
//			interfaceMap.put(interface0, ifMap);
//		} else {
//			ifMap.put(qualifier, implementationClass);
//		}
		componentContainer.putInterfaceImplementation(interface0, implementationClass);
	}
//
//	private Class<?> getImplementationClass(Class<?> type, String name, String qualifier) {
//		String selector = (Objects.isNull(qualifier) || qualifier.trim().isEmpty() ? name : qualifier).toLowerCase();
//
//		Class<?> implementationClass = null;
//		if (type.isInterface()) {
//			Map<String, Class<?>> ifMap = interfaceMap.get(type);
//			if (Objects.nonNull(ifMap)) {
//				implementationClass = ifMap.get(selector);
//				if (Objects.isNull(implementationClass)) {
//					Set<?> keySet = ifMap.keySet();
//					if (keySet.size() == 1) {
//						implementationClass = ifMap.get(keySet.iterator().next());
//					}
//				}
//			}
//		} else {
//			implementationClass = diMap.get(type);
//		}
//		if (Objects.nonNull(implementationClass)) {
//			return implementationClass;
//		}

//		Set<Entry<Class<?>, Class<?>>> implementationClasses = diMap.entrySet().stream()
//				.filter(entry -> entry.getValue() == type).collect(Collectors.toSet());
//		String errorMessage = "";
//		if (implementationClasses == null || implementationClasses.size() == 0) {
//			errorMessage = "no implementation found for interface " + type.getName();
//		} else if (implementationClasses.size() == 1) {
//			Optional<Entry<Class<?>, Class<?>>> optional = implementationClasses.stream().findFirst();
//			if (optional.isPresent()) {
//				return optional.get().getKey();
//			}
//		} else if (implementationClasses.size() > 1) {
//			final String findBy = (qualifier == null || qualifier.trim().length() == 0) ? name : qualifier;
//			Optional<Entry<Class<?>, Class<?>>> optional = implementationClasses.stream()
//					.filter(entry -> entry.getKey().getSimpleName().equalsIgnoreCase(findBy)).findAny();
//			if (optional.isPresent()) {
//				return optional.get().getKey();
//			} else {
//				errorMessage = "There are " + implementationClasses.size() + " of interface " + type.getName()
//						+ " Expected single implementation or make use of @Qualifier to resolve conflict";
//			}
//		}
//		String errorMessage = "Implementation class not found: " + type.getSimpleName();
////		System.err.println(errorMessage);
//		iocLogger.error(errorMessage);
//		throw new RuntimeErrorException(new Error(errorMessage));
//	}

	/**
	 * Initializes a new instance of the specified component class.
	 * <p>
	 * The new instance is autowired.
	 * 
	 * @param componentClass the component to instantiate
	 * @return the component instance or null if any exception was thrown when
	 *         creating the instance
	 */
	public static Object initialize(Class<?> componentClass) {
		Object editor = null;
		try {
			editor = injector.autoInstance(componentClass);
			injector.autowire(componentClass, editor);
//			injector.loadLabels(editor, applicationPackage);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return editor;
	}

	/**
	 * Loads the {@code Profile} from the specified {@code File}.
	 * <p>
	 * Not calling this method means defaulting to {@link Profile#PRODUCTION}.
	 * 
	 * @param profileFile the properties file containing the profile configuration
	 * @see Profile
	 */
	public static void loadProfile(PropertiesFile profileFile) {
		Profile.loadProfile(profileFile);
		try {
			Log.reconfigure();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Collection<Class<?>> searchClassesAssignableFrom(String packageName, Class<?> cls) {
		return scanner.searchClassesAssignableFrom(packageName, cls);
	}

	@Override
	public Collection<Class<?>> searchClassesAssignableFrom(Class<?> cls) {
		return scanner.searchClassesAssignableFrom(cls);
	}

	@Override
	public Collection<Class<?>> searchClassesByAnnotation(Class<? extends Annotation> cls) {
		return scanner.searchClassesByAnnotation(cls);
	}

	@Override
	public String getApplicationPackage() {
		return applicationPackage;
	}
}
