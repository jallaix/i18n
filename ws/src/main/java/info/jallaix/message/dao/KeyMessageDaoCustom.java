package info.jallaix.message.dao;

import info.jallaix.message.bean.KeyMessage;

/**
 * This interface manages custom datasource accesses related to a domain.
 */
public interface KeyMessageDaoCustom {

    /**
     * Find a message for the specified arguments.
     *
     * @param domainId    Domain identifier to filter messages
     * @param key         Message key
     * @param languageTag Language tag to filter messages
     * @return The matched message or {@code null}
     */
    @SuppressWarnings("unused")
    KeyMessage findOne(final String domainId, final String key, final String languageTag);

    /**
     * <p>Find messages with content matching the provided one.</p>
     * <p>The results may be filtered by domain identifier and language tag.</p>
     *
     * @param domainId    Domain identifier to filter messages
     * @param languageTag Language tag to filter messages
     * @param content     Content to match
     * @return The array of matching entity messages
     */
    @SuppressWarnings("unused")
    Iterable<KeyMessage> findByContent(final String domainId, final String languageTag, final String content);
}
