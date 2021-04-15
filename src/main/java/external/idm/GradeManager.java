package external.idm;

import discord.EmbedField;
import discord.MessageCrafter;
import log.Logger;
import main.zGPB;
import net.dv8tion.jda.api.entities.TextChannel;
import network.NetworkUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GradeManager {

    private final String AUTH_STATE_URL = "https://www.campus.uni-erlangen.de/Shibboleth.sso/Login";
    private final String OAUTH_URL = "https://www.sso.uni-erlangen.de/simplesaml/module.php/core/loginuserpass.php?";
    private final String SAML_URL = "https://www.campus.uni-erlangen.de/Shibboleth.sso/SAML2/POST";
    // Overview page we use to determine GRADE_PAGE_URL
    private final String EXAM_PAGE_URL = "https://www.campus.uni-erlangen.de/qisserver/rds?state=template&template=pruefungen";
    // Exam data can be retrieved here
    private final String EXAM_VIEW_URL = "https://www.campus.uni-erlangen.de/qisserver/rds?state=notenspiegelStudent&next=list.vm&nextdir=qispos/notenspiegel/student&createInfos=Y&struct=auswahlBaum&nodeID=auswahlBaum|abschluss:abschl=55,stg=079,abschlBE=&expand=0";
    public boolean isEnabled = true;
    public boolean insertTest = false;
    // Network related
    private String IDM_USERNAME;
    private String IDM_PASSWORD;
    // Non network related
    // Determine ASI value to retrieve exam data
    private String GRADE_PAGE_URL;
    private String ASI;
    private Set<GradeEntry> current;
    private boolean initAndMsg = false;
    private int exceptionCount = 0;
    private boolean exception = false;

    public void startMonitoring() {
        if (!zGPB.INSTANCE.botConfigurationHandler.getConfigValueBoolean("zGPB_idm_enabled"))
            return;

        IDM_USERNAME = zGPB.INSTANCE.botConfigurationHandler.getConfigValue("zGPB_idm_username");
        IDM_PASSWORD = zGPB.INSTANCE.botConfigurationHandler.getConfigValue("zGPB_idm_password");

        GRADE_PAGE_URL = null;
        current = null;
        ASI = null;
        if (initializeCampusConnection()) {
            current = Collections.emptySet();
            initAndMsg = true;
            Logger.logDebugMessage("GradeManager: isEnabled=" + isEnabled + "; initAndMsg=" + initAndMsg);
            waitForResults();
        } else {
            Logger.logException("Couldn't initialize campus connection");
            Logger.logDebugMessage("GradeManager: isEnabled=" + isEnabled + "; initAndMsg=" + initAndMsg);
        }
    }

    public boolean initializeCampusConnection() {
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
            String authRequest = NetworkUtil.sendGetRequest(AUTH_STATE_URL, Map.of(
                    "Connection", "keep-alive",
                    "Host", "www.campus.uni-erlangen.de",
                    "Referer", "https://www.campus.uni-erlangen.de/qisserver/rds?state=user&type=0"
            ));
            String authState = getAuthStateFromRequest(authRequest);

            Logger.logDebugMessage("Retrieving SAMLResponse");
            String oauthRequest = NetworkUtil.sendGetRequest(
                    OAUTH_URL + "AuthState=" + authState + "&username=" + IDM_USERNAME + "&password=" + IDM_PASSWORD,
                    Map.of(
                            "Content-Length", authState.length() + IDM_USERNAME.length() + IDM_PASSWORD.length() + ""
                    ));

            Logger.logDebugMessage("Finish SSO");
            String SAMLResponse = getSAMLResponseFromRequest(oauthRequest);
            String cookieRelayState = getCookieRelayState(oauthRequest);

            String htmlSAML = NetworkUtil.transformToHTMLString(SAMLResponse);
            String postSAML = NetworkUtil.sendPostRequest(SAML_URL, Map.of(
                    "Content-Length", SAMLResponse.length() + cookieRelayState.length() + "",
                    "Origin", "https://www.sso.uni-erlangen.de",
                    "RelayState", NetworkUtil.transformToHTMLString(cookieRelayState),
                    "Content-Type", "application/x-www-form-urlencoded"
            ), "SAMLResponse=" + htmlSAML);

            // Check that SSO was successful
            String checkAuth = NetworkUtil.sendGetRequest(EXAM_PAGE_URL, Collections.emptyMap());

            GRADE_PAGE_URL = NetworkUtil.transformFromHTMLString(getGradeURLFromRequest(checkAuth));
            ASI = getASIFromURL(GRADE_PAGE_URL);

        } catch (Exception e) {
            Logger.logException("Failed SSO", e);
        }

        if (GRADE_PAGE_URL != null) {
            Logger.logDebugMessage("SSO completed successfully");
            return true;
        }

        return false;
    }

    public Set<GradeEntry> getGradesFromMyCampus() {
        Set<GradeEntry> gradeEntries = new HashSet<>();

        // ensure that the connection was initialized correctly
        if (GRADE_PAGE_URL == null) {
            Logger.logException("grade page url was null");
            return null;
        }

        // retrieve exam data with previously fetched asi value
        String req = NetworkUtil.sendGetRequest(EXAM_VIEW_URL + ASI,
                Map.of("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));

        Document mainDoc = Jsoup.parse(Objects.requireNonNull(req));

        // main table that holds exam information
        Element gradeTable = mainDoc.getElementById("notenspiegel");

        Elements exams = gradeTable.select("tr");
        // first two are just headers so skip it
        for (int i = 2; i < exams.size(); i++) {
            Element currentExam = exams.get(i);
            Elements examValues = currentExam.getElementsByTag("td");

            if (examValues.size() < 9) {
                continue;
            }

            if (examValues.get(3).html().trim().isEmpty())
                continue;

            gradeEntries.add(new GradeEntry(
                    examValues.get(0).text(), examValues.get(1).html(), examValues.get(2).text(),
                    examValues.get(3).html(), examValues.get(4).text(), examValues.get(5).text(), examValues.get(6).text(),
                    examValues.get(7).text(), examValues.get(8).text(), examValues.get(9).text()
            ));

        }

        return gradeEntries;
    }

    public void waitForResults() {
        new Thread(() -> {
            while (true) {
                if (exceptionCount >= 6) {
                    Logger.logException("reached exception limit, stopping");
                    return;
                }
                if (exception) {
                    Logger.logDebugMessage("got exception, count " + exceptionCount);
                    initializeCampusConnection();
                    exception = false;
                }
                try {
                    if (isEnabled) {
                        Logger.logDebugMessage("Fetching new grades, old size: " + current.size());
                        Set<GradeEntry> newEntries = Objects.requireNonNull(getGradesFromMyCampus());

                        if (insertTest) {
                            newEntries.add(
                                    new GradeEntry(
                                            "00000", "test_exam", "WS 00/00", "01.01.1900", "1.0",
                                            "111", "yes", "10", "+", "1"));
                            insertTest = false;
                        }

                        if (newEntries.size() != current.size()) {
                            Set<GradeEntry> tempCopy = new HashSet<>(newEntries);
                            newEntries.removeAll(current);

                            if (!initAndMsg) {
                                for (long channelID : zGPB.INSTANCE.guildConfigurationHandler.getIDsForKey("grade_notification")) {
                                    TextChannel tc = zGPB.INSTANCE.discordHandler.getLocalJDA().getTextChannelById(channelID);

                                    if (tc != null) {
                                        for (GradeEntry ge : newEntries) {
                                            tc.sendMessage(MessageCrafter.craftGenericEmbedMessage("exam update",
                                                    new EmbedField("name", ge.name(), false),
                                                    new EmbedField("semester", ge.semester(), false),
                                                    new EmbedField("id", ge.id(), true),
                                                    new EmbedField("date", ge.date(), true),
                                                    new EmbedField("ects", ge.ects(), true)
                                            )).queue();
                                        }
                                    } else {
                                        Logger.logException("channel couldn't be found");
                                    }

                                }
                            }

                            initAndMsg = false;
                            current = tempCopy;
                        }
                    }

                    TimeUnit.MINUTES.sleep(zGPB.INSTANCE.botConfigurationHandler.getConfigValueInteger("zGPB_idm_refresh"));
                } catch (Exception e) {
                    e.printStackTrace();
                    exceptionCount++;
                    exception = true;
                }
            }
        }).start();
    }

    public String getAuthStateFromRequest(String request) {
        String[] split = request.split("<input type=\"hidden\" name=\"AuthState\" value=\"");
        return split[1].substring(0, split[1].indexOf("\""));
    }

    public String getSAMLResponseFromRequest(String request) {
        String[] split = request.split("<input type=\"hidden\" name=\"SAMLResponse\" value=\"");
        return split[1].substring(0, split[1].indexOf("\""));
    }

    public String getCookieRelayState(String request) {
        String[] split = request.split("<input type=\"hidden\" name=\"RelayState\" value=\"");
        return split[1].substring(0, split[1].indexOf("\""));
    }

    public String getGradeURLFromRequest(String request) {
        String[] split = request.split("<li><a id=\"notenspiegelStudent\" href=\"");
        return split[1].substring(0, split[1].indexOf("\""));
    }

    public String getASIFromURL(String url) {
        return url.split("/notenspiegel/student")[1];
    }

}
