package gis3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.map.Layer;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings("serial")
public class ParamsToolBar extends JToolBar implements ActionListener {
	
	private MapFrame mapFrame;
	private JTextField dumpAreaField;
	private JTextField distanceToForestField;
	private JTextField distanceToWaterField;
	private JTextField distanceToDistrictField;
	private int dumpAreaSize;
	private int distanceToForest;
	private int distanceToWater;
	private int distanceToDistrict;
	private Dimension fieldDimension;
	
	ParamsToolBar(MapFrame mapFrame){
		super();
		this.mapFrame = mapFrame;
		fieldDimension = new Dimension(26, 50);
		
		initializeFields();
		addField(this.dumpAreaField, "Plotas:");		
		addField(this.distanceToForestField, "Atstumas iki miško:");		
		addField(this.distanceToWaterField, "Atstumas iki vandens:");		
		addField(this.distanceToDistrictField, "Atstumas iki gyvenvietės:");
				
		JButton searchButton = new JButton("Ieškoti");
		searchButton.addActionListener(this);
		add(searchButton);

	}
	
	private void addField(JTextField field, String labelName){
		this.dumpAreaField.setMaximumSize(this.fieldDimension);
		JLabel label = new JLabel(labelName);
		add(label);
		add(field);
		addSeparator();
	}
	
	private void initializeFields(){
		this.dumpAreaField = new JTextField("0", 3);
		this.distanceToForestField = new JTextField("0", 3);
		this.distanceToWaterField = new JTextField("0", 3);
		this.distanceToDistrictField = new JTextField("0", 3);
	}
	
	public void actionPerformed(ActionEvent e) {
		SimpleFeature selectedDistrict2 = getSelectedFeature();
		Geometry g = (Geometry) selectedDistrict2.getDefaultGeometry();
		System.out.println(g.getArea());
		
		if(!fieldsAreValidAndInitiated()){
			return;
		}
		
		printWithTime("Pradžia");
		SimpleFeature selectedDistrict = getSelectedFeature();
		Layer selectedLayer = getSelectedLayer();

		printWithTime("Buferizuojam pasirinktą savivaldybę");
		Measure<Double, Length> distance = Measure.valueOf((double) maxDistance(), SI.METER);
		this.mapFrame.accept(new BufferVisitor(distance, "Buf. pasirinkta savivaldybė"));
				
		printWithTime("Darom intersect'ą su Vietoves_P");
		this.mapFrame.accept(new IntersectSelectedFeaturesVisitor("Vietoves_P", "Vietoves savivaldybėje"));		
		
		printWithTime("Vietoves buferizuojame " + this.distanceToDistrict + "m atstumu ir gaunam teritorijas kuriose negali būti savartyno");
		distance = Measure.valueOf((double) this.distanceToDistrict, SI.METER);
		this.mapFrame.accept(new BufferVisitor(distance, "Buf. Vietoves savivaldybėje"));

		printWithTime("Atimam buf. Vietoves savivaldybėje is buf. pasirinktos savivaldybės");
		setSelectedFeatureAndLayer(selectedDistrict, selectedLayer);
		this.mapFrame.accept(new DifferenceFeatureLayerVisitor("Buf. Vietoves savivaldybėje", "Savivaldybė, be vietovių"));

				
		printWithTime("Darom intersect'ą tarp 'Buf. savivaldybė, be vietovių' ir 'medziai_upes_ezerai'");
		this.mapFrame.accept(new IntersectSelectedFeaturesVisitor("medziai_upes_ezerai", "Medžių, upių, ežerų plotai"));		
		
		printWithTime("Naują layer buferizuojame " + this.distanceToForest + "m atstumu ir gaunam teritorijas kuriose negali būti savartyno");
		distance = Measure.valueOf((double) this.distanceToForest, SI.METER);
		this.mapFrame.accept(new BufferVisitor(distance, "Buf. medžių, upių, ežerų plotai"));

		
		printWithTime("Atimam buf. medžių, upių, ežerų plotus is buf. pasirinktos savivaldybės");
		selectLayerWithHisFeatures("Savivaldybė, be vietovių");
		this.mapFrame.accept(new DifferenceFeatureLayerVisitor("Buf. medžių, upių, ežerų plotai", "Buf. savivaldybė, be medžių, upių, ežerų plotų"));

		printWithTime("Darom naujo layer intersect'ą su sven_HID_P");
		this.mapFrame.accept(new IntersectSelectedFeaturesVisitor("sven_HID_L", "Upės"));	
		printWithTime("Buferizuojam " + this.distanceToWater + "m atstumu ir gaunam teritorijas kuriose negali būti savartyno");
		distance = Measure.valueOf((double) this.distanceToWater, SI.METER);
		this.mapFrame.accept(new BufferVisitor(distance, "Buf. upės"));

		printWithTime("Atimam buf. upes is buf. savivaldybė, be medžių, upių, ežerų plotų");
		selectLayerWithHisFeatures("Buf. savivaldybė, be medžių, upių, ežerų plotų");
		this.mapFrame.accept(new DifferenceFeatureLayerVisitor("Buf. upės", "Buf. savivaldybė, be medžių, upių, ežerų plotų, hidro"));
		
		printWithTime("Darom naujo layer intersect'ą su sven_PAS_P");
		this.mapFrame.accept(new IntersectSelectedFeaturesVisitor("sven_PAS_P", "Pastatai"));	
		printWithTime("Buferizuojam " + 50 + "m atstumu ir gaunam teritorijas kuriose negali būti savartyno");
		distance = Measure.valueOf((double) 50, SI.METER);
		this.mapFrame.accept(new BufferVisitor(distance, "Buf. pastatai"));

		printWithTime("Atimam Buf. pastatai is Buf. savivaldybė, be medžių, upių, ežerų plotų, hidro");
		selectLayerWithHisFeatures("Buf. savivaldybė, be medžių, upių, ežerų plotų, hidro");
		this.mapFrame.accept(new DifferenceFeatureLayerVisitor("Buf. pastatai", "Buf. savivaldybė, be medžių, upių, ežerų plotų, hidro, pastatu"));

		printWithTime("Keičiam multipoligoną į poligonų sluoksnį");
		this.mapFrame.accept(new ConvertMultypoligonsToPolyginsVisitor("Visi plotai savartynui"));
		
		printWithTime("Paliekam tik tuos plotus, kurių plotas didesnis nei: " + this.dumpAreaSize + " kv. m");
		this.mapFrame.accept(new FilterByAreaVisitor(this.dumpAreaSize, "Tinkami plotai savartynui"));

		
		this.mapFrame.accept(new PaintSelectedVisitor(Color.RED, Color.BLACK, Color.WHITE));

		printWithTime("Baigiam darbą.");

	}
	
//	public void actionPerformed(ActionEvent e) {
//		if(!fieldsAreValidAndInitiated()){
//			return;
//		}
//
//		System.out.println(currentTime(" - ") + "Pradžia");
//		SimpleFeature selectedDistrict = getSelectedFeature();
//		Layer selectedLayer = getSelectedLayer();
//		
//		System.out.println(currentTime(" - ") + "Buferizuojam pasirinktą savivaldybę");
//		Measure<Double, Length> distance = Measure.valueOf((double) maxDistance(), SI.METER);
//		this.mapFrame.accept(new BufferVisitor(distance, "Buf. pasirinkta savivaldybė"));
//		
//		SimpleFeature bufferedDistrict = getSelectedFeature();
//		Layer bufferedLayer = getSelectedLayer();
//				
//		System.out.println(currentTime(" - ") + "Darom intersect'ą su sven_PLO_P");
//		this.mapFrame.accept(new IntersectSelectedFeaturesVisitor("sven_PLO_P", "Visi plotai"));		
//		System.out.println(currentTime(" - ") + "Atrenkam tik ms0, hd1, hd2, hd3 geokodo atributus");
//		List<String> codeNames = Arrays.asList("ms0", "hd1", "hd2", "hd3");
//		this.mapFrame.accept(new FilterSelectedVisitior("sven_PLO_P_GKODAS", codeNames, "Reikiami plotai"));
//		System.out.println(currentTime(" - ") + "Buferizuojam " + this.distanceToForest + "m atstumu ir gaunam teritorijas kuriose negali būti savartyno");
//		distance = Measure.valueOf((double) this.distanceToForest, SI.METER);
//		this.mapFrame.accept(new BufferVisitor(distance, "Buf. reikiami plotai"));
//				
//		System.out.println(currentTime(" - ") + "Darom intersect'ą su sven_PAS_P");
//		setSelectedFeatureAndLayer(selectedDistrict, selectedLayer);
//		this.mapFrame.accept(new IntersectSelectedFeaturesVisitor("sven_PAS_P", "Pastatai"));		
//		System.out.println(currentTime(" - ") + "Buferizuojam 50m atstumu (simboliškai)");
//		distance = Measure.valueOf((double) 50.0, SI.METER);
//		this.mapFrame.accept(new BufferVisitor(distance, "Buf. pastatai"));
//		
//		System.out.println(currentTime(" - ") + "Darom intersect'ą su Vietoves_P");
//		setSelectedFeatureAndLayer(bufferedDistrict, bufferedLayer);
//		this.mapFrame.accept(new IntersectSelectedFeaturesVisitor("Vietoves_P", "Miestai"));
//		distance = Measure.valueOf((double) this.distanceToDistrict, SI.METER);
//		System.out.println(currentTime(" - ") + "Buferizuojam " + this.distanceToDistrict + "m atstumu ir gaunam teritorijas kuriose negali būti savartyno");
//		this.mapFrame.accept(new BufferVisitor(distance, "Buf. miestų plotai"));
//		
//		System.out.println(currentTime(" - ") + "Darom intersect'ą su sven_HID_L");
//		setSelectedFeatureAndLayer(bufferedDistrict, bufferedLayer);
//		this.mapFrame.accept(new IntersectSelectedFeaturesVisitor("sven_HID_L", "Vandenys"));	
//		System.out.println(currentTime(" - ") + "Buferizuojam " + this.distanceToWater + "m atstumu ir gaunam teritorijas kuriose negali būti savartyno");
//		distance = Measure.valueOf((double) this.distanceToWater, SI.METER);
//		this.mapFrame.accept(new BufferVisitor(distance, "Buf. vandenys"));
//
//		System.out.println(currentTime(" - ") + "Atimam vietas, kur negalima statyti savartyno");
//		setSelectedFeatureAndLayer(selectedDistrict, selectedLayer);
//		List<String> layersToSubstract = Arrays.asList("Buf. vandenys", "Buf. miestų plotai", "Buf. pastatai", "Buf. reikiami plotai");
//		this.mapFrame.accept(new DifferenceFeatureLayerVisitor(layersToSubstract, "Galimos vietos"));
//		
//		System.out.println(currentTime(" - ") + "Atrenkam tik tas kuriu plotas > " + this.dumpAreaSize);
//		this.mapFrame.accept(new FilterByAreaVisitor(this.dumpAreaSize, "Norimo savartyno vietos"));
//		this.mapFrame.accept(new PaintSelectedVisitor(Color.YELLOW, Color.BLACK, Color.WHITE));
//		
//		
//		setSelectedFeatureAndLayer(selectedDistrict, selectedLayer);
//				
//		this.mapFrame.accept(new PaintSelectedVisitor(Color.BLUE, Color.BLACK, Color.WHITE));
//
//		System.out.println(currentTime(" - ") + "Baigiam darbą.");
//	}
		
	private boolean fieldsAreValidAndInitiated(){
		boolean valid = true;
		
		try {
			this.dumpAreaSize = Integer.parseInt(this.dumpAreaField.getText());
			this.distanceToForest = Integer.parseInt(this.distanceToForestField.getText());
			this.distanceToWater = Integer.parseInt(this.distanceToWaterField.getText());
			this.distanceToDistrict = Integer.parseInt(this.distanceToDistrictField.getText());
		} catch(NumberFormatException e){
			valid = false;
		}
		
		if(this.mapFrame.getSelectedFeatures().size() == 0){
			valid = false;
		}
			
		return valid;
	}
	
	private int maxDistance(){
		return Math.max(this.distanceToForest, Math.max(this.distanceToWater, this.distanceToDistrict));
	}
	
	private SimpleFeature getSelectedFeature(){
		return this.mapFrame.getSelectedFeatures().features().next();
	}
	
	private Layer getSelectedLayer(){
		return this.mapFrame.getSelectedLayer();
	}
	
	private void setSelectedLayer(Layer layer){
		this.mapFrame.setSelectedLayer(layer);
	}
	
	private void setSelectedFeaure(SimpleFeature feature){
		DefaultFeatureCollection features = new DefaultFeatureCollection();
		features.add(feature);
		this.mapFrame.setSelectedFeatures(features);
	}
	
	private void setSelectedFeatureAndLayer(SimpleFeature feature, Layer layer){
		setSelectedFeaure(feature);
		setSelectedLayer(layer);
	}
	
	private String currentTime(String ending){
		Date now = new java.util.Date();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		
		return sdf.format(now.getTime()) + ending;
	}
	
	private void selectLayerWithHisFeatures(String layerName){
		Layer layer = findLayer(layerName);
		try {
			this.mapFrame.setSelectedFeatures((SimpleFeatureCollection) layer.getFeatureSource().getFeatures());
		} catch (IOException e) {
			e.printStackTrace();
			setSelectedFeaure(null);
		}
		setSelectedLayer(layer);
	}
	
	private Layer findLayer(String layerName){
		for(Layer layer : mapFrame.getMapContent().layers()){
			if(layer.getTitle().equals(layerName)){
				return layer;
			}
		}
		
		return null;
	}
	
	private void printWithTime(String text){
		System.out.println(currentTime(" - ") + text);
	}

}
