package wat.shop.test;

import hla.rti1516e.exceptions.RTIexception;
import wat.shop.abstracts.Federate;
import wat.shop.utils.Encoder;
import wat.shop.utils.Interaction;

public class Test1Fed extends Federate {

	private int globalId = 0;
	
	@Override
	protected void onRun() throws RTIexception {
		for(int i = 0; i < 3; i++) {
			this.sendInteraction("NadszedlKlient", "id", Encoder.encodeInt(encoderFactory, globalId++), 4-i);
			System.out.println("Wysylanie id="+globalId+" na czas=" + (4-i + fedamb.federateLookahead + fedamb.federateTime));
		}
		this.waitForUser();
	}

	@Override
	protected void publishAndSubscribe() throws RTIexception {
		this.publishInteraction("NadszedlKlient");
		
	}

	@Override
	protected void handleInteraction(Interaction i) throws RTIexception {

		
	}
	
	public static void main( String[] args )
	{
		try
		{
			Test1Fed federate = new Test1Fed();
			TestAmbassador ambassador = new TestAmbassador(federate, 1.0);
			federate.runFederate( "Test1", "Test1Type", "ShopFederation", 1.0, ambassador);
		}
		catch( Exception rtie ) { rtie.printStackTrace(); }
	}

}
