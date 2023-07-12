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
package com.github.epimethix.lumicore.devtools.gui.diagram.model;

public class DiagramEntityConfiguration {

	private boolean showStaticFields;
	private boolean showInstanceFields;
	private boolean showConstructors;
	private boolean showStaticMethods;
	private boolean showInstanceMethods;
	private boolean showMemberClasses;

	public DiagramEntityConfiguration() {}

	public DiagramEntityConfiguration(DiagramType type) {
		if (DiagramType.ERD.equals(type)) {
			showStaticFields = showInstanceFields = true;
			showStaticMethods = showInstanceMethods = showMemberClasses = showConstructors = false;
		} else {
			showStaticFields = showInstanceFields = showConstructors = true;
			showStaticMethods = showInstanceMethods = showMemberClasses = true;
		}
	}

	public DiagramEntityConfiguration(DiagramEntityConfiguration configuration) {
		this.showStaticFields = configuration.showStaticFields;
		this.showInstanceFields = configuration.showInstanceFields;
		this.showConstructors = configuration.showConstructors;
		this.showStaticMethods = configuration.showStaticMethods;
		this.showInstanceMethods = configuration.showInstanceMethods;
		this.showMemberClasses = configuration.showMemberClasses;
	}

	public boolean isShowStaticFields() {
		return showStaticFields;
	}

	public void setShowStaticFields(boolean showStaticFields) {
		this.showStaticFields = showStaticFields;
	}

	public boolean isShowInstanceFields() {
		return showInstanceFields;
	}

	public void setShowInstanceFields(boolean showInstanceFields) {
		this.showInstanceFields = showInstanceFields;
	}

	public boolean isShowConstructors() {
		return showConstructors;
	}

	public void setShowConstructors(boolean showConstructors) {
		this.showConstructors = showConstructors;
	}

	public boolean isShowStaticMethods() {
		return showStaticMethods;
	}

	public void setShowStaticMethods(boolean showStaticMethods) {
		this.showStaticMethods = showStaticMethods;
	}

	public boolean isShowInstanceMethods() {
		return showInstanceMethods;
	}

	public void setShowInstanceMethods(boolean showInstanceMethods) {
		this.showInstanceMethods = showInstanceMethods;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (showConstructors ? 1231 : 1237);
		result = prime * result + (showInstanceFields ? 1231 : 1237);
		result = prime * result + (showInstanceMethods ? 1231 : 1237);
		result = prime * result + (showStaticFields ? 1231 : 1237);
		result = prime * result + (showStaticMethods ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiagramEntityConfiguration other = (DiagramEntityConfiguration) obj;
		if (showConstructors != other.showConstructors)
			return false;
		if (showInstanceFields != other.showInstanceFields)
			return false;
		if (showInstanceMethods != other.showInstanceMethods)
			return false;
		if (showStaticFields != other.showStaticFields)
			return false;
		if (showStaticMethods != other.showStaticMethods)
			return false;
		return true;
	}
}
