package network;

import discord.DictionaryEntry;
import log.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

public class NetworkUtil {

    private NetworkUtil() {

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

}
