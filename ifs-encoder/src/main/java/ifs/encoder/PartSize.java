package ifs.encoder;

public enum PartSize {
	p32(32, 4), p16(16, 3), p8(8, 2), p4(4, 1); // do not dare to change it!
	private int size;
	private int depth;
	PartSize(int size, int depth) {
		this.size = size;
		this.depth = depth;
	}
	public int getSize() {
		return size;
	}
	public int getDepth() {
		return depth;
	}
	static public int getIdBySize(int size) {
		switch(size) {
			case 32: return 0;
			case 16: return 1;
			case 8: return 2;
			case 4:return 3;
		}
		return -1;
	}
};
