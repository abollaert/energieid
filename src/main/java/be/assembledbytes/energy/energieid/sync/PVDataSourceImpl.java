package be.assembledbytes.energy.energieid.sync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class PVDataSourceImpl implements PVDataSource {

    private static final String SELECT_TOTALS = """
            select readings.date_time, readings.reading
            from
            (
                select dd.datetime as date_time,
                       dd.etotaltoday as reading,
                       row_number() over (partition by date(dd.datetime) order by etotaltoday desc) as row_num
                from DayData dd
            ) readings
            where readings.date_time >= ? and readings.date_time < ?
            and readings.row_num = 1
            order by readings.date_time desc
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
        try (final Connection connection = DriverManager.getConnection(String.format("jdbc:mariadb://%s/%s?user=%s&password=%s",
                                                                                     this.databaseHost,
                                                                                     this.databaseName,
                                                                                     this.databaseUser,
                                                                                     this.databasePassword))) {
            final DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final DateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            final PreparedStatement statement = connection.prepareStatement(SELECT_TOTALS);

            statement.setString(1, fromFormat.format(Date.from(from.plus(1, ChronoUnit.DAYS))));
            statement.setString(2, toFormat.format(Date.from(to)));

            final List<Data> datapoints = new ArrayList<>();

            final ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Instant timestamp = Instant.ofEpochMilli(resultSet.getTimestamp(1).getTime());

                final LocalDate localDate = LocalDate.ofInstant(timestamp, ZoneId.of(TimeZone.getDefault().getID()));

                if (!localDate.isEqual(LocalDate.now())) {
                    timestamp = localDate.plus(1, ChronoUnit.DAYS)
                                         .atStartOfDay()
                                         .toInstant(ZoneId.systemDefault().getRules().getOffset(timestamp));
                }

                final double data = resultSet.getDouble(2);

                datapoints.add(new Data(Date.from(timestamp), data));
            }

            return datapoints;
        } catch (SQLException e) {
            throw new IllegalStateException(String.format("Cannot fetch data from database : %s", e.getMessage()), e);
        }
    }
}
