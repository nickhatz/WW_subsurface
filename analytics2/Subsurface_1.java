/* Copyright (C) 2001, 2011 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
 * 
 * @author Nikolaos J. Hatzopoulos
*/


package gov.nasa.worldwind.examples.analytics2;

import gov.nasa.worldwind.examples.ApplicationTemplate;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.data.BufferWrapperRaster;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWBufferUtil;
import gov.nasa.worldwind.util.WWMath;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.Format;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import javax.swing.SwingUtilities;
import java.awt.Point;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.net.URI;
import java.net.URLClassLoader;


public class Subsurface_1 extends ApplicationTemplate{
    public static class AppFrame extends ApplicationTemplate.AppFrame{
        
        protected static final double HUE_BLUE = 240d / 360d;
        protected static final double HUE_RED = 0d / 360d;
        protected RenderableLayer analyticSurfaceLayer;

        public AppFrame() throws IOException, ParserConfigurationException, SAXException{
            //Disable struts or skirts attached to the terrain tiles
            this.getWwd().getModel().getGlobe().getTessellator().setMakeTileSkirts(false);
            
            //Make transparent the layers
            for (Layer layer : this.getWwd().getModel().getLayers()){
                layer.setOpacity(1);
                
                
            }
            
            this.initAnalyticSurfaceLayer();
            
            
            
        }
        
        protected void initAnalyticSurfaceLayer()
        {
            this.analyticSurfaceLayer = new RenderableLayer();
            this.analyticSurfaceLayer.setPickEnabled(false);
            this.analyticSurfaceLayer.setName("Analytic Surfaces");
            insertBeforePlacenames(this.getWwd(), this.analyticSurfaceLayer);
            this.getLayerPanel().update(this.getWwd());

            //createRandomAltitudeSurface(HUE_BLUE, HUE_RED, 40, 40, this.analyticSurfaceLayer);
            //createRandomColorSurface(HUE_BLUE, HUE_RED, 40, 40, this.analyticSurfaceLayer);

            // Load the static precipitation data. Since it comes over the network, load it in a separate thread to
            // avoid blocking the example if the load is slow or fails.
            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    createPrecipitationSurface(HUE_BLUE, HUE_RED, analyticSurfaceLayer);
                }
            });
            t.start();
        }
        
    }
    
    protected static Renderable createLegendRenderable(final AnalyticSurface surface, final double surfaceMinScreenSize,
        final AnalyticSurfaceLegend legend)
    {
        return new Renderable()
        {
            public void render(DrawContext dc)
            {
                Extent extent = surface.getExtent(dc);
                if (!extent.intersects(dc.getView().getFrustumInModelCoordinates()))
                    return;

                if (WWMath.computeSizeInWindowCoordinates(dc, extent) < surfaceMinScreenSize)
                    return;

                legend.render(dc);
            }
        };
    }
    
    //**************************************************************//
    //********************  Precipitation Surface  *****************//
    //**************************************************************//

    protected static void createPrecipitationSurface(double minHue, double maxHue, final RenderableLayer outLayer)
    {
        BufferWrapperRaster raster = loadZippedBILData(
            //"http://worldwind.arc.nasa.gov/java/demos/data/wa-precip-24hmam.zip");
                "http://forecast.chapman.edu/nickhatz/data.zip");
        if (raster == null)
            return;

        double[] extremes = WWBufferUtil.computeExtremeValues(raster.getBuffer(), raster.getTransparentValue());
        if (extremes == null)
            return;        

        final AnalyticSurface surface = new AnalyticSurface();
        surface.setSector(raster.getSector());
        surface.setDimensions(raster.getWidth(), raster.getHeight());
        
        surface.setValues(AnalyticSurface.createColorGradientValues(
            raster.getBuffer(), raster.getTransparentValue(), extremes[0], extremes[1], minHue, maxHue));
        surface.setVerticalScale(-100);
        //surface.setVerticalScale(1);

        AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
        attr.setDrawOutline(true);
        attr.setDrawShadow(false);
        attr.setInteriorOpacity(0.3);
        //attr.setInteriorOpacity(1);
        surface.setSurfaceAttributes(attr);

        Format legendLabelFormat = new DecimalFormat("# ft")
        {
            public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition)
            {
                double valueInFeet = number * WWMath.METERS_TO_FEET;
                return super.format(valueInFeet, result, fieldPosition);
            }
        };
        
        final AnalyticSurfaceLegend legend = AnalyticSurfaceLegend.fromColorGradient(extremes[0], extremes[1],
            minHue, maxHue,
            AnalyticSurfaceLegend.createDefaultColorGradientLabels(extremes[0], extremes[1], legendLabelFormat),
            AnalyticSurfaceLegend.createDefaultTitle("2WhsBFScale Subsurface"));
        //legend.setOpacity(0.8);
        //legend.setOpacity(1);
        legend.setScreenLocation(new Point(100, 300));
         
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                surface.setClientLayer(outLayer);
                outLayer.addRenderable(surface);
                outLayer.addRenderable(createLegendRenderable(surface, 300, legend));
            }
        }); 
    }
    
    protected static ByteBuffer unzipEntryToBuffer(ZipFile zipFile, String entryName) throws IOException
    {
        ZipEntry entry = zipFile.getEntry(entryName);
        InputStream is = zipFile.getInputStream(entry);
        return WWIO.readStreamToBuffer(is);
    }
    
    protected static BufferWrapperRaster loadZippedBILData(String uriString)
    {
        try
        {
            File zipFile = File.createTempFile("data", ".zip");
            File hdrFile = File.createTempFile("data", ".hdr");
            File blwFile = File.createTempFile("data", ".blw");
            zipFile.deleteOnExit();
            hdrFile.deleteOnExit();
            blwFile.deleteOnExit();

            ByteBuffer byteBuffer = WWIO.readURLContentToBuffer(new URI(uriString).toURL());
            WWIO.saveBuffer(byteBuffer, zipFile);

            ZipFile zip = new ZipFile(zipFile);
            ByteBuffer dataBuffer = unzipEntryToBuffer(zip, "data.bil");
            WWIO.saveBuffer(unzipEntryToBuffer(zip, "data.hdr"), hdrFile);
            WWIO.saveBuffer(unzipEntryToBuffer(zip, "data.blw"), blwFile);
            zip.close();

            AVList params = new AVListImpl();
            WorldFile.decodeWorldFiles(new File[] {hdrFile, blwFile}, params);
            params.setValue(AVKey.DATA_TYPE, params.getValue(AVKey.PIXEL_TYPE));

            Double missingDataSignal = (Double) params.getValue(AVKey.MISSING_DATA_REPLACEMENT);
            if (missingDataSignal == null)
                missingDataSignal = Double.NaN;

            Sector sector = (Sector) params.getValue(AVKey.SECTOR);
            int[] dimensions = (int[]) params.getValue(WorldFile.WORLD_FILE_IMAGE_SIZE);
            BufferWrapper buffer = BufferWrapper.wrap(dataBuffer, params);

            BufferWrapperRaster raster = new BufferWrapperRaster(dimensions[0], dimensions[1], sector, buffer);
            raster.setTransparentValue(missingDataSignal);
            return raster;
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToReadFrom", uriString);
            Logging.logger().severe(message);
            return null;
        }
    }
    
    
    
    public static void main(String[] args){
        start("World Wind Shapefiles", AppFrame.class);
    }
    
    public static void test(){
        start("World Wind Shapefiles", AppFrame.class);
    }
    
}
