package im.r_c.android.dbox.sample;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import im.r_c.android.dbox.DBox;
import im.r_c.android.dbox.DBoxCondition;
import im.r_c.android.dbox.DBoxResults;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBox.init(this, "Test.db");

//        // Save
//        Student stu = new Student();
//        stu.setName("Richard");
//        Course c1 = new Course("1234", "CS");
//        Course c2 = new Course("1235", "IOT");
//        stu.addCourse(c1);
//        stu.addCourse(c2);
//        stu.setFavoriteCourses(new Course[]{c1});
//        Clazz clazz = new Clazz("Class 1");
//        stu.setClazz(clazz);
//        DBox.of(Clazz.class).save(clazz);
//        DBox.of(Course.class).save(c1);
//        DBox.of(Course.class).save(c2);
//        DBox.of(Student.class).save(stu);
//        Log.d(TAG, "Save: " + clazz.getId() + ", " + c1.getId() + ", " + c2.getId() + ", " + stu.getId());
//
//        // Update
//        stu.setName("Changed");
//        stu.setFavoriteCourses(new Course[]{c2});
//        boolean ok = DBox.of(Student.class).save(stu);
//        Log.d(TAG, "Update: " + ok);
//
//        // Remove
//        ok = DBox.of(Course.class).remove(c1);
//        Log.d(TAG, "Remove: " + ok);
//
//        // Clear
//        ok = DBox.of(Student.class).clear();
//        Log.d(TAG, "Clear: " + ok);
//
//        // Drop
//        ok = DBox.of(Student.class).drop();
//        Log.d(TAG, "Drop: " + ok);
//
//        // Re-save after clear and drop
//        DBox.of(Course.class).save(c1);
//        DBox.of(Course.class).save(c2);
//        DBox.of(Student.class).save(stu);
//        Log.d(TAG, "Save: " + clazz.getId() + ", " + c1.getId() + ", " + c2.getId() + ", " + stu.getId());

//        DBox<Clazz> clzBox = DBox.of(Clazz.class);
//        DBox<Course> crsBox = DBox.of(Course.class);
//        DBox<Student> stuBox = DBox.of(Student.class);
//
//        Random r = new Random();
//
//        List<Clazz> clzList = new ArrayList<>();
//        int n = 300 + r.nextInt(6);
//        for (int i = 0; i < n; i++) {
//            Clazz clz = new Clazz("Clazz " + i);
//            clzBox.save(clz);
//            clzList.add(clz);
//        }
//
//        List<Course> crsList = new ArrayList<>();
//        n = 800 + r.nextInt(10);
//        for (int i = 0; i < n; i++) {
//            Course crs = new Course("C" + (1000 + i), "Course " + i);
//            crsBox.save(crs);
//            crsList.add(crs);
//        }
//
//        n = 10000 + r.nextInt(20);
//        for (int i = 0; i < n; i++) {
//            Student stu = new Student();
//            stu.setName("Student " + (i + r.nextInt(10000)));
//            stu.addClazz(clzList.get(r.nextInt(clzList.size())));
//            stu.addClazz(clzList.get(r.nextInt(clzList.size())));
//            int crsCount = 10 + r.nextInt(4);
//            int start = r.nextInt(crsList.size() - crsCount + 1);
//            Course[] favCrs = new Course[r.nextInt(crsCount - 3)];
//            int favIdx = 0;
//            for (int j = start; j < start + crsCount; j++) {
//                stu.addCourse(crsList.get(j));
//                if (r.nextInt(100) > 50) {
//                    if (favIdx < favCrs.length) {
//                        favCrs[favIdx++] = crsList.get(j);
//                    }
//                }
//            }
//            stu.setFavoriteCourses(favCrs);
//            stuBox.save(stu);
//        }

        Log.d(TAG, "start: " + SystemClock.elapsedRealtime());
        List<Student> list = DBox.of(Student.class)
                .find(new DBoxCondition()
                        .between("id", "100", "120"))
                .orderByDesc("name")
                .orderBy("id")
                .results()
                .some(new DBoxResults.Filter<Student>() {
                    @Override
                    public boolean filter(Student student) {
                        return student.getFavoriteCourses() != null && student.getFavoriteCourses().length > 2;
                    }
                });
        Log.d(TAG, "end: " + SystemClock.elapsedRealtime());

//        List<Course> list = DBox.of(Course.class)
//                .findAll()
//                .orderByDesc("name")
//                .orderBy("id")
//                .results()
//                .some(new DBoxResults.Filter<Course>() {
//                    @Override
//                    public boolean filter(Course course) {
//                        return true;
//                    }
//                });
    }
}
