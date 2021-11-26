package ppppp.evernote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import ppppp.evernote.entity.Picture;
import ppppp.evernote.mapper.PictureMapper;
import ppppp.evernote.service.PictureService;

@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

}
