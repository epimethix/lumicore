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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.List;

public class Connector {
	public static enum Type {
		UML_INHERITANCE, UML_ASSOCIATION, UML_AGGREGATION, UML_COMPOSITION, UML_REALIZATION, UML_DEPENDENCY,
		
		ERD_MANY_TO_ONE, ERD_ONE_TO_MANY, ERD_ONE_TO_ONE, ERD_MANY_TO_MANY
	}
    private Point startPoint;
    private Point endPoint;
    List<Point> via;
    private Color color;
    private Stroke stroke;
    private Type type;
    
    public Connector(Point startPoint, Point endPoint, Color color) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.color = color;
    }
    
    public Point getStartPoint() {
        return startPoint;
    }
    
    public Point getEndPoint() {
        return endPoint;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }
    
    public void setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
    }
}