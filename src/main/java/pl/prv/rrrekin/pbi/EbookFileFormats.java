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

import java.util.ResourceBundle;

/**
 *
 * @author Michał Rudewicz
 */
public enum EbookFileFormats {

    EPUB, HTML;

    private static final ResourceBundle GUI_TEXTS = ResourceBundle.getBundle("pl/prv/rrrekin/pbi/gui");
    private static final String[] NAMES = GUI_TEXTS.getString("EBOOK_FILE_FORMATS").split("\\|");
    private static final String[] EXTENSIONS = "epub|html|mobi".split("\\|");
    private static final String[] DESCRIPTIONS = GUI_TEXTS.getString("EBOOK_FILE_DESC").split("\\|");
    private static final String[] REGEXPS = ".*\\.epub#.*\\.(html|htm)#.*\\.mobi".split("#");

    @Override
    public String toString() {
        return NAMES[ordinal()];
    }

    public String getExtension() {
        return EXTENSIONS[ordinal()];
    }

    String getFileDescription() {
        return DESCRIPTIONS[ordinal()];
    }

    String getFormatRegExp() {
        return REGEXPS[ordinal()];
    }

}
