package gis3;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

public class FilterSelectedVisitior implements Visitor {

	private MapFrame mapFrame; 
	private String property;
	private String layerName;
	private List<String> codeNames;
	
	FilterSelectedVisitior(String property, String value, String layerName){
		this(property, Arrays.asList(value), layerName);
	}
	
	public FilterSelectedVisitior(String property, List<String> codeNames, String layerName) {
		this.property = property;
		this.codeNames = codeNames;
		this.layerName = layerName;
	}

	public void visit(MapFrame mapFrame) {
		this.mapFrame = mapFrame;
		filterByPropery();
	}
		
	public void filterByPropery(){
		SimpleFeatureCollection features = this.mapFrame.getSelectedFeatures();
		DefaultFeatureCollection newFearures = new DefaultFeatureCollection();
		
		SimpleFeatureIterator iterator = features.features();
		while(iterator.hasNext()){
			SimpleFeature feature =  iterator.next();
						
			Collection<Property> properties = feature.getProperties();
			for(Property property : properties){
				if(property.getName().toString().equals(this.property) && 
						property.getValue() != null &&
						codeNamesHasValue(property.getValue().toString())){
					newFearures.add(feature);
					break;
				}
			}
			
		}
		
		Layer selectedLayer = this.mapFrame.getSelectedLayer();
		Style style = SLD.createSimpleStyle(selectedLayer.getFeatureSource().getSchema());
		FeatureLayer featureLayer = new FeatureLayer(newFearures, style);
		featureLayer.setVisible(false);
		featureLayer.setTitle(this.layerName);
		
		this.mapFrame.getMapContent().addLayer(featureLayer);
		this.mapFrame.setSelectedFeatures(newFearures);
		this.mapFrame.setSelectedLayer(featureLayer);				
	}
	
	private boolean codeNamesHasValue(String value){
		for(String name : this.codeNames){
			if(name.equals(value)){
				return true;
			}
		}
		return false;
	}
			
}
