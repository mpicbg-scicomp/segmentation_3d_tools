package de.mpicbg.scf.manualseg;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.frame.RoiManager;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import java.io.File;

/**
 * Small plugin to convert a list of Rois in the Roi manager to a 3D binary mask.
 */
@Plugin(type = Command.class, menuPath = "Plugins>SegTools>ROIManager ROIs to Mask (3D)")
public class RoisToMask3DPlugin implements Command {

    // @Parameter did not work since I want the user to actively see/verify the dimensions

    ImagePlus imp;
    int width;
    int height;
    int nslices;

    @Override
    public void run() {

        // get rois
        RoiManager rm = RoiManager.getRoiManager();

        // for development only
       /*if (new File("src/main/resources/RoiSet.zip").exists()) {
            rm.runCommand("Open", "src/main/resources/RoiSet.zip");
        }*/

        if (rm.getCount()==0) {
            IJ.error("Rois must be stored in Roi manager.");
            return;
        }


        // get target dimensions
        imp = IJ.getImage();
        if (imp!=null) {
            width=imp.getWidth();
            height=imp.getHeight();
            nslices=imp.getNSlices();
        }
        else {
            width=0;
            height=0;
            nslices=0;
        }

        GenericDialog gd = new GenericDialog("ROIs to 3D Mask");
        gd.addMessage("Target dimensions of mask.\nDefaults are taken from active image.");
        gd.addNumericField("Width", width, 0);
        gd.addNumericField("Height", height, 0);
        gd.addNumericField("Slices", nslices, 0);
        gd.addCheckbox("Use calibration from active image?", true);
        gd.showDialog();

        if (gd.wasCanceled()) {
            return;
        }

        width= (int) gd.getNextNumber();
        height= (int) gd.getNextNumber();
        nslices=(int) gd.getNextNumber();
        boolean useCalib=gd.getNextBoolean();

        // create binary mask
        ImagePlus mask = Conversions.BinaryMaskFromRois(rm,width,height,nslices);

        if (useCalib && (imp!=null)) {
            mask.setCalibration(imp.getCalibration());
        }
        mask.show();
    }

}