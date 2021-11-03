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

import java.util.Date;
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

        List<Notebook> notebooksList = notebookService.lambdaQuery()
                .eq(Notebook::getPid, 0)
                .orderByDesc(Notebook::getUpdateTime)
                .list();
        for (Notebook notebook : notebooksList) {
            getChildren(notebook);
        }

        return ResultUtil.successWithData(notebooksList);
    }


    @PostMapping("/updateNotebook")
    public String updateNotebook(@RequestBody Notebook notebook) {
        notebook.setUpdateTime(new Date());//设置更新时间
        boolean b = notebookService.updateById(notebook);
        return ResultUtil.successWithData(b);
    }

    public void getChildren(Notebook notebook) {
        List<Notebook> son = notebookService.lambdaQuery().eq(Notebook::getPid, notebook.getId()).list();
        if (son.size() > 0) {
            for (Notebook sonNotebook : son) {
                getChildren(sonNotebook);
            }
        }
        notebook.setChildren(son);
    }
}

