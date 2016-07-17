package im.r_c.android.dbox.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import im.r_c.android.dbox.DBox;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBox.init(this, "Test.db");
        Student stu = new Student();
        stu.setName("Richard");
        Course c1 = new Course("1234", "CS");
        Course c2 = new Course("1235", "IOT");
        stu.addCourse(c1);
        stu.addCourse(c2);
        stu.setFavoriteCourses(new Course[]{c1});
        Clazz clazz = new Clazz("Class 1");
        stu.setClazz(clazz);
        DBox.of(Clazz.class).save(clazz);
        DBox.of(Course.class).save(c1);
        DBox.of(Course.class).save(c2);
        DBox.of(Student.class).save(stu);

        Log.d(TAG, c1.getId() + ", " + c2.getId() + ", " + stu.getId());
    }
}
