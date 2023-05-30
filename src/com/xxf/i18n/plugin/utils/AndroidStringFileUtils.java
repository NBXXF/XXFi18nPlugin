package com.xxf.i18n.plugin.utils;

import com.intellij.openapi.vfs.VirtualFile;
import com.xxf.i18n.plugin.bean.StringEntity;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

public class AndroidStringFileUtils {

    /**
     * 查找到重复的字符串记录
     *
     * @param file
     * @return        //value keys
     */
   public static Map<String, List<String>> getRepeatRecords(VirtualFile file){
       //value keys
        Map<String, List<String>> recordMap=new LinkedHashMap<>();
        InputStream is = null;
        try {
            is = file.getInputStream();
            Node node= DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            findStringNode(node,recordMap);
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            FileUtils.closeQuietly(is);
        }

       //重复的ids
       Map<String, List<String>> repeatMap=new LinkedHashMap<>();
       for(Map.Entry<String,List<String>> entry : recordMap.entrySet()){
           if(entry.getValue()!=null&&entry.getValue().size()>1) {
               repeatMap.put(entry.getKey(), entry.getValue());
           }
       }
        return repeatMap;
    }

    private static void findStringNode(Node node, Map<String, List<String>> recordMap){
        if (node.getNodeType() == Node.ELEMENT_NODE
                &&"string".equals(node.getNodeName())) {
            Node key = node.getAttributes().getNamedItem("name");
            if(key!=null) {
                String valueKey=node.getTextContent();
                List<String> ids=recordMap.getOrDefault(valueKey,new ArrayList<>());
                ids.add(key.getNodeValue());
                recordMap.put(valueKey,ids);
            }
        }
        NodeList children = node.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            findStringNode(children.item(j),recordMap);
        }
    }
}
