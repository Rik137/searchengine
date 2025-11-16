package searchengine.model;

/**
 * Website indexing statuses.
 * <ul>
 *     <li>{@code INDEXING} — the site is currently being indexed</li>
 *     <li>{@code INDEXED} — the site has been successfully indexed</li>
 *     <li>{@code FAILED} — an error occurred during indexing</li>
 * </ul>
 */

public enum Status {
    INDEXING,
    INDEXED,
    FAILED
}
