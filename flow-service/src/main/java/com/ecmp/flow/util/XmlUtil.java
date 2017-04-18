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
    public static String SERIALIZE(Object entity) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(entity.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        StringWriter writer = new StringWriter();
        marshaller.marshal(entity, writer);
        return writer.toString();
    }
}
