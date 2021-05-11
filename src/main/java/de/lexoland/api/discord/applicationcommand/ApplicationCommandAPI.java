package de.lexoland.api.discord.applicationcommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import de.lexoland.api.discord.applicationcommand.ApplicationCommand.ApplicationCommandChoice;
import de.lexoland.api.discord.applicationcommand.ApplicationCommand.ApplicationCommandNode;
import de.lexoland.api.discord.applicationcommand.handlers.InteractionCreateHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.handle.SocketHandler;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.Route.CompiledRoute;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ApplicationCommandAPI {
	
	private JDAImpl jda;
	private HashMap<Long, ApplicationCommand> commands = new HashMap<>();
	
	public ApplicationCommandAPI(JDA jda) {
		this.jda = (JDAImpl) jda;
		setupHandlers();
	}
	
	private void setupHandlers() {
		Map<String, SocketHandler> handlers = jda.getClient().getHandlers();
		handlers.put("INTERACTION_CREATE", new InteractionCreateHandler(jda, this));
	}
	
	public void registerGuildCommand(Guild guild, ApplicationCommand command) {
		registerGuildCommand(guild.getIdLong(), command);
	}
	
	public void registerGuildCommand(long guildId, ApplicationCommand command) {
		ApplicationCommandNode node = command.build();
		requestPost(
			Route.post("applications/{}/guilds/{}/commands").compile(String.valueOf(jda.getSelfUser().getIdLong()), String.valueOf(guildId)),
			build(node).toJson(),
			response -> {
				DataObject obj = response.optObject().get();
				command.id = obj.getLong("id");
				command.applicationId = obj.getLong("application_id");
				command.node = node;
				commands.put(command.id, command);
			}
		);
	}
	
	public void registerGlobalCommand(ApplicationCommand command) {
		ApplicationCommandNode node = command.build();
		requestPost(
			Route.post("applications/{}/commands").compile(String.valueOf(jda.getSelfUser().getIdLong())),
			build(node).toJson(),
			response -> {
				DataObject obj = response.optObject().get();
				command.id = obj.getLong("id");
				command.applicationId = obj.getLong("application_id");
				command.node = node;
				commands.put(command.id, command);
			}
		);
	}
	
	public RestAction<ApplicationCommand> getGlobalCommand(String name) {
		RestActionImpl<ApplicationCommand> restAction = new RestActionImpl<>(
			jda,
			Route.get("applications/{}/commands").compile(String.valueOf(jda.getSelfUser().getIdLong())),
			(response, request) -> {
				DataArray array = response.getArray();
				for(int i = 0; i < array.length(); i++) {
					DataObject command = array.getObject(i);
					if(command.getString("name").equalsIgnoreCase(name))
						return commandFromDataObject(command);
				}
				return null;
			}
		);
		return restAction;
	}
	
	public RestAction<ApplicationCommand> getGlobalCommand(long id) {
		RestActionImpl<ApplicationCommand> restAction = new RestActionImpl<>(
			jda,
			Route.get("applications/{}/commands/{}").compile(String.valueOf(jda.getSelfUser().getIdLong()), String.valueOf(id)),
			(response, request) -> commandFromDataObject(response.getObject())
		);
		return restAction;
	}
	
	public RestAction<Void> deleteGlobalCommand(long id) {
		RestActionImpl<Void> restAction = new RestActionImpl<>(
			jda,
			Route.delete("applications/{}/commands/{}").compile(String.valueOf(jda.getSelfUser().getIdLong()), String.valueOf(id))
		);
		return restAction;
	}
	
	public RestAction<List<ApplicationCommand>> getGlobalCommands() {
		RestActionImpl<List<ApplicationCommand>> restAction = new RestActionImpl<>(
			jda,
			Route.get("applications/{}/commands").compile(String.valueOf(jda.getSelfUser().getIdLong())),
			(response, request) -> {
				List<ApplicationCommand> commands = new ArrayList<>();
				DataArray array = response.getArray();
				for(int i = 0; i < array.length(); i++) {
					DataObject command = array.getObject(i);
					commands.add(commandFromDataObject(command));
				}
				return commands;
			}
		);
		return restAction;
	}
	
	public RestAction<ApplicationCommand> getGuildCommand(long guildId, String name) {
		RestActionImpl<ApplicationCommand> restAction = new RestActionImpl<>(
			jda,
			Route.get("applications/{}/guilds/{}/commands").compile(String.valueOf(jda.getSelfUser().getIdLong()), String.valueOf(guildId)),
			(response, request) -> {
				DataArray array = response.getArray();
				for(int i = 0; i < array.length(); i++) {
					DataObject command = array.getObject(i);
					if(command.getString("name").equalsIgnoreCase(name))
						return commandFromDataObject(command);
				}
				return null;
			}
		);
		return restAction;
	}
	
	public RestAction<ApplicationCommand> getGuildCommand(long guildId, long id) {
		RestActionImpl<ApplicationCommand> restAction = new RestActionImpl<>(
			jda,
			Route.get("applications/{}/guilds/{}/commands/{}").compile(String.valueOf(jda.getSelfUser().getIdLong()), String.valueOf(guildId), String.valueOf(id)),
			(response, request) -> commandFromDataObject(response.getObject())
		);
		return restAction;
	}
	
	public RestAction<Void> deleteGuildCommand(long guildId, long id) {
		RestActionImpl<Void> restAction = new RestActionImpl<>(
			jda,
			Route.delete("applications/{}/guilds/{}/commands/{}").compile(String.valueOf(jda.getSelfUser().getIdLong()), String.valueOf(guildId), String.valueOf(id))
		);
		return restAction;
	}
	
	public RestAction<List<ApplicationCommand>> getGuildCommands(long guildId) {
		RestActionImpl<List<ApplicationCommand>> restAction = new RestActionImpl<>(
			jda,
			Route.get("applications/{}/guilds/{}/commands").compile(String.valueOf(jda.getSelfUser().getIdLong()), String.valueOf(guildId)),
			(response, request) -> {
				List<ApplicationCommand> commands = new ArrayList<>();
				DataArray array = response.getArray();
				for(int i = 0; i < array.length(); i++) {
					DataObject command = array.getObject(i);
					commands.add(commandFromDataObject(command));
				}
				return commands;
			}
		);
		return restAction;
	}
	
	private ApplicationCommand commandFromDataObject(DataObject commandData) {
		ApplicationCommand command = new ApplicationCommand() {
			@Override
			public ApplicationCommandNode build() { return node; }
		};
		command.id = commandData.getLong("id");
		command.applicationId = commandData.getLong("application_id");
		command.node = commandNodeFromDataObject(commandData);
		return command;
	}
	
	private ApplicationCommandNode commandNodeFromDataObject(DataObject nodeData) {
		ApplicationCommandNode node = new ApplicationCommandNode(
			nodeData.getString("name"),
			nodeData.getInt("type", 0)
		);
		node.description = nodeData.getString("description");
		node.required = nodeData.getBoolean("required", false);
		if(nodeData.hasKey("choices")) {
			DataArray choicesData = nodeData.getArray("choices");
			ApplicationCommandChoice[] choices = new ApplicationCommandChoice[choicesData.length()];
			for(int i = 0; i < choicesData.length(); i++) {
				DataObject choiceData = choicesData.getObject(i);
				choices[i] = new ApplicationCommandChoice(choiceData.getString("name"), choiceData.get("value"));
			}
			node.choices = choices;
		}
		if(nodeData.hasKey("options")) {
			DataArray optionsData = nodeData.getArray("options");
			for(int i = 0; i < optionsData.length(); i++) {
				node.options.add(commandNodeFromDataObject(optionsData.getObject(i)));
			}
		}
		return node;
	}
	
	private DataObject build(ApplicationCommandNode node) {
		DataObject obj = DataObject.empty()
			.put("name", node.name)
			.put("description", node.description);
		if(node.options.size() > 0) {
			
			DataArray optionArray = DataArray.empty();
			for(ApplicationCommandNode option : node.options)
				optionArray.add(buildOption(option));
			obj.put("options", optionArray);
		}
			
		return obj;
	}
	
	private DataObject buildOption(ApplicationCommandNode node) {
		DataObject obj = DataObject.empty();
		obj.put("name", node.name);
		obj.put("description", node.description);
		obj.put("type", node.type);
		obj.put("required", node.required);
		if(node.choices.length > 0) {
			DataArray choicesArray = DataArray.empty();
			for(ApplicationCommandChoice choice : node.choices) {
				DataObject choiceObj = DataObject.empty();
				choiceObj.put("name", choice.getName());
				choiceObj.put("value", choice.getValue());
				choicesArray.add(choiceObj);
			}
			obj.put("choices", choicesArray);
		}
		if(node.options.size() > 0) {
			DataArray optionArray = DataArray.empty();
			for(ApplicationCommandNode option : node.options)
				optionArray.add(buildOption(option));
			obj.put("options", optionArray);
		}
		return obj;
	}
	
	protected void requestPost(CompiledRoute route, byte[] content, Consumer<Response> onResponse) {
		RestActionImpl<?> restAction = new RestActionImpl<>(jda, route, RequestBody.create(MediaType.get("application/json"), content), (response, request) -> {
			onResponse.accept(response);
			return null;
		});
		restAction.queue();
	}
	
	protected RestActionImpl<Response> requestGet(CompiledRoute route) {
		RestActionImpl<Response> restAction = new RestActionImpl<>(jda, route, (response, request) -> {
			return response;
		});
		return restAction;
	}
	
	public JDA getJDA() {
		return jda;
	}
	
	public HashMap<Long, ApplicationCommand> getCommands() {
		return commands;
	}

}
