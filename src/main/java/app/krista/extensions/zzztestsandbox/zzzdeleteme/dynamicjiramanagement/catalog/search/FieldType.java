package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.search;

/** Fixed field-type set used by the field-mapping registry and the JQL operator matrix (per
 *  entity-search-implementation's canonical EntityStore pattern). */
public enum FieldType {
    TEXT,        // free-text, contains/startswith/endswith supported (JQL ~ operator)
    EXACT,       // exact-match string/enum-like field (JQL =, !=, IN, NOT IN)
    COMPARABLE,  // numeric/orderable (JQL =, !=, >, >=, <, <=)
    DATE,        // epoch-millis Long -> JQL date literal
    IDENTITY     // id/key style field; equals fast-paths to a direct lookup where possible
}
