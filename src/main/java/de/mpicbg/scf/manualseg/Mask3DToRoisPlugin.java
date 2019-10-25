package de.mpicbg.scf.manualseg;

import ij.ImagePlus;
import ij.plugin.frame.RoiManager;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Small plugin to convert a 3D binary mask to a list of Rois in the Roi manager.
 * Image threshold is taken at 1 (relevant if image is not binary, for example a label image)
 */
@Plugin(type = Command.class, menuPath = "Plugins>SegTools>Mask (3D) to ROIManager ROIs")
public class Mask3DToRoisPlugin implements Command {

    @Parameter(label = "mask image")
    ImagePlus imp;

    @Override
    public void run() {

        //  empty roi manager
        RoiManager rm = RoiManager.getRoiManager();
        rm.reset();

        // fill roi manager
        Conversions.RoisFromBinaryMask(rm, imp);
    }
}
