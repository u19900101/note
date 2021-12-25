package ppppp.evernote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

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
}
