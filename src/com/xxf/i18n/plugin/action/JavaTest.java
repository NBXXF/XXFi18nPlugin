package com.xxf.i18n.plugin.action;

import com.xxf.i18n.plugin.utils.FileUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class JavaTest {
    public static void main(String[] args) {
        InputStream is = null;
        try {
            //is = file.getInputStream();
            String str="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<resources>\n" +
                    "    <string name=\"flowus_app_name\">FlowUs 息流</string>\n" +
                    "    <string name=\"app_name\">FlowUs 息流</string>\n" +
                    "    <string name=\"ok\">确定</string>\n" +
                    "    <string name=\"ok\">1</string>\n" +
                    "</resources>";
            is =new ByteArrayInputStream(str.getBytes());
            Node node= DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            findStringNode(node);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            FileUtils.closeQuietly(is);
        }

    }

    private static void findStringNode(Node node){
        if (node.getNodeType() == Node.ELEMENT_NODE
                &&"string".equals(node.getNodeName())) {
            Node key = node.getAttributes().getNamedItem("name");
            if(key!=null) {
                System.out.println("=========>nodeType:" + key.getNodeValue() +"    "+ node.getTextContent());
            }
        }
        NodeList children = node.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            findStringNode(children.item(j));
        }
    }

}
