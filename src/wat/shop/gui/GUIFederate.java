package wat.shop.gui;

import hla.rti1516e.exceptions.RTIexception;
import wat.shop.abstracts.Federate;
import wat.shop.utils.Interaction;

public class GUIFederate extends Federate {
	
	public static GUIFederate instance;
	public GUIApplication application;
	
	public int openedCheckouts = 0;
	public int clientsInside = 0;
	
	public double averageRatio = 0;
	public double averageTime = 0;
	public double averageProducts = 0;
	
	public GUIFederate() {
	}

	@Override
	protected void onRun() throws RTIexception {
	}
	

	@Override
	protected void handleInteraction(Interaction inter) throws RTIexception {
		if(inter.getName().equals("NadszedlKlient")) {
			clientsInside++;
		}
		else if(inter.getName().equals("OpuscilKlient")) {
			clientsInside--;
		}
		else if(inter.getName().equals("OtwartoKase")) {
			openedCheckouts++;
			int qid = inter.getParameterInt(encoderFactory, "qid");
			GUIApplication.controller.checkoutOpenedThreaded(qid);
		}
		else if(inter.getName().equals("ZamknietoKase")) {
			openedCheckouts--;
			GUIApplication.controller.checkoutClosedThreaded(inter.getParameterInt(encoderFactory, "qid"));
		}
		else if(inter.getName().equals("PrzydzielenieDoKolejki")) {
			log("TEST PRZYDZIELENIE");
			GUIApplication.controller.checkoutClientComeThreaded(
					inter.getParameterInt(encoderFactory, "qid")
					);
		}
		else if(inter.getName().equals("PodejscieDoKasy")) {
			log("TEST PODEJSCIE");
			GUIApplication.controller.checkoutClientHandledThreaded(
					inter.getParameterInt(encoderFactory, "id"),
					inter.getParameterInt(encoderFactory, "queueid")
					);
		}
		else if(inter.getName().equals("ZwolnienieKasy")) {
			GUIApplication.controller.checkoutReleaseThreaded(
					inter.getParameterInt(encoderFactory, "qid")
					);
		}
		else if(inter.getName().equals("SredniCzasPobytu")) {
			averageTime = inter.getParameterDouble(encoderFactory, "mean");
		}
		else if(inter.getName().equals("SredniaIloscZakupow")) {
			averageProducts = inter.getParameterDouble(encoderFactory, "mean");
		}
		else if(inter.getName().equals("SredniStosunek")) {
			averageRatio = inter.getParameterDouble(encoderFactory, "mean");
		}
		
		System.out.println("Aktualizacja");
		GUIApplication.controller.updateNewThread();
	}


	@Override
	protected void publishAndSubscribe() throws RTIexception {
		this.subscribeToInteraction("OtwartoKase", new String[] {"qid"});
		this.subscribeToInteraction("NadszedlKlient", new String[] {"id"});
		this.subscribeToInteraction("OpuscilKlient", new String[] {"id"});
		this.subscribeToInteraction("ZamknietoKase", new String[] {"qid"});
		this.subscribeToInteraction("ZwolnienieKasy", new String[] {"qid"});
		this.subscribeToInteraction("PrzydzielenieDoKolejki", new String[] {"id", "qid"});
		this.subscribeToInteraction("PodejscieDoKasy", new String[] {"id", "queueid", "products"});
		this.subscribeToInteraction("SredniCzasPobytu", new String[] {"mean"});
		this.subscribeToInteraction("SredniaIloscZakupow", new String[] {"mean"});
		this.subscribeToInteraction("SredniStosunek", new String[] {"mean"});
	
	}
	
	public static void main( String[] args )
	{
		try
		{
			GUIFederate.create();
			GUIApplication application = new GUIApplication();		
			GUIAmbassador ambassador = new GUIAmbassador(GUIFederate.instance, 1.0);
			
			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						GUIFederate.instance.runFederate("GUIFederate", "GUIType", "ShopFederation", 1, ambassador);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};	
			thread.start();

			application.run(args);
			
		}
		catch( Exception rtie ) { rtie.printStackTrace(); }
	}
	
	public static void create() {
		if(instance == null)
			instance = new GUIFederate();
	}
}
