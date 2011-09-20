package ifs.applications;

import java.io.File;
import java.util.Arrays;

import org.apache.log4j.Logger;

import ifs.encoder.components.impl.BlockSlicerStrategyDynamic;
import ifs.encoder.components.impl.SliceComparatorSoftwareLinear;
import ifs.encoder.services.IFSImageService;
import ifs.encoder.services.RasterImageService;

public class Main {
	
	public static final Logger log = Logger.getLogger(Main.class);
	
	static String filesPath = "files/";
	static boolean debug = false;
	
	static public void main(String[] args) {
		
		if (args.length > 0) {
			log.info("Params bound:");
			log.info(Arrays.toString(args));
			if (args[0]!=null) {
				filesPath = args[0];
				log.info("Document path: " + (new File(filesPath)).getAbsolutePath());
			}
			if (args.length > 1 && args[1]!=null) {
				if ("true".equals(args[1].toLowerCase())) {
					debug = true;
				}
				if ("false".equals(args[1].toLowerCase())) {
					debug = false;
				}
			}
		}
		initializeObjects();
	}
	
	static void initializeObjects() {
		MainApplicationController menu = new MainApplicationController();
		menu.setInitImage(new File(filesPath + "init.png"));
		menu.setRasterImageService(new RasterImageService());
		menu.setIfsImageService(new IFSImageService());
		menu.getIfsImageService().setBlockSlicerStrategy(new BlockSlicerStrategyDynamic());
		menu.getIfsImageService().setSliceComparator(new SliceComparatorSoftwareLinear());
		menu.display();
	}
}
