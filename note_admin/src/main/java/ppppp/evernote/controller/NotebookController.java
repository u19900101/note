package ppppp.evernote.controller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;


import ppppp.evernote.entity.Notebook;
import ppppp.evernote.mapper.NotebookMapper;
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
@RequestMapping("/admin")
public class NotebookController {
    @Autowired
    private NotebookMapper notebookMapper;


    @GetMapping("/notebook")
    public String getList() {

        List<Notebook> notebooksList = notebookMapper.selectList(null);
        //notebooks.forEach(System.out::println);
        return ResultUtil.successWithData(notebooksList);
    }
}

