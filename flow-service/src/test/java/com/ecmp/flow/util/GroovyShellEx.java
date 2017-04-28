package com.ecmp.flow.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import groovy.lang.Binding;
import groovy.lang.GroovyShell;
public class GroovyShellEx {
        public static void main(String[] args) {
//        	    PojoTest1 p = new PojoTest1();
//        	    p.setId("xcvsdfsd");
//        	    p.setDate(new Date());
        	    List<String> listT = new ArrayList<String>();
        	    listT.add("list1");
        	    listT.add("list2");
                Binding bind = new Binding();
                Object da = new Date();
                Object xx ="xx";
                Object yy="yy";
                bind.setVariable("name", "iamzhongyong");
                bind.setVariable("age", "25");     
                bind.setVariable("date", da);     
                bind.setVariable("listT",listT);     
                bind.setVariable("xx",xx);     
                bind.setVariable("yy",yy);     
                GroovyShell shell = new GroovyShell(bind);               
                Object obj = shell.evaluate("return xx+yy");               
                System.out.println(obj);
                //listT.contains("list1");
//                Object obj2 = shell.evaluate("str =pojoTest.getId() ;return str");               
//                System.out.println(obj2);
//                
//                new Date().after(p.getDate());
                
                Object obj3 = shell.evaluate("str =new Date().after(date);return str");               
                System.out.println(obj3);
                
                Object obj4 = shell.evaluate("str = !listT.contains('list1');return str");               
                System.out.println(obj4);
        }
}
