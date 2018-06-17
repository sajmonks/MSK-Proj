package wat.shop.gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GUIApplication extends Application {
	
	public static GUIController controller;
	
	Thread windowThread;
	
	public GUIApplication () {
	}
	
	public void run(String[] args) {
		launch(GUIApplication.class, args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("GUI.fxml"));	
		
		GUIApplication.controller = new GUIController(this);
		loader.setController(controller);
		
		if(controller != null) {
			System.out.println("Controller not null");
		}
		
		Scene scene = new Scene(loader.load());
		
		primaryStage.setTitle("Zobrazowanie symulacji");
		primaryStage.setScene(scene);
		
		primaryStage.setOnHiding(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent arg0) {
				if(windowThread != null) {
					windowThread.interrupt();
				}
			}
		});
		
		primaryStage.show();
	}
	
	public GUIController getController() {
		return controller;
	}

}
