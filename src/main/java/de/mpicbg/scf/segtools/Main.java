package de.mpicbg.scf.segtools;

import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import java.io.File;

public class Main {
    public static void main(final String... args) throws Exception {
        // create the ImageJ application context with all available services

        // plugins dir for development: make IJ.run() work
        //String pluginsDir = "/Applications/Fiji/plugins";
        //System.setProperty("plugins.dir", pluginsDir);


        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        // load and show image

        File inputFile=new File("src/main/resources/mri-stack.tif");

        ImagePlus imp= IJ.openImage(inputFile.getPath());
        //imp.show();

        ImagePlus labels=IJ.openImage("src/main/resources/3dlabelled.tif");
        //labels.show();

        // invoke the plugin (IJ2 style)
        //ij.command().run(Mask3DToRoisPlugin.class,true);

        // invoke the plugin (IJ2 style)
        // automatize input for all @Parameters
   /*     Map<String, Object> map = new HashMap<>();
          map.put("segImp", labels);
          map.put("imp", imp);

        ij.command().run(Create3DOverlayPlugin.class, true, map);*/
        ij.command().run(AboutPlugin.class, true);
    }


}
