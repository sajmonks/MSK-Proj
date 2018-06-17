package wat.shop.checkout;

import java.util.ArrayList;

import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import wat.shop.abstracts.Federate;
import wat.shop.utils.Encoder;
import wat.shop.utils.Interaction;

public class CheckoutFederate extends Federate{

	private ArrayList<Integer> queues = new ArrayList<Integer>();
	private ArrayList<Integer> customerHandling = new ArrayList<Integer>();
	private int maxQueueSize = 5;
	
	@Override
	protected void onRun() throws RTIexception {		
		for(int j = 0; j < queues.size(); j++) { 
			if(queues.get(j) == 0) {
				queues.set(j, -1);
				customerHandling.set(j, -1);
				this.sendInteraction("ZamknietoKase", "qid", Encoder.encodeInt(encoderFactory, j), 0);
				log("Zwalnianie kasy");
				break;
			}
		}
		
	}

	@Override
	protected void handleInteraction(Interaction inter) throws RTIexception {
		if(inter.getName() == "KoniecZakupow") {
			int id = inter.getParameterInt(encoderFactory, "id");
			if(queues.size() == 0) { createQueue(); }
			
			//Petla szuka po kolejkach wolne miejsce, jesli wszystkie sa pelne to otwiera nowa kolejke
			int qid = -1;
			for(int j = 0; j < queues.size(); j++) {
				if(queues.get(j) == -1)
					continue;
				
				if(queues.get(j) < maxQueueSize) {
					qid = j;
					break;
				}
			}
			if(qid == -1) {
				qid = createQueue();
			}
			
			queues.set(qid, queues.get(qid) + 1); //Jak znaleziono to zwiekszenie miejsca o jeden
			
			try {
				byte[] idData = Encoder.encodeInt(encoderFactory, id);
				byte[] qidData = Encoder.encodeInt(encoderFactory, qid);
				this.sendInteraction("PrzydzielenieDoKolejki", "id", idData, "qid", qidData, 0);
			} catch (RTIinternalError e) {
				e.printStackTrace();
			}
			
			log("Wysylanie o przydzieleniu klienta=" + id + ", do kolejki=" + qid);
		}
		else if(inter.getName() == "PodejscieDoKasy") {
			int id = inter.getParameterInt(encoderFactory, "id");
			int qid = inter.getParameterInt(encoderFactory, "queueid");
			int products = inter.getParameterInt(encoderFactory, "products");
			System.out.println("TEST " + id + " " + qid + " " +products);
			customerHandling.set(qid, products);
			log("Rozpoczecie obslugi klienta="+id+", w kasie="+qid+", ilosc produktow="+products);
		}
		
		for(int j = 0 ; j < customerHandling.size(); j++) {
			int products = customerHandling.get(j);
			products--;
			if(products == 0) {
				try {
					byte [] qidData = Encoder.encodeInt(encoderFactory, j);
					this.sendInteraction("ZwolnienieKasy", "qid", qidData, 0);
					log("Wysylanie zwolnienia kasy="+j);
					queues.set(j, queues.get(j) - 1);
					customerHandling.set(j, -1);
					
				} catch (RTIinternalError e) {
					e.printStackTrace();
				}
			}				
			else {
				customerHandling.set(j, products);
			}
		}
		
	}
	@Override
	protected void publishAndSubscribe() throws RTIexception {
		this.publishInteraction("PrzydzielenieDoKolejki");
		this.publishInteraction("ZwolnienieKasy");
		this.publishInteraction("OtwartoKase");
		this.publishInteraction("ZamknietoKase");
		this.subscribeToInteraction("KoniecZakupow", new String[] {"id"});
		this.subscribeToInteraction("PodejscieDoKasy", new String[] {"id", "queueid", "products"});
	}
	
	private int createQueue() throws RTIexception {
		
		for(int j = 0; j < queues.size(); j++)
		{
			if(queues.get(j) == -1) {
				queues.set(j, 0);
				customerHandling.set(j, -1 );
				log("Tworzenie kolejki id=" + j );
				this.sendInteraction("OtwartoKase", "qid", Encoder.encodeInt(encoderFactory, j), 0 );
				return j;
			}
		}
		
		queues.add(0);
		customerHandling.add(-1);
		log("Tworzenie kolejki id=" + (queues.size() - 1) );
		this.sendInteraction("OtwartoKase", "qid", Encoder.encodeInt(encoderFactory, (queues.size() - 1)) , 0);
		return queues.size() - 1;
	}
	
	public static void main( String[] args )
	{
		try
		{
			CheckoutFederate federate = new CheckoutFederate();
			CheckoutAmbassador ambassador = new CheckoutAmbassador(federate, 1.0);
			federate.runFederate( "CheckoutFederate", "CheckoutType", "ShopFederation", 1.0, ambassador);
		}
		catch( Exception rtie ) { rtie.printStackTrace(); }
	}


}
