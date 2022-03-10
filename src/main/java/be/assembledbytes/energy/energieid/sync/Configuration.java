package be.assembledbytes.energy.energieid.sync;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public record Configuration(String energieIdWebhook,
                            String databaseHostname,
                            String databaseName,
                            String databaseUsername,
                            String databasePassword) {

    private static final String PROPERTY_ENERGIEID_WEBHOOK = "energieid.webhook";
    private static final String PROPERTY_DATABASE_HOSTNAME = "database.host";
    private static final String PROPERTY_DATABASE_NAME = "database.name";
    private static final String PROPERTY_DATABASE_USERNAME = "database.username";
    private static final String PROPERTY_DATABASE_PASSWORD = "database.password";

    public static Configuration loadFrom(final InputStream inputStream) throws IOException {
        final Properties properties = new Properties();
        properties.load(inputStream);

        final String webhookUrl = properties.getProperty(PROPERTY_ENERGIEID_WEBHOOK);
        final String databaseHostname = properties.getProperty(PROPERTY_DATABASE_HOSTNAME);
        final String databaseName = properties.getProperty(PROPERTY_DATABASE_NAME);
        final String databaseUsername = properties.getProperty(PROPERTY_DATABASE_USERNAME);
        final String databasePassword = properties.getProperty(PROPERTY_DATABASE_PASSWORD);

        return new Configuration(webhookUrl,
                                 databaseHostname,
                                 databaseName,
                                 databaseUsername,
                                 databasePassword);
    }

    public Configuration {
        Objects.requireNonNull(energieIdWebhook);
        Objects.requireNonNull(databaseHostname);
        Objects.requireNonNull(databaseName);
        Objects.requireNonNull(databaseUsername);
        Objects.requireNonNull(databasePassword);
    }
}
