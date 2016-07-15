package im.r_c.android.dbox;

import org.junit.Test;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        Condition condition = new Condition()
                .between("id", "2", "7")
                .and()
                .group()
                .greaterThan("id", "3")
                .and()
                .like("city", "%jing")
                .or()
                .in("city", "Berlin", "Paris")
                .endGroup();
        System.out.println(condition.build());
        System.out.println(condition.getArgList());
    }
}