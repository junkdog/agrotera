package net.onedaybeard.agrotera.matrix;

public enum ComponentReference
{
	REQUIRED("R"), ANY("A"), OPTIONAL("O"), EXCLUDED("X"), NOT_REFERENCED("");
	
	public final String symbol;
	
	private ComponentReference(String symbol)
	{
		this.symbol = symbol;
	}
}