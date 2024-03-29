package external.dict;

import main.zGPB;
import network.NetworkUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Collections;

public class DictionaryManager {

    public static DictionaryEntry getDefinitionForWord(String word) {
        JSONParser parser = new JSONParser();
        try {

            String wordURL = zGPB.INSTANCE.botConfigurationHandler.getConfigValue("zGPB_dict_api").replace("[X]", NetworkUtil.transformToHTMLString(word));
            String apiAnswer = NetworkUtil.sendGetRequest(wordURL, Collections.emptyMap());

            if (apiAnswer == null || apiAnswer.trim().isEmpty())
                return null;

            JSONObject root = (JSONObject) parser.parse(apiAnswer);

            JSONArray entries = (JSONArray) root.get("list");

            var keyIterator = entries.iterator();

            DictionaryEntry currentMax = null;

            while (keyIterator.hasNext()) {
                JSONObject cur = (JSONObject) keyIterator.next();
                long tu = (Long) cur.get("thumbs_up");
                long td = (Long) cur.get("thumbs_down");

                if (currentMax == null || currentMax.thumbsUp() + currentMax.thumbsDown() < tu + td) {
                    currentMax = new DictionaryEntry(
                            (String) cur.get("definition"), (String) cur.get("example"), (String) cur.get("author"),
                            zGPB.INSTANCE.botConfigurationHandler.getConfigValue("zGPB_dict_main").
                                    replace("[X]", NetworkUtil.transformToHTMLString(word)), Math.toIntExact((Long) cur.get("thumbs_up")),
                            Math.toIntExact((Long) cur.get("thumbs_down")), (String) cur.get("written_on"));
                }
            }

            return currentMax;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
