package ifs.encoder.utils;

/**
 * This enum is for profiling encoding job parts.
 * @author Dalius
 */

public enum Profiler {
	allocation, 
	subtraction,
	encode;
	
	private long elapsed;
	private long start;
	
	static public void resetAll() {
		for (Profiler p : Profiler.values()) {
			p.reset();
		}
	}
	
	static public void subtract(Profiler a, Profiler b) {
		a.elapsed -= b.elapsed;
	}
	
	public void reset() {
		this.elapsed = 0;
	}
	
	public void start() {
		start = System.currentTimeMillis();
	}
	
	public void end() {
		elapsed += (System.currentTimeMillis() - start);
		start = 0;
	}
	
	public long getElapsed() {
		return elapsed;
	}

}
