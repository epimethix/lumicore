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
package com.github.epimethix.lumicore.devtools.fs;

import java.awt.Point;

import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramEntityConfiguration;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramType;

public class DiagramData {
	private Point location;
	private DiagramEntityConfiguration configuration;

	public DiagramData() {}

	public DiagramData(DiagramType type) {
		this.location = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
		this.configuration = new DiagramEntityConfiguration(type);
	}

	public DiagramData(DiagramData diagramData) {
		this.location = new Point(diagramData.location);
		this.configuration = new DiagramEntityConfiguration(diagramData.configuration);
	}

	public DiagramData(DiagramEntityConfiguration configuration) {
		this.configuration = configuration;
		this.location = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public DiagramEntityConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(DiagramEntityConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
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
		DiagramData other = (DiagramData) obj;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}
}
