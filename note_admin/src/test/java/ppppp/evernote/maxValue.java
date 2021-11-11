package ppppp.evernote;

import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.service.TagService;

import java.util.List;

/**
 * @author pppppp
 * @date 2021/11/11 17:02
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class maxValue {
    @Autowired
    TagService tagService;
    @Test
    public void T_max(){
        List<Tag> last = tagService.lambdaQuery().orderByDesc(Tag::getSort).last("limit 1").list();
        System.out.println(last);

    }
}
