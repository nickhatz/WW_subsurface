/* Copyright (C) 2001, 2011 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
 * 
 * @author Nikolaos J. Hatzopoulos
*/

package gov.nasa.worldwindx.examples.analytics2;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindx.examples.ApplicationTemplate;



/**
 *
 * @author nickhatz
 */
public class Subsurface extends ApplicationTemplate{
    public static class AppFrame extends ApplicationTemplate.AppFrame{

        public AppFrame() throws IOException, ParserConfigurationException, SAXException{
            //Disable struts or skirts attached to the terrain tiles
            this.getWwd().getModel().getGlobe().getTessellator().setMakeTileSkirts(false);
            
            //Make transparent the layers
            for (Layer layer : this.getWwd().getModel().getLayers()){
                layer.setOpacity(0.4);
                
                
            }
            
        }
        
    }
    
    
    
    public static void main(String[] args){
        start("World Wind Shapefiles", AppFrame.class);
    }
    
}
