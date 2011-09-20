package ifs.encoder.services;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;
import org.apache.log4j.Logger;

import ifs.encoder.ImageType;
import ifs.encoder.RasterImage;

public class RasterImageService {
	
	public static final Logger log = Logger.getLogger(RasterImageService.class);
	
	public static final int WORKING_IMAGE_TYPE = BufferedImage.TYPE_INT_RGB;
	public static LinearDerivationService linearDerivationService;
	
	static {
		linearDerivationService = new LinearDerivationService(1.5);
	}
	
	/**
	 * Image load from file
	 * @param file
	 * @return
	 */
	public BufferedImage loadFromFile(File file) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(file);
		} catch(Exception ex) {
			log.error(ex.getMessage());
			log.error("File: " + file.getAbsolutePath());
		}
		return image;
	}
	
	/**
	 * Loads image to RasterImage type
	 * @param file
	 * @return
	 */
	
	public RasterImage factoryFromFile(File file) {
		
		String name = file.getName();
		return factoryFromImage(loadFromFile(file), name.substring(0, name.indexOf(".")));
	}
	
	public static RasterImage factoryFromImage(BufferedImage bufferedImage) {
		return factoryFromImage(bufferedImage, "");
	}
	
	/**
	 * Wraps BufferedImage image to RasterImage type image
	 * @param bufferedImage
	 * @param originalName
	 * @return
	 */
	
	public static RasterImage factoryFromImage(BufferedImage bufferedImage, String originalName) {
		if(bufferedImage==null){
			throw new IllegalArgumentException("Not all parammeters provided!");
		}
		
		ImageType imageType = null;
		
		if (bufferedImage.getRaster().getNumBands()==1) {
			imageType = ImageType.gray;
		} else if(bufferedImage.getRaster().getNumBands()>=3) {
			imageType = ImageType.rgb;
		} else {
			throw new IllegalArgumentException("Image type is not supported!");
		}
		
		//image type needs to be consistent in whole application
		bufferedImage = convertType(bufferedImage, WORKING_IMAGE_TYPE);
		
		RasterImage image = new RasterImage(bufferedImage.getWidth(), bufferedImage.getHeight(), imageType);
		image.setOriginalName(originalName);
		image.setImage(bufferedImage);
		
		return image;
	}
	/**
	 * Resizes image to preferred image size with bilinear interpolation
	 * @param image
	 * @param w
	 * @param h
	 * @return
	 */
	
	static public BufferedImage resize(BufferedImage image, int w, int h) {
		return resize(image, w, h, AffineTransformOp.TYPE_BILINEAR);
	}
	
	/**
	 * Resizes image by scale ratio with preferred  bilinear interpolation
	 * @param image
	 * @param w
	 * @param h
	 * @return
	 */
	
	static public BufferedImage resize(BufferedImage image, double scalex, double scaley) {
		return resize(image, scalex, scaley, AffineTransformOp.TYPE_BILINEAR);
	}
	
	/**
	 * Resizes image to required size with preferred interpolation type
	 * @see AffineTransformOp.TYPE_BILINEAR
	 * @see AffineTransformOp.TYPE_BICUBIC
	 * @see AffineTransformOp.TYPE_NEAREST_NEIGHBOR
	 * 
	 * @param image
	 * @param w
	 * @param h
	 * @param type
	 * @return
	 */

	static public BufferedImage resize(BufferedImage image, int w, int h, int type) {
		double scalex = (double)w / (double)image.getWidth();
		double scaley = (double)h / (double)image.getHeight();
		return resize(image, scalex, scaley, type);
	}
	
	/**
	 * Resizes image by scale ratio with preferred interpolation type
	 * @param image
	 * @param scalex
	 * @param scaley
	 * @param type
	 * @return
	 */

	static public BufferedImage resize(BufferedImage image, double scalex, double scaley, int type) {
		AffineTransform transform = new AffineTransform();
		transform.scale(scalex, scaley);
		AffineTransformOp af = new AffineTransformOp(transform, type);
		BufferedImage dest = af.createCompatibleDestImage(image, image.getColorModel());
		af.filter(image, dest);
		return dest;
	}
	/**
	 * Converts image to required type
	 * @param image
	 * @param type
	 * @return
	 */
	
	static public BufferedImage convertType(BufferedImage image, int type) {
		if (image.getType() == type)
		return image;
		BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), type);
		Graphics2D g = result.createGraphics();
		g.drawRenderedImage(image, null);
		g.dispose();
		return result;

	}
	
	/**
	 * Copies image
	 * @param initImage
	 * @return
	 */
	public static BufferedImage copy(BufferedImage initImage) {
		BufferedImage image = new BufferedImage(initImage.getWidth(), initImage.getHeight(), initImage.getType());
		image.getGraphics().drawImage(initImage, 0, 0, null);
		return image;
	}
	
	/**
	 * Converts image to gray scaled by the highest tone in pixel band
	 * @param image
	 * @return
	 */
	
	public static BufferedImage tonizated(BufferedImage image) {
		WritableRaster raster = image.getRaster();
		if(raster.getNumBands()>=3) {
			int tone = 0;
			for (int y=0; y<raster.getHeight(); y++) {
				for(int x=0; x<raster.getWidth(); x++) {
					tone = Math.max(raster.getSample(x, y, 0), 
							Math.max(raster.getSample(x, y, 1), raster.getSample(x, y, 2)));
					raster.setSample(x, y, 0, tone);
					raster.setSample(x, y, 1, tone);
					raster.setSample(x, y, 2, tone);
				}
			}			
		}
		return image;
	}
	
	/**
	 * Filter image to gradient. 
	 * @param image
	 * @return
	 */
	
	public static BufferedImage derived(BufferedImage image, int band) {
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		WritableRaster newRaster = newImage.getRaster();
		WritableRaster raster = image.getRaster();
		
		
		double[] result, diff1;
		double[] lineh = new double[h];
		double[] linew = new double[w];
		double[][] imageMatrix = new double[w][h];
		double[][] imageXDiff = new double[w][h];
		double[][] imageYDiff = new double[w][h];

		
		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;

		//column
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				lineh[y] = raster.getSample(x, y, band);
			}
			linearDerivationService.process(lineh, false);
			result = linearDerivationService.getResult();
			imageMatrix[x] = result;
			imageYDiff[x] = linearDerivationService.getDiff1();
		}

		//row
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				linew[x] = imageMatrix[x][y];
			}
			linearDerivationService.process(linew, false);
			result = linearDerivationService.getResult();
			diff1 = linearDerivationService.getDiff1();
			for(int x = 0; x < w; x++) {
				imageMatrix[x][y] = result[x];
				imageXDiff[x][y] = diff1[x];
			}
		}
		
		//column for y
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				linew[x] = imageYDiff[x][y];
			}
			result = linearDerivationService.process(linew).getResult();
			for(int x = 0; x < w; x++) {
				imageMatrix[x][y] = Math.sqrt(Math.sqrt(result[x]*result[x] + imageXDiff[x][y]*imageXDiff[x][y]));
				min = ((imageMatrix[x][y]<min) ? imageMatrix[x][y] : min);
				max = ((imageMatrix[x][y]>max) ? imageMatrix[x][y] : max);
			}
		}

		double delta = max - min;
		int tone;
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				tone = 0xFF & (int)((imageMatrix[x][y]-min)*255.99d/delta);
				newRaster.setSample(x, y, 0, tone);
				newRaster.setSample(x, y, 1, tone);
				newRaster.setSample(x, y, 2, tone);
			}
		}
		
		return newImage;
	}

}
