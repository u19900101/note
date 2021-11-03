package ppppp.evernote.entity;

import java.util.Date;
import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 笔记本
 * </p>
 *
 * @author ppppp
 * @since 2021-09-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Notebook implements Serializable {

    private static final long serialVersionUID=1L;

    /*自定义字段 用于封装 el-tree*/
    @TableField(exist = false)
    List<Notebook> children;

    private Float sort;
    /**
     * 唯一uid
     */
    private Integer id;

    private Integer pid;
    /**
     * 笔记本标题
     */
    private String title;

    /**
     * 笔记本简介
     */
    private String summary;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 笔记本包含的笔记条数
     */
    private Integer noteCount;

    /**
     * 笔记本图片url
     */
    private String imgUrl;

    /**
     * 逻辑删除状态
     */
    private Boolean status;


}
