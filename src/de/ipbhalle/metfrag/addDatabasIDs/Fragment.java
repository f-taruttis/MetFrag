package de.ipbhalle.metfrag.addDatabasIDs;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import de.ipbhalle.metfrag.fragmenter.SubMolecule;
import de.ipbhalle.metfrag.fragmenter.SubstructureGenerator;
import de.ipbhalle.metfrag.massbankParser.Peak;
import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;


public class Fragment {
	
	public static void main(String[] args) throws CDKException, Exception {
		
		WrapperSpectrum spec = new WrapperSpectrum("/home/ftarutti/testspectra/testPB/PB000125.txt");
		
		
		Vector<Peak> peakList = new Vector<Peak>();
		
		peakList = spec.getPeakList();
		
		String smiles = "C1C(OC2=CC(=CC(=C2C1=O)O)O)C3=CC=C(C=C3)O";
		SubstructureGenerator sub = new SubstructureGenerator(smiles, peakList);
		
		Map<IAtomContainer, SubMolecule> result = new HashMap<IAtomContainer, SubMolecule>();
		
		result = sub.getUniqueSubstructures2();
		
		Set<IAtomContainer> keys = new HashSet<IAtomContainer>();
		keys = result.keySet();
		int i=0;
		for (IAtomContainer iAtomContainer : keys) {
			
			IAtomContainer mol = AtomContainerManipulator.removeHydrogens(iAtomContainer);
			
			String smilesSub = sub.getSmiles(mol);
			System.out.println(smilesSub);
			drawMolecule(smilesSub,"/home/ftarutti/Talk-Poster-Paper/Progress2011/subs/","sub"+i);
			i++;
		}
		
	}

	public static void drawMolecule( String smiles,String picOutput,String picNam) throws Exception
	{
		
		int WIDTH = 300;
		int HEIGHT = 300;

		// the draw area and the image should be the same size
		Rectangle drawArea = new Rectangle(WIDTH, HEIGHT);
		Image image = new BufferedImage(
		  WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB
		);
		
		SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        //String smiles = "c1cc(CC=CC#N)ccn1";
        IMolecule molecule = smilesParser.parseSmiles(smiles);
        
        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        sdg.setMolecule(molecule);
        sdg.generateCoordinates();
        molecule=sdg.getMolecule();

        
     // generators make the image elements
        List generators = new ArrayList();
        generators.add(new BasicSceneGenerator());
        generators.add(new BasicBondGenerator());
        generators.add(new BasicAtomGenerator());

        // the renderer needs to have a toolkit-specific font manager
        AtomContainerRenderer renderer =
          new AtomContainerRenderer(generators, new AWTFontManager());

        // the call to 'setup' only needs to be done on the first paint
        renderer.setup(molecule, drawArea);

        // paint the background
        Graphics2D g2 = (Graphics2D)image.getGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        // the paint method also needs a toolkit-specific renderer
        renderer.paint(molecule, new AWTDrawVisitor(g2));

        ImageIO.write((RenderedImage)image, "PNG", new File(picOutput+picNam));

	}

	
}
