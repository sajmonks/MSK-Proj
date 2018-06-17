package wat.shop.gui;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;

public class GUIController {
	
	private static HashMap<Integer, GUIDataHolder> holder = new HashMap<Integer, GUIDataHolder>();
	
	private GUIApplication application;
	
	@FXML
	private Label labelOpenedCheckouts;
	
	@FXML
	private Label labelAllClients;
	
	@FXML
	private Label labelAverageTime;
	
	@FXML
	private Label labelAverageRatio;
	
	@FXML
	private Label labelAverageProducts;
	
	@FXML
	private Label labelAllClientsTogether;
	
	@FXML
	private Label labelSimulationTime;
	
	@FXML
	private GridPane grid;
	
	@FXML
	public void initialize() {
		System.out.println("Controller initialized (TEST guiobject = " + (labelOpenedCheckouts == null ? "NULL" : "NOT NULL") );
		holder.clear();
	}
	
	public GUIController(GUIApplication app) {
		this.application = app;
	}
	
	public void updateNewThread() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				update();
			}
		});
	}
	
	public void checkoutOpenedThreaded(int qid) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				checkoutOpened(qid);
			}
		});
	}
	
	public void checkoutClosedThreaded(int qid) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				checkoutClosed(qid);
			}
		});
	}
	
	public void checkoutClientHandledThreaded(int client, int qid) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				checkoutClientHandled(client, qid);
			}
		});
	}
	
	public void checkoutClientComeThreaded(int qid) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				checkoutClientCome(qid);
			}
		});
	}
	
	public void checkoutReleaseThreaded(int qid) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				checkoutRelease(qid);
			}
		});
	}
	
	public void checkoutOpened(int qid) {
		if(holder.containsKey(qid)) {
			GUIDataHolder dh = holder.get(qid);
			dh.status = true;
			dh.update();
		}
		else {
			GUIDataHolder dh = new GUIDataHolder();
			dh.qid = qid;
			dh.update();
			
			grid.add(dh.qidLabel, 0, qid + 1);
			grid.add(dh.clientLabel, 1, qid + 1);
			grid.add(dh.countLabel, 2, qid + 1);
			grid.add(dh.statusLabel, 3, qid + 1);
			
			holder.put(qid, dh);
		}
	}
	
	public void checkoutClosed(int qid) {
		if(holder.containsKey(qid)) {
			GUIDataHolder dh = holder.get(qid);
			dh.client = -1;
			dh.status = false;
			dh.update();
		}
	}
	
	public void checkoutClientHandled(int client, int qid) {
		if(holder.containsKey(qid)) {
			GUIDataHolder dh = holder.get(qid);
			dh.client = client;
			dh.update();
		}
	}
	
	public void checkoutClientCome(int qid) {
		if(holder.containsKey(qid)) {
			GUIDataHolder dh = holder.get(qid);
			dh.count++;
			dh.update();
		}
	}
	
	public void checkoutRelease(int qid) {
		if(holder.containsKey(qid)) {
			GUIDataHolder dh = holder.get(qid);
			dh.count--;
			dh.update();
		}
	}
	
	
	public void update() {
		System.out.println( GUIFederate.instance.clientsInside );
		DecimalFormat df = new DecimalFormat("#.0000");
		labelAllClients.setText("" + GUIFederate.instance.clientsInside);
		labelOpenedCheckouts.setText("" + GUIFederate.instance.openedCheckouts);
		labelAverageRatio.setText("" + (GUIFederate.instance.averageRatio == 0 ? "0" : df.format(GUIFederate.instance.averageRatio)) );
		labelAverageTime.setText("" + (GUIFederate.instance.averageTime == 0 ? "0" : df.format(GUIFederate.instance.averageTime)));
		labelAverageProducts.setText("" + (GUIFederate.instance.averageProducts == 0 ? "0" : df.format(GUIFederate.instance.averageProducts)));
		labelAllClientsTogether.setText("" + GUIFederate.instance.clientsAll);
		labelSimulationTime.setText("" + GUIFederate.instance.getSimulationTime());
	}

	
	public GUIApplication getApp() {
		return application;
	}
}
