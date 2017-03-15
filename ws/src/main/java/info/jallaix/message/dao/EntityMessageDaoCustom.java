package info.jallaix.message.dao;

import info.jallaix.message.bean.EntityMessage;

/**
 * This interface manages custom datasource accesses related to a domain.
 */
public interface EntityMessageDaoCustom {

    /**
     * Find a message for the specified arguments.
     *
     * @param domainId    Domain identifier to filter messages
     * @param type        Type to filter messages
     * @param entityId    Entity identifier to filter messages
     * @param languageTag Language tag to filter messages
     * @return The matched message or {@code null}
     */
    @SuppressWarnings("unused")
    EntityMessage findOne(final String domainId, final String type, final String entityId, final String languageTag);

    /**
     * <p>Find messages with content matching the provided one.</p>
     * <p>The results may be filtered by domain identifier, message type and language tag.</p>
     *
     * @param domainId    Domain identifier to filter messages
     * @param type        Type to filter messages
     * @param languageTag Language tag to filter messages
     * @param content     Content to match
     * @return The array of matching entity messages
     */
    @SuppressWarnings("unused")
    Iterable<EntityMessage> findByContent(final String domainId, final String type, final String languageTag, final String content);
}
