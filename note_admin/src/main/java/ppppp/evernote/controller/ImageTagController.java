package ppppp.evernote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ppppp.evernote.entity.ImageTag;
import ppppp.evernote.entity.Picture;
import ppppp.evernote.service.ImageTagService;
import ppppp.evernote.service.PictureService;
import ppppp.evernote.util.ResultUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/admin/imageTag")
public class ImageTagController {
    @Autowired
    ImageTagService imageTagService;
    @Autowired
    PictureService pictureService;

    @RequestMapping("/getImageTags")
    public String getImageTags() {
        List<List<ImageTag>> tags = new ArrayList<>();
        tags.add(getTagTree());
        /*封装单条数据*/
        tags.add(imageTagService.lambdaQuery().list());
        return ResultUtil.successWithData(tags);
    }

    @RequestMapping("/tagsTree")
    public String getTagsTree() {
        return ResultUtil.successWithData(getTagTree());
    }

    private List<ImageTag> getTagTree() {
        List<ImageTag> tagTree = imageTagService.lambdaQuery()
                .eq(ImageTag::getPid, 0)
                .orderByAsc(ImageTag::getSort)
                .list();
        for (ImageTag tag : tagTree) {
            getChildren(tag);
        }
        return tagTree;
    }

    // 新建
    @PostMapping("/insertImageTag")
    public String insertImageTag(@RequestBody ImageTag tag) {
        // 设置修改时间为当前时间
        tag.setCreateTime(new Date());
        /*找到最大sort值赋值给新的tag，不然会出现新建多个tag后无法有效记录排序*/
        ImageTag maxSortTag = imageTagService.lambdaQuery().orderByDesc(ImageTag::getSort).last("limit 1").list().get(0);
        tag.setSort(maxSortTag.getSort() + 1);
        imageTagService.save(tag);
        ImageTag newTag = imageTagService.getById(tag.getId());

        return ResultUtil.successWithData(newTag);
    }

    @PostMapping("/deleteImageTag")
    public String deleteImageTag(@RequestBody ImageTag tag) {

        /*1.删除笔记中相关的标签id*/
        /*得到所有的子id*/
        ArrayList<Integer> tagIds = new ArrayList<>();
        getAllChildernTagIds(tag, tagIds);
        List<Picture> allPictures = pictureService.lambdaQuery().select(Picture::getId,Picture::getTagUid).list();
        for (Picture picture : allPictures) {
            String tagUid = picture.getTagUid();
            if (tagUid == null || tagUid.length() < 2) {
                continue;
            }
            /*找到两者交集 替换为 '' */
            ArrayList<Integer> intersection = isIntersection(tagUid, tagIds);
            if (intersection.size() > 0) {
                for (Integer tagId : intersection) {
                    tagUid = tagUid.replace(tagId + ",", "");
                }
                picture.setTagUid(tagUid);
                pictureService.updateById(picture);
            }
        }

        /*2.更新父标签数量*/
        tag = imageTagService.getById(tag.getId());
        if (tag.getPid() != null) {
            updateAncestorsTags(0, tag.getPid());
        }

        /*3.级联删除标签*/
        for (Integer tagId : tagIds) {
            imageTagService.removeById(tagId);
        }


        return getImageTags();
    }


    @PostMapping("/updateImageTags")
    public String updateImageTags(@RequestBody HashMap obj) {
        Integer currentId = (Integer) obj.get("currentId");
        ImageTag tag = imageTagService.getById(currentId);
        tag.setUpdateTime(new Date());//设置更新时间

        /*只更新笔记标题*/
        if (obj.get("title") != null) {
            tag.setTitle((String) obj.get("title"));
            boolean b = imageTagService.updateById(tag);
            if (b) return ResultUtil.successWithData(b);
        }

        Integer newPid = (Integer) obj.get("pid");
        Integer oldPid = (Integer) obj.get("oldPid");

        /*1.更新笔记本的pid*/
        /*获取排序的指标值*/
        if (obj.get("preId") != null) {
            tag.setPid(newPid);
            float sort = getSort(obj);
            tag.setSort(sort);
            boolean b = imageTagService.updateById(tag);
        }

        /*2.根据新的层级关系 级联更新tag数量*/
        boolean isUpdateTagCountSucceed = updateAncestorsTags(oldPid, newPid);
        //封装tree进行返回
        if (isUpdateTagCountSucceed) {
            return ResultUtil.successWithData(getTagTree());
        }
        return ResultUtil.errorWithMessage("error");
    }

    private boolean updateAncestorsTags(Integer oldPid, Integer newPid) {
        boolean isSucceed = true;
        try {
            // 级联新标签 重新计算数量
            // newPid = 0  表示为一级tag 没有父节点
            if (newPid != 0) {
                updateTagNoteCount(newPid);
            }

            //级联旧标签 重新计算数量
            if (oldPid != 0) {
                updateTagNoteCount(oldPid);
            }
        } catch (NoteController.StopMsgException e) {
            isSucceed = false;
        }
        return isSucceed;
    }

    private void updateTagNoteCount(Integer tagId) {
        updateTagCountById(tagId);
        ImageTag tag = imageTagService.getById(tagId);
        /*父节点不为一级节点时 级联更新*/
        if (tag.getPid() != 0) {
            updateTagNoteCount(tag.getPid());
        }
    }


    /*级联所在标签以及所有的父标签*/
    public void updateTagCountById(int tagId) {
        ImageTag tag = imageTagService.getById(tagId);
        /*得到所有的子id*/
        ArrayList<Integer> tagIds = new ArrayList<>();
        getAllChildernTagIds(tag, tagIds);

        /*遍历所有的笔记 计算出包含该标签及其子标签的笔记数量*/
        int count = 0;
        List<Picture> allPictures = pictureService.lambdaQuery().select(Picture::getId,Picture::getTagUid).list();
        for (Picture picture : allPictures) {
            if (picture.getTagUid() != null && picture.getTagUid().length() > 1) {
                /*判断两者是否有交集*/
                if (isIntersection(picture.getTagUid(), tagIds).size() > 0) {
                    count++;
                }
            }
        }
        tag.setNoteCount(count);
        imageTagService.updateById(tag);
    }


    public ArrayList<Integer> isIntersection(String idsStr, ArrayList<Integer> tagIds) {
        ArrayList<Integer> temp = new ArrayList<>();
        ArrayList<Integer> temp2 = new ArrayList<>();
        temp2.addAll(tagIds);

        for (String s : idsStr.split(",")) {
            temp.add(Integer.parseInt(s));
        }
        temp2.retainAll(temp);
        return temp2;
    }

    private void getAllChildernTagIds(ImageTag tag, ArrayList<Integer> tagIds) {
        List<ImageTag> son = imageTagService.lambdaQuery()
                .eq(ImageTag::getPid, tag.getId())
                .list();
        /*加入id*/
        tagIds.add(tag.getId());
        if (son.size() > 0) {
            for (ImageTag sonTag : son) {
                getAllChildernTagIds(sonTag, tagIds);
            }
        }
    }


    public float getSort(HashMap obj) {

        float preSort = -1, nextSort = -1, sort = 1;

        Integer preId = (Integer) obj.get("preId");
        if (preId != 0) {
            preSort = imageTagService.getById(preId).getSort();
        }
        Integer nextId = (Integer) obj.get("nextId");
        if (nextId != 0) {
            nextSort = imageTagService.getById(nextId).getSort();
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

    public void getChildren(ImageTag tag) {
        List<ImageTag> son = imageTagService.lambdaQuery()
                .eq(ImageTag::getPid, tag.getId())
                .orderByAsc(ImageTag::getSort)
                .list();
        if (son.size() > 0) {
            for (ImageTag sonTag : son) {
                getChildren(sonTag);
            }
        }
        tag.setChildren(son);
    }
}

