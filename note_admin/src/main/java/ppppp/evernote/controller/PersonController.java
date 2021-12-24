package ppppp.evernote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ppppp.evernote.entity.Person;
import ppppp.evernote.service.FaceService;
import ppppp.evernote.service.PersonService;
import ppppp.evernote.service.PictureService;
import ppppp.evernote.util.ResultUtil;

import java.util.List;

/**
 * @author lppppp
 * @create 2021-12-22 08:01
 * 人物照片聚合
 */
@RestController
@RequestMapping("/admin/person")
public class PersonController {

    @Autowired
    FaceService faceService;
    @Autowired
    PictureService pictureService;
    @Autowired
    PersonService personService;
    /*将人物聚合封装*/
    @RequestMapping("/allPersons")
    public String getAllPersons() {

        List<Person> allPerson = personService.lambdaQuery().list();

        return ResultUtil.successWithData(allPerson);
    }

    /*修改人物姓名*/
    @PostMapping("/updatePersonName")
    public String updatePersonName(@RequestBody Person person) {
        boolean b = personService.updateById(person);
        return ResultUtil.successWithData(b);
    }
}
