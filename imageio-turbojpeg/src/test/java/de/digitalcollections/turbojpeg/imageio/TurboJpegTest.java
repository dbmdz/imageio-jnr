package de.digitalcollections.turbojpeg.imageio;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.digitalcollections.turbojpeg.TurboJpeg;
import de.digitalcollections.turbojpeg.TurboJpegException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TurboJpegTest {

    private TurboJpeg turboJpeg;

    @BeforeEach
    void setUp() {
        turboJpeg = new TurboJpeg();
    }

    @Test
    void encodeBufferedImageWithDataBufferByte() throws TurboJpegException {
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR);

        assertTrue(bufferedImage.getRaster()
                                .getDataBuffer() instanceof DataBufferByte);

        turboJpeg.encode(bufferedImage.getRaster(), 100);
    }

    @Test
    void encodeBufferedImageWithDataBufferInt() throws TurboJpegException {
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        assertTrue(bufferedImage.getRaster()
                                .getDataBuffer() instanceof DataBufferInt);

        turboJpeg.encode(bufferedImage.getRaster(), 100);
    }
}
