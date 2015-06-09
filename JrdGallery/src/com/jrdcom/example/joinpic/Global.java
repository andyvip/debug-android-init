package com.jrdcom.example.joinpic;

/**
 * User: Javan.Eu
 * Date: 12-12-12
 * Time: 下午2:55
 */
public class Global {

    private static IModelSaver sModelSaver = null;

	/**
	 * Get current model Saver.
	 * this method is used by the SaveAndShare activity.
	 * @since 2012-12-12
	 * @return  the Saver object.
	 */
    public static synchronized IModelSaver getModelSaver() {
        return sModelSaver;
    }

	/**
	 * Set current model saver that user is working on.
	 * this method is used by the SaveAndShare activity.
	 * @since 2012-12-12
	 * @param pModelSaver the new saver for saving.
	 */
    public static synchronized void setModelSaver(IModelSaver pModelSaver) {
		sModelSaver = pModelSaver;
    }
}
