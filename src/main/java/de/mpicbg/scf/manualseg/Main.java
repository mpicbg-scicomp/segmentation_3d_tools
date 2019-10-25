package de.mpicbg.scf.manualseg;

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

        File inputFile=new File("src/main/resources/3dmaskdummy.tif");

        ImagePlus imp= IJ.openImage(inputFile.getPath());
        imp.show();

        // invoke the plugin (IJ2 style)
        ij.command().run(Mask3DToRoisPlugin.class,true);
    }


}
