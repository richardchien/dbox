package im.r_c.android.dbox.sample;

import java.util.ArrayList;
import java.util.List;

import im.r_c.android.dbox.annotation.Column;
import im.r_c.android.dbox.annotation.ObjectColumn;
import im.r_c.android.dbox.annotation.Table;

/**
 * DBox
 * Created by richard on 7/16/16.
 */
@Table("Student")
public class Student {
    private long id;

    @Column(notNull = true)
    private String name;

    @ObjectColumn(Course.class)
    private List<Course> courseList;

    public Student() {
        courseList = new ArrayList<>();
    }

    public Student(String name, List<Course> courseList) {
        this.name = name;
        this.courseList = courseList;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean addCourse(Course course) {
        return courseList.add(course);
    }

    public boolean removeCourse(Course course) {
        return courseList.remove(course);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Student student = (Student) o;

        if (id != student.id) return false;
        if (name != null ? !name.equals(student.name) : student.name != null) return false;
        return courseList != null ? courseList.equals(student.courseList) : student.courseList == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (courseList != null ? courseList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", courseList=" + courseList +
                '}';
    }
}
