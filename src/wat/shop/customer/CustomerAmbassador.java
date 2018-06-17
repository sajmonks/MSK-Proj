package wat.shop.customer;

import wat.shop.abstracts.Ambassador;
import wat.shop.abstracts.Federate;

public class CustomerAmbassador extends Ambassador{

	public CustomerAmbassador(Federate federate, double lookahead) {
		super(federate, lookahead);
	}

}
