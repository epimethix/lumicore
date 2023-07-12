/*
 * Copyright 2022 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.swing.entityaccess;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.SwingUtilities;

import com.github.epimethix.lumicore.benchmark.Benchmark;
import com.github.epimethix.lumicore.benchmark.Benchmark.Check;
import com.github.epimethix.lumicore.common.Reflect;
import com.github.epimethix.lumicore.common.orm.Repository;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;
import com.github.epimethix.lumicore.orm.EntityController;
import com.github.epimethix.lumicore.swing.LumicoreSwingImpl;
import com.github.epimethix.lumicore.swing.editor.AbstractEditorPanel;

public class EntityAccessControllerFactory {
	private static final Logger LOGGER = Log.getLogger(Log.CHANNEL_SWING);
	
	private static class EntityAccessMapping {
		private final SwingUI ui;
		private final Repository<?, ?> repository;
		private final Class<AbstractEditorPanel<?, ?>> editorClass;

		public EntityAccessMapping(SwingUI ui, Repository<?, ?> repository, Class<AbstractEditorPanel<?, ?>> editorClass) {
			this.ui = ui;
			this.repository = repository;
			this.editorClass = editorClass;
		}
	}

	private static Map<Class<?>, EntityAccessMapping> entityAccessMappings = new HashMap<>();

	public static final void register(SwingUI ui, Class<AbstractEditorPanel<?, ?>> editorClass) throws RuntimeException {
		if (!AbstractEditorPanel.class.isAssignableFrom(editorClass)) {
			throw new RuntimeException("Editor<?> class should extend AbstractEditorPanel.");
		}
		Class<? extends Entity<?>> entityClass = Reflect.getEntityClass(editorClass);
		if (Objects.isNull(entityClass)) {
			throw new RuntimeException("Could not obtain Entity<?> class");
		}
		Repository<?, ?> repository = EntityController.getRepository(entityClass);
		if (Objects.isNull(repository)) {
			throw new RuntimeException(
					"Could not obtain Repository<?, ?> instance for [" + entityClass.getSimpleName() + "]");
		}
//		try {
//			Constructor<?>[] constructors = editorClass.getDeclaredConstructors();
//			System.out.println(Arrays.toString(constructors[0].getParameterTypes()));
//			Constructor<?> c = editorClass.getConstructor(ui.getClass(), repository.getClass());
//			Lumicore.initializeEditor(editorClass);
//		} catch (NoSuchMethodException | SecurityException e) {
//			e.printStackTrace();
//			throw new RuntimeException("Could not obtain the Constructor<?> 'SwingUI.class, Repository.class'");
//		}
		entityAccessMappings.put(entityClass, new EntityAccessMapping(ui, repository, editorClass));
		LOGGER.trace("Editor class %s was registered", editorClass.getSimpleName());
	}

	public static final EntityAccessController getEntityAccessController(Class<?> entityClass) {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new RuntimeException(
					"EntityAccessViewFactory.getEntityAccessView() must be called from the Event Dispatch Thread");
		}
		Check ckGetEntityAccessController = Benchmark.start(EntityAccessControllerFactory.class,
				"getEntityAccessController(" + entityClass.getSimpleName() + ")", "getEntityAccessController");
		try {
			EntityAccessMapping eam = entityAccessMappings.get(entityClass);
			if (Objects.nonNull(eam)) {
				try {
					AbstractEditorPanel<?, ?> editor = LumicoreSwingImpl.initializeEditor(eam.editorClass);
//							(AbstractEditorPanel<?, ?>) eam.editorClass
//							.getConstructor(eam.ui.getClass(), eam.repository.getClass())
//							.newInstance(eam.ui, eam.repository);
					editor.finishForm();
					EntityAccessController v = new EntityAccessController(eam.ui, eam.repository, editor);
					LOGGER.trace("Editor class for %s was created", entityClass.getSimpleName());
					return v;
				}

				catch

				(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
//					e.printStackTrace();
					LOGGER.error(e);
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
			}
			throw new RuntimeException("Could not load EntityAccessView");
		} finally {
			ckGetEntityAccessController.stop();
		}
	}

	private EntityAccessControllerFactory() {}
}
