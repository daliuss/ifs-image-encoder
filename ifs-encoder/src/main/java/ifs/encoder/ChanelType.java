package ifs.encoder;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

public enum ChanelType {
	
	grayChanel(0, true, true, true), 
	redChannel(0, true, false, false), 
	greenChanel(1, false, true, false), 
	blueChanel(2, false, false, true);
	
	private int band;
	private float[] scaleFacors;
	private float[] offsets;
	private boolean r, g, b;
	
	ChanelType(int band, boolean r, boolean g, boolean b) {
		this.band = band;
		this.r = r;
		this.g = g;
		this.b = b;
		
		scaleFacors = new float[3];
		//scaleFacors[3] = 1f;//alpha
		offsets = new float[3];
		//offsets[3] = 0f;//alpha
	}
	
	public int getBand() {
		return band;
	}
	
	public float[] getScaleFactors(float scale) {
		scaleFacors[0] = r ? scale : 1f;
		scaleFacors[1] = g ? scale : 1f;
		scaleFacors[2] = b ? scale : 1f;
		return scaleFacors;
	}
	
	public float[] getOffsets(float offset) {
		offsets[0] = r ? offset : 0f;
		offsets[1] = g ? offset : 0f;
		offsets[2] = b ? offset : 0f;
		return offsets;
	}
	
	public float[] getFilterFactors() {
		scaleFacors[0] = r ? 1f : 0f;
		scaleFacors[1] = g ? 1f : 0f;
		scaleFacors[2] = b ? 1f : 0f;
		return scaleFacors;
	}
	
	public float[] getFilterOffsets() {
		offsets[0] = 0f;
		offsets[1] = 0f;
		offsets[2] = 0f;
		return offsets;
	}
	
	public BufferedImage filterChanel(BufferedImage image) {
		if (image.getRaster().getNumBands()!=3) throw new IllegalArgumentException();
		RescaleOp rescaleOperator = new RescaleOp(getFilterFactors(), getFilterOffsets(), null);
		return rescaleOperator.filter(image, null);
		
	}
	
}
