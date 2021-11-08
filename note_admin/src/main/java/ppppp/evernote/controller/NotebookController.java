package ppppp.evernote.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;


import ppppp.evernote.entity.Note;
import ppppp.evernote.entity.Notebook;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.mapper.NoteMapper;
import ppppp.evernote.mapper.NotebookMapper;
import ppppp.evernote.mapper.TagMapper;
import ppppp.evernote.service.NotebookService;
import ppppp.evernote.util.ResultUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 笔记本 前端控制器
 * </p>
 *
 * @author ppppp
 * @since 2021-09-25
 */

@RestController
@RequestMapping("/admin/noteBook")
public class NotebookController {
    @Autowired
    private NotebookMapper notebookMapper;

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private TagMapper tagMapper;

    @RequestMapping("/allNoteBooks")
    public String allNotebooks() {
        ArrayList<List<Notebook>> list = new ArrayList<>();
        /*添加原始notebook数据*/
        list.add(notebookService.lambdaQuery().list());

        /*添加封装为tree 的 notebook*/
        list.add(getNoteBooksTree());
        return ResultUtil.successWithData(list);
    }

    /*添加封装为tree 的 notebook*/
    @RequestMapping("/noteBooksTree")
    public String noteBooksTree() {
        return ResultUtil.successWithData(getNoteBooksTree());
    }


    public List<Notebook> getNoteBooksTree() {
        List<Notebook> notebooksList = notebookService.lambdaQuery()
                .eq(Notebook::getPid, 0)
                .orderByAsc(Notebook::getSort)
                .list();
        for (Notebook notebook : notebooksList) {
            getChildren(notebook);
        }
        return notebooksList;
    }


    @PostMapping("/updateNotebook")
    public String updateNotebook(@RequestBody HashMap obj) {
        System.out.println(obj);
        Integer currentId = (Integer) obj.get("currentId");
        Notebook notebook = notebookService.getById(currentId);
        Integer pid = (Integer) obj.get("pid");
        notebook.setPid(pid);

        float sort = getSort(obj);

        notebook.setSort(sort);
        notebook.setUpdateTime(new Date());//设置更新时间
        boolean b = notebookService.updateById(notebook);
        return ResultUtil.successWithData(b);
    }
    public float getSort(HashMap obj) {
        float preSort = -1, nextSort = -1,sort = 1;

        Integer preId = (Integer) obj.get("preId");
        if(preId != 0){
            preSort = notebookService.getById(preId).getSort();
        }
        Integer nextId = (Integer) obj.get("nextId");
        if(nextId != 0){
            nextSort = notebookService.getById(nextId).getSort();
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
        return sort;
    }
    //todo 泛型优化一下？
    public void getChildren(Notebook notebook) {
        List<Notebook> son = notebookService.lambdaQuery()
                .eq(Notebook::getPid, notebook.getId())
                .orderByAsc(Notebook::getSort)
                .list();
        if (son.size() > 0) {
            for (Notebook sonNotebook : son) {
                getChildren(sonNotebook);
            }
        }
        notebook.setChildren(son);
    }


}

