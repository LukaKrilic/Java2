package hr.algebra.brassbirmingham.jndi;

public enum ConfigurationKey {

    HOST_NAME("host.name"),
    RMI_SERVER_PORT("rmi.server.port"),
    PLAYER_ONE_SERVER_PORT("player.one.server.port"),
    PLAYER_TWO_SERVER_PORT("player.two.server.port");

    private final String key;

    ConfigurationKey(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
