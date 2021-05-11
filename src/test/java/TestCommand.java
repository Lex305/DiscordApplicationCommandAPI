import de.lexoland.api.discord.applicationcommand.ApplicationCommand;
import de.lexoland.api.discord.applicationcommand.ArgumentType;
import de.lexoland.api.discord.applicationcommand.InteractionResponseType;

import java.util.Random;

public class TestCommand extends ApplicationCommand {

    @Override
    public void build(ApplicationRootCommandNode root) {
        root.then(
                subCommand("human")
                .argument("user", ArgumentType.USER)
                .executes(interaction -> {
                    interaction.sendInteractionResponse(
                            InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE,
                            interaction.getArgumentUserRetrieved("user").complete().getAsMention() + " wurde " + (new Random().nextBoolean() ? "positiv" : "negativ") + " getestet."
                    ).queue();
                })
        )
        .then(
                subCommand("role")
                .argument("role", ArgumentType.ROLE)
                .executes(interaction -> {
                    interaction.sendInteractionResponse(
                            InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE,
                            interaction.getArgumentRole("role").getName() + " wurde " + (new Random().nextBoolean() ? "positiv" : "negativ") + " getestet."
                    ).queue();
                })
        );
    }

    @Override
    public String getName() {
        return "test";
    }
}
