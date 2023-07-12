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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.epimethix.lumicore.devtools.fs.DiagramData;
import com.github.epimethix.lumicore.devtools.fs.DiagramFile;
import com.github.epimethix.lumicore.devtools.gui.diagram.DiagramController;

public class Diagram {

	private final DiagramController diagramController;

	private final Map<String, DiagramEntity> diagramEntities;
	private final Map<String, DiagramEntityRectangle> diagramRectangles;

	private final DiagramFile diagramFile;

	public Diagram(DiagramController diagramController, File diagramFile) throws IOException {
		this(diagramController, diagramFile, null);
	}

	public Diagram(DiagramController diagramController, File diagramFile, DiagramType type) throws IOException {
		this.diagramController = diagramController;
		this.diagramFile = DiagramFile.get(diagramFile, type);
		this.diagramRectangles = new HashMap<>();
		diagramEntities = new HashMap<>();
		if (Objects.nonNull(diagramFile) && diagramFile.exists()) {
			Set<String> keySet = this.diagramFile.keySet();
			for (String key : keySet) {
				DiagramEntity de = diagramController.getDiagramEntity(key);
				if (Objects.nonNull(de)) {
					diagramEntities.put(key, de);
					diagramRectangles.put(key, new DiagramEntityRectangle(this.diagramFile.get(key).getLocation()));
				}
			}
		}
	}

	public boolean hasChanges() {
		if (diagramFile.hasChanges()) {
			return true;
		}
		for (String key : diagramEntities.keySet()) {
			DiagramEntity de = diagramEntities.get(key);
			if (Objects.nonNull(de)) {
				if (de.hasChanges()) {
					return true;
				}
			}
		}
		return false;
	}

	public void setPath(Path path) {
		this.diagramFile.setPath(path);
	}

	public void persist() throws IOException {
		persist(diagramFile.getFile());
	}

	public void persist(File file) throws IOException {
		if (diagramFile.hasChanges() || !diagramFile.getFile().equals(file)) {
			DiagramFile.persist(diagramFile, file);
		}
		for (String key : diagramEntities.keySet()) {
			DiagramEntity de = diagramEntities.get(key);
			if (de.hasChanges()) {
				de.persist();
			}
		}
	}

	public List<String> getClassNames() {
		return new ArrayList<>(diagramEntities.keySet());
	}

	public String getName() {
		return diagramFile.getFileName();
	}

	public String getPath() {
		return diagramFile.getPath();
	}
	
	public DiagramType getType() {
		return diagramFile.getType();
	}

	public void setType(DiagramType type) {
		diagramFile.setType(type);
	}

	public void setClasses(List<String> list) {
		List<String> initialClasses = new ArrayList<>(diagramEntities.keySet());
		for (String clsName : initialClasses) {
			if (!list.contains(clsName)) {
				diagramFile.remove(clsName);
				diagramEntities.remove(clsName);
				diagramRectangles.remove(clsName);
			}
		}
		for (String clsName : list) {
			if (!diagramFile.contains(clsName)) {
				try {
					DiagramEntity de = diagramController.getDiagramEntity(clsName);
					if (Objects.nonNull(de)) {
						addEntity(clsName, de);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void addEntity(String key, DiagramEntity diagramEntity) {
		diagramEntities.put(key, diagramEntity);
		DiagramData dd = new DiagramData(diagramFile.getType());
		diagramFile.put(key, dd);
		diagramRectangles.put(key, new DiagramEntityRectangle(dd.getLocation()));
	}

	public Set<String> keySet() {
		return diagramEntities.keySet();
	}

	public DiagramEntity getDiagramEntity(String key) {
		return diagramEntities.get(key);
	}

//	public Point

	public DiagramEntityRectangle getRectangle(String key) {
		return diagramRectangles.get(key);
	}

	public File getFile() {
		return diagramFile.getFile();
	}

	public int getFontSize() {
		return diagramFile.getFontSize();
	}

	public void setFontSize(int fontSize) {
		diagramFile.setFontSize(fontSize);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((diagramFile == null) ? 0 : diagramFile.hashCode());
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
		Diagram other = (Diagram) obj;
		if (diagramFile == null) {
			if (other.diagramFile != null)
				return false;
		} else if (!diagramFile.equals(other.diagramFile))
			return false;
		return true;
	}

//	public String getCode() {
//		Set<String> keySet = keySet();
//		if(keySet.size() > 0) {
//			
//			DiagramEntity de = diagramEntities.get(keySet.iterator().next());
//			return de.getCode();
//		}
//		return "";
//	}

	public DiagramData getDiagramData(String key) {
		return diagramFile.get(key);
	}
}
