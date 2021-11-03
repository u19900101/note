package ppppp.evernote.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import ppppp.evernote.entity.Note;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.service.TagService;
import ppppp.evernote.util.ResultUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 标签表 前端控制器
 * </p>
 *
 * @author ppppp
 * @since 2021-09-26
 */
@RestController
@RequestMapping("/admin/tag")
public class TagController {
    @Autowired
    TagService tagService;
    @RequestMapping("/allTags")
    public String getAllTags() {
         List<Tag> tagList = tagService.lambdaQuery()
                .eq(Tag::getPid, 0)
                .orderByAsc(Tag::getSort)
                .list();
        for (Tag tag : tagList) {
            getChildren(tag);
        }
        return ResultUtil.successWithData(tagList);
    }

    @PostMapping("/updateTag")
    public String updateTag(@RequestBody HashMap obj) {
        System.out.println(obj);
        Integer currentId = (Integer) obj.get("currentId");
        Tag Tag = tagService.getById(currentId);
        Integer pid = (Integer) obj.get("pid");
        Tag.setPid(pid);

        float preSort = -1, nextSort = -1,sort = 1;

        Integer preId = (Integer) obj.get("preId");
        if(preId != 0  ){
            preSort = tagService.getById(preId).getSort();
        }
        Integer nextId = (Integer) obj.get("nextId");
        if(nextId != 0){
            nextSort = tagService.getById(nextId).getSort();
        }

        /*中间*/
        if(preId != 0 && nextId != 0){
            sort = (preSort + nextSort)/2;
        }else if(preId == 0 && nextId != 0){ /*第一*/
            sort = nextSort/2;
        }else if(preId != 0 && nextId == 0){  /*最后*/
            sort = preSort + 1;
        }else {  /*前后都为空 则表示拖拽到的父节点为叶子节点*/
            //sort = 1;
        }

        Tag.setSort(sort);
        Tag.setUpdateTime(new Date());//设置更新时间
        boolean b = tagService.updateById(Tag);
        return ResultUtil.successWithData(b);
    }
    
    public void getChildren(Tag tag) {
        List<Tag> son = tagService.lambdaQuery()
                .eq(Tag::getPid, tag.getId())
                .orderByAsc(Tag::getSort)
                .list();
        if (son.size() > 0) {
            for (Tag sonTag : son) {
                getChildren(sonTag);
            }
        }
        tag.setChildren(son);
    }
}

