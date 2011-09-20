package ifs.applications;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.border.LineBorder;

import ifs.encoder.ChanelType;
import ifs.encoder.DebugInformation;
import ifs.encoder.IFSImage;
import ifs.encoder.Transformation;
import ifs.encoder.components.Slicer;
import ifs.encoder.components.SlicerMock;
import ifs.encoder.services.IFSImageService;
import ifs.encoder.services.RasterImageService;

@SuppressWarnings({"serial"})
public class IFSProcessDebugerFrame extends JFrame implements MouseWheelListener  {
	
	private int w, h;
	private ChanelInfo display;
	
	private JComboBox chanelViews;
	private IFSImageService ifsImageService;
	private DebugInformation info;

	private static final int INIT_WIDHT = 295;
	private static final int INIT_CONTROLS_HEIGHT = 100;
	private static final int INIT_HEIGHT = 610;
	private static final DecimalFormat DF = new DecimalFormat("0.0000");
	
	public IFSProcessDebugerFrame() {
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
        this.setTitle("Chanel debug");
        this.add(chanelViews, BorderLayout.NORTH);
		this.add(display, BorderLayout.CENTER);
        this.addMouseWheelListener(this);
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
		this.info = info;
		
		for(ChanelType ct: info.getEncoded().getImageType().getChanels()) {
			this.chanelViews.addItem(ct.name());
		}
		
		this.setSize(INIT_WIDHT + info.getOriginal().getWidth(), 
				Math.max(INIT_CONTROLS_HEIGHT + info.getOriginal().getHeight(), INIT_HEIGHT));
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
		display.setOriginal(info.getWorking());
		display.setChanel(chanelType.filterChanel(info.getWorking()));
		display.setChanelView(RasterImageService.tonizated(chanelType.filterChanel(info.getWorking())));
		display.setChanelType(chanelType);
		display.setRangeParts(new Slicer(info.getWorking().getWidth(), info.getWorking().getHeight(), chanel.getRPartSize(), chanel.getQuadraticRangeTree()));
		display.setDomainParts(new Slicer(info.getWorking().getWidth(), info.getWorking().getHeight(), chanel.getDPartSize(), chanel.getQuadraticDomainTree()));
		display.setRangePartsMock(new SlicerMock(info.getWorking().getWidth(), info.getWorking().getHeight(), chanel.getRPartSize(), chanel.getQuadraticRangeTree()));
		display.setTransformations(chanel.getTransformations());
	}

	
	private class ChanelInfo extends JComponent {
		
		private static final long serialVersionUID = 1L;
		
		IFSProcessDebugerFrame ref;
		Graphics2D g2d;
		BufferedImage original, chanel, chanelView, range, domain, transformed, graph, domainExtracted;


		ChanelType chanelType;

		Rectangle rangeBlock, domainBlock;
		Slicer rangeParts, rangePartsMock, domainParts;

		List<Transformation> transformations;
		private JComboBox transformationList;
		
		private ChanelInfo(IFSProcessDebugerFrame ref) {
			this.ref = ref;
			this.setLayout(new BorderLayout());
			
			transformationList = new JComboBox();
			transformationList.setBorder(new LineBorder(Color.WHITE,5));
			transformationList.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					int selected = ((JComboBox)e.getSource()).getSelectedIndex();
					showTransformation(selected);
				}}
			);
			this.add(transformationList, BorderLayout.NORTH);
		}
		
		public void moveToTransformation(int notches) {
			int selected = transformationList.getSelectedIndex();
			selected = selected + notches;
			selected = selected < 0 ? 0 : selected;
			selected = selected > transformations.size()-1 ? transformations.size()-1 : selected;
			showTransformation(selected);
		}
		
		public void setTransformations(List<Transformation> transformations) {
			this.transformations = transformations;
			transformationList.removeAllItems();
			for(Transformation t : transformations) {
				transformationList.addItem("r("
					+t.getRangePart()
					+") ~ d("+t.getDomainPart()+")@"
					+t.getSymetricGroup().name() 
					+ " (" + DF.format(t.getK()) 
					+ ", " + DF.format(t.getR()) +") "
					+ DF.format( t.getSum() ) );
			}
		}
		
		public void showTransformation(int index) {
			if (index >= 0 && transformations!=null && index < transformations.size()) {
				transformationList.setSelectedIndex(index);
				showTransformation(transformations.get(index));
			}
		}

		private void showTransformation(Transformation transformation) {
						
			//set parts
			rangeParts.getCurrent().setPart(transformation.getRangePart());
			rangePartsMock.getCurrent().setPart(transformation.getRangePart());
			domainParts.getCurrent().setPart(transformation.getDomainPart());
			
			//get range sub image
			range = chanel.getSubimage(rangeParts.getCurrent().sx, rangeParts.getCurrent().sy, rangeParts.getCurrent().partSize, rangeParts.getCurrent().partSize);
			//create range locator
			rangeBlock = new Rectangle(rangeParts.getCurrent().sx, rangeParts.getCurrent().sy, rangeParts.getCurrent().partSize, rangeParts.getCurrent().partSize);
			//get domain sub image
			domain = chanel.getSubimage(domainParts.getCurrent().sx, domainParts.getCurrent().sy, domainParts.getCurrent().partSize, domainParts.getCurrent().partSize);
			//create domain locator
			domainBlock = new Rectangle(domainParts.getCurrent().sx, domainParts.getCurrent().sy, domainParts.getCurrent().partSize, domainParts.getCurrent().partSize);
			
			//Clone current transformation and modify for test
			Transformation temp = transformation.clone();
			//Create transformed image by current transformation with value change
			transformed = new BufferedImage(rangeParts.getCurrent().partSize, rangeParts.getCurrent().partSize, RasterImageService.WORKING_IMAGE_TYPE);
			ref.ifsImageService.applyTransformation(temp, chanelType, chanel, transformed, rangePartsMock, domainParts);
			
			//Create transformed image by current transformation without value change
			domainExtracted = new BufferedImage(rangeParts.getCurrent().partSize, rangeParts.getCurrent().partSize, RasterImageService.WORKING_IMAGE_TYPE);
			temp.setK(1);
			temp.setR(0);
			domainExtracted = ref.ifsImageService.applyTransformation(temp, chanelType, chanel, domainExtracted, rangePartsMock, domainParts);
			
			//create histogram graphic
			graph = new BufferedImage(256, 256, RasterImageService.WORKING_IMAGE_TYPE);
			paintGraph(graph, domainExtracted, range, chanelType, transformation);
			
			//zoom created images
			range = RasterImageService.resize(range, 100, 100, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			domain = RasterImageService.resize(domain, 100, 100, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			domainExtracted = RasterImageService.resize(domainExtracted, 100, 100, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			transformed = RasterImageService.resize(transformed, 100, 100, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			
			//lets look how does it look!
			this.repaint();
		}
		
		private void paintGraph(BufferedImage image, BufferedImage from, BufferedImage to, ChanelType chanelType, Transformation transformation) {
			int i=0, pos = 0, pixel=0;
			Graphics2D g2d = image.createGraphics();
			g2d.scale(1, -1);
			g2d.translate(0, -256);
			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, 256, 256);
			g2d.setColor(new Color(0,0,0,0.4f));
			
			for (int y=0; y<to.getHeight(); y++) {
				for (int x=0; x<to.getWidth(); x++) {
					pos = from.getRaster().getSample(x, y, chanelType.getBand());
					pixel = to.getRaster().getSample(x, y, chanelType.getBand());
					g2d.fillOval(pos-2, pixel-2, 5, 5);
				}
			}
			
			//draw graphic
			g2d.setColor(Color.RED);
			for (i=0; i< 256;i++) {
				pixel = transformation.apply(i);
				g2d.drawLine(i, pixel, i, pixel);
			}
		}
		
		@Override
	    public void paint(Graphics g){
	    	g2d = (Graphics2D)g;
	    	g2d.setColor(Color.white);
	    	g2d.fillRect(0, 0, ref.w, ref.h);
	    	g2d.translate(5, 40);
	    	g2d.drawImage(chanelView, null, 0, 0);
	    	
	    	if (rangeBlock!=null) {
	    		g2d.setColor( Color.white );
	    		g2d.drawRect(rangeBlock.x-1, rangeBlock.y-1, rangeBlock.width+1, rangeBlock.height+1);
	    		g2d.setColor( new Color(0,1,0,0.3f) );
	    		g2d.fillRect(rangeBlock.x, rangeBlock.y, rangeBlock.width, rangeBlock.height);
	    	}
	    	if (domainBlock!=null) {
	    		g2d.setColor( Color.white );
	    		g2d.drawRect(domainBlock.x-1, domainBlock.y-1, domainBlock.width+1, domainBlock.height+1);
	    		g2d.setColor( new Color(1,0,0,0.3f) );
	    		g2d.fillRect(domainBlock.x, domainBlock.y, domainBlock.width, domainBlock.height);
	    	}
	    	
	    	//range block
	    	g2d.translate(original.getWidth() + 10, 0);
    		if (range!=null) {
        		g2d.setColor(Color.green);
		    	g2d.fillRect(-5, -5, range.getWidth() + 10, range.getHeight() + 10);
		    	g2d.drawImage(range, null, 0, 0);
    		}
    			    	
    		//domain block
	    	g2d.translate(0, 115);
    		if (domain!=null) {
	    		g2d.setColor(Color.red);
		    	g2d.fillRect(-5, -5, domain.getWidth() + 10, domain.getHeight() + 10);
		    	g2d.drawImage(domain, null, 0, 0);
    		}
    		
    		//domain extracted
    		g2d.translate(115, 0);
    		if (domainExtracted!=null) {
	    		g2d.setColor(new Color(0.7f, 0, 1f));
		    	g2d.fillRect(-5, -5, domainExtracted.getWidth() + 10, domainExtracted.getHeight() + 10);
		    	g2d.drawImage(domainExtracted, null, 0, 0);
    		}
    	   	
    		//transformed block
	    	g2d.translate(0, -115);
    		if (transformed!=null) {
        		g2d.setColor(Color.blue);
        		g2d.fillRect(-5, -5, transformed.getWidth() + 10, transformed.getHeight() + 10);
        		g2d.setColor(Color.black);
        		g2d.fillRect(0, 0, transformed.getWidth(), transformed.getHeight());
		    	g2d.drawImage(transformed, null, 0, 0);
    		}
    		
	    	g2d.translate(-115, 230);
    		if (graph!=null) {
        		g2d.setColor(Color.DARK_GRAY);
		    	g2d.fillRect(-5, -5, graph.getWidth() + 10, graph.getHeight() + 10);
		    	g2d.drawImage(graph, null, 0, 0);
    		}
    		
			this.transformationList.repaint();
	    	
	    }

		public void setOriginal(BufferedImage original) {
			this.original = original;
		}

		public void setChanel(BufferedImage chanel) {
			this.chanel = chanel;
		}

		public void setChanelType(ChanelType chanelType) {
			this.chanelType = chanelType;
		}

		public void setRangeParts(Slicer rangeParts) {
			this.rangeParts = rangeParts;
		}
		
		public void setRangePartsMock(Slicer rangePartsMock) {
			this.rangePartsMock = rangePartsMock;
		}

		public void setDomainParts(Slicer domainParts) {
			this.domainParts = domainParts;
		}
		public void setChanelView(BufferedImage chanelView) {
			this.chanelView = chanelView;
		}
	}

	
	public IFSImageService getIfsImageService() {
		return ifsImageService;
	}

	
	public void setIfsImageService(IFSImageService ifsImageService) {
		this.ifsImageService = ifsImageService;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		this.display.moveToTransformation(notches);
	}

}
