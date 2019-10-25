package de.mpicbg.scf.manualseg;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * static utility functions to convert between list of 2d rois and 3d binary mask
 */
public class Conversions {

    /**
     * Creates a binary image mask of all roi's stored in roi manager. Works in 2D & 3D.
     *
     * @param rm  roi manager with rois
     * @param imp sets the target size of mask.
     * @return mask. 8 bit binary mask with foreground (=roi regions) value 255, background=0.
     * mask is calibrated like imp.
     */
    static public ImagePlus BinaryMaskFromRois(RoiManager rm, ImagePlus imp) {
        ImagePlus mask = IJ.createImage("binary mask", "8-bit black", imp.getWidth(), imp.getHeight(), imp.getNSlices());
        mask.setCalibration(imp.getCalibration());

        int numrois = rm.getCount();

        for (int idx = 0; idx < numrois; idx++) {
            Roi roi = rm.getRoi(idx);
            int slice = roi.getZPosition();
            if (imp.getNSlices() > 1 && slice == 0) {
                IJ.log("Warning: Roi " + idx + " is not associated to a specific slice. Skipping");
                continue;
            }

            mask.setSlice(slice);
            ImageProcessor maskip = mask.getProcessor();
            maskip.setValue(255);
            maskip.fill(roi);
        }

        mask.setSlice(1);
        return mask;
    }

    /**
     * Creates a binary image mask of all roi's stored in roi manager. Works in 2D & 3D.
     *
     * @param rm     roi manager with rois
     * @param width, height, nslices: target dimensions
     * @return mask. 8 bit binary mask with foreground (=roi regions) value 255, background=0. uncalibrated.
     */
    static public ImagePlus BinaryMaskFromRois(RoiManager rm, int width, int height, int nslices) {

        if (width < 1 || height < 1 || nslices < 1) {
            IJ.log("Target size for mask is zero or negative! Returning null");
            return null;
        }

        ImagePlus mask = IJ.createImage("binary mask", "8-bit black", width, height, nslices);

        int numrois = rm.getCount();

        for (int idx = 0; idx < numrois; idx++) {
            Roi roi = rm.getRoi(idx);
            int slice = roi.getZPosition();
            if (nslices > 1 && slice == 0) {
                IJ.log("Warning: Roi " + idx + " is not associated to a specific slice. Skipping");
                continue;
            }

            mask.setSlice(slice);
            ImageProcessor maskip = mask.getProcessor();
            maskip.setValue(255);
            maskip.fill(roi);
        }

        mask.setSlice(1);
        return mask;
    }

    /**
     * Creates a binary image mask of all roi's in the roi array. Works in 2D & 3D.
     * Rois for a 3D mask must be associated with a slices.
     *
     * @param roiarray Array of rois
     * @param width,   height, nslices: target dimensions
     * @return mask. 8 bit binary mask with foreground (=roi regions) value 255, background=0. uncalibrated
     */
    static public ImagePlus BinaryMaskFromRois(Roi[] roiarray, int width, int height, int nslices) {

        if (width < 1 || height < 1 || nslices < 1) {
            IJ.log("Target size for mask is zero or negative! Returning null");
            return null;
        }

        ImagePlus mask = IJ.createImage("binary mask", "8-bit black", width, height, nslices);

        int numrois = roiarray.length;

        for (int idx = 0; idx < numrois; idx++) {
            Roi roi = roiarray[idx];
            int slice = roi.getZPosition();
            if (nslices > 1 && slice == 0) {
                IJ.log("Warning: Roi " + idx + " is not associated to a specific slice. Skipping");
                continue;
            }

            mask.setSlice(slice);
            ImageProcessor maskip = mask.getProcessor();
            maskip.setValue(255);
            maskip.fill(roi);
        }

        mask.setSlice(1);
        return mask;
    }

    /**
     * Creates a binary image mask of all roi's in the roi array. Works in 2D & 3D.
     * Rois for a 3D mask must be associated with a slices.
     *
     * @param roiarray Array of rois
     * @param imp:     sets target dimensions of mask
     * @return mask. 8 bit binary mask with foreground (=roi regions) value 255, background=0.
     * mask is calibrated like imp.
     */
    static public ImagePlus BinaryMaskFromRois(Roi[] roiarray, ImagePlus imp) {

        ImagePlus mask = IJ.createImage("binary mask", "8-bit black", imp.getWidth(), imp.getHeight(), imp.getNSlices());
        mask.setCalibration(imp.getCalibration());

        int numrois = roiarray.length;

        for (int idx = 0; idx < numrois; idx++) {
            Roi roi = roiarray[idx];
            int slice = roi.getZPosition();
            if (imp.getNSlices() > 1 && slice == 0) {
                IJ.log("Warning: Roi " + idx + " is not associated to a specific slice. Skipping");
                continue;
            }

            mask.setSlice(slice);
            ImageProcessor maskip = mask.getProcessor();
            maskip.setValue(255);
            maskip.fill(roi);
        }

        mask.setSlice(1);
        return mask;
    }

    /**
     * Creates a ROI from each slice in a binary image and adds the ROIs (associated to slices) to the ROI manager.
     * If a slice is completely black (value=0) it is skipped.
     * Works in 2D & 3D
     *
     * @param rm   Roi manager where Rois will be stored
     * @param mask binary image (e.g. 0 background, 255 foreground). Image threshold for roi creation is internally set
     *             to 1, therefore a label image as input works as well.
     */
    static public void RoisFromBinaryMask(RoiManager rm, ImagePlus mask) {
        int nSlices = mask.getNSlices();

        for (int slice = 1; slice < nSlices + 1; slice++) {
            mask.setSlice(slice);
            ImageProcessor ip = mask.getProcessor();

            // threshold at 1
            ip.setThreshold(1, ip.maxValue(), ImageProcessor.NO_LUT_UPDATE); // maxValue() = max possible value

            Roi roi = new ThresholdToSelection().convert(ip);

            if (roi != null) {
                if (nSlices > 1) {
                    roi.setPosition(1, slice, 1);
                }
                rm.add(mask, roi, -1);
            }
        }
    }


    /**
     * Creates a ROI from each slice in a binary image and returns the ROIs (associated to slices) as an array.
     * If a slice is completely black (value=0) it is skipped.
     * Works in 2D & 3D
     *
     * @param mask binary image (e.g. 0 background, 255 foreground). Image threshold for roi creation is internally set
     *             to 1, therefore a label image as input works as well.
     * @return roiarray an array with all roi's
     */
    static public Roi[] RoisFromBinaryMask(ImagePlus mask) {

        List<Roi> roiList = new ArrayList<>();
        
        int nSlices = mask.getNSlices();

        for (int slice = 1; slice < nSlices + 1; slice++) {
            mask.setSlice(slice);
            ImageProcessor ip = mask.getProcessor();

            // threshold at 1
            ip.setThreshold(1, ip.maxValue(), ImageProcessor.NO_LUT_UPDATE); // maxValue() = max possible value

            Roi roi = new ThresholdToSelection().convert(ip);

            if (roi != null) {
                if (nSlices > 1) {
                    roi.setPosition(1, slice, 1);
                }
                roiList.add(roi);
            }
        }

        // convert to array
        Roi[] roiArray = roiList.toArray(new Roi[roiList.size()]);

        return roiArray;
    }

}
