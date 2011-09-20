package ifs.encoder.components;

import java.awt.image.BufferedImage;

import ifs.encoder.Transformation;

public interface SliceComparator {
	
	public void startSession();
		
	public void compare(Slicer rSlicer, Slicer dSlicer, final Transformation result, int band);
	
	public void setSource(BufferedImage source);
	
	public BufferedImage getSource(int pow);
	
}
