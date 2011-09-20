package ifs.encoder;

/**
 * Image type provider
 * @author Dalius
 */

public enum ImageType {
	
	gray(0, ChanelType.grayChanel), 
	rgb(1, ChanelType.redChannel, ChanelType.greenChanel, ChanelType.blueChanel);
	
	private ChanelType[] chanels;
	private int id;
	
	ImageType(int id, ChanelType... chanelTypes) {
		chanels = chanelTypes;
		this.id = id;
	}
	
	static public ImageType factory(int id) {
		switch(id) {
			case 1: return rgb;
			case 0: return gray;
			default: throw new IllegalArgumentException("Unsupported ImageType type: " + id);
		}
	}
	
	public ChanelType[] getChanels() {
		return chanels;
	}

	public int getId() {
		return id;
	}
}