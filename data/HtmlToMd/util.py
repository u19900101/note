import os
import re
# 去掉文件和文件夹名称中的特殊字符
def renameFiles(path):
    fileList=os.listdir(path)
    for i in fileList:
        if os.path.isdir(path + "\\"+i):
            print("文件夹")
            renameFiles(path + "\\"+i)
        # 中英文逗号
        os.rename(path + "\\"+i,path + "\\"+re.sub(r'_+', '_', re.sub(r'[\s\[\],，。]', '_', i)))

path = "D:\MyJava\mylifeImg\others\读研期间"
renameFiles(path)



