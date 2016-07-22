package im.r_c.android.dbox.sample;

import im.r_c.android.dbox.annotation.Column;
import im.r_c.android.dbox.annotation.Table;

/**
 * DBox
 * Created by richard on 7/17/16.
 */
@Table
class Clazz {
    private long id;

    @Column(notNull = true)
    private String name;

    Clazz() {
    }

    Clazz(String name) {
        this.name = name;
    }

    long getId() {
        return id;
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

        Clazz aClass = (Clazz) o;

        if (id != aClass.id) return false;
        return name != null ? name.equals(aClass.name) : aClass.name == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Class{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
