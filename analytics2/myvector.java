/* Copyright (C) 2001, 2011 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
 * 
 * @author Nikolaos J. Hatzopoulos
*/

package gov.nasa.worldwindx.examples.analytics2;

import org.gdal.gdal.gdal;
import org.gdal.ogr.*;
import org.gdal.osr.*;
import java.util.*;


/**
 *
 * @author nickhatz
 */

public class myvector {
    
    public static void main(String[] args){
        //gdal.AllRegister();
        
        gdal.SetConfigOption("GDAL_DATA", "data");    
        ogr.RegisterAll();
        
        //String pszDataSource = "/usr/local/itt/idl71/resource/maps/shape/lakes.shp";
    	//String pszDataSource = "/home/nickhatz/my.gml";
        //String pszDataSource = "WFS:http://nvclwebservices.vm.csiro.au/geoserverBH/wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=1.1.0&maxFeatures=100&typeName=gsml:Borehole";
        String pszDataSource = "/home/nickhatz/my2.gml";
        
        DataSource poDS;
            
        poDS = ogr.Open( pszDataSource);
        //System.out.println("Layer Count: "+poDS.GetLayerCount());
        //System.out.println("Layer Name: "+poDS.GetLayerByIndex(0).GetName());
        //System.out.println("Feature Count: "+poDS.GetLayerByIndex(0).GetFeatureCount());
        //System.out.println("Feature Count: "+poDS.GetLayerByIndex(0).);
        Layer layer = poDS.GetLayerByIndex(0);
        Feature feat;
        while((feat = layer.GetNextFeature()) != null){
            System.out.println("Name: "+feat.GetFieldAsString("gml_id"));
            System.out.println("Lon: "+feat.GetGeometryRef().GetX());
            System.out.println("Lat: "+feat.GetGeometryRef().GetY());
            System.out.println("Elev: "+feat.GetFieldAsString("elevation"));
            
        }
        
        //feat = layer.GetNextFeature();
        //System.out.println("Feat: "+feat.GetFieldAsString(0));
            
        
        //System.out.println("1st Feature X: "+Double.toString(poDS.GetLayerByIndex(0).GetFeature(0).GetGeometryRef().GetX()));
        //System.out.println("1st Feature Y: "+Double.toString(poDS.GetLayerByIndex(0).GetFeature(0).GetGeometryRef().GetY()));
        //System.out.println("Elevation"+poDS.GetLayerByIndex(0).GetFeature(0).GetFieldAsString("elevation"));
        
        //System.out.println(poDS.GetDriver().getName());
        
        
        //Dataset ds;
        //ds = GDALUtils.open("/home/nickhatz/Desktop/MODIS/fire_modis/images/a1.10225.2042.ndvi.tif",false);
        
        
        
        /* -------------------------------------------------------------------- */
        /*      Register format(s).                                             */
        /* -------------------------------------------------------------------- */
        //GDALUtils mytest;
        
    }
    
}
