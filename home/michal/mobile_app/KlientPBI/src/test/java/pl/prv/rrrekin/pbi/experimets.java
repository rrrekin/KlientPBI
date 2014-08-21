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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

/**
 *
 * @author Michał Rudewicz
 */
public class experimets {

    public static void main(String[] args) throws IOException {

        String txt
                = "  <p> Przedmowa do książki jest co sień do domu, z tą jednak różnicą, iż domowi być bez sieni trudno, a książka się bez przedmowy obejdzie. Starożytni autorowie nie znali przedmów - ich wynalazek, tak jak i innych wielu rzeczy mniej potrzebnych, jest dziełem późniejszych wieków. </p> \n" +
                "  <p> Wielorakie bywają przyczyny pobudzające autorów do kładzenia przedmów na czele pisma swojego. Jedni, fałszywej modestii pełni, zwierzają się czytelnikowi (choć ich o to nie prosił), jako pewni wielkiej dystynkcji i nie mniejszej doskonałości przyjaciele przymusili ich do wydania na świat tego, co dla własnej satysfakcji napisawszy chcieli mieć w ukryciu. Drudzy skarżą się na zdradę, że mimo ich wolą manuskrypt ich był porwany. Trzeci, czyniąc zadosyć rozkazom starszych, dając księgę do druku uczynili ofiarę heroiczną posłuszeństwa; i jakby to bardzo obchodziło ziewającego czytelnika, te i podobne czynią mu konfidencje. </p> \n" +
                "  <p> Nieznacznie przedmowy weszły w modę; teraz jednak ta moda najbardziej panuje, gdy kunszt autorski został rzemiosłem. Bardzo wielu, a podobno większa połowa współbraci moich, autorów, żyje z druku; tak teraz robiemy książki jak zegarki, a że ich dobroć od grubości najbardziej zawisła, staramy się ile możności rozciągać, przedłużać i rozprzestrzeniać dzieła nasze. Jak więc przedmowy do literackiego handlu służą, łatwo baczny czytelnik domyślić się może. </p> \n";

        Document doc = Jsoup.parseBodyFragment(txt);
        for (Element e : doc.select("P")) {
            txt = e.html();
            System.out.println("txt1 = '" + txt + "'");
            txt = txt.replaceFirst("^[ \\t\\r\\n ]+", ""); // Remove spaces at the beggining
            System.out.println("txt2 = '" + txt + "'");
            txt = txt.replaceFirst("[ \\t\\r\\n ]+$", ""); // Remove spaces at the end
            System.out.println("txt3 = '" + txt + "'");
            txt = txt.replaceAll("[ \\t\\r\\n ]+-[ \\t\\r\\n ]+", " &emdash; "); // Replace hyphens with emdash
            System.out.println("txt4 = '" + txt + "'");
            txt = txt.replaceFirst("^-[ \\t\\r\\n ]+", "&emdash; "); // Replace hyphens with emdash
            System.out.println("txt5 = '" + txt + "'");
            e.html(txt);
        }

        System.out.println("doc = " + doc.html());
    }
}
