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
			if(set.size() == 0) {
				set.add(new CustomerSet(customer.getId()));
				customerQueues.put(qid, set);
				return 0;
			}
			else {
				for(int i = 0; i < set.size(); i++) {
					Customer c = federate.getCustomer(set.get(i).index);
					if(c.isChecking()) { continue; }
					if(!c.isPrivileged()) {
						set.add(i, new CustomerSet(set.get(i).index));
						customerQueues.put(qid, set);
						return set.get(i).index;
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
