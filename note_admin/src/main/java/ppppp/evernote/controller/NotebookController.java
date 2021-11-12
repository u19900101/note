package ppppp.evernote.controller;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;


import org.springframework.web.client.RestTemplate;
import ppppp.evernote.entity.Notebook;

import ppppp.evernote.entity.Tag;
import ppppp.evernote.service.NotebookService;
import ppppp.evernote.util.ResultUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static ppppp.evernote.util.RequestUtils.sendPostRequest;

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
    private NotebookService notebookService;

    @RequestMapping("/allNoteBooks")
    public String allNotebooks() {
        ArrayList<List<Notebook>> list = new ArrayList<>();
        /*添加原始notebook数据*/
        list.add(notebookService.lambdaQuery().list());

        /*添加封装为tree 的 notebook*/
        list.add(getNoteBooksTree());
        return ResultUtil.successWithData(list);
    }

    @RequestMapping("/insertNoteBook")
    public String insertNoteBook(@RequestBody Notebook notebook) {

        notebook.setCreateTime(new Date());
        Notebook maxSort = notebookService.lambdaQuery().orderByDesc(Notebook::getSort).last("limit 1").list().get(0);
        notebook.setSort(maxSort.getSort()+1);
        boolean save = notebookService.save(notebook);
        Notebook newNoteBook = notebookService.getById(notebook.getId());
        return ResultUtil.successWithData(newNoteBook);
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

        /*获取数据*/
        System.out.println(obj);
        Integer currentId = (Integer) obj.get("currentId");
        Notebook notebook = notebookService.getById(currentId);
        notebook.setUpdateTime(new Date());//设置更新时间
        /*1.级联更新笔记本数量*/
        if(obj.get("pid") != null){
            Integer newPid  = (Integer) obj.get("pid");
            Integer oldPid = (Integer) obj.get("oldPid");
            notebook.setPid(newPid);
            boolean isUpdateNoteBookCountSucceed = updateAncestorsNoteBooks(oldPid,newPid,notebook.getNoteCount());
            if(isUpdateNoteBookCountSucceed) {
                /*2.更新笔记本的pid*/
                /*获取排序的指标值*/
                float sort = getSort(obj);
                notebook.setSort(sort);
            }
            boolean b = notebookService.updateById(notebook);

            //封装tree进行返回
            if(b) return sendPostRequest("http://localhost:8080/admin/noteBook/noteBooksTree");
        }
        if(obj.get("title") != null){
            notebook.setTitle((String) obj.get("title"));
            boolean b = notebookService.updateById(notebook);
            if(b) return ResultUtil.successWithData(b);
        }

        return ResultUtil.errorWithMessage("error");
    }

    private boolean updateAncestorsNoteBooks(Integer oldPid, Integer newPid, int count) {
        // 新笔记本级联 +count
        boolean updateNewNoteBooks = updateNoteCountWrapper(newPid, count);

        //旧笔记本级联 -count
        boolean updateOldNotebook = updateNoteCountWrapper(oldPid, -count);
        boolean isUpdateNoteBookCountSucceed = updateNewNoteBooks && updateOldNotebook;
        return isUpdateNoteBookCountSucceed;
    }

    private boolean updateNoteCountWrapper(Integer noteBookId, int count) {
        RestTemplate restTemplate = new RestTemplate();
        //1. 简单Get请求
        String rootUrl = "http://localhost:8080/admin/note/updateNoteCountWrapper";
        String result = restTemplate.getForObject(rootUrl + "?noteBookId="+noteBookId+"&count="+count, String.class);
       return "true".contentEquals(result);
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

