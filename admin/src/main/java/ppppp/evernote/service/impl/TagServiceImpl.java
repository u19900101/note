package ppppp.evernote.service.impl;

import ppppp.evernote.entity.Tag;
import ppppp.evernote.mapper.TagMapper;
import ppppp.evernote.service.TagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 标签表 服务实现类
 * </p>
 *
 * @author ppppp
 * @since 2021-09-25
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

}
