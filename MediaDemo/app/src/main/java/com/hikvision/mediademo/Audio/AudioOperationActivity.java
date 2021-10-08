package com.hikvision.mediademo.Audio;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.hikvision.mediademo.log.MXLog;


import androidx.annotation.Nullable;

import com.hikvision.mediademo.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioOperationActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "AudioOperationActivity";
    boolean isRecording=false;
    private AudioRecord audioRecord = null; // 声明 AudioRecord 对象
    private int recordBufSize = 0; // 声明recoordBufffer的大小字段private
    private static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mediaDemo";
    private static final int AUDIO_RATE = 16000;

    private static final int PROMTP_MSG       = 0;    // 提示信息
    private static final int SUCCESS_MSG      = 1;    // 成功
    private static final int FAILED_MSG       = 2;    // 失败
    private static final int SHOW_DLG_MSG     = 4;    // 弹出提示对话框
    private static final int CHANGE_DLG_MSG   = 5;    // 更改提示对话框信息
    private static final int STOP_DLG_MSG     = 6;    // 结束提示对话框
    private static final int CLEAR_MSG        = 7;    // 清空日志信息

    private ProgressDialog                  m_progressDlg             = null;
    private AudioRecordThread               m_AudioRecordThread       = null;
    private AudioPlayRecordThread           m_AudioPlayRecordThread   = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_opration_layout);
        findViewById(R.id.btn_capRecord).setOnClickListener(this);
        findViewById(R.id.btn_cancelRecord).setOnClickListener(this);
        findViewById(R.id.btn_playRecord).setOnClickListener(this);
        findViewById(R.id.btn_getvoiceDB).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_capRecord:
                zzAudioRecord();
                break;
            case R.id.btn_cancelRecord:
                //isRecording=true;
                AudioManager manager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                manager.setParameters("audio_devices_out_active=AUDIO_CODEC");
                break;
            case R.id.btn_playRecord:
                zzAudioTrack();
                break;
            case R.id.btn_getvoiceDB:
                AudioManager audioManager = (AudioManager) getSystemService(this.AUDIO_SERVICE);
                int minIndex = audioManager.getStreamMinVolume (AudioManager.STREAM_MUSIC);
                int maxIndex = audioManager.getStreamMaxVolume (AudioManager.STREAM_MUSIC);
                for(int i=minIndex; i<maxIndex; i++) {
                    float db = audioManager.getStreamVolumeDb(AudioManager.STREAM_MUSIC, i, AudioDeviceInfo.TYPE_WIRED_HEADSET);
                    Log.d(TAG, "volume db = " + db + "  at index= " + i);
                }
                break;
            default:
                break;
        }
    }

    public void zzAudioRecord(){
        if (m_AudioRecordThread != null) {
            m_AudioRecordThread.interrupt();
            m_AudioRecordThread = null;
        }
        m_AudioRecordThread = new AudioRecordThread();
        m_AudioRecordThread.start();
    }

    public void zzAudioTrack(){
    }

    //播放录音线程
    private class AudioPlayRecordThread extends Thread {
        public void run() {

        }

    }


    //录音线程
    private class AudioRecordThread extends Thread {
        public void run() {
            audioRecord();
        }

    }

    public void audioRecord(){
        recordBufSize = AudioRecord.getMinBufferSize(AUDIO_RATE, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT); //audioRecord能接受的最小的buffer大小
        if (recordBufSize<=0){
            SendMsg(FAILED_MSG,"获取recordBufSize失败，recordBufSize: " + recordBufSize);
            return;
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_RATE,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, recordBufSize);
        if (audioRecord==null){
            SendMsg(FAILED_MSG,"分配对象失败");
            return;
        }

        byte data[] = new byte[recordBufSize];

        audioRecord.startRecording();
        isRecording = false;

        FileOutputStream wavFos = null;

        try {
            //没有先创建文件夹
            File dir = new File(PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            //创建 wav 文件
            File wavFile = getFile(PATH, "test.wav");

            wavFos = new FileOutputStream(wavFile);

            //先写头部，刚才是，我们并不知道 pcm 文件的大小
            byte[] headers = generateWavFileHeader(0, AUDIO_RATE, audioRecord.getChannelCount());
            wavFos.write(headers, 0, headers.length);

            while (isRecording==false) {
                int read = audioRecord.read(data, 0, recordBufSize);
                if (read<=0) {
                    SendMsg(FAILED_MSG,"read failed ret="+read);
                    return;
                }
                //写 wav 格式数据
                wavFos.write(data, 0, read);
            }
            if(audioRecord!=null){
                //录制结束
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }

            wavFos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private File getFile(String path, String name) {
        File file = new File(path, name);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，
     * wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
     * FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的
     *
     * @param pcmAudioByteCount 不包括header的音频数据总长度
     * @param longSampleRate    采样率,也就是录制时使用的频率
     * @param channels          audioRecord的频道数量
     */
    private byte[] generateWavFileHeader(long pcmAudioByteCount, long longSampleRate, int channels) {
        long totalDataLen = pcmAudioByteCount + 36; // 不包含前8个字节的WAV文件总长度
        long byteRate = longSampleRate * 2 * channels;
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);

        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (2 * channels);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (pcmAudioByteCount & 0xff);
        header[41] = (byte) ((pcmAudioByteCount >> 8) & 0xff);
        header[42] = (byte) ((pcmAudioByteCount >> 16) & 0xff);
        header[43] = (byte) ((pcmAudioByteCount >> 24) & 0xff);
        return header;
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
