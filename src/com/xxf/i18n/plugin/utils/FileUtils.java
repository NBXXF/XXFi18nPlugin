package com.xxf.i18n.plugin.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.*;

/**
 * Created by xyw on 2023/5/21.
 */
public class FileUtils {

    public static VirtualFile getConfigPathDir(Project currentProject){
        VirtualFile ideaConfigDir= currentProject.getProjectFile().getParent();
        return ideaConfigDir;
    }

    public static String getConfigPathFileName(){
        String configFileName="xxf_i18n_plugin_path.txt";
        return configFileName;
    }
    public static VirtualFile getConfigPathFile(Project currentProject){
        VirtualFile ideaConfigDir=getConfigPathDir(currentProject);
        String configFileName=getConfigPathFileName();
        VirtualFile pathConfigFile = ideaConfigDir.findChild(configFileName);
        return pathConfigFile;
    }
    public static String getConfigPathValue(Project currentProject){
        VirtualFile pathConfigFile = getConfigPathFile(currentProject);
        return getString(pathConfigFile);
    }

    public static String getString(VirtualFile file){
        try {
            return new String(file.contentsToByteArray(), "utf-8");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
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
