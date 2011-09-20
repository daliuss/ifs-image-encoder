package ifs.applications;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.border.LineBorder;

import ifs.encoder.ChanelType;
import ifs.encoder.DebugInformation;
import ifs.encoder.IFSImage;
import ifs.encoder.components.Slicer;
import ifs.encoder.services.IFSImageService;
import ifs.encoder.services.RasterImageService;

@SuppressWarnings("serial")
public class QuadraticTreeDebuger extends JFrame {

	private int w, h;
	private ChanelInfo display;

	private DebugInformation info;
	private JComboBox chanelViews;
	private BufferedImage restored;
	private IFSImageService ifsImageService;
	private static final int INIT_WIDHT = 40;
	private static final int INIT_HEIGHT = 100;
	
	private static final int ITERATIONS = 8;
	
	public QuadraticTreeDebuger() {
		display = new ChanelInfo(this);
		
		chanelViews = new JComboBox();
		chanelViews.setBorder(new LineBorder(Color.WHITE,5));
		chanelViews.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Integer selected = ((JComboBox)e.getSource()).getSelectedIndex();
				showChanel(selected);
			}}
		);
		
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(true);
        this.setTitle("Block debug");
        this.add(chanelViews, BorderLayout.NORTH);
		this.add(display, BorderLayout.CENTER);
        this.setVisible(false);
	}
	
	@Override
	public void setSize(int w, int h) {
		this.w = w;
		this.h = h;
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension scrnsize = toolkit.getScreenSize();
		
        Insets insets = this.getInsets();
        super.setSize(w+insets.left+insets.right, h+insets.top+insets.bottom);
        this.setLocation((int)Math.round((scrnsize.getWidth()-this.getWidth())/2), (int)Math.round((scrnsize.getHeight()-this.getHeight())/2));
	}
	
	public void viewData(DebugInformation info) {
		if (this.info != null) return; //immutable!

		this.restored = ifsImageService.decode(info.getEncoded(), ITERATIONS);
		this.info = info;
		
		for(ChanelType ct: info.getEncoded().getImageType().getChanels()) {
			this.chanelViews.addItem(ct.name());
		}
		//showChanel(0);
		
		this.setSize(INIT_WIDHT + info.getOriginal().getWidth()*2, INIT_HEIGHT + info.getOriginal().getHeight()*2);
		this.setVisible(true);
	}
	
	public void showChanel(int index) {
		if (info.getEncoded().getImageType().getChanels() != null) {
			int total = info.getEncoded().getImageType().getChanels().length;
			if (index >= 0 &&  index < total) {
				chanelViews.setSelectedIndex(index);
				showChanel(info.getEncoded().getImageType().getChanels()[index]);
			}
		}
	}
	
	private void showChanel(ChanelType chanelType) {
		IFSImage.Chanel chanel = info.getEncoded().getChanel(chanelType);
		display.setName(chanelType.name());
		display.setOriginal(info.getOriginal());
		display.setChanelType(chanelType);
		display.setChanel(RasterImageService.derived(chanelType.filterChanel(info.getOriginal()),chanelType.getBand()));
		display.setRangeParts(new Slicer(info.getEncoded().getW(), info.getEncoded().getH(), chanel.getRPartSize(), chanel.getQuadraticRangeTree()));
		display.repaint();
	}
	
	public IFSImageService getIfsImageService() {
		return ifsImageService;
	}

	public void setIfsImageService(IFSImageService ifsImageService) {
		this.ifsImageService = ifsImageService;
	}
	
	private class ChanelInfo extends JComponent {
		
		private static final long serialVersionUID = 1L;
		
		QuadraticTreeDebuger ref;
		Graphics2D g2d;
		BufferedImage original, chanel, range, substractions;
		ChanelType chanelType;

		Slicer rangeParts;
		
		private ChanelInfo(QuadraticTreeDebuger ref) {
			this.ref = ref;
			this.setLayout(new BorderLayout());
		}
		
		@Override
	    public void paint(Graphics g){
	    	g2d = (Graphics2D)g;
	    	g2d.setColor(Color.white);
	    	g2d.fillRect(0, 0, ref.w, ref.h);
	    	g2d.translate(5, 5);
	    	g2d.drawImage(chanel, null, 0, 0);
	    	
	    	//range block
	    	g2d.translate(original.getWidth() + 5, 0);
    		if (range!=null) {
		    	g2d.drawImage(range, null, 0, 0);
    		}
    		

	    	//substraction
	    	g2d.translate(-(original.getWidth() + 5), original.getHeight()+5);
    		if (range!=null) {
		    	g2d.drawImage(substractions, null, 0, 0);
    		}
	    }
		
		public void repaint() {
			range = drawBlocs(rangeParts);
			substractions = drawSubstraction();
			super.repaint();
		}
		
		private BufferedImage drawBlocs(Slicer slicer) {
			BufferedImage block = new BufferedImage(slicer.w, slicer.h, RasterImageService.WORKING_IMAGE_TYPE);
			Graphics2D g2d = block.createGraphics();
			g2d.fillRect(0, 0, slicer.w, slicer.h);
			g2d.setColor(Color.BLACK);
			for(int i=0; i<slicer.getBlockCount(); i++) {
				slicer.getCurrent().setPart(i);
				g2d.drawRect(slicer.getCurrent().sx, slicer.getCurrent().sy, slicer.getCurrent().partSize, slicer.getCurrent().partSize);
			}
			return block;
		}
		
		private BufferedImage drawSubstraction() {
			BufferedImage result = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			Raster restored = ref.restored.getRaster();
			Raster original = this.original.getRaster();
			int sub[][] = new int[original.getWidth()][original.getHeight()];
			int max = 0;
			for (int y=0; y<sub[0].length;y++) {
				for (int x=0; x<sub.length;x++) {
					sub[x][y] = Math.abs(
							restored.getSample(x, y, chanelType.getBand()) - original.getSample(x, y, chanelType.getBand())
					);
					if(sub[x][y]>max) {
						max = sub[x][y];
					}
				}
			}
			WritableRaster resultRaster = result.getRaster();
			for (int y=0; y<sub[0].length;y++) {
				for (int x=0; x<sub.length;x++) {
					resultRaster.setSample(x, y, 0, 255 -sub[x][y]);
				}
			}
			Graphics2D g2d = result.createGraphics();
			g2d.setColor(Color.black);
			g2d.drawRect(0, 0, sub.length-1, sub[0].length-1);
			return result;
		}

		public void setOriginal(BufferedImage original) {
			this.original = original;
		}

		public void setChanel(BufferedImage chanel) {
			this.chanel = chanel;
		}

		public void setRangeParts(Slicer rangeParts) {
			this.rangeParts = rangeParts;
		}

		public ChanelType getChanelType() {
			return chanelType;
		}

		public void setChanelType(ChanelType chanelType) {
			this.chanelType = chanelType;
		}
	}
}
