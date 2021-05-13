package de.lexoland.api.discord.applicationcommand;

import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

@FunctionalInterface
public interface ChoiceProvider {

    void get(List<ApplicationCommand.ApplicationCommandChoice> choices, Guild g);

}
