/*
 * Name: Lauren Johnson (lajohnso)
 *
 * Project 4 - Task 1, Part 1
 * Fetch a chapter from the Free Use Bible API, extract one verse, print it.
 *
 * Endpoint: https://bible.helloao.org/api/{translation}/{book}/{chapter}.simple.json
 */

// imports for converting fetched JSON to pojo
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// imports for fetch method
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class BibleApiTest {

    static final String BASE = "https://bible.helloao.org/api/";
    static final int MAX_CROSS_REFS = 5;   // the dataset can have dozens per verse; show the top few

    public static void main(String[] args) {
        String translation = "BSB";
        String book = "JHN";
        int chapter = 3;
        int verse = 16;


        // fetch verse text and footnotes
        JsonObject verseObj = findVerse(translation, book, chapter, verse);
        if (verseObj == null) {
            System.out.println("Verse not found: " + book + " " + chapter + ":" + verse);
            return;
        }

        System.out.println(book + " " + chapter + ":" + verse + " (" + translation + ")");
        System.out.println(verseObj.get("text").getAsString());
        System.out.println();

        JsonArray footnotes;
        if (verseObj.has("footnotes")) {
            footnotes = verseObj.getAsJsonArray("footnotes");
        } else {
            footnotes = new JsonArray();   // empty array
        }
    }

    // Fetches a chapter in the simplified format and returns the matching verse object, or null
    private static JsonObject findVerse(String translation, String book, int chapter, int verse) {
        String json = fetch(BASE + translation + "/" + book + "/" + chapter + ".simple.json");
        if (json.isEmpty()) return null;

        JsonArray content = JsonParser.parseString(json).getAsJsonObject()
                .getAsJsonObject("chapter").getAsJsonArray("content");

        for (JsonElement el : content) {
            JsonObject item = el.getAsJsonObject();
            // content also holds headings and line breaks, so only look at verses
            if (item.has("type") && item.get("type").getAsString().equals("verse")
                    && item.get("number").getAsInt() == verse) {
                return item;
            }
        }
        return null;
    }

    // Adapted from InterestingPictureModel.fetch()
    private static String fetch(String urlString) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = URI.create(urlString).toURL();   // new URL(String) is deprecated in newer JDKs
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Request failed: " + e.getMessage());
        }
        return response.toString();
    }
}
