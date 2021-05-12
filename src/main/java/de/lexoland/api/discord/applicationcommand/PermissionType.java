package de.lexoland.api.discord.applicationcommand;

public enum PermissionType {

    ROLE(1),
    USER(2);

    private final int value;

    PermissionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
