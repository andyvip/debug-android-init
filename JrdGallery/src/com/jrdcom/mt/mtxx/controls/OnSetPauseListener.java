/******************************************************************************************************************/
/*                                                                         Date : 08/2013  */
/*                            PRESENTATION                                                 */
/*              Copyright (c) 2010 JRD Communications, Inc.                                */
/******************************************************************************************************************/
/*                                                                                                                */
/*    This material is company confidential, cannot be reproduced in any                   */
/*    form without the written permission of JRD Communications, Inc.                      */
/*                                                                                                                */
/*================================================================================================================*/
/*   Author :                                                                              */
/*   Role :                                                                                */
/*   Reference documents :                                                                 */
/*================================================================================================================*/
/* Comments :                                                                              */
/*     file    :packages/apps/JrdGallery/src/com/jrdcom/mt/mtxx/controls/OnSetPauseListener.java              */
/*     Labels  :                                                                           */
/*================================================================================================================*/
/* Modifications   (month/day/year)                                                        */
/*================================================================================================================*/
/* date    | author       |FeatureID                |modification                          */
/*============|==============|=========================|==========================================================*/
/*08/06/13 | zhangcheng |PR498772-zhangcheng-001 |Pop up gallery force close when tap home key during loanding images. */
/*============|==============|=========================|==========================================================*/
package com.jrdcom.mt.mtxx.controls;

import com.jrdcom.android.gallery3d.filtershow.PauseListener;

public interface OnSetPauseListener {
	void setPauseListener(PauseListener pauseListener);
}
