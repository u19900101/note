package ppppp.evernote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ppppp.evernote.entity.Notebook;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.mapper.NotebookMapper;
import ppppp.evernote.mapper.TagMapper;


import java.util.Date;
import java.util.List;

@SpringBootTest
class EvernoteApplicationTests {

    @Test
    void contextLoads() {
    }
    @Autowired
    private TagMapper userMapper;

    @Test
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        List<Tag> userList = userMapper.selectList(null);

        //userList.forEach(System.out::println);
        //userMapper.insert(new Tag("tag_id_5","数据库",78,
        //        new Date(),null,false));
    }

}
