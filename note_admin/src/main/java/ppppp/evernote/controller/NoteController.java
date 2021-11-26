package ppppp.evernote.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import ppppp.evernote.entity.*;
import ppppp.evernote.service.NoteService;
import ppppp.evernote.service.NotebookService;
import ppppp.evernote.service.SortWayService;
import ppppp.evernote.service.TagService;
import ppppp.evernote.util.ResultUtil;
import ppppp.evernote.util.ftp.FTPUtil;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

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
        if (note.getTagUid() != null) {
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

    // 新建笔记
    @PostMapping("/insert")
    public String insertNote(@RequestBody Note note) {
        // 设置修改时间为当前时间
        note.setCreateTime(new Date());
        note.setTitle("");
        note.setContent("");
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

    /*文件上传*/
    public Map<String, Object> uploadFileAndInsert(HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        List<MultipartFile> fileList = multiRequest.getFiles("file");
        ArrayList<Map<String, Object>> item = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (MultipartFile multipartFile : fileList) {

            if (multipartFile.isEmpty()) {
                map.put("uploaded", -1);
                map.put("errorMessage", "上传失败...");
                return map;
            }
            // 保存到数据库 得到唯一的id  上传的文件名称格式  2021-11-25-fileName-id.type
            // String fileName = insertFile(multipartFile);

            // 上传到服务器
            try {
                Map<String, Object> temp = new HashMap<>();
                byte[] bytes = multipartFile.getBytes();
                String fileName = multipartFile.getName();
                FTPUtil.sshSftp(bytes, fileName);
                temp.put("fileName", fileName);
                temp.put("url", "http://lpgogo.top/img/" + fileName);
                item.add(temp);
            } catch (Exception e) {
                System.out.println("bug");
                map.put("uploaded", -1);
                map.put("errorMessage", "上传失败...");
                return map;
            }
        }
        map.put("uploaded", 1); //"上传成功"
        map.put("item", item);
        return map;
    }

    @PostMapping("/uploadFileAndInsert")
    public void T_(HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        List<MultipartFile> fileList = multiRequest.getFiles("file");
        ArrayList<Map<String, Object>> item = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (MultipartFile multipartFile : fileList) {
            Picture picture = getPictureInfo(multipartFile);
            String uploadDir = new SimpleDateFormat("yyyy-MM-dd").format(picture.getPcreatime());
            FTPUtil.sshSftp(multipartFile.getBytes(), uploadDir + "/" + picture.getPname());
        }

    }

    // 获取文件的信息 保存到数据库 返回文件唯一的名称  2021-11-25-fileName-id.type
    private Picture getPictureInfo(MultipartFile multipartFile) throws Exception {
        long size = multipartFile.getSize();
        // 得到照片的信息 0.照片的名称(规范时间信息)  1.坐标 2.拍摄时间(没有就为当前时间) 3.宽高
        Picture imgInfo = PictureService.getImgInfo(multipartFile.getInputStream(),multipartFile.getOriginalFilename());
        imgInfo.setPsize(size);
        return imgInfo;
    }


    @PostMapping("/uploadFile")
    public Map<String, Object> uploadFile(HttpServletRequest request) {

        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        List<MultipartFile> fileList = multiRequest.getFiles("file");
        ArrayList<Map<String, Object>> item = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (MultipartFile file : fileList) {
            Map<String, Object> temp = new HashMap<>();
            if (file.isEmpty()) {
                map.put("uploaded", -1);
                map.put("errorMessage", "上传失败...");
                return map;
            }

            String fileName = file.getOriginalFilename();
            // String filePath = "C:\\Users\\Administrator\\Desktop\\temp\\";
            // File dest = new File(filePath + fileName);
            try {
                // file.transferTo(dest); // 保存到本地
                byte[] bytes = file.getBytes();
                FTPUtil.sshSftp(bytes, file.getOriginalFilename());
                temp.put("fileName", fileName);
                temp.put("url", "http://lpgogo.top/img/" + fileName);
                item.add(temp);
            } catch (Exception e) {
                System.out.println("bug");
                map.put("uploaded", -1);
                map.put("errorMessage", "上传失败...");
                return map;
            }
        }

        map.put("uploaded", 1); //"上传成功"
        map.put("item", item);
        return map;
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
            if (note.getMediaUid() != null) {
                for (String mediaId : note.getMediaUid().split(",")) {
                    //todo 图片视频表
                    note.getMediaList().add(mediaId);
                }
            }
        }
    }

    private List<Note> getSortWayNotes() {
        // 根据sortway中的排序字段进行查询
        Sortway sortway = sortWayService.getById(1);
        List<Note> noteList = null;

        // 默认均为日期逆序为正常排序
        if (sortway.getCreateTime()) {
            noteList = noteService.lambdaQuery().eq(Note::getWastepaper, false).orderByDesc(Note::getCreateTime).last("limit 10").list();
        } else if (sortway.getUpdateTime()) {
            noteList = noteService.lambdaQuery().eq(Note::getWastepaper, false).orderByDesc(Note::getUpdateTime).last("limit 10").list();
        }
        //逆序
        if (sortway.getReverse()) Collections.reverse(noteList);
        return noteList;
    }

    static class StopMsgException extends RuntimeException {
    }
}

