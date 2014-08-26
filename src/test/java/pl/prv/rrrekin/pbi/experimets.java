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
import java.io.FileFilter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
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

        int days = 2;
        final FileTime oldest = FileTime.fromMillis(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * days);
        File[] cacheFiles = Util.CACHE_DIR.listFiles(new FileFilter() {

            @Override
            public boolean accept(File f) {
                boolean older = false;
//
                try {
                    FileTime modTime = Files.getLastModifiedTime(f.toPath());
                    if (modTime.compareTo(oldest) < 0) {
                        older = true;
                    }
                } catch (IOException ex) {
                }
                boolean isFile = f.isFile();
                final boolean isDigitOnly = f.getName().toString().matches("\\d*");
                return isDigitOnly && older && isFile;
            }
        });
        
        System.out.println(Arrays.toString(cacheFiles));

    }
}
