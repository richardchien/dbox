package im.r_c.android.dbox.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import im.r_c.android.dbox.annotation.Column;
import im.r_c.android.dbox.annotation.ObjectColumn;
import im.r_c.android.dbox.annotation.Table;

/**
 * DBox
 * Created by richard on 7/16/16.
 */
@Table("Student")
class Student {
    private long id;

    @Column(notNull = true)
    private String name;

    @ObjectColumn(Course.class)
    private List<Course> courseList;

    @ObjectColumn(Course.class)
    private Course[] favoriteCourses;

    @ObjectColumn(Clazz.class)
    private Clazz clazz;

    public Student() {
        courseList = new ArrayList<>();
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

    public Course[] getFavoriteCourses() {
        return favoriteCourses;
    }

    public void setFavoriteCourses(Course[] favoriteCourses) {
        this.favoriteCourses = favoriteCourses;
    }

    public Clazz getClazz() {
        return clazz;
    }

    public void setClazz(Clazz clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Student student = (Student) o;

        if (id != student.id) return false;
        if (name != null ? !name.equals(student.name) : student.name != null) return false;
        if (courseList != null ? !courseList.equals(student.courseList) : student.courseList != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equalTo
        if (!Arrays.equals(favoriteCourses, student.favoriteCourses)) return false;
        return clazz != null ? clazz.equals(student.clazz) : student.clazz == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (courseList != null ? courseList.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(favoriteCourses);
        result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", courseList=" + courseList +
                ", favoriteCourses=" + Arrays.toString(favoriteCourses) +
                ", clazz=" + clazz +
                '}';
    }
}
