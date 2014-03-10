import sys

direction=sys.argv[1]
size=sys.argv[2]

def val(j,k):
  if (direction == "up"):
    return 1 if (j >= k) else 0
  else:
    return 0 if (j < k) else 1

i = int(size)
for j in range(i):
  for k in range(i):
    print("M"+ direction + " " + str(j) + " " + str(k) + " " + str(val(j,k)))
