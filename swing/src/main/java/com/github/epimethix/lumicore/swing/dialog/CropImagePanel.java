/*
 * Copyright 2022 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.swing.dialog;

public class CropImagePanel { // extends ImagePanel implements MouseListener, MouseMotionListener, MouseWheelListener {
//	private static final long serialVersionUID = 1L;
//	private final Dimension targetSize;
//
//	private final static double scrollStep = 0.1;
//
//	private int cropX;
//	private int cropY;
//	private int cropWidth;
//	private int cropHeight;
//	private int maxX;
//	private int maxY;
//	private double cropScale;
//
//	private final Color shadowColor;
//
//	private Rectangle2D selection;
//
//	/*
//	 * ##----- ##----- ##----- ##-----
//	 */
//	private Rectangle2D cropShadowLeft;
//	/*
//	 * -----## -----## -----## -----##
//	 */
//	private Rectangle2D cropShadowRight;
//	/*
//	 * --###-- ------- ------- -------
//	 */
//	private Rectangle2D cropShadowTop;
//	/*
//	 * ------- ------- ------- --###--
//	 */
//	private Rectangle2D cropShadowBottom;
//
//	private Graphics2D g2;
//
//	private Color currentColor;
//	private Stroke currentStroke;
//
//	private boolean crop;
//
//	boolean selecting;
//
//	private File fSource;
//
//	public CropImagePanel(Dimension targetSize) {
//		this.targetSize = targetSize;
//		shadowColor = new Color(255, 222, 173, 90);
//		Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
//		setPreferredSize(new Dimension((int) (scr.width * 0.7), (int) (scr.height * 0.8)));
//		crop = true;
//		addMouseListener(this);
//		addMouseMotionListener(this);
//		addMouseWheelListener(this);
//		selection = new Rectangle2D.Double();
//		cropShadowTop = new Rectangle2D.Double();
//		cropShadowBottom = new Rectangle2D.Double();
//		cropShadowLeft = new Rectangle2D.Double();
//		cropShadowRight = new Rectangle2D.Double();
//	}
//
//	public void setfSource(File fSource) {
//		this.fSource = fSource;
//		setImage(fSource);
//	}
//
//	public void setImage(File f) {
//		super.setImage(f, false);
//		System.out.println(f.getName());
//		System.out.println(targetSize.height);
//		if (targetSize.width <= getImage().getWidth() && targetSize.height <= getImage().getHeight()) {
//			cropWidth = (int) (targetSize.width / getScale());
//			cropHeight = (int) (targetSize.height / getScale());
//			cropX = getWidth() / 2 - cropWidth / 2;
//			cropY = getHeight() / 2 - cropHeight / 2;
//			maxX = getImageX() + getImageWidth() - cropWidth;
//			maxY = getImageY() + getImageHeight() - cropHeight;
//			cropScale = 1.0;
//			refreshUI();
//
//		}
//		repaint();
//	}
//
//	private void refreshUI() {
//		if (cropX > maxX) {
//			cropX = maxX;
//		} else if (cropX < getImageX()) {
//			cropX = getImageX();
//		}
//		if (cropY > maxY) {
//			cropY = maxY;
//		} else if (cropY < getImageY()) {
//			cropY = getImageY();
//		}
//
//		selection.setFrame(cropX, cropY, cropWidth, cropHeight);
//		cropShadowLeft.setFrame(0, 0, cropX, getHeight());
//		cropShadowTop.setFrame(cropX, 0, cropWidth, cropY);
//		int srX = cropX + cropWidth;
//		cropShadowRight.setFrame(srX, 0, getWidth() - srX, getHeight());
//		int sbY = cropY + cropHeight;
//		cropShadowBottom.setFrame(cropX, sbY, cropWidth, getHeight() - sbY);
//	}
//
//	private void rescaleCrop(int x, int y) {
//		cropWidth = (int) ((targetSize.width * cropScale) / getScale());
//		cropHeight = (int) ((targetSize.height * cropScale) / getScale());
//		maxX = getImageX() + getImageWidth() - cropWidth;
//		maxY = getImageY() + getImageHeight() - cropHeight;
//		centerCrop(x, y);
//	}
//
//	private void centerCrop(int x, int y) {
//		cropX = x - cropWidth / 2;
//		cropY = y - cropHeight / 2;
//		refreshUI();
//		repaint();
//	}
//
//	public void dontCrop() {
//		crop = false;
//	}
//
//	public boolean isCrop() {
//		return crop;
//	}
//
//	public final void persistCrop(File targetFile, String formatName) throws IOException {
//		int x = (int) ((cropX - getImageX()) * getScale());
//		int y = (int) ((cropY - getImageY()) * getScale());
//		int w = (int) (cropWidth * getScale());
//		int h = (int) (cropHeight * getScale());
//
//		BufferedImage cropped = getImage().getSubimage(x, y, w, h);
//		Image scaled = cropped.getScaledInstance(targetSize.width, targetSize.height, BufferedImage.SCALE_SMOOTH);
//		BufferedImage scaled2 = new BufferedImage(targetSize.width, targetSize.height, BufferedImage.TYPE_INT_RGB);
//		Graphics g = scaled2.createGraphics();
//		g.drawImage(scaled, 0, 0, null);
//		ImageIO.write(scaled2, formatName, targetFile);
//	}
//
//	@Override
//	public void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		g2 = (Graphics2D) g;
//		currentColor = g2.getColor();
//		currentStroke = g2.getStroke();
//		g2.setColor(shadowColor);
//		g2.fill(cropShadowLeft);
//		g2.fill(cropShadowTop);
//		g2.fill(cropShadowRight);
//		g2.fill(cropShadowBottom);
//		g2.setStroke(new BasicStroke(3.0F));
//		g2.draw(selection);
//		g2.setColor(currentColor);
//		g2.setStroke(currentStroke);
//	}
//
//	@Override
//	public void mouseClicked(MouseEvent e) {
//		if (e.getSource() == this) {
//			selecting = !selecting;
//			centerCrop(e.getX(), e.getY());
//		}
//	}
//
//	@Override
//	public void mouseMoved(MouseEvent e) {
//		if (e.getSource() == this) {
//			if (selecting) {
//				centerCrop(e.getX(), e.getY());
//			}
//		}
//	}
//
//	@Override
//	public void mouseWheelMoved(MouseWheelEvent e) {
//		if (e.getSource() == this) {
//			if (selecting) {
//				if (e.getWheelRotation() < 0) {
//					if (cropScale > 0.2) {
//						cropScale -= scrollStep;
//						rescaleCrop(e.getX(), e.getY());
//					} else {
//						cropScale = 0.2;
//					}
//				} else if (e.getWheelRotation() > 0) {
//					boolean widthFits = ((int) ((targetSize.width * (cropScale + scrollStep))
//							/ getScale())) <= getImageWidth();
//					boolean heightFits = ((int) ((targetSize.height * (cropScale + scrollStep))
//							/ getScale())) <= getImageHeight();
//					if (widthFits && heightFits) {
//						cropScale += scrollStep;
//						rescaleCrop(e.getX(), e.getY());
//					}
//				}
//			}
//		}
//	}
//
//	@Override
//	public void mousePressed(MouseEvent e) {}
//
//	@Override
//	public void mouseReleased(MouseEvent e) {}
//
//	@Override
//	public void mouseEntered(MouseEvent e) {}
//
//	@Override
//	public void mouseExited(MouseEvent e) {}
//
//	@Override
//	public void mouseDragged(MouseEvent e) {}
}