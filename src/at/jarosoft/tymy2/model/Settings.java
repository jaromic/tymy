package at.jarosoft.tymy2.model;

/**
 *
 * @author mj
 */
public class Settings {

    public String redmineUrl = null;
    public String apiKey = null;

    public Settings(String redmineUrl, String apiKey) {
        this.redmineUrl = redmineUrl;
        this.apiKey = apiKey;
    }

}
