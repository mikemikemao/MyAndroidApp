package com.hikvision.mediademo.Audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MyAudioRecord extends Thread{
    private static final String TAG = "MyAudioRecord";
    private AudioRecord record;
    private int minBufferSize;
    private boolean isDone = false;
    private static final int AUDIO_RATE = 44100;
    private static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VideoDemo";

    public MyAudioRecord(){
        minBufferSize = AudioRecord.getMinBufferSize(AUDIO_RATE, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        //使用 AudioRecord 去录音
        record = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                AUDIO_RATE,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize
        );
    }
    public void done() {
        interrupt();
        isDone = true;
    }

    @Override
    public void run() {
        super.run();
        FileOutputStream fos = null;
        FileOutputStream wavFos = null;
        RandomAccessFile wavRaf = null;
        try {
            //没有先创建文件夹
            File dir = new File(PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            //创建 pcm 文件
            File pcmFile = getFile(PATH, "test.pcm");
            //创建 wav 文件
            File wavFile = getFile(PATH, "test.wav");

            fos = new FileOutputStream(pcmFile);
            wavFos = new FileOutputStream(wavFile);

            //先写头部，刚才是，我们并不知道 pcm 文件的大小
            byte[] headers = generateWavFileHeader(0, AUDIO_RATE, record.getChannelCount());
            wavFos.write(headers, 0, headers.length);

            //开始录制
            record.startRecording();
            byte[] buffer = new byte[minBufferSize];
            while (!isDone) {
                //读取数据
                int read = record.read(buffer, 0, buffer.length);
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    //写 pcm 数据
                    fos.write(buffer, 0, read);
                    //写 wav 格式数据
                    wavFos.write(buffer, 0, read);
                }

            }
            //录制结束
            record.stop();
            record.release();

            fos.flush();
            wavFos.flush();

            //修改头部的 pcm文件 大小
            wavRaf = new RandomAccessFile(wavFile, "rw");
            byte[] header = generateWavFileHeader(pcmFile.length(), AUDIO_RATE, record.getChannelCount());
            wavRaf.seek(0);
            wavRaf.write(header);

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "zsr run: " + e.getMessage());
        } finally {
            CloseUtils.close(fos, wavFos,wavRaf);
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
}
