package ifs.encoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import javax.xml.bind.ValidationException;

public class IFSImage {
	
	public static final Logger log = Logger.getLogger(IFSImage.class);
	
	private Map<ChanelType, Chanel> chanels;
	private int w, h, originalW, originalH;
	protected ImageType imageType;
	
	public IFSImage(int width, int height, ImageType imageType) {
		originalW = width;
		originalH = height;
		setW(width);
		setH(height);
		chanels = new HashMap<ChanelType, Chanel>();
		for(ChanelType chanelType : imageType.getChanels()) {
			chanels.put(chanelType, new Chanel(chanelType));
		}
		this.imageType = imageType;
	}
	
	private IFSImage() {}
	
	public Chanel getChanel(ChanelType chanelType) {
		return chanels.get(chanelType);
	}
	
	public class Chanel {

		private List<Transformation> transformations;
		private ChanelType chanelType;
		private PartSize rPartSize, dPartSize;
		private boolean[] quadraticRangeTree;
		private boolean[] quadraticDomainTree;

		private Chanel(ChanelType chanelType) {
			this.chanelType = chanelType;
		}
		
		public ChanelType getChanelType() {
			return chanelType;
		}

		public List<Transformation> getTransformations() {
			return transformations;
		}

		public void setTransformations(List<Transformation> transformations) {
			this.transformations = transformations;
		}

		public void setRPartSize(PartSize rPartSize) {
			this.rPartSize = rPartSize;
		}
		
		public PartSize getRPartSize() {
			return rPartSize;
		}
		
		public void setDPartSize(PartSize dPartSize) {
			this.dPartSize = dPartSize;
		}
		
		public PartSize getDPartSize() {
			return dPartSize;
		}
		
		public boolean[] getQuadraticRangeTree() {
			return quadraticRangeTree;
		}

		public void setQuadraticRangeTree(boolean[] quadraticRangeTree) {
			this.quadraticRangeTree = quadraticRangeTree;
		}
		
		public boolean[] getQuadraticDomainTree() {
			return quadraticDomainTree;
		}

		public void setQuadraticDomainTree(boolean[] quadraticDomainTree) {
			this.quadraticDomainTree = quadraticDomainTree;
		}

		public void validate(Chanel chanel) throws ValidationException {
			if(!chanelType.equals(chanel.getChanelType())) throw new ValidationException("Validation fail");
			if(!getRPartSize().equals(chanel.getRPartSize())) throw new ValidationException("Validation fail");
			if(!getDPartSize().equals(chanel.getDPartSize())) throw new ValidationException("Validation fail");
			if(getQuadraticDomainTree().length != chanel.getQuadraticDomainTree().length) throw new ValidationException("QT validation fail");
			if(getQuadraticRangeTree().length != chanel.getQuadraticRangeTree().length) throw new ValidationException("QT validation fail");
			for(int i=0; i<getQuadraticDomainTree().length; i++) {
				if(getQuadraticDomainTree()[i]!=chanel.getQuadraticDomainTree()[i])
					 throw new ValidationException("QT validation fail");
			}
			for(int i=0; i<getQuadraticRangeTree().length; i++) {
				if(getQuadraticRangeTree()[i]!=chanel.getQuadraticRangeTree()[i])
					 throw new ValidationException("QT validation fail");
			}
			for (Transformation t: transformations) {
				t.validate(chanel.getTransformations().get(transformations.indexOf(t))); // - Wow Baby, did you see that!
			}
			
		}
		
		public Chanel clone() {
			Chanel chanel = new Chanel(chanelType);
			chanel.setDPartSize(dPartSize);
			chanel.setRPartSize(rPartSize);
			chanel.setQuadraticDomainTree(new boolean[getQuadraticDomainTree().length]);
			chanel.setQuadraticRangeTree(new boolean[getQuadraticRangeTree().length]);
			for(int i=0; i<getQuadraticDomainTree().length; i++) {
				chanel.getQuadraticDomainTree()[i] = getQuadraticDomainTree()[i];
			}
			for(int i=0; i<getQuadraticRangeTree().length; i++) {
				chanel.getQuadraticRangeTree()[i] = getQuadraticRangeTree()[i];
			}
			
			chanel.setTransformations(new ArrayList<Transformation>());
			for(Transformation t: transformations) {
				chanel.getTransformations().add(t.clone());
			}
			return chanel;
		}
	}
	
	public int getW() {
		return w;
	}
	public void setW(int w) {
		this.w = w;
	}
	public int getH() {
		return h;
	}
	public void setH(int h) {
		this.h = h;
	}

	public int getOriginalW() {
		return originalW;
	}

	public int getOriginalH() {
		return originalH;
	}
	
	public ImageType getImageType() {
		return imageType;
	}
	
	public String toString() {
		String ret = "IFSImage["+w+"x"+h+"]{type:"+imageType.name()+":";
		for(ChanelType chanelType : imageType.getChanels()) {
			ret += "{" + chanelType.name();
			ret += ",r:" + this.getChanel(chanelType).getRPartSize();
			ret += ",d:" + this.getChanel(chanelType).getDPartSize();
			ret += ",t:" + this.getChanel(chanelType).getTransformations().size();
			ret += "}";
		}
		ret += "}";
		
		return ret;
		
	}

	public void validate(IFSImage image) throws ValidationException {
		
		if(!imageType.equals(image.getImageType())) throw new ValidationException("Validation fail");
		if(getW() != image.getW()) throw new ValidationException("Validation fail");
		if(getH() != image.getH()) throw new ValidationException("Validation fail");
		for(ChanelType chanelType : imageType.getChanels()) {
			chanels.get(chanelType).validate(image.getChanel(chanelType));
		}
	}
	
	public IFSImage clone() {
		IFSImage image = new IFSImage();
		image.w = w;
		image.h = h;
		image.originalW = originalW;
		image.originalH = originalH;
		image.chanels = new HashMap<ChanelType, Chanel>();
		for(ChanelType chanelType : imageType.getChanels()) {
			image.chanels.put(chanelType, this.getChanel(chanelType).clone());
		}
		image.imageType = imageType;
		try {
			this.validate(image);
		}catch(ValidationException e) {
			log.error("Clone failed:");
			log.error(e.getMessage());
		}
		return image;
	}


}
