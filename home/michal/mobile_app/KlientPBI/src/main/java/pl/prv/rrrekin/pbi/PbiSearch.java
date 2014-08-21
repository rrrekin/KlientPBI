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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * <p>
 * PbiSearch class.</p>
 *
 * @author Michał Rudewicz
 * @version $Id: $Id
 */
public class PbiSearch {

    private Log logger = LogFactory.getLog(this.getClass());

    /**
     * <p>
     * main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     * @throws java.io.IOException if any.
     */
    public static void main(String[] args) throws IOException {
        PbiSearch instance = new PbiSearch();
        List<PbiBookEntry> list = instance.searchByAuthor("mickiewicz", "");
        for (PbiBookEntry book : list) {
            System.out.println(book.toString());
        }
    }

    /**
     * <p>
     * searchByAuthor.</p>
     *
     * @param author a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @param title a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<PbiBookEntry> searchByAuthor(String author, String title) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Searching for title '%s' and author '%s'", title, author));
        }

        if (author == null) {
            author = "";
        }
        if (title == null) {
            title = "";
        }
        ArrayList<PbiBookEntry> result = new ArrayList<PbiBookEntry>();

        Configuration config = Configuration.getInstance();
        final Document page = Jsoup.connect(config.getServerBaseUrl().toString()).get();

        final String sValue = page.select("input[name=s]").first().attr("value");

        if (logger.isDebugEnabled()) {
            logger.debug("Getting first page");
        }
        Document page2 = Jsoup.connect(String.format("%ssite.php?aut=%s&tyt=%s&wyd=&s=%s", config.getServerBaseUrl(),
                URLEncoder.encode(author, "UTF-8"), URLEncoder.encode(title, "UTF-8"), URLEncoder.encode(sValue, "UTF-8"))).get();

        Elements links = page2.select("a[href~=^javascript:showPublication]");

        int pageNo = 1;
        boolean hasNext;
        do {
            hasNext = false;
            String nextLink = "javascript:gotoPage(" + (++pageNo) + ")";
            for (Element l : links) {
                final String href = l.attr("href");
                String bookId = StringUtils.substringBetween(href, "(", ",");
                String bookTitle = l.text().trim();
                String bookAuthor = l.nextElementSibling().nextElementSibling().text().trim();
                try {
                    result.add(new PbiBookEntry(bookTitle, bookAuthor, bookId));
                } catch (NumberFormatException ex) {
                    logger.debug("Invalid ID", ex);
                }
            }
            hasNext = !page2.select("a[href=" + nextLink + "]").isEmpty();
            if (hasNext) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Getting page " + pageNo);
                }
                page2 = Jsoup.connect(String.format("%ssite.php?aut=%s&tyt=%s&wyd=&pg=%d&s=%s",
                        config.getServerBaseUrl(), URLEncoder.encode(author, "UTF-8"), URLEncoder.encode(title, "UTF-8"),
                        pageNo, URLEncoder.encode(sValue, "UTF-8"))).get();
                links = page2.select("a[href~=^javascript:showPublication]");
            }
        } while (hasNext);

        logger.debug("Search results: " + result);
        return result;
    }

}
