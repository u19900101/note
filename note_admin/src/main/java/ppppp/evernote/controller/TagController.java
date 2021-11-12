package ppppp.evernote.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import ppppp.evernote.entity.Note;
import ppppp.evernote.entity.Notebook;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.service.NoteService;
import ppppp.evernote.service.TagService;
import ppppp.evernote.util.ResultUtil;

import java.util.*;
import java.util.stream.Collectors;

import static jdk.nashorn.internal.objects.NativeString.substring;
import static jdk.nashorn.internal.runtime.ScriptObject.setGlobalObjectProto;
import static ppppp.evernote.util.RequestUtils.sendPostRequest;

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
    @Autowired
    NoteService noteService;

    @RequestMapping("/allTags")
    public String getAllTags() {
        List<List<Tag>> tags = new ArrayList<>();
        tags.add(getTagTree());
        /*封装单条数据*/
        tags.add(tagService.lambdaQuery().list());
        return ResultUtil.successWithData(tags);
    }

    @RequestMapping("/tagsTree")
    public String getTagsTree() {
        return ResultUtil.successWithData(getTagTree());
    }

    private List<Tag> getTagTree() {
        List<Tag> tagTree = tagService.lambdaQuery()
                .eq(Tag::getPid, 0)
                .orderByAsc(Tag::getSort)
                .list();
        for (Tag tag : tagTree) {
            getChildren(tag);
        }
        return tagTree;
    }

    // 新建笔记
    @PostMapping("/insert")
    public String insertTag(@RequestBody Tag tag) {
        // 设置修改时间为当前时间
        tag.setCreateTime(new Date());
        /*找到最大sort值赋值给新的tag，不然会出现新建多个tag后无法有效记录排序*/
        Tag  maxSortTag = tagService.lambdaQuery().orderByDesc(Tag::getSort).last("limit 1").list().get(0);
        tag.setSort(maxSortTag.getSort()+1);
        tagService.save(tag);
        Tag newTag = tagService.getById(tag.getId());
        return ResultUtil.successWithData(newTag);
    }

    @PostMapping("/updateTag")
    public String updateTag(@RequestBody HashMap obj) {
        Integer currentId = (Integer) obj.get("currentId");
        Tag tag = tagService.getById(currentId);
        tag.setUpdateTime(new Date());//设置更新时间

        /*只更新笔记标题*/
        if(obj.get("title") != null){
            tag.setTitle((String) obj.get("title"));
            boolean b = tagService.updateById(tag);
            if(b) return ResultUtil.successWithData(b);
        }

        Integer newPid = (Integer) obj.get("pid");
        Integer oldPid = (Integer) obj.get("oldPid");

        /*1.更新笔记本的pid*/
        /*获取排序的指标值*/
        if(obj.get("preId") != null){
            tag.setPid(newPid);
            float sort = getSort(obj);
            tag.setSort(sort);
            boolean b = tagService.updateById(tag);
        }

        /*2.根据新的层级关系 级联更新tag数量*/
        boolean isUpdateTagCountSucceed = updateAncestorsTags(oldPid, newPid);
        //封装tree进行返回
        if (isUpdateTagCountSucceed) return ResultUtil.successWithData(getTagTree());
        return ResultUtil.errorWithMessage("error");
    }

    private boolean updateAncestorsTags(Integer oldPid, Integer newPid) {
        boolean isSucceed = true;
        try {
            // 级联新标签 重新计算数量
            // newPid = 0  表示为一级tag 没有父节点
            if (newPid != 0) updateTagNoteCount(newPid);

            //级联旧标签 重新计算数量
            if (oldPid != 0) updateTagNoteCount(oldPid);
        } catch (NoteController.StopMsgException e) {
            isSucceed = false;
        }
        return isSucceed;
    }

    private void updateTagNoteCount(Integer tagId) {
        updateTagCountById(tagId);
        Tag tag = tagService.getById(tagId);
        /*父节点不为一级节点时 级联更新*/
        if (tag.getPid() != 0) updateTagNoteCount(tag.getPid());
    }


    /*级联所在标签以及所有的父标签*/
    public void updateTagCountById(int tagId) {
        Tag tag = tagService.getById(tagId);
        /*得到所有的子id*/
        ArrayList<Integer> tagIds = new ArrayList<>();
        getAllChildernTagIds(tag, tagIds);

        /*遍历所有的笔记 计算出包含该标签及其子标签的笔记数量*/
        int count = 0;
        List<Note> allNotes = noteService.lambdaQuery().list();
        for (Note note : allNotes) {
            if (note.getTagUid() != null && note.getTagUid().length() > 1) {
                /*判断两者是否有交集*/
                if (isIntersection(note.getTagUid(),tagIds)) count++;
            }
        }
        tag.setNoteCount(count);
        tagService.updateById(tag);
    }


    public boolean isIntersection(String idsStr, ArrayList<Integer> tagIds) {
        ArrayList<Integer> temp = new ArrayList<>();
        ArrayList<Integer> temp2 = new ArrayList<>();
        temp2.addAll(tagIds);

        for (String s : idsStr.split(",")) {
            temp.add(Integer.parseInt(s));
        }
        temp2.retainAll(temp);
        return temp2.size() > 0;
    }

    private void getAllChildernTagIds(Tag tag, ArrayList<Integer> tagIds) {
        List<Tag> son = tagService.lambdaQuery()
                .eq(Tag::getPid, tag.getId())
                .list();
        /*加入id*/
        tagIds.add(tag.getId());
        if (son.size() > 0) {
            for (Tag sonTag : son) {
                getAllChildernTagIds(sonTag,tagIds);
            }
        }
    }


    public float getSort(HashMap obj) {

        float preSort = -1, nextSort = -1, sort = 1;

        Integer preId = (Integer) obj.get("preId");
        if (preId != 0) {
            preSort = tagService.getById(preId).getSort();
        }
        Integer nextId = (Integer) obj.get("nextId");
        if (nextId != 0) {
            nextSort = tagService.getById(nextId).getSort();
        }

        /*中间*/
        if (preId != 0 && nextId != 0) {
            sort = (preSort + nextSort) / 2;
        } else if (preId == 0 && nextId != 0) { /*第一*/
            sort = nextSort / 2;
        } else if (preId != 0 && nextId == 0) {  /*最后*/
            sort = preSort + 1;
        } else {  /*前后都为空 则表示拖拽到的父节点为叶子节点*/
            //sort = 1;
        }
        return sort;
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

