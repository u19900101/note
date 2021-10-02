package ppppp.evernote.controller;


import com.sun.javafx.binding.ObjectConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ppppp.evernote.entity.Note;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.service.NoteService;
import ppppp.evernote.util.ResultUtil;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 笔记 前端控制器
 * </p>
 *
 * @author ppppp
 * @since 2021-09-26
 */
@RestController
@RequestMapping("/admin/note")
public class NoteController {
    @Autowired
    private NoteService noteService;

    @RequestMapping("/allNotes")
    public String getAllNotes() {
        List<Note> noteList = noteService.lambdaQuery().list();
        for (Note note : noteList) {

            if(note.getTagUid() != null){
                for (String tag : note.getTagUid().split(",")) {
                    note.getTagList().add(tag);
                }
            }
            if(note.getMediaUid() != null){
                for (String mediaId : note.getMediaUid().split(",")) {
                    //todo 图片视频表
                    note.getMediaList().add(mediaId);
                }
            }

        }
        return ResultUtil.successWithData(noteList);
    }



    @PostMapping("/update")
    public String updateNote(@RequestBody Note note) {
        // 设置修改时间为当前时间
        note.setUpdateTime(new Date());
        boolean b = noteService.updateById(note);

        Note byId = noteService.getById(note.getId());
        return ResultUtil.successWithData(byId);

    }

    @PostMapping("/insert")
    public String insertNote(@RequestBody Note note) {
        // 设置修改时间为当前时间
        note.setCreateTime(new Date());
        noteService.save(note);
        Note byId = noteService.getById(note.getId());
        return ResultUtil.successWithData(byId);

    }
}

