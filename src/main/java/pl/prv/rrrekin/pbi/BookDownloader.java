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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.swing.JDialog;
import javax.swing.text.html.HTML;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;

/**
 *
 * @author Michał Rudewicz
 */
public class BookDownloader {

    private int id;

    private boolean usingCache = true;
    private PbiBook book;

    private boolean indeterminated = false;
    private boolean finished = false;
    private int pageCount = 0;
    private int pagesGot = 0;
    private String author = "";
    private boolean cancel = false;
    private boolean hasImages = false;

    private final Log logger = LogFactory.getLog(this.getClass());
    private final ConcurrentLinkedQueue<Integer> downloadQueue = new ConcurrentLinkedQueue<Integer>();
    private Configuration config;
    private final transient PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    private static JAXBContext jaxbContext;
    private static Marshaller marshaller;
    private static Unmarshaller unmarshaller;
    private static final ResourceBundle guiTexts = ResourceBundle.getBundle("pl/prv/rrrekin/pbi/gui");
    public static final String PROP_USINGCACHE = "usingCache";
    public static final String PROP_INDETERMINATED = "indeterminated";
    public static final String PROP_FINISHED = "finished";
    public static final String PROP_PAGECOUNT = "pageCount";
    public static final String PROP_PAGESGOT = "pagesGot";
    public static final String PROP_AUTHOR = "author";
    public static final String PROP_TITLE = "title";
    public static final String PROP_DEPAGINATION = "depagination";
    public static final String PROP_HASIMAGES = "hasImages";

    static {
        try {
            jaxbContext = JAXBContext.newInstance(PbiBook.class);
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException ex) {
            ex.printStackTrace();
//            System.exit(2);
        }
    }

    public BookDownloader() {
        id = 1800;
        Configuration c = null;
        try {
            c = Configuration.getInstance();
        } catch (IOException ex) {
            logger.error(ex);
            System.exit(2);
        }
        config = c;
    }

    public BookDownloader(int id) {
        this.id = id;
        Configuration c = null;
        try {
            c = Configuration.getInstance();
        } catch (IOException ex) {
            logger.error(ex);
            System.exit(2);
        }
        config = c;
    }

    public static void main(String[] args) throws IOException {
        BookDownloader downloader = new BookDownloader(1800);
//        BookDownloader downloader = new BookDownloader(1345);

//        BookDownloader downloader = new BookDownloader(52097);
        downloader.download(true);

//        downloader.exportAsHtml();
//        downloader.book.guessDepagination();
//        downloader.exportAsEpub(new File(downloader.getTitle() + ".epub"));
    }

    public static boolean hasCache(int id) {
        File file = Util.cacheFile(id);
        return file.exists() && file.isFile() && file.canRead();
    }

    public void exportAsHtml(JDialog parent, File file, boolean process, int procType, int bookId, int width, int height) throws
            IOException {
        if (book != null) {
            book.exportAsHtml(parent, file, process, procType, bookId, width, height);
        }
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    public void download(boolean useCache) throws IOException {
        try {
            config = Configuration.getInstance();
        } catch (IOException ex) {
            logger.error(ex);
            System.exit(2);
        }
        Depagination oldDepagination = Depagination.STANDARD;
        setIndeterminated(true);
        boolean loaded = false;
        if (useCache && hasCache(id)) {
            try {
                // Load book from cache
                loadBook();
                if (book != null) {
//                    oldDepagination = book.getDepagination();
                    book.guessDepagination();
                    for (String p : book.getPages()) {
                        if (p.contains("<img")) {
                            setHasImages(true);
                            break;
                        }
                    }
                }
                propertyChangeSupport.firePropertyChange(PROP_AUTHOR, "", book.getCorrectAuthor());
                propertyChangeSupport.firePropertyChange(PROP_TITLE, "", book.getCorrectAuthor());
                propertyChangeSupport.firePropertyChange(PROP_DEPAGINATION, oldDepagination, book.getDepagination());
                loaded = true;
            } catch (JAXBException ex) {
                logger.error("Cannot read cache file");
//                throw new IOException(guiTexts.getString("CANT_READ_CACHE_FILE"), ex);
            }
        }
        if (!loaded) {
            setPageCount(0);
            setPagesGot(0);
            if (book != null) {
                setAuthor("");
            }

            setFinished(false);

            File file = Util.cacheFile(id);
            file.delete();

            book = new PbiBook();
            book.setId(id);

            Document page = Jsoup.connect(config.getServerBaseUrl().toString()).get();

            final String sValue = page.select("input[name=s]").first().attr("value");
            if (isCancel()) {
                return;
            }

            page = Jsoup.connect(config.getServerBaseUrl() + "left_1.php?p=" + id + "&s=1&w=").get();
            if (isCancel()) {
                return;
            }
            String title = page.select("body > table > tbody > tr:nth-child(3) > td.t11-red").text().trim();
            logger.debug("title = " + title);
            String authorP = page.select("body > table > tbody > tr:nth-child(6) > td:nth-child(2)").text().trim();
            logger.debug("author = " + authorP);
            String pagesStr = page.select(
                    "#forma > table > tbody > tr:nth-child(5) > td > table > tbody > tr > td:nth-child(2)").
                    text().trim();
            logger.debug("pages = " + pagesStr);
            int pages;
            try {
                pages = Integer.parseInt(pagesStr);
            } catch (NumberFormatException ex) {
                throw new IOException(ex.getLocalizedMessage());
            }

            book.setAuthor(authorP);
            setAuthor(book.getCorrectAuthor());
            setTitle(title);
            book.setPages(new String[pages]);

            setPageCount(pages);
            setIndeterminated(false);

            // Content /left_2.php?p=1800&s=1&w=?p=1800&s=1&w=
            if (isCancel()) {
                return;
            }
            page = Jsoup.connect(config.getServerBaseUrl() + "left_2.php?p=" + id + "&s=1&w=").get();
//        System.out.println(page.getWebResponse().getContentAsString());
            if (isCancel()) {
                return;
            }

            Elements links = page.select("a[href~=^javascript:page\\(]");

            List<String> content = new ArrayList<>();
            List<Integer> contentPages = new ArrayList<>();

            for (Element l : links) {
                final String href = l.attr("href");
//                System.out.println(href);
                String pageNoStr = StringUtils.substringBetween(href, "('", "')");
                String chapter = l.text().trim();
                int pageNo;
                try {
                    pageNo = Integer.parseInt(pageNoStr);
                } catch (Exception ex) {
                    pageNo = -1;
                }
                if (pageNo >= 0) {
                    content.add(chapter);
                    contentPages.add(pageNo);
                }
//                System.out.println(chapter + " " + pageNo);

            }
            if (!content.isEmpty()) {
                book.setContents(content.toArray(new String[0]));
                book.setContentsPages(contentPages.toArray(new Integer[0]));
            }

//            saveBook();
        }

        // Chreate directory for potentioal images
        File images = new File(Util.CACHE_IMAGES, Integer.toString(id));
        images.mkdir();

        if (!Util.LOGO_FILE.isFile()) {
            Util.LOGO_FILE.renameTo(new File(Util.CACHE_IMAGES, "" + System.currentTimeMillis() + ".gif"));
        }

        if (!Util.LOGO_FILE.exists()) {
            // Download logo
            URL website = new URL("http://www.pbi.edu.pl/img/header_01.gif");
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(Util.LOGO_FILE);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            logger.info("Logo fetched");
        }

        if (isCancel()) {
            return;
        }
        // From this point we have book initialized or loaded from cache
        // Prepare list of pages to download
        final String[] pages = book.getPages();
        downloadQueue.clear();

        logger.debug("Preparing list of pages to download");
        for (int i = 0; i < pages.length; i++) {
            if (pages[i] == null) {
                downloadQueue.offer(i + 1);
            }
        }
        logger.debug(downloadQueue);
        setPagesGot(getPageCount() - downloadQueue.size());

        if (downloadQueue.isEmpty()) {
            setFinished(true);
            logger.debug("Book already downloaded. Finishing.");
            return;
        } else {
            int threadNum = Math.min(downloadQueue.size(), config.getThreads());
            // Launch download threads
            ExecutorService pool = Executors.newFixedThreadPool(threadNum, new DownloadThreadFactory());
            for (int i = 0; i < threadNum; i++) {
                pool.submit(new DownloadThread());
            }
            // Wait for termination
            logger.debug("Waiting for termination");

            pool.shutdown();
            try {
                pool.awaitTermination(1, TimeUnit.DAYS);
            } catch (InterruptedException ex) {
                logger.warn(ex);
            }
            book.guessDepagination();
            propertyChangeSupport.firePropertyChange(PROP_DEPAGINATION, oldDepagination, book.getDepagination().toString());

            logger.debug("Finished");
            // Save final book
            saveBook();
        }
    }

    private void saveBook() throws IOException {
//        if (!isFinished()) {
//            throw new IOException(guiTexts.getString("NOT_FULLY_DOWNLOADED"));
//        }

        File file = Util.cacheFile(id);
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdir();
        }
        if (!dir.isDirectory() || !dir.canWrite()) {
            throw new IOException(guiTexts.getString("CANT_WRITE_CACHE_DIR"));
        }
        try {
            marshaller.marshal(book, file);
        } catch (JAXBException ex) {
            throw new IOException(guiTexts.getString("CANT_WRITE_TO_FILE"), ex);
        }
    }

    private void loadBook() throws JAXBException {
        File file = Util.cacheFile(id);
        book = (PbiBook) unmarshaller.unmarshal(file);
        setIndeterminated(false);
        if (book.getPages() == null) {
            throw new JAXBException("Book without pages");
        }
        setPageCount(book.getPages().length);
        int downloaded = 0;
        for (String p : book.getPages()) {
            if (p != null) {
                downloaded++;
            }
        }
        setPagesGot(downloaded);
        setFinished(getPageCount() == getPagesGot());
        try {
            Files.setLastModifiedTime(file.toPath(), FileTime.fromMillis(System.currentTimeMillis()));
        } catch (IOException ex) {
        }
    }

    public void exportAsEpub(JDialog parent, File file, boolean process, int procType, int bookId, int width, int height) throws
            IOException {
        if (book != null) {
            book.exportAsEpub(parent, file, process, procType, bookId, width, height);
        }
    }

    /**
     * @return the usingCache
     */
    public boolean isUsingCache() {
        return usingCache;
    }

    /**
     * @param usingCache the usingCache to set
     */
    public void setUsingCache(boolean usingCache) {
        boolean oldUsingCache = this.usingCache;
        this.usingCache = usingCache;
        propertyChangeSupport.firePropertyChange(PROP_USINGCACHE, oldUsingCache, usingCache);
    }

    /**
     * @return the indeterminated
     */
    public boolean isIndeterminated() {
        return indeterminated;
    }

    /**
     * @param indeterminated the indeterminated to set
     */
    public void setIndeterminated(boolean indeterminated) {
        boolean oldIndeterminated = this.indeterminated;
        this.indeterminated = indeterminated;
        propertyChangeSupport.firePropertyChange(PROP_INDETERMINATED, oldIndeterminated, indeterminated);
    }

    /**
     * @return the finished
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @param finished the finished to set
     */
    public void setFinished(boolean finished) {
        boolean oldFinished = this.finished;
        this.finished = finished;
        propertyChangeSupport.firePropertyChange(PROP_FINISHED, oldFinished, finished);
    }

    synchronized public void incrementPagesGot() {
        setPagesGot(pagesGot + 1);
        if (pagesGot >= pageCount) {
            setFinished(true);
        }
    }

    /**
     * @return the pageCount
     */
    synchronized public int getPageCount() {
        return pageCount;
    }

    /**
     * @param pageCount the pageCount to set
     */
    synchronized public void setPageCount(int pageCount) {
        int oldPageCount = this.pageCount;
        this.pageCount = pageCount;
        propertyChangeSupport.firePropertyChange(PROP_PAGECOUNT, oldPageCount, pageCount);
    }

    /**
     * @return the pagesGot
     */
    public int getPagesGot() {
        return pagesGot;
    }

    /**
     * @param pagesGot the pagesGot to set
     */
    public void setPagesGot(int pagesGot) {
        int oldPagesGot = this.pagesGot;
        this.pagesGot = pagesGot;
        propertyChangeSupport.firePropertyChange(PROP_PAGESGOT, oldPagesGot, pagesGot);
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        if (book != null && book.getPages() != null) {
            String correctAuthor = book.getCorrectAuthor();
//            System.out.println("Getting c author = " + correctAuthor);
            return correctAuthor;
        }
//        System.out.println("Getting author = " + author);
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {//
//        System.out.println("Setting author = " + author);
        java.lang.String oldAuthor = getAuthor();
        book.setCorrectAuthor(author);
        this.author = author;
        propertyChangeSupport.firePropertyChange(PROP_AUTHOR, oldAuthor, author);
        try {
            saveBook();
        } catch (IOException ex) {
            logger.warn(ex);
        }
    }

    public String getTitle() {
        if (book != null) {
            return book.getTitle();
        }
        return "";
    }

    public void setTitle(String title) {
        java.lang.String oldTitle = book.getAuthor();
        book.setTitle(title);
        propertyChangeSupport.firePropertyChange(PROP_TITLE, oldTitle, title);
    }

    public Depagination getDepagination() {
        if (book != null) {
            return book.getDepagination();
        }
        return Depagination.STANDARD;
    }

    public void setDepagination(Depagination val) {
        Depagination oldDep = book == null ? Depagination.STANDARD : book.getDepagination();
        Depagination d = val;
        if (book != null) {
            book.setDepagination(d);
        }
        propertyChangeSupport.firePropertyChange(PROP_DEPAGINATION, oldDep, val);
    }

    /**
     * @return the book
     */
    public PbiBook getBook() {
        return book;
    }

    /**
     * @return the cancel
     */
    synchronized public boolean isCancel() {
        return cancel;
    }

    /**
     * @param cancel the cancel to set
     */
    synchronized public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * @return the hasImages
     */
    public boolean isHasImages() {
        return hasImages;
    }

    /**
     * @param hasImages the hasImages to set
     */
    public synchronized void setHasImages(boolean hasImages) {
        boolean oldHasImages = this.hasImages;
        this.hasImages = hasImages;
        propertyChangeSupport.firePropertyChange(PROP_HASIMAGES, oldHasImages, hasImages);
    }

    private class DownloadThreadFactory implements ThreadFactory {

        private int i = 0;

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, String.format("dw %02d", i++));
        }

    }

    private class DownloadThread implements Runnable {

        Log logger = LogFactory.getLog(this.getClass());
        final String[] pages = getBook().getPages();

        @Override
        public void run() {
            logger.debug("Starting download thread");
            Integer pageNo;
            while ((pageNo = downloadQueue.poll()) != null) {
                if (isCancel()) {
                    return;
                }
                try {
                    logger.debug("Downloading page " + pageNo);
                    URL url = new URL(config.getServerBaseUrl() + "content.php?p=" + id + "&s=" + pageNo + "&w=");
                    URLConnection con = url.openConnection();
                    InputStream in = con.getInputStream();
                    String encoding = con.getContentEncoding();
                    encoding = encoding == null ? "UTF-8" : encoding;
                    String body = IOUtils.toString(in, encoding);
                    in.close();
                    Document page = Jsoup.parse(body.replaceAll("<<", "«").replaceAll(">>", "»"));

//                    Document page
//                            = Jsoup.connect(config.getServerBaseUrl() + "content.php?p=" + id + "&s=" + pageNo + "&w=").get();
                    page.outputSettings().charset("UTF-8").escapeMode(Entities.EscapeMode.xhtml);
                    logger.debug("Got page " + pageNo);
                    Element content = page.select("body > table > tbody > tr:nth-child(5) > td").first();
                    content.select("img[src=config/images/blank.gif").remove();
                    content.select("style").remove();
                    Element br = content.child(0);
                    if (br.tag().equals(Tag.valueOf("br"))) {
                        br.remove();
                    }
//                    Elements blanks = content.select("img[src=config/images/blank.gif");
//                    if (blanks.first().nextElementSibling().tag() == Tag.valueOf("br")) {
//                        blanks.first().nextElementSibling().remove();
//                    }
//                    if (blanks.first().nextElementSibling().text().trim().isEmpty()) {
//                        blanks.first().nextElementSibling().remove();
//                    }
//                    
//                    blanks.remove();

                    content.select("span[style=font-size: 100%]").removeAttr("style");
                    content.select("span[class=txt]").removeAttr("class");
                    content.select("div[style=text-align:left;").removeAttr("style");

                    // Move this to depagination code
//                    for (Element div : content.select("div")) {
//                        final String cnt = div.text().trim();
//                        if (cnt.isEmpty() || cnt.equals("&nbsp;")||cnt.equals(" ")) {
//                            div.remove();
//                        } else {
//                            div.tagName("p");
//                        }
//                    }
                    // Remove spans without attributes
                    for (Element span : content.select("span")) {
                        if (span.attributes().asList().isEmpty()) {
                            span.before(span.html());
                            span.remove();
                        }
                    }

                    // Remove span, i, b with blank only characters
                    for (Element e : content.select("span,i,b")) {
                        String txt = e.text();
                        if (txt.matches("^(\\s| )*")) {
                            e.prepend(" ");
                            e.remove();
                        }
                    }

                    // Replace forms with paragraphs
                    content.select("form").tagName("p").removeAttr("id");

                    for (Element img : content.select("img")) {
//                        System.out.println("Page "+pageNo+": "+img);
                        BookDownloader.this.setHasImages(true);
                        String src = img.attr("src");
                        img.removeAttr("width");
                        img.removeAttr("height");
                        img.attr("id", "p" + pageNo);
                        img.attr("alt", "Strona " + pageNo);
                        String localPath = "images/" + id + "/" + (src.replaceFirst(".*/", ""));
                        img.attr("src", localPath);
                        File localFile = new File(new File(Util.CACHE_IMAGES, Integer.toString(id)), src.replaceFirst(".*/", ""));
                        URL srcUrl = new URL(config.getServerBaseUrl() + src);
                        ReadableByteChannel rbc = Channels.newChannel(srcUrl.openStream());
                        FileOutputStream fos = new FileOutputStream(localFile);
                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        if (logger.isDebugEnabled()) {
                            logger.debug(String.format("Image on page %d fetched", pageNo));
                        }
                    }

                    String text = content.html();

                    logger.debug(String.format("Page %d: %s", pageNo, text));

                    synchronized (pages) {
                        pages[pageNo - 1] = text;
                    }
                    incrementPagesGot();
                } catch (Exception ex) {
                    logger.error(ex);
                }
                logger.debug("Page " + pageNo + " fetching finished");
            }
            logger.debug("Finishing download thread");
        }

    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
//        System.out.println("listener = " + listener);
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
//        System.out.println("listener = " + listener);
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

}
