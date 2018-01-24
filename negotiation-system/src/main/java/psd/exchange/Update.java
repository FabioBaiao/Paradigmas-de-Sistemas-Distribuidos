package psd.exchange;

public class Update {
	public final String exchangeName;
	public final String company;
	public final Type type;
	public final double newValue;

	public Update(String exchangeName, String company, Type type, double newValue) {
	    this.exchangeName = exchangeName;
	    this.company = company;
		this.type = type;
		this.newValue = newValue;
	}

	public enum Type {
		OPENING_UNIT_PRICE,
		MIN_UNIT_PRICE,
		MAX_UNIT_PRICE,
		CLOSING_UNIT_PRICE
	}
}
