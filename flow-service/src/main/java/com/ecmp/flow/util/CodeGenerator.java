package com.ecmp.flow.util;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：生成随机字符串
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/6/21 11:08      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CodeGenerator {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        List<String> results=genCodes(6,1);
        System.out.println(results.size()+":"+results.get(0));

        String s = "Java Code Geeks";
        System.out.println("Original String: " + s);

        String su = s.toUpperCase();
        System.out.println("String to upper case: " + su);
    }

    /**
     * 生成随机字符
     * @param length  长度
     * @param num   生成个数
     * @return
     */
    public static List<String> genCodes(int length,long num){
        List<String> results=new ArrayList<String>();
        for(int j=0;j<num;j++){
            String val = "";
            Random random = new Random();
            for(int i = 0; i < length; i++)
            {
                String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num"; // 输出字母还是数字

                if("char".equalsIgnoreCase(charOrNum)) // 字符串
                {
                    int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; //取得大写字母还是小写字母
                    val += (char) (choice + random.nextInt(26));
                }
                else if("num".equalsIgnoreCase(charOrNum)) // 数字
                {
                    val += String.valueOf(random.nextInt(10));
                }
            }
            val=val.toUpperCase();
            if(results.contains(val)){
                continue;
            }else{
                results.add(val);
            }
        }
        return results;
    }


}
