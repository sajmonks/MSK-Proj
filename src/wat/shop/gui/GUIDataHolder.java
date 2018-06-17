package wat.shop.gui;

import javafx.scene.control.Label;

public class GUIDataHolder {
	public int qid;
	public int client;
	public int count;
	boolean status;
	
	public Label qidLabel;
	public Label clientLabel;
	public Label countLabel;
	public Label statusLabel;
	
	public GUIDataHolder() {
		qidLabel = new Label();
		clientLabel = new Label();
		countLabel = new Label();
		statusLabel = new Label();
		
		this.qid = 0;
		this.client = -1;
		this.count = 0;
		status = true;
	}
	
	public void update() {
		qidLabel.setText("" + qid);
		clientLabel.setText("" + (client == -1 ? "Brak" : "" + client) );
		countLabel.setText("" + (count <= 0 ? "Brak" : "" + count) );
		statusLabel.setText("" + (status == true ? "Otwarta" : "Zamkniêta") );
	}
}
