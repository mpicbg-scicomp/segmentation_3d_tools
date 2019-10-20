package de.mpicbg.scf.manualseg;

import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
        imp.show();

        // invoke the plugin (IJ2 style)
        ij.command().run(RoisToMask3D.class,true);
    }


}
