package com.xxf.i18n.plugin;

import com.xxf.i18n.plugin.bean.StringEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    private static int index = 0;
    public static void main(String[] args) {
        String s=replaceUsingSB("MainActivity.kt"," val DateFormat.displayName: String\n" +
                "        get() = when (this) {\n" +
                "            DateFormat.LL -> com.xxf.application.applicationContext.getString(com.next.space.cflow.resources.R.string.full_date)\n" +
                "            DateFormat.YYYY_MM_DD -> \"年/月/日\"\n" +
                "            DateFormat.DD_MM_YYYY -> \"日/月/年\"\n" +
                "            DateFormat.MM_DD_YYYY -> \"月/日/年\"\n" +
                "            DateFormat.RELATIVE -> \"相对日期\"\n" +
                "            else -> com.xxf.application.applicationContext.getString(com.next.space.cflow.resources.R.string.unknown)\n" +
                "        }\n" +
                "\n" +
                "    val TimeFormat.displayName: String\n" +
                "        get() = when (this) {\n" +
                "            TimeFormat.H_12 -> \"12小时制\"\n" +
                "            TimeFormat.H_24 -> \"24小时制\"\n" +
                "            else -> com.xxf.application.applicationContext.getString(com.next.space.cflow.resources.R.string.unknown)\n" +
                "        }",new ArrayList<>());
        System.out.println("============>s:"+s);
    }
    public static String replaceUsingSB(String fileName, String str, List<StringEntity> strings) {
        StringBuilder sb = new StringBuilder(str.length());
        Pattern p = Pattern.compile("(?=\".{1,20}\")\"[^$+,\\n]*[\\u4E00-\\u9FFF]+[^$+,\\n]*\"");
        Matcher m = p.matcher(str);
        int lastIndex = 0;
        while (m.find()) {
            sb.append(str, lastIndex, m.start());

            final String id = fileName + "_kt_str_" + (index++);
            strings.add(new StringEntity(id, m.group()));

            sb.append("com.xxf.application.applicationContext.getString(com.next.space.cflow.resources.R.string."+id+")");
            lastIndex = m.end();
        }
        sb.append(str.substring(lastIndex));
        return sb.toString();
    }
}
