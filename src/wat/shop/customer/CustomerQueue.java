package wat.shop.customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class CustomerQueue {
	private CustomerFederate federate;
	private HashMap <Integer, ArrayList<CustomerSet>> customerQueues = new HashMap<Integer, ArrayList<CustomerSet>>();
	
	public CustomerQueue(CustomerFederate fed) {
		this.federate = fed; //Pobieranie referencji do federata
	}
	
	public class CustomerSet {
		public int index;

		public CustomerSet(int id) {
			this.index = id;
		}
	}
	
	public int addCustomer(Customer customer, int qid) {
		if(!customerQueues.containsKey(qid)) {
			customerQueues.put(qid, new ArrayList<CustomerSet>());
		}
		ArrayList<CustomerSet> set = customerQueues.get(qid);
		if(!customer.isPrivileged()) { //Nie uprzywilejowany wrzucany na koniec kolejki
			set.add(new CustomerSet(customer.getId()));
			customerQueues.put(qid, set);
			return set.size()-1;
		}
		else { //Uprzywilejowany
			
			//Jesli kolejka jest pusta to ustawianie na poczatek kolejki lub jesli
			//kolejka jest rowna jeden i aktualny klient jest w trakcie rozliczania
			if(set.size() == 0 || (set.size() == 1 && federate.getCustomer(set.get(0).index).isChecking()) ) {
				set.add(new CustomerSet(customer.getId()));
				customerQueues.put(qid, set);
				System.out.println("Uprzywilejowany ustawiony na koniec kolejki");
				return set.size()-1;
			}
			else {	
				for(int i = 0; i < set.size(); i++) {
					Customer c = federate.getCustomer(set.get(i).index);
					
					//Jesli jest aktualnie obslugiwany to pomijany
					if(c.isChecking()) { continue; }
					
					//Jesli nie jest obslugiwany i nie jest uprzywilejowany
					if(!c.isPrivileged()) {
								
						//Dodawanie nowego miejsca do przesuniêcia
						set.add(set.get(set.size() - 1));
						
						//Przesuwanie listy w prawo
						for(int j = set.size() - 1; j > i; j--) {
							set.set(j, set.get(j - 1) );
							System.out.println("Przesuwanie id=" + set.get(j).index + " na pozycje="+j);
						}
						
						//Ustawianie na miejscu
						set.set(i, new CustomerSet(customer.getId()));
						
						customerQueues.put(qid, set);
						return i;	
					}
				}
			}
		}
		return -1;
	}
	
	public Set<Integer> getQueues() {
		return customerQueues.keySet();
	}
	
	public int get(int qid, int index) {
		ArrayList<CustomerSet> set = customerQueues.get(qid);
		if(index < set.size()) {
			return set.get(index).index;
		}
		else
			return -1;
	}
	
	public void removeFirst(int qid) {

            ArrayList<CustomerSet> set = customerQueues.get(qid);
		set.remove(0);
		customerQueues.put(qid, set);
	}
	
}
