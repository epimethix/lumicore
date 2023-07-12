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
package com.github.epimethix.lumicore.devtools.gui.diagram.model.connector;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;
import java.util.Objects;

public final class Relation {

	public static enum RelationType {
		UML_ASSOCIATION, UML_INHERITANCE, UML_AGGREGATION, UML_COMPOSITION, UML_REALIZATION, UML_DEPENDENCY,
		ERD_ONE_TO_ONE, ERD_ONE_TO_MANY, ERD_MANY_TO_ONE, ERD_MANY_TO_MANY;
	}

	private final RelationType type;
	private String parentEntityName;
	private String childEntityName;
	private Point parentConnectorLocation;
	private Point childConnectorLocation;
	private String parentMultiplicity;
	private String childMultiplicity;
	private String label;
	private List<Point> viaLocations;

	public Relation(RelationType type) {
		this.type = type;
		if(type.equals(RelationType.ERD_ONE_TO_ONE)) {
			parentMultiplicity = "1";
			childMultiplicity = "1";
		} else if(type.equals(RelationType.ERD_MANY_TO_ONE)) {
			parentMultiplicity = "1";
			childMultiplicity = "n";
		} else if(type.equals(RelationType.ERD_ONE_TO_MANY)) {
			parentMultiplicity = "n";
			childMultiplicity = "1";
		} else if(type.equals(RelationType.ERD_MANY_TO_MANY)) {
			parentMultiplicity = "n";
			childMultiplicity = "n";
		}
	}

	public Relation(Relation r) {
		this.type = r.type;
		this.parentEntityName = r.parentEntityName;
		this.childEntityName = r.childEntityName;
		this.parentConnectorLocation = r.parentConnectorLocation;
		this.childConnectorLocation = r.childConnectorLocation;
		this.parentMultiplicity = r.parentMultiplicity;
		this.childMultiplicity = r.childMultiplicity;
		this.label = r.label;
		this.viaLocations = r.viaLocations;
	}

	public RelationType getType() {
		return type;
	}

	public final String getChildEntityName() {
		return childEntityName;
	}

	public final void setChildEntityName(String childEntityName) {
		this.childEntityName = childEntityName;
	}

	public final Point getChildConnectorLocation() {
		return childConnectorLocation;
	}

	public final void setChildConnectorLocation(Point childConnectorLocation) {
		this.childConnectorLocation = childConnectorLocation;
	}

	public final String getParentEntityName() {
		return parentEntityName;
	}

	public final void setParentEntityName(String parentEntityName) {
		this.parentEntityName = parentEntityName;
	}

	public final Point getParentConnectorLocation() {
		return parentConnectorLocation;
	}

	public final void setParentConnectorLocation(Point parentConnectorLocation) {
		this.parentConnectorLocation = parentConnectorLocation;
	}

	public final String getChildMultiplicity() {
		return childMultiplicity;
	}

	public final void setChildMultiplicity(String multiplicity) {
		this.childMultiplicity = multiplicity;
	}

	public final String getParentMultiplicity() {
		return parentMultiplicity;
	}

	public final void setParentMultiplicity(String multiplicity) {
		this.parentMultiplicity = multiplicity;
	}

	public final String getLabel() {
		return label;
	}

	public final void setLabel(String label) {
		this.label = label;
	}

	public List<Point> getViaLocations() {
		return viaLocations;
	}

	public void setViaLocations(List<Point> viaLocations) {
		this.viaLocations = viaLocations;
	}
	
	public final void paintConnector(Graphics2D g) {
		if(Objects.isNull(viaLocations) || viaLocations.isEmpty()) {
			g.drawLine(childConnectorLocation.x, childConnectorLocation.y, parentConnectorLocation.x, parentConnectorLocation.y);
		} else {
			Point start = childConnectorLocation;
			Point end;
			int i = 0;
			do {
				end = viaLocations.get(i);
				g.drawLine(start.x, start.y, end.x, end.y);
				start = end;
				i++;
			} while (i < viaLocations.size());
			end = parentConnectorLocation;
			g.drawLine(start.x, start.y, end.x, end.y);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((childEntityName == null) ? 0 : childEntityName.hashCode());
		result = prime * result + ((parentEntityName == null) ? 0 : parentEntityName.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Relation other = (Relation) obj;
		if (childEntityName == null) {
			if (other.childEntityName != null)
				return false;
		} else if (!childEntityName.equals(other.childEntityName))
			return false;
		if (parentEntityName == null) {
			if (other.parentEntityName != null)
				return false;
		} else if (!parentEntityName.equals(other.parentEntityName))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
