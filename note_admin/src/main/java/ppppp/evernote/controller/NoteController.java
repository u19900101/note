package ppppp.evernote.controller;


import com.sun.javafx.binding.ObjectConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ppppp.evernote.entity.Note;
import ppppp.evernote.entity.Notebook;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.service.NoteService;
import ppppp.evernote.service.NotebookService;
import ppppp.evernote.service.TagService;
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

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private TagService tagService;

    @RequestMapping("/allNotes")
    public String getAllNotes() {
        List<Note> noteList = noteService.lambdaQuery().orderByDesc(Note::getId).list();
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

    // 更新笔记内容 移动笔记
    @PostMapping("/update")
    public String updateNote(@RequestBody Note note) {
        // 设置修改时间为当前时间
        note.setUpdateTime(new Date());


        // 更新 笔记本  携带 pid 则表示移动笔记
        if(note.getPid() != null){
            // 新笔记本 +1
            Notebook notebook = notebookService.getById(note.getPid());
            notebook.setNoteCount(notebook.getNoteCount() +1);
            notebookService.updateById(notebook);

            //旧笔记本 -1
            notebook = notebookService.getById( noteService.getById(note.getId()).getPid());
            notebook.setNoteCount(notebook.getNoteCount() -1);
            notebookService.updateById(notebook);
        }
        // 收藏数量 +1  -1
        if(note.getStar() != null){
            int changeCount = note.getStar() ? 1:-1;
            Notebook notebook = notebookService.getById(1);
            notebook.setNoteCount(notebook.getNoteCount() + changeCount);
            notebookService.updateById(notebook);
        }
        // 更新 笔记
        boolean b = noteService.updateById(note);

        Note byId = noteService.getById(note.getId());
        return ResultUtil.successWithData(byId);
    }

    // 新建笔记
    @PostMapping("/insert")
    public String insertNote(@RequestBody Note note) {
        // 设置修改时间为当前时间
        note.setCreateTime(new Date());
        note.setTitle("");
        note.setContent("");
        noteService.save(note);
        Note byId = noteService.getById(note.getId());

        // 给 所在笔记本的数量 + 1
        Notebook notebook = notebookService.getById(note.getPid());
        notebook.setNoteCount(notebook.getNoteCount() + 1);
        boolean updateNoteBook = notebookService.updateById(notebook);
        return ResultUtil.successWithData(byId);

    }

    // todo delete 请求？？
    @PostMapping("/delete")
    public String deleteNote(@RequestBody Note note) {

        note = noteService.getById(note.getId());
        // 1.更新 标签数据
        if(note.getTagUid() != null){
            // 新笔记本 +1
            String[] tagIdList = note.getTagUid().split(",");
            for (String tagId : tagIdList) {
                Tag tag = tagService.getById(tagId);
                tag.setNoteCount(tag.getNoteCount() -1);
                tagService.updateById(tag);
            }
        }

        // 2.更新 noteBook的数量
        Notebook notebook = notebookService.getById(note.getPid());
        notebook.setNoteCount(notebook.getNoteCount() - 1);
        boolean isNoteBookUpdated = notebookService.updateById(notebook);

        // 3.逻辑删除note
        boolean isLogicalDelete = noteService.removeById(note);
        System.out.println(ResultUtil.successWithData(isNoteBookUpdated && isLogicalDelete));
        return ResultUtil.successWithData(isLogicalDelete);
    }
}

