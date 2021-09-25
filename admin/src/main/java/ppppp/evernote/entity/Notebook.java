package ppppp.evernote.entity;

import java.util.Date;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 笔记本
 * </p>
 *
 * @author ppppp
 * @since 2021-09-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Notebook implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一uid
     */
    private String uid;

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
