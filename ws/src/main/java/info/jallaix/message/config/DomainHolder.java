package info.jallaix.message.config;

import info.jallaix.message.bean.Domain;

/**
 * An domain holder defines a way to get the internationalized domain matching the application.
 */
public interface DomainHolder {

    /**
     * Get the internationalized domain of the application
     *
     * @return The found domain
     */
    Domain getDomain();
}
