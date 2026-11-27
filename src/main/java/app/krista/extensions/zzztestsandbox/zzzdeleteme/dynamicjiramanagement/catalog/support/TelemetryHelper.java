package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support;

import app.krista.ksdk.telemetry.TelemetryMetrics;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/** Telemetry lifecycle wrapper. Recording must NEVER crash business logic (swallow-all), and attributes
 *  stay low-cardinality (operation name + status/error-class only — never issue keys, emails, URLs). */
@Service
public class TelemetryHelper {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryHelper.class);
    private final TelemetryMetrics metrics;

    @Inject
    public TelemetryHelper(TelemetryMetrics metrics) {
        this.metrics = metrics;
    }

    /** prefix e.g. "jira.CreateIssue". Call at request start; returns the start timestamp. */
    public long start(String prefix) {
        try {
            metrics.incrementCounter(prefix + ".requests", 1, Map.of());
        } catch (Exception ignored) {
            LOG.trace("telemetry start failed for {}", prefix);
        }
        return System.currentTimeMillis();
    }

    public void recordSuccess(String prefix, long startTime) {
        try {
            long ms = System.currentTimeMillis() - startTime;
            Map<String, String> t = new HashMap<>();
            t.put("status", "success");
            metrics.incrementCounter(prefix + ".success", 1, t);
            metrics.recordDuration(prefix + ".duration", ms, t);
        } catch (Exception ignored) {
            LOG.trace("telemetry success failed for {}", prefix);
        }
    }

    public void recordError(String prefix, long startTime, Exception ex) {
        try {
            long ms = System.currentTimeMillis() - startTime;
            Map<String, String> t = new HashMap<>();
            t.put("status", "error");
            t.put("error_class", ex.getClass().getSimpleName());
            metrics.incrementCounter(prefix + ".failure", 1, t);
            metrics.recordDuration(prefix + ".duration", ms, t);
        } catch (Exception ignored) {
            LOG.trace("telemetry error failed for {}", prefix);
        }
    }
}
