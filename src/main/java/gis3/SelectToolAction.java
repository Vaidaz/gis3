package gis3;

import java.awt.event.ActionEvent;
import org.geotools.swing.action.NoToolAction;

@SuppressWarnings("serial")
public class SelectToolAction extends NoToolAction {

	private MapFrame mapFrame;
	
	public SelectToolAction(MapFrame mapFrame) {
		super(mapFrame.getMapPane());
		this.mapFrame = mapFrame;
	}
	
	@Override
	public void actionPerformed(ActionEvent ev) {
		getMapPane().setCursorTool(new SelectionTool(this.mapFrame));                    
	}


}
