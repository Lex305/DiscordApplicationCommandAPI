package de.lexoland.api.discord.applicationcommand;

import java.util.HashMap;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class Interaction {
	
	private final JDAImpl jda;
	private final ApplicationCommandAPI api;
	private long id;
	private InteractionType type;
	private User user;
	private Member member;
	private Guild guild;
	private MessageChannel messageChannel;
	private String token;
	private HashMap<String, Object> optionValues;
	
	public Interaction(JDAImpl jda, ApplicationCommandAPI api, DataObject content, HashMap<String, Object> optionValues) {
		this.jda = jda;
		this.api = api;
		this.optionValues = optionValues;
		EntityBuilder entityBuilder = jda.getEntityBuilder();
		id = content.getLong("id");
		type = InteractionType.getByValue(content.getInt("type"));
		if(content.hasKey("member")) {
			guild = jda.getGuildById(content.getLong("guild_id"));
			member = entityBuilder.createMember((GuildImpl) guild, content.getObject("member"));
			messageChannel = guild.getTextChannelById(content.getLong("channel_id"));
		} else {
			user = entityBuilder.createUser(content.getObject("user"));
			messageChannel = user.openPrivateChannel().complete();
		}
		token = content.getString("token");
	}
	
	public RestAction<Void> sendInteractionResponse(InteractionResponseType type) {
		return sendInteractionResponse(type, null);
	}
	
	public RestAction<Void> sendInteractionResponse(InteractionResponseType type, String content, MessageEmbed... embeds) {
		DataObject obj = DataObject.empty()
			.put("type", type.getValue());
		if(content != null) {
			DataObject data = DataObject.empty()
				.put("content", content);
			if(embeds.length > 0) {
				DataArray embedData = DataArray.empty();
				for(MessageEmbed embed : embeds)
					embedData.add(embed.toData());
				data.put("embeds", embedData);
			}
			obj.put("data", data);
			
		}
		return new RestActionImpl<>(
			jda,
			Route.post("interactions/{}/{}/callback").compile(String.valueOf(id), token),
			RequestBody.create(MediaType.get("application/json"), obj.toJson()),
			(response, request) -> null
		);
	}
	
	public RestAction<Void> sendFollowUpMessage(String content) {
		DataObject obj = DataObject.empty();
		obj.put("content", content);
		return new RestActionImpl<>(
			jda,
			Route.post("webhooks/{}/{}").compile(String.valueOf(jda.getSelfUser().getIdLong()), token),
			RequestBody.create(MediaType.get("application/json"), obj.toJson()),
			(response, request) -> null
		);
	}
	
	private long getOptionLong(String name) {
		return Long.parseLong((String) optionValues.get(name));
	}
	
	private long getOptionLong(String name, long def) {
		return Long.parseLong((String) optionValues.getOrDefault(name, String.valueOf(def)));
	}
	
	public int getOptionInt(String name) {
		return (int) optionValues.get(name);
	}
	
	public String getOptionString(String name) {
		return (String) optionValues.get(name);
	}
	
	public boolean getOptionBoolean(String name) {
		return (boolean) optionValues.get(name);
	}
	
	public User getOptionUser(String name) {
		return jda.getUserById((long) optionValues.get(name));
	}
	
	public long getOptionUserLong(String name) {
		return (long) getOptionLong(name);
	}
	
	public GuildChannel getOptionChannel(String name) {
		return jda.getGuildChannelById((long) optionValues.get(name));
	}
	
	public long getOptionChannelLong(String name) {
		return (long) getOptionLong(name);
	}
	
	public Role getOptionRole(String name) {
		return jda.getRoleById((long) optionValues.get(name));
	}
	
	public long getOptionRoleLong(String name) {
		return (long) getOptionLong(name);
	}
	
	public int getOptionInt(String name, int def) {
		return (int) optionValues.getOrDefault(name, def);
	}
	
	public String getOptionString(String name, String def) {
		return (String) optionValues.getOrDefault(name, def);
	}
	
	public boolean getOptionBoolean(String name, boolean def) {
		return (boolean) optionValues.getOrDefault(name, def);
	}
	
	public User getOptionUser(String name, User def) {
		return jda.getUserById((long) optionValues.getOrDefault(name, def));
	}
	
	public long getOptionUserLong(String name, long def) {
		return getOptionLong(name, def);
	}
	
	public GuildChannel getOptionChannel(String name, GuildChannel def) {
		return jda.getGuildChannelById((long) optionValues.getOrDefault(name, def));
	}
	
	public long getOptionChannelLong(String name, long def) {
		return getOptionLong(name, def);
	}
	
	public Role getOptionRole(String name, Role def) {
		return jda.getRoleById((long) optionValues.getOrDefault(name, def));
	}
	
	public long getOptionRoleLong(String name, long def) {
		return getOptionLong(name, def);
	}
	
	public Object getOption(String name, Object def) {
		return optionValues.getOrDefault(name, def);
	}
	
	public boolean hasOption(String name) {
		return optionValues.containsKey(name);
	}
	
//	public GuildChannel getOptionUser(String name) {
//		return createGuildChannel(jda.getEntityBuilder(), (GuildImpl) guild, (DataObject) optionValues.get(name));
//	}
//	
//    private void createGuildChannel(EntityBuilder eb, GuildImpl guildObj, DataObject channelData)
//    {
//        final ChannelType channelType = ChannelType.fromId(channelData.getInt("type"));
//        switch (channelType)
//        {
//        case TEXT:
//            eb.createTextChannel(guildObj, channelData, guildObj.getIdLong());
//            break;
//        case VOICE:
//        	eb.createVoiceChannel(guildObj, channelData, guildObj.getIdLong());
//            break;
//        case CATEGORY:
//        	eb.createCategory(guildObj, channelData, guildObj.getIdLong());
//            break;
//        case STORE:
//        	eb.createStoreChannel(guildObj, channelData, guildObj.getIdLong());
//            break;
//        default:
//            EntityBuilder.LOG.debug("Cannot create channel for type " + channelData.getInt("type"));
//        }
//    }
	
	public long getId() {
		return id;
	}
	
	public InteractionType getType() {
		return type;
	}
	
	public Guild getGuild() {
		return guild;
	}
	
	public Member getMember() {
		return member;
	}
	
	public MessageChannel getMessageChannel() {
		return messageChannel;
	}
	
	public String getToken() {
		return token;
	}
	
	public User getUser() {
		return user != null ? user : member.getUser();
	}
	
	public JDA getJDA() {
		return jda;
	}
	
	public ApplicationCommandAPI getAPI() {
		return api;
	}

}
