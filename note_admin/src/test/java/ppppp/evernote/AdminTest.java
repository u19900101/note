package ppppp.evernote;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ppppp.evernote.entity.Note;
import ppppp.evernote.entity.Notebook;
import ppppp.evernote.entity.Picture;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.mapper.NoteMapper;
import ppppp.evernote.service.NoteService;
import ppppp.evernote.service.NotebookService;
import ppppp.evernote.service.PictureService;
import ppppp.evernote.service.TagService;
import ppppp.evernote.util.ResultUtil;

import java.util.Date;
import java.util.List;

import static ppppp.evernote.util.RequestUtils.sendGetRequest;


@RunWith(SpringRunner.class)
@SpringBootTest
public class AdminTest {
    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private PictureService pictureService;

    @Test  /*lambdaQuery 会查询出逻辑删除*/
    public void 交换pic经纬度(){
        List<Picture> all = pictureService.lambdaQuery().list();
        for (Picture picture : all) {
            if(picture.getLnglat() != null){
                String[] split = picture.getLnglat().split(",");
                if(split.length > 0 ){
                    System.out.println("kkk");
                    picture.setLnglat(split[1] + "," + split[0]);
                    pictureService.updateById(picture);
                }
            }

        }
    }

    @Test  /*lambdaQuery 会查询出逻辑删除*/
    public void 更新地址(){
        List<Picture> all = pictureService.lambdaQuery().list();
        for (Picture picture : all) {
            if((picture.getLocation() == null || picture.getLocation().length() == 0) && picture.getLnglat() != null){
                    picture.setLocation(getLocation(picture.getLnglat()));
                    pictureService.updateById(picture);
            }

        }
    }
    //经纬度转地址
    public static String getLocation(String lnglat){
        String key = "GjG3XAdmywz7CyETWqHwIuEC6ZExY6QT";
        String url="http://api.map.baidu.com/geocoder/v2/?ak="+key+"&output=json&coordtype=bd09ll&location="+lnglat;
        String res=sendGetRequest(url);

        //获取详细地址
        String addressLocation= JSON.parseObject(res).getJSONObject("result").getString("formatted_address");
        return addressLocation;
    }

    @Test  /*lambdaQuery 会查询出逻辑删除*/
    public void T_delete(){
        List<Notebook> son = notebookService.lambdaQuery()
                .eq(Notebook::getPid, 15)
                .orderByAsc(Notebook::getSort)
                .list();
        System.out.println(son);
    }

    @Test  /*lambdaQuery 会查询出逻辑删除*/
    public void T_lambdaQuery_delete(){
        List<Notebook> son = notebookService.lambdaQuery()
                .list();
        System.out.println(son.size());
    }
    @Test
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        List<Note> notes = noteMapper.selectList(null);
        notes.forEach(System.out::println);
    }


    @Test
    public void insertNote() {
        // 设置修改时间为当前时间
        Note note = new Note();
        note.setCreateTime(new Date());
        note.setPid(1);
        noteService.save(note);
        Note byId = noteService.getById(note.getId());
        String s = ResultUtil.successWithData(byId);
        System.out.println(s);

    }

    @Autowired
    private NoteService noteService;


    @Test
    public void T_orderBy(){
        List<Note> noteList = noteService.lambdaQuery().orderByDesc(Note::getId).list();
        System.out.println(noteList);
    }

    @Test
    public void T_note_updateTime(){
        Note note = new Note();
        note.setUpdateTime(new Date());
        note.setId(1);
        boolean b = noteService.updateById(note);
        System.out.println(b);
    }

    @Test
    public void T_(){
        System.out.println(new Date(1632905983000L));
    }

    @Autowired
    private NotebookService notebookService;
    @Test
    public void T_noteBook(){
        List<Notebook> list = notebookService.lambdaQuery().orderByAsc(Notebook::getId).list();
        //list.forEach(System.out::println);
        System.out.println(ResultUtil.successWithData(list));
    }

    @Autowired
    private TagService tagService;
    @Test
    public void T_tag(){
        List<Tag> list = tagService.lambdaQuery().list();

        System.out.println(ResultUtil.successWithData(list));
    }

    @Test
    public void T_getNoteBookTree(){

        List<Notebook> list = notebookService.lambdaQuery().eq(Notebook::getPid,0).list();
        for (Notebook notebook : list) {
            getChildren(notebook);
        }
        System.out.println(list);
    }

    public void getChildren(Notebook notebook){
        List<Notebook> son = notebookService.lambdaQuery().eq(Notebook::getPid,notebook.getId()).list();
        if(son.size() > 0 ){
            for (Notebook sonNotebook : son) {
                getChildren(sonNotebook);
            }
        }
        notebook.setChildren(son);
    }
}
