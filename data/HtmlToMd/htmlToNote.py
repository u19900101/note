from HtmlToMd.sql import renameFiles, insertNote, getTag_uid, md_sql, htmlToMd, closeConn
import os
dir = "D:/MyJava/mylifeImg/others/2021/12"
# dir = "./temp"
# 去除文件名称中的特殊字符
renameFiles(dir)
for i in os.listdir(dir):#and not i.startswith('5.323') and not i.startswith('5.324')
    if i.endswith(".html") and i.startswith('5.3') :
        print(i)
        title, createTime, updateTime, location, lng_lat, tagList, content = md_sql(htmlToMd(dir,i),dir)
        # 封装 tag 写进tag表中 写入 note表中
        insertNote(title,getTag_uid(tagList), createTime, updateTime, location, lng_lat,  str(content))
# 关闭数据库连接
closeConn()


