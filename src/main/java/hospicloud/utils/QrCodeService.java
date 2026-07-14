package hospicloud.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class QrCodeService {

	public static BufferedImage generateBufferedImage(String text, int size) {
	    if (text == null || text.trim().isEmpty()) {
	        throw new IllegalArgumentException("Le contenu du QR Code est vide.");
	    }
	    try {
	        QRCodeWriter writer = new QRCodeWriter();
	        BitMatrix matrix = writer.encode(text.trim(), BarcodeFormat.QR_CODE, size, size);
	        return MatrixToImageWriter.toBufferedImage(matrix);
	    } catch (Exception e) {
	        throw new RuntimeException("Erreur lors de la génération du QR Code", e);
	    }
	}

	public static byte[] generateQrCodeBytes(String text) {
	    System.out.println("Texte QR = " + text);

	    if (text == null || text.trim().isEmpty()) {
	        throw new IllegalArgumentException("Le contenu du QR Code est vide.");
	    }

	    try {
	        QRCodeWriter writer = new QRCodeWriter();
	        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, 250, 250);

	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        MatrixToImageWriter.writeToStream(matrix, "PNG", out);

	        byte[] result = out.toByteArray();

	        System.out.println("Taille QR = " + result.length);

	        return result;

	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new RuntimeException("Erreur lors de la génération du flux QR Code", e);
	    }
	}
}
