import numpy as np
import matplotlib.pyplot as plt
from Cell import Cell
from Grid import Grid
import time

seed = int(np.random.uniform(0,10000))
print("Random Seed: " + str(seed))

maxPaths = int(input("How many paths will be added to the maze? "))

b = Grid()
start_time = time.time()
b.genMaze(seed,maxPaths)
end_time = time.time()

print("Run time = " + str(end_time - start_time))
b.showMaze()

# for cell in b.maze:
# 	print(b.maze[cell].getArray().shape)
