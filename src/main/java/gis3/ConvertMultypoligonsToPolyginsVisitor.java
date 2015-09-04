package gis3;

import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.map.FeatureLayer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class ConvertMultypoligonsToPolyginsVisitor implements Visitor {

	private String layerName;
	private MapFrame mapFrame;
	
	ConvertMultypoligonsToPolyginsVisitor(String layerName){
		this.layerName = layerName;
	}
	
	public void visit(MapFrame mapFrame) {
		this.mapFrame = mapFrame;
		
		SimpleFeatureCollection features = mapFrame.getSelectedFeatures();
		
		features = convertMultypolygonsToPolygons(features);
		
		Style style = SLD.createSimpleStyle(mapFrame.getSelectedLayer().getFeatureSource().getSchema());
		FeatureLayer featureLayer = new FeatureLayer(features, style);
		featureLayer.setTitle(this.layerName);

		this.mapFrame.getMapContent().addLayer(featureLayer);
		this.mapFrame.setSelectedFeatures(features);
		this.mapFrame.setSelectedLayer(featureLayer);		

	}

	private SimpleFeatureCollection convertMultypolygonsToPolygons(SimpleFeatureCollection features){
		DefaultFeatureCollection newFeatures = new DefaultFeatureCollection();		
		SimpleFeatureIterator featureIterator = features.features();
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
		return newFeatures;
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
