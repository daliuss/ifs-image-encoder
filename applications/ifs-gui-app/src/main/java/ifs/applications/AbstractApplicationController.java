package ifs.applications;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ifs.encoder.DebugInformation;
import ifs.encoder.IFSImage;
import ifs.encoder.PartSize;
import ifs.encoder.RasterImage;
import ifs.encoder.services.IFSImageService;
import ifs.encoder.services.RasterImageService;

@SuppressWarnings("serial")
public abstract class AbstractApplicationController extends JFrame {
	
	static public final int containerH = 250;
	static public final int containerW = 350;
	static private final JFileChooser FILE_CHOOSER = new JFileChooser();
	
	protected JButton openButton, encodeButton, saveButton, openIFSButton, displayButton, debugChanelButton, debugPartsButton;
	protected JProgressBar progressBar;
	protected JCheckBox debugInd;
	protected JComboBox rangeSize;
	
	private File initImage;
	
	//Entity Keeper
	private RasterImage ratserImage;
	private IFSImage ifsImage;
	private DebugInformation debugInformation;

	private RasterImageService rasterImageService;
	private IFSImageService ifsImageService;
	
	public AbstractApplicationController() {
		this.setVisible(false);
        
        FILE_CHOOSER.setSelectedFile(new File(System.getProperty("user.dir")));
		
        this.setLayout(new BorderLayout());
        this.setSize(containerW, containerH);
        this.setVisible(true);
        Insets insets = this.getInsets();
        this.setSize(containerW+insets.left+insets.right, containerH+insets.top+insets.bottom);
        this.setLocation(75, 75);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("IFS");
        //this.setResizable(false);
        this.add(createMenu());
        initMenu();
	}
	
	private JComponent createMenu() {
		

		JPanel encodePanel = new JPanel();
		encodePanel.setBorder(BorderFactory.createTitledBorder("Encoder"));
				
		JPanel decodePanel = new JPanel();
		decodePanel.setBorder(BorderFactory.createTitledBorder("Decoder"));
		
		openButton = new JButton("Open Image");
		
		progressBar = new JProgressBar(0, 1);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		
		encodeButton = new JButton("Encode");
		encodeButton.setEnabled(false);
		
		debugInd = new JCheckBox("Gather Debug Info");
		debugInd.setSelected(true);
		
		rangeSize = new JComboBox();
		for(PartSize rs : PartSize.values()) {
			if(rs.ordinal() > 0) {
				rangeSize.addItem(rs.getSize() + "x" + rs.getSize());
			}
		}
		
		saveButton = new JButton("Save As");
		saveButton.setEnabled(false);
		
		openIFSButton = new JButton("Open encoded");
		
		displayButton = new JButton("Display");
		
		debugChanelButton = new JButton("Debug Chanels");
		debugChanelButton.setEnabled(false);
		
		debugPartsButton = new JButton("Debug Parts");
		debugPartsButton.setEnabled(false);
		
		
		displayButton.setEnabled(false);

		JPanel menu = new JPanel();
		menu.setLayout(new GridLayout());
		menu.add(encodePanel);
		menu.add(decodePanel);
		
		encodePanel.add(openButton);
		encodePanel.add(rangeSize);
		encodePanel.add(debugInd);
		encodePanel.add(progressBar);
		encodePanel.add(encodeButton);
		encodePanel.add(saveButton);
		
		decodePanel.add(openIFSButton);
		decodePanel.add(displayButton);
		decodePanel.add(debugChanelButton);
		decodePanel.add(debugPartsButton);
		return menu;
	}
	
	abstract void initMenu();
	
	public static JFileChooser getFileChooser() {
		return FILE_CHOOSER;
	}	
	
	public RasterImage getRatserImage() {
		return ratserImage;
	}

	public void setRatserImage(RasterImage ratserImage) {
		this.ratserImage = ratserImage;
	}

	public IFSImage getIfsImage() {
		return ifsImage;
	}

	public void setIfsImage(IFSImage ifsImage) {
		this.ifsImage = ifsImage;
	}
	
	public File getInitImage() {
		return initImage;
	}

	public void setInitImage(File initImage) {
		this.initImage = initImage;
	}

	public RasterImageService getRasterImageService() {
		return rasterImageService;
	}

	public void setRasterImageService(RasterImageService rasterImageService) {
		this.rasterImageService = rasterImageService;
	}

	public IFSImageService getIfsImageService() {
		return ifsImageService;
	}

	public void setIfsImageService(IFSImageService ifsImageService) {
		this.ifsImageService = ifsImageService;
	}
	
	public void setDebugInformation(DebugInformation debugInformation) {
		this.debugInformation = debugInformation;
	}

	public DebugInformation getDebugInformation() {
		return debugInformation;
	}

	static public class Encoder extends Thread {

		static private Encoder instance;
		private IFSImageService ifsImageService;
		private RasterImage image;
		private DebugInformation info;
		
		static public void encode(RasterImage image, DebugInformation info, IFSImageService ifsImageService) {
			instance = new Encoder(image, ifsImageService, info);
			instance.start();
		}
		
		public Encoder(RasterImage image, IFSImageService ifsImageService, DebugInformation info) {
			this.image = image;
			this.ifsImageService = ifsImageService;
			this.info = info;
		}
		
		public void run(){
			IFSImage ifsImage = ifsImageService.encode(image, info);
			MainApplicationController.finishEncoding(ifsImage, info);
	    }
	}
}
