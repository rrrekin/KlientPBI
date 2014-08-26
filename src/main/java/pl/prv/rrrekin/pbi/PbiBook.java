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

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.text.html.HTML;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import static java.util.zip.ZipOutputStream.STORED;

/**
 *
 * @author Michał Rudewicz
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PbiBook {

    @XmlTransient
    private final Log logger = LogFactory.getLog(this.getClass());
    private int id;
    private String[] pages;
    private String author;
    private String correctAuthor = null;
    private String title;
    private String[] contents;
    private Integer[] contentsPages;
    private Depagination depagination = Depagination.NONE;
    private static final ResourceBundle guiTexts = ResourceBundle.getBundle("pl/prv/rrrekin/pbi/gui");

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the pages
     */
    public String[] getPages() {
        return pages;
    }

    /**
     * @param pages the pages to set
     */
    public void setPages(String[] pages) {
        this.pages = pages;
    }

    /**
     * @return the contents
     */
    public String[] getContents() {
        return contents;
    }

    /**
     * @param contents the contents to set
     */
    public void setContents(String[] contents) {
        this.contents = contents;
    }

    /**
     * @param contentsPages the contentsPages to set
     */
    public void setContentsPages(Integer[] contentsPages) {
        this.contentsPages = contentsPages;
    }

    void exportAsHtml(final JDialog parent, final File file, final boolean process, final int procType, final int bookId,
            final int width, final int height) throws
            IOException {
//        File file = new File(String.format("%d.html", id));
        final Document document = buildHtml(false);
        final Elements imageElements = document.select("img");
        final int barSize = imageElements.size();

        final JDialog dialog = barSize > 2 ? new JDialog(parent, guiTexts.getString("SAVE_PROGRES_TITLE"), true) : null;
        final JProgressBar bar = barSize > 2 ? new JProgressBar(0, barSize) : null;
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            IOException ex = null;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    int i = 0;
                    // Prepare dir
                    String basename = file.getName().replaceFirst("(?i)\\.(htm|html)$", "") + "-img";
                    File imageDir = new File(file.getParent(), basename);
                    if (barSize > 0) {
                        imageDir.mkdirs();
                    }
//                    File logo = new File(imageDir, Util.LOGO_FILE.getName());
//                    Files.copy(Paths.get(Util.LOGO_FILE.toURI()), Paths.get(logo.toURI()), StandardCopyOption.REPLACE_EXISTING);

                    // Copy rest of images
                    for (Element image : imageElements) {
                        String fileName = image.attr("src").replaceFirst(".*/", "");
                        try {
                            URL fileUrl = Util.prepareImageFile(fileName, process, procType, bookId, width, height);
                            final String targetFileName = fileUrl.toString().replaceFirst(".*/", "");
                            Files.copy(Paths.get(fileUrl.toURI()), Paths.get((new File(imageDir, targetFileName)).toURI()),
                                    StandardCopyOption.REPLACE_EXISTING);
                            image.attr("src", basename + "/" + targetFileName);
                        } catch (MalformedURLException ex) {
                            logger.warn("Invalid URL", ex);
                        }
                        if (bar != null) {
                            bar.setValue(i++);
                        }
                    }
                    // Save hatml
                    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                        String html = document.html();
                        out.write(html.getBytes(StandardCharsets.UTF_8));
                    }

                } catch (IOException e) {
                    logger.error(null, e);
                    ex = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (dialog != null) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
                if (ex != null) {
                    // display error
                    JOptionPane.showMessageDialog(parent,
                            ex.getCause() == null ? ex.getLocalizedMessage() :
                            ex.getLocalizedMessage() + "\n" + ex.getCause().
                            getLocalizedMessage(), guiTexts.getString("SAVE_ERROR"), JOptionPane.ERROR_MESSAGE);
                }
            }

        };
        worker.execute();
        if (dialog != null) {
            dialog.setMinimumSize(new Dimension(200, 5));
            dialog.setLocationByPlatform(true);

            dialog.add(bar);
            dialog.setVisible(true);
        }
    }

    void exportAsEpub(final JDialog parent, final File file, final boolean process, final int procType, final int bookId,
            final int width, final int height) throws
            IOException {
        // https://www.ibm.com/developerworks/xml/tutorials/x-epubtut/
        // http://webdesign.about.com/od/epub/a/build-an-epub.htm
//        File file = new File(String.format("%d.epub", id));
        final Document document = buildHtml(false);
        final Elements imageElements = document.select("img");
        final int barSize = imageElements.size();

        final JDialog dialog = barSize > 2 ? new JDialog(parent, guiTexts.getString("SAVE_PROGRES_TITLE"), true) : null;
        final JProgressBar bar = barSize > 2 ? new JProgressBar(0, barSize) : null;
        SwingWorker<Void, Void> worker
                = new SwingWorker<Void, Void>() {
                    IOException ex = null;

                    @Override
                    protected Void doInBackground() throws Exception {
                        try {
                            int i = 0;
                            ArrayList<URL> imagesToInclude = new ArrayList<URL>();
                            // Prepare images
                            for (Element image : imageElements) {
                                String fileName = image.attr("src").replaceFirst(".*/", "");
                                try {
                                    URL fileUrl = Util.prepareImageFile(fileName, process, procType, bookId, width, height);
                                    imagesToInclude.add(fileUrl);
                                    image.attr("src", "images/" + fileUrl.toString().replaceFirst(".*/", ""));
                                } catch (MalformedURLException ex) {
                                    logger.warn("Invalid URL", ex);
                                }
                                if (bar != null) {
                                    bar.setValue(i++);
                                }
                            }

                            final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
                            out.setLevel(9);
                            Charset chSet = Charset.forName("UTF-8");

                            final byte[] mimetype = "application/epub+zip".getBytes(chSet);

                            ZipEntry zipEntry = new ZipEntry("mimetype");
                            zipEntry.setMethod(STORED);
                            zipEntry.setCompressedSize(mimetype.length);
                            zipEntry.setSize(mimetype.length);
                            CRC32 crc = new CRC32();
                            crc.update(mimetype);
                            zipEntry.setCrc(crc.getValue());
                            out.putNextEntry(zipEntry);
                            out.write(mimetype);
                            out.closeEntry();

                            out.putNextEntry(new ZipEntry("META-INF/container.xml"));
                            out.write(("<?xml version=\"1.0\"?>\n" +
                                    "<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n" +
                                    "  <rootfiles>\n" +
                                    "    <rootfile full-path=\"OEBPS/content.opf\"\n" +
                                    "     media-type=\"application/oebps-package+xml\" />\n" +
                                    "  </rootfiles>\n" +
                                    "</container>").getBytes(chSet));
                            out.closeEntry();

                            out.putNextEntry(new ZipEntry("OEBPS/content.opf"));
                            out.write(("<?xml version='1.0' encoding='utf-8'?>\n" +
                                    "<package xmlns=\"http://www.idpf.org/2007/opf\"\n" +
                                    "            xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
                                    "            unique-identifier=\"bookid\" version=\"2.0\">\n" +
                                    "  <metadata  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:opf=\"http://www.idpf.org/2007/opf\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n" +
                                    "    <dc:title>" + StringEscapeUtils.escapeXml10(title) + "</dc:title>\n" +
                                    "    <dc:creator>" + StringEscapeUtils.escapeXml10(getCorrectAuthor()) + "</dc:creator>\n" +
                                    "    <dc:publisher>Klient PBI</dc:publisher>\n" +
                                    "    <dc:identifier id=\"bookid\">urn:uuid:" + getUid() + "</dc:identifier>\n" +
                                    "    <dc:language>pol</dc:language>\n" +
                                    //                "    <meta name=\"cover\" content=\"cover-image\" />\n" +
                                    "  </metadata>\n" +
                                    "  <manifest>\n" +
                                    "    <item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>\n" +
                                    "    <item id=\"cover\" href=\"title.xhtml\" media-type=\"application/xhtml+xml\"/>\n" +
                                    "    <item id=\"toc\" href=\"toc.xhtml\" media-type=\"application/xhtml+xml\"/>\n" +
                                    "    <item id=\"content\" href=\"content.xhtml\" media-type=\"application/xhtml+xml\"/>\n" +
                                    //                "    <item id=\"cover-image\" href=\"images/cover.png\" media-type=\"image/png\"/>\n" +
                                    "    <item id=\"css\" href=\"stylesheet.css\" media-type=\"text/css\"/>\n" +
                                    "    <item id=\"logo\" href=\"images/pbi.gif\" media-type=\"image/gif\"/>\n" +
                                    "  </manifest>\n" +
                                    "  <spine toc=\"ncx\">\n" +
                                    "    <itemref idref=\"cover\" linear=\"no\"/>\n" +
                                    "    <itemref idref=\"toc\" linear=\"no\"/>\n" +
                                    "    <itemref idref=\"content\"/>\n" +
                                    "  </spine>\n" +
                                    "  <guide>\n" +
                                    "    <reference href=\"title.xhtml\" type=\"cover\" title=\"Okładka\"/>\n" +
                                    "    <reference href=\"toc.xhtml\" type=\"toc\" title=\"Spis treści\"/>\n" +
                                    "  </guide>\n" +
                                    "</package>").getBytes(chSet));
                            out.closeEntry();
                            out.putNextEntry(new ZipEntry("OEBPS/title.xhtml"));
                            out.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
                                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                                    "    <head>\n" +
                                    "        <title>" + StringEscapeUtils.escapeXml10(title) + "</title>\n" +
                                    "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n" +
                                    "        <link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\"/> \n" +
                                    "    </head>\n" +
                                    "    <body>\n" +
                                    "        <div class=\"logo\"><img src=\"images/pbi.gif\" alt=\"PBI\" /></div>\n" +
                                    "        <h1>" + StringEscapeUtils.escapeXml10(title) + "</h1>\n" +
                                    "        <h2>" + StringEscapeUtils.escapeXml10(getCorrectAuthor()) + "</h2>\n" +
                                    "    </body>\n" +
                                    "</html>").getBytes(chSet));
                            out.closeEntry();
                            out.putNextEntry(new ZipEntry("OEBPS/stylesheet.css"));
                            out.write(("body{\n" +
                                    "    font-family: Vardana, Helvetica, sans-serif;\n" +
                                    "    font-size: 10pt;\n" +
                                    "}\n" +
                                    ".logo {\n" +
                                    "text-align: center;\n" +
                                    "}\n" +
                                    "h1{\n" +
                                    "    font-size: 22pt;\n" +
                                    "    text-align: center;\n" +
                                    "    font-weight: bold;\n" +
                                    "}\n" +
                                    "h2{\n" +
                                    "    font-size: 18pt;\n" +
                                    "    text-align: center;\n" +
                                    "    font-weight: bold;\n" +
                                    "}\n" +
                                    "h3{\n" +
                                    "    font-size: 13pt;\n" +
                                    "    text-align: center;\n" +
                                    "    font-weight: bold;\n" +
                                    "}\n" +
                                    "h3{\n" +
                                    "    font-size: 12pt;\n" +
                                    "    text-align: center;\n" +
                                    "    font-style: italic;\n" +
                                    "}\n").getBytes(chSet));
                            if (depagination == Depagination.VERSE) {
                                out.write(("p { margin:0;text-indent:0;}").getBytes(chSet));
                            }
                            out.closeEntry();

                            out.putNextEntry(new ZipEntry("OEBPS/toc.xhtml"));
                            String html = buildTocHtml();
                            out.write(html.getBytes(chSet));
                            out.closeEntry();

                            out.putNextEntry(new ZipEntry("OEBPS/content.xhtml"));
                            html = document.html();
                            out.write(html.getBytes(chSet));
                            out.closeEntry();

                            out.putNextEntry(new ZipEntry("OEBPS/toc.ncx"));
                            out.write(("<?xml version='1.0' encoding='utf-8'?>\n" +
                                    "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\"\n" +
                                    "                 \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">\n" +
                                    "<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\">\n" +
                                    "  <head>\n" +
                                    "    <meta name=\"dtb:uid\" content=\"urn:uuid:" + getUid() + "\"/>\n" +
                                    "    <meta content=\"Klient PBI\" name=\"dtb:generator\"/>\n" +
                                    "    <meta name=\"dtb:depth\" content=\"1\"/>\n" +
                                    "    <meta name=\"dtb:totalPageCount\" content=\"0\"/>\n" +
                                    "    <meta name=\"dtb:maxPageNumber\" content=\"0\"/>\n" +
                                    "  </head>\n" +
                                    "  <docTitle>\n" +
                                    "    <text>" + StringEscapeUtils.escapeXml10(title) + "</text>\n" +
                                    "  </docTitle>\n" +
                                    "  <navMap>\n" +
                                    "    <navPoint id=\"navpoint-1\" playOrder=\"1\">\n" +
                                    "      <navLabel>\n" +
                                    "        <text>Strona tytułowa</text>\n" +
                                    "      </navLabel>\n" +
                                    "      <content src=\"title.xhtml\"/>\n" +
                                    "    </navPoint>\n" +
                                    "    <navPoint id=\"navpoint-2\" playOrder=\"2\">\n" +
                                    "      <navLabel>\n" +
                                    "        <text>Spis treści</text>\n" +
                                    "      </navLabel>\n" +
                                    "      <content src=\"toc.xhtml\"/>\n" +
                                    "    </navPoint>\n" +
                                    "    <navPoint id=\"navpoint-3\" playOrder=\"3\">\n" +
                                    "      <navLabel>\n" +
                                    "        <text>Treść</text>\n" +
                                    "      </navLabel>\n" +
                                    "      <content src=\"content.xhtml\"/>\n" +
                                    "    </navPoint>\n" +
                                    "  </navMap>\n</ncx>").getBytes(chSet));
                            out.closeEntry();

                            File logo = Util.LOGO_FILE;
                            zipEntry = new ZipEntry("OEBPS/images/pbi.gif");
                            zipEntry.setMethod(STORED);
                            final int length = (int) logo.length();
                            zipEntry.setCompressedSize(length);
                            zipEntry.setSize(length);
                            crc = new CRC32();
                            byte[] buff = new byte[length];
                            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(logo))) {
                                in.read(buff);
                                crc.update(buff);
                            }
                            zipEntry.setCrc(crc.getValue());
                            out.putNextEntry(zipEntry);
                            out.write(buff);
                            out.closeEntry();

                            for (URL image : imagesToInclude) {
                                try {
                                    String filename = image.toString().replaceFirst(".*/", "");
                                    File imageFile = new File(image.toURI());
                                    zipEntry = new ZipEntry("OEBPS/images/" + filename);
                                    zipEntry.setMethod(STORED);
                                    int fileSize = (int) imageFile.length();
                                    zipEntry.setCompressedSize(fileSize);
                                    zipEntry.setSize(fileSize);
                                    crc = new CRC32();
                                    buff = new byte[fileSize];
                                    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(imageFile))) {
                                        in.read(buff);
                                        crc.update(buff);
                                    }
                                    zipEntry.setCrc(crc.getValue());
                                    out.putNextEntry(zipEntry);
                                    out.write(buff);
                                    out.closeEntry();

                                } catch (URISyntaxException ex) {
                                    logger.error(null, ex);
                                }
                            }

                            out.close();
                        } catch (IOException e) {
                            logger.error(null, e);
                            ex = e;
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        if (dialog != null) {
                            dialog.setVisible(false);
                            dialog.dispose();
                        }
                        if (ex != null) {
                            // display error
                            JOptionPane.showMessageDialog(parent,
                                    ex.getCause() == null ? ex.getLocalizedMessage() :
                                    ex.getLocalizedMessage() + "\n" + ex.getCause().
                                    getLocalizedMessage(), guiTexts.getString("SAVE_ERROR"), JOptionPane.ERROR_MESSAGE);
                        }
                    }

                };
        worker.execute();
        if (dialog != null) {
            dialog.setMinimumSize(new Dimension(200, 5));
            dialog.setLocationByPlatform(true);

            dialog.add(bar);
            dialog.setVisible(true);
        }

    }

    public Document buildHtml(boolean preview) {
        Document doc = Jsoup.parse(Util.EMPTY_HTML);
        doc.outputSettings().charset("UTF-8").escapeMode(Entities.EscapeMode.xhtml);
        doc.title(title);
        doc.head().append("<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\"/>");
        int i = 1;
        Element body = doc.body();
        // Join paragraphs when the las one is terminated with letter or digit (for non-verse)
        for (String page : pages) {
            boolean done = false;
//            System.out.println("Using " + depagination);
            if (depagination != Depagination.VERSE && depagination != Depagination.NONE) {
                // Remove empty paragraphs at the end of file
                if (!body.children().isEmpty()) {
//                    System.out.println("Entering body");
                    Element last;
                    last = body.children().last();
                    if (isEmptyPar(last.text().trim()) && last.select("img").isEmpty()) {
//                        System.out.println("removing " + last);
                        last.remove();
                        last = body.children().last();
                    }
                    if (i > 2 && last != null && last.select("img").isEmpty() && !last.text().matches(".+[.:?!\"'](\\s| )*$")) { // Need to append nest paragraph to the last one
//                        System.out.println("Found: " + last.text().substring(Math.max(0, last.text().length() - 20)));
                        Elements elements = Jsoup.parseBodyFragment(page).body().children();
                        boolean first = true;
                        for (Element e : elements) {
                            if (first) {
                                if (!e.select("img").isEmpty()) {
                                    first = false;
                                    body.appendChild(e);
                                } else if (!isEmptyPar(e.text().trim())) {
                                    first = false;
                                    last.append(" " + e.html());
                                    last.appendElement("a").attr("id", "pg" + i);
                                }
                            } else {
                                body.appendChild(e);
                            }
                        }
                        done = true;
                    }
                }
            }

            if (!done) {
                String p;
                if (page.contains("</span>")) {
                    p = page.replaceFirst("</span>", String.format("</span><a id=\"pg%d\" />", i));
                } else {
                    p = page.replaceFirst(">\\s*\\w+[^<]*", String.format("$0<a id=\"pg%d\" />", i));
                }
                body.append(p);
            }
            i++;
            if (preview && i > 5) {
                break;
            }
        } // END of loop
        body.select("div").tagName("p");

        switch (depagination) {
            case VERSE:

                // verse (leave empty lines with non-breaking space)'
                for (Element par : body.select("p")) {
                    final String cnt = par.text().trim();
                    if ((cnt.isEmpty() || cnt.equals(" ")) && par.select("img").isEmpty()) {
                        par.html("&nbsp;");
                    } else {
                        String txt = par.html();
                        txt = txt.replaceFirst("^[ \\t\\r\\n ]+", ""); // Remove spaces at the beggining
                        txt = txt.replaceFirst("[ \\t\\r\\n ]+$", ""); // Remove spaces at the end
                        txt = txt.replaceAll("[ \\t\\r\\n ]+-[ \\t\\r\\n ]+", " — "); // Replace hyphens with emdash
                        txt = txt.replaceFirst("^-[ \\t\\r\\n ]+", "— "); // Replace hyphens with emdash
                        txt = txt.replaceFirst("[ \\t\\r\\n ]+-$", " —"); // Replace hyphens with emdash at line end
                        par.html(txt);
                    }
                }
                break;
            case DEHYPHENATE:
            // Remove empty paragraphs (STANDARD) and join paragraphs when privious one ends with hyphen
            case STANDARD:
                // Remove empty paragraphs, 
                for (Element par : body.select("p")) {
                    final String cnt = par.text().trim();
                    if (isEmptyPar(cnt) && par.select("img").isEmpty()) {
                        par.remove();
                    } else {
                        String txt = par.html();
                        txt = txt.replaceFirst("^[ \\t\\r\\n ]+", ""); // Remove spaces at the beggining
                        txt = txt.replaceFirst("[ \\t\\r\\n ]+$", ""); // Remove spaces at the end
                        txt = txt.replaceAll("[ \\t\\r\\n ]+-[ \\t\\r\\n ]+", " — "); // Replace hyphens with emdash
                        txt = txt.replaceFirst("^-[ \\t\\r\\n ]+", "— "); // Replace hyphens with emdash at line start
                        txt = txt.replaceFirst("[ \\t\\r\\n ]+-$", " —"); // Replace hyphens with emdash at line end
                        par.html(txt);
                    }
                }
                break;
            default: // NONE
            // ? Possible something to do

        }

        final String html = doc.toString();
        return doc;
    }

    public static boolean isEmptyPar(final String cnt) {
        return cnt.isEmpty() || cnt.equals("&nbsp;") || cnt.equals(" ");
    }

    private String getUid() {
        return "6c4b6569746e50-4942-" + Integer.toHexString(id);
    }

    public String getCorrectAuthor() {
//        System.out.println("getCorrectAuthor author = " + author);
//        System.out.println("getCorrectAuthor correctAuthor = " + correctAuthor);
        if (correctAuthor != null) {
            return correctAuthor;
        }
        if (author == null) {
            return "";
        }
        String[] s = author.split("\\s+");
        if (s.length < 2) {
            return author;
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < s.length; i++) {
            sb.append(s[i]).append(' ');
        }
        sb.append(s[0]);
        correctAuthor = sb.toString();
//        System.out.println("getCorrectAuthor calculated correctAuthor = " + correctAuthor);
        return correctAuthor;
    }

    /**
     * @param correctAuthor the correctAuthor to set
     */
    public void setCorrectAuthor(String correctAuthor) {
        this.correctAuthor = correctAuthor;
    }

    private String buildTocHtml() {
        Document doc = Jsoup.parse(Util.EMPTY_HTML);
        doc.outputSettings().charset("UTF-8").escapeMode(Entities.EscapeMode.xhtml);
        doc.title(title);
        doc.head().append("<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\"/>");
        Element body = doc.body();
        body.appendElement("h1").text("Spis treści");
        if (contents != null && contents.length > 0) {
            for (int i = 0; i < contents.length; i++) {
                body.appendElement("div").appendElement("a").attr("href", "content.xhtml#pg" + contentsPages[i]).text(contents[i]);
            }
        } else {
            body.appendElement("div").appendElement("a").attr("href", "content.xhtml").text(title);
        }
        final String html = doc.toString();
        return html;
    }

    /**
     * @return the depagination
     */
    public Depagination getDepagination() {
        return depagination;
    }

    /**
     * @param depagination the depagination to set
     */
    public void setDepagination(Depagination depagination) {
        this.depagination = depagination;
    }

    void guessDepagination() {
        int shorts = 0;
        int longs = 0;
        int images = 0;
        int hyphenated = 0;
        for (String p : pages) {
            if (p != null) {
                Document html = Jsoup.parseBodyFragment(p);
                if (!html.select("img").isEmpty()) {
                    images++;
                }
                final Elements divs = html.select("div");
                for (Element div : divs) {
                    int l = div.text().length();
                    if (l > 5 && l < 100) {
                        shorts++;
                    } else if (l >= 100) {
                        longs++;
                    }
                }
                if (!divs.isEmpty() && divs.last().text().matches("-\\s*$")) {
                    hyphenated++;
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("shorts: " + shorts);
            logger.debug("longs: " + longs);
            logger.debug("hyphenated: " + hyphenated);
            logger.debug("images: " + images);
            logger.debug("pages.length: " + pages.length);
        }

        if (shorts > longs * 2) {
            depagination = Depagination.VERSE;

        } else if (images > (pages.length / 2) && longs < images) {
            depagination = Depagination.NONE;
        } else if (hyphenated > (pages.length / 2)) {
            depagination = Depagination.DEHYPHENATE;
        } else {
            depagination = Depagination.STANDARD;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("depagination: " + depagination);
        }
    }
}
