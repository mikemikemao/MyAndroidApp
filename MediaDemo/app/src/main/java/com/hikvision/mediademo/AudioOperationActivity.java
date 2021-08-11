package com.hikvision.mediademo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hikvision.mediademo.Audio.MyAudioRecord;
import com.hikvision.mediademo.log.MXLog;
import com.manager.Manager;

public class AudioOperationActivity extends Activity {

    Button btn_PlayRecord;
    Button btn_cancelRecord;
    boolean isRecording=false;
    Manager manager;

    private AudioRecord audioRecord = null; // 声明 AudioRecord 对象
    private int recordBufSize = 0; // 声明recoordBufffer的大小字段private

    private static final int PROMTP_MSG       = 0;    // 提示信息
    private static final int SUCCESS_MSG      = 1;    // 成功
    private static final int FAILED_MSG       = 2;    // 失败
    private static final int SHOW_DLG_MSG     = 4;    // 弹出提示对话框
    private static final int CHANGE_DLG_MSG   = 5;    // 更改提示对话框信息
    private static final int STOP_DLG_MSG     = 6;    // 结束提示对话框
    private static final int CLEAR_MSG        = 7;    // 清空日志信息

    private ProgressDialog m_progressDlg    = null;
    private AudioRecordThread m_AudioRecordThread       = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_opration_layout);

        manager=new Manager();
        btn_PlayRecord=(Button)findViewById(R.id.btn_playRecord);
        btn_cancelRecord=(Button)findViewById(R.id.btn_cancelRecord);

        btn_PlayRecord.setOnClickListener(v -> {
            if (m_AudioRecordThread != null) {
                m_AudioRecordThread.interrupt();
                m_AudioRecordThread = null;
            }
            m_AudioRecordThread = new AudioRecordThread();
            m_AudioRecordThread.start();
        });

        btn_cancelRecord.setOnClickListener(v -> {
            isRecording=true;
            manager.zzAudioRecord();

        });
    }



    private class AudioRecordThread extends Thread {
        public void run() {
            audioRecord();
        }

    }

    public void audioRecord(){
        recordBufSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT); //audioRecord能接受的最小的buffer大小
        if (recordBufSize<=0){
            SendMsg(FAILED_MSG,"获取recordBufSize失败，recordBufSize: " + recordBufSize);
            return;
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recordBufSize);
        if (audioRecord==null){
            SendMsg(FAILED_MSG,"分配对象失败");
            return;
        }

        byte data[] = new byte[recordBufSize];

        audioRecord.startRecording();
        isRecording = false;
        int i=0;
        while (isRecording==false) {
            int read = audioRecord.read(data, 0, recordBufSize);
            if (read<=0) {
                SendMsg(FAILED_MSG,"read failed ret="+read);
                return;
            }
            i++;
            if (i==100){
                SendMsg(FAILED_MSG,"data=="+data[0]  +data[1]   +data[2]    +data[3]);
                i=0;
            }

        }
    }

    /* Toast控件显示提示信息 */
    public void DisplayToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }



    private void ShowProgressDlg(final String strTitle, final String strMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_progressDlg = ProgressDialog.show(AudioOperationActivity.this, strTitle,
                        strMsg, true);
            }
        });

    }

    private void CancelProgressDlg() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (m_progressDlg!=null){
                    m_progressDlg.cancel();
                }
            }
        });

    }

    /**
     * 提示信息
     * */
    private void ShowMessage(String strMsg,Boolean bAdd){
        EditText edit_show_msg = (EditText) findViewById(R.id.edit_show_msg);
        if (bAdd) {
            String strShowMsg  = edit_show_msg.getText().toString();
            strMsg = strShowMsg+strMsg;
        }
        edit_show_msg.setText(strMsg+"\r\n");
        ScrollView scrollView_show_msg = (ScrollView) findViewById(R.id.scrollView_show_msg);
        scrollToBottom(scrollView_show_msg,edit_show_msg);
    }

    public static void scrollToBottom(final View scroll, final View inner) {
        Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            public void run() {
                if (scroll == null || inner == null) {
                    return;
                }
                int offset = inner.getMeasuredHeight() - scroll.getHeight();
                if (offset < 0) {
                    offset = 0;
                }
                scroll.scrollTo(0, offset);
            }
        });
    }

    private void SendMsg(int what, String obj) {
        Message message = new Message();
        message.what = what;
        message.obj  = obj;
        message.arg1 = 0;
        LinkDetectedHandler.sendMessage(message);
    }

    private Handler LinkDetectedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_DLG_MSG:
                    ShowProgressDlg("提示信息",""+msg.obj);
                    break;
                case CHANGE_DLG_MSG:
                    m_progressDlg.setMessage((CharSequence) msg.obj);
                    break;
                case STOP_DLG_MSG:
                    CancelProgressDlg();
                    break;
                case PROMTP_MSG:
                    ShowMessage(""+msg.obj,true);
                    break;
                case SUCCESS_MSG:
                case FAILED_MSG:
                    ShowMessage(""+msg.obj,true);
                    break;
                case CLEAR_MSG:
                    ShowMessage("",false);
                    break;
                case MXLog.CLEAR_MSG:
                    ShowMessage("",false);
                    break;
                default:
                    ShowMessage(""+msg.obj,true);
                    break;
            }
        }
    };
}
