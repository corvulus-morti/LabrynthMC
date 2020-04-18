package com.github.labrynthmc;

import java.util.Arrays;

public class Cell
{

	private char type = '0';
	private byte openSides[] = {0,0,0,0};

	public Cell(){}
	public Cell(char type)
	{
		this.type = type;
		if   (this.type == 'L')	this.openSides = new byte[] {1,1,0,0};
		else if (this.type == 'T')	this.openSides = new byte[] {1,1,0,0};
		else if (this.type == 'H')	this.openSides = new byte[] {1,0,1,0};
		else if (this.type == 'D')	this.openSides = new byte[] {1,0,0,0};
		else if (this.type == '4')	this.openSides = new byte[] {1,1,1,1};
	}

	public String toString()
	{
		String ret = this.type + " " + Arrays.toString(this.openSides);
		return ret;
	}

	public byte getOpenSides()[]
	{
		return openSides;
	}
	public void setOpenSides(String openSides)
	{
		byte bytes[] = new byte[4];
		for(int n = 0; n < openSides.length(); n++)
			bytes[n] = Byte.parseByte(openSides.charAt(n)+"");
		this.openSides = bytes;
	}
	public void setSide(int side, boolean open)
	{
		byte c;
		if (open) c = 1;
		else      c = 0;

		this.openSides[side] = c;
		this.setType();
	}

	public char getType()
	{
		return type;
	}
	private void setType()
	{
		int sum = 0;
		for (byte b: this.openSides) sum += b;

		if (sum == 0) this.type = '0';
		else if (sum == 1) this.type = 'D';
		else if (sum == 2)
		{
			if (this.openSides[0]==this.openSides[2])  this.type = 'H';
			else this.type = 'L';
		}
		else if (sum == 3) this.type = 'T';
		else if (sum == 4) this.type = '4';
	}
	//CREATE METHOD getCellRep() TO RETURN THE STRUCTURE FOR THIS CELL.
}
