package net.sf.buildbox.changes;

public enum BuildToolRole {
    COMPILER("compiler"),
    BUILD("build"),
    RELEASE("release");
    private final String name;

    private BuildToolRole(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
