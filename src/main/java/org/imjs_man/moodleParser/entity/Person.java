package org.imjs_man.moodleParser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;


@Entity
public class Person {
    @Id
    private long id;
    private String login;
    private String password;
    private String groupName;
    @OneToMany
    private List<org.imjs_man.moodleParser.entity.Course> courseList;




}
