package network;

import log.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NetworkUtil {

    private final static Map<String, String> GENERAL_REQUEST_HEADERS = Map.of(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:87.0) Gecko/20100101 Firefox/87.0",
            "DNT", "1"
    );


    private NetworkUtil() {
    }

    public static String sendGetRequest(String url, Map<String, String> headers) {
        try {
            URL requestURL = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) requestURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);

            GENERAL_REQUEST_HEADERS.forEach(connection::setRequestProperty);
            headers.forEach(connection::setRequestProperty);

            InputStream responseStream;

            boolean unsuccessfulRequest = false;

            if (connection.getResponseCode() != 200) {
                responseStream = connection.getErrorStream();
                Logger.logDebugMessage("GET request failed, " + connection.getResponseCode() + " - " + connection.getResponseMessage() +
                                       "collection error information");
                unsuccessfulRequest = true;
            } else {
                responseStream = connection.getInputStream();
            }

            BufferedReader rd = new BufferedReader(new InputStreamReader(responseStream));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append(System.lineSeparator());
            }
            rd.close();

            if (unsuccessfulRequest) {
                Logger.logDebugMessage(response.toString());
                return null;
            }

            return response.toString();
        } catch (Exception e) {
            Logger.logException(e);
        }

        return null;
    }

    public static String sendPostRequest(String url, Map<String, String> headers, String payload) {
        try {
            URL requestURL = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) requestURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setInstanceFollowRedirects(true);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            GENERAL_REQUEST_HEADERS.forEach(connection::setRequestProperty);
            headers.forEach(connection::setRequestProperty);

            connection.connect();

            if (payload != null) {
                OutputStream os = connection.getOutputStream();
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.US_ASCII));
                pw.write(payload);
                pw.close();
            }

            InputStream responseStream;
            boolean unsuccessfulRequest = false;

            StringBuilder responseBuilder = new StringBuilder();

            if (connection.getResponseCode() != 200) {
                responseStream = connection.getErrorStream();
                Logger.logDebugMessage("POST request failed, " + connection.getResponseCode() + " - " + connection.getResponseMessage() +
                                       "collection error information");
                unsuccessfulRequest = true;
            } else {
                responseStream = connection.getInputStream();
            }

            BufferedReader responseReader = new BufferedReader(new InputStreamReader(responseStream));
            String currentLine;
            while ((currentLine = responseReader.readLine()) != null) {
                responseBuilder.append(currentLine);
                responseBuilder.append(System.lineSeparator());
            }
            responseReader.close();

            if (unsuccessfulRequest) {
                Logger.logDebugMessage(responseBuilder.toString());
                return null;
            }

            return responseBuilder.toString();
        } catch (Exception e) {
            Logger.logException(e);
        }

        return null;
    }

    public static byte[] getBytesFromURL(String url) {
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

    // this is by no means a exhaustive list, just enough to work
    public static String transformToHTMLString(String in) {
        return in.replaceAll("\\+", "%2B").replaceAll("=", "%3D").
                replaceAll(":", "%3A").replaceAll(" ", "%20");
    }

    public static String transformFromHTMLString(String in) {
        return in.replace("&amp;", "&");
    }

}
