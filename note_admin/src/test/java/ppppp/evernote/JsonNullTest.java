package ppppp.evernote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import ppppp.evernote.entity.Note;
import ppppp.evernote.util.json.StringAdapter;

/**
 * @author pppppp
 * @date 2021/10/11 9:55
 */
public class JsonNullTest {
    @Test
    public void T_(){
        Note note = new Note();
        note.setId(1);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd");//设置时间格式
        //Gson gson = gsonBuilder.serializeNulls().create();
        //注册自定义String的适配器
        gsonBuilder.registerTypeAdapter(String.class, new StringAdapter());
        Gson gson = gsonBuilder.create();
        System.out.println(gson.toJson(note));//序列化
        System.out.println(gson.fromJson(gson.toJson(note),Note.class).toString());

    }


}
