package ppppp.evernote;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ppppp.evernote.entity.Search;
import ppppp.evernote.service.SearchService;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchApplicationTests {

    @Autowired
    SearchService searchService;
    @Test
    public void contextLoads() {
        List<Search> searchList = searchService.lambdaQuery().orderByDesc(Search::getCreateTime).last("limit 10").list();
        System.out.println(searchList);
    }

    @Test
    public void T_(){
        System.out.println();
    }

}
