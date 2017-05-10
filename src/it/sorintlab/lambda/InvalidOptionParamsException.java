package it.sorintlab.lambda;

import java.util.List;

@SuppressWarnings("serial")
public class InvalidOptionParamsException extends Exception {

	public InvalidOptionParamsException(String optionName, String... optionParams) {
		super(optionName + "[\n\t" + String.join("\n\t", optionParams) + "\n]");
	}

	public InvalidOptionParamsException(String optionName, List<String> optionParams) {
		this(optionName, optionParams.toArray(new String[optionParams.size()]));
	}

}