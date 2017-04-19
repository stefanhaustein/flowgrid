package com.badlogic.audio.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EndianDataInputStream extends DataInputStream
{	
	public EndianDataInputStream(InputStream in)
	{
		super(in);		
	}

	public String read4ByteString( ) throws IOException
	{
		byte[] bytes = new byte[4];
		readFully(bytes);
		return new String( bytes, "US-ASCII" );
	}
	
	public short readShortLittleEndian( ) throws IOException
	{
		int result = readUnsignedByte();
		result |= readUnsignedByte() << 8;		
		return (short)result;		
	}
	
	public int readIntLittleEndian( ) throws IOException
	{
		int result = readUnsignedByte();
		result |= readUnsignedByte() << 8;
		result |= readUnsignedByte() << 16;
		result |= readUnsignedByte() << 24;
		return result;		
	}
	
	public int readInt24BitLittleEndian( ) throws IOException
	{
		int result = readUnsignedByte();
		result |= readUnsignedByte() << 8;
		result |= readUnsignedByte() << 16;
		if( (result & ( 1 << 23 )) == 8388608 )
			result |= 0xff000000;
		return result;		
	}
	
	public int readInt24Bit( ) throws IOException
	{
		int result = readUnsignedByte() << 16;
		result |= readUnsignedByte() << 8;
		result |= readUnsignedByte();		
		return result;		
	}
}
