package net.accela.prisma.session;

import net.accela.prisma.PrismaWM;
import net.accela.prisma.geometry.Size;
import net.accela.server.permissions.Permission;
import net.accela.server.permissions.PermissionAttachment;
import net.accela.server.permissions.PermissionAttachmentInfo;
import net.accela.server.plugin.Plugin;
import net.accela.server.session.Session;
import net.accela.server.session.SessionLogger;
import net.accela.server.session.provider.SessionCreator;
import net.accela.server.session.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class TextGraphicsSession extends Session {
    // Charset configuration
    public final static Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    public final static Charset ASCII_CHARSET = StandardCharsets.US_ASCII;
    public final static Charset IBM437_CHARSET = Charset.forName("IBM437");

    protected final SessionLogger sessionLogger;
    protected PrismaWM windowManager;
    protected User user;

    // Terminal feature compatibility flags
    protected boolean supportsAixtermColor = true;
    protected boolean supports8BitColor = true;
    protected boolean supports24BitColor = true;
    protected boolean supportsICEColor = true;
    protected boolean supportsUnicode = true;
    protected @NotNull Size terminalSize = new Size(80, 24);

    public TextGraphicsSession(@NotNull final SessionCreator sessionCreator, @NotNull final UUID uuid) {
        // Perform default actions
        super(sessionCreator, uuid);

        // Create a session logger
        this.sessionLogger = new SessionLogger(this);
    }

    public abstract void writeToClient(@NotNull String str);

    public abstract void swapWM(@NotNull Class<? extends PrismaWM> engineClass);

    public @Nullable PrismaWM getWindowManager() {
        return windowManager;
    }

    public void setTerminalSize(@NotNull Size size) {
        terminalSize = size;
    }

    public @NotNull Size getTerminalSize() {
        return terminalSize;
    }

    public boolean setAixtermColorSupport() {
        return supportsAixtermColor;
    }

    public boolean set8BitColorSupport() {
        return supports8BitColor;
    }

    public boolean set24BitColorSupport() {
        return supports24BitColor;
    }

    public boolean setIceColorSupport() {
        return supportsICEColor;
    }

    public boolean getUnicodeSupport() {
        return supportsUnicode;
    }

    public void setUnicodeSupport(boolean enable) {
        supportsUnicode = enable;
    }

    public abstract @NotNull Charset getCharset();

    public abstract @NotNull List<@NotNull Charset> getSupportedCharsets();

    public abstract void setCharset(@NotNull Charset charset) throws UnsupportedCharsetException;

    /**
     * @return This sessions logger
     */
    @Override
    public @NotNull SessionLogger getLogger() {
        return sessionLogger;
    }

    /**
     * @return The user that is currently logged in to this session, if any.
     */
    @Override
    public @Nullable User getUser() {
        return user;
    }

    /**
     * If a user has logged in to this session, or if it is anonymous
     *
     * @return true for user, false for anonymous
     */
    @Override
    public boolean hasUser() {
        return user != null;
    }

    @Override
    public @NotNull String getFriendlyName() {
        if (hasUser()) return user.getName();
        else {
            String UUIDString = uuid.toString();
            return UUIDString.substring(UUIDString.length() - 7);
        }
    }

    //
    // CommandSender
    //

    /**
     * Sends this sender a message
     *
     * @param message Message to be displayed
     */
    @Override
    public void sendMessage(@NotNull String message) {

    }

    /**
     * Sends this sender multiple messages
     *
     * @param messages An array of messages to be displayed
     */
    @Override
    public void sendMessage(@NotNull String[] messages) {

    }

    // Permissions -------------------------------------------------------------------------------------------

    /**
     * Checks if this object contains an override for the specified
     * permission, by fully qualified name
     *
     * @param name Name of the permission
     * @return true if the permission is set, otherwise false
     */
    @Override
    public boolean isPermissionSet(String name) {
        return false;
    }

    /**
     * Checks if this object contains an override for the specified {@link
     * Permission}
     *
     * @param perm Permission to check
     * @return true if the permission is set, otherwise false
     */
    @Override
    public boolean isPermissionSet(Permission perm) {
        return false;
    }

    @Override
    public boolean hasPermission(String p) {
        return false;
    }

    /**
     * Gets the value of the specified permission, if set.
     * <p>
     * If a permission override is not set on this object, the default value
     * of the permission will be returned
     *
     * @param perm Permission to get
     * @return Value of the permission
     */
    @Override
    public boolean hasPermission(Permission perm) {
        return false;
    }

    /**
     * Adds a new {@link PermissionAttachment} with a single permission by
     * name and value
     *
     * @param plugin Plugin responsible for this attachment, may not be null
     *               or disabled
     * @param name   Name of the permission to attach
     * @param value  Value of the permission
     * @return The PermissionAttachment that was just created
     */
    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return null;
    }

    /**
     * Adds a new empty {@link PermissionAttachment} to this object
     *
     * @param plugin Plugin responsible for this attachment, may not be null
     *               or disabled
     * @return The PermissionAttachment that was just created
     */
    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return null;
    }

    /**
     * Removes the given {@link PermissionAttachment} from this object
     *
     * @param attachment Attachment to remove
     * @throws IllegalArgumentException Thrown when the specified attachment
     *                                  isn't part of this object
     */
    @Override
    public void removeAttachment(PermissionAttachment attachment) {

    }

    /**
     * Recalculates the permissions for this object, if the attachments have
     * changed values.
     * <p>
     * This should very rarely need to be called from a plugin.
     */
    @Override
    public void recalculatePermissions() {

    }

    /**
     * Gets a set containing all of the permissions currently in effect by
     * this object
     *
     * @return Set of currently effective permissions
     */
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return null;
    }
}
