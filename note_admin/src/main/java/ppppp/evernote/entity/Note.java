package ppppp.evernote.entity;

import java.util.Date;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 笔记
 * </p>
 *
 * @author ppppp
 * @since 2021-09-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Note implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一uid
     */
      private String uid;

    /**
     * 所属笔记本id
     */
    private String pid;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 逻辑删除状态
     */
    private Boolean status;

    /**
     * 笔记简介
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
     * 笔记提醒时间
     */
    private Date remindTime;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 标签uid
     */
    private String tagUid;

    /**
     * 文件路径
     */
    private String mediaUid;

    /**
     * 收藏
     */
    private Boolean star;


}
