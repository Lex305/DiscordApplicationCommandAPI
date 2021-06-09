package de.lexoland.api.discord.applicationcommand;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CommandListener extends ListenerAdapter {

    private final ApplicationCommandAPI commandAPI;

    protected CommandListener(ApplicationCommandAPI commandAPI) {
        this.commandAPI = commandAPI;
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        ApplicationCommand command;
        if(event.isFromGuild())
            command = commandAPI.getGuildCommand(event.getGuild().getIdLong(), event.getCommandIdLong());
        else
            command = commandAPI.getGlobalCommand(event.getCommandIdLong());
        String[] path = event.getCommandPath().split("/");
        ApplicationCommand.ApplicationCommandNode node = command.getNode();
        if(path.length == 1) {
            node.execute.accept(event);
        } else if(path.length >= 2) {
            for(ApplicationCommand.ApplicationCommandNode node1 : node.getOptions()) {
                if(node1.getName().equals(path[1])) {
                    if(path.length == 2 && node1 instanceof ApplicationCommand.ApplicationSubCommandNode)
                        node1.execute.accept(event);
                    else {
                        for(ApplicationCommand.ApplicationCommandNode node2 : node1.getOptions()) {
                            if(node2.getName().equals(path[2]))
                                node2.execute.accept(event);
                        }
                    }
                }
            }
        }

    }
}
