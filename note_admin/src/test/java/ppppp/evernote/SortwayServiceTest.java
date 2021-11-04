package ppppp.evernote;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ppppp.evernote.entity.Sortway;
import ppppp.evernote.service.SortWayService;

import java.util.List;

/**
 * @author pppppp
 * @date 2021/11/4 20:45
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SortwayServiceTest {
    @Autowired
    SortWayService sortWayService;
    @Test
    public void T_(){
        List<Sortway> list = sortWayService.lambdaQuery().list();
        System.out.println(list);
    }
}
