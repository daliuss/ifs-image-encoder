package ifs.encoder.components;

import ifs.encoder.PartSize;
import ifs.encoder.SymmetricGroup;

public class Slicer {
	
	public int w, h, count, wSize, hSize;
	protected Identity current;
	

	protected PartSize initPartSize;

	protected int[][] blockInfo; // hash of blocks by block number
	protected boolean[] quadraticTree; // encoded quadratic tree

	public Slicer(int w, int h, PartSize initPartSize, boolean[] quadraticTree) {
		this.w = w;
		this.h = h;
		this.initPartSize = initPartSize;
		this.quadraticTree = quadraticTree;
		initTree();
		initIdenentity();
	}
	
	protected void initIdenentity() {
		this.current = new Identity();
		this.current.ref = this;
		this.current.symetricGroup = SymmetricGroup.group0;
		this.current.setPart(0);
	}
	
	private void initTree() {
		int sx=0, sy=0;
		this.count = 0;
		this.blockInfo = new int[this.quadraticTree.length][];
		int index = 0;
		while(index < this.quadraticTree.length) {
			index = initInnerNodes(index, sx, sy, this.initPartSize.getSize());
			sx += this.initPartSize.getSize();
			if (sx >= w) {
				sx = 0;
				sy += this.initPartSize.getSize();
			}
		}
	}
	
	private int initInnerNodes(int index, int sx, int sy, int partSize) {
		if (!this.quadraticTree[index]) {//means block
			this.blockInfo[this.count] = new int[]{sx, sy, partSize};
			this.count++;
			index++;
		} else {
			int half = partSize/2;
			index = initInnerNodes(index+1, sx, sy, half);
			index = initInnerNodes(index, sx+half, sy, half);
			index = initInnerNodes(index, sx, sy+half, half);
			index = initInnerNodes(index, sx+half, sy+half, half);
		}
		return index;
	}
	
	public class Identity {

		public int partSize = 0;
		public int currentPart = 0;
		public int sx, sy;
		
		protected Slicer ref = null;
		protected SymmetricGroup symetricGroup;
		
		public void setPart(int part) {
			currentPart = part;
			sx = ref.blockInfo[part][0];
			sy = ref.blockInfo[part][1];
			partSize = ref.blockInfo[part][2];
		}
		
		public boolean hasNext() {
			return (currentPart+1 < ref.count);
		}
		
		public void setNext() {
			if (hasNext()) {
				setPart(currentPart+1);
			}
		}

		public SymmetricGroup getSymetricGroup() {
			return symetricGroup;
		}

		public void setSymetricGroup(SymmetricGroup symetricGroup) {
			this.symetricGroup = symetricGroup;
		}
		
	}
	
	/**
	 * All part count in slicer
	 * @return
	 */
	
	public int getBlockCount() {
		return count;
	}	

	public boolean[] getQuadraticTree() {
		return quadraticTree;
	}
	
	public PartSize getInitPartSize() {
		return initPartSize;
	}

	public Identity getCurrent() {
		return current;
	}
}
