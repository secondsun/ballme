package net.saga.googleimagegetter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author summers
 */
public class Getter {

    private static final String template = "https://www.googleapis.com/customsearch/v1?q=${query}&fileType=png+svg&imgSize=large&num=1&key=${key}&cx=${engineId}";
    private static final Gson gson = new Gson();
    public static byte[] getImage(String query, String key, String engineId) {
        try {
            String localUrl = template.replace("${key}", key);
            localUrl =  localUrl.replace("${engineId}", engineId);
            localUrl = localUrl.replace("${query}", query.replace(' ', '+'));
            
            String response = IOUtils.toString(new URL(localUrl));
            JsonElement json = new JsonParser().parse(response);
            JsonObject item0 = json.getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject();
            
            String imageURL;
            if ("UNLV Rebels".equals(query.trim())) {
                imageURL = "http://content.sportslogos.net/logos/35/888/full/9062_unlv_rebels-secondary-2006.png";
            } else if (item0.has("pagemap")) {
                imageURL = item0.get("pagemap").getAsJsonObject().get("cse_image").getAsJsonArray().get(0).getAsJsonObject().get("src").getAsString();
            } else {
                imageURL = item0.get("link").getAsString();
            }

            return IOUtils.toByteArray(new URL(imageURL));
        } catch (Exception ex) {
            Logger.getLogger(Getter.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        
    }
    
}
