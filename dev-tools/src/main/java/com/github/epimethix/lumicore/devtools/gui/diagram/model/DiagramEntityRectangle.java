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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.UUID;

public class DiagramEntityRectangle {

	private final String uuid = UUID.randomUUID().toString();
	private final Rectangle rectangle;
	private final Point fileLocation;
//	private int width;
//	private int height;
	private boolean sizeOld;
	private final Point initialLocation;
	private final Rectangle editButtonRectangle;

	public DiagramEntityRectangle(Point fileLocation) {
		this.rectangle = new Rectangle();
		this.rectangle.setLocation(fileLocation);
		this.fileLocation = fileLocation;
		this.initialLocation = new Point();
		this.editButtonRectangle = new Rectangle();
		sizeOld = true;
	}

	public Rectangle getRectangle() {
		return rectangle;
	}

	public Point getInitialLocation() {
		return initialLocation;
	}

	public void setLocation(int x, int y) {
		rectangle.setLocation(x, y);
		fileLocation.setLocation(x, y);
	}

	public void setSize(int width, int height) {
		rectangle.setSize(width, height);
//		this.width = width;
//		this.height = height;
		sizeOld = false;
	}

	public void setEditButtonBounds(int x, int y, int width, int height) {
		editButtonRectangle.setBounds(x - rectangle.x, y - rectangle.y, width, height);
	}

	public boolean editButtonContains(int x, int y) {
		return editButtonRectangle.contains(x - rectangle.x, y - rectangle.y);
	}

//	public boolean intersects(Rectangle r) {
//		return rectangle.intersects(r);
//	}

	public int getX() {
//		return rectangle.x;
		return fileLocation.x;
	}

	public int getY() {
//		return rectangle.y;
		return fileLocation.y;
	}

	public int getWidth() {
		return rectangle.width;
//		return width;
	}

	public int getHeight() {
		return rectangle.height;
//		return height;
	}

	public boolean isSizeOld() {
		return sizeOld;
	}

	public boolean contains(Point p) {
		return rectangle.contains(p);
//		boolean c = p.x >= fileLocation.x && p.y >= fileLocation.y && p.x <= fileLocation.x + width
//				&& p.y <= fileLocation.y + height;
//		PrintStream ps;
//		if (c) {
//			ps = System.err;
//		} else {
//			ps = System.out;
//		}
//		ps.printf("contains(%d, %d) minX: %d / minY: %d / maxX: %d / maxY: %d%n", p.x, p.y, fileLocation.x,
//				fileLocation.y, fileLocation.x + width, fileLocation.y + height);
//
//		return c;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileLocation == null) ? 0 : fileLocation.hashCode());
//		result = prime * result + height;
		result = prime * result + (sizeOld ? 1231 : 1237);
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
//		result = prime * result + width;
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
		DiagramEntityRectangle other = (DiagramEntityRectangle) obj;
		if (fileLocation == null) {
			if (other.fileLocation != null)
				return false;
		} else if (!fileLocation.equals(other.fileLocation))
			return false;
//		if (height != other.height)
//			return false;
		if (sizeOld != other.sizeOld)
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
//		if (width != other.width)
//			return false;
		return true;
	}

	public boolean sizeEquals(int width, int height) {
		return rectangle.width == width && rectangle.height == height;
	}

	public void setDeltaLocation(int deltaX, int deltaY) {
		setLocation(initialLocation.x + deltaX, initialLocation.y + deltaY);
	}

	public void setInitialLocation() {
		initialLocation.setLocation(fileLocation);
	}
}
