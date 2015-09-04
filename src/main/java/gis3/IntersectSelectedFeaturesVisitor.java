package gis3;

import java.io.File;
import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;

public class IntersectSelectedFeaturesVisitor implements Visitor {
	
	private String layerName;
	private MapFrame mapFrame;
	private Layer layer;
	private SimpleFeatureCollection intersectedFeatures;
	private String layerTitle;
	private String newLayerName;
	
	IntersectSelectedFeaturesVisitor(String layerName){
		this(layerName, null);
	}
	
	IntersectSelectedFeaturesVisitor(String layerName, String newName){
		this.layerName = layerName;
		this.newLayerName = newName;
	}
	
	public void visit(MapFrame mapFrame){
		this.mapFrame = mapFrame;
		createTitle();
		findLayer();
		
		if(this.layer == null){
			System.out.println("Layer not foud: " + layerName);
		}
		
		File layerFile = new File("./shp/system_created/" + this.layerTitle + ".shp");
			
		intersect();
		
		Style style = SLD.createSimpleStyle(this.layer.getFeatureSource().getSchema());
		FeatureLayer featureLayer = new FeatureLayer(this.intersectedFeatures, style);
		featureLayer.setVisible(false);
		featureLayer.setTitle(this.layerTitle);
		
		this.mapFrame.getMapContent().addLayer(featureLayer);
		this.mapFrame.setSelectedFeatures(this.intersectedFeatures);
		this.mapFrame.setSelectedLayer(featureLayer);		

	}
	
	private void findLayer(){
		for(Layer layer : mapFrame.getMapContent().layers()){
			if(layer.getTitle().equals(this.layerName)){
				this.layer = layer;
			}
		}
	}
	
	private void intersect(){		
		SimpleFeatureCollection filteredFeatures;
		try {
			filteredFeatures = (SimpleFeatureCollection) this.layer.getFeatureSource().getFeatures();
		} catch (IOException e) {
			e.printStackTrace();
			filteredFeatures = null;
		}
		SimpleFeatureCollection selectedFeatures = this.mapFrame.getSelectedFeatures();
		
		IntersectionFeatureCollection intersection = new IntersectionFeatureCollection();
        SimpleFeatureCollection intersectedCollection = intersection.execute(
        		filteredFeatures,
        		selectedFeatures,
                null,
                null,
                IntersectionFeatureCollection.IntersectionMode.INTERSECTION,
                false,
                false
        );
        
		this.intersectedFeatures = intersectedCollection;
		
	}
		
	private void createTitle(){
		if(this.newLayerName != null){
			this.layerTitle = this.newLayerName;
		} else {
			this.layerTitle = this.mapFrame.getSelectedLayer().getTitle() + "_intersect_" + this.layerName;
		}
	}
	
}
