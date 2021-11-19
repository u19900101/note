package ppppp.evernote;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ppppp.evernote.entity.Note;
import ppppp.evernote.entity.Sortway;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.service.NoteService;
import ppppp.evernote.service.SortWayService;
import ppppp.evernote.service.TagService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pppppp
 * @date 2021/11/19 23:02
 * 手动同步标签数量
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class SyncTag {
    @Autowired
    NoteService noteService;

    @Autowired
    TagService tagService;
    @Test
    public void T_sync(){
        List<Tag> tagList = tagService.lambdaQuery().list();
        List<Note> allNotes = noteService.lambdaQuery().list();
        for (Tag tag : tagList) {
            /*得到所有的子id*/
            ArrayList<Integer> tagIds = new ArrayList<>();
            getAllChildernTagIds(tag, tagIds);
            /*遍历所有的笔记 计算出包含该标签及其子标签的笔记数量*/
            int count = 0;
            for (Note note : allNotes) {
                if (note.getTagUid() != null && note.getTagUid().length() > 1) {
                    /*判断两者是否有交集*/
                    if (isIntersection(note.getTagUid(), tagIds).size() > 0) count++;
                }
            }
            tag.setNoteCount(count);
            tagService.updateById(tag);
        }
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

    private void getAllChildernTagIds(Tag tag, ArrayList<Integer> tagIds) {
        List<Tag> son = tagService.lambdaQuery()
                .eq(Tag::getPid, tag.getId())
                .list();
        /*加入id*/
        tagIds.add(tag.getId());
        if (son.size() > 0) {
            for (Tag sonTag : son) {
                getAllChildernTagIds(sonTag, tagIds);
            }
        }
    }
}