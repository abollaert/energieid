package be.assembledbytes.energy.energieid.sync;

import java.util.List;

public interface EnergieIDClient {

    /**
     * Posts the given {@link List} of {@link Data} points.
     *
     * @param   datapoints  The data points.
     */
    void post(final List<Data> datapoints);
}
