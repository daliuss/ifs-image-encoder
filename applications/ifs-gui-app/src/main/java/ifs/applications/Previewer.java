package ifs.applications;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import org.apache.log4j.Logger;

import ifs.encoder.IFSImage;
import ifs.encoder.services.IFSImageService;


public class Previewer extends JFrame implements KeyListener {
	
	public static final Logger log = Logger.getLogger(Previewer.class);

	private static final long serialVersionUID = 1L;
	private View view;
	static public Previewer instance;
	private int w, h;
	private BufferedImage output;
	private int iterations = 0;
	private IFSImageService ifsImageService;
	private IFSImage ifsImage;


	public Previewer(int w, int h, String title) {
		this.setVisible(false);
		instance = this;
		this.w = w;
		this.h = h;
		
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension scrnsize = toolkit.getScreenSize();
		view = new View(this);
		
        this.setSize(w, h);
        this.setVisible(true);
        this.addKeyListener(this);
        Insets insets = this.getInsets();
        this.setSize(w+insets.left+insets.right, h+insets.top+insets.bottom);
        this.setLocation((int)Math.round((scrnsize.getWidth()-this.getWidth())/2), (int)Math.round((scrnsize.getHeight()-this.getHeight())/2));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(true);
        this.setTitle(title);
        this.add(view);
	}
	
	public void display() {
		log.info("Rendering with iterations: " + iterations);
		this.output = ifsImageService.decode(ifsImage, iterations);
		this.view.repaint();
	}	
	
	private class View extends JComponent {
		
		private static final long serialVersionUID = 1L;
		
		Previewer ref;
		Graphics2D g2d;
		private View(Previewer ref) {
			this.ref = ref;
		}
		@Override
	    public void paint(Graphics g){
	    	g2d = (Graphics2D)g;
	    	g2d.setColor(Color.white);
	    	g2d.fillRect(0, 0, ref.w, ref.h);
	    	g2d.drawImage(output, null, 0, 0);
	    }
	}

	@Override
	public void keyTyped(KeyEvent event) {
		int key = event.getKeyChar() & 0xFF;
		if (key > 47 && key < 58) {
			if (key - 48 > 8) return;
			this.iterations  = (int)key - 48;
			this.display();
		} else if (key == 'z') {
			if (this.iterations == 50) return;
			this.iterations++;
			this.display();
		} else if (key == 'x') {
			if (this.iterations == 0) return;
			this.iterations--;
			this.display();
		}
	}
	
	@Override
	public void keyPressed(KeyEvent event) {}
	
	@Override
	public void keyReleased(KeyEvent event) {}


	public IFSImageService getIfsImageService() {
		return ifsImageService;
	}

	public void setIfsImageService(IFSImageService ifsImageService) {
		this.ifsImageService = ifsImageService;
	}

	public IFSImage getIfsImage() {
		return ifsImage;
	}

	public void setIfsImage(IFSImage ifsImage) {
		this.ifsImage = ifsImage;
	}

}
