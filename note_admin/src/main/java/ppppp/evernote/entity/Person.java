package ppppp.evernote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pppppp
 * @date 2021/12/22 17:06
 */
@Data
public class Person {
    @TableId(type = IdType.INPUT) //type = IdType.INPUT,没有的话无法插入主键
    private Integer id;
    private String name;
    private Integer count;
    private String pictureUid;
    @TableField(exist = false)
    private List<Face> faceList = new ArrayList<>();
    @TableField(exist = false)
    private List<Picture> pictureList = new ArrayList<>();

    public Person(Integer id, String name, int count, String pictureUid) {
        this.id = id;
        this.name =name;
        this.count = count;
        this.pictureUid = pictureUid;
    }
}
