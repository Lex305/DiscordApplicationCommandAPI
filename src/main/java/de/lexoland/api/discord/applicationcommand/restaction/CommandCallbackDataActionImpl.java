package de.lexoland.api.discord.applicationcommand.restaction;

import de.lexoland.api.discord.applicationcommand.InteractionResponseType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.MessageActionImpl;
import okhttp3.RequestBody;

public class CommandCallbackDataActionImpl extends MessageActionImpl {

    private final InteractionResponseType interactionResponseType;

    public CommandCallbackDataActionImpl(JDA api, Route.CompiledRoute route, InteractionResponseType interactionResponseType, MessageChannel channel) {
        super(api, route, channel);
        this.interactionResponseType = interactionResponseType;
    }

    @Override
    protected DataObject getJSON() {
        DataObject dataObject = DataObject.empty();
        dataObject.put("type", interactionResponseType.getValue());
        dataObject.put("data", super.getJSON());
        return dataObject;
    }
}
