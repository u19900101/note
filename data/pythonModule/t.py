import os
import os.path
import uuid
faceimagepath_pre = "kk/"
name = faceimagepath_pre + str(uuid.uuid1()) + ".jpg"
while(os.path.isfile(name)):
    print("kkkk")
    name = faceimagepath_pre + str(uuid.uuid1()) + ".jpg"
print(name)

