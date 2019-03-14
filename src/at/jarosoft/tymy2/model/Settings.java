package at.jarosoft.tymy2.model;

/**
 *
 * @author mj
 */
public class Settings {

    public String redmineUrl = null;
    public String apiKey = null;
    public int lastProjectId = 0;
    public String lastTicketId = null;
    public String lastComment = null;
    public Double lastHours = null;
    public int lastActivityId = 0;

    public Settings(String redmineUrl, String apiKey, int lastProjectId, String lastTicketId, String lastComment, Double lastHours, int lastActivityId) {
        this.redmineUrl = redmineUrl;
        this.apiKey = apiKey;
        this.lastProjectId = lastProjectId;
        this.lastTicketId = lastTicketId;
        this.lastComment = lastComment;
        this.lastHours = lastHours;
        this.lastActivityId = lastActivityId;
    }

}
