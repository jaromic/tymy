package at.jarosoft.tymy2.controller;

import at.jarosoft.tymy2.Tymy2;
import at.jarosoft.tymy2.exception.CouldNotSaveSettingsException;
import at.jarosoft.tymy2.exception.CouldNotSaveTimeEntryException;
import at.jarosoft.tymy2.model.ActivityWrapper;
import at.jarosoft.tymy2.model.ProjectWrapper;
import at.jarosoft.tymy2.model.Settings;
import com.taskadapter.redmineapi.ProjectManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.TimeEntryManager;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import com.taskadapter.redmineapi.bean.TimeEntryFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author mj
 */
public class TymyController {

    private static Instant startTimestamp = null;
    private static Instant endTimestamp = null;
    private static TimeEntry lastEntry = null;

    private static RedmineManager getRedmineManager() {
        Settings settings = TymyController.loadSettings();

        String uri = settings.redmineUrl;
        String apiAccessKey = settings.apiKey;

        final int maxProjects = 100;

        RedmineManager redmineManager = RedmineManagerFactory.createWithApiKey(uri, apiAccessKey);

        redmineManager.setObjectsPerPage(maxProjects);

        return redmineManager;
    }

    private static TimeEntryManager getTimeEntryManager() {
        RedmineManager redmineManager = TymyController.getRedmineManager();
        TimeEntryManager timeEntryManager = redmineManager.getTimeEntryManager();
        return timeEntryManager;
    }

    public static List<ProjectWrapper> getProjectsWrapped() {

        RedmineManager redmineManager = TymyController.getRedmineManager();

        ProjectManager projectManager = redmineManager.getProjectManager();
        List<ProjectWrapper> projectsWrapped;
        projectsWrapped = new ArrayList<>();
        try {
            List<Project> projects;
            projects = projectManager.getProjects();
            for (Project project : projects) {
                projectsWrapped.add(new ProjectWrapper(project));
            }

        } catch (RedmineException ex) {
            Logger.getLogger(TymyController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return projectsWrapped;
    }

    public static List<ActivityWrapper> getActivitiesWrapped() {
        TimeEntryManager timeEntryManager = TymyController.getTimeEntryManager();
        List<ActivityWrapper> activitiesWrapped;
        activitiesWrapped = new ArrayList<>();
        try {
            List<TimeEntryActivity> activities;
            activities = timeEntryManager.getTimeEntryActivities();
            for (TimeEntryActivity activity : activities) {
                activitiesWrapped.add(new ActivityWrapper(activity));
            }
        } catch (RedmineException ex) {
            Logger.getLogger(TymyController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return activitiesWrapped;
    }

    public static void saveSettings(String redmineUrl, String apiKey, Integer lastProjectId, String lastTicketId, String lastComment, Double lastHours, Integer lastActivityId) throws CouldNotSaveSettingsException {
        Preferences preferences;
        preferences = Preferences.userRoot().node(Tymy2.class.getName());
        try {
            preferences.put("redmineUrl", redmineUrl);
            preferences.put("apiKey", apiKey);
            preferences.put("lastProjectId", lastProjectId.toString());
            preferences.put("lastTicketId", lastTicketId);
            preferences.put("lastComment", lastComment);
            preferences.put("lastHours", lastHours.toString());
            preferences.put("lastActivityId", lastActivityId.toString());
            preferences.sync();
        } catch (BackingStoreException e) {
            throw new CouldNotSaveSettingsException(e.getMessage());
        }
    }

    public static Settings loadSettings() {
        Preferences preferences;
        String redmineUrl;
        String apiKey;
        int lastProjectId;
        String lastTicketId;
        String lastComment;
        Double lastHours;
        int lastActivityId;
        preferences = Preferences.userRoot().node(Tymy2.class.getName());
        redmineUrl = preferences.get("redmineUrl", "");
        apiKey = preferences.get("apiKey", "");
        try {
            lastProjectId = Integer.parseInt(preferences.get("lastProjectId", "0"));
            lastTicketId = preferences.get("lastTicketId", "");
            lastComment = preferences.get("lastComment", "");
            lastHours = Double.parseDouble(preferences.get("lastHours", "0.0"));
            lastActivityId = Integer.parseInt(preferences.get("lastActivityId", "0"));
        } catch (NumberFormatException e) {
            throw e;
        }
        return new Settings(redmineUrl, apiKey, lastProjectId, lastTicketId, lastComment, lastHours, lastActivityId);
    }

    public static void startMeasurement() {
        TymyController.startTimestamp = Instant.now();
    }

    public static void stopMeasurement() {
        TymyController.endTimestamp = Instant.now();
    }

    public static void continueMeasurement() {
        long secondsSinceStopping = ChronoUnit.SECONDS.between(endTimestamp, Instant.now());
        TymyController.startTimestamp = TymyController.startTimestamp.plus(secondsSinceStopping, ChronoUnit.SECONDS);
    }

    public static long getMeasurementSeconds() {
        Instant startTimestamp = TymyController.startTimestamp;
        Instant endTimestamp = TymyController.endTimestamp;
        if (startTimestamp == null || endTimestamp == null) {
            return 0;
        } else {
            return ChronoUnit.SECONDS.between(startTimestamp, endTimestamp);
        }
    }

    public static double secondsToIndustrialHours(long seconds) {
        double industrialHours = ((double) (seconds)) / 3600d;
        double industrialHoursRounded = Math.ceil(industrialHours * 4d) / 4d;
        return industrialHoursRounded;
    }

    public static String submitMeasurement(ProjectWrapper projectWrapper, String ticket, String comment, double hours, ActivityWrapper activityWrapper) throws CouldNotSaveTimeEntryException {
        Project project = projectWrapper.getProject();
        TimeEntryActivity activity = activityWrapper.getActivity();

        TimeEntryManager timeEntryManager = getTimeEntryManager();
        TimeEntry timeEntry = TimeEntryFactory.create();
        if (ticket.isEmpty()) {
            timeEntry.setProjectId(project.getId());
            timeEntry.setProjectName(project.getName());
        } else {
            timeEntry.setIssueId(Integer.parseInt(ticket));
        }
        timeEntry.setComment(comment);
        timeEntry.setHours(new Float(hours));
        timeEntry.setActivityId(activity.getId());
        timeEntry.setActivityName(activity.getName());
        try {
            Logger.getLogger(TymyController.class.getName()).log(Level.INFO, String.format("Sending %s hours comment='%s' for issue=%s project='%s' activity='%s'", hours, comment, ticket, projectWrapper.toString(), activityWrapper.toString()));
            TymyController.lastEntry = timeEntryManager.createTimeEntry(timeEntry);
        } catch (RedmineException ex) {
            Logger.getLogger(TymyController.class.getName()).log(Level.SEVERE, null, ex);
            throw new CouldNotSaveTimeEntryException(ex.getMessage());
        }

        return TymyController.lastEntry.toString();
    }

    public static String undoLastMeasurement() throws CouldNotSaveTimeEntryException {
        TimeEntry lastEntry = TymyController.lastEntry;
        String lastEntryString = lastEntry.toString();
        if (lastEntry instanceof TimeEntry) {
            try {
                TimeEntryManager timeEntryManager = getTimeEntryManager();
                timeEntryManager.deleteTimeEntry(lastEntry.getId());
                TymyController.lastEntry = null;
            } catch (RedmineException ex) {
                Logger.getLogger(TymyController.class.getName()).log(Level.SEVERE, null, ex);
                throw new CouldNotSaveTimeEntryException(ex.getMessage());
            }
        }
        return lastEntryString;
    }

    public static boolean canUndo() {
        return (TymyController.lastEntry != null);
    }
}
