/*
 * Copyright 2023 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.swing;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import javax.swing.SwingUtilities;

import com.github.epimethix.lumicore.benchmark.Benchmark;
import com.github.epimethix.lumicore.benchmark.Benchmark.Check;
import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.common.ApplicationUtils;
import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.ioc.Injector;
import com.github.epimethix.lumicore.common.swing.SwingInjector;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.common.ui.C;
import com.github.epimethix.lumicore.common.ui.SplashScreenController;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayerPool;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsScan;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsController;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;
import com.github.epimethix.lumicore.swing.editor.EntityEditorPanel;
import com.github.epimethix.lumicore.swing.entityaccess.EntityAccessControllerFactory;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

public class LumicoreSwing implements SwingInjector {

	private static final Logger LOGGER_IOC = Log.getLogger(Log.CHANNEL_IOC);
	private static final Logger LOGGER_SWING = Log.getLogger(Log.CHANNEL_SWING);
	private static final Logger LOGGER = Log.getLogger();

	private static Injector injector;
//	private static DefaultLabelsManager labelsManager;

//	public static DefaultLabelsManager getLabelsManager() {
//		return labelsManager;
//	}

	private final Class<? extends SwingUI> swingUIClass;

	private final String[] args;

	public LumicoreSwing(Injector injector, Class<? extends SwingUI> swingUIClass, String[] args) {
		LumicoreSwing.injector = injector;
		this.swingUIClass = swingUIClass;
		this.args = args;
	}

	private Exception exitSwingInitializerException;

	@Override
	public void initializeSwingUI(Class<?> applicationClass, Application application) throws Exception {

		try {
			exitSwingInitializerException = null;
			SwingUtilities.invokeAndWait(() -> {
				/*
				 * LayoutUtils must be loaded before creating the Swing UI Components because it
				 * may modify swing layout defaults (font sizes)
				 */
				Check ckLayoutUtils = Benchmark.start(LumicoreSwing.class, "Initialize LayoutUtils");
				LayoutUtils.touch();
				ckLayoutUtils.stop();
				/*
				 * Load LabelsControllers
				 */
//				Check ckLabelsControllers = Benchmark.start(LumicoreSwingImpl.class, "Initialize LabelsControllers");
				Check ckLabels = Benchmark.start(LumicoreSwing.class, "configure Labels");
				Collection<Class<?>> labelsControllers = injector.searchClassesByAnnotation(LabelsController.class);
				labelsControllers.add(C.class);
				for (String arg : args) {
					if (arg.startsWith("--labels-controller=")) {
						try {
							String className = arg.substring(arg.indexOf("=") + 1);
							System.err.println("Adding labels controller " + className);
							labelsControllers.add(Class.forName(className));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
//				ckLabelsControllers.stop();
//				System.err.println(ApplicationUtils.classNamesToString(labelsControllers));

				/*
				 * Registering UI Packages to be included in LabelsDisplayer scan
				 */
//				Check ckRegisterUIPackages = Benchmark.start(LumicoreSwingImpl.class, "Register UI Packages");
				LabelsDisplayerPool.registerUiPackage(LumicoreSwing.class.getPackageName());
				LabelsDisplayerPool.registerUiPackage(swingUIClass.getPackageName());
				if (swingUIClass.isAnnotationPresent(LabelsScan.class)) {
					for (String pkg : swingUIClass.getAnnotation(LabelsScan.class).scope()) {
						LabelsDisplayerPool.registerUiPackage(pkg);
					}
				}
//				ckRegisterUIPackages.stop();
				/*
				 * Configure LabelsDisplayerPool for handling Swing Components
				 */
				LabelsDisplayerPool.setLoadingAction((ld, rtl) -> {
					if (ld instanceof Component) {
						Component c = (Component) ld;
						if (rtl) {
							c.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
						} else {
							c.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
						}
						c.revalidate();
					}
				});
				LabelsDisplayerPool.setAddingAction((ld, rtl) -> {
					if (ld instanceof Component) {
						Component c = (Component) ld;
						if (rtl) {
							c.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
						} else {
							c.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
						}
						c.revalidate();
					}
				});
				LabelsManagerPool.setLocale(application.getDefaultLocale());
				ckLabels.stop();
				try {
					Check ckSearchForSwingComponents = Benchmark.start(LumicoreSwing.class,
							"Search For Swing Components");
					Collection<Class<?>> swingComponents = injector.searchClassesByAnnotation(SwingComponent.class);
					Iterator<Class<?>> i = swingComponents.iterator();
					while (i.hasNext()) {
						Class<?> swingComponent = i.next();
						if (EntityEditorPanel.class.isAssignableFrom(swingComponent)) {
							i.remove();
							LOGGER.warn(
									"Autowire-Swing: Class ignored [%s] Editor classes are not components. Use EntityAccessControllerFactory instead.",
									swingComponent.getSimpleName());
						}
					}
					ckSearchForSwingComponents.stop();
					Check ckInitializeUIController = Benchmark.start(LumicoreSwing.class, "initialize SwingUI");
					SwingUI uiControllerInstance = null;
//						uiControllerInstance = (SwingUI) autoInstance(uiControllerClass);
					uiControllerInstance = (SwingUI) injector.getComponent(swingUIClass);
					if (Objects.isNull(uiControllerInstance)) {
						LumicoreSwing.this.exitSwingInitializerException = new ConfigurationException(
								ConfigurationException.INITIALIZE_SWING_UI_FAILED, swingUIClass.getSimpleName());
						ckInitializeUIController.stop();
						return;
					}
					injector.registerComponent(swingUIClass, uiControllerInstance, swingComponents);
					ckInitializeUIController.stop();
					uiControllerInstance.setupTheme();
					Check ckRegisterEditors = Benchmark.start(LumicoreSwing.class, "register editors");
					Collection<Class<?>> editors = injector.searchClassesAssignableFrom(EntityEditorPanel.class);
					for (Class<?> editorClass : editors) {
						@SuppressWarnings("unchecked")
						Class<EntityEditorPanel<?, ?>> editorClass1 = (Class<EntityEditorPanel<?, ?>>) editorClass;
						EntityAccessControllerFactory.register(uiControllerInstance, editorClass1);
					}
					ckRegisterEditors.stop();
					Check ckAutowireSwing = Benchmark.start(LumicoreSwing.class, "autowire-swing");
					for (Class<?> implementationClass : swingComponents) {
						injector.putImplementation(implementationClass);
						Class<?>[] interfaces = implementationClass.getInterfaces();
						for (Class<?> interface0 : interfaces) {
							injector.putInterfaceImplementation(interface0, implementationClass);
						}
					}
					StringBuilder autowireDiagnostics = new StringBuilder();
					StringBuilder labelsDisplayersDiagnostics = new StringBuilder();
					autowireDiagnostics.append(ApplicationUtils.createBanner("autowire swing")).append("\n");
					for (Class<?> cls : swingComponents) {
						Object classInstance = injector.getComponent(cls);
						if (Objects.isNull(classInstance)) {
							LumicoreSwing.this.exitSwingInitializerException = new ConfigurationException(
									ConfigurationException.INITIALIZE_SWING_COMPONENT_FAILED, cls.getSimpleName());
							return;
						} else {
							autowireDiagnostics.append(injector.autowire(cls, classInstance));
							labelsDisplayersDiagnostics.append(LabelsDisplayerPool.addLabelsDisplayers(classInstance));
						}
					}
//					System.err.println(labelsDisplayersDiagnostics.toString());
					LOGGER_IOC.info("%n%s", autowireDiagnostics.toString());
					LOGGER_SWING.info("%n%s", labelsDisplayersDiagnostics.toString());
					ckAutowireSwing.stop();
					Check ckShowUI = Benchmark.start(LumicoreSwing.class, "show UI");
					SplashScreenController.hideSplashScreen();
					uiControllerInstance.showUI();
					ckShowUI.stop();
//				} catch (InstantiationException e) {
//					e.printStackTrace();
//				} catch (IllegalAccessException e) {
//					e.printStackTrace();
//				} catch (IllegalArgumentException e) {
//					e.printStackTrace();
//				} catch (InvocationTargetException e) {
//					e.printStackTrace();
//				} catch (SecurityException e) {
//					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
					LumicoreSwing.this.exitSwingInitializerException = e;
//					SplashScreenController.hideSplashScreen();
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
		if (Objects.nonNull(exitSwingInitializerException)) {
			throw exitSwingInitializerException;
		}
	}

	/**
	 * Initializes a new instance of the specified editorClass.
	 * <p>
	 * The new instance is autowired and its labels have been loaded.
	 * 
	 * @param editorClass the editor to instantiate
	 * @return the editor instance or null if any exception was thrown when creating
	 *         the instance
	 */
	public static EntityEditorPanel<?, ?> initializeEditor(Class<EntityEditorPanel<?, ?>> editorClass) {
		Object editor = null;
		try {
			editor = injector.autoInstance(editorClass);
			injector.autowire(editorClass, editor);
			LabelsDisplayerPool.loadLabels(editor);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return (EntityEditorPanel<?, ?>) editor;
	}

	public static Application getApplication() {
		return injector.getApplication();
	}

	public static final <T> T initComponent(Class<T> cls) {
		T component;
		try {
			component = injector.autoInstance(cls);
			injector.autowire(cls, component);
			LabelsDisplayerPool.addLabelsDisplayers(component);
			return component;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}
