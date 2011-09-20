package ifs.encoder.components.impl;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import ifs.encoder.Transformation;
import ifs.encoder.components.Slicer;
import ifs.encoder.utils.Profiler;
import ifs.encoder.PartSize;

public class SliceComparatorSoftwareLinear extends SliceComparatorAbstract {
	
	public static final int INIT_CACHE_CAPACITY = 10000;
	Map<Integer, int[]> domainsCatche;
	static int[] sourceHash = new int[]{-1, 0, 1, -1, 2, -1, -1, -1, 3};
	
	public void startSession() {
		super.startSession();
		domainsCatche = new HashMap<Integer, int[]>(INIT_CACHE_CAPACITY);
	}
				
	@Override
	public void compare(Slicer rangeSlicer, Slicer domainSlicer, Transformation result, int band) {
		Profiler.allocation.start();
		
		BufferedImage rangePart = bufferedImageSource[0].getSubimage(rangeSlicer.getCurrent().sx, rangeSlicer.getCurrent().sy, rangeSlicer.getCurrent().partSize, rangeSlicer.getCurrent().partSize);

		int[] rangeData = new int[rangeSlicer.getCurrent().partSize*rangeSlicer.getCurrent().partSize];
		rangeData = rangePart.getRaster().getSamples(0, 0, rangeSlicer.getCurrent().partSize, rangeSlicer.getCurrent().partSize, band, rangeData);

		float[] krs = this.resolveKRS(rangeData, getDomainData(domainSlicer, rangeSlicer.getCurrent().partSize, band));
		
		result.setK(krs[0]);
		result.setR(krs[1]);
		result.setSum(krs[2]);
		result.setSymetricGroup(domainSlicer.getCurrent().getSymetricGroup());
		result.setRangePart(rangeSlicer.getCurrent().currentPart);
		result.setDomainPart(domainSlicer.getCurrent().currentPart);
		Profiler.allocation.end();
	}
	
	/**
	 * 
	 */
	public float[] resolveKRS(int[] rangeData, int[] domainData) {
		Profiler.subtraction.start();
		float k = 0, s = 0;
		int a = 0, b = 0, sumAB=0, sumA = 0, sumB = 0, sumApow2 = 0, r = 0;
		double n = rangeData.length; //total pixel in range block
		double nInv = 1d/n;
		double nsumAB=0, n2sumAsumB=0, nsumApow2=0, n2sumA$pow2=0;
			
		//start calculating k r elements
		for (int i=0; i<rangeData.length; i++) {
			a = domainData[i];
			b = rangeData[i];
			sumAB += a * b;
			sumA += a;
			sumB += b;
			sumApow2 += a * a;
		}
		nsumAB = n * sumAB;
		n2sumAsumB = sumA * sumB;
		nsumApow2 = n * sumApow2;
		n2sumA$pow2 = sumA * sumA;
		//end calculating elements
		
		k = (float)((nsumAB-n2sumAsumB)/(nsumApow2-n2sumA$pow2+1));
		r = (int) Math.round(nInv * (sumB - k*sumA)); // true pixel value
		
		//start calculating s element
		for (int i=0; i<rangeData.length; i++) {
			a = domainData[i];
			b = rangeData[i];
			s += Math.abs(k*a + r - b);
		}

		Profiler.subtraction.end();
		return new float[]{k, r, s};
	}
	
	/**
	 * Hash key calculation.
	 * @param slicer
	 * @param band
	 * @return
	 */
	
	public Integer getKey(Slicer slicer, int rPartSize, int band) {
		return new Integer(slicer.getCurrent().currentPart*96 + 
				PartSize.getIdBySize(rPartSize)*24 + 
				slicer.getCurrent().getSymetricGroup().getId()*3 + band);
	}
	
	public int getIndex(int domain, int range) {
		int pow = domain / range;
		return sourceHash[pow];
	}
	
	public int[] getDomainData(Slicer domain, int rPartSize, int band) {
		Integer key = getKey(domain, rPartSize, band);
		if (domainsCatche.get(key)==null) {
			int[] domainData = new int[rPartSize*rPartSize];
			
			int pow = domain.getCurrent().partSize / rPartSize;
			
			BufferedImage domainPart = domain.getCurrent().getSymetricGroup().filter(
					bufferedImageSource[sourceHash[pow]].getSubimage(domain.getCurrent().sx/pow, domain.getCurrent().sy/pow, rPartSize, rPartSize), 1);
			domainPart.getRaster().getSamples(0, 0, rPartSize, rPartSize, band, domainData);
			domainsCatche.put(key, domainData);
		}
		return domainsCatche.get(key);
	}
	
	
}
