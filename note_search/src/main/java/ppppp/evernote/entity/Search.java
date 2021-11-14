package ppppp.evernote.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Search implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 唯一uid
     */
    private Integer id;
    private String keyword;
    /**
     * 创建时间
     */
    private Date createTime;

}
