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
package com.github.epimethix.lumicore.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImagePanel extends JPanel {
	private BufferedImage image;
	private int x, y, width, height;
	private double scale;
	
	private Dimension preferredSize;
	
	private final boolean fixedSize;

	public ImagePanel(Dimension size) {
		this.preferredSize = size;
		scale = 1.0;
		fixedSize = true;
	}

	public ImagePanel(int width, int height) {
		this.preferredSize = new Dimension(width, height);
		scale = 1.0;
		fixedSize = true;
	}

	public ImagePanel(InputStream imageStream) throws IOException {
		this(ImageIO.read(imageStream));
	}
	
	public ImagePanel(BufferedImage image) {
		this.image = Objects.requireNonNull(image);
		width = image.getWidth();
		height = image.getHeight();
		this.preferredSize = new Dimension(width, height);
		fixedSize = false;
		setImage(image);
		scale = 1.0;
	}

	/**
	 * Sets the image to display. <code>setImage</code> should be called after the
	 * <code>ImagePanel</code> is made visible.
	 * <p>
	 * The image will be displayed scaled to the panel dimension and centered on the
	 * panel.
	 * 
	 * @param f The image file to display.
	 */
	public void setImage(File f) {
		try (InputStream is = new FileInputStream(f)) {
			setImage(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the image to display. <code>setImage</code> should be called after the
	 * <code>ImagePanel</code> is made visible.
	 * <p>
	 * The image will be displayed scaled to the panel dimension and centered on the
	 * panel.
	 * 
	 * @param f The image file to display as <code>InputStream</code>.
	 */
	public final void setImage(InputStream f) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(f);
			setImage(image);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public final void setImage(BufferedImage image) {
		this.image = image;
		x = y = 0;
		scale = 1.0;
		if (!fixedSize) {
			width = image.getWidth();
			height = image.getHeight();
			this.preferredSize = new Dimension(width, height);
		} else {
			if (image.getWidth() > preferredSize.width || image.getHeight() > preferredSize.height) {
				double imgRatio = image.getWidth() / (double) image.getHeight();
				double vpRatio = preferredSize.width / (double) preferredSize.height;
				if (imgRatio > vpRatio) {
					width = preferredSize.width;
					height = (int) (image.getHeight() * (width / (double) image.getWidth()));
					y = preferredSize.height / 2 - height / 2;
				} else {
					height = preferredSize.height;
					width = (int) (image.getWidth() * (height / (double) image.getHeight()));
					x = preferredSize.width / 2 - width / 2;
				}
				scale = image.getWidth() / (double) width;
			} else {
				width = image.getWidth();
				height = image.getHeight();
				x = preferredSize.width / 2 - width / 2;
				y = preferredSize.height / 2 - height / 2;
			}
		}
		repaint();
	}

	/**
	 * Gets the scale the current image is displayed in.
	 * 
	 * @return The scale the current image is displayed in.
	 */
	public final double getScale() {
		return scale;
	}

	public BufferedImage getImage() {
		return image;
	}

	public int getImageX() {
		return x;
	}

	public int getImageY() {
		return y;
	}

	public int getImageWidth() {
		return width;
	}

	public int getImageHeight() {
		return height;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			g.drawImage(image, x, y, width, height, null);
		}
	}
	
	@Override
	public Dimension getPreferredSize() {
		return preferredSize;
	}

	public void setMinimumSize() {
		setMinimumSize(preferredSize);		
	}
}