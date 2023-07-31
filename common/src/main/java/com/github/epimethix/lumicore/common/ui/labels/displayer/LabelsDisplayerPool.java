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
package com.github.epimethix.lumicore.common.ui.labels.displayer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import com.github.epimethix.lumicore.benchmark.Benchmark;
import com.github.epimethix.lumicore.benchmark.Benchmark.Check;
import com.github.epimethix.lumicore.common.Reflect;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManagerPool;
import com.github.epimethix.lumicore.common.ui.labels.manager.RTL;

public final class LabelsDisplayerPool {

//	private static final Logger LOGGER = Log.getLogger();

	private final static List<LabelsDisplayer> labelsDisplayers = new ArrayList<>();

	private final static Set<String> uiPackages = new HashSet<>();

	private static Locale currentLocale = Locale.getDefault();

	private static BiConsumer<LabelsDisplayer, Boolean> loadingAction;
	private static BiConsumer<LabelsDisplayer, Boolean> addingAction;

	public static void setLoadingAction(BiConsumer<LabelsDisplayer, Boolean> loadingAction) {
		LabelsDisplayerPool.loadingAction = loadingAction;
	}

	public static void setAddingAction(BiConsumer<LabelsDisplayer, Boolean> addingAction) {
		LabelsDisplayerPool.addingAction = addingAction;
	}

	/**
	 * UI Packages are registered here to enable labels displayer scan
	 * 
	 * @param packageName the ui package that contains implementations of the
	 *                    LabelsDisplayer interface.
	 * @return true if the package was added, false if it is registered already or
	 *         packageName is null
	 */
	public final static boolean registerUiPackage(String packageName) {
		if (Objects.isNull(packageName)) {
			return false;
		}
		for (Iterator<String> i = uiPackages.iterator(); i.hasNext();) {
			String next = i.next();
			if (!next.equals(packageName) && next.startsWith(packageName)) {
				i.remove();
			}
		}
		return uiPackages.add(packageName);
	}

	public static final boolean isInScope(String packageName) {
		if (uiPackages.contains(packageName)) {
			return true;
		}
		for (String scope : uiPackages) {
			if (packageName.startsWith(scope)) {
				return true;
			}
		}
		return false;
	}

//	private Object addingAction;

//	private final BiConsumer<LabelsDisplayer, Boolean> addingAction;
//	private final BiConsumer<LabelsDisplayer, Boolean> loadingAction;

	// @Override
	public final static boolean addLabelsDisplayer(Object labelsDisplayer) {
		if (labelsDisplayer instanceof LabelsDisplayer) {
			LabelsDisplayer ld = (LabelsDisplayer) labelsDisplayer;
			if (!labelsDisplayers.contains(ld)) {
//				System.err.println("loading " + labelsDisplayer.getClass().getSimpleName());
				ld.loadLabels();
				boolean rtl = RTL.isRTL(currentLocale);
				ld.setOrientation(rtl);
				if (Objects.nonNull(addingAction)) {
					addingAction.accept(ld, rtl);
				}
				labelsDisplayers.add(ld);
				return true;
//			} else {

//				System.err.println("skipping " + labelsDisplayer.getClass().getSimpleName());
			}
		}
		return false;
	}

//	@Override
	public final static boolean removeLabelsDisplayer(Object labelsDisplayer) {
		if (labelsDisplayer instanceof LabelsDisplayer) {
			LabelsDisplayer ld = (LabelsDisplayer) labelsDisplayer;
			if (labelsDisplayers.contains(ld)) {
				labelsDisplayers.remove(ld);
				return true;
			}
		}
		return false;
	}

	public final static void loadLabels(boolean setOrientation) {
//		if (!SwingUtilities.isEventDispatchThread()) {
//			throw new RuntimeException("Labels.loadLabels() must be called from the Event Dispatch Thread");
//		}
		currentLocale = Locale.getDefault();
		List<LabelsDisplayer> labelsDisplayersReversed = new ArrayList<>(labelsDisplayers);
		Collections.reverse(labelsDisplayersReversed);
		boolean rtl = RTL.isRTL(currentLocale);
//		System.err.println();
		for (LabelsDisplayer ld : labelsDisplayersReversed) {
//			System.err.println(ld.getClass());
			ld.loadLabels();
			if (setOrientation) {
				ld.setOrientation(rtl);
			}
			if (Objects.nonNull(loadingAction)) {
				loadingAction.accept(ld, rtl);
			}
		}
	}

	public static String addLabelsDisplayers(Object component) {
		StringBuilder diagnostics = new StringBuilder();
		scanForLabelsDisplayers(component, "", new ArrayList<>(), true, diagnostics);
		return diagnostics.toString();
	}

	public static String loadLabels(Object component) {
		StringBuilder diagnostics = new StringBuilder();
		scanForLabelsDisplayers(component, "", new ArrayList<>(), false, diagnostics);
		return diagnostics.toString();
	}

	public static String removeLabelsDisplayers(Object component) {
		StringBuilder diagnostics = new StringBuilder();
		scanToRemoveLabelsDisplayers(component, diagnostics);
		return diagnostics.toString();
	}

	private static void scanToRemoveLabelsDisplayers(Object component, StringBuilder diagnostics) {
		diagnostics.append(String.format("### Scanning [%s] for removal%n", component.getClass().getSimpleName()));
		scanForLabelsDisplayers(component, "", new ArrayList<>(), false, diagnostics, true, "");
	}

	private static void scanForLabelsDisplayers(Object component, String prefix, List<Object> path, boolean add,
			StringBuilder diagnostics) {
		diagnostics.append(String.format("### Scanning [%s] for labels%n", component.getClass().getSimpleName()));
		Check ckLabelsDisplayersScan = Benchmark.start(LabelsDisplayerPool.class,
				"scanForLabelsDisplayers(" + component.getClass().getSimpleName() + ")", "scanForLabelsDisplayers");
		scanForLabelsDisplayers(component, prefix, path, add, diagnostics, false, "");
		ckLabelsDisplayersScan.stop();
	}

	private static void scanForLabelsDisplayers(Object component, String prefix, List<Object> path, boolean add,
			StringBuilder diagnostics, boolean remove, String fieldName) {
		if (Objects.isNull(component)) {
			return;
		}
		if (path.contains(component)) {
			return;
		}
		List<Object> p = new ArrayList<>(path);
		p.add(component);
		Class<?> componentClass = component.getClass();
		String compPackageName = componentClass.getPackageName();
		if (!LabelsDisplayerPool.isInScope(compPackageName)) {
//			System.err.println("Break on: " + compPackageName);
			return;
		}
		diagnostics.append(String.format("%s+-LabelsDisplayers Scan %s (%s)", prefix,
				fieldName.isEmpty() ? "-" : fieldName, component.getClass().getSimpleName()));
		if (LabelsDisplayer.class.isAssignableFrom(componentClass)) {
			if (add) {
				if (LabelsDisplayerPool.addLabelsDisplayer(component)) {
					diagnostics.append(" [ADDED]");
				}
			} else if (remove) {
				if (LabelsDisplayerPool.removeLabelsDisplayer(component)) {
					diagnostics.append(" [REMOVED]");
				}
			} else {
				((LabelsDisplayer) component).loadLabels();
				((LabelsDisplayer) component).setOrientation(LabelsManagerPool.isRTL());
				diagnostics.append(" [LOADED]");
			}
		}
		diagnostics.append(String.format("%n"));
		do {
			compPackageName = componentClass.getPackageName();
			if (!LabelsDisplayerPool.isInScope(compPackageName)) {
				break;
			}
			Field[] declaredFields = componentClass.getDeclaredFields();
			for (Field field : declaredFields) {
				if (field.isAnnotationPresent(IgnoreLabels.class)) {
					continue;
				}
				Class<?> fieldType = field.getType();
				if (Reflect.isComponent(fieldType) || Reflect.isSwingComponent(fieldType)) {
					continue;
				} else if (fieldType.isPrimitive() || Reflect.typeEquals(fieldType, String.class)) {
					continue;
				} else if (Modifier.isStatic(field.getModifiers())) {
					continue;
				} else {
					Object fieldValue;
					try {
						field.setAccessible(true);
						fieldValue = field.get(component);
						if (Collection.class.isAssignableFrom(fieldType)) {
							Collection<?> collection = (Collection<?>) fieldValue;
							if (Objects.nonNull(collection)) {
								List<Object> p2 = new ArrayList<>(p);
								p2.add(collection);
								for (Object o : collection) {
									scanForLabelsDisplayers(o, "  " + prefix, p2, add, diagnostics, remove,
											componentClass.getSimpleName() + "." + field.getName());
								}
							}
						} else {
							scanForLabelsDisplayers(fieldValue, "  ".concat(prefix), p, add, diagnostics, remove,
									componentClass.getSimpleName() + "." + field.getName());
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		} while (Objects.nonNull(componentClass = componentClass.getSuperclass()));
	}
}
