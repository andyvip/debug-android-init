package com.jrdcom.mt.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileInputStreamOpener implements IInputStreamOpener{
	private String filePath;

	public FileInputStreamOpener(String FilePath){
		this.filePath = FilePath;
	}
	
	@Override
	public InputStream open() throws IOException {
		// TODO Auto-generated method stub
		return new FileInputStream(filePath);
	}

}
