package com.xxf.i18n.plugin.utils;

import com.intellij.openapi.vfs.VirtualFile;
import com.xxf.i18n.plugin.bean.StringEntity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

public class AndroidStringFileUtils {

    public static void main(String[] args) {
        String str="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<ScrollView xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n" +
                "    android:id=\"@+id/scrollView\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"match_parent\">\n" +
                "\n" +
                "    <LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "        android:layout_width=\"match_parent\"\n" +
                "        android:layout_height=\"match_parent\"\n" +
                "        android:background=\"@android:color/white\"\n" +
                "        android:orientation=\"vertical\">\n" +
                "\n" +
                "        <com.xxf.view.round.XXFRoundImageTextView\n" +
                "            android:id=\"@+id/textImage\"\n" +
                "            android:layout_width=\"40dp\"\n" +
                "            android:layout_height=\"40dp\"\n" +
                "            android:layout_marginTop=\"40dp\"\n" +
                "            android:background=\"#6cf\" />\n" +
                "\n" +
                "        <View\n" +
                "            android:id=\"@+id/statusbarLayout\"\n" +
                "            android:layout_width=\"wrap_content\"\n" +
                "            android:layout_height=\"25dp\" />\n" +
                "\n" +
                "        <LinearLayout\n" +
                "            android:layout_width=\"match_parent\"\n" +
                "            android:layout_height=\"wrap_content\"\n" +
                "            android:orientation=\"horizontal\">\n" +
                "\n" +
                "            <TextView\n" +
                "                android:layout_width=\"match_parent\"\n" +
                "                android:layout_height=\"match_parent\"\n" +
                "                android:layout_weight=\"1\"\n" +
                "                android:background=\"@drawable/test\"\n" +
                "                android:padding=\"30dp\"\n" +
                "                android:text=\"测试\"\n" +
                "                android:textColor=\"@android:color/white\" />\n" +
                "\n" +
                "            <TextView\n" +
                "                android:layout_width=\"match_parent\"\n" +
                "                android:layout_height=\"match_parent\"\n" +
                "                android:layout_weight=\"1\"\n" +
                "                android:background=\"@drawable/test2\"\n" +
                "                android:gravity=\"right\"\n" +
                "                android:padding=\"20dp\"\n" +
                "                android:text=\"测试\"\n" +
                "                android:textColor=\"@android:color/white\" />\n" +
                "\n" +
                "        </LinearLayout>\n" +
                "\n" +
                "        <com.xxf.view.view.ReverseFrameLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "            android:id=\"@+id/grayLayout\"\n" +
                "            android:layout_width=\"match_parent\"\n" +
                "            android:layout_height=\"match_parent\">\n" +
                "\n" +
                "\n" +
                "            <LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "                android:layout_width=\"match_parent\"\n" +
                "                android:layout_height=\"match_parent\"\n" +
                "                android:background=\"@android:color/white\"\n" +
                "                android:orientation=\"vertical\">\n" +
                "\n" +
                "                <com.xxf.arch.test.MyEditText\n" +
                "                    android:layout_width=\"match_parent\"\n" +
                "                    android:layout_height=\"wrap_content\"\n" +
                "                    android:hint=\"请输入\" />\n" +
                "\n" +
                "                <TextView\n" +
                "                    android:layout_width=\"match_parent\"\n" +
                "                    android:layout_height=\"wrap_content\"\n" +
                "                    android:background=\"@color/BC2\"\n" +
                "                    android:padding=\"10dp\"\n" +
                "                    android:text=\"测试文本1234567abcdefg\"\n" +
                "                    android:textColor=\"@color/C1\" />\n" +
                "\n" +
                "                <TextView\n" +
                "                    android:layout_width=\"match_parent\"\n" +
                "                    android:layout_height=\"wrap_content\"\n" +
                "                    android:background=\"@android:color/black\"\n" +
                "                    android:padding=\"10dp\"\n" +
                "                    android:text=\"测试文本1234567abcdefg\"\n" +
                "                    android:textColor=\"@android:color/white\" />\n" +
                "\n" +
                "\n" +
                "                <Button\n" +
                "                    android:id=\"@+id/bt_http\"\n" +
                "                    android:layout_width=\"match_parent\"\n" +
                "                    android:layout_height=\"wrap_content\"\n" +
                "                    android:background=\"@color/BC2\"\n" +
                "                    android:padding=\"10dp\"\n" +
                "                    android:text=\"http\"\n" +
                "                    android:textColor=\"@color/C1\" />\n" +
                "\n" +
                "                <Button\n" +
                "                    android:id=\"@+id/bt_test\"\n" +
                "                    android:layout_width=\"wrap_content\"\n" +
                "                    android:layout_height=\"wrap_content\"\n" +
                "                    android:padding=\"10dp\"\n" +
                "                    android:text=\"test\" />\n" +
                "\n" +
                "                <Button\n" +
                "                    android:id=\"@+id/bt_permission_req\"\n" +
                "                    android:layout_width=\"wrap_content\"\n" +
                "                    android:layout_height=\"wrap_content\"\n" +
                "                    android:padding=\"10dp\"\n" +
                "                    android:text=\"permission_req\" />\n" +
                "\n" +
                "\n" +
                "                <Button\n" +
                "                    android:id=\"@+id/bt_permission_get\"\n" +
                "                    android:layout_width=\"wrap_content\"\n" +
                "                    android:layout_height=\"wrap_content\"\n" +
                "                    android:padding=\"10dp\"\n" +
                "                    android:text=\"permission_get\" />\n" +
                "\n" +
                "                <Button\n" +
                "                    android:id=\"@+id/file\"\n" +
                "                    android:layout_width=\"wrap_content\"\n" +
                "                    android:layout_height=\"wrap_content\"\n" +
                "                    android:text=\"file\" />\n" +
                "\n" +
                "                <EditText\n" +
                "                    android:layout_width=\"match_parent\"\n" +
                "                    android:layout_height=\"wrap_content\"\n" +
                "                    android:inputType=\"numberDecimal\" />\n" +
                "\n" +
                "\n" +
                "                <Button\n" +
                "                    android:id=\"@+id/bt_startActivityForResult\"\n" +
                "                    android:layout_width=\"wrap_content\"\n" +
                "                    android:layout_height=\"wrap_content\"\n" +
                "                    android:padding=\"10dp\"\n" +
                "                    android:text=\"startActivityForResult\" />\n" +
                "\n" +
                "                <Button\n" +
                "                    android:layout_width=\"wrap_content\"\n" +
                "                    android:layout_height=\"wrap_content\"\n" +
                "                    android:onClick=\"jumpState\"\n" +
                "                    android:padding=\"10dp\"\n" +
                "                    android:text=\"state\" />\n" +
                "                <Button\n" +
                "                    android:id=\"@+id/bt_sp\"\n" +
                "                    android:layout_width=\"wrap_content\"\n" +
                "                    android:layout_height=\"wrap_content\"\n" +
                "                    android:padding=\"10dp\"\n" +
                "                    android:text=\"navigation\" />\n" +
                "\n" +
                "                <FrameLayout\n" +
                "                    android:id=\"@+id/contentPanel\"\n" +
                "                    android:layout_width=\"match_parent\"\n" +
                "                    android:layout_height=\"wrap_content\" />\n" +
                "\n" +
                "\n" +
                "                <androidx.cardview.widget.CardView\n" +
                "                    app:cardCornerRadius=\"8dp\"\n" +
                "                    app:cardElevation=\"10dp\"\n" +
                "                    android:layout_width=\"match_parent\"\n" +
                "                    android:layout_height=\"wrap_content\">\n" +
                "\n" +
                "                    <TextView\n" +
                "                        android:layout_width=\"wrap_content\"\n" +
                "                        android:layout_height=\"wrap_content\"\n" +
                "                        android:padding=\"10dp\"\n" +
                "                        android:text=\"startA\\n\\n\\n\\nctivityForResult\" />\n" +
                "                </androidx.cardview.widget.CardView>\n" +
                "            </LinearLayout>\n" +
                "        </com.xxf.view.view.ReverseFrameLayout>\n" +
                "\n" +
                "        <TextView\n" +
                "            android:layout_width=\"wrap_content\"\n" +
                "            android:layout_height=\"wrap_content\"\n" +
                "            android:text=\"hgdghfdgdf\n" +
                "\\nhsfdhgfghdf\\nhgfdhgfdgdgf\\nsdhggfd\\bgfdhgd\\nhsdfd\\\n" +
                "nsdfhsfd\\nsdhgs\" />\n" +
                "    </LinearLayout>\n" +
                "</ScrollView>";
        InputStream is = null;
        try {
            is =new ByteArrayInputStream(str.getBytes());
            Node node=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            test(node);
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            FileUtils.closeQuietly(is);
        }

    }


    private static void test(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i <attributes.getLength() ; i++) {
                Node item = attributes.item(i);
                String nodeName = item.getNodeName();
                if(Arrays.asList("android:text","android:hint","app:leftText","app:rightText","app:titleText").contains(nodeName)) {
                    String value =item.getNodeValue();
                    if (value!=null && !value.contains("@string")) {
                        System.out.println("=========>" + nodeName+"  "+value);
                    }
                }
            }
        }
        NodeList children = node.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            test(children.item(j));
        }
    }

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
