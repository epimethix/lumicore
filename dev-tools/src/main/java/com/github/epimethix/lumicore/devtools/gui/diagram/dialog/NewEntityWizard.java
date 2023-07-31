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

import static com.github.epimethix.lumicore.swing.util.GridBagUtils.addGridBagLine;
import static com.github.epimethix.lumicore.swing.util.GridBagUtils.finishGridBagForm;
import static com.github.epimethix.lumicore.swing.util.GridBagUtils.initGridBagConstraints;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.common.orm.model.MutableEntity;
import com.github.epimethix.lumicore.common.ui.Answer;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.devtools.gui.D;
import com.github.epimethix.lumicore.devtools.gui.DevToolsGUIController;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.Diagram;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.EntitySource;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;
import com.github.epimethix.lumicore.orm.annotation.entity.Table;
import com.github.epimethix.lumicore.orm.annotation.field.PrimaryKey;
import com.github.epimethix.lumicore.sourceutil.JavaSource;
import com.github.epimethix.lumicore.sourceutil.ProjectSource;
import com.github.epimethix.lumicore.sourceutil.JavaSource.AnnotationSource;
import com.github.epimethix.lumicore.sourceutil.JavaSource.Builder;
import com.github.epimethix.lumicore.sourceutil.JavaSource.FieldSource;
import com.github.epimethix.lumicore.sourceutil.JavaSource.MethodSource;
import com.github.epimethix.lumicore.swing.dialog.wizard.AbstractWizard;
import com.github.epimethix.lumicore.swing.dialog.wizard.WizardUI;

@SwingComponent
public class NewEntityWizard extends AbstractWizard implements LabelsDisplayer {

	private final DevToolsGUIController guiController;
	private final Icon icon = new ImageIcon(getClass().getResource("/img/wizard.png"));

	/*
	 * Page 1
	 */
	private final JLabel lbClassName;
	private final JLabel lbPackageName;
	private final JComboBox<String> cbPackageName;
	private final JTextField tfClassName;
	private final JCheckBox ckAbstract;

	/*
	 * Page 2
	 */
	private final JLabel lbPattern;
	private final JRadioButton rbMutable;
	private final JRadioButton rbImmutable;
	private final JLabel lbId;
	private final JRadioButton rbLong;
	private final JRadioButton rbString;
	private final JCheckBox ckAutoIncrement;
	private final JCheckBox ckAutoUUID;
	private final JCheckBox ckWithoutRowId;

	@Override
	public void clear() {
		model.clear();
		// @formatter:off
		ComboBoxModel<String> model = new DefaultComboBoxModel<>(
			ClassSelectorDialog.listPackages(guiController.getSourcesDirectory())
			.stream()
			.map((uip) -> uip.getPackageName())
			.collect(Collectors.toList())
			.toArray(new String[] {})
		);
		// @formatter:on
		cbPackageName.setModel(model);
		tfClassName.setText("");
		ckAbstract.setSelected(false);
		rbImmutable.setSelected(true);
		rbLong.setSelected(true);
		ckAutoIncrement.setSelected(false);
		ckAutoIncrement.setEnabled(true);
		ckAutoUUID.setSelected(false);
		ckAutoUUID.setEnabled(false);
		ckWithoutRowId.setSelected(false);
	}

	private final class Model {
		private String packageName;
		private String className;
		private String fullClassName;
		private boolean isAbstract;
		private boolean mutablePattern;
		private Class<?> idType;
		private boolean autoIncrement;
		private boolean autoUUID;
		private boolean withoutRowId;
		private File javaFile;
		private EntitySource entitySource;

		private Model() {}

		private void clear() {
			/*
			 * Page 1
			 */
			packageName = null;
			className = null;
			isAbstract = false;
			/*
			 * Page 2
			 */
			mutablePattern = false;
			idType = Long.class;
			autoIncrement = false;
			autoUUID = false;
			withoutRowId = false;
			javaFile = null;
			entitySource = null;
		}

		private void initializeSource() {
			Builder b = JavaSource.Builder.newClass(packageName, className);
			String param = String.format("<%s>", idType.getSimpleName());
			b.addImport(PrimaryKey.class.getName());
			FieldSource.Builder idFieldBuilder = FieldSource.Builder.newField(idType.getSimpleName(), "id")
					.addAnnotation(AnnotationSource.Builder.newAnnotation(1, "@PrimaryKey").build());
			if (mutablePattern) {
				b.addInterface(MutableEntity.class.getSimpleName() + param);
				b.addImport(MutableEntity.class.getName());
				// @formatter:off
				b.addMethod(MethodSource.Builder.newMethod("setId")
					.addAnnotation(AnnotationSource.Builder.newAnnotation(1, "@Override").build())
					.addParameter(idType.getSimpleName() + " id")
					.addStatement("this.id = id")
					.setPublic()
					.build()
				);
				// @formatter:on
			} else {
				b.addInterface(Entity.class.getSimpleName() + param);
				b.addImport(Entity.class.getName());
				idFieldBuilder.setFinal();
			}
			idFieldBuilder.setPrivate();
			// @formatter:off
			b.addMethod(MethodSource.Builder
				.newMethod(idType.getSimpleName(), "getId")
				.addAnnotation(AnnotationSource.Builder.newAnnotation(1, "@Override").build())
				.addStatement("return id")
				.setPublic()
				.build()
			);
			// @formatter:on
			if (withoutRowId) {
				b.addImport(Table.class.getName());
				// @formatter:off
				b.addAnnotation(
					AnnotationSource.Builder
					.newAnnotation(0, "@EntityDefinition")
					.addValue("withoutRowID = true")
					.build());
				// @formatter:on
			}
			if (autoIncrement) {
//				b.add
			}
			if (autoUUID) {
//				b.add
			}
			FieldSource idField = idFieldBuilder.build();
			b.addField(idField);
			JavaSource src = b.build();
			try {
				entitySource = new EntitySource(javaFile, src);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String toString() {
			return "Model [packageName=" + packageName + ", className=" + className + ", isAbstract=" + isAbstract
					+ ", mutablePattern=" + mutablePattern + ", idType=" + idType + ", autoIncrement=" + autoIncrement
					+ ", withoutRowId=" + withoutRowId + "]";
		}

	}

	private final Model model = new Model();

	/*
	 * Page 2) Class Configuration
	 */

	/* @formatter:off
	public final boolean newEntity(Diagram diagram) throws IOException {
		DiagramController controller = guiController.getDiagramController();
		
		
		JTextField tfClassName = new JTextField();
		JComboBox<String> cbPackageName = new JComboBox<>(
				classSelectorDialog.getPackageNames(controller.getSourcesDirectory()));
		cbPackageName.setEditable(true);
//		JButton btOk = new JButton(D.getLabel(Key.BUTTON_SAVE));
//		btOk.addActionListener(this);
//		btOk.setActionCommand(MESSAGE_OK);
//		JButton btCancel = new JButton(D.getLabel(Key.BUTTON_CANCEL));
//		btCancel.addActionListener(this);
//		btCancel.setActionCommand(MESSAGE_CANCEL);
//		JPanel pnButtons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
//		pnButtons.add(btCancel);
//		pnButtons.add(btOk);
		JPanel pnEditor = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.insets = UIUtils.createDefaultMargin();
		c.fill = GridBagConstraints.HORIZONTAL;
		pnEditor.add(lbPackageName, c);
		c.gridx++;
		pnEditor.add(cbPackageName, c);
		c.gridx = 0;
		c.gridy++;
		pnEditor.add(lbClassName, c);
		c.gridx++;
		pnEditor.add(tfClassName, c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		pnEditor.add(initializeButtons(), c);

		String title = D.getLabel(Key.DIALOG_TITLE_ADD_ENTITY);
		currentDialog = UIUtils.initializeJDialog(guiController.getFrame(), title, pnEditor, true);
		answer = MESSAGE_CANCEL;
		currentDialog.setVisible(true);
		if (answer.equals(MESSAGE_OK)) {
			String packageName = cbPackageName.getSelectedItem().toString();
			String className = tfClassName.getText();
			File javaFile = DevToolsFiles.getFileName(controller.getSourcesDirectory(), packageName, className);
			JavaSource javaSource = JavaSource.Builder.newClass(packageName, className).build();
			EntitySource es = new EntitySource(javaFile, javaSource);
			es.persist();
			DiagramEntity de = es;
			controller.putDiagramEntity(es.getName(), de);
			diagram.addEntity(es.getName(), de);
			return true;
		} else {
			return false;
		}
	}
	@formatter:on */

	private final WizardUI wizardUI;

	private Diagram diagram;
//	private DiagramView diagramView;

	public NewEntityWizard(DevToolsGUIController guiController) {
		super(() -> D.getLabel(D.WIZARD_TITLE_NEW_ENTITY));
		this.guiController = guiController;
		/*
		 * Page 1: Class Definition
		 */
		lbClassName = new JLabel();
		lbPackageName = new JLabel();
		cbPackageName = new JComboBox<>();
		cbPackageName.setEditable(true);
		tfClassName = new JTextField();
		ckAbstract = new JCheckBox("abstract");
		final JPanel pnEditorClassDefinition = new JPanel(new GridBagLayout());
		GridBagConstraints c = initGridBagConstraints();
		addGridBagLine(pnEditorClassDefinition, c, lbPackageName, cbPackageName);
		addGridBagLine(pnEditorClassDefinition, c, lbClassName, tfClassName);
		addGridBagLine(pnEditorClassDefinition, c, ckAbstract);
		finishGridBagForm(pnEditorClassDefinition, c, 2);
		addPage(pnEditorClassDefinition, () -> D.getLabel(D.WIZARD_TITLE_NEW_ENTITY_PACKAGE_AND_NAME), icon,
				(dialog) -> {
					Object selectedPackageItem = cbPackageName.getSelectedItem();
					String packageName = null;
					if (Objects.isNull(selectedPackageItem)
							|| (packageName = selectedPackageItem.toString().trim()).isEmpty()) {
						guiController.showErrorMessage(dialog, D.ERROR_MSG_PACKAGE_NAME_EMPTY);
						return false;
//						
					}
					System.err.println(packageName);
					String className = tfClassName.getText();
					if (className.trim().isEmpty()) {
						guiController.showErrorMessage(dialog, D.ERROR_MSG_CLASS_NAME_EMPTY);
						return false;
					}
					model.fullClassName = packageName.concat(".").concat(className);
					model.javaFile = ProjectSource.getJavaFile(
							guiController.getSourcesDirectory(), model.fullClassName);
					if (model.javaFile.exists()) {
						Answer answer = guiController.showYesNoDialog(dialog, D.ERROR_MSG_CLASS_ALREADY_EXISTS);
						if (answer == Answer.NO) {
							return false;
						}
					}
					this.model.packageName = packageName;
					this.model.className = className;
					this.model.isAbstract = ckAbstract.isSelected();
					return true;
				});
		/*
		 * Page 2: Class Configuration
		 */
		lbPattern = new JLabel();
		rbMutable = new JRadioButton();
		rbImmutable = new JRadioButton();
		ButtonGroup bgPattern = new ButtonGroup();
		bgPattern.add(rbMutable);
		bgPattern.add(rbImmutable);
		JPanel pnPatternRadios = new JPanel(new FlowLayout(FlowLayout.LEADING));
		pnPatternRadios.add(rbImmutable);
		pnPatternRadios.add(rbMutable);
		lbId = new JLabel();
		rbLong = new JRadioButton("INTEGER");
		rbString = new JRadioButton("TEXT");
		ButtonGroup bgId = new ButtonGroup();
		bgId.add(rbLong);
		bgId.add(rbString);
		JPanel pnIdRadios = new JPanel(new FlowLayout(FlowLayout.LEADING));
		pnIdRadios.add(rbLong);
		pnIdRadios.add(rbString);
		ckAutoIncrement = new JCheckBox("AUTOINCREMENT");
		ckAutoUUID = new JCheckBox("AUTO-UUID");
		ActionListener alId = e -> {
			if (e.getSource() == rbLong) {
				ckAutoIncrement.setEnabled(true);
				ckAutoUUID.setEnabled(false);
				ckAutoUUID.setSelected(false);
			} else {
				ckAutoIncrement.setEnabled(false);
				ckAutoIncrement.setSelected(false);
				ckAutoUUID.setEnabled(true);
			}
		};
		rbLong.addActionListener(alId);
		rbString.addActionListener(alId);
		ckWithoutRowId = new JCheckBox("WITHOUT ROWID");
		final JPanel pnEditorClassConfiguration = new JPanel(new GridBagLayout());
		c = initGridBagConstraints();
		addGridBagLine(pnEditorClassConfiguration, c, lbPattern, pnPatternRadios);
		addGridBagLine(pnEditorClassConfiguration, c, lbId, pnIdRadios);
		addGridBagLine(pnEditorClassConfiguration, c, ckAutoIncrement);
		addGridBagLine(pnEditorClassConfiguration, c, ckWithoutRowId);
		finishGridBagForm(pnEditorClassConfiguration, c, 2);

//		pn.setBackground(Color.GREEN);
		addPage(pnEditorClassConfiguration, () -> D.getLabel(D.WIZARD_TITLE_NEW_ENTITY_PATTERN_AND_OPTIONS), icon,
				(d) -> {
					if (rbImmutable.isSelected()) {
						model.mutablePattern = false;
					} else {
						model.mutablePattern = true;
					}
					if (rbLong.isSelected()) {
						model.idType = Long.class;
					} else {
						model.idType = String.class;
					}
					model.autoIncrement = ckAutoIncrement.isSelected();
					model.withoutRowId = ckWithoutRowId.isSelected();
					model.autoUUID = ckAutoUUID.isSelected();
					model.initializeSource();
					return true;
				});
//		pn = new JPanel();
//		pn.setBackground(Color.RED);
//		addPage(pn, () -> D.getLabel(D.Key.WIZARD_TITLE_NEW_ENTITY_SUPER_CLASS), icon,
//				(d) -> true);
//		pn = new JPanel();
//		pn.setBackground(Color.BLUE);
//		addPage(pn, () -> D.getLabel(D.Key.WIZARD_TITLE_NEW_ENTITY_FIELDS), icon,
//				(d) -> true);

		wizardUI = WizardUI.getWizardUI(this, guiController.getFrame());
	}

	public final void showNewEntityWizard(Diagram diagram) {
		this.diagram = diagram;
//		this.diagramView = diagramView;
		WizardUI.runWizardUI(wizardUI);
	}

//	@Override
//	public boolean isPageRequired(int n) {
//		if (n == 0)
//			return false;
//		return true;
//	}
//
//	@Override
//	public int getPagesToSkipFrom(int pageIndex) {
//		if (pageIndex == 0)
//			return 2;
//		return 1;
//	}

//	@Override
//	public Trail[] getTrails() {
//		return super.getTrails();
//	}
//
//	@Override
//	public int getTrailChoice(int splitPage) {
//		return super.getTrailChoice(splitPage);
//	}

	@Override
	public void finish(boolean[] skipped) {
		diagram.addEntity(model.fullClassName, model.entitySource);
		guiController.getDiagramController().putDiagramEntity(model.fullClassName, model.entitySource);
		try {
			model.entitySource.persist();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cancel() {
		System.err.println("### Cancelled");
	}

	@Override
	public void loadLabels() {
		lbClassName.setText(D.getLabel(D.CLASS_NAME));
		lbPackageName.setText(D.getLabel(D.PACKAGE_NAME));
		lbPattern.setText(D.getLabel(D.ENTITY_PATTERN));
		rbMutable.setText(D.getLabel(D.ENTITY_PATTERN_MUTABLE));
		rbImmutable.setText(D.getLabel(D.ENTITY_PATTERN_IMMUTABLE));
		lbId.setText(D.getLabel(D.ENTITY_ID));
	}
}
