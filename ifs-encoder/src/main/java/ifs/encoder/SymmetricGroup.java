package ifs.encoder;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public enum SymmetricGroup {
	group0(0, 0, 0),
	group1(1,-1, 0),
	group2(2,-1,-1),
	group3(3, 0,-1),
	group4(4, 0, 0),
	group5(5, 0,-1),
	group6(6,-1,-1),
	group7(7,-1, 0);
	
	public final static int TRANSFORM_TYPE = AffineTransformOp.TYPE_BILINEAR;
	private int tx, ty, id, dimension;
	private AffineTransform transformation;
	private AffineTransformOp afineFilter;
	private double[] matrix = {0,0,0,0,0,0};
	private BufferedImage returnHandler;
	private WritableRaster returnRasterHandler;
	
	static {
		
		group1.transformation.rotate(-Math.PI/2d);
		//t1.translate(-p, 0);

		group2.transformation.rotate(-Math.PI);
		//t2.translate(-p, -p);
		
		group3.transformation.rotate(-Math.PI*3d/2d);
		//t3.translate(0, -p);
		
		group4.transformation.rotate(-Math.PI/2d);
		group4.transformation.scale(-1, 1);
		
		group5.transformation.scale(1, -1);
		//t5.translate(0, -p);
		
		group6.transformation.rotate(-Math.PI/2d);
		group6.transformation.scale(1, -1);
		//t6.translate(-p, -p);
		
		group7.transformation.scale(-1, 1);
		//t7.translate(-p, 0);
	}
	
	SymmetricGroup(int id, int tx, int ty) {
		this.transformation = new AffineTransform();
		this.id = id;
		this.tx = tx;
		this.ty = ty;
	}
	
	public static SymmetricGroup factory(int id) {
		switch(id){
			case 1: return SymmetricGroup.group1;
			case 2: return SymmetricGroup.group2;
			case 3: return SymmetricGroup.group3;
			case 4: return SymmetricGroup.group4;
			case 5: return SymmetricGroup.group5;
			case 6: return SymmetricGroup.group6;
			case 7: return SymmetricGroup.group7;
			case 0:
			default: return SymmetricGroup.group0;
		}
	}
	
	public BufferedImage filter(BufferedImage image) {
		dimension = image.getWidth();
		transformation.getMatrix(matrix); //save original state
		transformation.translate(tx*dimension, ty*dimension);
		afineFilter = new AffineTransformOp(transformation, TRANSFORM_TYPE);
		returnHandler = afineFilter.createCompatibleDestImage(image, image.getColorModel());
		afineFilter.filter(image, returnHandler);
		restoreMatrix(); //restore original state
		return returnHandler;
	}
	
	public WritableRaster filter(WritableRaster image) {
		dimension = image.getWidth();
		transformation.getMatrix(matrix); //save original state
		transformation.translate(tx*dimension, ty*dimension);
		afineFilter = new AffineTransformOp(transformation, TRANSFORM_TYPE);
		returnRasterHandler = afineFilter.createCompatibleDestRaster(image);
		afineFilter.filter(image, returnRasterHandler);
		restoreMatrix(); //restore original state
		return returnRasterHandler;
	}
	
	public BufferedImage filter(BufferedImage image, double scale) {
		dimension = image.getWidth();
		transformation.getMatrix(matrix); //save original state
		transformation.translate(scale*tx*dimension, scale*ty*dimension);
		transformation.scale(scale, scale);
		afineFilter = new AffineTransformOp(transformation, TRANSFORM_TYPE);
		returnHandler = afineFilter.createCompatibleDestImage(image, image.getColorModel());
		afineFilter.filter(image, returnHandler);
		restoreMatrix(); //restore original state
		return returnHandler;
	}
	
	public WritableRaster filter(WritableRaster image, double scale) {
		dimension = image.getWidth();
		transformation.getMatrix(matrix); //save original state
		transformation.translate(scale*tx*dimension, scale*ty*dimension);
		transformation.scale(scale, scale);
		afineFilter = new AffineTransformOp(transformation, TRANSFORM_TYPE);
		returnRasterHandler = afineFilter.createCompatibleDestRaster(image);
		afineFilter.filter(image, returnRasterHandler);
		restoreMatrix(); //restore original state
		return returnRasterHandler;
	}
	
	private void restoreMatrix() {
		transformation.setTransform(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
	}
	
	public int getId() {
		return id;
	}
}
