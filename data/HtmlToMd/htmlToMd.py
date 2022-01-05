from HtmlToMd.sql import renameFiles, insertNote, getTag_uid, md_sql, htmlToMd, closeConn
import os
dir = "D:\MyJava\mylifeImg\others\我的抗战2.0\\"
# dir = "./temp"
# 去除文件名称中的特殊字符
renameFiles(dir)
for i in os.listdir(dir):
    if i.endswith(".html"):
        print(i)
        title, createTime, updateTime, location, lng_lat, tagList, content = md_sql(htmlToMd(dir,i))
        # 封装 tag 写进tag表中 写入 note表中
        insertNote(title,getTag_uid(tagList), createTime, updateTime, location, lng_lat,  str(content))
#       写入md
#       with open(dir + i.replace(".html",".md"), 'w', encoding='UTF-8') as f:
#           f.write(createTime + "\n" + updateTime+ "\n" +location+ "\n" +lng_lat+ "\n" +str(tagList)+ "\n" +content)
# 关闭数据库连接
closeConn()


