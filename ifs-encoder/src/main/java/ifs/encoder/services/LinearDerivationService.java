package ifs.encoder.services;


public class LinearDerivationService {
	
    private double[] source, result, diff1, diff2;
    private double q, qd;
    private int sourceLength, sourceLastIndex;
    private double[] v0p, v0n, v1p, v1n;
    private double[] qs;
    private double[] ab0 = new double[2], ab1 = new double[2], ab2 = new double[2];
    
    public LinearDerivationService(double sigma) {
    	this.q = Math.exp((double)(-1d/(double)sigma));
        this.qs = new double[4];
        this.qd = this.q/((1-this.q)*(1-this.q));//optimization
    	this.qs[0]=(1d+this.q)/(1d-this.q);
    	this.qs[1]=2d*this.q/Math.pow(1d-this.q,2);
    	this.qs[2]=2d*this.q*(1d+this.q)/Math.pow(1d-this.q,3);
    	this.qs[3]=2d*this.q*(1d+4*this.q+this.q*this.q)/Math.pow(1d-this.q,4);
    }
    
    public LinearDerivationService process(double[] source) {
    	this.process(source, true);
    	return this;
    }

    public void process(double[] source, boolean shorten){
    	this.source = source;
        this.sourceLength = source.length;
        this.sourceLastIndex = this.sourceLength-1;

        this.v0p= new double[this.sourceLength];
        this.v0n= new double[this.sourceLength];
        this.v1p= new double[this.sourceLength];
        this.v1n= new double[this.sourceLength];
        
        //result
        int j = 0;
        for (int i = 0; i < this.sourceLength; i++){
        	j = this.sourceLastIndex-i;
        	this.v0p[i] = this.calc0p(i, this.source);
        	this.v0n[j] = this.calc0n(j, this.source);
        	this.v1p[i] = this.calc1p(i, this.source);
        	this.v1n[j] = this.calc1n(j, this.source);
        }
        
        this.ab0 = solve2(new double[][] {{qs[0], qs[1]},{qs[2],qs[3]}}, new double[] {1,0});
        this.result = new double[this.sourceLength];
        
        for (int i=0; i<this.sourceLength; i++) {
        	this.result[i] = this.ab0[0] * (this.v0p[i] + this.v0n[i] - this.source[i]) + this.ab0[1] * (this.v1p[i] + this.v1n[i]);
        }
        
        if (shorten) return;
        //diff 1, diff2
        
        for (int i = 0; i < this.sourceLength; i++){
        	j = this.sourceLastIndex-i;
        	this.v0p[i] = this.calc0p(i, this.result);
        	this.v0n[j] = this.calc0n(j, this.result);
        	this.v1p[i] = this.calc1p(i, this.result);
        	this.v1n[j] = this.calc1n(j, this.result);
        }

		this.ab1 = solve2(new double[][]{{qs[2], 0},{0, qs[2]}}, new double[]{-1, 1});
		this.ab2 = solve2(new double[][]{{qs[0], qs[1]},{qs[2], qs[3]}}, new double[]{0, 2});
		//modification
		this.ab0[0] = this.ab2[0]/(double)(ab2[0]*qs[0]-ab2[1]*qs[1]);
		this.ab0[1] = -this.ab2[1]/(double)(ab2[0]*qs[0]-ab2[1]*qs[1]);
		
        this.diff1 = new double[this.sourceLength];
        this.diff2 = new double[this.sourceLength];
        
        for (int i=0; i<this.sourceLength; i++) {
        	this.diff1[i] = this.ab1[0] * (this.v1p[i]) + this.ab1[1] * (this.v1n[i]);
        	this.diff2[i] = this.ab2[0] * (this.v0p[i] + this.v0n[i] - this.result[i]) + this.ab2[1] * (this.v1p[i] + this.v1n[i]);
        }
        
    }
    
    public double calc0p(int i, double[] source) {
        if (i != 0) {
            return source[i] + this.q*this.v0p[i-1];
        }
    	return 1/(1-this.q) * source[0];
    }
    public double calc0n(int i, double[] source) {
        if (i == this.sourceLastIndex) {
        	return 1/(1-this.q) * source[this.sourceLastIndex];
        }
        return source[i] + this.q*this.v0n[i+1];
    }
    public double calc1p(int i, double[] source) {
        if (i != 0) {
            return this.q*(this.v1p[i-1] + this.v0p[i-1]); 
        }
    	return source[0] * this.qd;
    }
    public double calc1n(int i, double[] source) {
        if (i == this.sourceLastIndex) {
        	return source[this.sourceLastIndex] * this.qd;
        }
        return this.q*(this.v1n[i+1] + this.v0n[i+1]); 
    }
    
	static public double[] solve2 (double a[][], double b[]) {
		double det = a[0][0]*a[1][1] - a[0][1]*a[1][0];
		return new double[] {(b[0]*a[1][1] - a[0][1]*b[1])/det, (b[1]*a[0][0] - a[1][0]*b[0])/det}; 
	}
    
    public double[] getResult() {
        return this.result;
    }

    public double[] getDiff1() {
        return this.diff1;
    }
    
    public double[] getDiff2() {
        return this.diff2;
    }
}
