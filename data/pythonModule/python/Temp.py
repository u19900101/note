import numpy as np
# l = [2,3]
# with open('./t.txt', 'a+') as f:
#     f.write('\n' + str(l).replace("[","").replace("]","").replace(","," "))
# known_face_ids = np.loadtxt('t.txt').tolist()
# print(known_face_ids)
known_face_encodings = np.loadtxt('known_face_encodings.txt').tolist()
print(known_face_encodings)
