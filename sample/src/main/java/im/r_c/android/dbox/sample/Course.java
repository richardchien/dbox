package im.r_c.android.dbox.sample;

import im.r_c.android.dbox.annotation.Column;
import im.r_c.android.dbox.annotation.Table;

/**
 * DBox
 * Created by richard on 7/16/16.
 */
@Table
class Course {
    private long id;

    @Column(notNull = true, unique = true)
    private String code;

    @Column(notNull = true)
    private String name;

    Course() {
    }

    Course(String code, String name) {
        this.code = code;
        this.name = name;
    }

    long getId() {
        return id;
    }

    String getCode() {
        return code;
    }

    void setCode(String code) {
        this.code = code;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Course course = (Course) o;

        if (id != course.id) return false;
        if (code != null ? !code.equals(course.code) : course.code != null) return false;
        return name != null ? name.equals(course.name) : course.name == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
