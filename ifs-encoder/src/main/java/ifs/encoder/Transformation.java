package ifs.encoder;

import javax.xml.bind.ValidationException;


public class Transformation implements Cloneable {
	private int rangePart, domainPart;

	private float k, r;
	private SymmetricGroup symetricGroup;
	private float sum; //transient
	
	public boolean isBetterThan(Transformation compareResult) {
		return (compareResult == null || this.sum < compareResult.getSum());
	}
	
	public boolean equalsTo(Transformation compareResult) {
		if (rangePart != compareResult.getRangePart()) return false;
		if (domainPart != compareResult.getDomainPart()) return false;
		if (k != compareResult.getK()) return false;
		if (r != compareResult.getR()) return false;
		if (sum != compareResult.getSum()) return false;
		if (!symetricGroup.equals(compareResult.getSymetricGroup())) return false;
		return true;
	}
	
	public int getRangePart() {
		return rangePart;
	}
	
	public void setRangePart(int rPart) {
		this.rangePart = rPart;
	}
	
	public int getDomainPart() {
		return domainPart;
	}
	
	public void setDomainPart(int dPart) {
		this.domainPart = dPart;
	}
	
	public SymmetricGroup getSymetricGroup() {
		return symetricGroup;
	}
	
	public void setSymetricGroup(SymmetricGroup symetricGroup) {
		this.symetricGroup = symetricGroup;
	}
	
	public float getSum() {
		return sum;
	}
	
	public void setSum(float sum) {
		this.sum = sum;
	}
	
	public float getK() {
		return k;
	}

	public void setK(float k) {
		this.k = k;
	}

	public float getR() {
		return r;
	}

	public void setR(float r) {
		this.r = r;
	}
	
	public Transformation clone() {
		Transformation clone =  new Transformation();
		clone.setDomainPart(domainPart);
		clone.setRangePart(rangePart);
		clone.setK(k);
		clone.setR(r);
		clone.setSymetricGroup(symetricGroup);
		clone.setSum(sum);
		return clone;
	}
	
	@Override
	public String toString() {
		return "{" + rangePart + ":" + domainPart + ":" + symetricGroup.name() + "}";
	}

	public void validate(Transformation transformation) throws ValidationException {
		if (rangePart != transformation.getRangePart()) throw new ValidationException("Validation fail");
		if (domainPart != transformation.getDomainPart()) throw new ValidationException("Validation fail");
		if (!symetricGroup.equals(transformation.getSymetricGroup())) throw new ValidationException("Validation fail");
		if (k != transformation.getK()) throw new ValidationException("Validation fail");
		if (r != transformation.getR()) throw new ValidationException("Validation fail");
	}

	public int apply(int i) {
		return Math.round(((float)i * k + r));
	}
}