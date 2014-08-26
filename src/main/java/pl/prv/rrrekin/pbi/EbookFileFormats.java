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

import java.util.Properties;
import java.util.ResourceBundle;

/**
 *
 * @author Michał Rudewicz
 */
public enum EbookFileFormats {

    EPUB, HTML;//, MOBI;

    private static final ResourceBundle guiTexts = ResourceBundle.getBundle("pl/prv/rrrekin/pbi/gui");
    private final static String[] names = guiTexts.getString("EBOOK_FILE_FORMATS").split("\\|");
    private final static String[] extensions = "epub|html|mobi".split("\\|");
    private final static String[] descriptions = guiTexts.getString("EBOOK_FILE_DESC").split("\\|");
    private final static String[] re = ".*\\.epub#.*\\.(html|htm)#.*\\.mobi".split("#");
    
    @Override
    public String toString() {
        return names[ordinal()];
    }
    
    public String getExtension(){
        return extensions[ordinal()];
    }

//    static{
//        for(EbookFileFormats v:values()){
//            System.out.println(v.name()+": "+v);
//        }
//    }

    String getFileDescription() {
        return descriptions[ordinal()];
    }

    String getFormatRegExp() {
        return re[ordinal()];
    }
    
   

}
