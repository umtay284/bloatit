package com.bloatit.framework.utils;

/**
 * <p>
 * A class used to describe a set of parameters
 * </p>
 * <p>
 * Parameters are described as pairs Key->value
 * </p>
 * <p>
 * Whenever a parameter is read, it is deleted. A convenience method is provided to read
 * without deletion, but should be used unless for checking
 * </p>
 */
public class Parameters extends GenericParameters<HttpParameter, String> {

    /**
     * Create an empty list of parameter
     */
    public Parameters() {
        super();
    }

    /**
     * Add a parameter to the list
     * 
     * @param name the name of the parameter to add
     * @param value the value of the paramter to add
     * @return itself
     */
    @Override
    public final void add(final String name, final String value) {
        final HttpParameter httpParameter = getElements().get(name);
        if (httpParameter != null) {
            httpParameter.add(value);
        } else {
            final HttpParameter newHttpParameter = new HttpParameter(value);
            getElements().put(name, newHttpParameter);
        }
    }
}
