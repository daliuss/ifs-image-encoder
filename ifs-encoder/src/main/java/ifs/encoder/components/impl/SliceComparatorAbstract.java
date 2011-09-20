package ifs.encoder.components.impl;

import java.awt.image.BufferedImage;

import ifs.encoder.PartSize;
import ifs.encoder.components.SliceComparator;
import ifs.encoder.services.RasterImageService;

public abstract class SliceComparatorAbstract implements SliceComparator {
	
	public void startSession() {
		this.bufferedImageSource = new BufferedImage[PartSize.values().length];  //4
	}

	protected BufferedImage[] bufferedImageSource;

	@Override
	public void setSource(BufferedImage source) {
		this.bufferedImageSource[0] = source;
		for (int i=1; i<this.bufferedImageSource.length; i++) {
			this.bufferedImageSource[i] = RasterImageService.resize(this.bufferedImageSource[i-1], 0.5, 0.5);
		}
	}


	@Override
	public BufferedImage getSource(int pow) {
		return this.bufferedImageSource[pow];
	}
}
