package com.xxf.i18n.plugin.action;

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
public class LayoutDirAction extends AnAction {

    private int index = 0;

    @Override
    public void actionPerformed(AnActionEvent e) {

        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (file == null) {
            showError("找不到目标文件");
            return;
        }

        if (!file.isDirectory()) {
            showError("请选择layout或者java文件夹");
            return;
        } else if (!(file.getName().startsWith("layout")||file.getName().startsWith("java"))) {
            showError("请选择layout或者java文件夹");
            return;
        }

        VirtualFile[] children = file.getChildren();
        StringBuilder sb = new StringBuilder();
        VirtualFile resDir = file.getParent();//获取layout文件夹的父文件夹，看是不是res
        if(file.getName().startsWith("layout")) {
            //遍历所有layout文件，然后获取其中的字串写到stringbuilder里面去
            for (VirtualFile child : children) {
                layoutChild(child, sb);
            }
        }else if(file.getName().startsWith("java")){
            resDir =  file.getParent().findChild("res");
            //避免重复 key 中文字符串 value 为已经生成的id
            Map<String,String> strDistinctMap= new  HashMap();
            //遍历所有 kt文件，然后获取其中的字串写到stringbuilder里面去
            for (VirtualFile child : children) {
                classChild(child, sb,strDistinctMap);
            }
        }


        //获取res文件夹下面的values
        if (resDir.getName().equalsIgnoreCase("res")) {
            VirtualFile[] chids = resDir.getChildren(); //获取res文件夹下面文件夹列表
            for (VirtualFile chid : chids) { //遍历寻找values文件夹下面的strings文件
                if (chid.getName().startsWith("values")) {
                    if (chid.isDirectory()) {
                        VirtualFile[] values = chid.getChildren();
                        for (VirtualFile value : values) {
                            if (value.getName().startsWith("strings")) { //找到第一个strings文件
                                try {
                                    String content = new String(value.contentsToByteArray(), "utf-8"); //源文件内容
                                    System.out.println("utf-8=" + content);
                                    String result = content.replace("</resources>", sb.toString() + "\n</resources>"); //在最下方加上新的字串
                                    FileUtils.replaceContentToFile(value.getPath(), result);//替换文件
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                    showError(e1.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }

        e.getActionManager().getAction(IdeActions.ACTION_SYNCHRONIZE).actionPerformed(e);
    }


    /**
     *  执行 java 文件和kt文件
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

            String subStr=m.group();
            //去除前后的双引号
            if(subStr.startsWith("\"")&&subStr.endsWith("\"")){
                subStr=subStr.substring(1,subStr.length()-1);
            }
            //复用已经存在的
            String id=strDistinctMap.get(subStr);
            if(id==null||id.length()<=0){
                //生成新的id
                id = currentIdString(fileName);
                strDistinctMap.put(subStr,id);
                strings.add(new StringEntity(id, subStr));
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
                showError("请选择布局文件");
                return;
            }
        }

//        showHint(file.getName());
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
                    final String id = currentIdString(fileName);
                    strings.add(new StringEntity(id, value));
                    String newContent = oldContent.toString().replaceFirst("\"" + value + "\"", "\"@string/" + id + "\"");
                    oldContent = oldContent.replace(0, oldContent.length(), newContent);
                }
            }
            Node hintNode = node.getAttributes().getNamedItem("android:hint");
            if (hintNode != null) {
                String value = hintNode.getNodeValue();
                if (!value.contains("@string")) {
                    final String id = currentIdString(fileName);
                    strings.add(new StringEntity(id, value));
                    String newContent = oldContent.toString().replaceFirst("\"" + value + "\"", "\"@string/" + id + "\"");
                    oldContent = oldContent.replace(0, oldContent.length(), newContent);
                }
            }

            Node leftTitleNode = node.getAttributes().getNamedItem("app:leftText");
            if (leftTitleNode != null) {
                String value = leftTitleNode.getNodeValue();
                if (!value.contains("@string")) {
                    final String id = currentIdString(fileName);
                    strings.add(new StringEntity(id, value));
                    String newContent = oldContent.toString().replaceFirst("\"" + value + "\"", "\"@string/" + id + "\"");
                    oldContent = oldContent.replace(0, oldContent.length(), newContent);
                }
            }

            Node rightTitleNode = node.getAttributes().getNamedItem("app:rightText");
            if (rightTitleNode != null) {
                String value = rightTitleNode.getNodeValue();
                if (!value.contains("@string")) {
                    final String id = currentIdString(fileName);
                    strings.add(new StringEntity(id, value));
                    String newContent = oldContent.toString().replaceFirst("\"" + value + "\"", "\"@string/" + id + "\"");
                    oldContent = oldContent.replace(0, oldContent.length(), newContent);
                }
            }

            Node titleTitleNode = node.getAttributes().getNamedItem("app:titleText");
            if (titleTitleNode != null) {
                String value = titleTitleNode.getNodeValue();
                if (!value.contains("@string")) {
                    final String id = currentIdString(fileName);
                    strings.add(new StringEntity(id, value));
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


    private void showHint(String msg) {
        Notifications.Bus.notify(new Notification("DavidString", "DavidString", msg, NotificationType.WARNING));
    }

    private void showError(String msg) {
        Notifications.Bus.notify(new Notification("DavidString", "DavidString", msg, NotificationType.ERROR));
    }
}
