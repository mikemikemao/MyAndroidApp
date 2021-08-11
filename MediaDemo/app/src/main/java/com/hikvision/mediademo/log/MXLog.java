package com.hikvision.mediademo.log;

import android.os.Handler;
import android.os.Message;

public class MXLog {
	
	// 调试
	public static boolean   LOG_MSG    = false;
	public static final int SHOW_MSG   = 255;  // 显示结果信息，用于调试
	public static final int CLEAR_MSG  = 256;  // 清空显示信息，用于调试
	static private Handler m_fHandler = null;
	
	static public void SetHandler(Handler fHandler){
		m_fHandler = fHandler;
	}
	
	/**
	 * 功	能：调试
	 * 参	数：obj - 调试打印信息
	 * 返	回：
	 * */
	static public void SendMsg(String obj) {
		if(MXLog.LOG_MSG==false)
		{
			return;
		}
		Message message = new Message();
		message.what  = MXLog.SHOW_MSG;
		message.obj   = obj;
		message.arg1  = 0;
		if (m_fHandler!=null) {
			m_fHandler.sendMessage(message);	
		}
	}
	
	/**
	 * 功	能：调试
	 * 参	数：obj - 调试打印信息
	 * 返	回：
	 * */
	static public void ClearMsg() {
		if(MXLog.LOG_MSG==false)
		{
			return;
		}
		Message message = new Message();
		message.what  = MXLog.CLEAR_MSG;
		message.obj   = null;
		message.arg1  = 0;
		if (m_fHandler!=null) {
			m_fHandler.sendMessage(message);	
		}
	}
	
}
