import de.lexoland.api.discord.applicationcommand.ApplicationCommandAPI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Main {

    private static JDA jda;
    private static ApplicationCommandAPI api;

    public static void main(String[] args) throws LoginException {
        jda = JDABuilder.createDefault("ODQxNzY4MDEyMzc3MjI3Mjk0.YJrjjQ.a1rVYYVJ6xivbDKp1ZRJXzSaoXk").build();
        api = new ApplicationCommandAPI(jda);
        api.registerGuildCommand(841341296216113172L, new TestCommand()).queue();
    }

}
