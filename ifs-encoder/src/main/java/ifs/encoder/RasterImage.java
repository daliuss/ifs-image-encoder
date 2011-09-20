package ifs.encoder;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class RasterImage {

	protected int w, h;

	protected BufferedImage image;
	protected String originalName;

	protected ImageType imageType;
	
	public RasterImage(int w, int h, ImageType imageType) {
		this.w = w;
		this.h = h;
		this.imageType = imageType;
	}

	public int getTotalPixels() {
		return this.h*this.w;
	}
	
	/* setters and getters */

	public ImageType getImageType() {
		return imageType;
	}
	
	public int getW() {
		return w;
	}

	public int getH() {
		return h;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage original) {
		this.w = original.getWidth();
		this.h = original.getHeight();
		this.image = original;
	}

	public WritableRaster getRaster() {
		return image.getRaster();
	}
}
