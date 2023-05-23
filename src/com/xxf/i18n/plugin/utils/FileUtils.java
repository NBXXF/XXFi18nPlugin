package com.xxf.i18n.plugin.utils;

import java.io.*;

/**
 * Created by xyw on 2023/5/21.
 */
public class FileUtils {

    /*
    * append内容到目标文件
    * */
    public static void replaceContentToFile(String path, String con) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(con);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void closeQuietly(Closeable c) {
        if(c != null) {
            try {
                c.close();
            } catch (IOException var2) {
                ;
            }
        }

    }

}
