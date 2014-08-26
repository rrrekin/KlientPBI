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

import OCG.image.NeuQuant;
import OCG.image.NeuQuantMod;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import javax.imageio.ImageIO;
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
    static String VERSION_URL = "http://rrrekin.github.io/KlientPBI/last_version";
    private static File imagesDir;

    static {
        try {
            DEFAULT_URL = new URL(DEFAULT_URL_STRING);
            DEFAULT_URL.openConnection().connect();
        } catch (MalformedURLException ex) {
            logger.fatal(ex.getLocalizedMessage());
            System.exit(1);
        } catch (IOException ex) {
            logger.error("Cannot connect to the default PBI URL: " + ex.getLocalizedMessage() + ". Using fallback URL.");
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
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");

    }

    static File cacheFile(int id) {
        return new File(CACHE_DIR, Integer.toString(id));
    }

    public static BufferedImage indexImage(BufferedImage image) {
        NeuQuant nq;

        try {
            final int x = image.getWidth();
            final int y = image.getHeight();
            nq = new NeuQuant(image, x, y);
            nq.init();
            int argb;

            for (int yIndex = 0; yIndex < y; yIndex++) {
                for (int xIndex = 0; xIndex < x; xIndex++) {
                    argb = image.getRGB(xIndex, yIndex);
                    image.setRGB(xIndex, yIndex, nq.convert(argb));
                }
            }
        } catch (final IOException ex) {
            // throw new RuntimeException("Quantizer failed" + e.getMessage());
            // // Should never happen so no RuntimeException
            logger.error(null, ex);
        }

        return image;
    }

    static URL prepareImageFile(String fileName, boolean process, int processType, int bookId, final int desWidth, int desHeight)
            throws MalformedURLException {
        if (imagesDir == null) {
            imagesDir = new File(Util.CACHE_IMAGES, Integer.toString(bookId));
        }
        if (process) {
            String processedSubdir = String.format("C%d-%dx%d", processType, desWidth, desHeight);
            File outDir = new File(imagesDir, processedSubdir);
            outDir.mkdirs();
            String newFilename;
            if (processType != 0) {
                newFilename = fileName.replaceFirst("\\.[^.]*", ".png");
            } else {
                newFilename = fileName;
            }
            File outFile = new File(outDir, newFilename);
            File inFile = new File(imagesDir, fileName);

            if (!outFile.exists()) {
                genarateProcessedImage(inFile, desWidth, desHeight, outFile, processType);
            }

            return outFile.toURI().toURL();
        } else {
            return (new File(imagesDir, fileName)).toURI().toURL();
        }
    }

    static void genarateProcessedImage(File inFile, final int desWidth, final int desHeight, File outFile, int processType) {
        BufferedImage originalImage = null;
        try {
            originalImage = ImageIO.read(inFile);

            int imgWidth = originalImage.getWidth();
            int imgHeight = originalImage.getHeight();
            double hRatio = ((double) desWidth) / ((double) imgWidth);
            double vRatio = ((double) desHeight) / ((double) imgHeight);
            double ratio = Math.min(vRatio, hRatio);

            int newWidth = (int) (imgWidth * ratio);
            int newHeight = (int) (imgHeight * ratio);

            switch (processType) {
                case 0: { // Fullcolor
                    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = resizedImage.createGraphics();
                    g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                    g.dispose();
                    try {
                        ImageIO.write(resizedImage, "jpeg", outFile);
                    } catch (IOException ex) {
                        logger.error("Unable to write output image", ex);
                    }
                    break;
                }

                case 1: { //B&W
                    BufferedImage resizedImage = getNormalizedGrayscaleImage(newWidth, newHeight, originalImage);
                    BufferedImage bitmap = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_BINARY);
                    Graphics2D g = bitmap.createGraphics();
                    g.drawImage(resizedImage, 0, 0, newWidth, newHeight, null);
                    g.dispose();
                    try {
                        ImageIO.write(bitmap, "png", outFile);
                    } catch (IOException ex) {
                        logger.error("Unable to write output image", ex);
                    }
                    break;
                }
                case 2: { //4 gray
                    BufferedImage resizedImage = getNormalizedGrayscaleImage(newWidth, newHeight, originalImage);
                    final byte[] scale = new byte[]{0, 80, -100, -1};
                    final IndexColorModel cm = new IndexColorModel(2, 4, scale, scale, scale);
                    BufferedImage bitmap = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_INDEXED, cm);
                    Graphics2D g = bitmap.createGraphics();
                    g.drawImage(resizedImage, 0, 0, newWidth, newHeight, null);
                    g.dispose();
                    try {
                        ImageIO.write(bitmap, "png", outFile);
                    } catch (IOException ex) {
                        logger.error("Unable to write output image", ex);
                    }
                    break;
                }
                case 3: { //16 gray
                    BufferedImage resizedImage = getNormalizedGrayscaleImage(newWidth, newHeight, originalImage);
                    final byte[] scale = new byte[]{0, 17, 34, 51, 68, 85, 102, 119, -120, -103, -86, -69, -52, -35, -18, -1};
                    final IndexColorModel cm = new IndexColorModel(4, 16, scale, scale, scale);
                    BufferedImage bitmap = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_INDEXED, cm);
                    Graphics2D g = bitmap.createGraphics();
                    g.drawImage(resizedImage, 0, 0, newWidth, newHeight, null);
                    g.dispose();
                    try {
                        ImageIO.write(bitmap, "png", outFile);
                    } catch (IOException ex) {
                        logger.error("Unable to write output image", ex);
                    }
                    break;
                }
                case 4: { //16 colors
                    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = resizedImage.createGraphics();
                    g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                    g.dispose();
                    try {
                        NeuQuantMod quant = new NeuQuantMod(resizedImage, 16);
                        BufferedImage indexImage = quant.getIndexImage();
                        try {
                            ImageIO.write(indexImage, "png", outFile);
                        } catch (IOException ex) {
                            logger.error("Unable to write output image", ex);
                        }
                    } catch (IOException ex) {
                        logger.error("Unable to auantize image", ex);
                    }

                    break;
                }
                case 5: { //256 colors
                    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = resizedImage.createGraphics();
                    g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                    g.dispose();
                    try {
                        NeuQuantMod quant = new NeuQuantMod(resizedImage);
                        BufferedImage indexImage = quant.getIndexImage();
                        try {
                            ImageIO.write(indexImage, "png", outFile);
                        } catch (IOException ex) {
                            logger.error("Unable to write output image", ex);
                        }
                    } catch (IOException ex) {
                        logger.error("Unable to auantize image", ex);
                    }

                    break;
                }
            }
        } catch (IOException ex) {
            logger.error("Unable to read input image", ex);
        }
    }

    static BufferedImage getNormalizedGrayscaleImage(int newWidth, int newHeight, BufferedImage originalImage) {
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();
        final byte[] data = ((DataBufferByte) resizedImage.getRaster().getDataBuffer()).getData();
        int[] histogram = new int[256];
        for (int v : data) {
            if (v >= 0) {
                histogram[v]++;
            } else {
                histogram[256 + v]++;
            }
        }
        int pixCount = newWidth * newHeight;
        int percentToSkip = pixCount / 40;
        int min = 0, max = 0;
        int sum = 0;
        for (int i = 0; i < 256; i++) { //dark
            sum += histogram[i];
            if (sum >= percentToSkip) {
                min = i;
                break;
            }
        }
        // Find upper limit
        sum = 0;
        for (int i = 255; i >= 0; i--) { //light
            sum += histogram[i];
            if (sum >= percentToSkip * 4) {
                max = i;
                break;
            }
        }
        float multiply = 255.0f / (max - min);
        float offset = 255.0f * min / (min - max);
        RescaleOp rescaleOp = new RescaleOp(multiply, offset, null);
        rescaleOp.filter(resizedImage, resizedImage);
        return resizedImage;
    }
}
