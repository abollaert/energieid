package be.assembledbytes.energy.energieid.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonSerialize
public record MeterReading(@JsonProperty("remoteId") String remoteId,
                           @JsonProperty("remoteName") String remoteName,
                           @JsonProperty("metric") String metric,
                           @JsonProperty("unit") String unit,
                           @JsonProperty("readingType") String readingType,
                           @JsonProperty("data") List<Data> data) {

    private static final String REMOTE_ID = "solar-sunny-boy-3000-tl";
    private static final String REMOTE_NAME = "Solar Panels";
    private static final String METRIC = "electricityExport";
    private static final String UNIT = "kWh";
    private static final String READING_TYPE = "counter";

    public static final MeterReading createReading(final List<Data> datapoints) {
        return new MeterReading(REMOTE_ID,
                                REMOTE_NAME,
                                METRIC,
                                UNIT,
                                READING_TYPE,
                                datapoints);
    }
}
