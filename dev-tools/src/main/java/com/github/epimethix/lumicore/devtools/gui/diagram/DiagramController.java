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
package com.github.epimethix.lumicore.devtools.gui.diagram;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.github.epimethix.lumicore.common.ui.labels.displayer.IgnoreLabels;
import com.github.epimethix.lumicore.devtools.ClassPathIndex;
import com.github.epimethix.lumicore.devtools.ClassPathIndex.IndexedClass;
import com.github.epimethix.lumicore.devtools.fs.DiagramData;
import com.github.epimethix.lumicore.devtools.gui.GUIController;
import com.github.epimethix.lumicore.devtools.gui.diagram.dialog.DiagramEditorDialog;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.Diagram;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramEntity;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.EntityClass;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.EntitySource;
import com.github.epimethix.lumicore.ioc.annotation.Autowired;
import com.github.epimethix.lumicore.ioc.annotation.PostConstruct;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;
import com.github.epimethix.lumicore.sourceutil.ProjectSource;

@SwingComponent
public class DiagramController {

	@Autowired
	private GUIController guiController;

	private final File sourcesDirectory;

//	private final int indexOfRelativePath;

	@IgnoreLabels
	private final Map<String, DiagramEntity> diagramEntities;

//	private final ClassSelectorDialog classSelectorDialog;

	@Autowired
	private DiagramEditorDialog diagramEditorDialog;

	public DiagramController(GUIController guiController) {
		this.sourcesDirectory = guiController.getSourcesDirectory();
		this.diagramEntities = new HashMap<>();
//		this.classSelectorDialog = new ClassSelectorDialog(guiController.getFrame(), sourcesDirectory);
//		this.diagramEditorDialog = new DiagramEditorDialog(guiController);
	}

	@PostConstruct
	private void init() {}

	public DiagramEntity getDiagramEntity(String key) throws IOException {
		if (diagramEntities.containsKey(key)) {
			return diagramEntities.get(key);
		}
		IndexedClass ic = ClassPathIndex.getIndex(key).orElse(null);
		if (Objects.nonNull(ic)) {
			if (ic.isSource()) {
				File javaFile = ProjectSource.getJavaFile(new File(ic.getLocation().toURL().getPath()), key);
				DiagramEntity de = new EntitySource(javaFile);
				putDiagramEntity(key, de);
				return de;
			} else {
				try {
					Class<?> cls = Class.forName(key);
					DiagramEntity de = new EntityClass(cls);
					putDiagramEntity(key, de);
					return de;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public void putDiagramEntity(String key, DiagramEntity diagramEntity) {
//		classSelectorDialog.put(key);
		diagramEntities.put(key, diagramEntity);
	}

	public final void showClassSelectorDialog(Diagram diagram) {
		diagramEditorDialog.showClassSelectorDialog(diagram);
	}

	public void openDiagram() {
		Optional<File> open = guiController.showOpenDialog(".json");
		if (open.isPresent()) {
			Diagram diagram;
			try {
				diagram = new Diagram(this, open.get());
				guiController.openDiagramTab(diagram);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void newDiagram() {
		Optional<Diagram> o = diagramEditorDialog.newDiagram();
		if (o.isPresent()) {
			Diagram diagram = o.get();
			DiagramView diagramView = guiController.openDiagramTab(diagram);
			showClassSelectorDialog(diagram);
			diagramView.repaint();
		}
	}

	public File getSourcesDirectory() {
		return sourcesDirectory;
	}

	public GUIController getGUIController() {
		return guiController;
	}

	public void newEntity(Diagram diagram) {
		diagramEditorDialog.newEntity(diagram);
	}

	public void addEntity(Diagram diagram) throws IOException {
		diagramEditorDialog.addEntity(diagram);
	}

	public void addField(DiagramEntity diagramEntity) {
		diagramEditorDialog.addField(diagramEntity);
	}

	public boolean addConstructor(DiagramEntity diagramEntity) {
		return diagramEditorDialog.addConstructor(diagramEntity);
	}

	public boolean addMethod(DiagramEntity diagramEntity) {
		return diagramEditorDialog.addMethod(diagramEntity);
	}

	public void showProperties(Map<String, DiagramData> diagramEntity) {
		diagramEditorDialog.showProperties(diagramEntity);
	}

	public void showSetFontSize(Diagram diagram) {
		diagramEditorDialog.setFontSize(diagram);
	}

	public void viewSource(File file, String code) {
		guiController.openCodeTab(file, code);
	}

	public boolean showRelations(Diagram diagram) {
		return diagramEditorDialog.showRelations(diagram);
	}
}
