package wat.shop.shop;

import java.util.Random;

import hla.rti1516e.exceptions.RTIexception;
import wat.shop.abstracts.Federate;
import wat.shop.utils.Encoder;
import wat.shop.utils.Interaction;

public class ShopFederate extends Federate {

	private int lastId = 0;
	
	//Uruchamiane w kazdym ticku symulacji
	@Override
	protected void onRun() throws RTIexception {
		//%25 procent na przyjscie klienta w jednym ticku
		if(new Random().nextInt(2) == 0) 
		{
			this.sendInteraction("NadszedlKlient", "id", Encoder.encodeInt(encoderFactory, lastId), 0); //Wysylanie interakcji
			log("Wysylanie nadejcie klienta.");
			lastId++;
		}
		this.waitForUser();
	}
	
	@Override
	protected void handleInteraction(Interaction i) throws RTIexception {
		
	}

	@Override
	protected void publishAndSubscribe() throws RTIexception {
		this.publishInteraction("NadszedlKlient"); //Informuje ze publikuje NadszedlKlient
	}
	
	public static void main( String[] args )
	{
		try
		{
			ShopFederate federate = new ShopFederate();
			ShopAmbassador ambassador = new ShopAmbassador(federate, 1.0);
			federate.runFederate( "ShopFederate", "ShopType", "ShopFederation", 1.0, ambassador);
		}
		catch( Exception rtie )
		{
			rtie.printStackTrace();
		}
	}

}
