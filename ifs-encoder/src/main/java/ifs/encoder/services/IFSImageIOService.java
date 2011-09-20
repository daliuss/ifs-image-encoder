package ifs.encoder.services;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.ValidationException;

import org.apache.log4j.Logger;

import ifs.encoder.ChanelType;
import ifs.encoder.IFSImage;
import ifs.encoder.ImageType;
import ifs.encoder.PartSize;
import ifs.encoder.Transformation;
import ifs.encoder.SymmetricGroup;

import ifs.encoder.utils.BitInputStream;
import ifs.encoder.utils.BitOutputStream;

public class IFSImageIOService {
	
	public static final Logger log = Logger.getLogger(IFSImageIOService.class);
	
	static final short BITS_IMAGE_TYPE = 1;
	static final short BITS_IMAGE_DIMENSION = 11; // [1-2048] [0000 0000 0100 0000 0000]
	static final short BITS_SLICER_DIMENSION = 2;
	static final short BITS_TREE_LENGTH = 19; // [1-344064]   [0101 0100 0000 0000 0000]
	static final short BITS_PART_TOTAl = 18; //  [1-262144]   [0100 0000 0000 0000 0000]
	static final short BITS_DOMAIN_NUM = BITS_PART_TOTAl;
	static final short BITS_SYMMETRIC_GROUP = 3;
	static final short BITS_K_VALUE = 32;
	static final short BITS_R_VALUE = 16;
	
	public void writeToFile(IFSImage image, File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
			BitOutputStream bos = new BitOutputStream(fos);

			bos.writeBits(image.getImageType().getId(), BITS_IMAGE_TYPE);

			bos.writeBits(dec(image.getOriginalW()), BITS_IMAGE_DIMENSION);
			bos.writeBits(dec(image.getOriginalH()), BITS_IMAGE_DIMENSION);
			
			bos.writeBits(dec(image.getW()), BITS_IMAGE_DIMENSION);
			bos.writeBits(dec(image.getH()), BITS_IMAGE_DIMENSION);
			
			for(ChanelType chanelType : image.getImageType().getChanels()) {
			
				bos.writeBits(image.getChanel(chanelType).getRPartSize().ordinal(), BITS_SLICER_DIMENSION);
				writeQuadraticTree(bos, image.getChanel(chanelType).getQuadraticRangeTree());
				bos.writeBits(image.getChanel(chanelType).getDPartSize().ordinal(), BITS_SLICER_DIMENSION);
				writeQuadraticTree(bos, image.getChanel(chanelType).getQuadraticDomainTree());
				
				bos.writeBits(dec(image.getChanel(chanelType).getTransformations().size()), BITS_PART_TOTAl);
				
				for (Transformation result : image.getChanel(chanelType).getTransformations()) {
					bos.writeBits(result.getDomainPart(), BITS_DOMAIN_NUM);
					bos.writeBits(result.getSymetricGroup().getId(), BITS_SYMMETRIC_GROUP);
					bos.writeBits(Float.floatToIntBits(result.getK()), BITS_K_VALUE);
					bos.writeBits((short)result.getR(), BITS_R_VALUE);
				}
			}
			
			bos.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		validateWrite(image, file);
		log.debug(image.toString());
	}
	
	private void writeQuadraticTree(BitOutputStream bos, boolean[] bits) throws IOException {
		bos.writeBits(bits.length, BITS_TREE_LENGTH);
		for(boolean bit : bits) {
			bos.writeBit(bit ? 1: 0);
		}
	}
	
	private int dec(int num) {
		return num-1;
	}
	
	private int inc(int num) {
		return num+1;
	}
	
	
	private boolean[] readQuadraticTree(BitInputStream bis) throws IOException {
		int lenght = bis.readBits(BITS_TREE_LENGTH);
		boolean[] tree = new boolean[lenght];
		for(int i=0; i<lenght; i++) {
			tree[i] = bis.readBit() == 1;
		}
		return tree;
	}
	
	public IFSImage readFromFile(File file) {
		
		IFSImage image = null;
		int totalParts = 0;
		try {
			FileInputStream fis = new FileInputStream(file.getAbsolutePath());
			BitInputStream bis = new BitInputStream(fis);
			
			ImageType imageType = ImageType.factory(bis.readBits(BITS_IMAGE_TYPE));
			int w = inc(bis.readBits(BITS_IMAGE_DIMENSION));
			int h = inc(bis.readBits(BITS_IMAGE_DIMENSION));
			
			image = new IFSImage(w, h, imageType);
			
			image.setW(inc(bis.readBits(BITS_IMAGE_DIMENSION)));
			image.setH(inc(bis.readBits(BITS_IMAGE_DIMENSION)));
			
			for(ChanelType chanelType : image.getImageType().getChanels()) {
			
				image.getChanel(chanelType).setRPartSize(PartSize.values()[bis.readBits(BITS_SLICER_DIMENSION)]);
				image.getChanel(chanelType).setQuadraticRangeTree(readQuadraticTree(bis));
				image.getChanel(chanelType).setDPartSize(PartSize.values()[bis.readBits(BITS_SLICER_DIMENSION)]);
				image.getChanel(chanelType).setQuadraticDomainTree(readQuadraticTree(bis));
				
				totalParts = inc(bis.readBits(BITS_PART_TOTAl));
				
				List<Transformation> results = new ArrayList<Transformation>();
				image.getChanel(chanelType).setTransformations(results);
				Transformation result;
				
				for (int i=0; i<totalParts; i++) {
					result = new Transformation();
					results.add(result);
	
					result.setRangePart(i); // all transformations are sorted
					result.setDomainPart(bis.readBits(BITS_DOMAIN_NUM));
					result.setSymetricGroup(SymmetricGroup.factory(bis.readBits(BITS_SYMMETRIC_GROUP)));
					result.setK(Float.intBitsToFloat(bis.readBits(BITS_K_VALUE)));
					result.setR(((short)bis.readBits(BITS_R_VALUE)));
				}
			}
			bis.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			log.error("Image corrupted!");
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		log.debug(image.toString());
		return image;
	}
	
	private void validateWrite(IFSImage image, File file) {
		IFSImage persisted = readFromFile(file);
		try {
			image.validate(persisted);
			log.info("File write validation successful!");
		} catch (ValidationException e) {
			log.error("File write validation failed!");
			e.printStackTrace();
		}
	}
	
}
