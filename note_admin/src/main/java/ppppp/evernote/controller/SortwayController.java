package ppppp.evernote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ppppp.evernote.entity.Sortway;
import ppppp.evernote.entity.Tag;
import ppppp.evernote.service.SortWayService;
import ppppp.evernote.service.TagService;
import ppppp.evernote.util.ResultUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/admin/sortWay")
public class SortwayController {
    @Autowired
    SortWayService sortWayService;
    @RequestMapping("/allSortWay")
    public String getSortWay() {
        List<Sortway> sortWayList = sortWayService.lambdaQuery().list();
        return ResultUtil.successWithData(sortWayList);
    }
    /*ps 更新 导航栏和笔记列表栏的宽度*/
    @PostMapping("/updateSortWay")
    public String updateSortWay(@RequestBody Sortway sortway) {
        boolean b = sortWayService.updateById(sortway);
        return ResultUtil.successWithData(b);
    }
}

