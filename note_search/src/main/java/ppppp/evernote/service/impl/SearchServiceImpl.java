package ppppp.evernote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import ppppp.evernote.entity.Search;
import ppppp.evernote.mapper.SearchMapper;
import ppppp.evernote.service.SearchService;

@Service
public class SearchServiceImpl extends ServiceImpl<SearchMapper, Search> implements SearchService {

}
