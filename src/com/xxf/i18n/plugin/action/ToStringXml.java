package com.xxf.i18n.plugin.action;

import com.xxf.i18n.plugin.bean.StringEntity;
import com.xxf.i18n.plugin.utils.FileUtils;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.vfs.VirtualFile;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class ToStringXml extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (file == null) {
            showHint("找不到目标文件");
            return;
        }

        String extension = file.getExtension();
        if (extension != null && extension.equalsIgnoreCase("xml")) {
            if (!file.getParent().getName().startsWith("layout")) {
                showError("请选择布局文件");
                return;
            }
        }

        //获取当前编辑器对象
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        //获取选择的数据模型
        SelectionModel selectionModel = editor.getSelectionModel();
        //获取当前选择的文本
        String selectedText = selectionModel.getSelectedText();

        StringBuilder sb = new StringBuilder();

        try {
            StringEntity singleStrings;
            StringBuilder oldContent = new StringBuilder(); //整个file字串
            try {
                oldContent.append(new String(file.contentsToByteArray(), "utf-8")); //源文件整体字符串
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            InputStream is = null;
            try {
                is = file.getInputStream(); //源文件layout下面的xml
                singleStrings = extraStringEntity(is, file.getNameWithoutExtension().toLowerCase(), oldContent,
                        selectionModel.getSelectionEndPosition().line,selectedText);
                if (singleStrings != null) {
                    sb.append("\n    <string name=\"" + singleStrings.getId() + "\">" + singleStrings.getValue() + "</string>");
                    FileUtils.replaceContentToFile(file.getPath(), oldContent.toString()); //保存到layout.xml
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                FileUtils.closeQuietly(is);
            }

        }catch (Exception ioException) {
            ioException.printStackTrace();
        }

        //保存到strings.xml
        VirtualFile resDir = file.getParent().getParent();//获取layout文件夹的父文件夹，看是不是res
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
                                    showHint("转换成功!");
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

        //System.out.println(selectedText);

    }


    private int index = 0;
   /* private void layoutChild(VirtualFile file, StringBuilder sb) {
        index = 0;

        String extension = file.getExtension();
        if (extension != null && extension.equalsIgnoreCase("xml")) {
            if (!file.getParent().getName().startsWith("layout")) {
                showError("请选择布局文件");
                return;
            }
        }

        List<StringEntity> strings;
        StringBuilder oldContent = new StringBuilder(); //整个file字串
        try {
            oldContent.append(new String(file.contentsToByteArray(), "utf-8")); //源文件整体字符串
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        InputStream is = null;
        try {
            is = file.getInputStream(); //源文件layout下面的xml
            strings = extraStringEntity(is, file.getNameWithoutExtension().toLowerCase(), oldContent);
            if (strings != null) {
                for (StringEntity string : strings) { //创建字符串
                    sb.append("\n    <string name=\"" + string.getId() + "\">" + string.getValue() + "</string>");
                }
                FileUtils.replaceContentToFile(file.getPath(), oldContent.toString());
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            FileUtils.closeQuietly(is);
        }

    }*/

    /*
    * 传入源xml的文件流。文件名称，文件字符串
    * */
    private StringEntity extraStringEntity(InputStream is, String fileName, StringBuilder oldContent,int line,String oricontent) {

        try {
            return generateStrings(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is), fileName, oldContent,line, oricontent);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    * 传入
    * */
    private StringEntity generateStrings(Node node, String fileName, StringBuilder oldContent,int line,String oricontent) {
        StringEntity result = new StringEntity();
        if (node.getNodeType() == Node.ELEMENT_NODE) {//文件换行节点
            Node stringNode = node.getAttributes().getNamedItem("android:text"); //获取该名字的节点
            if (stringNode != null) { //有值
                String value = stringNode.getNodeValue();
                if (!value.contains("@string")&&value.contains(oricontent)) { //判断是否已经是@字符串
                    final String id = fileName + "_text_" + (line); //命名方式：文件名称_text+_index
                    result=new StringEntity(id, value);
                    String newContent = oldContent.toString().replaceFirst("\"" + value + "\"", "\"@string/" + id + "\"");
                    oldContent = oldContent.replace(0, oldContent.length(), newContent);
                    return result;
                }
            }

            Node hintNode = node.getAttributes().getNamedItem("android:hint");
            if (hintNode != null) {
                String value = hintNode.getNodeValue();
                if (!value.contains("@string")&&value.contains(oricontent)) {
                    final String id = fileName + "_hint_text_" + (line);
                    result=new StringEntity(id, value);
                    String newContent = oldContent.toString().replaceFirst("\"" + value + "\"", "\"@string/" + id + "\"");
                    oldContent = oldContent.replace(0, oldContent.length(), newContent);
                    return result;
                }
            }

        }



        NodeList children = node.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            StringEntity itemresult = generateStrings(children.item(j), fileName, oldContent,line,oricontent);
            if (itemresult!=null){
                return itemresult;
            }
        }
        return null;
    }





    private void showHint(String msg) {
        Notifications.Bus.notify(new Notification("DavidString", "DavidString", msg, NotificationType.WARNING));
    }
    private void showError(String msg) {
        Notifications.Bus.notify(new Notification("DavidString", "DavidString", msg, NotificationType.ERROR));
    }

}
