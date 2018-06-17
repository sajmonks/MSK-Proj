package wat.shop.customer;

import java.util.ArrayList;

import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import wat.shop.abstracts.Federate;
import wat.shop.utils.Encoder;
import wat.shop.utils.Interaction;

public class CustomerFederate extends Federate {
	
	private ArrayList<Customer> customerList = new ArrayList<Customer>();
	private CustomerQueue customerQueue = new CustomerQueue(this);

	@Override
	protected void onRun() throws RTIexception {		
		//Obsluga klienta ktory konczy robic zakupy i idzie do kasy
		for(Customer c : customerList) {
			c.advanceShoppingTime();
			if(c.getShoppingTime() == 0) {
				log("Klient " + c.getId() + " zakonczyl zakupy");
				try {
					byte[] id = Encoder.encodeInt(encoderFactory, c.getId());
					this.sendInteraction("KoniecZakupow", "id", id, 0);
				} catch (RTIinternalError e) {
					e.printStackTrace();
				}
			}		
		}
		
		for(int i : customerQueue.getQueues()) {
			Customer customer = getCustomer(customerQueue.get(i, 0));
			if(customer == null)
				continue;
				
			if(!customer.isChecking()) {
				customer.setChecking();
				try {
					byte[] id = Encoder.encodeInt(encoderFactory, customer.getId());
					byte[] qid = Encoder.encodeInt(encoderFactory, i);
					byte[] products = Encoder.encodeInt(encoderFactory, customer.getProductNum());
					System.out.println( "WYSY£ANIE " + customer.getId() + " " + i + " " + customer.getProductNum());
					this.sendInteraction("PodejscieDoKasy", "id", id, "queueid", qid, "products", products, 0);
				} catch (RTIinternalError e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	protected void handleInteraction(Interaction inter) throws RTIexception {
		if(inter.getName() == "NadszedlKlient") {
			Customer customer = new Customer();
			log("Dodano klienta id = " + customer.getId() + ", przywilej=" + customer.isPrivileged() + 
					", czas zakupow=" + customer.getShoppingTime() + ", ilosc zakupow=" + customer.getProductNum() +
					", uprzywilejowany=" + customer.isPrivileged());
			customerList.add(customer);
		}
		if(inter.getName() == "PrzydzielenieDoKolejki") {
			int id = inter.getParameterInt(encoderFactory, "id");
			int qid = inter.getParameterInt(encoderFactory, "qid");
			Customer customer = getCustomer(id);
			
			int pos = customerQueue.addCustomer(customer, qid);
			log("Klient id=" + id + ", podchodzi do kasy="+qid+", na pozycji=" + pos);
		}
		if(inter.getName() == "ZwolnienieKasy") {
			int qid = inter.getParameterInt(encoderFactory, "qid");
			int id = customerQueue.get(qid, 0); //Pobieranie id z pierwszej pozycji kolejki qid
			customerList.remove(getCustomer(id)); //Usuwanie z listy klientow
			customerQueue.removeFirst(qid); //Usuwanie z kolejki
			
			log("Klient="+id+", opuszcza sklep.");
			this.sendInteraction("OpuscilKlient", "id", Encoder.encodeInt(encoderFactory, id), 0);
		}
	}

	@Override
	protected void publishAndSubscribe() throws RTIexception {
		this.publishInteraction("KoniecZakupow");
		this.publishInteraction("PodejscieDoKasy");
		this.publishInteraction("OpuscilKlient");
		this.subscribeToInteraction("PrzydzielenieDoKolejki", new String[] {"id", "qid"});
		this.subscribeToInteraction("ZwolnienieKasy", new String[] {"qid"});
		this.subscribeToInteraction("NadszedlKlient", null); //Informuje ze subskrybuje NadszedlKlient
	}
	
	public Customer getCustomer(int id) {
		for(Customer c : customerList) {
			if(c.getId() == id) {
				return c;
			}
		}
		return null;
	}
	
	public static void main( String[] args )
	{
		try
		{
			CustomerFederate federate = new CustomerFederate();
			CustomerAmbassador ambassador = new CustomerAmbassador(federate, 1.0);
			federate.runFederate( "CustomerFederate", "CustomerType", "ShopFederation", 1.0, ambassador);
		}
		catch( Exception rtie ) { rtie.printStackTrace(); }
	}
}
