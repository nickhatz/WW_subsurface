/*
Copyright (C) 2001, 2011 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwindx.examples.analytics2;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.Cylinder2;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.Hashtable;

import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;
import org.gdal.ogr.Feature;
import org.gdal.ogr.DataSource;


/**
 * Illustrates how to use the World Wind <code>{@link Cylinder}</code> rigid shape to display an arbitrarily sized and
 * oriented cylinder at a geographic position on the Globe.
 *
 * @author ccrick
 * @version $Id: Cylinders.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class Cylinders extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            // Add detail hint slider panel
            this.getLayerPanel().add(this.makeDetailHintControlPanel(), BorderLayout.SOUTH);

            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.YELLOW);
            attrs.setInteriorOpacity(0.3);
            attrs.setEnableLighting(true);
            attrs.setOutlineMaterial(Material.RED);
            attrs.setOutlineWidth(2d);
            attrs.setDrawInterior(true);
            attrs.setDrawOutline(false);
            

            // ********* sample  Cylinders  *******************

            // Cylinder with equal axes, ABSOLUTE altitude mode
            
            gdal.SetConfigOption("GDAL_DATA", "data");
            ogr.RegisterAll();
            
            String pszDataSource = "/home/nickhatz/my2.gml";
            
            DataSource poDS;
            poDS = ogr.Open( pszDataSource);
            Feature feat;
            
            while((feat = poDS.GetLayerByIndex(0).GetNextFeature()) != null){
                //System.out.println("Name: "+feat.GetFieldAsString("gml_id"));
                //System.out.println("Lon: "+feat.GetGeometryRef().GetX());
                //System.out.println("Lat: "+feat.GetGeometryRef().GetY());
                //System.out.println("Elev: "+feat.GetFieldAsString("elevation"));
                double elevation = Double.parseDouble(feat.GetFieldAsString("elevation"))*10;
                Cylinder2 mycylinder = new Cylinder2(Position.fromDegrees(feat.GetGeometryRef().GetY(), feat.GetGeometryRef().GetX(), (elevation/2)-elevation), elevation , 20);
                mycylinder.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                mycylinder.setAttributes(attrs);
                mycylinder.setVisible(true);
                mycylinder.setValue(AVKey.DISPLAY_NAME, "Cylinder with equal axes, Relative altitude mode");
                layer.addRenderable(mycylinder);
                
            }
            
            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);
            // Update layer panel
            this.getLayerPanel().update(this.getWwd());
        }

        protected JPanel makeDetailHintControlPanel()
        {
            JPanel controlPanel = new JPanel(new BorderLayout(0, 10));
            controlPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                new TitledBorder("Detail Hint")));

            JPanel detailHintSliderPanel = new JPanel(new BorderLayout(0, 5));
            {
                int MIN = -10;
                int MAX = 10;
                int cur = 0;
                JSlider slider = new JSlider(MIN, MAX, cur);
                slider.setMajorTickSpacing(10);
                slider.setMinorTickSpacing(1);
                slider.setPaintTicks(true);
                Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
                labelTable.put(-10, new JLabel("-1.0"));
                labelTable.put(0, new JLabel("0.0"));
                labelTable.put(10, new JLabel("1.0"));
                slider.setLabelTable(labelTable);
                slider.setPaintLabels(true);
                slider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        double hint = ((JSlider) e.getSource()).getValue() / 10d;
                        setCylinderDetailHint(hint);
                        getWwd().redraw();
                    }
                });
                detailHintSliderPanel.add(slider, BorderLayout.SOUTH);
            }

            JPanel sliderPanel = new JPanel(new GridLayout(2, 0));
            sliderPanel.add(detailHintSliderPanel);

            controlPanel.add(sliderPanel, BorderLayout.SOUTH);
            return controlPanel;
        }

        protected RenderableLayer getLayer()
        {
            for (Layer layer : getWwd().getModel().getLayers())
            {
                if (layer.getName().contains("Renderable"))
                {
                    return (RenderableLayer) layer;
                }
            }

            return null;
        }

        protected void setCylinderDetailHint(double hint)
        {
            for (Renderable renderable : getLayer().getRenderables())
            {
                Cylinder2 current = (Cylinder2) renderable;
                current.setDetailHint(hint);
            }
            System.out.println("cylinder detail hint set to " + hint);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Cylinders", AppFrame.class);
    }
}

