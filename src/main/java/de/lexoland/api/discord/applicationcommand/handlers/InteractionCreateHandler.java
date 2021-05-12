package de.lexoland.api.discord.applicationcommand.handlers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import de.lexoland.api.discord.applicationcommand.ApplicationCommand;
import de.lexoland.api.discord.applicationcommand.ApplicationCommand.ApplicationCommandNode;
import de.lexoland.api.discord.applicationcommand.ApplicationCommandAPI;
import de.lexoland.api.discord.applicationcommand.Interaction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.handle.SocketHandler;

public class InteractionCreateHandler extends SocketHandler {

	private final ApplicationCommandAPI commandAPI;
	
	public InteractionCreateHandler(JDAImpl api, ApplicationCommandAPI commandAPI) {
		super(api);
		this.commandAPI = commandAPI;
	}

	@Override
	protected Long handleInternally(DataObject content) {
		DataObject data = content.getObject("data");
		long id = data.getLong("id");
		ApplicationCommand command = null;
		for(ApplicationCommand c : commandAPI.getAllCommands())
			if(c.getId() == id)
				command = c;
		if(command == null)
			return null;
		ApplicationCommandNode mainNode = command.getNode();
		
		execute = null;
		HashMap<String, Object> optionValues = new HashMap<>();
		execute(content, data, Arrays.asList(mainNode), optionValues);
		if(execute != null) {
			Interaction interaction = new Interaction(api, commandAPI, content, optionValues);
			execute.accept(interaction);
		}
			
		return null;
	}
	
	private Consumer<Interaction> execute;
	
	private void execute(DataObject content, DataObject data, List<ApplicationCommandNode> nodes, HashMap<String, Object> optionValues) {
		ApplicationCommandNode node = null;
		for(ApplicationCommandNode n : nodes)
			if(n.getName().equals(data.getString("name")))
				node = n;
		if(node == null)
			return;
		if(data.hasKey("value")) {
			optionValues.put(node.getName(), data.get("value"));
		}
		if(data.hasKey("options")) {
			DataArray dataOptions = data.getArray("options");
			for(int i = 0; i < dataOptions.length(); i++) {
				execute(content, dataOptions.getObject(i), node.getOptions(), optionValues);
			}
		}
		if(node.getExecute() != null)
			execute = node.getExecute();
			
	}

}
