package com.hikvision.mediademo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ToolUnit {
    // 写文件
    public int fileWrite(String fileName, char[] buffer) {
        int i = -1;
        try {
            File file = new File(fileName);
            if(!file.exists()){
                if(!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(buffer);
            fileWriter.close();
            i = 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return i;
    }

    //读取文件的数据
    public String ReadFromFile(String fileName) {
        File file = new File(fileName);
        if ((file != null) && file.exists()) {
            try {
                FileInputStream fin = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
                String flag = reader.readLine();
                fin.close();
                return flag;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
