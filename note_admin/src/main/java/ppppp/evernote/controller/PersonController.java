package ppppp.evernote.controller;

import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ppppp.evernote.entity.Face;
import ppppp.evernote.entity.Person;
import ppppp.evernote.mapper.FaceMapper;
import ppppp.evernote.service.FaceService;
import ppppp.evernote.service.PersonService;
import ppppp.evernote.service.PictureService;
import ppppp.evernote.util.ResultUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    FaceMapper faceMapper;
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

    /*todo 事务*/
    /*合并 更新数据库 1.修改所有fromId的 faceNameId为 to 2.增加to的数量 3.去重合并 pictureUid 4.删除from */
    @PostMapping("/mergePerson")
    public String mergePerson(@RequestBody HashMap obj) {

        Integer fromIndex = (Integer) obj.get("fromIndex");
        Integer toIndex = (Integer) obj.get("toIndex");

        //1.修改所有fromId的 faceNameId为 to
        LambdaUpdateChainWrapper<Face> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<>(faceMapper);
        boolean update = lambdaUpdateChainWrapper.eq(Face::getFaceNameId, fromIndex).set(Face::getFaceNameId, toIndex).update();

        //2.增加to的数量
        Person fromPerson = personService.getById(fromIndex);
        Person toPerson = personService.getById(toIndex);
        toPerson.setCount(toPerson.getCount() + fromPerson.getCount());

        //3.去重合并 pictureUid
        String toPersonPictureUid = toPerson.getPictureUid() + fromPerson.getPictureUid();
        List<String> personUids =  new ArrayList<>(Arrays.asList(toPersonPictureUid.split(",")));
        List uniqueList = personUids.stream().distinct().collect(Collectors.toList());
        toPersonPictureUid = uniqueList.toString().substring(1,uniqueList.toString().length()-1).replaceAll(" ","") + ",";
        toPerson.setPictureUid(toPersonPictureUid);
        boolean update1 = personService.updateById(toPerson);

        //3.删除from
        boolean b = personService.removeById(fromIndex);

        return ResultUtil.successWithData(update && update1 && b);
    }
}
