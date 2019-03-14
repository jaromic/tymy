/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jarosoft.tymy2.model;

import com.taskadapter.redmineapi.bean.TimeEntryActivity;

/**
 *
 * @author mj
 */
public class ActivityWrapper implements ComboBoxable {

    private TimeEntryActivity activity;

    public ActivityWrapper(TimeEntryActivity activity) {
        this.activity = activity;
    }

    public TimeEntryActivity getActivity() {
        return this.activity;
    }

    public void setActivity(TimeEntryActivity activity) {
        this.activity = activity;
    }

    @Override
    public String toString() {
        return activity.getName();
    }

    @Override
    public int getId() {
        return this.activity.getId();
    }

}
