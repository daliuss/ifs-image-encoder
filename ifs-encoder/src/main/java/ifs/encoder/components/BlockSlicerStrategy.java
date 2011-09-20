package ifs.encoder.components;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import ifs.encoder.ChanelType;
import ifs.encoder.PartSize;


public interface BlockSlicerStrategy {
	
	public void setRangePart(PartSize domainPart);

	public Slicer resolveRangeSlicer(BufferedImage bufferedImage, ChanelType chanelType);
	
	public Slicer resolveDomainSlicer(BufferedImage bufferedImage, ChanelType chanelType);
	
	public Dimension getWorkingDimension(BufferedImage bufferedImage);

}
