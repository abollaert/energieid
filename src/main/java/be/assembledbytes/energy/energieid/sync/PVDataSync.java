package be.assembledbytes.energy.energieid.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

public final class PVDataSync {

    private static final Logger logger = LoggerFactory.getLogger(PVDataSync.class);

    public static final void main(final String[] args) {
        if (args.length != 1) {
            logger.error("Expected exactly one argument : the configuration properties file, instead got {} arguments : {}",
                         args.length,
                         args);

            System.exit(-1);
        }

        try {
            final Configuration configuration = Configuration.loadFrom(new FileInputStream(args[0]));

            final PVDataSource dataSource = new PVDataSourceImpl(configuration.databaseHostname(),
                                                                 configuration.databaseName(),
                                                                 configuration.databaseUsername(),
                                                                 configuration.databasePassword());

            logger.info("Loading datapoints from database, going back 100 days.");

            final List<Data> datapoints = dataSource.getData(Instant.now().minus(100, ChronoUnit.DAYS), Instant.now());

            logger.info("Posting {} datapoints to EnergieID.", datapoints.size());

            final EnergieIDClient client = new EnergieIDClientImpl(configuration.energieIdWebhook());
            client.post(datapoints);

            logger.info("Data posted.");
        } catch (IOException e) {
            logger.error(String.format("IO error while loading configuration : %s", e.getMessage()), e);

            System.exit(-2);
        }
    }
}
