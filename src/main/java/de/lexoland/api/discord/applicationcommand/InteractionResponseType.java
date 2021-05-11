package de.lexoland.api.discord.applicationcommand;

public enum InteractionResponseType {
	
	PONG(1),
	CHANNEL_MESSAGE_WITH_SOURCE(4),
	DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE(5);
	
	private int value;
	
	private InteractionResponseType(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public static InteractionResponseType getByValue(int value) {
		for(InteractionResponseType type : InteractionResponseType.values())
			if(type.getValue() == value)
				return type;
		return null;
	}

}
