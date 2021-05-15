package de.lexoland.api.discord.applicationcommand.restaction;

import de.lexoland.api.discord.applicationcommand.InteractionResponseType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.DataMessage;
import net.dv8tion.jda.internal.requests.Method;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class CommandCallbackDataActionImpl extends RestActionImpl<Void> implements CommandCallbackDataAction {

    private static final String CONTENT_TOO_BIG = String.format("A message may not exceed %d characters. Please limit your input!", Message.MAX_CONTENT_LENGTH);
    protected static EnumSet<Message.MentionType> defaultMentions = EnumSet.allOf(Message.MentionType.class);

    private final InteractionResponseType interactionResponseType;
    private int flags = -1;

    protected final Map<String, InputStream> files = new HashMap<>();
    protected final Set<InputStream> ownedResources = new HashSet<>();
    protected final StringBuilder content;
    protected final MessageChannel channel;
    protected Set<MessageEmbed> embeds = new HashSet<>();
    protected String nonce = null;
    protected boolean tts = false, override = false;
    protected EnumSet<Message.MentionType> allowedMentions;
    protected Set<String> mentionableUsers = new HashSet<>();
    protected Set<String> mentionableRoles = new HashSet<>();


    public CommandCallbackDataActionImpl(JDA api, Route.CompiledRoute route, InteractionResponseType interactionResponseType, MessageChannel channel) {
        super(api, route);
        this.content = new StringBuilder();
        this.channel = channel;
        this.allowedMentions = defaultMentions;
        this.interactionResponseType = interactionResponseType;
    }


    @Nonnull
    @Override
    public MessageChannel getChannel()
    {
        return channel;
    }

    @Override
    public boolean isEmpty()
    {
        return Helpers.isBlank(content)
                && (embeds.isEmpty() || !hasPermission(Permission.MESSAGE_EMBED_LINKS));
    }

    @Override
    public boolean isEdit()
    {
        return finalizeRoute().getMethod() == Method.PATCH;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    public CommandCallbackDataActionImpl apply(final Message message)
    {
        if (message == null || message.getType() != MessageType.DEFAULT)
            return this;
        final List<MessageEmbed> embeds = message.getEmbeds();
        if (embeds != null && !embeds.isEmpty())
            embeds(embeds.get(0));
        files.clear();

        String content = message.getContentRaw();

        // Insert allowed mentions
        if (message instanceof DataMessage)
        {
            DataMessage data = (DataMessage) message;
            String[] mentionedRoles = data.getMentionedRolesWhitelist();
            String[] mentionedUsers = data.getMentionedUsersWhitelist();
            EnumSet<Message.MentionType> allowedMentions = data.getAllowedMentions();
            if (allowedMentions != null)
                allowedMentions(allowedMentions);
            mentionRoles(mentionedRoles);
            mentionUsers(mentionedUsers);
        }
        else
        {
            // Only ping everyone if the message also did
            if (message.mentionsEveryone())
            {
                EnumSet<Message.MentionType> parse = EnumSet.noneOf(Message.MentionType.class);
                if (content.contains("@everyone"))
                    parse.add(Message.MentionType.EVERYONE);
                if (content.contains("@here"))
                    parse.add(Message.MentionType.HERE);
                allowedMentions = parse;
            }
            else
            {
                allowedMentions = EnumSet.noneOf(Message.MentionType.class);
            }

            this.mention(message.getMentionedUsers())
                    .mention(message.getMentionedRoles());
        }
        return content(content).tts(message.isTTS());
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public CommandCallbackDataActionImpl tts(final boolean isTTS)
    {
        this.tts = isTTS;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public CommandCallbackDataActionImpl reset()
    {
        embeds.clear();
        return content(null).nonce(null).tts(false).override(false);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public CommandCallbackDataActionImpl nonce(final String nonce)
    {
        this.nonce = nonce;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public CommandCallbackDataActionImpl content(final String content)
    {
        if (content == null || content.isEmpty())
            this.content.setLength(0);
        else if (content.length() <= Message.MAX_CONTENT_LENGTH)
            this.content.replace(0, this.content.length(), content);
        else
            throw new IllegalArgumentException(CONTENT_TOO_BIG);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public CommandCallbackDataActionImpl embeds(final MessageEmbed... embeds)
    {
        for(MessageEmbed embed : embeds) {
            if (embed != null)
            {
                Checks.check(embed.isSendable(),
                        "Provided Message contains an empty embed or an embed with a length greater than %d characters, which is the max for bot accounts!",
                        MessageEmbed.EMBED_MAX_LENGTH_BOT);
            }
            this.embeds.add(embed);
        }
        return this;
    }

    @Override
    public CommandCallbackDataAction clearEmbeds() {
        embeds.clear();
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public CommandCallbackDataActionImpl append(final CharSequence csq, final int start, final int end)
    {
        if (content.length() + end - start > Message.MAX_CONTENT_LENGTH)
            throw new IllegalArgumentException("A message may not exceed 2000 characters. Please limit your input!");
        content.append(csq, start, end);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public CommandCallbackDataActionImpl append(final char c)
    {
        if (content.length() == Message.MAX_CONTENT_LENGTH)
            throw new IllegalArgumentException("A message may not exceed 2000 characters. Please limit your input!");
        content.append(c);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public CommandCallbackDataActionImpl override(final boolean bool)
    {
        this.override = isEdit() && bool;
        return this;
    }

    @Nonnull
    @Override
    public CommandCallbackDataAction allowedMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        this.allowedMentions = allowedMentions == null
                ? EnumSet.allOf(Message.MentionType.class)
                : Helpers.copyEnumSet(Message.MentionType.class, allowedMentions);
        return this;
    }

    @Nonnull
    @Override
    public CommandCallbackDataAction mention(@Nonnull IMentionable... mentions)
    {
        Checks.noneNull(mentions, "Mentionables");
        for (IMentionable mentionable : mentions)
        {
            if (mentionable instanceof User || mentionable instanceof Member)
                mentionableUsers.add(mentionable.getId());
            else if (mentionable instanceof Role)
                mentionableRoles.add(mentionable.getId());
        }
        return this;
    }

    @Nonnull
    @Override
    public CommandCallbackDataAction mentionUsers(@Nonnull String... userIds)
    {
        Checks.noneNull(userIds, "User Id");
        Collections.addAll(mentionableUsers, userIds);
        return this;
    }

    @Nonnull
    @Override
    public CommandCallbackDataAction mentionRoles(@Nonnull String... roleIds)
    {
        Checks.noneNull(roleIds, "Role Id");
        Collections.addAll(mentionableRoles, roleIds);
        return this;
    }

    private String applyOptions(String name, AttachmentOption[] options)
    {
        for (AttachmentOption opt : options)
        {
            if (opt == AttachmentOption.SPOILER)
            {
                name = "SPOILER_" + name;
                break;
            }
        }
        return name;
    }

    private void clearResources()
    {
        for (InputStream ownedResource : ownedResources)
        {
            try
            {
                ownedResource.close();
            }
            catch (IOException ex)
            {
                if (!ex.getMessage().toLowerCase().contains("closed"))
                    LOG.error("Encountered IOException trying to close owned resource", ex);
            }
        }
        ownedResources.clear();
    }

    private long getMaxFileSize()
    {
        if (channel.getType().isGuild())
            return ((GuildChannel) channel).getGuild().getMaxFileSize();
        return getJDA().getSelfUser().getAllowedFileSize();
    }

    protected RequestBody asJSON()
    {
        return RequestBody.create(Requester.MEDIA_TYPE_JSON, getJSON().toJson());
    }

    protected DataObject getJSON()
    {
        DataObject obj = DataObject.empty();
        obj.put("type", interactionResponseType.getValue());
        {
            final DataObject data = DataObject.empty();
            if (override) {
                data.put("embeds", DataArray.fromCollection(embeds));
                if (content.length() == 0)
                    data.putNull("content");
                else
                    data.put("content", content.toString());
            } else {
                if (!embeds.isEmpty())
                    data.put("embeds", DataArray.fromCollection(embeds));
                if (content.length() > 0)
                    data.put("content", content.toString());
            }
            data.put("tts", tts);
            if (allowedMentions != null || !mentionableUsers.isEmpty() || !mentionableRoles.isEmpty())
                data.put("allowed_mentions", getAllowedMentionsObj());
            if(flags != -1)
                data.put("flags", flags);
            obj.put("data", data);
        }
        return obj;
    }

    protected DataObject getAllowedMentionsObj()
    {
        DataObject allowedMentionsObj = DataObject.empty();
        DataArray parsable = DataArray.empty();
        if (allowedMentions != null)
        {
            // Add parsing options
            allowedMentions.stream()
                    .map(Message.MentionType::getParseKey)
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(parsable::add);
        }
        if (!mentionableUsers.isEmpty())
        {
            // Whitelist certain users
            parsable.remove(Message.MentionType.USER.getParseKey());
            allowedMentionsObj.put("users", DataArray.fromCollection(mentionableUsers));
        }
        if (!mentionableRoles.isEmpty())
        {
            // Whitelist certain roles
            parsable.remove(Message.MentionType.ROLE.getParseKey());
            allowedMentionsObj.put("roles", DataArray.fromCollection(mentionableRoles));
        }
        return allowedMentionsObj.put("parse", parsable);
    }

    protected void checkEdit()
    {
        if (isEdit())
            throw new IllegalStateException("Cannot add files to an existing message! Edit-Message does not support this operation!");
    }

    protected void checkPermission(Permission perm)
    {
        if (!channel.getType().isGuild())
            return;
        GuildChannel gc = (GuildChannel) channel;
        if (!gc.getGuild().getSelfMember().hasAccess(gc))
            throw new MissingAccessException(gc, Permission.VIEW_CHANNEL);
        if (!hasPermission(perm))
            throw new InsufficientPermissionException(gc, perm);
    }

    protected boolean hasPermission(Permission perm)
    {
        if (channel.getType() != ChannelType.TEXT)
            return true;
        TextChannel text = (TextChannel) channel;
        Member self = text.getGuild().getSelfMember();
        return self.hasPermission(text, perm);
    }

    @Override
    protected RequestBody finalizeData()
    {
        if (!isEmpty())
            return asJSON();
        throw new IllegalStateException("Cannot build a message without content!");
    }

    @Override
    protected void handleSuccess(Response response, Request<Void> request) {
        request.onSuccess(null);
    }

    @Override
    @SuppressWarnings("deprecation") /* If this was in JDK9 we would be using java.lang.ref.Cleaner instead! */
    protected void finalize()
    {
        if (ownedResources.isEmpty())
            return;
        LOG.warn("Found unclosed resources in CommandCallbackDataAction instance, closing on finalization step!");
        clearResources();
    }

    @Override
    public CommandCallbackDataAction flags(int flags) {
        this.flags = flags;
        return this;
    }

    @Override
    public int getFlags() {
        return flags;
    }
}
