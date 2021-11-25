package ppppp.evernote.entity;

import lombok.Data;

@Data
public class Picture {
    private String path;

    private String pid;

    private String pname;

    private String pcreatime;

    private String gpsLongitude;

    private String gpsLatitude;

    private Integer pwidth;

    private Integer pheight;

    private Double psize;

    private String plabel;

    private String pdesc;

}