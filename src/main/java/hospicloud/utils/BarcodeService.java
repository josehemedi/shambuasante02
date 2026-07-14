package hospicloud.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import java.awt.image.BufferedImage;

public final class BarcodeService {

    private BarcodeService() {
    }

    public static BufferedImage generateCode128Image(String text, int width, int height) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Le contenu du code-barres est vide.");
        }
        try {
            Code128Writer writer = new Code128Writer();
            BitMatrix matrix = writer.encode(text.trim(), BarcodeFormat.CODE_128, width, height);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la génération du code-barres.", e);
        }
    }
}
