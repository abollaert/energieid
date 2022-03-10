package be.assembledbytes.energy.energieid.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class EnergieIDClientImpl implements EnergieIDClient {

    /** Logger instance. */
    private static final Logger logger = LoggerFactory.getLogger(EnergieIDClientImpl.class);

    /** The webhook URL. */
    private final String webhookURL;

    /** {@link com.fasterxml.jackson.databind.ObjectMapper}. */
    private final ObjectMapper objectMapper;

    /** The {@link org.apache.http.client.HttpClient}. */
    private final HttpClient httpClient;

    public EnergieIDClientImpl(final String webhookURL) {
        this.webhookURL = webhookURL;
        this.objectMapper = new ObjectMapper();

        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ssZ");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        this.objectMapper.setDateFormat(dateFormat);

        this.httpClient = HttpClients.createDefault();
    }

    @Override
    public final void post(final List<Data> datapoints) {
        try {
            final MeterReading meterReading = MeterReading.createReading(datapoints);
            final byte[] dataToSend = this.objectMapper.writeValueAsBytes(meterReading);

            final BasicHttpEntity payload = new BasicHttpEntity();
            payload.setContent(new ByteArrayInputStream(dataToSend));
            payload.setContentLength(dataToSend.length);

            final HttpPost post = new HttpPost(this.webhookURL);

            post.setHeader("Content-type", "application/json");
            post.setEntity(payload);

            final HttpResponse response = this.httpClient.execute(post);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IllegalStateException(String.format("Error raised by EnergieID : status [%s]", response.getStatusLine()));
            }
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Error posting data to EnergieID : [%s]", e.getMessage()), e);
        }
    }
}
