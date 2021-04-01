package network;

import discord.DictionaryEntry;
import log.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

// TODO: 01/04/2021 Move my campus related code to a separate handler
public class NetworkUtil {

    private final static Map<String, String> GENERAL_REQUEST_HEADERS = Map.of(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:87.0) Gecko/20100101 Firefox/87.0",
            "DNT", "1"
    );

    private final static String IDM_USERNAME = System.getenv("jadb_idm_username");
    private final static String IDM_PASSWORD = System.getenv("jadb_idm_password");
    private final static String AUTH_STATE_URL = "https://www.campus.uni-erlangen.de/Shibboleth.sso/Login";
    private final static String OAUTH_URL = "https://www.sso.uni-erlangen.de/simplesaml/module.php/core/loginuserpass.php?";
    private final static String SAML_URL = "https://www.campus.uni-erlangen.de/Shibboleth.sso/SAML2/POST";


    // Overview page we use to determine GRADE_PAGE_URL
    private final static String EXAM_PAGE_URL = "https://www.campus.uni-erlangen.de/qisserver/rds?state=template&template=pruefungen";
    // Exam data can be retrieved here
    private final static String EXAM_VIEW_URL = "https://www.campus.uni-erlangen.de/qisserver/rds?state=notenspiegelStudent&next=list.vm&nextdir=qispos/notenspiegel/student&createInfos=Y&struct=auswahlBaum&nodeID=auswahlBaum|abschluss:abschl=55,stg=079,abschlBE=&expand=0";
    // Determine ASI value to retrieve exam data
    private static String GRADE_PAGE_URL;
    private static String ASI;

    private NetworkUtil() {
    }

    public static boolean initializeCampusConnection() {
        if (IDM_USERNAME == null || IDM_PASSWORD == null) {
            Logger.logException("You need to provide IDM credentials to query exam information");
            return false;
        }

        // Persist cookies over various connections
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        try {
            Logger.logDebugMessage("Retrieving AuthState");
            // Retrieve AuthState
            String authRequest = sendGetRequest(AUTH_STATE_URL, Map.of(
                    "Connection", "keep-alive",
                    "Host", "www.campus.uni-erlangen.de",
                    "Referer", "https://www.campus.uni-erlangen.de/qisserver/rds?state=user&type=0"
            ));
            String authState = getAuthStateFromRequest(authRequest);

            Logger.logDebugMessage("Retrieving SAMLResponse");
            String oauthRequest = sendGetRequest(
                    OAUTH_URL + "AuthState=" + authState + "&username=" + IDM_USERNAME + "&password=" + IDM_PASSWORD,
                    Map.of(
                            "Content-Length", authState.length() + IDM_USERNAME.length() + IDM_PASSWORD.length() + ""
                    ));

            Logger.logDebugMessage("Finish SSO");
            String SAMLResponse = getSAMLResponseFromRequest(oauthRequest);
            String cookieRelayState = getCookieRelayState(oauthRequest);

            String htmlSAML = transformToHTMLString(SAMLResponse);
            String postSAML = sendPostRequest(SAML_URL, Map.of(
                    "Content-Length", SAMLResponse.length() + cookieRelayState.length() + "",
                    "Origin", "https://www.sso.uni-erlangen.de",
                    "RelayState", transformToHTMLString(cookieRelayState),
                    "Content-Type", "application/x-www-form-urlencoded"
            ), "SAMLResponse=" + htmlSAML);

            // Check that SSO was successful
            String checkAuth = sendGetRequest(EXAM_PAGE_URL, Collections.emptyMap());

            GRADE_PAGE_URL = getGradeURLFromRequest(checkAuth).replace("&amp;", "&");
            // TODO: 01/04/2021 extract into own method
            ASI = GRADE_PAGE_URL.split("/notenspiegel/student")[1];

        } catch (Exception e) {
            Logger.logException("Failed SSO", e);
        }

        if (GRADE_PAGE_URL != null) {
            Logger.logDebugMessage("SSO completed successfully");
            return true;
        }

        return false;
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

    public static Set<GradeEntry> getGradesFromMyCampus() {
        Set<GradeEntry> gradeEntries = new HashSet<>();

        // ensure that the connection was initialized correctly
        if (GRADE_PAGE_URL == null) {
            Logger.logException("grade page url was null");
            return null;
        }

        // retrieve exam data with previously fetched asi value
        String req = sendGetRequest(EXAM_VIEW_URL + ASI,
                Map.of("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));

        Document mainDoc = Jsoup.parse(Objects.requireNonNull(req));

        // main table that holds exam information
        Element gradeTable = mainDoc.getElementById("notenspiegel");

        Elements exams = gradeTable.select("tr");
        // first two are just headers so skip it
        for (int i = 2; i < exams.size(); i++) {
            Element currentExam = exams.get(i);
            Elements examValues = currentExam.getElementsByTag("td");

            if(examValues.get(3).html().trim().isEmpty())
                continue;

            gradeEntries.add(new GradeEntry(
                    examValues.get(0).text(), examValues.get(1).html(), examValues.get(2).text(),
                    examValues.get(3).html(), examValues.get(4).text(), examValues.get(5).text(), examValues.get(6).text(),
                    examValues.get(7).text(), examValues.get(8).text(), examValues.get(9).text()
            ));

        }

        return gradeEntries;
    }

    public static DictionaryEntry getDefinitionForWord(String word) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject root = (JSONObject) parser.parse(new String(getBytesFromURL(System.getenv("jadb_urban").replace("[X]", word))));

            JSONArray entries = (JSONArray) root.get("list");

            Iterator keyIterator = entries.iterator();

            DictionaryEntry currentMax = null;

            while (keyIterator.hasNext()) {
                JSONObject cur = (JSONObject) keyIterator.next();
                long tu = (Long) cur.get("thumbs_up");
                long td = (Long) cur.get("thumbs_down");
                if (tu + td < 10)
                    continue;
                if (td == 0)
                    td = 1;
                if ((float) tu / td > (currentMax == null ? 0 : (float) currentMax.thumbsUp() / currentMax.thumbsDown())) {
                    currentMax = new DictionaryEntry(
                            (String) cur.get("definition"), (String) cur.get("example"), (String) cur.get("author"),
                            System.getenv("jadb_urban_main").replace("[X]", word), Math.toIntExact((Long) cur.get("thumbs_up")),
                            Math.toIntExact((Long) cur.get("thumbs_down")), (String) cur.get("written_on"));
                }
            }

            return currentMax;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // TODO: 01/04/2021 now that jsoup is part of the build anyway, it would be good to parse html the proper way
    public static String getAuthStateFromRequest(String request) {
        String[] split = request.split("<input type=\"hidden\" name=\"AuthState\" value=\"");
        return split[1].substring(0, split[1].indexOf("\""));
    }

    public static String getSAMLResponseFromRequest(String request) {
        String[] split = request.split("<input type=\"hidden\" name=\"SAMLResponse\" value=\"");
        return split[1].substring(0, split[1].indexOf("\""));
    }

    public static String getCookieRelayState(String request) {
        String[] split = request.split("<input type=\"hidden\" name=\"RelayState\" value=\"");
        return split[1].substring(0, split[1].indexOf("\""));
    }

    public static String getGradeURLFromRequest(String request) {
        String[] split = request.split("<li><a id=\"notenspiegelStudent\" href=\"");
        return split[1].substring(0, split[1].indexOf("\""));
    }

    // this is by no means a exhaustive list, just enough to work
    public static String transformToHTMLString(String in) {
        return in.replaceAll("\\+", "%2B").replaceAll("=", "%3D").replaceAll(":", "%3A");
    }

}
