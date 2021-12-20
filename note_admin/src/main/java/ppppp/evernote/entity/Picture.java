package ppppp.evernote.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Picture implements Serializable {
    private static final long serialVersionUID = 1L;

    // 主键
    @TableField()
    private Integer id;
    // 照片指纹
    private String imguuid;
    //名称
    private String title;
    //位置
    private String location;
    //宽高
    private String widthH;
    //经纬度
    private String lnglat;
    // 逻辑删除状态
    @TableLogic // 逻辑删除字段 ，1-删除，0-未删除
    private Boolean status;
    //废纸篓
    private Boolean wastepaper;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    // 标签uid
    private String tagUid;
    //收藏
    private Boolean star;
    //网络地址
    private String url;
    // 字节大小
    private Long size;

    // 视频时长
    private String lastTime;
    @TableField(exist = false)
    // 解决初始化赋值时为null
    private List<ImageTag> tagList = new ArrayList<>();

}