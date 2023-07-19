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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.epimethix.lumicore.devtools.DevTools;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramType;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.connector.Relation;
import com.github.epimethix.lumicore.swing.LumicoreSwing;

public class DiagramFile {
	
	public static void persist(DiagramFile diagramFile) throws IOException {
		persist(diagramFile, diagramFile.file);
	}

	public static void persist(DiagramFile diagramFile, File file) throws IOException {
		if (!diagramFile.file.equals(file)) {
			diagramFile.setFile(file);
		}
		ObjectMapper om = new ObjectMapper();
		String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(diagramFile);
		try (FileWriter fw = new FileWriter(diagramFile.file, diagramFile.charset)) {
			fw.write(json);
			fw.flush();
		}
		diagramFile.setInitialData();
	}

	public static DiagramFile get(File diagramFile, DiagramType type) {
		return get(diagramFile, ((DevTools)LumicoreSwing.getApplication()).getDefaultCharset(), type);
	}

	public static DiagramFile get(File diagramFile, Charset charset, DiagramType type) {
		ObjectMapper om = new ObjectMapper();
		if (Objects.nonNull(diagramFile) && diagramFile.exists()) {
			try (FileReader fr = new FileReader(diagramFile.getPath(), charset)) {
				DiagramFile d = om.readValue(fr, DiagramFile.class);
				d.setFile(diagramFile);
				d.setInitialData();
				return d;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			return new DiagramFile(diagramFile, charset, type);
		}
		return null;
	}

	private File file;
	private final Charset charset;
	private DiagramType type;
	private int fontSize;
	private int initialFontSize;
//	private File sourcesDirectory;
	private Map<String, DiagramData> data;
	private List<Relation> relations;
	private Map<String, DiagramData> initialData;
	private List<Relation> initialRelations;

	public DiagramFile() {
		this(null, ((DevTools)LumicoreSwing.getApplication()).getDefaultCharset(), null);
	}

	private DiagramFile(File file, Charset charset, DiagramType type) {
		this.file = file;
		this.charset = charset;
		if (Objects.nonNull(type)) {
			this.type = type;
		} else {
			this.type = DiagramType.UML;
		}

		fontSize = 16;

		data = new HashMap<>();
		relations = new ArrayList<>();
		initialData = new HashMap<>();
		initialRelations = new ArrayList<>();
	}

	private void setInitialData() {
		initialData.clear();
		Set<String> k = data.keySet();
		for (String key : k) {
			initialData.put(key, new DiagramData(data.get(key)));
		}
		initialRelations.clear();
		for(Relation r : relations) {
			initialRelations.add(new Relation(r));
		}
		initialFontSize = fontSize;
	}

	public boolean hasChanges() {
		return !data.equals(initialData)|| !relations.equals(initialRelations) || initialFontSize != fontSize;
	}

//	public File getSourcesDirectory() {
//		return sourcesDirectory;
//	}
//
//	public void setSourcesDirectory(File sourcesDirectory) {
//		this.sourcesDirectory = sourcesDirectory;
//	}

	public DiagramType getType() {
		return type;
	}

	public void setType(DiagramType type) {
		this.type = type;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public Map<String, DiagramData> getData() {
		return data;
	}

	public void setData(Map<String, DiagramData> data) {
		this.data = data;
	}

	public List<Relation> getRelations() {
		return relations;
	}

	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}
	
	public void addRelation(Relation r) {
		relations.add(r);
	}

	@JsonIgnore
	public Set<String> keySet() {
		return data.keySet();
	}

	public void put(String key, DiagramData diagramData) {
		data.put(key, diagramData);
	}

	public DiagramData get(String key) {
		return data.get(key);
	}

	public DiagramData remove(String key) {
		return data.remove(key);
	}

	public void setPath(Path path) {
		this.file = path.toFile();
	}

	public void setFile(File file) {
		this.file = file;
	}

	@JsonIgnore
	public File getFile() {
		return file;
	}

	@JsonIgnore
	public String getFileName() {
		return file.getName();
	}

	@JsonIgnore
	public String getPath() {
		return file.getPath();
	}

	public boolean contains(String key) {
		return data.containsKey(key);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
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
		DiagramFile other = (DiagramFile) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}
}
