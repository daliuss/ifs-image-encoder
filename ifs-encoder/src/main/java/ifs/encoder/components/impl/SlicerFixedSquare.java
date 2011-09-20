package ifs.encoder.components.impl;

import ifs.encoder.ChanelType;
import ifs.encoder.SymmetricGroup;


public class SlicerFixedSquare {
	
	public int w, h, partSize, size, wSize, hSize, currentPart, sx, sy, ex, ey, nx, ny;

	private ChanelType chanelType;
	
	SymmetricGroup symetricGroup;
	
	public SlicerFixedSquare(int w, int h, int partSize) {
		this.w = w;
		this.h = h;
		this.partSize = partSize;
		wSize = w / partSize;
		hSize = h / partSize;
		size = wSize * hSize;
		currentPart = 0;
		symetricGroup = SymmetricGroup.group0;
		chanelType = ChanelType.grayChanel;
	}
	
	public ChanelType getChanelType() {
		return chanelType;
	}

	public void setChanelType(ChanelType chanelType) {
		this.chanelType = chanelType;
	}
	
	/**
	 * All part count in slicer
	 * @return
	 */
	
	public int getSize() {
		return size;
	}
	
	public int getPartSize() {
		return partSize;
	}
	
	public void setPart(int part) {
		currentPart = part;
		sx = partSize * (currentPart % wSize);
		sy = partSize * (currentPart / hSize);
		ex = sx + partSize;
		ey = sy + partSize;
	}
	
	public int getPart() {
		return currentPart;
	}
	
	public boolean hasNext() {
		return (currentPart+1 < size);
	}
	
	public void setNext() {
		if (hasNext()) {
			setPart(currentPart+1);
		}
	}

	public int getOriginalX(int x) {
		return sx+x;
	}
	
	public int getOriginalY(int y) {
		return sy+y;
	}

	public int getOriginalX() {
		return sx;
	}
	
	public int getOriginalY() {
		return sy;
	}

	public SymmetricGroup getSymetricGroup() {
		return symetricGroup;
	}

	public void setSymetricGroup(SymmetricGroup symetricGroup) {
		this.symetricGroup = symetricGroup;
	}
		

}
