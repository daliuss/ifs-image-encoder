package ifs.applications;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import org.apache.log4j.Logger;

import ifs.encoder.DebugInformation;
import ifs.encoder.IFSImage;
import ifs.encoder.PartSize;


public class MainApplicationController extends AbstractApplicationController {
	
	public static final Logger log = Logger.getLogger(Main.class);
	private static final long serialVersionUID = 1L;
		
	static private MainApplicationController instance;

	public MainApplicationController() {
		super();
		instance = this;
	}
	
	public void display() {
        (new Sincronizer()).start();
        this.setVisible(true);
	}

	public void initMenu() {
		
		openButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				instance.setVisible(false);
				selectImageAndLoad();
			}
		});		
		encodeButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				encodeImage();
			}
		});
		rangeSize.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
			}}
		);
		saveButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				instance.setVisible(false);
				saveEncoded();
			}
		});
		openIFSButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				instance.setVisible(false);
				selectEncodedAndLoad();
			}
		});
		displayButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				displayEncoded();
			}
		});

		debugChanelButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				debugChanels();
			}
		});

		debugPartsButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				debugParts();
			}
		});
	}
	
	/**
	 * Selects image from image file
	 */
	
	static public void selectImageAndLoad() {
		getFileChooser().showOpenDialog(instance);
		File imageFile = getFileChooser().getSelectedFile();
		instance.setVisible(true);
		if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) return;
		try {
			instance.setRatserImage(instance.getRasterImageService().factoryFromFile(imageFile));
			instance.encodeButton.setEnabled(true);
			instance.progressBar.setValue(0);
			instance.progressBar.setMaximum(1);
			instance.progressBar.setString(instance.getRatserImage().getOriginalName());
		}catch(Exception e) {
			log.error("Not an image file!");
		}
	}
	
	/**
	 * Opens IFS image and loads as current
	 */

	static public void selectEncodedAndLoad() {
		getFileChooser().showOpenDialog(instance);
		File encodedFile = getFileChooser().getSelectedFile();
		instance.setVisible(true);
		if (encodedFile == null || !encodedFile.exists() || !encodedFile.isFile()) return;
		IFSImage image = instance.getIfsImageService().readFromFile(encodedFile);
		if (image == null) return;
		instance.setIfsImage(image);
		instance.displayButton.setEnabled(true);
	}
	
	/**
	 * Displays current IFS image
	 */
	
	static public void displayEncoded() {
		
		String title = "";
		if (getFileChooser().getSelectedFile()!=null) {
			title = getFileChooser().getSelectedFile().getName();
			title += " [" + instance.getIfsImage().getW() + " x " + instance.getIfsImage().getH() + "]";
		}
		Previewer previewer = new Previewer(instance.getIfsImage().getOriginalW(), instance.getIfsImage().getOriginalH(), title);
		previewer.setIfsImage(instance.getIfsImage());
		previewer.setIfsImageService(instance.getIfsImageService());
		try {
			instance.getIfsImageService().setInitImage(
					instance.getRasterImageService().loadFromFile(instance.getInitImage()));
		}catch(Exception e) {
			//e.printStackTrace();
			log.error("Init image not loaded!");
		}
		
		previewer.display();
	}
	
	/**
	 * Displays current debug information
	 */
	
	static public void debugChanels() {
		IFSProcessDebugerFrame debuger = new IFSProcessDebugerFrame();
		debuger.setIfsImageService(instance.getIfsImageService());
		debuger.viewData(instance.getDebugInformation());
	}
	
	
	/**
	 * Displays current debug information
	 */
	
	static public void debugParts() {
		QuadraticTreeDebuger debuger = new QuadraticTreeDebuger();
		debuger.setIfsImageService(instance.getIfsImageService());
		debuger.viewData(instance.getDebugInformation());
	}
	
	/**
	 * Saves current IFS image to binary format file
	 */
	
	static public void saveEncoded() {		
		getFileChooser().setSelectedFile(null);
		int result = getFileChooser().showSaveDialog(instance);
		File encodedFile = getFileChooser().getSelectedFile();
		if(result != JFileChooser.APPROVE_OPTION) return;
		String path = encodedFile.getAbsolutePath();
		if (path!=null) {
			path = path.substring(0, path.lastIndexOf("."));
			path = path.concat(".ifsm");
			encodedFile = new File(path);
		}
		instance.setVisible(true);
		if (encodedFile == null || instance.getIfsImage()==null) return;
		instance.getIfsImageService().writeToFile(instance.getIfsImage(), encodedFile);
		log.info("Saved as: " + encodedFile.getAbsolutePath());
	}
	
	/**
	 * Starts encoding IFS image from current image file
	 */
	
	static public void encodeImage() {
		int selected = instance.rangeSize.getSelectedIndex();
		instance.getIfsImageService().getBlockSlicerStrategy().setRangePart(PartSize.values()[selected+1]);
		
		instance.progressBar.setVisible(true);
		instance.encodeButton.setEnabled(false);
		instance.openButton.setEnabled(false);
		instance.debugInd.setEnabled(false);
		instance.displayButton.setEnabled(false);
		instance.debugChanelButton.setEnabled(false);
		instance.debugPartsButton.setEnabled(false);
		instance.rangeSize.setEnabled(false);
		instance.saveButton.setEnabled(false);
		if(instance.debugInd.isSelected()) {
			Encoder.encode(instance.getRatserImage(), new DebugInformation(), instance.getIfsImageService());
		}else{
			Encoder.encode(instance.getRatserImage(), null, instance.getIfsImageService());
		}
	}
	
	/**
	 * Finish encoding of IFS image. Sets current FIS image and debug information.
	 * @param ifsImage
	 */
	
	static public void finishEncoding(IFSImage ifsImage, DebugInformation info) {
		instance.setIfsImage(ifsImage);
		instance.setDebugInformation(info);
		instance.openButton.setEnabled(true);
		instance.debugInd.setEnabled(true);
		instance.rangeSize.setEnabled(true);
		instance.encodeButton.setEnabled(true);
		
		if (instance.getIfsImage()!=null) {
			instance.displayButton.setEnabled(true);
			instance.saveButton.setEnabled(true);
		} else {
			instance.displayButton.setEnabled(false);
			instance.saveButton.setEnabled(false);
		}
		if (instance.getDebugInformation()!=null) {
			instance.debugChanelButton.setEnabled(true);
			instance.debugPartsButton.setEnabled(true);
		} else {
			instance.debugChanelButton.setEnabled(false);
			instance.debugPartsButton.setEnabled(false);
		}
	}
	
	/**
	 * Static process synchronization service
	 * @author Dalius
	 */
	
	static public class Sincronizer extends Thread {

		private int sleep = 40;
		static int total, current;
		static boolean was = false;
		
	    public void run(){
	        while(true){
	            try{
	            	Thread.sleep(this.sleep);
	            }catch(InterruptedException e){}
	            sync();
	        }
	    }
	    
	    static private void sync() {
	    	if (instance.getIfsImageService().getStatus().isEncoding()) {
	    		updateStatus();
	    		instance.progressBar.setString(current + "/" + total);
	    		was = true;
	    	} else if (was && !instance.getIfsImageService().getStatus().isEncoding()) {
	    		updateStatus();
	    		instance.progressBar.setString("Finished at: " + current + "/" + total);
	    	}
	    }
	    
	    static private void updateStatus() {
    		total = instance.getIfsImageService().getStatus().getTotalSteps();
    		current = instance.getIfsImageService().getStatus().getCurrentStep();
    		if (total > 0) {
    			instance.progressBar.setMaximum(total);
    		}
    		if (current > 0) {
    			instance.progressBar.setValue(current);
    		}
	    }
	}
}
