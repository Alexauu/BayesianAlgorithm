package nativebayes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

class Classifier {

    private static List<String> attributes = new ArrayList<>();
    private static Map<String,Set<String>> types = new HashMap<>();
    private static Map<String, Integer> unconditional = new HashMap<>();
    private static Map<String, Integer> conditional = new HashMap<>();
    private static List<String> sample = new ArrayList<>();

    private static int X_COUNT = 0; //样本总数

    static void analyzeData() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("data"));
        String str = reader.readLine();
        attributes.addAll(Arrays.asList(str.split(",")));
        for (String attribute : attributes) {
            types.put(attribute, new HashSet<>());
        }
        System.out.println("读取原始数据如下：");
        System.out.println(str);
        while((str = reader.readLine()) != null){
            System.out.println(str);
            X_COUNT-=-1;
            String[] strings = str.split(",");
            int length = strings.length;
            for (int i = 0; i < length; i++) {
                types.get(attributes.get(i)).add(strings[i]);
                Integer uncon_count;
                if (i+1 == length) {
                    sample.add(str.substring(0,str.lastIndexOf(strings[i])-1).replaceAll(","," "));
                    uncon_count = unconditional.get(strings[length - 1]);
                    unconditional.put(strings[length - 1], uncon_count == null ? 1 : uncon_count + 1);
                    break;
                }
                uncon_count = unconditional.get(strings[i]);
                unconditional.put(strings[i], uncon_count==null?1:uncon_count+1);
                Integer con_count = conditional.get(strings[i] + "|" + strings[length - 1]);
                conditional.put(strings[i] + "|" + strings[length - 1], con_count==null?1:con_count+1);
            }
        }
    }

    static void printResult(){
        System.out.println("统计过程如下：");
        int size = attributes.size();
        String clazz = attributes.get(size - 1);
        for (String s : types.get(clazz)) {
            System.out.println(clazz+"="+s+":" + unconditional.get(s));
        }
        for (int i = 0; i < size-1; i++) {
            String attribute = attributes.get(i);
            for (String s : types.get(attribute)) {
                for (String clz : types.get(clazz)) {
                    Integer count = conditional.get(s + "|" + clz);
                    if (count == null) continue;
                    System.out.println(attribute+"="+s+"|"+clazz+"="+clz+":"+ count);
                }
            }
        }
        System.out.println("计算概率过程如下：");
        for (String s : types.get(clazz)) {
            System.out.println(clazz+"="+s+":" + String.format("%.6f",unconditional.get(s)*1.0/X_COUNT));
        }
        for (int i = 0; i < size-1; i++) {
            String attribute = attributes.get(i);
            for (String s : types.get(attribute)) {
                for (String clz : types.get(clazz)) {
                    Integer count = conditional.get(s + "|" + clz);
                    if (count == null) continue;
                    System.out.println(attribute+"="+s+"|"+clazz+"="+clz+":"+ String.format("%.6f",count*1.0/unconditional.get(clz)));
                }
            }
        }
        System.out.println("经过简单贝叶斯算法重新分类的结果如下：");
        StringBuilder stringBuilder;
        String maxA = "";
        double maxP ;
        for (String s : sample) {
            stringBuilder = new StringBuilder();
            maxP = -1;
            stringBuilder.append(s).append(" 后验概率:");
            for (String clz : types.get(clazz)) {
                stringBuilder.append(clz).append(":");
                double v = V(s, clz);
                stringBuilder.append(String.format("%.6f",v)).append(" ");
                if (v > maxP) {
                    maxP = v;
                    maxA = clz;
                }
            }
            stringBuilder.append("归类:").append(clazz).append("=").append(maxA);
            System.out.println(stringBuilder.toString());
        }

    }

    private static double V(String attribute, String type){
        String[] item = attribute.split(" ");
        double p = 1.0;
        for (String s : item) {
            Integer count = conditional.get(s + "|" + type);
            if (count == null) count = 0;
            p *= count*1.0/unconditional.get(type);
        }
        return unconditional.get(type)*1.0/X_COUNT * p;
    }
}
