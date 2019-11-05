package de.mpicbg.scf.segtools;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import inra.ijpb.label.LabelImages;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.awt.*;
import java.util.Arrays;

/*
 * Author: Noreen Walker, Scientific Computing Facility, MPI-CBG
 * Date: 2019-10
 */


/**
 * Small plugin to create overlays of 3D segmentation (binary mask or labelled regions) onto a grayscale image.
 * If grayscale image has multiple frames or channels, then same overlay is added to all frames or channels.
 */
@Plugin(type = Command.class, menuPath = "Plugins>SegTools>Create Overlay of Segmentation (3D)")
public class Create3DOverlayPlugin implements Command {

    //still unused TODO
    final String helpURL="https://github.com/mpicbg-scicomp/segmentation_3d_tools";

    @Parameter(label = "segmentation image (binary or labelled regions)", description = "binary: foreground=255, labelled: regions=1,2,3,...")
    ImagePlus segImp;

    @Parameter(label= "grayscale image", description = "Overlay will be added to this image")
    ImagePlus imp;

    @Parameter(label="color mode",choices = {"multicolor","red", "cyan", "magenta"},description = "if multicolor and labelled regions: different objects are displayed in different colors")
    String colorStr="multicolor";

    @Override
    public void run() {

        if (!checkInput()) return;

        // morphlibj is required
        try {
            Class.forName("inra.ijpb.label.LabelImages");
        } catch (ClassNotFoundException e) {
            IJ.error("MorpholibJ was not found. Please add the IJPB-plugins update site.");
            return;
        }


        int[] labelIds = LabelImages.findAllLabels(segImp);

        Color[] colors = pickColors(colorStr, labelIds.length);

        Overlay ov = new Overlay();

        // draw each region into overlay
        for (int idx=0; idx<labelIds.length; idx++) {
            Roi[] rois = Conversions.RoisFromOneLabel(segImp, labelIds[idx]);

            AddRoiArrayToOverlay(ov,imp,rois,colors[idx]);
        }


        imp.setOverlay(ov);
    }

    /**
     * sanity checks.
     * @return whether checks were successful
     */
    private final boolean checkInput() {
        if ((imp.getHeight()!=segImp.getHeight()) || (imp.getWidth()!=segImp.getWidth()) || (imp.getNSlices()!=segImp.getNSlices())) {
            IJ.error("Image dimensions in x,y,z must be the same for grayscale and segmentation image");
            return false;
        }
        return true;
    }


    /**
     * Creates an array with colors, which will be used to draw the overlays
     * @param colorStr , see plugin @Parameter colorStr
     * @param count number of different regions to color
     * @return colors
     */
    private Color[] pickColors(String colorStr, int count) {

        // default order for multicolor mode
        final Color[] defaultColors= new Color[] {Color.red, Color.green, Color.cyan, Color.magenta, Color.orange, Color.blue, Color.yellow};

        Color[] colors = new Color[count];

        if (colorStr.equals("red")) {
            Arrays.fill(colors,Color.red);
        }
        else if (colorStr.equals("cyan")) {
            Arrays.fill(colors,Color.cyan);
        }
        else if (colorStr.equals("magenta")) {
            Arrays.fill(colors,Color.magenta);
        }
        else { // multicolor
            for (int i = 0; i < colors.length; i++) {
                int idx=i % defaultColors.length;
                colors[i]=defaultColors[idx];
            }
        }

        return colors;
    }


    /**
     * Adds an array of rois to an existing overlay. The slice position is extracted from the roi position.
     * @param ov Overlay to which the roi's will be added. ov is updated in this function
     * @param imp target image for overlay. needed to extract stack/hyperstack info
     * @param rois array of rois
     * @param color color of the added overlay
     */
    public static void AddRoiArrayToOverlay(Overlay ov, final ImagePlus imp, final Roi[] rois, final Color color) {
        for (Roi roi : rois) {
            roi.setStrokeColor(color);

            // set the slice
            if (imp.getNSlices() > 2) {
                if (imp.isHyperStack()) {
                    roi.setPosition(0, roi.getZPosition(), 0); // add to all slices and frames
                } else { // Stack
                    roi.setPosition(roi.getZPosition());
                }
            }
            ov.add(roi);
        }
    }

}
