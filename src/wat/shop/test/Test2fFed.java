package wat.shop.test;

import hla.rti1516e.exceptions.RTIexception;
import wat.shop.abstracts.Federate;
import wat.shop.utils.Interaction;

public class Test2fFed extends Federate{
	@Override
	protected void onRun() throws RTIexception {
					
	}

	@Override
	protected void publishAndSubscribe() throws RTIexception {
		this.subscribeToInteraction("NadszedlKlient", new String[] {"id"});
	}

	@Override
	protected void handleInteraction(Interaction i) throws RTIexception {
		
	}
	
	public static void main( String[] args )
	{
		try
		{
			Test2fFed federate = new Test2fFed();
			TestAmbassador ambassador = new TestAmbassador(federate, 5.0);
			federate.runFederate( "Test2", "Test2Type", "ShopFederation", 5.0, ambassador);
		}
		catch( Exception rtie ) { rtie.printStackTrace(); }
	}
}
