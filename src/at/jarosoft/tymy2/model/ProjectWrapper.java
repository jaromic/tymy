/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jarosoft.tymy2.model;

import com.taskadapter.redmineapi.bean.Project;

/**
 *
 * @author mj
 */
public class ProjectWrapper {

    private Project project;

    public ProjectWrapper(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public String toString() {
        return project.getName();
    }

}
