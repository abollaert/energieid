package be.assembledbytes.energy.energieid.sync;

import java.time.Instant;
import java.util.List;

public interface PVDataSource {

    List<Data> getData(final Instant from, final Instant to);
}
