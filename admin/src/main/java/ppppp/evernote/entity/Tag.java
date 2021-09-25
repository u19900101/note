package ppppp.evernote.entity;

import java.util.Date;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 标签表
 * </p>
 *
 * @author ppppp
 * @since 2021-09-25
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一uid
     */
      private String uid;

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
    private Boolean status;


}
