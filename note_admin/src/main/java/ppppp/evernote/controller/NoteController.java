package ppppp.evernote.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ppppp.evernote.entity.Note;
import ppppp.evernote.entity.Notebook;
import ppppp.evernote.entity.Sortway;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.mapper.NoteMapper;
import ppppp.evernote.service.NoteService;
import ppppp.evernote.service.NotebookService;
import ppppp.evernote.service.SortWayService;
import ppppp.evernote.service.TagService;
import ppppp.evernote.util.ResultUtil;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ppppp.evernote.util.RequestUtils.sendPostRequest;

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

    @Autowired
    private SortWayService sortWayService;

    @Autowired
    private NoteMapper noteMapper;

    @RequestMapping("/allNotes")
    public String getAllNotes() {
        List<Note> noteList = getSortWayNotes();
        fillNoteList(noteList);
        return ResultUtil.successWithData(noteList);
    }

    @RequestMapping("/getWastepaperNotes")
    public String getWastepaperNotes() {
        List<Note> noteList = noteService.lambdaQuery().eq(Note::getWastepaper, true).list();
        fillNoteList(noteList);
        return ResultUtil.successWithData(noteList);
    }


    // 1.更新笔记内容 2.移动笔记  3.从废纸篓恢复笔记
    @PostMapping("/update")
    public String updateNote(@RequestBody Note note) {
        boolean isUpdateNoteBookCountSucceed = true;
        boolean isWastepaperSucceed = true;
        int oldPid = noteService.getById(note.getId()).getPid();
        // 设置修改时间为当前时间
        note.setUpdateTime(new Date());
        // 更新 笔记
        boolean b = noteService.updateById(note);

        // 更新 笔记本  携带 pid 则表示移动笔记
        if (note.getPid() != null && note.getWastepaper() == null) {
            // 新笔记本级联 +1
            boolean updateNewNoteBooks = updateNoteCountWrapper(note.getPid(), 1);

            //旧笔记本级联 -1
            Notebook OldNotebook = notebookService.getById(oldPid);
            boolean updateOldNotebook = updateNoteCountWrapper(OldNotebook.getId(), -1);
            isUpdateNoteBookCountSucceed = updateNewNoteBooks && updateOldNotebook;

            //封装tree进行返回
            return sendPostRequest("http://localhost:8080/admin/noteBook/noteBooksTree");
        }
        // 收藏数量 +1  -1
        if (note.getStar() != null) {
            int changeCount = note.getStar() ? 1 : -1;
            Notebook notebook = notebookService.getById(1);
            notebook.setNoteCount(notebook.getNoteCount() + changeCount);
            notebookService.updateById(notebook);
        }

        // 从废纸篓恢复笔记  1.级联修改相关笔记本的数量 +1  2.废纸篓 -1
        if (note.getWastepaper() != null && note.getWastepaper() == false) {
            isUpdateNoteBookCountSucceed = updateNoteCountWrapper(note.getPid(), 1);
            // 修改废纸篓的数量 -1
            Notebook wastepaperNotebook = notebookService.getById(2);
            isWastepaperSucceed = notebookService.updateById(wastepaperNotebook.setNoteCount(wastepaperNotebook.getNoteCount() - 1));

        }
        return ResultUtil.successWithData(b && isUpdateNoteBookCountSucceed && isWastepaperSucceed);
    }

    /* 包含 1.状态修改 2.逻辑删除 */
    @PostMapping("/deleteNote")
    public String deleteNote(@RequestBody Note note) {

        note = noteService.getById(note.getId());
        // 1.更新 标签数量
        if (note.getTagUid() != null && note.getTagUid().length() > 2) {
            // 新笔记本 +1
            String[] tagIdList = note.getTagUid().split(",");
            for (String tagId : tagIdList) {
                Tag tag = tagService.getById(tagId);
                tag.setNoteCount(tag.getNoteCount() - 1);
                tagService.updateById(tag);
            }
        }

        boolean isLogicalDelete, isWastepaperSucceed;
        boolean isUpdateNoteBookCountSucceed = true;
        // 3.逻辑删除和状态修改
        Notebook wastepaperNotebook = notebookService.getById(2);
        if (note.getWastepaper()) {
            //  逻辑删除
            isLogicalDelete = noteService.removeById(note.getId());
            // 修改废纸篓的数量 -1
            isWastepaperSucceed = notebookService.updateById(wastepaperNotebook.setNoteCount(wastepaperNotebook.getNoteCount() - 1));
        } else { // 状态修改
            // 2.更新 noteBook的数量
            isUpdateNoteBookCountSucceed = updateNoteCountWrapper(note.getPid(), -1);
            note.setWastepaper(true);
            isLogicalDelete = noteService.updateById(note);
            // 修改废纸篓的数量 +1
            isWastepaperSucceed = notebookService.updateById(wastepaperNotebook.setNoteCount(wastepaperNotebook.getNoteCount() + 1));
        }
        if (isUpdateNoteBookCountSucceed && isLogicalDelete && isWastepaperSucceed) {
            return sendPostRequest("http://localhost:8080/admin/noteBook/noteBooksTree");
        }
        return ResultUtil.errorWithMessage("error");
    }

    // 清空废纸篓
    @PostMapping("/clearAllWasteNotes")
    public String clearAllWasteNotes() {
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("wastepaper", 1);
        boolean removeAll = noteService.remove(queryWrapper);
        return ResultUtil.successWithData(removeAll);
    }

    // 恢复所有废纸篓中的笔记
    @PostMapping("/recoverAllNotes")
    public String recoverAllNotes() {
        LambdaUpdateChainWrapper<Note> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<>(noteMapper);
        boolean update = lambdaUpdateChainWrapper.eq(Note::getWastepaper, 1).set(Note::getWastepaper, 0).update();
        return ResultUtil.successWithData(update);
    }

    // 新建笔记
    @PostMapping("/insert")
    public String insertNote(@RequestBody Note note) {
        // 设置修改时间为当前时间
        note.setCreateTime(new Date());
        note.setTitle("标题");
        note.setContent("# 标题 \n内容");
        note.setSummary("新建内容");
        noteService.save(note);
        Note newNote = noteService.getById(note.getId());

        // 给 所在笔记本和所有父节点的数量 + 1
        boolean isSucceed = updateNoteCountWrapper(note.getPid(), 1);

        return ResultUtil.successWithData(newNote);

    }

    @RequestMapping("/updateNoteCountWrapper")
    public boolean updateNoteCountWrapper(int noteBookId, int count) {
        boolean isSucceed = true;
        try {
            updateNoteCount(noteBookId, count);
        } catch (StopMsgException e) {
            isSucceed = false;
        }
        return isSucceed;
    }



    /*级联更新所在笔记本以及所有的父笔记本*/
    public void updateNoteCount(int noteBookId, int count) {
        Notebook notebook = notebookService.getById(noteBookId);
        notebook.setNoteCount(notebook.getNoteCount() + count);
        boolean updateNoteBook = notebookService.updateById(notebook);
        if (!updateNoteBook) {
            throw new StopMsgException();
        }
        if (notebook.getPid() != null) {
            updateNoteCount(notebook.getPid(), count);
        }
    }

    private void fillNoteList(List<Note> noteList) {
        for (Note note : noteList) {
            if (note.getTagUid() != null && note.getTagUid().length() > 1) {
                for (String tagId : note.getTagUid().split(",")) {
                    Tag tag = tagService.getById(tagId);
                    note.getTagList().add(tag);
                }
            }
            /*从content中提取 封面url和summary*/

            String content = note.getContent();
            int i = content.indexOf('\n') + 1; //去掉标题
            String replaceAll = content.substring(i).replaceAll("\\<img.*?\\>|\\<video.*?video\\>|\\n","");
            int end = 50 > replaceAll.length() ? replaceAll.length() : 50;
            String summary = replaceAll.substring(0, end);
            note.setSummary(summary);

            Pattern p = Pattern.compile("\\<img.*?\\>");
            Matcher m = p.matcher(content);
            while (m.find()) {
                String sub = content.substring(m.start(), m.end());
                System.out.println(sub);
                String s = sub.substring(sub.indexOf("\"") + 1);
                String url = s.substring(0, s.indexOf("\""));
                note.setMediaUid(url);
                break;
            }
        }
    }

    private List<Note> getSortWayNotes() {
        // 根据sortway中的排序字段进行查询
        Sortway sortway = sortWayService.getById(1);
        List<Note> noteList = null;

        // 默认均为日期逆序为正常排序
        if (sortway.getCreateTime()) {
            noteList = noteService.lambdaQuery().eq(Note::getWastepaper, false).orderByDesc(Note::getCreateTime).list();/*.last("limit 10")*/
        } else if (sortway.getUpdateTime()) {
            noteList = noteService.lambdaQuery().eq(Note::getWastepaper, false).orderByDesc(Note::getUpdateTime).list();
        }
        //逆序
        if (sortway.getReverse()) {
            Collections.reverse(noteList);
        }
        return noteList;
    }

    static class StopMsgException extends RuntimeException {
    }
}

