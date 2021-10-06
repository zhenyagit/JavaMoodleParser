package org.imjs_man.moodleParser.entity;

import javax.persistence.*;
import java.util.List;
import java.util.Set;


@Entity
public class PersonEntity {
    @Id
    private long id;
    private String login;
    private String password;
    private String groupName;
    private String token;
    private String name;
    private String surname;
    private String patronymic;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<CourseEntity> courseEntityList;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Set<CourseEntity> getCourseEntityList() {
        return courseEntityList;
    }

    public void addCourseEntityList(Set<CourseEntity> newCourses)
    {
        this.courseEntityList.addAll(newCourses);
    }
    public void setCourseEntityList(Set<CourseEntity> courseEntityList) {
        this.courseEntityList = courseEntityList;
    }
}
