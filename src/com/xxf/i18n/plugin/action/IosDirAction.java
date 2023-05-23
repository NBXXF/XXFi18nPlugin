package com.xxf.i18n.plugin.action;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.xxf.i18n.plugin.bean.StringEntity;
import com.xxf.i18n.plugin.utils.FileUtils;
import com.xxf.i18n.plugin.utils.MessageUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 支持ios的.m文件自动抽取字符串
 * Created by xyw on 2023/5/23.
 */
public class IosDirAction extends AnAction {

    private int index = 0;

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project currentProject = e.getProject();

        //检查项目的配置
        String path= FileUtils.getConfigPathValue(currentProject);
        if(path==null||path.length()<=0){
            MessageUtils.showAlert(e,String.format("请在%s\n目录下面创建%s文件,且设置有效的生成文件路径(string.xml或者xxx.strings)",
                    FileUtils.getConfigPathDir(currentProject).getPath(),
                    FileUtils.getConfigPathFileName()));
            return;
        }
        VirtualFile targetStringFile = StandardFileSystems.local().findFileByPath(path);
        if (targetStringFile == null||!targetStringFile.exists()) {
            MessageUtils.showAlert(e,String.format("请在%s\n目录下面创建%s文件,且设置有效的生成文件路径(string.xml或者xxx.strings)",
                    FileUtils.getConfigPathDir(currentProject).getPath(),
                    FileUtils.getConfigPathFileName()));
            return;
        }

        String extension = targetStringFile.getExtension();
        if (extension == null || !extension.equalsIgnoreCase("strings")) {
            MessageUtils.showAlert(e,"生成的文件类型必须是strings");
            return;
        }


        Map<String,String> strDistinctMap= new HashMap();
        //读取已经存在的 复用,这里建议都是按中文来
        readFileToDict(targetStringFile,strDistinctMap);

        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        StringBuilder sb = new StringBuilder();
        classChild(file,sb,strDistinctMap);


        try {
            String content = new String(targetStringFile.contentsToByteArray(), "utf-8"); //源文件内容
            String result ="\n"+content+sb.toString();
            FileUtils.replaceContentToFile(targetStringFile.getPath(), result);//替换文件

            MessageUtils.showAlert(e,"国际化执行完成");
        } catch (IOException ex) {
            ex.printStackTrace();
            MessageUtils.showAlert(e,ex.getMessage());
        }
    }

    /**
     * 将已经存在字符串读取到字典里面 避免重复
     * @param file
     * @param strDistinctMap
     */
    private void readFileToDict( VirtualFile file,Map<String,String> strDistinctMap){
        try  {
            BufferedReader br
                    = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                //"email_input_hint"="请输入邮箱";
                if(line.endsWith(";")){
                    String[] split = line.split("=");
                    if(split!=null&&split.length==2){
                        String key=split[0].trim();
                        String value=split[1].trim();
                        if(key.startsWith("\"")&&key.endsWith("\"")){
                            key=key.substring(1,key.length()-1);
                        }
                        if(value.startsWith("\"")&&value.endsWith("\";")){
                            value=value.substring(1,value.length()-2);
                        }

                        strDistinctMap.put(value,key);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  执行 .m文件
     * @param file
     * @param sb
     */
    private  void classChild(VirtualFile file, StringBuilder sb,Map<String,String> strDistinctMap){
        index = 0;
        if(file.isDirectory()){
            VirtualFile[] children = file.getChildren();
            for (VirtualFile child : children) {
                classChild(child,sb,strDistinctMap);
            }
        }else{
            String extension = file.getExtension();
            if (extension != null && extension.equalsIgnoreCase("m")) {
                List<StringEntity> strings;
                StringBuilder oldContent = new StringBuilder();
                try {
                    oldContent.append(new String(file.contentsToByteArray(), "utf-8"));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                InputStream is = null;
                try {
                    is = file.getInputStream();
                    //ios 文件名可以有+号
                    strings = extraClassEntity(is, file.getNameWithoutExtension().toLowerCase().replaceAll("\\+","_"), oldContent,strDistinctMap);
                    if (strings != null) {
                        for (StringEntity string : strings) {
                            sb.append("\n\""+string.getId() + "\"=\"" + string.getValue() + "\";");
                        }
                        FileUtils.replaceContentToFile(file.getPath(), oldContent.toString());
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                } finally {
                    FileUtils.closeQuietly(is);
                }
            }
        }
    }


    private List<StringEntity> extraClassEntity(InputStream is, String fileName, StringBuilder oldContent,Map<String,String> strDistinctMap) {
        List<StringEntity> strings = Lists.newArrayList();
        String resultText=replaceUsingSB(fileName,oldContent.toString(),strings,strDistinctMap);
        oldContent = oldContent.replace(0, oldContent.length(), resultText);
        return strings;
    }

    public  String replaceUsingSB(String fileName, String str, List<StringEntity> strings,Map<String,String> strDistinctMap) {
        StringBuilder sb = new StringBuilder(str.length());
        Pattern p = Pattern.compile("(?=@\".{1,20}\")@\"[^$+,\\n\"{}]*[\\u4E00-\\u9FFF]+[^$+,\\n\"{}]*\"");
        Matcher m = p.matcher(str);
        int lastIndex = 0;
        while (m.find()) {
            sb.append(str, lastIndex, m.start());

            String subStr=m.group();
            //去除前后的双引号
            if(subStr.startsWith("@\"")&&subStr.endsWith("\"")){
                //这里截取
                subStr=subStr.substring(2,subStr.length()-1);
            }
            //复用已经存在的
            String id=strDistinctMap.get(subStr);
            if(id==null||id.length()<=0){
                //生成新的id
                id = currentIdString(fileName);
                strDistinctMap.put(subStr,id);
                strings.add(new StringEntity(id, subStr));
            }

            sb.append("R.string.localized_"+id+"");
            lastIndex = m.end();
        }
        sb.append(str.substring(lastIndex));
        return sb.toString();
    }

    private String currentIdString(String fileName){
        //需要加时间  多次生成的key避免错位和冲突,key 一样 内容不一样 合并风险太高
        final String id = fileName +"_"+ System.currentTimeMillis() +"_"  + (index++);
        return id;
    }
}
