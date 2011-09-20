package ifs.encoder;

import java.awt.image.BufferedImage;

public class DebugInformation {
	
	private IFSImage encoded;
	private BufferedImage original, working;

	public void setEncoded(IFSImage encoded) {
		this.encoded = encoded;
	}

	public IFSImage getEncoded() {
		return encoded;
	}

	public void setOriginal(BufferedImage original) {
		this.original = original;
	}

	public BufferedImage getOriginal() {
		return original;
	}

	public void setWorking(BufferedImage working) {
		this.working = working;
	}

	public BufferedImage getWorking() {
		return working;
	}
}
