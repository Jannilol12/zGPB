package network;

import log.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class NetworkUtil {

    private NetworkUtil() {

    }

    public static byte[] getBase64FromURL(String url) {
        try {
            URL imageUrl = new URL(url);
            URLConnection connection = imageUrl.openConnection();
            InputStream imageStream = connection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[2048];
            int read;
            while ((read = imageStream.read(buffer, 0, buffer.length)) != -1) {
                byteArrayOutputStream.write(buffer, 0, read);
            }
            byteArrayOutputStream.flush();

            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            Logger.logException(e);
        }
        return null;
    }

}
