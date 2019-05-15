package com.ecmp.flow.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：xml
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/12 9:04      陈飞(fly)                  新建
 * <p/>
 * *************************************************************************************************
 */
public class XmlUtil {

    /**
     * javabean转xml
     * @param entity
     * @return
     */
    public static String serialize(Object entity) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(entity.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        StringWriter writer = new StringWriter();
        marshaller.marshal(entity, writer);
        return writer.toString();
    }

    // 去掉首尾指定字符串
    public static String trimFirstAndLastChar(String source,char element){

        boolean beginIndexFlag = true;

        boolean endIndexFlag = true;

        do{

            int beginIndex = source.indexOf(element) == 0 ? 1 : 0;

            int endIndex = source.lastIndexOf(element) + 1 == source.length() ? source.lastIndexOf(element) : source.length();

            source = source.substring(beginIndex, endIndex);

            beginIndexFlag = (source.indexOf(element) == 0);

            endIndexFlag = (source.lastIndexOf(element) + 1 == source.length());

        } while (beginIndexFlag || endIndexFlag);

        return source;
    }
}
