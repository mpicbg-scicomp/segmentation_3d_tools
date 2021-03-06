package de.mpicbg.scf.segtools;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.List;

/*
 * Author: Noreen Walker, Scientific Computing Facility, MPI-CBG
 * Date: 2019-10
 */


/**
 * static utility functions to convert between list of 2d rois and 3d binary mask and 3d label images
 */
public class Conversions {

    /**
     * Creates a binary image mask of all roi's stored in roi manager. Works in 2D & 3D.
     *
     * @param rm  roi manager with rois
     * @param imp sets the target size of mask.
     * @param associate if true the ROIs are only drawn in the associated slices, otherwise they are drawn in all slices.
     * @return mask. 8 bit binary mask with foreground (=roi regions) value 255, background=0.
     * mask is calibrated like imp.
     */
    static public ImagePlus BinaryMaskFromRois(RoiManager rm, ImagePlus imp, boolean associate) {
        ImagePlus mask = IJ.createImage("binary mask", "8-bit black", imp.getWidth(), imp.getHeight(), imp.getNSlices());
        mask.setCalibration(imp.getCalibration());

        int numrois = rm.getCount();

        for (int idx = 0; idx < numrois; idx++) {
            Roi roi = rm.getRoi(idx);

            UpdateMask(mask,roi,associate,idx);
        }

        mask.setSlice(1);
        return mask;
    }

    /**
     * Creates a binary image mask of all roi's stored in roi manager. Works in 2D & 3D.
     *
     * @param rm     roi manager with rois
     * @param width, height, nslices: target dimensions
     * @param associate if true the ROIs are only drawn in the associated slices, otherwise they are drawn in all slices.
     * @return mask. 8 bit binary mask with foreground (=roi regions) value 255, background=0. uncalibrated.
     */
    static public ImagePlus BinaryMaskFromRois(RoiManager rm, int width, int height, int nslices, boolean associate) {

        if (width < 1 || height < 1 || nslices < 1) {
            IJ.log("Target size for mask is zero or negative! Returning null");
            return null;
        }

        ImagePlus mask = IJ.createImage("binary mask", "8-bit black", width, height, nslices);

        int numrois = rm.getCount();

        for (int idx = 0; idx < numrois; idx++) {
            Roi roi = rm.getRoi(idx);

            UpdateMask(mask, roi, associate, idx);

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
     * @param associate if true the ROIs are only drawn in the associated slices, otherwise they are drawn in all slices.
     * @return mask. 8 bit binary mask with foreground (=roi regions) value 255, background=0. uncalibrated
     */
    static public ImagePlus BinaryMaskFromRois(Roi[] roiarray, int width, int height, int nslices, boolean associate) {

        if (width < 1 || height < 1 || nslices < 1) {
            IJ.log("Target size for mask is zero or negative! Returning null");
            return null;
        }

        ImagePlus mask = IJ.createImage("binary mask", "8-bit black", width, height, nslices);

        int numrois = roiarray.length;

        for (int idx = 0; idx < numrois; idx++) {
            Roi roi = roiarray[idx];

            UpdateMask(mask, roi, associate, idx);
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
     * @param associate if true the ROIs are only drawn in the associated slices, otherwise they are drawn in all slices.
     * @return mask. 8 bit binary mask with foreground (=roi regions) value 255, background=0.
     * mask is calibrated like imp.
     */
    static public ImagePlus BinaryMaskFromRois(Roi[] roiarray, ImagePlus imp, boolean associate) {

        ImagePlus mask = IJ.createImage("binary mask", "8-bit black", imp.getWidth(), imp.getHeight(), imp.getNSlices());
        mask.setCalibration(imp.getCalibration());

        int numrois = roiarray.length;

        for (int idx = 0; idx < numrois; idx++) {
            Roi roi = roiarray[idx];

            UpdateMask(mask, roi, associate, idx);
        }

        mask.setSlice(1);
        return mask;
    }


    /** Helper for the the BinaryMaskFromROIs function family. Draws a single roi.
     */
    private static void UpdateMask(ImagePlus mask, final Roi roi, final boolean associate, final int idx) {

        int nslices=mask.getNSlices();

        int roislice = roi.getZPosition();

        // check if roi has an associated slice
        boolean associateThisRoi=associate;
        if (associate && nslices > 1 && roislice == 0) {
            IJ.log("Warning: Roi " + idx + " is not associated to a specific slice. Drawing into all slices");
            associateThisRoi=false;
        }

        // draw roi into single slice
        if (associateThisRoi) {
            if (roislice<=nslices){
                mask.setSliceWithoutUpdate(roislice);
                ImageProcessor maskip = mask.getProcessor();
                maskip.setValue(255);
                maskip.fill(roi);
            }
        }
        else {
            // draw into all slices
            for (int sliceid = 1; sliceid < 1+nslices; sliceid++) {
                mask.setSlice(sliceid);
                ImageProcessor maskip = mask.getProcessor();
                maskip.setValue(255);
                maskip.fill(roi);
            }
        }
    }

    /**
     * Creates a ROI from each slice in a binary image and adds the ROIs (associated to slices) to the ROI manager.
     * Null Rois (from black slices) are skipped.
     * Works in 2D & 3D
     *
     * @param rm   Roi manager where Rois will be stored
     * @param mask binary image (e.g. 0 background, 255 foreground). Image threshold for roi creation is internally set
     *             to 1, therefore a label image as input works as well.
     */
    static public void RoisFromBinaryMask(RoiManager rm, ImagePlus mask) {
        int nSlices = mask.getNSlices();

        for (int slice = 1; slice < nSlices + 1; slice++) {
            mask.setSliceWithoutUpdate(slice);
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
     * Null Rois (from black slices) are skipped.
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
            mask.setSliceWithoutUpdate(slice);
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


    /**
     * Like RoisFromBinaryMask(ImagePlus mask) but creates Rois for a single label from a label image.
     * Rois are returned as array and associated with slices. Null Rois (from black slices) are skipped.
     * Works in 2D & 3D
     *
     * @param labelImp label image (connected components) with regions of value 1,2,3,....
     * @param labelId region for which Rois are extracted
     * @return roiarray an array with all roi's
     */
    static public Roi[] RoisFromOneLabel(ImagePlus labelImp, int labelId) {

        List<Roi> roiList = new ArrayList<>();

        int nSlices = labelImp.getNSlices();

        for (int slice = 1; slice < nSlices + 1; slice++) {
            labelImp.setSliceWithoutUpdate(slice);
            ImageProcessor ip = labelImp.getProcessor();

            // threshold at labelId
            ip.setThreshold(labelId, labelId+0.1, ImageProcessor.NO_LUT_UPDATE);

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
