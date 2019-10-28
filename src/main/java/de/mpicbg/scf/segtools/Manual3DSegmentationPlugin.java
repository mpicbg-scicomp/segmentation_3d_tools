package de.mpicbg.scf.segtools;


import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;
import inra.ijpb.plugins.AnalyzeRegions;
import inra.ijpb.plugins.AnalyzeRegions3D;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.awt.Color;


/*
 * Author: Noreen Walker, Scientific Computing Facility, MPI-CBG
 * Date: 2019-10
 */


// notes:
// * interpolation of rois only works for single object (at a time), so splitting of binary mask into connected
//      components is not necessary

/**
 * Plugin to do semi-manual segmentation in 3D (and 2D) by drawing and interpolating ROIs.
 * Plugin creates a mask and optionally computes statistics (the latter requires MorpholibJ).
 */
@Plugin(type = Command.class, menuPath = "Plugins>SegTools>Semi-manual Segmentation (3D)")
public class Manual3DSegmentationPlugin implements Command {

    @Parameter(label = "input image")
    ImagePlus imp;

    @Override
    public void run() {

        if (!checkInput()) return;

        // set up roi manager
        RoiManager rm = RoiManager.getRoiManager();
        rm.reset();
        rm.runCommand("Show All");
        boolean origpref_sliceonly = Prefs.showAllSliceOnly;
        rm.runCommand("Associate","true"); // associate rois' to single slices

        IJ.setTool("freehand");

        // for development only
       /* if (new File("src/main/resources/RoiSet.zip").exists()) {
            rm.runCommand("Open", "src/main/resources/RoiSet.zip");
        }*/

        // dialog
        NonBlockingGenericDialog gd = new NonBlockingGenericDialog("Manual Segmentation (3D)");
        gd.setTitle("Semi-Manual Segmentation (3D)");
        gd.addMessage("Plugin for segmentation of a single 3D object.");
        gd.addMessage("Instructions:\n"+
                "* Draw a Roi of the object outline and add it to the Roi Manager.\n"+
                "* Do this for several slices, including the first & last slice containing the object.\n"+
                "* Interpolate Roi's for the other slices: In Roi manager, right click into the Roi list, choose 'Interpolate ROIs'\n" +
                "* Verify that the outline of the shape is good in all slices, otherwise correct it.\n"+
                "* When finished, press OK.");
        gd.addCheckbox("Add overlay", true);
        gd.addCheckbox("Compute statistics (MorpholibJ)", true);
        gd.showDialog();

        if (gd.wasCanceled()) {
            return;
        }

        boolean addOverlay=gd.getNextBoolean();
        boolean computeStatistics=gd.getNextBoolean();

        // process the ROIs from the roi manager

        // create binary mask
        ImagePlus mask = Conversions.BinaryMaskFromRois(rm, imp);
        mask.show();

        // overlay on original image
        if (addOverlay) {
            createOverlay(rm);
        }


        // statistics (single object, no conncomp labeling done)
        if (computeStatistics) {
            try {
                ResultsTable rt;
                if (mask.getNSlices() > 1) {
                    rt = new AnalyzeRegions3D().process(mask);
                } else {
                    rt = new AnalyzeRegions().process(mask);
                }
                rt.show("results");
                // IJ.run did not work (error in ellipses): IJ.run(mask, "Analyze Regions 3D", "volume surface_area mean_breadth sphericity euler_number bounding_box centroid equivalent_ellipsoid ellipsoid_elongations max._inscribed surface_area_method=[Crofton (13 dirs.)] euler_connectivity=C26");
                // IJ.run for intensities does work. but strange image name (no extension) required: IJ.run("Intensity Measurements 2D/3D", "input=[" + "mri-stack" + "] labels=[" + mask.getTitle() + "] mean stddev max min median numberofvoxels volume");
            } catch (NoClassDefFoundError e) { // catches the AnalyzeRegions(), the IJ.run would be automatically caught with a pop-up window
                IJ.error("MorpholibJ was not found. Please add the IJPB-plugins update site.");
            }
        }

        // restore the original roi manager settings
        rm.runCommand("Associate", String.valueOf(origpref_sliceonly));
        rm.runCommand("Show None");
        rm.deselect();

        IJ.setTool("rectangle");
    }


    /**
     * sanity checks.
     * @return whether checks were successful
     */
    private final boolean checkInput() {
        if (imp.getNChannels()>1 || imp.getNFrames()>1) {
            IJ.error("Image must have a single channel and single time-point.");
            return false;
        }
        return true;
    }


    /**
     * Creates an overlay (in red) of all roi's stored in roi manager, drawn on the respective slices. works in 2D & 3D
     * @param rm
     */
    private void createOverlay(RoiManager rm) {
        int numrois = rm.getCount();

        Overlay ov = new Overlay();

        for (int idx = 0; idx < numrois; idx++) {
            Roi roi = rm.getRoi(idx);
            int slice=roi.getZPosition();
            if (imp.getNSlices()>1 && slice==0) {
                IJ.log("Warning: Roi "+idx+" is not associated to a specific slice. Skipping");
                continue;
            }
            ov.add(roi);
        }
        ov.setStrokeColor(Color.red);

        imp.setOverlay(ov);
    }


}