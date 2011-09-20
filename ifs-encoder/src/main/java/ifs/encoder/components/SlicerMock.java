package ifs.encoder.components;

import ifs.encoder.PartSize;
import ifs.encoder.SymmetricGroup;

public class SlicerMock extends Slicer {

	public SlicerMock(int w, int h, PartSize initPartSize, boolean[] quadraticTree) {
		super(w, h, initPartSize, quadraticTree);
	}

	protected void initIdenentity() {
		this.current = new IdentityMock();
		this.current.ref = this;
		this.current.symetricGroup = SymmetricGroup.group0;
		this.current.setPart(0);
	}
	
	public class IdentityMock extends Slicer.Identity {
		
		public void setPart(int part) {
			currentPart = part;
			sx = 0;
			sy = 0;
			partSize = ref.blockInfo[part][2];
		}
		
	}

}
