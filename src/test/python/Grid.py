import numpy as np
import matplotlib.pyplot as plt
from Cell import Cell

class Grid:
	__MOVE = [
		np.array([-1,0]),
		np.array([0,1]),
		np.array([1,0]),
		np.array([0,-1]),
	]
	center = tuple()
	maze = dict()
	size = int()

	__maxDist = int()
	__FIXED = []

	def __addCell(self,pos):


		if (self.maze.get(tuple(pos)) == None):
			pos = tuple(pos)
			self.maze[pos] = Cell()
		else:
			return

		if (len(self.maze) == 1):
			self.__maxDist = 1
		elif (int(np.linalg.norm(pos - self.center)) > self.__maxDist):
			self.__maxDist = int(np.linalg.norm(pos - self.center))

	def __addPath(self,path):
		self.__addCell(path[0])
		for m in range(1,len(path)):
			last = tuple(path[m-1])
			curr = tuple(path[m])

			if (self.maze.get(curr) == None):
				self.__addCell(curr)

			disp = np.array(last) - np.array(curr)
			if (disp[0] == 1):
				self.maze[last].setSide(0,True)
				self.maze[curr].setSide(2,True)
			elif (disp[0] == -1):
				self.maze[last].setSide(2,True)
				self.maze[curr].setSide(0,True)
			elif (disp[1] == 1):
				self.maze[last].setSide(3,True)
				self.maze[curr].setSide(1,True)
			elif (disp[1] == -1):
				self.maze[last].setSide(1,True)
				self.maze[curr].setSide(3,True)

	def __buildPath(self,start,startDir):
		if (tuple(start) in self.maze):
			return None
		move = startDir

		path = [list(start)]
		# print(path)
		curr = start
		while (tuple(curr) not in self.maze):
			rot = int(np.random.uniform(0,3))-1
			move = (rot+move)%len(self.__MOVE)
			next = curr+self.__MOVE[move]

			if (list(next) in path or list(next) in self.__FIXED):
				return None

			curr = next
			path = [list(curr)]+path

		return path



	def showMaze(self):
		# print(self.__maxX)
		# print(self.__minX)
		# print(self.__maxY)
		# print(self.__minY)

		rad = self.__maxDist
		width = 2*rad
		height = 2*rad

		# print("Width = " + str(width))
		# print("Height = " + str(height))
		imCenter = np.array([rad,rad])

		im = np.zeros((5*width+5,5*height+5))
		# print(im.shape)
		for pos in self.maze:
			pos = np.array(pos)
			ind = tuple(pos-self.center+imCenter)

			m = ind[0]%(width+1)
			n = ind[1]%(height+1)
			# print("Im array: (pos " + str(tuple(pos-self.center)) + ") (inds " + str(ind) + ")")
			# print(im[5*m:5*(m+1),5*n:5*(n+1)].shape)
			# print("Cell array")
			# print(self.maze[tuple(pos)].getArray().shape)
			# print()
			im[5*m:5*(m+1),5*n:5*(n+1)] += self.maze[tuple(pos)].getArray()

		plt.imsave("maze_"+str(self.size)+".png",-im,cmap='gray')
		# plt.matshow(-im,cmap='gray')
		# plt.show()


	def genMaze(self,seed,maxPaths):
		self.size = maxPaths
		np.random.seed(seed)

		self.center = np.random.multivariate_normal([0,0],[[100,0],[0,100]])
		self.center = self.center.astype(int)
		self.center = np.array([0,0])
		# print(self.center)

		self.__addCell(self.center)
		self.__addCell(self.center+self.__MOVE[0])
		self.__addCell(self.center+self.__MOVE[1])
		self.__addCell(self.center+self.__MOVE[2])
		self.__addCell(self.center+self.__MOVE[3])
		self.maze[tuple(self.center)].setOpenSides("1111")
		self.maze[tuple(self.center+self.__MOVE[0])].setOpenSides("1111")
		self.maze[tuple(self.center+self.__MOVE[1])].setOpenSides("1111")
		self.maze[tuple(self.center+self.__MOVE[2])].setOpenSides("1111")
		self.maze[tuple(self.center+self.__MOVE[3])].setOpenSides("1111")

		self.__addCell(self.center+self.__MOVE[0]+self.__MOVE[3])
		self.__addCell(self.center+self.__MOVE[0]+self.__MOVE[1])
		self.__addCell(self.center+self.__MOVE[2]+self.__MOVE[3])
		self.__addCell(self.center+self.__MOVE[2]+self.__MOVE[1])
		self.maze[tuple(self.center+self.__MOVE[0]+self.__MOVE[3])].setOpenSides("0110")
		self.maze[tuple(self.center+self.__MOVE[0]+self.__MOVE[1])].setOpenSides("0011")
		self.maze[tuple(self.center+self.__MOVE[2]+self.__MOVE[3])].setOpenSides("1100")
		self.maze[tuple(self.center+self.__MOVE[2]+self.__MOVE[1])].setOpenSides("1001")

		self.__addCell(self.center+2*self.__MOVE[0])
		self.__addCell(self.center+2*self.__MOVE[1])
		self.__addCell(self.center+2*self.__MOVE[2])
		self.__addCell(self.center+2*self.__MOVE[3])
		self.maze[tuple(self.center+2*self.__MOVE[0])].setOpenSides("0010")
		self.maze[tuple(self.center+2*self.__MOVE[1])].setOpenSides("0001")
		self.maze[tuple(self.center+2*self.__MOVE[2])].setOpenSides("1000")
		self.maze[tuple(self.center+2*self.__MOVE[3])].setOpenSides("0100")

		self.__FIXED = [
			list(self.center+self.__MOVE[0]+self.__MOVE[3]),
			list(self.center+self.__MOVE[0]+self.__MOVE[1]),
			list(self.center+self.__MOVE[2]+self.__MOVE[3]),
			list(self.center+self.__MOVE[2]+self.__MOVE[1]),
		]

		for m in range(4):
			move = m
			start = self.center + 2*self.__MOVE[m]
			# print(str(m) + ": Starting")
			path = [list(start)]
			# print(path)
			curr = start
			while (len(path) < 5):
				rot = int(np.random.uniform(0,3))-1
				rot = 0
				move = (rot+move)%len(self.__MOVE)
				next = curr+self.__MOVE[move]

				if (list(next) in path or self.maze.get(tuple(next)) != None):
					path = [list(start)]
					move = m
					curr = start
					continue

				curr = next
				path = [list(curr)]+path

			self.__addPath(path)
			# print(str(m)+": Ending")


		scalar = 2
		r = scalar*np.sqrt(len(self.maze)/np.pi)
		# r = 0
		print("r = " + str(r))
		std = 2
		countFailed = 0
		for m in range(maxPaths-4):
			pathFound = False
			while (not pathFound):
				trials = 0
				while (trials < 100):
					R = np.random.normal(r,std)
					theta = np.random.uniform(0,2*np.pi)
					start = np.array([
							int(R*np.cos(theta)),
							int(R*np.sin(theta))
						])
					path = self.__buildPath(start,int(np.random.uniform(0,4)))
					if (path != None):
						pathFound = True
						break
					else:
						trials += 1
				# print(path)
				if (pathFound):
					# print(m/maxPaths)
					break

				r = scalar*np.sqrt(len(self.maze)/np.pi)
				# r += std
				print("Failed: r = " + str(r) + "\t size = " + str(m+4))
				countFailed += 1

			self.__addPath(path)
		# print("Failures = " + str(countFailed))
