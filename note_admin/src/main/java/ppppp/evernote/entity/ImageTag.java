package ppppp.evernote.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author pppppp
 * @date 2021/12/5 15:55
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
/* todo 使用继承*/
public class ImageTag implements Serializable {
    private static final long serialVersionUID=1L;

    /**
     * 唯一uid
     */
    private Integer id;
    private Integer pid;

    Float sort;
    /*自定义字段 用于封装 el-tree*/
    @TableField(exist = false)
    List<ImageTag> children;
    /**
     * 标签内容
     */
    private String title;

    /**
     * 拥有该标签的笔记数量
     */
    private Integer noteCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除状态
     */
    @TableLogic // 逻辑删除字段 ，1-删除，0-未删除
    private Boolean status;
}

