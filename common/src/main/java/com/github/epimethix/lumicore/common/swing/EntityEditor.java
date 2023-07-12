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
package com.github.epimethix.lumicore.common.swing;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.github.epimethix.lumicore.common.orm.model.Entity;

public interface EntityEditor<E extends Entity<?>> {
	/**
	 * Creates a new/empty instance of T
	 * 
	 * @return a new/empty instance of T
	 */
	E initItem();
	default Comparator<E> getComparator(){
		return (a,b)->a.toString().compareTo(b.toString());
	}
//	boolean load(E item);
	void load(Object item);
	Optional<SwingWorker<E, Void>> save(Consumer<E> onDone);
	boolean clear();
	boolean hasChanges();
	default boolean isDeletable() {
		return false;
	}
//	void delete(Object id);
	default void setParent(Object parent) {};
}
