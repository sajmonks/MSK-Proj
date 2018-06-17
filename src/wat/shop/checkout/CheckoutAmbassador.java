package wat.shop.checkout;

import wat.shop.abstracts.Ambassador;
import wat.shop.abstracts.Federate;

public class CheckoutAmbassador extends Ambassador{

	public CheckoutAmbassador(Federate federate, double lookahead) {
		super(federate, lookahead);
	}

}
