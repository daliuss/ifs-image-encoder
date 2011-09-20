package ifs.encoder.services;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ifs.encoder.ChanelType;
import ifs.encoder.DebugInformation;
import ifs.encoder.IFSImage;
import ifs.encoder.IFSImage.Chanel;
import ifs.encoder.RasterImage;
import ifs.encoder.SymmetricGroup;
import ifs.encoder.Transformation;
import ifs.encoder.components.BlockSlicerStrategy;
import ifs.encoder.components.SliceComparator;
import ifs.encoder.components.Slicer;
import ifs.encoder.utils.Profiler;


/**
 * Encoding/decoding service
 * @author Dalius
 */

public class IFSImageService extends IFSImageIOService {
	
	public static final Logger log = Logger.getLogger(IFSImageService.class);

	private SliceComparator sliceComparator;
	private BlockSlicerStrategy blockSlicerStrategy;
	private ProcessStatus status;
	private BufferedImage initImage;
	private static final int epsilon = 40;

	public IFSImageService() {
		status = new ProcessStatus();
	}
	
	/**
	 * Creates IFS image from raster image
	 * @param rasterImage
	 * @return
	 */
	public IFSImage encode(RasterImage rasterImage) {
		return encode(rasterImage, null);
	}
	
	/**
	 * Creates IFS image from raster image. Gathers debug information if asked.
	 * @param rasterImage
	 * @param debugInfo
	 * @return
	 */
	
	public IFSImage encode(RasterImage rasterImage, DebugInformation info) {
		
		Profiler.resetAll();
		Profiler.encode.start();
		log.info("Start");
		
		sliceComparator.startSession(); //resets cache
		status.reset();
		status.setEncoding(true);
		
		IFSImage ifsImage = new IFSImage(rasterImage.getW(), rasterImage.getH(), rasterImage.getImageType());

		//perform resize from original to working image dimensions
		Dimension dim = blockSlicerStrategy.getWorkingDimension(rasterImage.getImage());
		BufferedImage workingImage = RasterImageService.resize(rasterImage.getImage(), dim.width, dim.height);
		ifsImage.setW(dim.width);
		ifsImage.setH(dim.height);
		
		//iteration of channels [gray] or [red,green,blue]		
		for (ChanelType chanel: rasterImage.getImageType().getChanels()){
			ifsImage.getChanel(chanel).setTransformations(new ArrayList<Transformation>());
			//encoding channel
			encodeChanel(workingImage, chanel, ifsImage.getChanel(chanel));
		}
		status.setEncoding(false);

		Profiler.encode.end();
		Profiler.subtract(Profiler.allocation, Profiler.subtraction); //Removing subtraction from allocation subset
		
		
		log.info("Finish at " + Profiler.encode.getElapsed());
		log.info("Allocation:  " + Profiler.allocation.getElapsed());
		log.info("Substraction: " + Profiler.subtraction.getElapsed());
		if(info!=null) {
			info.setOriginal(RasterImageService.copy(rasterImage.getImage()));
			info.setWorking(RasterImageService.copy(workingImage));
			info.setEncoded(ifsImage.clone());
		}
		return ifsImage;
	}
	
	/**
	 * 
	 * @param image
	 * @param chanelType
	 * @param ifsChanel
	 */
	
	private void encodeChanel(BufferedImage image, ChanelType chanelType, Chanel ifsChanel) {

		//Prepare slicers according to image
		Slicer rangeParts = blockSlicerStrategy.resolveRangeSlicer(image, chanelType);
		Slicer domainParts = blockSlicerStrategy.resolveDomainSlicer(image, chanelType);

		//Total steps for observation
		status.setTotalSteps(rangeParts.getBlockCount());
		
		//Set working source
		sliceComparator.setSource(image);
		
		//Temporary result handlers
		Transformation best, tempResult = new Transformation();
		
		for(int i=0; i<rangeParts.getBlockCount(); i++) {
			rangeParts.getCurrent().setPart(i);
			status.setCurrentStep(i+1);
			best = null;
			for(int j=0; j<domainParts.getBlockCount(); j++) {
				domainParts.getCurrent().setPart(j);
				if(rangeParts.getCurrent().partSize == domainParts.getCurrent().partSize)
					continue; //must be compressing ratio!
				for (SymmetricGroup group: SymmetricGroup.values()) {
					domainParts.getCurrent().setSymetricGroup(group);
					sliceComparator.compare(rangeParts, domainParts, tempResult, chanelType.getBand());
					if(tempResult.isBetterThan(best)) {
						best = tempResult.clone();
					}
				}
				if (best!=null && best.getSum() < epsilon)
					break;
			}
			ifsChanel.getTransformations().add(best);
		}

		//Remember slicer information
		ifsChanel.setQuadraticRangeTree(rangeParts.getQuadraticTree());
		ifsChanel.setQuadraticDomainTree(domainParts.getQuadraticTree());
		ifsChanel.setRPartSize(rangeParts.getInitPartSize());
		ifsChanel.setDPartSize(domainParts.getInitPartSize());
	}
	
	/**
	 * 
	 * @param image
	 * @return
	 */
	
	public BufferedImage decode(IFSImage image) {
		return decode(image, 8);
	}
	
	/**
	 * 
	 * @param image
	 * @param iterations
	 * @return
	 */
		
	public BufferedImage decode(IFSImage image, int iterations) {
		Slicer rangeParts;
		Slicer domainParts;
		BufferedImage result = getInitImage(image.getW(), image.getH());
		for(ChanelType chanelType: image.getImageType().getChanels()) {
			rangeParts = new Slicer(image.getW(), image.getH(), image.getChanel(chanelType).getRPartSize(), image.getChanel(chanelType).getQuadraticRangeTree());
			domainParts = new Slicer(image.getW(), image.getH(), image.getChanel(chanelType).getDPartSize(), image.getChanel(chanelType).getQuadraticDomainTree());
			result = iterate(result, chanelType, image.getChanel(chanelType).getTransformations(), rangeParts, domainParts, iterations);
		}
		//image resizing from working copy to original dimensions
		result = RasterImageService.resize(result, image.getOriginalW(), image.getOriginalH());
		return result;
	}
	
	/**
	 * Iteration method
	 * @param workingImage
	 * @param chanelType
	 * @param chanel
	 * @param rangeParts
	 * @param domainParts
	 * @param iteration
	 * @return
	 */
	
	private BufferedImage iterate(BufferedImage workingImage, ChanelType chanelType, List<Transformation> chanel, Slicer rangeParts, Slicer domainParts, int iteration) {
		if (iteration == 0) {
			return workingImage;
		}
		for(Transformation transformation : chanel) {
			applyTransformation(transformation, chanelType, workingImage, workingImage, rangeParts, domainParts);
		}
		return iterate(workingImage, chanelType, chanel, rangeParts, domainParts, --iteration);
	}
	
	/**
	 * Applies block transformation from source to destination images. Source pixels are scaled to fit destination block size.
	 * Every pixel from block are transformed by one of 8 Symmetric Group transformations and change value
	 * (x * k) + r operation applied.
	 * @param transformation
	 * @param chanelType
	 * @param source
	 * @param destination
	 * @param rangeSlicer
	 * @param domainSlicer
	 * @return
	 */
	
	public BufferedImage applyTransformation(Transformation transformation, ChanelType chanelType, BufferedImage source,
			BufferedImage destination, Slicer rangeSlicer, Slicer domainSlicer) {
		
		domainSlicer.getCurrent().setPart(transformation.getDomainPart());
		rangeSlicer.getCurrent().setPart(transformation.getRangePart());
		domainSlicer.getCurrent().setSymetricGroup(transformation.getSymetricGroup());
		int rangeSize = rangeSlicer.getCurrent().partSize;
		int domainSize = domainSlicer.getCurrent().partSize;
		/*
		 * Get scale factor (range/domain)
		 */
		float scale = (float)rangeSize / (float)domainSize;
		
		/*
		 * Applying Symmetric Group transformation and shrinking 
		 * source area (domain block) to fit destination area (range block)
		 */
		BufferedImage sourceBlock = domainSlicer.getCurrent().getSymetricGroup().filter(
				source.getSubimage(domainSlicer.getCurrent().sx, domainSlicer.getCurrent().sy, domainSize, domainSize), scale);
		
		/*
		 * Applying pixel change
		 */
		float k = transformation.getK();
		float r = transformation.getR();
		RescaleOp rescaleOperator = new RescaleOp(chanelType.getScaleFactors(k), chanelType.getOffsets(r), null);
		sourceBlock = rescaleOperator.filter(sourceBlock, null);
		
		/*
		 * Pixel by pixel copy to new created image
		 */
		int pixelValue;
		if (ChanelType.grayChanel.equals(chanelType)) {
			for (int y=0; y<rangeSlicer.getCurrent().partSize; y++) {
				for (int x=0; x<rangeSlicer.getCurrent().partSize; x++) {
					pixelValue = sourceBlock.getRGB(x, y);
					destination.setRGB(rangeSlicer.getCurrent().sx + x, rangeSlicer.getCurrent().sy + y, pixelValue);
				}
			}
		} else {
			WritableRaster destinationRaster = destination.getRaster();
			Raster sourceRaster = sourceBlock.getRaster();
			for (int y=0; y<rangeSlicer.getCurrent().partSize; y++) {
				for (int x=0; x<rangeSlicer.getCurrent().partSize; x++) {
					pixelValue = sourceRaster.getSample(x, y, chanelType.getBand());
					destinationRaster.setSample(rangeSlicer.getCurrent().sx + x, rangeSlicer.getCurrent().sy + y, chanelType.getBand(), pixelValue);
				}
			}
		}
		return sourceBlock;
	}
	

	public void setInitImage(BufferedImage initImage) {
		this.initImage = initImage;
	}

	public BufferedImage getInitImage(int w, int h) {
		if (initImage==null) {
			initImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

			for (int y=0; y<h; y++) {
				for (int x=0; x<w; x++) {
					initImage.setRGB(x, y, 0x888888);
				}
			}

			for (int y=0; y<h; y=y+5) {
				for (int x=0; x<w; x++) {
					initImage.setRGB(x, y, 0xFFFFFF);
				}
			}
		} else if (initImage.getWidth()!=w || initImage.getHeight()!=h) {
			initImage = RasterImageService.resize(initImage, w, h);
		}
		initImage = RasterImageService.convertType(initImage, RasterImageService.WORKING_IMAGE_TYPE);
		return RasterImageService.copy(initImage);
	}

	public class ProcessStatus {
		private int currentStep;
		private int totalSteps;
		private boolean encoding;
		
		private void reset() {
			currentStep = 0;
			totalSteps = 0;
			encoding = false;
		}
		
		public int getCurrentStep() {
			return currentStep;
		}
		
		public int getTotalSteps() {
			return totalSteps;
		}
		
		public boolean isEncoding() {
			return encoding;
		}
		
		private void setCurrentStep(int currentStep) {
			this.currentStep = currentStep;
		}
		
		private void setTotalSteps(int totalSteps) {
			this.totalSteps = totalSteps;
		}
		
		private void setEncoding(boolean encoding) {
			this.encoding = encoding;
		}
		
	}
	
	public BlockSlicerStrategy getBlockSlicerStrategy() {
		return blockSlicerStrategy;
	}

	public void setBlockSlicerStrategy(BlockSlicerStrategy blockSlicerStrategy) {
		this.blockSlicerStrategy = blockSlicerStrategy;
	}
	
	public SliceComparator getSliceComparator() {
		return sliceComparator;
	}

	public void setSliceComparator(SliceComparator sliceComparator) {
		this.sliceComparator = sliceComparator;
	}
	
	public ProcessStatus getStatus() {
		return status;
	}

}
