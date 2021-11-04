package ppppp.evernote.entity;

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
    private Integer id;
    private boolean createTime;
    private boolean updateTime;
    private boolean location;
    private boolean reverse;

}
