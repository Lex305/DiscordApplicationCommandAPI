package de.lexoland.api.discord.applicationcommand.restaction;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.requests.restaction.MessageActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public interface CommandCallbackDataAction extends RestAction<Void>, Appendable {

    /**
     * The target {@link MessageChannel} for this message
     *
     * @return The target channel
     */
    @Nonnull
    MessageChannel getChannel();

    /**
     * Whether this CommandCallbackDataAction has no values set.
     * <br>Trying to execute with {@code isEmpty() == true} will result in an {@link java.lang.IllegalStateException IllegalStateException}!
     *
     * <p><b>This does not check for files!</b>
     *
     * @return True, if no settings have been applied
     */
    boolean isEmpty();

    /**
     * Whether this CommandCallbackDataAction will be used to update an existing message.
     *
     * @return True, if this CommandCallbackDataAction targets an existing message
     */
    boolean isEdit();

    /**
     * Applies the sendable information of the provided {@link net.dv8tion.jda.api.entities.Message Message}
     * to this CommandCallbackDataAction settings.
     * <br>This will override all existing settings <b>if</b> new settings are available.
     *
     * <p><b>This does not copy files!</b>
     *
     * @param  message
     *         The nullable Message to apply settings from
     *
     * @throws java.lang.IllegalArgumentException
     *         If the message contains a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     *         that exceeds the sendable character limit,
     *         see {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() MessageEmbed.isSendable()}
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    CommandCallbackDataAction apply(@Nullable final Message message);


    /**
     * Enable/Disable Text-To-Speech for the resulting message.
     * <br>This is only relevant to MessageActions that are not {@code isEdit() == true}!
     *
     * @param  isTTS
     *         True, if this should cause a Text-To-Speech effect when sent to the channel
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    CommandCallbackDataAction tts(final boolean isTTS);

    /**
     * Resets this CommandCallbackDataAction to empty state
     * <br>{@link #isEmpty()} will result in {@code true} after this has been performed!
     *
     * <p>Convenience over using
     * {@code content(null).nonce(null).embed(null).tts(false).override(false).clearFiles()}
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    CommandCallbackDataAction reset();

    /**
     * Sets the validation nonce for the outgoing Message
     *
     * <p>For more information see {@link net.dv8tion.jda.api.MessageBuilder#setNonce(String) MessageBuilder.setNonce(String)}
     * and {@link net.dv8tion.jda.api.entities.Message#getNonce() Message.getNonce()}
     *
     * @param  nonce
     *         The nonce that shall be used
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     *
     * @see    net.dv8tion.jda.api.entities.Message#getNonce()
     * @see    net.dv8tion.jda.api.MessageBuilder#setNonce(String)
     * @see    <a href="https://en.wikipedia.org/wiki/Cryptographic_nonce" target="_blank">Cryptographic Nonce - Wikipedia</a>
     */
    @Nonnull
    @CheckReturnValue
    CommandCallbackDataAction nonce(@Nullable final String nonce);

    /**
     * Overrides existing content with the provided input
     * <br>The content of a Message may not exceed {@value Message#MAX_CONTENT_LENGTH}!
     *
     * @param  content
     *         Sets the specified content and overrides previous content
     *         or {@code null} to reset content
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided content exceeds the {@value Message#MAX_CONTENT_LENGTH} character limit
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    CommandCallbackDataAction content(@Nullable final String content);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     * that should be used for this Message.
     * Refer to {@link net.dv8tion.jda.api.EmbedBuilder EmbedBuilder} for more information.
     *
     * @param  embeds
     *         The {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} that should
     *         be attached to this message, {@code null} to use no embed.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided MessageEmbed is not sendable according to
     *         {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() MessageEmbed.isSendable()}!
     *         If the provided MessageEmbed is an unknown implementation this operation will fail as we are unable to deserialize it.
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    CommandCallbackDataAction embeds(final MessageEmbed... embeds);

    CommandCallbackDataAction clearEmbeds();

    /**
     * {@inheritDoc}
     * @throws java.lang.IllegalArgumentException
     *         If the appended CharSequence is too big and will cause the content to
     *         exceed the {@value net.dv8tion.jda.api.entities.Message#MAX_CONTENT_LENGTH} character limit
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     */
    @Nonnull
    @Override
    @CheckReturnValue
    default CommandCallbackDataAction append(@Nonnull final CharSequence csq)
    {
        return append(csq, 0, csq.length());
    }

    /**
     * {@inheritDoc}
     * @throws java.lang.IllegalArgumentException
     *         If the appended CharSequence is too big and will cause the content to
     *         exceed the {@value net.dv8tion.jda.api.entities.Message#MAX_CONTENT_LENGTH} character limit
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     */
    @Nonnull
    @Override
    @CheckReturnValue
    CommandCallbackDataAction append(@Nullable final CharSequence csq, final int start, final int end);

    /**
     * {@inheritDoc}
     * @throws java.lang.IllegalArgumentException
     *         If the appended CharSequence is too big and will cause the content to
     *         exceed the {@value net.dv8tion.jda.api.entities.Message#MAX_CONTENT_LENGTH} character limit
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     */
    @Nonnull
    @Override
    @CheckReturnValue
    CommandCallbackDataAction append(final char c);

    /**
     * Applies the result of {@link String#format(String, Object...) String.format(String, Object...)}
     * as content.
     *
     * <p>For more information of formatting review the {@link java.util.Formatter Formatter} documentation!
     *
     * @param  format
     *         The format String
     * @param  args
     *         The arguments that should be used for conversion
     *
     * @throws java.lang.IllegalArgumentException
     *         If the appended formatting is too big and will cause the content to
     *         exceed the {@value net.dv8tion.jda.api.entities.Message#MAX_CONTENT_LENGTH} character limit
     * @throws java.util.IllegalFormatException
     *         If a format string contains an illegal syntax,
     *         a format specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other illegal conditions.
     *         For specification of all possible formatting errors,
     *         see the <a href="../util/Formatter.html#detail">Details</a>
     *         section of the formatter class specification.
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default CommandCallbackDataAction appendFormat(@Nonnull final String format, final Object... args)
    {
        return append(String.format(format, args));
    }

    /**
     * Whether all fields should be considered when editing a message
     *
     * @param  bool
     *         True, to override all fields even if they are not set
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    CommandCallbackDataAction override(final boolean bool);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Message.MentionType MentionTypes} that should be parsed.
     * <br>If a message is sent with an empty Set of MentionTypes, then it will not ping any User, Role or {@code @everyone}/{@code @here},
     * while still showing up as mention tag.
     * <p>
     * If {@code null} is provided to this method, then all Types will be pingable
     * (unless whitelisting via one of the {@code mention*} methods is used).
     * <p>
     * Note: A default for this can be set using {@link MessageAction#setDefaultMentions(Collection) MessageAction.setDefaultMentions(Collection)}.
     *
     * @param  allowedMentions
     *         MentionTypes that are allowed to being parsed and pinged. {@code null} to disable and allow all mentions.
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    CommandCallbackDataAction allowedMentions(@Nullable Collection<Message.MentionType> allowedMentions);

    /**
     * Used to provide a whitelist for {@link net.dv8tion.jda.api.entities.User Users}, {@link net.dv8tion.jda.api.entities.Member Members}
     * and {@link net.dv8tion.jda.api.entities.Role Roles} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     * <br>On other types of {@link net.dv8tion.jda.api.entities.IMentionable IMentionable}, this does nothing.
     *
     * <p><b>Note:</b> When a User/Member is whitelisted this way, then parsing of User mentions is automatically disabled (same applies to Roles).
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link MessageAction#setDefaultMentions(Collection)} or {@link #allowedMentions(Collection)}.
     *
     * @param  mentions
     *         Users, Members and Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     *
     * @see    #allowedMentions(Collection)
     * @see    MessageAction#setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    CommandCallbackDataAction mention(@Nonnull IMentionable... mentions);

    /**
     * Used to provide a whitelist for {@link net.dv8tion.jda.api.entities.User Users}, {@link net.dv8tion.jda.api.entities.Member Members}
     * and {@link net.dv8tion.jda.api.entities.Role Roles} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     * <br>On other types of {@link net.dv8tion.jda.api.entities.IMentionable IMentionable}, this does nothing.
     *
     * <p><b>Note:</b> When a User/Member is whitelisted this way, then parsing of User mentions is automatically disabled (same applies to Roles).
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link MessageAction#setDefaultMentions(Collection)} or {@link #allowedMentions(Collection)}.
     *
     * @param  mentions
     *         Users, Members and Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     *
     * @see    #allowedMentions(Collection)
     * @see    MessageAction#setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default CommandCallbackDataAction mention(@Nonnull Collection<? extends IMentionable> mentions)
    {
        Checks.noneNull(mentions, "Mention");
        return mention(mentions.toArray(new IMentionable[0]));
    }

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.User Users} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a User is whitelisted this way, then parsing of User mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link MessageAction#setDefaultMentions(Collection)} or {@link #allowedMentions(Collection)}.
     *
     * @param  userIds
     *         Ids of Users that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     *
     * @see    #allowedMentions(Collection)
     * @see    MessageAction#setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    CommandCallbackDataAction mentionUsers(@Nonnull String... userIds);

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.User Users} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a User is whitelisted this way, then parsing of User mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link MessageAction#setDefaultMentions(Collection)} or {@link #allowedMentions(Collection)}.
     *
     * @param  userIds
     *         Ids of Users that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     *
     * @see    #allowedMentions(Collection)
     * @see    MessageAction#setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default CommandCallbackDataAction mentionUsers(@Nonnull long... userIds)
    {
        Checks.notNull(userIds, "UserId array");
        String[] stringIds = new String[userIds.length];
        for (int i = 0; i < userIds.length; i++)
        {
            stringIds[i] = Long.toUnsignedString(userIds[i]);
        }
        return mentionUsers(stringIds);
    }

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.Role Roles} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a Role is whitelisted this way, then parsing of Role mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link MessageAction#setDefaultMentions(Collection)} or {@link #allowedMentions(Collection)}.
     *
     * @param  roleIds
     *         Ids of Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     *
     * @see    #allowedMentions(Collection)
     * @see    MessageAction#setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    CommandCallbackDataAction mentionRoles(@Nonnull String... roleIds);

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.Role Roles} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a Role is whitelisted this way, then parsing of Role mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link MessageAction#setDefaultMentions(Collection)} or {@link #allowedMentions(Collection)}.
     *
     * @param  roleIds
     *         Ids of Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return Updated CommandCallbackDataAction for chaining convenience
     *
     * @see    #allowedMentions(Collection)
     * @see    MessageAction#setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default CommandCallbackDataAction mentionRoles(@Nonnull long... roleIds)
    {
        Checks.notNull(roleIds, "RoleId array");
        String[] stringIds = new String[roleIds.length];
        for (int i = 0; i < roleIds.length; i++)
        {
            stringIds[i] = Long.toUnsignedString(roleIds[i]);
        }
        return mentionRoles(stringIds);
    }

    CommandCallbackDataAction flags(int flags);
    int getFlags();

}
