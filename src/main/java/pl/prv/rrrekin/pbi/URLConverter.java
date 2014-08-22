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

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>URLConverter class.</p>
 *
 * @author Michał Rudewicz
 * @version $Id: $Id
 */
public class URLConverter extends org.jdesktop.beansbinding.Converter<URL, String> {
    private final Log logger = LogFactory.getLog(this.getClass());
    /** {@inheritDoc} */
    @Override
    public String convertForward(URL url) {
        if (logger.isDebugEnabled()) {
            logger.debug("URL to String "+url);
        }
        return url.toString();
    }

    /** {@inheritDoc} */
    @Override
    public URL convertReverse(String t) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("String to URL "+t);
            }
            return new URL(t);
        } catch (Exception ex) {
            return Util.DEFAULT_URL;
        }
    }

}
