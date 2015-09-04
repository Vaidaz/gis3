package gis3;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;

@SuppressWarnings("serial")
public class MapFrame extends JMapFrame implements Visitable {
	
	private JButton zoomToSelectButton;
	private SimpleFeatureCollection selectedFeatures;
	private Layer selectedLayer;
	private ParamsToolBar parametersToolBar;
		 
	MapFrame(){
		super();
		setMapContent(new MapContent());
		this.selectedFeatures = new DefaultFeatureCollection();
		
		setLook();
		pause();
		addDataLayers();
	}
	
	private void pause(){
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void setLook(){
	    enableLayerTable(true);
	    enableStatusBar(true);
	    enableToolBar(true);
	    enableTool(Tool.PAN, Tool.RESET, Tool.ZOOM, Tool.INFO);
	    setSize(1000, 600);	   
	    resetSelectionTool();
//	    enableZoomToSelectButton();
	    
	    setVisible(true);
	    showParameters();
	}
	
	public void addDataLayers(){
//    	addLayer("shp/gis3/sven_SAV_P.shp", "sven_SAV_P", true);
//    	addLayer("shp/gis3/sven_PLO_P.shp", "sven_PLO_P", false);
//    	addLayer("shp/gis3/sven_PAS_P.shp", "sven_PAS_P", false);
//    	addLayer("shp/gis3/Vietoves_P.shp", "Vietoves_P", false);
//    	addLayer("shp/gis3/sven_HID_P.shp", "sven_HID_L", false);
		
    	addLayer("shp/gis3/sven_SAV_P.shp", "sven_SAV_P", true);
    	addLayer("shp/gis3/medziai_upes_ezerai.shp", "medziai_upes_ezerai", false);
    	addLayer("shp/gis3/sven_PAS_P.shp", "sven_PAS_P", false);
    	addLayer("shp/gis3/Vietoves_P.shp", "Vietoves_P", false);
    	addLayer("shp/gis3/sven_HID_P.shp", "sven_HID_L", false);
	}
	
	public void addLayer(String path, String title, boolean visible){
		File file = new File(path);
        FileDataStore store;
		try {
			store = FileDataStoreFinder.getDataStore(file);
	        SimpleFeatureSource featureSource = store.getFeatureSource();
	        
	        Style style = SLD.createSimpleStyle(featureSource.getSchema());
	        Layer layer = new FeatureLayer(featureSource, style);
	        layer.setTitle(title);
	        layer.setVisible(visible);
	        this.getMapContent().addLayer(layer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void enableZoomToSelectButton(){
		this.zoomToSelectButton = new JButton("Zoom to Select");
        getToolBar().addSeparator();
        getToolBar().add(zoomToSelectButton);
	}
	
	public void addZoomToSelectActionListener(ActionListener zoomToSelectListener){
		zoomToSelectButton.addActionListener(zoomToSelectListener);
	}
	
	public void addSelectActionListener(MouseEvent clicked){
	}
	
	public void resetSelectionTool(){
		getMapPane().setCursorTool(new SelectionTool(this));
		getToolBar().add(new SelectToolAction(this));
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	
	public void setSelectedFeatures(SimpleFeatureCollection features){
		this.selectedFeatures = features;
	}
	
	public SimpleFeatureCollection getSelectedFeatures(){
		return this.selectedFeatures;
	}

	public Layer getSelectedLayer() {
		return selectedLayer;
	}

	public void setSelectedLayer(Layer selectedLayer) {
		this.selectedLayer = selectedLayer;
	}
	
	public void showParameters(){		
		this.parametersToolBar = new ParamsToolBar(this);
        getToolBar().add(this.parametersToolBar);

	}
}
