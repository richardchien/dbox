package im.r_c.android.dbox.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import im.r_c.android.dbox.DBox;
import im.r_c.android.dbox.DBoxCondition;
import im.r_c.android.dbox.DBoxResults;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.init(TAG);

        DBox.init(this, "Test.db");

        if (!getDatabasePath("Test.db").exists()) {
            // Generate random data

            DBox<Clazz> clzBox = DBox.of(Clazz.class);
            DBox<Course> crsBox = DBox.of(Course.class);
            DBox<Student> stuBox = DBox.of(Student.class);

            Random r = new Random();

            List<Clazz> clzList = new ArrayList<>();
            int n = 100 + r.nextInt(6);
            for (int i = 0; i < n; i++) {
                Clazz clz = new Clazz("Clazz " + r.nextInt(1000));
                clzBox.save(clz);
                clzList.add(clz);
            }

            List<Course> crsList = new ArrayList<>();
            n = 200 + r.nextInt(10);
            for (int i = 0; i < n; i++) {
                Course crs = new Course("C" + (1000 + i), "Course " + i);
                crsBox.save(crs);
                crsList.add(crs);
            }

            n = 1000 + r.nextInt(20);
            for (int i = 0; i < n; i++) {
                Student stu = new Student();
                stu.setName("Student " + (i + r.nextInt(1000)));
                stu.setClazz(clzList.get(r.nextInt(clzList.size())));
                int crsCount = 10 + r.nextInt(4);
                int start = r.nextInt(crsList.size() - crsCount + 1);
                Course[] favCrs = new Course[r.nextInt(crsCount - 3)];
                int favIdx = 0;
                for (int j = start; j < start + crsCount; j++) {
                    stu.addCourse(crsList.get(j));
                    if (r.nextInt(100) > 50) {
                        if (favIdx < favCrs.length) {
                            favCrs[favIdx++] = crsList.get(j);
                        }
                    }
                }
                stu.setFavoriteCourses(favCrs);
                stuBox.save(stu);
            }
        }

        DBoxCondition condition = new DBoxCondition()
                .between("id", "100", "110")
                .or().in("id", "210", "220")
                .or()
                .beginGroup()
                .not().equalTo("id", "110")
                .and().notEqualTo("id", "120")
                .beginGroup()
                .greaterThan("id", "180")
                .or().greaterThanOrEqualTo("id", "170")
                .or().lessThan("id", "130")
                .or().lessThanOrEqualTo("id", "140")
                .or().compare("id", "=", "150")
                .endGroup()
                .contains("name", "Student")
                .startsWith("name", "Student")
                .endsWith("name", "1")
                .like("name", "%t%")
                .not().isNull("name")
                .isNotNull("id")
                .endGroup();

        List<Student> list = DBox.of(Student.class)
                .find(condition)
                .orderByDesc("name")
                .orderBy("id")
                .results()
                .some(new DBoxResults.Filter<Student>() {
                    @Override
                    public boolean filter(Student student) {
                        return student != null && student.getFavoriteCourses() != null && student.getFavoriteCourses().length > 2;
                    }
                });
        Logger.d(list);

        DBoxResults<Student> results = DBox.of(Student.class).findAll().results();
        Logger.d(results.getFirst());
        Logger.d(results.getOne(1));
        Logger.d(results.getLast());
        Logger.d(results.getSome(0, 3));
        Logger.d(results.getSome(new DBoxResults.Filter<Student>() {
            @Override
            public boolean filter(Student student) {
                return student != null && student.getCourseList().size() > 11;
            }
        }));
        Logger.d(results.getAll());
        for (Student stu : results) {
            Logger.d(stu);
        }
        results.close();
    }
}
