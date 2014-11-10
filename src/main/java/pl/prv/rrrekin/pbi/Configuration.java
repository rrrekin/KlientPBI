/*
 * Copyright 2014 Michał Rudewicz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.prv.rrrekin.pbi;

import com.oracle.nio.BufferSecrets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * <p>
 * Configuration class.</p>
 *
 * @author Michał Rudewicz
 * @version $Id: $Id
 */
public class Configuration {

    private URL serverBaseUrl;
    private Integer timeout = 15000;
    private Integer threads = 10;
    private Integer maxCacheDays = 7;
    private final transient VetoableChangeSupport vetoableChangeSupport = new java.beans.VetoableChangeSupport(this);
    private static Configuration instance = null;
    private final transient PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    /**
     * Constant <code>PROP_SERVERBASEURL="PROP_SERVERBASEURL"</code>
     */
    public static final String PROP_SERVERBASEURL = "serverBaseUrl";
    /**
     * Constant <code>PROP_TIMEOUT="PROP_TIMEOUT"</code>
     */
    public static final String PROP_TIMEOUT = "timeout";
    /**
     * Constant <code>PROP_THREADS="PROP_THREADS"</code>
     */
    public static final String PROP_THREADS = "threads";

    /**
     * Constant <code>PARAM_THREADS="threads"</code>
     */
    protected static final String PARAM_THREADS = "threads";
    /**
     * Constant <code>PARAM_TIMEOUT="timeout"</code>
     */
    protected static final String PARAM_TIMEOUT = "timeout";
    protected static final String PARAM_MAXCACHEDAYS = "maxCacheDays";
    /**
     * Constant <code>PARAM_URL="URL"</code>
     */
    protected static final String PARAM_URL = "URL";
    private static final ResourceBundle guiTexts = ResourceBundle.getBundle("pl/prv/rrrekin/pbi/gui");
    public static final String PROP_MAXCACHEDAYS = "maxCacheDays";

    /**
     * Default config
     */
    public Configuration() {
        serverBaseUrl = Util.DEFAULT_URL;
    }

    /**
     * Copying constructor
     *
     * @param o a {@link pl.prv.rrrekin.pbi.Configuration} object.
     */
    public Configuration(Configuration o) {
        serverBaseUrl = o.serverBaseUrl;
        timeout = o.timeout;
        threads = o.threads;
        maxCacheDays = o.maxCacheDays;
    }

    /**
     * Return active instance of configuration
     *
     * @throws java.io.IOException if any.
     * @return a {@link pl.prv.rrrekin.pbi.Configuration} object.
     */
    synchronized public static Configuration getInstance() throws IOException {
        if (instance == null) {
            instance = new Configuration();
            try {
                instance.read();
            } catch (Exception ex) {
                return new Configuration();
            }
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        Properties p = buildProperties();
        return p.toString();
    }

    /**
     * <p>
     * updateConfiguration.</p>
     *
     * @param newConfig a {@link pl.prv.rrrekin.pbi.Configuration} object.
     */
    public static void updateConfiguration(Configuration newConfig) {
        if (!instance.serverBaseUrl.toString().equals(newConfig.serverBaseUrl.toString())) {
            instance.serverBaseUrl = newConfig.serverBaseUrl;
            instance.propertyChangeSupport.firePropertyChange(PROP_SERVERBASEURL, instance.serverBaseUrl, newConfig.serverBaseUrl);
        }
        if (!instance.timeout.equals(newConfig.timeout)) {
            instance.timeout = newConfig.timeout;
            instance.propertyChangeSupport.firePropertyChange(PROP_TIMEOUT, instance.timeout, newConfig.timeout);
        }
        if (!instance.threads.equals(newConfig.threads)) {
            instance.threads = newConfig.threads;
            instance.propertyChangeSupport.firePropertyChange(PROP_THREADS, instance.threads, newConfig.threads);
        }
        if (!instance.threads.equals(newConfig.maxCacheDays)) {
            instance.maxCacheDays = newConfig.maxCacheDays;
            instance.propertyChangeSupport.firePropertyChange(PROP_MAXCACHEDAYS, instance.maxCacheDays, newConfig.maxCacheDays);
        }
    }

    /**
     * <p>
     * read.</p>
     *
     * @throws java.io.IOException if any.
     */
    public void read() throws IOException {
        Properties props = new Properties();

        // First try loading from the current directory
        File file = Util.CONFIG_FILE;
        try (InputStream is = new FileInputStream(file)) {
            props.load(is);
        }

        //        try {
//            if (is == null) {
//                // Try loading from classpath
//                is = getClass().getResourceAsStream("server.properties");
//            }
//
//            // Try loading properties from the file (if found)
//        } catch (Exception e) {
//        }
        try {
            serverBaseUrl = new URL(props.getProperty(PARAM_URL, serverBaseUrl.toString()));
        } catch (MalformedURLException ex) {
        }
        try {
            timeout = Integer.valueOf(props.getProperty(PARAM_TIMEOUT, timeout.toString()));
        } catch (NumberFormatException ex) {
        }
        try {
            threads = new Integer(props.getProperty(PARAM_THREADS, threads.toString()));
        } catch (NumberFormatException ex) {
        }
        try {
            maxCacheDays = new Integer(props.getProperty(PARAM_MAXCACHEDAYS, maxCacheDays.toString()));
        } catch (NumberFormatException ex) {
        }
    }

    /**
     * <p>
     * save.</p>
     *
     * @throws java.io.IOException if any.
     */
    public void save() throws IOException {
        Properties props = buildProperties();
        File file = Util.CONFIG_FILE;
        try (OutputStream out = new FileOutputStream(file)) {
            props.store(out, "");
        }
    }

    /**
     * <p>
     * buildProperties.</p>
     *
     * @return a {@link java.util.Properties} object.
     */
    protected Properties buildProperties() {
        Properties props = new Properties();
        props.setProperty(PARAM_URL, serverBaseUrl.toString());
        props.setProperty(PARAM_TIMEOUT, timeout.toString());
        props.setProperty(PARAM_THREADS, threads.toString());
        props.setProperty(PARAM_MAXCACHEDAYS, maxCacheDays.toString());
        return props;
    }

    /**
     * <p>
     * Getter for the field <code>serverBaseUrl</code>.</p>
     *
     * @return the serverBaseUrl
     */
    public URL getServerBaseUrl() {
        return serverBaseUrl;
    }

    /**
     * <p>
     * Setter for the field <code>serverBaseUrl</code>.</p>
     *
     * @param serverBaseUrl the serverBaseUrl to set
     * @throws java.beans.PropertyVetoException if any.
     */
    public void setServerBaseUrl(URL serverBaseUrl) throws PropertyVetoException {
        java.net.URL oldServerBaseUrl = this.serverBaseUrl;
//        try {
//            java.net.URL newServerBaseUrl = new URL(serverBaseUrl);
        vetoableChangeSupport.fireVetoableChange(PROP_SERVERBASEURL, oldServerBaseUrl, serverBaseUrl);
        this.serverBaseUrl = serverBaseUrl;
//        } catch (MalformedURLException ex) {
//            throw new PropertyVetoException(ex.getLocalizedMessage(), new PropertyChangeEvent(this, PROP_SERVERBASEURL,
//                    oldServerBaseUrl.toString(), serverBaseUrl));
//        }
        propertyChangeSupport.firePropertyChange(PROP_SERVERBASEURL, oldServerBaseUrl, serverBaseUrl);
    }

    /**
     * <p>
     * Getter for the field <code>timeout</code>.</p>
     *
     * @return the timeout
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * <p>
     * Setter for the field <code>timeout</code>.</p>
     *
     * @param timeout the timeout to set
     * @throws java.beans.PropertyVetoException if any.
     */
    public void setTimeout(Integer timeout) throws PropertyVetoException {
        java.lang.Integer oldTimeout = this.timeout;
        if (timeout < 1000 || timeout > 300000) {
            throw new PropertyVetoException(guiTexts.getString("INVALID_TIMEOUT"), new PropertyChangeEvent(this, PROP_TIMEOUT,
                    oldTimeout, timeout));
        }
        vetoableChangeSupport.fireVetoableChange(PROP_TIMEOUT, oldTimeout, timeout);
        this.timeout = timeout;
        propertyChangeSupport.firePropertyChange(PROP_TIMEOUT, oldTimeout, timeout);
    }

    /**
     * <p>
     * Getter for the field <code>threads</code>.</p>
     *
     * @return the threads
     */
    public Integer getThreads() {
        return threads;
    }

    /**
     * <p>
     * Setter for the field <code>threads</code>.</p>
     *
     * @param threads the threads to set
     * @throws java.beans.PropertyVetoException if any.
     */
    public void setThreads(Integer threads) throws PropertyVetoException {
        java.lang.Integer oldThreads = this.threads;
        if (threads < 1 || threads > 50) {
            throw new PropertyVetoException(guiTexts.getString("INVALID_THREADS_NUMBER"), new PropertyChangeEvent(this,
                    PROP_THREADS, oldThreads, threads));
        }
        vetoableChangeSupport.fireVetoableChange(PROP_THREADS, oldThreads, threads);
        this.threads = threads;
        propertyChangeSupport.firePropertyChange(PROP_THREADS, oldThreads, threads);
    }

    /**
     * <p>
     * addPropertyChangeListener.</p>
     *
     * @param listener a {@link java.beans.PropertyChangeListener} object.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * <p>
     * removePropertyChangeListener.</p>
     *
     * @param listener a {@link java.beans.PropertyChangeListener} object.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * @return the maxCacheDays
     */
    public Integer getMaxCacheDays() {
        return maxCacheDays;
    }

    /**
     * @param maxCacheDays the maxCacheDays to set
     */
    public void setMaxCacheDays(Integer maxCacheDays) throws PropertyVetoException {
        java.lang.Integer oldMaxCacheDays = this.maxCacheDays;
        if (maxCacheDays < 1 || maxCacheDays > 500) {
            throw new PropertyVetoException(guiTexts.getString("INVALID_THREADS_NUMBER"), new PropertyChangeEvent(this,
                    PROP_MAXCACHEDAYS, oldMaxCacheDays, maxCacheDays));
        }
        vetoableChangeSupport.fireVetoableChange(PROP_MAXCACHEDAYS, oldMaxCacheDays, maxCacheDays);
        this.maxCacheDays = maxCacheDays;
        propertyChangeSupport.firePropertyChange(PROP_MAXCACHEDAYS, oldMaxCacheDays, maxCacheDays);
    }

}
