package com.jrdcom.mt.core;

import java.io.File;
/**
 * 文件相关的处理方法
 * @author aidy
 *
 */
public class MTFile {

    /**
     * 根据绝对路径的地址来创建文件夹
     * @param strPath   文件夹的绝对路径
     */
	public static void createPath(String strPath)
	{
		File file = new File(strPath);
		if (file.exists())
		{
			delFolder(strPath);
		}
		file.mkdir();
	}

    /**
     * 删除文件夹
     * @param folderPath        文件夹的绝对路径
     */
    public static void delFolder(String folderPath) {
            try {
                    delAllFile(folderPath); //删除完里面所有内容
                    String filePath = folderPath;
                    filePath = filePath.toString();
                    java.io.File myFilePath = new java.io.File(filePath);
                    myFilePath.delete(); //删除空文件夹

           }
            catch (Exception e) {
                    System.out.println("删除文件夹操作出错");
                    e.printStackTrace();

           }
    }

   /**
     * 删除文件夹里面的所有文件
     * @param path String 文件夹路径 如 c:/fqf
     */
    public static void delAllFile(String path) {
            File file = new File(path);
            if (!file.exists()) {
                    return;
            }
            if (!file.isDirectory()) {
           return;
            }
            String[] tempList = file.list();
            File temp = null;
            for (int i = 0; i < tempList.length; i++) {
                    if (path.endsWith(File.separator)) {
                            temp = new File(path + tempList[i]);
                    }
                    else {
                            temp = new File(path + File.separator + tempList[i]);
                    }
                    if (temp.isFile()) {
                            temp.delete();
                    }
                    if (temp.isDirectory()) {
                            delAllFile(path+"/"+ tempList[i]);//先删除文件夹里面的文件
                            delFolder(path+"/"+ tempList[i]);//再删除空文件夹
                    }
            }
    } 
}
