package be.assembledbytes.energy.energieid.sync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PVDataSourceImpl implements PVDataSource {

    private static final String SELECT_TOTALS = """
            select date(dd.datetime), max(etotaltoday)
            from DayData dd
            where date(dd.datetime) > ? and date(dd.datetime) <= ? 
            group by date(dd.datetime)
            order by date(dd.datetime) desc
            """;

    private final String databaseHost;
    private final String databaseName;
    private final String databaseUser;
    private final String databasePassword;

    public PVDataSourceImpl(final String databaseHost,
                            final String databaseName,
                            final String databaseUser,
                            final String databasePassword) {
        this.databaseHost = databaseHost;
        this.databaseName = databaseName;
        this.databaseUser = databaseUser;
        this.databasePassword = databasePassword;
    }

    @Override
    public List<Data> getData(final Instant from, final Instant to) {
        try (final Connection connection = DriverManager.getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s",
                                                                                     this.databaseHost,
                                                                                     this.databaseName,
                                                                                     this.databaseUser,
                                                                                     this.databasePassword))) {
            final DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
            final DateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd");

            final PreparedStatement statement = connection.prepareStatement(SELECT_TOTALS);

            statement.setString(1, fromFormat.format(Date.from(from)));
            statement.setString(2, toFormat.format(Date.from(to)));

            final List<Data> datapoints = new ArrayList<>();

            final ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Date timestamp = resultSet.getTimestamp(1);
                final double data = resultSet.getDouble(2);

                datapoints.add(new Data(timestamp, data));
            }

            return datapoints;
        } catch (SQLException e) {
            throw new IllegalStateException(String.format("Cannot fetch data from database : %s", e.getMessage()), e);
        }
    }
}
