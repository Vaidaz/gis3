package gis3;

import java.awt.Color;

import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;

public class SelectionTool extends CursorTool {

	private MapFrame mapFrame;
	
	SelectionTool(MapFrame mapFrame){
		super();
		this.mapFrame = mapFrame;
	}

	@Override
	public void onMouseClicked(MapMouseEvent ev) {		
		this.mapFrame.accept(new SelectAreaVisitor(ev));		
		this.mapFrame.accept(new PaintSelectedVisitor(Color.BLUE, Color.BLACK, Color.WHITE));
	}
	
}
