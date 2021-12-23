package ppppp.evernote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import ppppp.evernote.entity.Person;
import ppppp.evernote.mapper.PersonMapper;
import ppppp.evernote.service.PersonService;

@Service
public class PersonServiceImpl extends ServiceImpl<PersonMapper, Person> implements PersonService {

}
