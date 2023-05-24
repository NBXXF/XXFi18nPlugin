package com.xxf.i18n.plugin.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.xxf.i18n.plugin.bean.StringEntity;
import com.xxf.i18n.plugin.utils.FileUtils;
import com.google.common.collect.Lists;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.xxf.i18n.plugin.utils.MessageUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * layout或者java(仅支持kt)文件夹转成strings
 * Created by xyw on 2023/5/21.
 */
public class AndroidDirAction extends AnAction {

    private int index = 0;

    //避免重复 key 中文字符串 value 为已经生成的id
    Map<String,String> strDistinctMap= new  HashMap();
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project currentProject = e.getProject();
        //检查项目的配置
        String path= FileUtils.getConfigPathValue(currentProject);
        if(path==null||path.length()<=0){
            MessageUtils.showAlert(e,String.format("请在%s\n目录下面创建%s文件,且设置有效的生成文件路径(string.xml)",
                    FileUtils.getConfigPathDir(currentProject).getPath(),
                    FileUtils.getConfigPathFileName()));
            return;
        }
        VirtualFile targetStringFile = StandardFileSystems.local().findFileByPath(path);
        if (targetStringFile == null||!targetStringFile.exists()) {
            MessageUtils.showAlert(e,String.format("请在%s\n目录下面创建%s文件,且设置有效的生成文件路径(string.xml)",
                    FileUtils.getConfigPathDir(currentProject).getPath(),
                    FileUtils.getConfigPathFileName()));
            return;
        }

        String extension = targetStringFile.getExtension();
        if (extension == null || !extension.equalsIgnoreCase("xml")) {
            MessageUtils.showAlert(e,"生成的文件类型必须是string.xml");
            return;
        }


        VirtualFile eventFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (eventFile == null) {
            MessageUtils.showAlert(e,"找不到目标文件");
            return;
        }


        strDistinctMap.clear();
        StringBuilder sb = new StringBuilder();
        //layout 目录 可能是pad layout_s600dp等等
        if(eventFile.isDirectory() && eventFile.getName().startsWith("layout")) {
            //遍历所有layout文件，然后获取其中的字串写到stringbuilder里面去
            VirtualFile[] children = eventFile.getChildren();
            for (VirtualFile child : children) {
                layoutChild(child, sb);
            }
        }else if(eventFile.getExtension()!=null && eventFile.getExtension().equalsIgnoreCase("xml")){
            //可能是layout布局文件
            layoutChild(eventFile, sb);
        }else{
            //遍历所有 kt文件，然后获取其中的字串写到stringbuilder里面去
            classChild(eventFile, sb);
        }


        try {
            if(!sb.isEmpty()) {
                String content = new String(targetStringFile.contentsToByteArray(), "utf-8"); //源文件内容
                String result = content.replace("</resources>", sb.toString() + "\n</resources>"); //在最下方加上新的字串
                FileUtils.replaceContentToFile(targetStringFile.getPath(), result);//替换文件
            }
            MessageUtils.showAlert(e,String.format("国际化执行完成,新生成（%d)条结果",strDistinctMap.size()));
        } catch (IOException ex) {
            ex.printStackTrace();
            MessageUtils.showAlert(e,ex.getMessage());
        }

        e.getActionManager().getAction(IdeActions.ACTION_SYNCHRONIZE).actionPerformed(e);
    }


    /**
     *  执行 java 文件和kt文件
     * @param file
     * @param sb
     */
    private  void classChild(VirtualFile file, StringBuilder sb){
        index = 0;
        if(file.isDirectory()){
            VirtualFile[] children = file.getChildren();
            for (VirtualFile child : children) {
                classChild(child,sb);
            }
        }else{
            String extension = file.getExtension();
            if (extension != null && extension.equalsIgnoreCase("kt")) {
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
                    strings = extraClassEntity(is, file.getNameWithoutExtension().toLowerCase(), oldContent,strDistinctMap);
                    if (strings != null) {
                        for (StringEntity string : strings) {
                            sb.append("\n    <string name=\"" + string.getId() + "\">" + string.getValue() + "</string>");
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
        Pattern p = Pattern.compile("(?=\".{1,60}\")\"[^$+,\\n\"{}]*[\\u4E00-\\u9FFF]+[^$+,\\n\"{}]*\"");
        Matcher m = p.matcher(str);
        int lastIndex = 0;
        while (m.find()) {
            sb.append(str, lastIndex, m.start());

            String value=m.group();
            //去除前后的双引号
            if(value.startsWith("\"")&&value.endsWith("\"")){
                value=value.substring(1,value.length()-1);
            }
            //复用已经存在的
            String id=strDistinctMap.get(value);
            if(id==null||id.length()<=0){
                //生成新的id
                id = currentIdString(fileName);
                strDistinctMap.put(value,id);
                strings.add(new StringEntity(id, value));
            }

            sb.append("com.xxf.application.applicationContext.getString(com.next.space.cflow.resources.R.string."+id+")");
            lastIndex = m.end();
        }
        sb.append(str.substring(lastIndex));
        return sb.toString();
    }


    /**
     * 递归执行layout xml
     * @param file
     * @param sb
     */
    private void layoutChild(VirtualFile file, StringBuilder sb) {
        index = 0;

        String extension = file.getExtension();
        if (extension != null && extension.equalsIgnoreCase("xml")) {
            if (!file.getParent().getName().startsWith("layout")) {
                MessageUtils.showNotify("请选择布局文件");
                return;
            }
        }


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
            strings = extraStringEntity(is, file.getNameWithoutExtension().toLowerCase(), oldContent);
            if (strings != null) {
                for (StringEntity string : strings) {
                    sb.append("\n    <string name=\"" + string.getId() + "\">" + string.getValue() + "</string>");
                }
                FileUtils.replaceContentToFile(file.getPath(), oldContent.toString());
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            FileUtils.closeQuietly(is);
        }

    }

    private List<StringEntity> extraStringEntity(InputStream is, String fileName, StringBuilder oldContent) {
        List<StringEntity> strings = Lists.newArrayList();
        try {
            return generateStrings(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is), strings, fileName, oldContent);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    private String currentIdString(String fileName){
        //需要加时间  多次生成的key避免错位和冲突,key 一样 内容不一样 合并风险太高
        final String id = fileName +"_"+ System.currentTimeMillis() +"_"  + (index++);
        return id;
    }

    private List<StringEntity> generateStrings(Node node, List<StringEntity> strings, String fileName, StringBuilder oldContent) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Node stringNode = node.getAttributes().getNamedItem("android:text");
            if (stringNode != null) {
                String value = stringNode.getNodeValue();
                if (!value.contains("@string")) {
                    //复用已经存在的
                    String id=strDistinctMap.get(value);
                    if(id==null||id.length()<=0){
                        //生成新的id
                        id = currentIdString(fileName);
                        strDistinctMap.put(value,id);
                        strings.add(new StringEntity(id, value));
                    }

                    String newContent = oldContent.toString().replaceFirst("\"" + value + "\"", "\"@string/" + id + "\"");
                    oldContent = oldContent.replace(0, oldContent.length(), newContent);
                }
            }
            Node hintNode = node.getAttributes().getNamedItem("android:hint");
            if (hintNode != null) {
                String value = hintNode.getNodeValue();
                if (!value.contains("@string")) {
                    //复用已经存在的
                    String id=strDistinctMap.get(value);
                    if(id==null||id.length()<=0){
                        //生成新的id
                        id = currentIdString(fileName);
                        strDistinctMap.put(value,id);
                        strings.add(new StringEntity(id, value));
                    }

                    String newContent = oldContent.toString().replaceFirst("\"" + value + "\"", "\"@string/" + id + "\"");
                    oldContent = oldContent.replace(0, oldContent.length(), newContent);
                }
            }

            Node leftTitleNode = node.getAttributes().getNamedItem("app:leftText");
            if (leftTitleNode != null) {
                String value = leftTitleNode.getNodeValue();
                if (!value.contains("@string")) {
                    //复用已经存在的
                    String id=strDistinctMap.get(value);
                    if(id==null||id.length()<=0){
                        //生成新的id
                        id = currentIdString(fileName);
                        strDistinctMap.put(value,id);
                        strings.add(new StringEntity(id, value));
                    }

                    String newContent = oldContent.toString().replaceFirst("\"" + value + "\"", "\"@string/" + id + "\"");
                    oldContent = oldContent.replace(0, oldContent.length(), newContent);
                }
            }

            Node rightTitleNode = node.getAttributes().getNamedItem("app:rightText");
            if (rightTitleNode != null) {
                String value = rightTitleNode.getNodeValue();
                if (!value.contains("@string")) {
                    //复用已经存在的
                    String id=strDistinctMap.get(value);
                    if(id==null||id.length()<=0){
                        //生成新的id
                        id = currentIdString(fileName);
                        strDistinctMap.put(value,id);
                        strings.add(new StringEntity(id, value));
                    }

                    String newContent = oldContent.toString().replaceFirst("\"" + value + "\"", "\"@string/" + id + "\"");
                    oldContent = oldContent.replace(0, oldContent.length(), newContent);
                }
            }

            Node titleTitleNode = node.getAttributes().getNamedItem("app:titleText");
            if (titleTitleNode != null) {
                String value = titleTitleNode.getNodeValue();
                if (!value.contains("@string")) {
                    //复用已经存在的
                    String id=strDistinctMap.get(value);
                    if(id==null||id.length()<=0){
                        //生成新的id
                        id = currentIdString(fileName);
                        strDistinctMap.put(value,id);
                        strings.add(new StringEntity(id, value));
                    }

                    String newContent = oldContent.toString().replaceFirst("\"" + value + "\"", "\"@string/" + id + "\"");
                    oldContent = oldContent.replace(0, oldContent.length(), newContent);
                }
            }
        }
        NodeList children = node.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            generateStrings(children.item(j), strings, fileName, oldContent);
        }
        return strings;
    }

}
