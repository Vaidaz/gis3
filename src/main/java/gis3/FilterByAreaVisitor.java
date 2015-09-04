package gis3;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.map.FeatureLayer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Geometry;

public class FilterByAreaVisitor implements Visitor {

	private MapFrame mapFrame;
	private int m;
	private String layerName;
	
	FilterByAreaVisitor(int m, String layerName){
		this.m = m;
		this.layerName = layerName;
	}
	
	public void visit(MapFrame mapFrame) {
		this.mapFrame = mapFrame;
		
		SimpleFeature feature = null;
		Geometry featureGeometry;
		double featureArea;
		DefaultFeatureCollection newFeatures = new DefaultFeatureCollection();		
		
		SimpleFeatureIterator selectedFeatures = mapFrame.getSelectedFeatures().features();

		while(selectedFeatures.hasNext()){
			feature = selectedFeatures.next();
			featureGeometry = (Geometry) feature.getDefaultGeometry();
			featureArea = featureGeometry.getArea() / 1000000; // meters
			System.out.println(featureArea);
			if(featureArea >= this.m){
				newFeatures.add(feature);
			}
		}
		
		Style style = SLD.createSimpleStyle(mapFrame.getSelectedLayer().getFeatureSource().getSchema());
		FeatureLayer featureLayer = new FeatureLayer(newFeatures, style);
		featureLayer.setTitle(this.layerName);

		this.mapFrame.getMapContent().addLayer(featureLayer);
		this.mapFrame.setSelectedFeatures(newFeatures);
		this.mapFrame.setSelectedLayer(featureLayer);		

	}

}
