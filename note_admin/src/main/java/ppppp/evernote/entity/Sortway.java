package ppppp.evernote.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Sortway implements Serializable {

    private static final long serialVersionUID=1L;
   /*四个排序字段*/
   @TableField()
    private Integer id;
   /* 要区分包装类 否则不会将@Response没有的字段封装为null*/
    private Boolean createTime;
    private Boolean updateTime;
    private Boolean location;
    private Boolean reverse;

    /*宽度字段*/
    private Integer navWidth;
    private Integer listWidth;

}
