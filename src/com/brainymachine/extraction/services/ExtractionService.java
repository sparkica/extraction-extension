package com.brainymachine.extraction.services;

/**
 * Interface for extraction services
 * @author Mateja Verlic
 */
public interface ExtractionService {
    /**
     * Extracts specified elements from the specified text
     * @param text The text
     * @return The extracted elements
     * @throws Exception if the extraction fails
     */
    public String[] extractElementValues(String text) throws Exception;
    
    /**
     * Gets the names of supported properties of the service
     * @return The property names
     */
    public String[] getPropertyNames();
    
    /**
     * Gets the value of the specified property
     * @param name The property name
     * @return The property value
     */
    public String getProperty(String name);
    
    /**
     * Sets the value of the specified property
     * @param name The property name
     * @param value The property value
     */
    public void setProperty(String name, String value);
    
    /**
     * Indicates whether the service has been configured
     * @return <tt>true</tt> if the service has been configured
     */
    public boolean isConfigured();
    

}
