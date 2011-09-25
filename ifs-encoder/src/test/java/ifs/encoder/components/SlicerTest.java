package ifs.encoder.components;

import ifs.encoder.PartSize;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import junit.framework.Assert;
import junit.framework.TestCase;

@RunWith(PowerMockRunner.class)
public class SlicerTest extends TestCase {
	
	Slicer slicer;
	
	@Before
	public void before() {
		slicer = new Slicer(64,64,PartSize.p32,new boolean[]{
				true, 
					false, 
					false, 
					false,
					true,
						false, 
						false, 
						false,
						true,
							false, 
							false, 
							false,
							false,
				false, 
				false, 
				false
		});
	}
	
	public void testIdentity() {
		Assert.assertTrue(slicer.getBlockCount()==13);
		Assert.assertEquals(slicer.getInitPartSize(),PartSize.p32);
		
		slicer.getCurrent().setPart(0);
		Assert.assertTrue(slicer.getCurrent().sx==0);
		Assert.assertTrue(slicer.getCurrent().sy==0);
		Assert.assertTrue(slicer.getCurrent().partSize==16);

		slicer.getCurrent().setPart(3);
		Assert.assertTrue(slicer.getCurrent().sx==16);
		Assert.assertTrue(slicer.getCurrent().sy==16);
		Assert.assertTrue(slicer.getCurrent().partSize==8);
		
		slicer.getCurrent().setPart(6);
		Assert.assertTrue(slicer.getCurrent().sx==24);
		Assert.assertTrue(slicer.getCurrent().sy==24);
		Assert.assertTrue(slicer.getCurrent().partSize==4);

		slicer.getCurrent().setPart(7);
		Assert.assertTrue(slicer.getCurrent().sx==28);
		Assert.assertTrue(slicer.getCurrent().sy==24);
		Assert.assertTrue(slicer.getCurrent().partSize==4);

		slicer.getCurrent().setPart(8);
		Assert.assertTrue(slicer.getCurrent().sx==24);
		Assert.assertTrue(slicer.getCurrent().sy==28);
		Assert.assertTrue(slicer.getCurrent().partSize==4);
		
		slicer.getCurrent().setPart(9);
		Assert.assertTrue(slicer.getCurrent().sx==28);
		Assert.assertTrue(slicer.getCurrent().sy==28);
		Assert.assertTrue(slicer.getCurrent().partSize==4);
		
		slicer.getCurrent().setPart(10);
		Assert.assertTrue(slicer.getCurrent().sx==32);
		Assert.assertTrue(slicer.getCurrent().sy==0);
		Assert.assertTrue(slicer.getCurrent().partSize==32);
		
		slicer.getCurrent().setPart(11);
		Assert.assertTrue(slicer.getCurrent().sx==0);
		Assert.assertTrue(slicer.getCurrent().sy==32);
		Assert.assertTrue(slicer.getCurrent().partSize==32);
		
		slicer.getCurrent().setPart(12);
		Assert.assertTrue(slicer.getCurrent().sx==32);
		Assert.assertTrue(slicer.getCurrent().sy==32);
		Assert.assertTrue(slicer.getCurrent().partSize==32);
	}
	
}