package info.jallaix.message.dao.interceptor;

import org.springframework.data.mapping.PropertyPath;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Julien on 01/02/2017.
 */
public class I18nMessagePropertiesHolder {

    private Map<Class<?>, Set<PropertyPath>> i18nMessageProperties = new HashMap<>();

    public Set<PropertyPath> getPropertyPaths(Class<?> clazz) {
        return i18nMessageProperties.get(clazz);
    }

    public void setPropertyPaths(Class<?> clazz, Set<PropertyPath> propertyPaths) {
        i18nMessageProperties.put(clazz, propertyPaths);
    }
}
