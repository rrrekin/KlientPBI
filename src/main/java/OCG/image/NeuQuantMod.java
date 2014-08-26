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
package OCG.image;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * @author Michał Rudewicz
 */
public class NeuQuantMod extends NeuQuant {

    private final BufferedImage image;
    private BufferedImage indexImage = null;

    private final Log logger = LogFactory.getLog(this.getClass());

    public NeuQuantMod(BufferedImage im) throws IOException {
        super(im, im.getWidth(), im.getHeight());
        image = im;
    }

    public NeuQuantMod(BufferedImage im, int numberOfColors) throws IOException {
        super(im, im.getWidth(), im.getHeight());
        if(numberOfColors>=256|| numberOfColors<4) throw new IllegalArgumentException("numberOfColors has to be < 256 nad >3");
        image = im;
        netsize = numberOfColors;
        cutnetsize = netsize - specials;
        maxnetpos = netsize - 1;
        initrad = netsize / 8;
    }

    public static void main(String[] args) {
        System.out.println((byte) 255);
    }

    /**
     * @return the image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * @return the indexImage
     */
    public BufferedImage getIndexImage() {
        if (indexImage != null) {
            return indexImage;
        }

        final int x = image.getWidth();
        final int y = image.getHeight();
        init();
        int argb;

        byte[] r = new byte[netsize];
        byte[] g = new byte[netsize];
        byte[] b = new byte[netsize];
        for (int i = 0; i < netsize; i++) {
            b[i] = (byte) colormap[i][0];
            g[i] = (byte) colormap[i][1];
            r[i] = (byte) colormap[i][2];
        }

        int bits;
        if (netsize <= 4) {
            bits = 2;
        }else if(netsize <= 8) {
            bits = 3;
        }else if(netsize <= 16) {
            bits = 4;
        }else if(netsize <= 32) {
            bits = 5;
        }else if(netsize <= 64) {
            bits = 6;
        }else if(netsize <= 128) {
            bits = 7;
        }else  {
            bits = 8;
        }
        IndexColorModel model = new IndexColorModel(8, netsize, r, g, b);
        indexImage = new BufferedImage(x, y, BufferedImage.TYPE_BYTE_INDEXED, model);
        for (int yIndex = 0; yIndex < y; yIndex++) {
            for (int xIndex = 0; xIndex < x; xIndex++) {
                argb = image.getRGB(xIndex, yIndex);
                indexImage.setRGB(xIndex, yIndex, convert(argb));
            }
        }
        return indexImage;
    }

}
