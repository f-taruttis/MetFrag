package de.ipbhalle.metfrag.addDatabasIDs;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.smiles.SmilesParser;



//draws some nice pictures of the molecules
public class drawMolecules {
	
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

	public static void main(String[] args) throws Exception {
	
		BufferedReader in = new BufferedReader(new FileReader("/home/ftarutti/ChemFrag/Meeting/Fragments.txt"));
		String line="";
		
		while( (line=in.readLine())!=null)
		{
		
		
		String smiles =line;
		String picOutput = "/home/ftarutti/ChemFrag/Meeting/pics/";
		
		String picNam = smiles+".png";
		
		drawMolecule(smiles, picOutput, picNam);
		
		}
		
	}
	
}
