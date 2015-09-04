package gis3;

import java.io.IOException;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class DifferenceFeatureLayerVisitor implements Visitor {

	private List<String> layersToSubstract;
	private MapFrame mapFrame;
	private String layerName;
	private DefaultFeatureCollection features;
	
	DifferenceFeatureLayerVisitor(List<String> layersToSubstract, String layerName){
		this.layersToSubstract = layersToSubstract;
		this.layerName = layerName;
		this.features = new DefaultFeatureCollection();		
	}
	
	public void visit(MapFrame mapFrame) {
		this.mapFrame = mapFrame;
				
		SimpleFeatureIterator selectedFeatures = mapFrame.getSelectedFeatures().features();
		SimpleFeature feature = null;
		SimpleFeature substractedFeature = null;
		
		while(selectedFeatures.hasNext()){
			feature = selectedFeatures.next();
			substractedFeature = substractFeatures(feature);
			this.features.add(substractedFeature);
		}
		
		convertMultypolygonsToPolygons();

		Style style = SLD.createSimpleStyle(mapFrame.getSelectedLayer().getFeatureSource().getSchema());
		FeatureLayer featureLayer = new FeatureLayer(this.features, style);
		featureLayer.setTitle(this.layerName);

		this.mapFrame.getMapContent().addLayer(featureLayer);
		this.mapFrame.setSelectedFeatures(this.features);
		this.mapFrame.setSelectedLayer(featureLayer);		

	}
	
	private void convertMultypolygonsToPolygons(){
		DefaultFeatureCollection newFeatures = new DefaultFeatureCollection();		
		SimpleFeatureIterator featureIterator = this.features.features();
		SimpleFeature feature, tmpFeature;
		MultiPolygon mp;
		int n;
		
		while(featureIterator.hasNext()){
			feature = featureIterator.next();
			mp = (MultiPolygon) feature.getDefaultGeometry(); 
            n = mp.getNumGeometries(); 
            for (int i = 0; i < n; i++) { 
                Polygon poly = (Polygon) mp.getGeometryN(i); 
                
                tmpFeature = makeFeature(poly, feature);
                
                newFeatures.add(tmpFeature);
            } 

		}
		this.features = newFeatures;
		System.out.println("Featurs kiekis: " + newFeatures.size());
	}
	
	private SimpleFeature substractFeatures(SimpleFeature feature){
		Layer layer = null;
		FeatureIterator<?> featureIterator = null;
		SimpleFeature featureToSubstract;
				
		for(String layerName : layersToSubstract){
			layer = findLayer(layerName);
			
			if(layer != null){
				try {
					featureIterator = layer.getFeatureSource().getFeatures().features();
					
					while(featureIterator.hasNext()){
						featureToSubstract = (SimpleFeature) featureIterator.next();				
						feature = substract(feature, featureToSubstract);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}

			}

		}
		
		return feature;
	}
	
	private SimpleFeature substract(SimpleFeature feature, SimpleFeature featureToSubstract){
		Geometry selectedGeometry = (Geometry) featureToSubstract.getDefaultGeometry();
		Geometry bufferedGeometry = (Geometry) feature.getDefaultGeometry();
		Geometry newGeometry = bufferedGeometry.difference(selectedGeometry);
		return makeFeature(newGeometry, feature);
	}
		
	private Layer findLayer(String layerName){
		for(Layer layer : mapFrame.getMapContent().layers()){
			if(layer.getTitle().equals(layerName)){
				return layer;
			}
		}
		
		return null;
	}
	
	private SimpleFeature makeFeature(Geometry geometry, SimpleFeature feature){
    	SimpleFeatureType schema = feature.getFeatureType();
    	SimpleFeatureTypeBuilder ftBuilder = new SimpleFeatureTypeBuilder();
    	
    	GeometryAttribute gProp = feature.getDefaultGeometryProperty();
    	CoordinateReferenceSystem origCRS = gProp.getDescriptor().getCoordinateReferenceSystem();
    	ftBuilder.setCRS(origCRS);
    	ftBuilder.addAll(schema.getAttributeDescriptors());
    	ftBuilder.setName(schema.getName());
    	
    	SimpleFeatureType nSchema = ftBuilder.buildFeatureType();
    	SimpleFeatureBuilder builder = new SimpleFeatureBuilder(nSchema);
    	List<Object> atts = feature.getAttributes();
    	for(int i=0;i<atts.size();i++) {
    		if(atts.get(i) instanceof Geometry) {
    			atts.set(i, geometry);
    	    }
    	}
    	SimpleFeature nFeature = builder.buildFeature(null, atts.toArray() );

		return nFeature;
	}

}
