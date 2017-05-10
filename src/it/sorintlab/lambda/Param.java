package it.sorintlab.lambda;

public class Param {
	private final String type;
	private final String source;

	public Param(String type, String source) {
		this.type = type;
		this.source = source;
	}

	public String getType() {
		return type;
	}

	public String getSource() {
		return source;
	}
}