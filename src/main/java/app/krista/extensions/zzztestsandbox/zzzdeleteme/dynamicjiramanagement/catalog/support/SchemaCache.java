package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraFieldSchema;
import org.jvnet.hk2.annotations.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches discovered field schema keyed by Project Key + Issue Type Id (requirement section 6 — "Schema
 * Caching and Refresh"). In-memory, per-JVM — proportionate for a per-invoker extension process; avoids
 * repeated {@code createmeta} round-trips for the common "create several tickets of the same type in
 * the same project" pattern, while a configurable TTL ({@code Schema Cache Minutes} on the Setup tab)
 * and an explicit {@code Force Refresh} request input (see SchemaArea) together satisfy "never use
 * stale schema for mandatory field validation without an appropriate refresh mechanism."
 */
@Service
public class SchemaCache {

    private final Map<String, Entry> cache = new ConcurrentHashMap<>();

    public List<JiraFieldSchema> get(String projectKey, String issueTypeId, int ttlMinutes) {
        Entry entry = cache.get(key(projectKey, issueTypeId));
        if (entry == null) {
            return null;
        }
        long ageMinutes = (System.currentTimeMillis() - entry.cachedAtMillis) / 60_000L;
        return ageMinutes <= ttlMinutes ? entry.fields : null;
    }

    public void put(String projectKey, String issueTypeId, List<JiraFieldSchema> fields) {
        cache.put(key(projectKey, issueTypeId), new Entry(fields, System.currentTimeMillis()));
    }

    public void invalidate(String projectKey, String issueTypeId) {
        cache.remove(key(projectKey, issueTypeId));
    }

    private static String key(String projectKey, String issueTypeId) {
        return projectKey + "::" + issueTypeId;
    }

    private record Entry(List<JiraFieldSchema> fields, long cachedAtMillis) {
    }
}
