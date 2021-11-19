import pymysql
import time
# 打开数据库连接
db = pymysql.connect(host='192.168.56.10', user='root', password='root', database='canal_test')
cursor = db.cursor()

def insertTag(tagName):
    timestr = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
    sql = "INSERT INTO tag(title, sort,create_time,update_time) VALUES ('%s', '%s', '%s', '%s')" % \
          (tagName,getMaxSort()[0][0] + 1,timestr,timestr)
    try:
        # 执行sql语句
        cursor.execute(sql)
        # 最新插入行的主键id
        tag_id = db.insert_id()
        # 执行sql语句
        db.commit()
        return tag_id
    except:
        print('error...')
        db.rollback()

def getMaxSort():
    sql = "SELECT MAX(sort) FROM tag"
    try:
        # 执行SQL语句
        cursor.execute(sql)
        return cursor.fetchall()
    except:
        print("Error: unable to fetch data")


def isTagExist(tagName):
    sql = "SELECT id,title FROM tag "
    try:
        # 执行SQL语句
        cursor.execute(sql)
        # 获取所有记录列表
        results = cursor.fetchall()
        for row in results:
            # 忽略大小写 比较tag名称
            if tagName.lower() == row[1].lower():
                return row[0]
        return -1
    except:
        print("Error: unable to fetch data")

def insertNote(title, tag_uid, createTime, updateTime, location, lnglat, content):
    # pymysql.escape_string 解决 ' 保存的问题
    if len(updateTime) > 0:
        sql = "INSERT INTO note(title, tag_uid, create_time, update_time, location, lnglat, content)  VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s')" % \
          (pymysql.escape_string(title), tag_uid, createTime, updateTime, location, lnglat, pymysql.escape_string(content))
    else:
        sql = "INSERT INTO note(title, tag_uid, create_time,  location, lnglat, content)  VALUES ('%s', '%s', '%s', '%s', '%s', '%s')" % \
              (pymysql.escape_string(title), tag_uid, createTime,  location, lnglat, pymysql.escape_string(content))
    try:
        cursor.execute(sql)
        db.commit()
        print('保存成功',title)
    except Exception as e:
        print(e,title)
        db.rollback()

def closeConn():
    db.close()
# tag_uid = ''
# for tagName in '历史性的时刻,有想法'.split(","):
#     resId = isTagExist(tagName)
#     if resId == -1:
#         resId = insertTag(tagName)
#     tag_uid += str(resId) + ','
# print(tag_uid)
# insertNote("总结", "8,", "2015-08-23 21:38:00" , "2015-08-23 21:38:00" ,"武汉市 黄陂区 武大高速", "114.334000,31.000800" ,"周一周二:兴趣爱好—")

