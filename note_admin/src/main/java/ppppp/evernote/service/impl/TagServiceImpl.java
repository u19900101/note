package ppppp.evernote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.mapper.TagMapper;
import ppppp.evernote.service.TagService;

/**
 * <p>
 * 标签表 服务实现类
 * </p>
 *
 * @author ppppp
 * @since 2021-09-26
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

}
