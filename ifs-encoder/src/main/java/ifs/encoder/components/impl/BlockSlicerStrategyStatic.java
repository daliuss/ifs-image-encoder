package ifs.encoder.components.impl;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import ifs.encoder.ChanelType;
import ifs.encoder.PartSize;
import ifs.encoder.components.BlockSlicerStrategy;
import ifs.encoder.components.Slicer;

public class BlockSlicerStrategyStatic implements BlockSlicerStrategy {
	

	protected PartSize domainPart, rangePart;
	
	public BlockSlicerStrategyStatic() {
		setRangePart(PartSize.p16);
	}
	
	public void setRangePart(PartSize rangePart){
		this.rangePart = rangePart;
		this.domainPart = PartSize.values()[rangePart.ordinal()-1];
	}

	@Override
	public Slicer resolveRangeSlicer(BufferedImage bufferedImage, ChanelType chanelType) {
		return new Slicer(bufferedImage.getWidth(), bufferedImage.getHeight(), rangePart, simpleQuadraticTree(bufferedImage, rangePart));
	}

	@Override
	public Slicer resolveDomainSlicer(BufferedImage bufferedImage, ChanelType chanelType) {
		return new Slicer(bufferedImage.getWidth(), bufferedImage.getHeight(), domainPart, simpleQuadraticTree(bufferedImage, domainPart));
	}

	@Override
	public Dimension getWorkingDimension(BufferedImage bufferedImage) {
		int w = bufferedImage.getWidth();
		int h = bufferedImage.getHeight();
		int size = domainPart.getSize();
		Dimension dim = new Dimension((w/size)*size, (h/size)*size);
		dim.width += ((w%size)>0 ? size : 0);
		dim.height += ((w%size)>0 ? size : 0);
		return dim;
	}
	
	protected boolean[] simpleQuadraticTree(BufferedImage bufferedImage, PartSize partSize) {
		Dimension dim = getWorkingDimension(bufferedImage);
		int w = dim.width / partSize.getSize();
		int h = dim.height / partSize.getSize();
		boolean[] grid = new boolean[w*h]; //static grid
		return grid;
	}

}
