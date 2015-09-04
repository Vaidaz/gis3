package gis3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.map.Layer;
import org.opengis.feature.simple.SimpleFeature;

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
		if(!fieldsAreValidAndInitiated()){
			return;
		}

		System.out.println(currentTime(" - ") + "Pradžia");
		SimpleFeature selectedDistrict = getSelectedFeature();
		Layer selectedLayer = getSelectedLayer();
		
		System.out.println(currentTime(" - ") + "Buferizuojam pasirinktą savivaldybę");
		Measure<Double, Length> distance = Measure.valueOf((double) maxDistance(), SI.METER);
		this.mapFrame.accept(new BufferVisitor(distance, "Buf. pasirinkta savivaldybė"));
		
		SimpleFeature bufferedDistrict = getSelectedFeature();
		Layer bufferedLayer = getSelectedLayer();
				
		System.out.println(currentTime(" - ") + "Darom intersect'ą su sven_PLO_P");
		this.mapFrame.accept(new IntersectSelectedFeaturesVisitor("sven_PLO_P", "Visi plotai"));		
		System.out.println(currentTime(" - ") + "Atrenkam tik ms0, hd1, hd2, hd3 geokodo atributus");
		List<String> codeNames = Arrays.asList("ms0", "hd1", "hd2", "hd3");
		this.mapFrame.accept(new FilterSelectedVisitior("sven_PLO_P_GKODAS", codeNames, "Reikiami plotai"));
		System.out.println(currentTime(" - ") + "Buferizuojam " + this.distanceToForest + "m atstumu ir gaunam teritorijas kuriose negali būti savartyno");
		distance = Measure.valueOf((double) this.distanceToForest, SI.METER);
		this.mapFrame.accept(new BufferVisitor(distance, "Buf. reikiami plotai"));
				
		System.out.println(currentTime(" - ") + "Darom intersect'ą su sven_PAS_P");
		setSelectedFeatureAndLayer(selectedDistrict, selectedLayer);
		this.mapFrame.accept(new IntersectSelectedFeaturesVisitor("sven_PAS_P", "Pastatai"));		
		System.out.println(currentTime(" - ") + "Buferizuojam 50m atstumu (simboliškai)");
		distance = Measure.valueOf((double) 50.0, SI.METER);
		this.mapFrame.accept(new BufferVisitor(distance, "Buf. pastatai"));
		
		System.out.println(currentTime(" - ") + "Darom intersect'ą su Vietoves_P");
		setSelectedFeatureAndLayer(bufferedDistrict, bufferedLayer);
		this.mapFrame.accept(new IntersectSelectedFeaturesVisitor("Vietoves_P", "Miestai"));
		distance = Measure.valueOf((double) this.distanceToDistrict, SI.METER);
		System.out.println(currentTime(" - ") + "Buferizuojam " + this.distanceToDistrict + "m atstumu ir gaunam teritorijas kuriose negali būti savartyno");
		this.mapFrame.accept(new BufferVisitor(distance, "Buf. miestų plotai"));
		
		System.out.println(currentTime(" - ") + "Darom intersect'ą su sven_HID_L");
		setSelectedFeatureAndLayer(bufferedDistrict, bufferedLayer);
		this.mapFrame.accept(new IntersectSelectedFeaturesVisitor("sven_HID_L", "Vandenys"));	
		System.out.println(currentTime(" - ") + "Buferizuojam " + this.distanceToWater + "m atstumu ir gaunam teritorijas kuriose negali būti savartyno");
		distance = Measure.valueOf((double) this.distanceToWater, SI.METER);
		this.mapFrame.accept(new BufferVisitor(distance, "Buf. vandenys"));

		System.out.println(currentTime(" - ") + "Atimam vietas, kur negalima statyti savartyno");
		setSelectedFeatureAndLayer(selectedDistrict, selectedLayer);
		List<String> layersToSubstract = Arrays.asList("Buf. vandenys", "Buf. miestų plotai", "Buf. pastatai", "Buf. reikiami plotai");
		this.mapFrame.accept(new DifferenceFeatureLayerVisitor(layersToSubstract, "Galimos vietos"));
		
		System.out.println(currentTime(" - ") + "Atrenkam tik tas kuriu plotas > " + this.dumpAreaSize);
		this.mapFrame.accept(new FilterByAreaVisitor(this.dumpAreaSize, "Norimo savartyno vietos"));
		this.mapFrame.accept(new PaintSelectedVisitor(Color.YELLOW, Color.BLACK, Color.WHITE));
		
		
		setSelectedFeatureAndLayer(selectedDistrict, selectedLayer);
				
		this.mapFrame.accept(new PaintSelectedVisitor(Color.BLUE, Color.BLACK, Color.WHITE));

		System.out.println(currentTime(" - ") + "Baigiam darbą.");
	}
		
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

}
