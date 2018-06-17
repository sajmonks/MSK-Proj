package wat.shop.statistics;

import java.util.HashMap;

import hla.rti1516e.exceptions.RTIexception;
import wat.shop.abstracts.Federate;
import wat.shop.utils.Encoder;
import wat.shop.utils.Interaction;

public class StatisticsFederate extends Federate {
	
	HashMap<Integer, Double> startTimes = new HashMap<Integer, Double>();
	double averageSum;
	double averageDivide;
	
	
	private int clientsNum;
	private int checkoutsNum;
	
	private int sumProducts;
	private int sumProductsDivide;
	
	private double sumRatio;
	private double sumRatioDivide;

	@Override
	protected void onRun() throws RTIexception {
		
	}
	
	@Override
	protected void handleInteraction(Interaction inter) throws RTIexception {
		if(inter.getName().equals("NadszedlKlient")) {
			clientsNum++;
			
			int id = inter.getParameterInt(encoderFactory, "id");
			if(!startTimes.containsKey(id)) {
				startTimes.put(id, inter.getTime());
			}
		}
		else if(inter.getName().equals("OpuscilKlient")) {
			clientsNum--;
			
			int id = inter.getParameterInt(encoderFactory, "id");
			if(startTimes.containsKey(id)) {				
				double diff = inter.getTime() - startTimes.get(id);
				averageSum += diff;
				averageDivide++;
				
				double averageTime = averageSum / averageDivide;
				this.sendInteraction("SredniCzasPobytu", "mean", Encoder.encodeDouble(encoderFactory, averageTime), 0);
				startTimes.remove(id);
			}
		}
		else if(inter.getName().equals("OtwartoKase")) {
			checkoutsNum++;
			
			double ratio = clientsNum *1.0 / checkoutsNum;
			sumRatio += ratio;
			sumRatioDivide++;
			double averageRatio = sumRatio / sumRatioDivide;
			log("Œredni stosunek " + averageRatio);
			this.sendInteraction("SredniStosunek", "mean", Encoder.encodeDouble(encoderFactory, averageRatio), 0);
		}	
		else if(inter.getName().equals("ZamknietoKase")) {
			checkoutsNum--;
			
			double ratio = clientsNum *1.0 / checkoutsNum;
			sumRatio += ratio;
			sumRatioDivide++;
			double averageRatio = sumRatio / sumRatioDivide;
			log("Œredni stosunek " + averageRatio);
			this.sendInteraction("SredniStosunek", "mean", Encoder.encodeDouble(encoderFactory, averageRatio), 0);
		}	
		else if(inter.getName().equals("PodejscieDoKasy")) {
			sumProducts += inter.getParameterInt(encoderFactory, "products");
			sumProductsDivide++;
			double averageProductNum = (sumProducts*1.0) / sumProductsDivide;
			log("Œrednia iloœæ produktów: " + averageProductNum);
			this.sendInteraction("SredniaIloscZakupow", "mean", Encoder.encodeDouble(encoderFactory, averageProductNum), 0);
		}
		else if(inter.getName().equals("ZwolnienieKasy")) {
			
		}
	}

	@Override
	protected void publishAndSubscribe() throws RTIexception {
		this.subscribeToInteraction("NadszedlKlient", new String[] {"id"} );
		this.subscribeToInteraction("OpuscilKlient",  new String[] {"id"});
		this.subscribeToInteraction("PodejscieDoKasy", new String[] {"id", "queueid", "products"});
		this.subscribeToInteraction("ZwolnienieKasy", new String[] {"qid"});
		this.subscribeToInteraction("OtwartoKase", new String[] {"qid"});
		this.subscribeToInteraction("ZamknietoKase", new String[] {"qid"});
		
		this.publishInteraction("SredniCzasPobytu");
		this.publishInteraction("SredniaIloscZakupow");
		this.publishInteraction("SredniStosunek");
	}
	
	public static void main( String[] args )
	{
		try
		{
			StatisticsFederate federate = new StatisticsFederate();
			StatisticsAmbassador ambassador = new StatisticsAmbassador(federate, 1.0);
			federate.runFederate( "StatisticsFederate", "FederateType", "ShopFederation", 1.0, ambassador);
		}
		catch( Exception rtie )
		{
			rtie.printStackTrace();
		}
	}

}
