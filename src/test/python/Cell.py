import numpy as np

class Cell:

	__openSides = []

	def __init__(self):
		self.__openSides = [False,False,False,False]

	def getArray(self):
		arr = np.zeros([5,5])
		arr[0,0] = 1
		arr[0,-1] = 1
		arr[-1,0] = 1
		arr[-1,-1] = 1

		if (not self.__openSides[0]):
			arr[0,:] = 1
		if (not self.__openSides[1]):
			arr[:,-1] = 1
		if (not self.__openSides[2]):
			arr[-1,:] = 1
		if (not self.__openSides[3]):
			arr[:,0] = 1

		return arr

	def setOpenSides(self,str):
		for n in range(len(str)):
			self.__openSides[n] = (int(str[n]) == 1)

	def setSide(self,side,open):
		self.__openSides[side] = open
