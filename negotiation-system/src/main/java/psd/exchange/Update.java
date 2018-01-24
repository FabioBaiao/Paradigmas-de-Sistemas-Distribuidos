package psd.exchange;

public class Update {
	public final Type type;
	public final double newValue;

	public Update(Type type, double newValue) {
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
