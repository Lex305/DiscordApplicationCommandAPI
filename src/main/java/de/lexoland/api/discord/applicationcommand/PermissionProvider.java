package de.lexoland.api.discord.applicationcommand;

import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

@FunctionalInterface
public interface PermissionProvider {

    List<ApplicationCommandPermission> get(Guild g);

}
