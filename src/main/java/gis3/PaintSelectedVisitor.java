package gis3;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class PaintSelectedVisitor implements Visitor {

	private MapFrame mapFrame;
	private enum GeomType { POINT, LINE, POLYGON };
	private GeomType geometryType;
//    private static final Color LINE_COLOUR = Color.BLACK;
//    private static final Color FILL_COLOUR = Color.WHITE;
    private Color LINE_COLOUR = Color.BLACK;
    private Color FILL_COLOUR = Color.WHITE;
    private Color SELECTED_COLOUR;
    private static final float LINE_WIDTH = 1.0f;
    private static final float POINT_SIZE = 1.0f;
    private static final float OPACITY = 1.0f;
    private StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    private String geometryAttributeName;

    PaintSelectedVisitor(){
    	this(Color.BLUE);
    }
    
    PaintSelectedVisitor(Color color){
    	this.SELECTED_COLOUR = color;
    	this.LINE_COLOUR = Color.BLACK;
    	this.FILL_COLOUR = Color.WHITE;
    }
    
    PaintSelectedVisitor(Color selected, Color line, Color fill){
    	this.SELECTED_COLOUR = selected;
    	this.LINE_COLOUR = line;
    	this.FILL_COLOUR = fill;
    }
    
	public void visit(MapFrame mapFrame) {
		this.mapFrame = mapFrame;
		setGeometry();
		displaySelectedFeatures();
	}
	
    /**
     * Retrieve information about the feature geometry
     */
    private void setGeometry() {
        GeometryDescriptor geomDesc = this.mapFrame.getSelectedLayer().getFeatureSource().getSchema().getGeometryDescriptor();
        geometryAttributeName = geomDesc.getLocalName();

        Class<?> clazz = geomDesc.getType().getBinding();

        if (Polygon.class.isAssignableFrom(clazz) ||
                MultiPolygon.class.isAssignableFrom(clazz)) {
            geometryType = GeomType.POLYGON;

        } else if (LineString.class.isAssignableFrom(clazz) ||
                MultiLineString.class.isAssignableFrom(clazz)) {

            geometryType = GeomType.LINE;

        } else {
            geometryType = GeomType.POINT;
        }
        
    }

	
	private void displaySelectedFeatures(){	
        /*
         * Use the filter to identify the selected features
         */
        try {
        	
            SimpleFeatureIterator iter = this.mapFrame.getSelectedFeatures().features();
            Set<FeatureId> IDs = new HashSet<FeatureId>();
            try {
                while (iter.hasNext()) {
                    SimpleFeature feature = iter.next();
                    IDs.add(feature.getIdentifier());

                }

            } finally {
                iter.close();
            }

            if (IDs.isEmpty()) {
                System.out.println("   no feature selected");
            }

            updateLayerStyles(IDs);

        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
	}
	
    /**
     * Sets the display to paint selected features yellow and
     * unselected features in the default style.
     *
     * @param IDs identifiers of currently selected features
     */
    private void updateLayerStyles(Set<FeatureId> IDs) {
        Style style;

        if (IDs.isEmpty()) {
            style = createDefaultStyle();

        } else {
            style = createSelectedStyle(IDs);
        }

        ((FeatureLayer) this.mapFrame.getSelectedLayer()).setStyle(style);
    }

    /**
     * Create a default Style for feature display
     */
    private Style createDefaultStyle() {
        Rule rule = createRule(LINE_COLOUR, FILL_COLOUR);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(rule);

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }
    
    /**
     * Helper for createXXXStyle methods. Creates a new Rule containing
     * a Symbolizer tailored to the geometry type of the features that
     * we are displaying.
     */
    private Rule createRule(Color outlineColor, Color fillColor) {
        Symbolizer symbolizer = null;
        Fill fill = null;
        Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(LINE_WIDTH));

        switch (geometryType) {
            case POLYGON:
                fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
                symbolizer = sf.createPolygonSymbolizer(stroke, fill, geometryAttributeName);
                break;

            case LINE:
                symbolizer = sf.createLineSymbolizer(stroke, geometryAttributeName);
                break;

            case POINT:
                fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));

                Mark mark = sf.getCircleMark();
                mark.setFill(fill);
                mark.setStroke(stroke);

                Graphic graphic = sf.createDefaultGraphic();
                graphic.graphicalSymbols().clear();
                graphic.graphicalSymbols().add(mark);
                graphic.setSize(ff.literal(POINT_SIZE));

                symbolizer = sf.createPointSymbolizer(graphic, geometryAttributeName);
        }

        Rule rule = sf.createRule();
        rule.symbolizers().add(symbolizer);
        return rule;
    }
    
    /**
     * Create a Style where features with given IDs are painted
     * yellow, while others are painted with the default colors.
     */
    private Style createSelectedStyle(Set<FeatureId> IDs) {
        return createSelectedStyle(IDs, SELECTED_COLOUR);
    }

    private Style createSelectedStyle(Set<FeatureId> IDs, Color color) {
        Rule selectedRule = createRule(color, color);
        selectedRule.setFilter(ff.id(IDs));

        Rule otherRule = createRule(LINE_COLOUR, FILL_COLOUR);
        otherRule.setElseFilter(true);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(selectedRule);
        fts.rules().add(otherRule);

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }



}
