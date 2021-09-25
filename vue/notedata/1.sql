/*Table structure for table `t_notebook` */
DROP TABLE IF EXISTS `t_notebook`;

CREATE TABLE `t_notebook` (
  `uid` varchar(32) NOT NULL COMMENT '唯一uid',
  `title` varchar(200) DEFAULT NULL COMMENT '笔记本标题',
  `status` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除状态',
  `summary` varchar(200) DEFAULT NULL COMMENT '笔记本简介',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `note_count` int(11) DEFAULT '0' COMMENT '笔记本包含的笔记条数',
  `img_url` varchar(255) DEFAULT NULL COMMENT '笔记本图片url'
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8 COMMENT='笔记本';

insert  into `t_notebook`(`uid`,`title`,`status`,`summary`,`create_time`,`update_time`,`note_count`,`img_url`) values ('1','数据库-1',0,'笔记本1-简介','2020-10-13 10:01:00','2020-12-26 11:24:33',2,'1.jpg'),
('2','数据库-2',0,'笔记本2-简介','2020-10-13 10:01:00','2020-12-26 11:24:33',2,'2.jpg');

/*Table structure for table `t_note` */
DROP TABLE IF EXISTS `t_note`;
CREATE TABLE `t_note` (
  `uid` varchar(32) NOT NULL COMMENT '唯一uid',
  `title` varchar(200) DEFAULT NULL COMMENT '笔记标题',
  `status` tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '逻辑删除状态',
  `summary` varchar(200) DEFAULT NULL COMMENT '笔记简介',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `remind_time` timestamp  DEFAULT '0000-00-00 00:00:00' COMMENT '笔记提醒时间',
  `content` longtext COMMENT '笔记内容',
  `tag_uid` varchar(511) DEFAULT NULL COMMENT '标签uid',
  `media_uid` varchar(511) DEFAULT NULL COMMENT '文件路径',
  `star` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '收藏',
  `pid` varchar(32) NOT NULL COMMENT '所属笔记本id',
  PRIMARY KEY (`uid`,`pid`),
  KEY `uid` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8 COMMENT='笔记';

insert  into `t_note`
(`uid`,`title`,`status`,`summary`,`create_time`,`update_time`,`remind_time`,`content`,`tag_uid`,`media_uid`,`star`,`pid`) values
('1','笔记-1',0,'笔记1-简介','2020-10-13 10:01:00','2020-12-26 11:24:33',NULL,'笔记1-内容','tag_id_1','3.jpg',0,'1'),
('2','笔记-2',0,'笔记2-简介','2020-10-14 10:01:00','2020-12-27 11:24:33',NULL,'笔记2-内容','tag_id_2','4.jpg',1,'1'),
('3','笔记-3',0,'笔记3-简介','2020-10-15 10:01:00','2020-12-28 11:24:33',NULL,'笔记3-内容','tag_id_3','5.jpg',0,'2'),
('4','笔记-4',0,'笔记4-简介','2020-10-16 10:01:00','2020-12-29 11:24:33',NULL,'笔记4-内容','tag_id_4','6.jpg',1,'2');

DROP TABLE IF EXISTS `t_tag`;
CREATE TABLE `t_tag` (
  `uid` varchar(32) NOT NULL COMMENT '唯一uid',
  `title` varchar(1000) DEFAULT NULL COMMENT '标签内容',
  `status` tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '逻辑删除状态',
  `note_count` int(11) DEFAULT '0' COMMENT '拥有该标签的笔记数量',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='标签表';
insert  into `t_tag`(`uid`,`title`,`status`,`note_count`,`create_time`,`update_time`) values
('tag_id_1','数据库',0,78,'2020-10-13 10:01:00','2020-12-26 11:24:33'),
('tag_id_2','JVM',0,33,'2020-10-13 10:01:00','2021-01-24 10:00:58'),
('tag_id_3','标签2',0,0,'2020-10-13 10:01:10','2020-10-13 10:01:15'),
('tag_id_4','RabbitMQ',0,106,'2020-10-13 10:01:00','2020-12-26 11:24:33');

