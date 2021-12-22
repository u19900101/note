package ppppp.evernote.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * @author pppppp
 * @date 2021/12/22 17:06
 */
@Data
public class FaceName {
    @TableField()
    private Integer id;
    private String name;
    private Integer count;
}
