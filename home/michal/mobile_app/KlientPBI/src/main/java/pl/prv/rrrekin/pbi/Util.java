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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import javax.swing.UIManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Utility class.</p>
 *
 * @author Michał Rudewicz
 * @version $Id: $Id
 */
public class Util {

    /**
     * Configuration file name.
     */
    protected static final File CONFIG_FILE;
    /**
     * Default URL value for PBI. <code>DEFAULT_URL_STRING="http://www.pbi.edu.pl/"</code>
     */
    public static final String DEFAULT_URL_STRING = "http://www.pbi.edu.pl/";
    public static final String FALLBACK_URL_STRING = "http://91.238.85.34/";
    /**
     * Default URL value for PBI as URL object.
     */
    public static URL DEFAULT_URL;
    public final static File CACHE_DIR;
    public final static File CACHE_IMAGES;
    public final static File LOGO_FILE;
    static String EMPTY_HTML
            = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"></html>";

    private static final Log logger = LogFactory.getLog(Util.class);

    static {
        try {
            DEFAULT_URL = new URL(DEFAULT_URL_STRING);
            DEFAULT_URL.openConnection().connect();
        } catch (MalformedURLException ex) {
            logger.fatal(ex.getLocalizedMessage());
            System.exit(1);
        } catch (IOException ex) {
            logger.error("Cannot connect to the default PBI URL: " + ex.getLocalizedMessage()+". Using fallback URL.");
            try {
                DEFAULT_URL = new URL(FALLBACK_URL_STRING);
            } catch (MalformedURLException ex1) {
                logger.fatal(ex.getLocalizedMessage());
                System.exit(1);
            }
        }

        File dir;
        // Try Windows App Data dir.
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) { // Windows
            String dataFolder = System.getenv("APPDATA");
            if (dataFolder == null) {
                dataFolder = System.getProperty("user.home") + "\\Local Settings\\Application Data";
            }
            dir = new File(dataFolder);
//            if (!(dir.exists() && dir.isDirectory())) {
//                dir = new File(System.getProperty("user.home"));
//            }
            dir = new File(dir, "Klient PBI");

        } else if (os.contains("mac")) { // Mac OS '~/Library/Caches'
            dir = new File(System.getProperty("user.home"), "Library");
            dir = new File(dir, "Caches");
            dir = new File(dir, "Klient PBI");
        } else { // Unices '~/.cache'
            dir = new File(System.getProperty("user.home"), ".cache");
            dir = new File(dir, "Klient PBI");
        }

        if (dir.exists() && !dir.isDirectory()) {
            dir.delete();
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }

        CACHE_DIR = dir;
        CACHE_IMAGES = new File(dir, "images");
        if (CACHE_IMAGES.exists() && !CACHE_IMAGES.isDirectory()) {
            CACHE_IMAGES.delete();
        }
        if (!CACHE_IMAGES.exists()) {
            CACHE_IMAGES.mkdirs();
        }
        LOGO_FILE = new File(CACHE_IMAGES, "pbi.gif");
        CONFIG_FILE = new File(dir, "pbi.config");

        try {
            // Preload all project classes
            Class.forName("pl.prv.rrrekin.pbi.About");
            Class.forName("pl.prv.rrrekin.pbi.BookDownloader");
            Class.forName("pl.prv.rrrekin.pbi.ConfigWindow");
            Class.forName("pl.prv.rrrekin.pbi.Configuration");
            Class.forName("pl.prv.rrrekin.pbi.Depagination");
            Class.forName("pl.prv.rrrekin.pbi.DownloadWindow");
            Class.forName("pl.prv.rrrekin.pbi.MainWindow");
            Class.forName("pl.prv.rrrekin.pbi.PbiBook");
            Class.forName("pl.prv.rrrekin.pbi.PbiBookEntry");
            Class.forName("pl.prv.rrrekin.pbi.PbiSearch");
            Class.forName("pl.prv.rrrekin.pbi.URLConverter");
            Class.forName("pl.prv.rrrekin.pbi.URLValidator");
            Class.forName("pl.prv.rrrekin.pbi.models.SearchResultModel");
        } catch (ClassNotFoundException ex) {
            logger.info(ex.getLocalizedMessage());
        }

    }

    static File cacheFile(int id) {
        return new File(CACHE_DIR, Integer.toString(id));
    }

}
