package gis3;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.swing.MapPane;
import org.geotools.swing.event.MapMouseEvent;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

public class SelectAreaVisitor implements Visitor {

	private Rectangle rectangle;
	private MapFrame mapFrame;
	private ReferencedEnvelope bbox; 
	private Layer layer;
    private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    private DefaultFeatureCollection allSelectedFeatures;
	
	SelectAreaVisitor(MapMouseEvent ev){
		this.allSelectedFeatures = new DefaultFeatureCollection();
		this.rectangle = new Rectangle(ev.getPoint().x, ev.getPoint().y, 1, 1);
	}
	
	public void visit(MapFrame mapFrame) {
		this.mapFrame = mapFrame;
		calculateBBox();
		select();
		this.mapFrame.setSelectedLayer(this.layer);
		this.mapFrame.setSelectedFeatures(this.allSelectedFeatures);
	}
	
	private void calculateBBox(){
        /*
         * Transform the screen rectangle into bounding box in the coordinate
         * reference system of our map context. Note: we are using a naive method
         * here but GeoTools also offers other, more accurate methods.
         */
		
		Rectangle screenRect = this.rectangle;
		MapPane mapPane = this.mapFrame.getMapPane();
		
        AffineTransform screenToWorld = mapPane.getScreenToWorldTransform();
        Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
        this.bbox = new ReferencedEnvelope(worldRect, mapPane.getMapContent().getCoordinateReferenceSystem());

	}
	
	private void select(){		
		List<Layer> layers = this.mapFrame.getMapContent().layers();
		for(Layer layer : layers){
			if(layer.getTitle().equals("sven_SAV_P")){
				this.layer = layer;
				selectFeatures();
			}
		}
	}
	    
	private void selectFeatures(){
		String layerLocalName = layer.getFeatureSource().getSchema().getGeometryDescriptor().getLocalName();
		Filter filter = ff.bbox(ff.property(layerLocalName), bbox);

    	SimpleFeatureCollection selectedFeatures;
		try {
			selectedFeatures = (SimpleFeatureCollection) layer.getFeatureSource().getFeatures(filter);
		
	    	this.allSelectedFeatures.clear();
	    	this.allSelectedFeatures.addAll(selectedFeatures);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	


}
