package gis3;

import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

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

public class BufferVisitor implements Visitor {
	
	private MapFrame mapFrame;
	private Measure<Double, Length> distance;
	private String layerName;
	
	BufferVisitor(Measure<Double, Length> distance, String layerName){
		this.distance = distance;
		this.layerName = layerName;
	}

	public void visit(MapFrame mapFrame) {
		this.mapFrame = mapFrame;
		
		DefaultFeatureCollection newFeatures = new DefaultFeatureCollection();		
		
		SimpleFeatureIterator featureIterator = mapFrame.getSelectedFeatures().features();
		while(featureIterator.hasNext()){
			SimpleFeature feature = featureIterator.next();
			SimpleFeature newFeature = bufferFeature(feature, this.distance);
						
			newFeatures.add(newFeature);
		}		
	
		Style style = SLD.createSimpleStyle(mapFrame.getSelectedLayer().getFeatureSource().getSchema());
		FeatureLayer featureLayer = new FeatureLayer(newFeatures, style);
		featureLayer.setTitle(this.layerName);
		
		this.mapFrame.getMapContent().addLayer(featureLayer);
		this.mapFrame.setSelectedFeatures(newFeatures);
		this.mapFrame.setSelectedLayer(featureLayer);		

	}
	
	
    public SimpleFeature bufferFeature(SimpleFeature feature, Measure<Double, Length> distance) {
    	GeometryAttribute gProp = feature.getDefaultGeometryProperty();
    	CoordinateReferenceSystem origCRS = gProp.getDescriptor().getCoordinateReferenceSystem();

    	Geometry geom = (Geometry) feature.getDefaultGeometry();
    	Geometry pGeom = geom;

    	Geometry out = buffer(pGeom, distance.doubleValue(SI.METER));
    	Geometry retGeom = out;

    	SimpleFeatureType schema = feature.getFeatureType();
    	SimpleFeatureTypeBuilder ftBuilder = new SimpleFeatureTypeBuilder();
    	ftBuilder.setCRS(origCRS);
    	ftBuilder.addAll(schema.getAttributeDescriptors());
    	ftBuilder.setName(schema.getName());
    	
    	SimpleFeatureType nSchema = ftBuilder.buildFeatureType();
    	SimpleFeatureBuilder builder = new SimpleFeatureBuilder(nSchema);
    	List<Object> atts = feature.getAttributes();
    	for(int i=0;i<atts.size();i++) {
    		if(atts.get(i) instanceof Geometry) {
    			atts.set(i, retGeom);
    	    }
    	}
    	SimpleFeature nFeature = builder.buildFeature(null, atts.toArray() );
    	return nFeature;
    }

    /**
     * create a buffer around the geometry, assumes the geometry is in the same
     * units as the distance variable.
     * 
     * @param geom
     *            a projected geometry.
     * @param dist
     *            a distance for the buffer in the same units as the projection.
     * @return
     */
    private Geometry buffer(Geometry geom, double dist) {
	
    	Geometry buffer = geom.buffer(dist);
    	
    	return buffer;

    }

}
