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

/**
 * <p>URLValidator class.</p>
 *
 * @author Michał Rudewicz
 * @version $Id: $Id
 */
public class URLValidator extends org.jdesktop.beansbinding.Validator<String> {

    /** {@inheritDoc} */
    @Override
    public Result validate(String t) {
        try {
            new URL(t);
            return null;
        } catch (Exception ex) {
            return new Result(null, ex.getLocalizedMessage());
        }
    }

}
