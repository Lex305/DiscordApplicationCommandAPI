package de.lexoland.api.discord.applicationcommand;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ApplicationCommand {
	
	public static final int SUB_COMMAND = 1;
	public static final int SUB_COMMAND_GROUP = 2;
	public static final int STRING = 3;
	public static final int INTEGER = 4;
	public static final int BOOLEAN = 5;
	public static final int USER = 6;
	public static final int CHANNEL = 7;
	public static final int ROLE = 8;
	
	protected long id;
	protected long applicationId;
	protected ApplicationCommandNode node;
	
	public abstract ApplicationCommandNode build();
	
	protected ApplicationCommandNode option(String name, int type) {
		return new ApplicationCommandNode(name, type);
	}
	
	public long getId() {
		return id;
	}
	
	public long getApplicationId() {
		return applicationId;
	}
	
	public ApplicationCommandNode getNode() {
		return node;
	}
	
	public static ApplicationCommandChoice choice(String name, String value) {
		return new ApplicationCommandChoice(name, value);
	}
	
	public static ApplicationCommandChoice choice(String name, int value) {
		return new ApplicationCommandChoice(name, value);
	}
	
	public static class ApplicationCommandNode {
		
		protected String name, description = "No Description";
		protected int type;
		protected boolean required;
		protected ApplicationCommandChoice[] choices = new ApplicationCommandChoice[0];
		protected Consumer<Interaction> execute;
		protected List<ApplicationCommandNode> options = new ArrayList<>();
		
		public ApplicationCommandNode(String name, int type) {
			this.name = name.toLowerCase();
			this.type = type;
		}
		
		public ApplicationCommandNode(String name) {
			this.name = name.toLowerCase();
			this.type = 0;
		}
		
		public ApplicationCommandNode name(String name) {
			this.name = name;
			return this;
		}
		
		public ApplicationCommandNode description(String description) {
			this.description = description;
			return this;
		}
		
		public ApplicationCommandNode required(boolean required) {
			this.required = required;
			return this;
		}

		public ApplicationCommandNode choices(ApplicationCommandChoice... choices) {
			this.choices = choices;
			return this;
		}
		
		public ApplicationCommandNode executes(Consumer<Interaction> execute) {
			this.execute = execute;
			return this;
		}
		
		public ApplicationCommandNode then(ApplicationCommandNode option) {
			options.add(option);
			return this;
		}
		
		public ApplicationCommandChoice[] getChoices() {
			return choices;
		}
		
		public String getDescription() {
			return description;
		}
		
		public Consumer<Interaction> getExecute() {
			return execute;
		}
		
		public String getName() {
			return name;
		}
		
		public List<ApplicationCommandNode> getOptions() {
			return options;
		}
		
		public int getType() {
			return type;
		}
	}
	
	public static class ApplicationCommandChoice {
		
		private final String name;
		private final Object value;
		
		public ApplicationCommandChoice(String name, Object value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public Object getValue() {
			return value;
		}
	}

}
