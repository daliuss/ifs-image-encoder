package ifs.encoder.components.impl;

import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ifs.encoder.ChanelType;
import ifs.encoder.PartSize;
import ifs.encoder.components.Slicer;
import ifs.encoder.services.RasterImageService;

public class BlockSlicerStrategyDynamic extends BlockSlicerStrategyStatic {
	
	static final double[] half = {0.125d, 0.25d, 0.5d};
	static final int[] cut = {64, 128};
	
	@Override
	public Slicer resolveRangeSlicer(BufferedImage bufferedImage, ChanelType chanelType) {
		return new Slicer(bufferedImage.getWidth(), bufferedImage.getHeight(), rangePart, dynamicQuadraticTree(bufferedImage, rangePart, chanelType));
	}

	protected boolean[] dynamicQuadraticTree(BufferedImage image, PartSize partSize, ChanelType chanelType) {
		if (PartSize.p4.equals(partSize)) {
			return super.simpleQuadraticTree(image, rangePart);
		}
		
		boolean[][][] layers = findLayers(image, partSize, chanelType);
		makeSoftEdges(layers);
		int maxPossible = layers[layers.length-1].length * layers[layers.length-1][0].length * 5;
		
		for (int i=layers.length-2; i>=0; i--) {
			maxPossible += layers[i].length * layers[i][0].length;
		}
		
		boolean[] quadraticTree = new boolean[maxPossible];
		int index = 0;
		for(int y = 0; y < layers[0][0].length; y++) {
			for(int x = 0; x < layers[0].length; x++) {
				index = produceQuadraticTree(layers, index, 0, x, y, quadraticTree);
			}
		}
		
		boolean[] grid = new boolean[index];
		for (int i=0; i<index;i++) {
			grid[i] = quadraticTree[i]; //copying exact size of information
		}
		return grid;
	}
	
	private int produceQuadraticTree(boolean[][][] layers, int index, int level, int x, int y, boolean[] quadraticTree) {
		if (layers[level][x][y]) {
			quadraticTree[index] = true; //marking as has inner blocks
			index++;
			if (level+1 == layers.length) {
				index += 4;
			} else {
				x = x*2;
				y = y*2;
				index = produceQuadraticTree(layers, index, level+1, x, y, quadraticTree);
				index = produceQuadraticTree(layers, index, level+1, x+1, y, quadraticTree);
				index = produceQuadraticTree(layers, index, level+1, x, y+1, quadraticTree);
				index = produceQuadraticTree(layers, index, level+1, x+1, y+1, quadraticTree);
			}
		} else {
			index++;
		}
		return index;
	}
	
	private void makeSoftEdges(boolean[][][] layers) {
		int level = 0;
		while(level + 1 < layers.length) {
			makeSoftEdges(layers, level);
			level++;
		}
	}
	
	private void makeSoftEdges(boolean[][][] layers, int level) {
		int sx, sy, nextLevel = level+1;
		for(int y = 1; y < layers[level][0].length-1; y++) {
			for(int x = 1; x < layers[level].length-1; x++) {
				if(!layers[level][x][y]) {
					sx = x*2;
					sy = y*2;
					//east
					if(layers[level][x+1][y] && layers[nextLevel][sx+2][sy] || layers[nextLevel][sx+2][sy+1]){
						layers[level][x][y] = true;
						continue;
					}
					//west
					if(layers[level][x-1][y] && layers[nextLevel][sx-1][sy] || layers[nextLevel][sx-1][sy+1]){
						layers[level][x][y] = true;
						continue;
					}
					//south
					if(layers[level][x][y+1] && layers[nextLevel][sx][sy+2] || layers[nextLevel][sx+1][sy+2]){
						layers[level][x][y] = true;
						continue;
					}
					//north
					if(layers[level][x][y-1] && layers[nextLevel][sx][sy-1] || layers[nextLevel][sx+1][sy-1]){
						layers[level][x][y] = true;
						continue;
					}
					//east-south
					if(layers[level][x+1][y+1] && layers[nextLevel][sx+2][sy+2]){
						layers[level][x][y] = true;
						continue;
					}
					//east-north
					if(layers[level][x+1][y-1] && layers[nextLevel][sx+2][sy-1]){
						layers[level][x][y] = true;
						continue;
					}
					//west-south
					if(layers[level][x-1][y+1] && layers[nextLevel][sx-1][sy+2]){
						layers[level][x][y] = true;
						continue;
					}
					//west-north
					if(layers[level][x-1][y-1] && layers[nextLevel][sx-1][sy-1]){
						layers[level][x][y] = true;
						continue;
					}
					
				}
			}
		}
	}
	
	private boolean[][][] findLayers(BufferedImage image, PartSize partSize, ChanelType chanelType) {
		boolean[][][] layers = new boolean[partSize.getDepth()-1][][];
		BufferedImage derived = RasterImageService.derived(image, chanelType.getBand());
		BufferedImage[] handler = new BufferedImage[partSize.getDepth()-1];
		for (int i=0; i<handler.length; i++) {
			handler[i] = RasterImageService.copy(derived);
			toneCut(handler[i], cut[i]);
			handler[i] = RasterImageService.resize(handler[i], half[i+partSize.ordinal()-1], half[i+partSize.ordinal()-1]);
			toneHold(handler[i], null);
		}
		
		for(int i=0;i<handler.length; i++) {
			layers[i] = new boolean[handler[i].getWidth()/2][handler[i].getHeight()/2];
			handler[i] = RasterImageService.resize(handler[i], half[half.length-1], half[half.length-1]);
			toneHold(handler[i], layers[i]);
		}
		return layers;
	}
	
	static public BufferedImage toneCut(BufferedImage image, int cut) {
		int w = image.getWidth();
		int h = image.getHeight();
		WritableRaster raster = image.getRaster();

		int tone;
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				tone = raster.getSample(x, y, 0) > cut ? 255 :0;
				raster.setSample(x, y, 0, tone);
				raster.setSample(x, y, 1, tone);
				raster.setSample(x, y, 2, tone);
			}
		}
		return image;
	}
	
	static public BufferedImage toneHold(BufferedImage image, final boolean[][] layer) {
		int w = image.getWidth();
		int h = image.getHeight();
		WritableRaster raster = image.getRaster();

		int tone;
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				tone = raster.getSample(x, y, 0)> 0 ? 255 :0;
				raster.setSample(x, y, 0, tone);
				raster.setSample(x, y, 1, tone);
				raster.setSample(x, y, 2, tone);
				if (layer != null) layer[x][y] = tone > 0;
			}
		}
		
		return image;
	}
	
	/**
	 * Please use 256x256 image
	 * @param image
	 * @param name
	 */
	static void debug(BufferedImage image, String name) {
		try {
			ImageIO.write(RasterImageService.resize(image, 255, 255, AffineTransformOp.TYPE_NEAREST_NEIGHBOR), "png", new File(name + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

}
