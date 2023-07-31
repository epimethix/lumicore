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
package com.github.epimethix.lumicore.devtools.gui.diagram.dialog;

import java.util.Map;
import java.util.Optional;

import com.github.epimethix.lumicore.common.ui.labels.displayer.IgnoreLabels;
import com.github.epimethix.lumicore.devtools.fs.DiagramData;
import com.github.epimethix.lumicore.devtools.gui.DevToolsGUIController;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.Diagram;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramEntity;
import com.github.epimethix.lumicore.ioc.annotation.Autowired;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;

@SwingComponent
public class DiagramEditorDialog {

	@Autowired
	private NewDiagramDialog newDiagramDialog;

	@Autowired
	private AddEntityDialog addEntityDialog;

	@Autowired
	private ClassSelectorDialog classSelectorDialog;

	@Autowired
	private NewEntityWizard newEntityWizard;

	@Autowired
	private SetFontDialog setFontDialog;

	@Autowired
	private PropertiesDialog propertiesDialog;

	@Autowired
	private FieldEditorDialog addFieldDialog;

	@IgnoreLabels
	private final DevToolsGUIController guiController;

	public DiagramEditorDialog(DevToolsGUIController guiController) {
		this.guiController = guiController;
	}

	public Optional<Diagram> newDiagram() {
		return newDiagramDialog.showNewDiagramDialog();
	}

	public void addEntity(Diagram diagram) {
		addEntityDialog.showDialog(diagram);
	}

	public void showClassSelectorDialog(Diagram diagram) {
		classSelectorDialog.showDialog(guiController.getSourcesDirectory(), diagram);
	}

	public void newEntity(Diagram diagram) {
		newEntityWizard.showNewEntityWizard(diagram);
	}

	public void setFontSize(Diagram diagram) {
		setFontDialog.showSetFontDialog(diagram);
	}

	public void showProperties(Map<String, DiagramData> diagramEntity) {
		propertiesDialog.showPropertiesDialog(diagramEntity);
	}

	public void addField(DiagramEntity diagramEntity) {
		addFieldDialog.showAddFieldDialog(diagramEntity);
	}

	public boolean addConstructor(DiagramEntity diagramEntity) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean showRelations(Diagram diagram) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addMethod(DiagramEntity diagramEntity) {
		// TODO Auto-generated method stub
		return false;
	}
}
