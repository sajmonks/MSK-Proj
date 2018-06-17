package wat.shop.shop;

import wat.shop.abstracts.Ambassador;
import wat.shop.abstracts.Federate;

public class ShopAmbassador extends Ambassador {

	public ShopAmbassador(Federate federate, double lookahead) {
		super(federate, lookahead);
	}

}
