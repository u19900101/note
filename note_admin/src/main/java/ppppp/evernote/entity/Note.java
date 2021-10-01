package ppppp.evernote.entity;

import java.util.ArrayList;
import java.util.Date;
import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 笔记
 * </p>
 *
 * @author ppppp
 * @since 2021-09-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Note implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 唯一uid
     */
    private String id;

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
    @TableField(exist = false)
    // 解决初始化赋值时为null
    private List<String> tagList = new ArrayList<>();

    /**
     * 图片，视频，文件,一篇博客对应多媒体文件路径
     */
    @TableField(exist = false)
    private List<String> mediaList = new ArrayList<>();

}
