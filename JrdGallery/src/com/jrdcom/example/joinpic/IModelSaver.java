package com.jrdcom.example.joinpic;

/**
 * 各个模块的数据模型保存使用的通用接口
 * <p/>
 * User: Javan.Eu
 * Date: 12-11-14
 * Time: 下午5:24
 */
public interface IModelSaver {
	/**
	 * 保存图片到指定路劲
	 *
	 * @param pPath 具体保存的路劲
	 * @return true，保存成功；false 保存失败。
	 */
	public boolean saveImageToPath(String pPath);
	
	/**
	 * 清除拼图底层数据
	 */
	public void clearAllPuzzleData();
}
