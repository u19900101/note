package ppppp.evernote.service.impl;

import ppppp.evernote.entity.Note;
import ppppp.evernote.mapper.NoteMapper;
import ppppp.evernote.service.NoteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 笔记 服务实现类
 * </p>
 *
 * @author ppppp
 * @since 2021-09-25
 */
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

}
