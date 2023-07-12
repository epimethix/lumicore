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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.github.epimethix.lumicore.common.swing.Document;
import com.github.epimethix.lumicore.common.swing.MutableFileDocument;
import com.github.epimethix.lumicore.common.ui.labels.displayer.IgnoreLabels;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayer;
import com.github.epimethix.lumicore.devtools.fs.DiagramData;
import com.github.epimethix.lumicore.devtools.gui.D;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.Diagram;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramEntity;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramEntityConfiguration;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramEntityConstructor;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramEntityField;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramEntityMethod;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.DiagramEntityRectangle;
import com.github.epimethix.lumicore.devtools.gui.diagram.model.EntitySource;
import com.github.epimethix.lumicore.sourceutil.ProjectSource;
import com.github.epimethix.lumicore.swing.util.LayoutUtils;

@SuppressWarnings("serial")
public class DiagramView extends JPanel
		implements MutableFileDocument, ActionListener, MouseListener, MouseMotionListener, LabelsDisplayer {

	private class DiagramContextMenu implements LabelsDisplayer, ActionListener {
		private final JMenuItem miAddField;
		private final JMenuItem miAddConstructor;
		private final JMenuItem miAddMethod;
		private final JMenuItem miEntityInfo;
		private final JMenuItem miViewSource;
		private final JPopupMenu popupMenu;
		@IgnoreLabels
		private DiagramEntity focusedEntity;

		private DiagramContextMenu() {
			miAddField = new JMenuItem();
			miAddField.addActionListener(this);
			miAddConstructor = new JMenuItem();
			miAddConstructor.addActionListener(this);
			miAddMethod = new JMenuItem();
			miAddMethod.addActionListener(this);
			miEntityInfo = new JMenuItem();
			miEntityInfo.addActionListener(this);
			miViewSource = new JMenuItem();
			miViewSource.addActionListener(this);
			popupMenu = new JPopupMenu();
			popupMenu.add(miAddField);
			popupMenu.add(miAddConstructor);
			popupMenu.add(miAddMethod);
			popupMenu.add(miViewSource);
			popupMenu.add(miEntityInfo);
		}

		private void showMenu(Component parent, int x, int y, DiagramEntity focusedEntity) {
			this.focusedEntity = focusedEntity;
			popupMenu.show(parent, x, y);
		}

		@Override
		public void loadLabels() {
			miAddField.setText(D.getLabel(D.MENU_ADD_FIELD));
			miAddConstructor.setText(D.getLabel(D.MENU_ADD_CONSTRUCTOR));
			miAddMethod.setText(D.getLabel(D.MENU_ADD_METHOD));
			miEntityInfo.setText(D.getLabel(D.MENU_ENTITY_INFO));
			miViewSource.setText(D.getLabel(D.MENU_VIEW_SOURCE));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == miAddField) {
				diagramController.addField(focusedEntity);
				canvas.repaint();
			} else if (e.getSource() == miAddConstructor) {
				diagramController.addConstructor(focusedEntity);
			} else if (e.getSource() == miAddMethod) {
				diagramController.addMethod(focusedEntity);
			} else if (e.getSource() == miViewSource) {
				if (focusedEntity instanceof EntitySource) {
					File source = ProjectSource.getJavaFile(
							diagramController.getGUIController().getSourcesDirectory(),
							focusedEntity.getName());
					diagramController.viewSource(source, ((EntitySource) focusedEntity).getCode());
				}
			} else if (e.getSource() == miEntityInfo) {
				Map<String, DiagramData> data = new HashMap<>();
				data.put(focusedEntity.getName(), diagram.getDiagramData(focusedEntity.getName()));
				diagramController.showProperties(data);
				canvas.repaint();
			}
		}
	}

	private final static Color BG_COLOR = Color.decode("#e6ffe6");
	private final static Color ENTITY_COLOR = Color.decode("#ffe6ff");
//	private final static Color INTERFACE_COLOR = Color.decode("#e6f7ff");
//	private final static Color HIGHLIGHTED_COLOR = Color.decode("#80d4ff");
//	private final static Color HIGHLIGHTED_COLOR = Color.decode("#ff99ff");
	private final static Color HIGHLIGHTED_COLOR = Color.decode("#ffb3ff");

	private final class Canvas extends JComponent {
		private DiagramEntity focusedEntity;
		private DiagramEntityRectangle focusedEntityRectangle;
		private final Point cursorPosition = new Point();
		private final Stroke highlightedStroke = new BasicStroke(1.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
				1.0F, new float[] { 5.0F, 5.0F }, 1.0F);

		private Dimension preferredSize = new Dimension(0, 0);

		private Point autoLocation = new Point();
		private int autoLocationNextColumn;

		private Rectangle selectionRectangle;

		private List<DiagramEntityRectangle> selectedRectangles = new ArrayList<>();

		private Font defaultFont;
		private Font boldFont;

		private double scale = 1.0;
		private double initialScale = scale;
		private int initialFontSize;

		Point maxAll = new Point(0, 0);

		public Canvas() {}

		private void max(Point a) {
			maxAll.setLocation(Math.max(a.x, maxAll.x), Math.max(a.y, maxAll.y));
		}

		@Override
		protected void paintComponent(Graphics g) {
//		diagram.paint(g);
//			System.err.println("Paint Canvas");
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
//			AffineTransform initialTransform = g2.getTransform();
//			g2.scale(scale, scale);
			Color initialColor = g.getColor();
			Font initialFont = g.getFont();
			if (Objects.isNull(defaultFont) || initialFontSize != diagram.getFontSize() || initialScale != scale) {
				initialFontSize = diagram.getFontSize();
				initialScale = scale;
				defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, (int) (diagram.getFontSize() * scale));
				boldFont = new Font(Font.MONOSPACED, Font.BOLD, (int) (diagram.getFontSize() * scale));
			}
			g.setColor(BG_COLOR);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(initialColor);
//			int locY = 25;
			autoLocation.setLocation(LayoutUtils.MEDIUM_MARGIN, LayoutUtils.MEDIUM_MARGIN);
			autoLocationNextColumn = 0;
			maxAll.setLocation(0, 0);
			/*
			 * Draw entities: base layer
			 */
			for (String key : diagram.keySet()) {
				DiagramEntity diagramEntity = diagram.getDiagramEntity(key);
				if (diagramEntity == focusedEntity) {
					continue;
				}
				DiagramEntityRectangle der = diagram.getRectangle(key);
				if (selectedRectangles.contains(der)) {
					continue;
				}
				Point max = paintEntity(g, diagramEntity, ENTITY_COLOR, false);
				max(max);
				g.setColor(initialColor);
				g.setFont(initialFont);
			}
			/*
			 * Draw entities: selected layer above base layer
			 */
			for (String key : diagram.keySet()) {
				DiagramEntity diagramEntity = diagram.getDiagramEntity(key);
				if (diagramEntity == focusedEntity) {
					continue;
				}
				DiagramEntityRectangle der = diagram.getRectangle(key);
				if (selectedRectangles.contains(der)) {
					Point max = paintEntity(g, diagramEntity, ENTITY_COLOR, true);
					max(max);
					g.setColor(initialColor);
					g.setFont(initialFont);
				}
			}
			/*
			 * Draw entities: focused entity on top
			 */
			if (Objects.nonNull(focusedEntity)) {
				Point max = paintEntity(g, focusedEntity, ENTITY_COLOR,
						selectedRectangles.contains(diagram.getRectangle(focusedEntity.getName())));
				max(max);
				g.setColor(initialColor);
				g.setFont(initialFont);
			}
			if (preferredSize.width == 0) {
				shrink();
			}
			/*
			 * Draw selection rectangle on top
			 */
//			g2.setTransform(initialTransform);
			if (Objects.nonNull(selectionRectangle)) {
				Stroke initialStroke = g2.getStroke();
				g2.setStroke(highlightedStroke);
//				g2.setColor(HIGHLIGHTED_COLOR);
				g2.draw(selectionRectangle);
//				g.setColor(initialColor);
				g2.setStroke(initialStroke);
			}
		}

		private Point paintEntity(Graphics g, DiagramEntity diagramEntity, Color bgColor, boolean isEntitySelected) {
			FontMetrics FONT_METRICTS = getFontMetrics(defaultFont);
			int lineHeight = (int) FONT_METRICTS.getStringBounds("Test", g).getHeight(); //$NON-NLS-1$
			int margin = LayoutUtils.DEFAULT_MARGIN;
			DiagramEntityRectangle diagramEntityRectangle = diagram.getRectangle(diagramEntity.getName());
			Color initialColor = g.getColor();
			boolean autoLocate = diagramEntityRectangle.getX() == diagramEntityRectangle.getY()
					&& diagramEntityRectangle.getX() == Integer.MIN_VALUE;
			if (autoLocate) {
				if (autoLocation.y > 800) {
					autoLocation.x = autoLocationNextColumn;
					autoLocation.y = LayoutUtils.MEDIUM_MARGIN;
				}
				diagramEntityRectangle.setLocation(autoLocation.x, autoLocation.y);
			}
			int width = 0;
			int height = 0;
			DiagramEntityConfiguration conf = diagram.getDiagramData(diagramEntity.getName()).getConfiguration();
			List<DiagramEntityField> staticFields;
			if (conf.isShowStaticFields()) {
				staticFields = diagramEntity.getStaticFields();
			} else {
				staticFields = Collections.emptyList();
			}
			List<DiagramEntityField> instanceFields;
			if (conf.isShowInstanceFields()) {
				instanceFields = diagramEntity.getInstanceFields();
			} else {
				instanceFields = Collections.emptyList();
			}
			List<DiagramEntityConstructor> constructors;
			if (conf.isShowConstructors()) {
				constructors = diagramEntity.getConstructors();
			} else {
				constructors = Collections.emptyList();
			}
			List<DiagramEntityMethod> staticMethods;
			if (conf.isShowStaticMethods()) {
				staticMethods = diagramEntity.getStaticMethods();
			} else {
				staticMethods = Collections.emptyList();
			}
			List<DiagramEntityMethod> instanceMethods;
			if (conf.isShowInstanceMethods()) {
				instanceMethods = diagramEntity.getInstanceMethods();
			} else {
				instanceMethods = Collections.emptyList();
			}
			int nTotal = staticFields.size() + instanceFields.size() + constructors.size() + staticMethods.size()
					+ instanceMethods.size();
//			boolean isEntitySelected = false;
//			if (selectedRectangles.contains(diagramEntityRectangle)) {
//				isEntitySelected = true;
//			}
			if (!isEntitySelected && Objects.nonNull(selectionRectangle)) {
				if (selectionRectangle.contains(diagramEntityRectangle.getRectangle())) {
					isEntitySelected = true;
				}
			}
			for (int i = 0, j = 0; i < 2 && j < 3; i++, j++) {
//				if (i == 0 && !diagramEntityRectangle.isSizeOld()) {
//					continue;
//				}
				Graphics2D g2d = (Graphics2D) g;
				if (i == 1) {
					if (isEntitySelected) {
						g.setColor(HIGHLIGHTED_COLOR);
					} else {
						g.setColor(bgColor);
					}
					g2d.fill(diagramEntityRectangle.getRectangle());
					g.setColor(initialColor);
				}
				int x = diagramEntityRectangle.getX() + margin;
				int y = diagramEntityRectangle.getY() + margin + lineHeight;
				int yTitleLine = -1;
				int yFieldsLine = -1;
				int yConstructorsLine = -1;
				int xEditButton;
				int widthEditButton;
				int heightEditButton;

				Font initialFont = defaultFont;
//				Font boldFont = new Font(initialFont.getFontName(), Font.BOLD, initialFont.getSize());
				g.setFont(boldFont);
				String title = diagramEntity.getSimpleName();
				if (diagramEntity.isAbstract() || diagramEntity.isInterface()) {
					title = String.format("<<%s>>", title);
				}
				if (i == 1) {
					g.drawString(title, x, y);
				}
				Rectangle2D testWidth = getFontMetrics(boldFont).getStringBounds(title, g);

				width = (int) testWidth.getWidth() + 2 * margin;
				g.setFont(initialFont);
				xEditButton = diagramEntityRectangle.getX() + width;
				testWidth = FONT_METRICTS.getStringBounds("...", g); //$NON-NLS-1$
				widthEditButton = (int) testWidth.getWidth() + 2 * margin;
				width += widthEditButton;
				y += margin;
				yTitleLine = y;
				height = y - diagramEntityRectangle.getY();
				heightEditButton = height;
//				Color highlightedColor = Color.red;
				if (nTotal == 0) {
					if (i == 0 || !diagramEntityRectangle.sizeEquals(width, height)) {
						diagramEntityRectangle.setSize(width, height);
						diagramEntityRectangle.setEditButtonBounds(xEditButton, diagramEntityRectangle.getY(),
								widthEditButton, heightEditButton);
						i = 0;
						continue;
					}
				} else {
//					y += margin;
					if (staticFields.size() > 0 || instanceFields.size() > 0) {
						for (DiagramEntityField def : staticFields) {
							y += margin + lineHeight;
							height = y - diagramEntityRectangle.getY() + margin;
							String label = def.toString();
							testWidth = FONT_METRICTS.getStringBounds(label, g);
							width = (int) Math.max(width, testWidth.getWidth() + 2 * margin);
							if (i == 1) {
								g.drawString(label, x, y);
								g.drawLine(x, y + 2, x + (int) testWidth.getWidth(), y + 2);
							}
						}
						for (DiagramEntityField def : instanceFields) {
							y += margin + lineHeight;
							height = y - diagramEntityRectangle.getY() + margin;
							String label = def.toString();
							testWidth = FONT_METRICTS.getStringBounds(label, g);
							width = (int) Math.max(width, testWidth.getWidth() + 2 * margin);
							if (i == 1) {
								g.drawString(label, x, y);
							}
						}
						y += margin;
						yFieldsLine = y;
					}
					if (constructors.size() > 0) {
						for (DiagramEntityConstructor dec : constructors) {
							y += margin + lineHeight;
							height = y - diagramEntityRectangle.getY() + margin;
							String label = dec.toString();
							testWidth = FONT_METRICTS.getStringBounds(label, g);
							width = (int) Math.max(width, testWidth.getWidth() + 2 * margin);
							if (i == 1) {
								g.drawString(label, x, y);
							}
						}
						y += margin;
						yConstructorsLine = y;
					}
					if (staticMethods.size() > 0 || instanceMethods.size() > 0) {
						for (DiagramEntityMethod dem : staticMethods) {
							y += margin + lineHeight;
							height = y - diagramEntityRectangle.getY() + margin;
							String label = dem.toString();
							testWidth = FONT_METRICTS.getStringBounds(label, g);
							width = (int) Math.max(width, testWidth.getWidth() + 2 * margin);
							if (i == 1) {
								g.drawString(label, x, y);
								g.drawLine(x, y + 2, x + (int) testWidth.getWidth(), y + 2);
							}
						}
						for (DiagramEntityMethod dem : instanceMethods) {
							y += margin + lineHeight;
							height = y - diagramEntityRectangle.getY() + margin;
							String label = dem.toString();
							testWidth = FONT_METRICTS.getStringBounds(label, g);
							width = (int) Math.max(width, testWidth.getWidth() + 2 * margin);
							if (i == 1) {
								g.drawString(label, x, y);
							}
						}
					}
//					height += margin;
					xEditButton = Math.max(xEditButton, diagramEntityRectangle.getX() + width - widthEditButton);
					if (i == 0 || !diagramEntityRectangle.sizeEquals(width, height)) {
						diagramEntityRectangle.setSize(width, height);
						diagramEntityRectangle.setEditButtonBounds(xEditButton, diagramEntityRectangle.getY(),
								widthEditButton, heightEditButton);
						i = 0;
						continue;
					}
					if (i == 1) {
//						if (diagramEntity == focusedEntity) {
//							g.setColor(highlightedColor);
//						}
						g2d.drawLine(diagramEntityRectangle.getX(), yTitleLine, diagramEntityRectangle.getX() + width,
								yTitleLine);
						if (yFieldsLine != -1) {
							g2d.drawLine(diagramEntityRectangle.getX(), yFieldsLine,
									diagramEntityRectangle.getX() + width, yFieldsLine);
						}
						if (yConstructorsLine != -1) {
							g2d.drawLine(diagramEntityRectangle.getX(), yConstructorsLine,
									diagramEntityRectangle.getX() + width, yConstructorsLine);
						}
					}
				}

				if (i == 1) {
					g2d.drawLine(xEditButton, diagramEntityRectangle.getY(), xEditButton,
							diagramEntityRectangle.getY() + heightEditButton);
					xEditButton += margin;
					g2d.drawString("...", xEditButton, yTitleLine - margin);
					g2d.draw(diagramEntityRectangle.getRectangle());
				}
			}
			if (autoLocate) {
				autoLocation.y += height + LayoutUtils.MEDIUM_MARGIN;
				autoLocationNextColumn = Math.max(autoLocationNextColumn,
						diagramEntityRectangle.getX() + diagramEntityRectangle.getWidth() + LayoutUtils.MEDIUM_MARGIN);
			}
			return new Point(diagramEntityRectangle.getX() + diagramEntityRectangle.getWidth() + LayoutUtils.MEDIUM_MARGIN,
					diagramEntityRectangle.getY() + diagramEntityRectangle.getHeight() + LayoutUtils.MEDIUM_MARGIN);
		}

		@Override
		public Dimension getPreferredSize() {
			return preferredSize;
		}

		public void shrink() {
			if (preferredSize.width != maxAll.x || preferredSize.height != maxAll.y) {
				preferredSize.setSize(maxAll.x, maxAll.y);
				revalidate();
			}
		}

		@Override
		public JToolTip createToolTip() {
			System.err.println("createToolTip");
			return super.createToolTip();
		}

	} // End of class Canvas

	@IgnoreLabels
	private final Diagram diagram;
	@IgnoreLabels
	private final Canvas canvas;
	private final JButton btSave;
	private final JButton btManageEntities;
	private final JButton btNewEntity;
	private final JButton btAddEntity;
	private final JButton btFontSize;
	private final JButton btProperties;
	private final JButton btRelations;
	@IgnoreLabels
	private final DiagramController diagramController;
	private final Point mousePressed;
	private final DiagramContextMenu contextMenu;
	private final JPanel pnQuickInfo;
	private final RSyntaxTextArea taCode;

	public DiagramView(DiagramController diagramController, Diagram diagram) {
		super(new GridBagLayout());
		this.diagramController = diagramController;
		this.diagram = diagram;
		this.mousePressed = new Point();
		this.canvas = new Canvas();
		this.canvas.addMouseMotionListener(this);
		this.canvas.addMouseListener(this);
		taCode = new RSyntaxTextArea(20, 60);
		taCode.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		taCode.setCodeFoldingEnabled(true);
//		taCode.setText(diagram.getCode());
		RTextScrollPane rTextScrollPane = new RTextScrollPane(taCode);
		pnQuickInfo = new JPanel(new GridLayout(1, 1));
		pnQuickInfo.add(rTextScrollPane);
//		canvas.add(pnQuickInfo);
		pnQuickInfo.setBounds(50, 50, 800, 200);
		contextMenu = new DiagramContextMenu();
		btSave = new JButton();
		btSave.addActionListener(this);
		btManageEntities = new JButton();
		btManageEntities.addActionListener(this);
		btAddEntity = new JButton();
		btAddEntity.addActionListener(this);
		btNewEntity = new JButton();
		btNewEntity.addActionListener(this);
		btFontSize = new JButton();
		btFontSize.addActionListener(this);
		btProperties = new JButton();
		btProperties.addActionListener(this);
		btRelations = new JButton();
		btRelations.addActionListener(this);
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(btSave);
		toolBar.add(btManageEntities);
		toolBar.add(btNewEntity);
		toolBar.add(btAddEntity);
		toolBar.add(btFontSize);
		toolBar.add(btProperties);
		toolBar.add(btRelations);
		toolBar.add(new JPanel());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(toolBar, c);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = c.weighty = 1.0;
		c.gridy++;
		add(LayoutUtils.initScrollPane(canvas), c);
	}

	public void exportAsImage() throws IOException {
		Optional<File> result = diagramController.getGUIController().showSaveDialog(".png");
		if (result.isEmpty()) {
			return;
		}
		File selectedFile = result.get();
		BufferedImage image = new BufferedImage(canvas.preferredSize.width, canvas.preferredSize.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics g = image.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.setColor(Color.black);
		for (String key : diagram.keySet()) {
			DiagramEntity de = diagram.getDiagramEntity(key);
			canvas.paintEntity(g, de, Color.white, false);
		}
		g.dispose();
		ImageIO.write(image, "png", selectedFile);

	}

	/*
	 * LabelsDisplayer
	 */

	@Override
	public void loadLabels() {
		btSave.setText(D.getLabel(D.BUTTON_SAVE));
		btManageEntities.setText(D.getLabel(D.BUTTON_MANAGE));
		btNewEntity.setText(D.getLabel(D.BUTTON_NEW_ENTITY));
		btAddEntity.setText(D.getLabel(D.BUTTON_ADD_ENTITY));
		btFontSize.setText(D.getLabel(D.FONT_SIZE));
		btProperties.setText(D.getLabel(D.BUTTON_PROPERTIES));
		btRelations.setText(D.getLabel(D.BUTTON_RELATIONS));
	}

	/*
	 * Document
	 */

	@Override
	public boolean hasChanges() {
		return diagram.hasChanges();
	}

	@Override
	public boolean persist() {
		try {
			diagram.persist();
			diagramController.getGUIController().showStatus(D.getLabel(D.STATUS_SAVED));
			return true;
		} catch (IOException e) {
			diagramController.getGUIController().showStatus(D.getLabel(D.STATUS_SAVE_ERROR));
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean persist(File file) {
		try {
			diagram.persist(file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public File getDocumentFile() {
		return diagram.getFile();
	}

	@Override
	public String getDocumentName() {
		return diagram.getName();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btSave) {
			persist();
		} else if (e.getSource() == btManageEntities) {
			diagramController.showClassSelectorDialog(diagram);
			repaint();
		} else if (e.getSource() == btNewEntity) {
			diagramController.newEntity(diagram);
			canvas.repaint();
		} else if (e.getSource() == btAddEntity) {
			try {
				diagramController.addEntity(diagram);
				canvas.repaint();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if (e.getSource() == btFontSize) {
			diagramController.showSetFontSize(diagram);
			canvas.repaint();
			SwingUtilities.invokeLater(() -> {
				canvas.shrink();
			});
		} else if (e.getSource() == btProperties) {
			Map<String, DiagramData> data = new HashMap<>();
			Set<String> keySet = diagram.keySet();
			for (String key : keySet) {
				data.put(key, diagram.getDiagramData(key));
			}
			diagramController.showProperties(data);
			canvas.repaint();
		} else if (e.getSource() == btRelations) {
			if (diagramController.showRelations(diagram)) {
				canvas.repaint();
			}
		}
	}

//	@Override
//	public void mouseWheelMoved(MouseWheelEvent e) {
//		if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
//			double increment = 0.02d;
//			if (e.getWheelRotation() < 0) {
//				canvas.scale += increment;
//			} else {
//				canvas.scale -= increment;
//			}
//			canvas.repaint();
//		}
//	}

	@Override
	public void mouseDragged(MouseEvent e) {
		canvas.cursorPosition.setLocation(e.getX(), e.getY());
		if (Objects.nonNull(canvas.selectionRectangle)) {
			int width = Math.abs(e.getX() - mousePressed.x);
			int height = Math.abs(e.getY() - mousePressed.y);
			int x = Math.min(mousePressed.x, e.getX());
			int y = Math.min(mousePressed.y, e.getY());
			canvas.selectionRectangle.setBounds(x, y, width, height);
			canvas.repaint();
		} else if (canvas.selectedRectangles.size() > 0) {
			int deltaX = e.getX() - mousePressed.x;
			int deltaY = e.getY() - mousePressed.y;
			for (DiagramEntityRectangle movingEntityRectangle : canvas.selectedRectangles) {
				movingEntityRectangle.setDeltaLocation(deltaX, deltaY);
			}
			canvas.repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		canvas.cursorPosition.setLocation(e.getX(), e.getY());
		if (Objects.nonNull(canvas.focusedEntity)) {
			if (!canvas.focusedEntityRectangle.contains(canvas.cursorPosition)) {
				canvas.focusedEntity = null;
				canvas.focusedEntityRectangle = null;
				canvas.repaint();
			}
		} else {
			List<String> keys = new ArrayList<>(diagram.keySet());
			Collections.reverse(keys);
			for (String key : keys) {
				DiagramEntityRectangle der = diagram.getRectangle(key);
				if (der.contains(canvas.cursorPosition)) {
					DiagramEntity de = diagram.getDiagramEntity(key);
					canvas.focusedEntity = de;
					canvas.focusedEntityRectangle = der;
					canvas.repaint();
					break;
				}
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (Objects.nonNull(canvas.focusedEntityRectangle)) {
				if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
					if (canvas.selectedRectangles.contains(canvas.focusedEntityRectangle)) {
						canvas.selectedRectangles.remove(canvas.focusedEntityRectangle);
					} else {
						canvas.selectedRectangles.add(canvas.focusedEntityRectangle);
					}
				} else if (canvas.focusedEntityRectangle.editButtonContains(e.getX(), e.getY())) {
					contextMenu.showMenu(canvas, e.getX(), e.getY(), canvas.focusedEntity);
				} else {
					canvas.selectedRectangles.clear();
					canvas.selectedRectangles.add(canvas.focusedEntityRectangle);
				}
				canvas.repaint();
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mousePressed.setLocation(e.getX(), e.getY());
		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
			if (Objects.nonNull(canvas.focusedEntity)) {
				if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0) {
					if (!canvas.selectedRectangles.contains(canvas.focusedEntityRectangle)) {
						canvas.selectedRectangles.clear();
						canvas.selectedRectangles.add(canvas.focusedEntityRectangle);
					}
				}
				for (DiagramEntityRectangle der : canvas.selectedRectangles) {
					der.setInitialLocation();
				}
			} else {
				if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0) {
					canvas.selectedRectangles.clear();
				}
				canvas.selectionRectangle = new Rectangle(e.getX(), e.getY(), 0, 0);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (Objects.nonNull(canvas.selectionRectangle)) {
			/*
			 * evaluate selection
			 */
			for (String key : diagram.getClassNames()) {
				DiagramEntityRectangle der = diagram.getRectangle(key);
				if (canvas.selectionRectangle.contains(der.getRectangle())) {
					if (canvas.selectedRectangles.contains(der)) {
						if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
							canvas.selectedRectangles.remove(der);
						}
					} else {
						canvas.selectedRectangles.add(der);
					}
				}
			}
			canvas.selectionRectangle = null;
		} else if (canvas.selectedRectangles.size() > 0) {
			/*
			 * check if any rectangles have negative coordinates
			 */
			int minX = 0;
			int minY = 0;
			for (DiagramEntityRectangle der : canvas.selectedRectangles) {
				minX = Math.min(minX, der.getX());
				minY = Math.min(minY, der.getY());
			}
			if (minX < 0 || minY < 0) {
				int incrementX = minX < 0 ? Math.abs(minX) + LayoutUtils.MEDIUM_MARGIN : 0;
				int incrementY = minY < 0 ? Math.abs(minY) + LayoutUtils.MEDIUM_MARGIN : 0;
				for (String key : diagram.keySet()) {
					DiagramEntityRectangle der = diagram.getRectangle(key);
					der.setLocation(der.getX() + incrementX, der.getY() + incrementY);
				}
			}
		}
		canvas.shrink();
		canvas.repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((diagram == null) ? 0 : diagram.hashCode());
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
		DiagramView other = (DiagramView) obj;
		if (diagram == null) {
			if (other.diagram != null)
				return false;
		} else if (!diagram.equals(other.diagram))
			return false;
		return true;
	}
}
