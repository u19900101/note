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

        List<Notebook> notebooksList = notebookService.lambdaQuery().orderByAsc(Notebook::getId).list();
        //notebooks.forEach(System.out::println);
        return ResultUtil.successWithData(notebooksList);
    }


}

