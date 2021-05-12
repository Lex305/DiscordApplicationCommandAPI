package de.lexoland.api.discord.applicationcommand;

import net.dv8tion.jda.api.utils.data.DataObject;

public class ApplicationCommandPermission {

    private final long id;
    private final PermissionType type;
    private final boolean permission;

    public ApplicationCommandPermission(long id, PermissionType type, boolean permission) {
        this.id = id;
        this.type = type;
        this.permission = permission;
    }

    public long getId() {
        return id;
    }

    public PermissionType getType() {
        return type;
    }

    public boolean hasPermission() {
        return permission;
    }

    public DataObject getJSON() {
        DataObject dataObject = DataObject.empty();
        dataObject.put("id", String.valueOf(id));
        dataObject.put("type", type.getValue());
        dataObject.put("permission", permission);
        return dataObject;
    }
}
