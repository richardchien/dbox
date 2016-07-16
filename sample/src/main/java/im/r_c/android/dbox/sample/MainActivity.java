package im.r_c.android.dbox.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import im.r_c.android.dbox.DBox;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBox.init(this, "Test.db");
        Student stu = new Student();
        stu.setName("Richard");
        stu.addCourse(new Course("1234", "CS"));
        DBox.of(Student.class).save(stu);
    }
}
