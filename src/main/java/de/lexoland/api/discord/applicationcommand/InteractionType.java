package de.lexoland.api.discord.applicationcommand;

public enum InteractionType {
	
	PING(1),
	APPLICATION_COMMAND(2);
	
	private final int value;
	
	InteractionType(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public static InteractionType getByValue(int value) {
		for(InteractionType type : InteractionType.values())
			if(type.getValue() == value)
				return type;
		return null;
	}

}
