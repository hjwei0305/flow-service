//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.sf.json;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.sf.ezmorph.Morpher;
import net.sf.ezmorph.array.ObjectArrayMorpher;
import net.sf.ezmorph.bean.BeanMorpher;
import net.sf.ezmorph.object.IdentityObjectMorpher;
import net.sf.json.processors.JsonBeanProcessor;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.processors.JsonVerifier;
import net.sf.json.processors.PropertyNameProcessor;
import net.sf.json.regexp.RegexpUtils;
import net.sf.json.util.CycleDetectionStrategy;
import net.sf.json.util.EnumMorpher;
import net.sf.json.util.JSONTokener;
import net.sf.json.util.JSONUtils;
import net.sf.json.util.PropertyFilter;
import net.sf.json.util.PropertySetStrategy;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class JSONObject extends AbstractJSON implements JSON, Map, Comparable {
    private static final Log log = LogFactory.getLog(JSONObject.class);
    private boolean nullObject;
    private Map properties;

    public static JSONObject fromObject(Object object) {
        return fromObject(object, new JsonConfig());
    }

    public static JSONObject fromObject(Object object, JsonConfig jsonConfig) {
        if (object != null && !JSONUtils.isNull(object)) {
            if (object instanceof Enum) {
                throw new JSONException("'object' is an Enum. Use JSONArray instead");
            } else if (object instanceof Annotation || object != null && object.getClass().isAnnotation()) {
                throw new JSONException("'object' is an Annotation.");
            } else if (object instanceof JSONObject) {
                return _fromJSONObject((JSONObject)object, jsonConfig);
            } else if (object instanceof DynaBean) {
                return _fromDynaBean((DynaBean)object, jsonConfig);
            } else if (object instanceof JSONTokener) {
                return _fromJSONTokener((JSONTokener)object, jsonConfig);
            } else if (object instanceof JSONString) {
                return _fromJSONString((JSONString)object, jsonConfig);
            } else if (object instanceof Map) {
                return _fromMap((Map)object, jsonConfig);
            } else if (object instanceof String) {
                return _fromString((String)object, jsonConfig);
            } else if (!JSONUtils.isNumber(object) && !JSONUtils.isBoolean(object) && !JSONUtils.isString(object)) {
                if (JSONUtils.isArray(object)) {
                    throw new JSONException("'object' is an array. Use JSONArray instead");
                } else {
                    return _fromBean(object, jsonConfig);
                }
            } else {
                return new JSONObject();
            }
        } else {
            return new JSONObject(true);
        }
    }

    public static Object toBean(JSONObject jsonObject) {
        if (jsonObject != null && !jsonObject.isNullObject()) {
            DynaBean dynaBean = null;
            JsonConfig jsonConfig = new JsonConfig();
            Map props = JSONUtils.getProperties(jsonObject);
            dynaBean = JSONUtils.newDynaBean(jsonObject, jsonConfig);
            Iterator entries = jsonObject.names(jsonConfig).iterator();

            while(entries.hasNext()) {
                String name = (String)entries.next();
                String key = JSONUtils.convertToJavaIdentifier(name, jsonConfig);
                Class type = (Class)props.get(name);
                Object value = jsonObject.get(name);

                try {
                    if (!JSONUtils.isNull(value)) {
                        if (value instanceof JSONArray) {
                            dynaBean.set(key, JSONArray.toCollection((JSONArray)value));
                        } else if (!String.class.isAssignableFrom(type) && !Boolean.class.isAssignableFrom(type) && !JSONUtils.isNumber(type) && !Character.class.isAssignableFrom(type) && !JSONFunction.class.isAssignableFrom(type)) {
                            dynaBean.set(key, toBean((JSONObject)value));
                        } else {
                            dynaBean.set(key, value);
                        }
                    } else if (type.isPrimitive()) {
                        log.warn("Tried to assign null value to " + key + ":" + type.getName());
                        dynaBean.set(key, JSONUtils.getMorpherRegistry().morph(type, (Object)null));
                    } else {
                        dynaBean.set(key, (Object)null);
                    }
                } catch (JSONException var10) {
                    throw var10;
                } catch (Exception var11) {
                    throw new JSONException("Error while setting property=" + name + " type" + type, var11);
                }
            }

            return dynaBean;
        } else {
            return null;
        }
    }

    public static Object toBean(JSONObject jsonObject, Class beanClass) {
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setRootClass(beanClass);
        return toBean(jsonObject, jsonConfig);
    }

    public static Object toBean(JSONObject jsonObject, Class beanClass, Map classMap) {
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setRootClass(beanClass);
        jsonConfig.setClassMap(classMap);
        return toBean(jsonObject, jsonConfig);
    }

    public static Object toBean(JSONObject jsonObject, JsonConfig jsonConfig) {
        if (jsonObject != null && !jsonObject.isNullObject()) {
            Class beanClass = jsonConfig.getRootClass();
            Map classMap = jsonConfig.getClassMap();
            if (beanClass == null) {
                return toBean(jsonObject);
            } else {
                if (classMap == null) {
                    classMap = Collections.EMPTY_MAP;
                }

                Object bean = null;

                try {
                    if (beanClass.isInterface()) {
                        if (!Map.class.isAssignableFrom(beanClass)) {
                            throw new JSONException("beanClass is an interface. " + beanClass);
                        }

                        bean = new HashMap();
                    } else {
                        bean = jsonConfig.getNewBeanInstanceStrategy().newInstance(beanClass, jsonObject);
                    }
                } catch (JSONException var18) {
                    throw var18;
                } catch (Exception var19) {
                    throw new JSONException(var19);
                }

                Map props = JSONUtils.getProperties(jsonObject);
                PropertyFilter javaPropertyFilter = jsonConfig.getJavaPropertyFilter();
                Iterator entries = jsonObject.names(jsonConfig).iterator();

                while(true) {
                    String name;
                    Class type;
                    Object value;
                    do {
                        if (!entries.hasNext()) {
                            return bean;
                        }

                        name = (String)entries.next();
                        type = (Class)props.get(name);
                        value = jsonObject.get(name);
                    } while(javaPropertyFilter != null && javaPropertyFilter.apply(bean, name, value));

                    String key = Map.class.isAssignableFrom(beanClass) && jsonConfig.isSkipJavaIdentifierTransformationInMapKeys() ? name : JSONUtils.convertToJavaIdentifier(name, jsonConfig);
                    PropertyNameProcessor propertyNameProcessor = jsonConfig.findJavaPropertyNameProcessor(beanClass);
                    if (propertyNameProcessor != null) {
                        key = propertyNameProcessor.processPropertyName(beanClass, key);
                    }

                    try {
                        if (Map.class.isAssignableFrom(beanClass)) {
                            if (JSONUtils.isNull(value)) {
                                setProperty(bean, key, value, jsonConfig);
                            } else if (value instanceof JSONArray) {
                                setProperty(bean, key, convertPropertyValueToCollection(key, value, jsonConfig, name, classMap, List.class), jsonConfig);
                            } else if (!String.class.isAssignableFrom(type) && !JSONUtils.isBoolean(type) && !JSONUtils.isNumber(type) && !JSONUtils.isString(type) && !JSONFunction.class.isAssignableFrom(type)) {
                                Class targetClass = resolveClass(classMap, key, name, type);
                                JsonConfig jsc = jsonConfig.copy();
                                jsc.setRootClass(targetClass);
                                jsc.setClassMap(classMap);
                                if (targetClass != null) {
                                    setProperty(bean, key, toBean((JSONObject)value, jsc), jsonConfig);
                                } else {
                                    setProperty(bean, key, toBean((JSONObject)value), jsonConfig);
                                }
                            } else if (jsonConfig.isHandleJettisonEmptyElement() && "".equals(value)) {
                                setProperty(bean, key, (Object)null, jsonConfig);
                            } else {
                                setProperty(bean, key, value, jsonConfig);
                            }
                        } else {
                            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(bean, key);
                            if (pd != null && pd.getWriteMethod() == null) {
                                log.info("Property '" + key + "' of " + bean.getClass() + " has no write method. SKIPPED.");
                            } else {
                                Class targetType;
                                JsonConfig jsc;
                                if (pd == null) {
                                    if (!JSONUtils.isNull(value)) {
                                        if (value instanceof JSONArray) {
                                            setProperty(bean, key, convertPropertyValueToCollection(key, value, jsonConfig, name, classMap, List.class), jsonConfig);
                                        } else if (!String.class.isAssignableFrom(type) && !JSONUtils.isBoolean(type) && !JSONUtils.isNumber(type) && !JSONUtils.isString(type) && !JSONFunction.class.isAssignableFrom(type)) {
                                            if (jsonConfig.isHandleJettisonSingleElementArray()) {
                                                targetType = resolveClass(classMap, key, name, type);
                                                jsc = jsonConfig.copy();
                                                jsc.setRootClass(targetType);
                                                jsc.setClassMap(classMap);
                                                setProperty(bean, key, toBean((JSONObject)value, jsc), jsonConfig);
                                            } else {
                                                setProperty(bean, key, value, jsonConfig);
                                            }
                                        } else if (beanClass != null && !(bean instanceof Map) && jsonConfig.getPropertySetStrategy() == null && jsonConfig.isIgnorePublicFields()) {
//                                            log.warn("Tried to assign property " + key + ":" + type.getName() + " to bean of class " + bean.getClass().getName());
                                        } else {
                                            setProperty(bean, key, value, jsonConfig);
                                        }
                                    } else if (type.isPrimitive()) {
                                        log.warn("Tried to assign null value to " + key + ":" + type.getName());
                                        setProperty(bean, key, JSONUtils.getMorpherRegistry().morph(type, (Object)null), jsonConfig);
                                    } else {
                                        setProperty(bean, key, (Object)null, jsonConfig);
                                    }
                                } else {
                                    targetType = pd.getPropertyType();
                                    if (!JSONUtils.isNull(value)) {
                                        if (value instanceof JSONArray) {
                                            if (List.class.isAssignableFrom(pd.getPropertyType())) {
                                                setProperty(bean, key, convertPropertyValueToCollection(key, value, jsonConfig, name, classMap, pd.getPropertyType()), jsonConfig);
                                            } else if (Set.class.isAssignableFrom(pd.getPropertyType())) {
                                                setProperty(bean, key, convertPropertyValueToCollection(key, value, jsonConfig, name, classMap, pd.getPropertyType()), jsonConfig);
                                            } else {
                                                setProperty(bean, key, convertPropertyValueToArray(key, value, targetType, jsonConfig, classMap), jsonConfig);
                                            }
                                        } else if (!String.class.isAssignableFrom(type) && !JSONUtils.isBoolean(type) && !JSONUtils.isNumber(type) && !JSONUtils.isString(type) && !JSONFunction.class.isAssignableFrom(type)) {
                                            if (jsonConfig.isHandleJettisonSingleElementArray()) {
                                                JSONArray array = (new JSONArray()).element(value, jsonConfig);
                                                Class newTargetClass = resolveClass(classMap, key, name, type);
                                                jsc = jsonConfig.copy();
                                                jsc.setRootClass(newTargetClass);
                                                jsc.setClassMap(classMap);
                                                if (targetType.isArray()) {
                                                    setProperty(bean, key, JSONArray.toArray(array, jsc), jsonConfig);
                                                } else if (JSONArray.class.isAssignableFrom(targetType)) {
                                                    setProperty(bean, key, array, jsonConfig);
                                                } else if (!List.class.isAssignableFrom(targetType) && !Set.class.isAssignableFrom(targetType)) {
                                                    setProperty(bean, key, toBean((JSONObject)value, jsc), jsonConfig);
                                                } else {
                                                    jsc.setCollectionType(targetType);
                                                    setProperty(bean, key, JSONArray.toCollection(array, jsc), jsonConfig);
                                                }
                                            } else {
                                                if (targetType == Object.class || targetType.isInterface()) {
                                                    Class targetTypeCopy = targetType;
                                                    targetType = findTargetClass(key, classMap);
                                                    targetType = targetType == null ? findTargetClass(name, classMap) : targetType;
                                                    targetType = targetType == null && targetTypeCopy.isInterface() ? targetTypeCopy : targetType;
                                                }

                                                jsc = jsonConfig.copy();
                                                jsc.setRootClass(targetType);
                                                jsc.setClassMap(classMap);
                                                setProperty(bean, key, toBean((JSONObject)value, jsc), jsonConfig);
                                            }
                                        } else if (pd != null) {
                                            if (jsonConfig.isHandleJettisonEmptyElement() && "".equals(value)) {
                                                setProperty(bean, key, (Object)null, jsonConfig);
                                            } else if (!targetType.isInstance(value)) {
                                                setProperty(bean, key, morphPropertyValue(key, value, type, targetType), jsonConfig);
                                            } else {
                                                setProperty(bean, key, value, jsonConfig);
                                            }
                                        } else if (beanClass != null && !(bean instanceof Map)) {
                                            log.warn("Tried to assign property " + key + ":" + type.getName() + " to bean of class " + bean.getClass().getName());
                                        } else {
                                            setProperty(bean, key, value, jsonConfig);
                                        }
                                    } else if (type.isPrimitive()) {
                                        log.warn("Tried to assign null value to " + key + ":" + type.getName());
                                        setProperty(bean, key, JSONUtils.getMorpherRegistry().morph(type, (Object)null), jsonConfig);
                                    } else {
                                        setProperty(bean, key, (Object)null, jsonConfig);
                                    }
                                }
                            }
                        }
                    } catch (JSONException var20) {
                        throw var20;
                    } catch (Exception var21) {
                        throw new JSONException("Error while setting property=" + name + " type " + type, var21);
                    }
                }
            }
        } else {
            return null;
        }
    }

    public static Object toBean(JSONObject jsonObject, Object root, JsonConfig jsonConfig) {
        if (jsonObject != null && !jsonObject.isNullObject() && root != null) {
            Class rootClass = root.getClass();
            if (rootClass.isInterface()) {
                throw new JSONException("Root bean is an interface. " + rootClass);
            } else {
                Map classMap = jsonConfig.getClassMap();
                if (classMap == null) {
                    classMap = Collections.EMPTY_MAP;
                }

                Map props = JSONUtils.getProperties(jsonObject);
                PropertyFilter javaPropertyFilter = jsonConfig.getJavaPropertyFilter();
                Iterator entries = jsonObject.names(jsonConfig).iterator();

                while(true) {
                    String name;
                    Class type;
                    Object value;
                    do {
                        if (!entries.hasNext()) {
                            return root;
                        }

                        name = (String)entries.next();
                        type = (Class)props.get(name);
                        value = jsonObject.get(name);
                    } while(javaPropertyFilter != null && javaPropertyFilter.apply(root, name, value));

                    String key = JSONUtils.convertToJavaIdentifier(name, jsonConfig);

                    try {
                        PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(root, key);
                        if (pd != null && pd.getWriteMethod() == null) {
                            log.info("Property '" + key + "' of " + root.getClass() + " has no write method. SKIPPED.");
                        } else if (!JSONUtils.isNull(value)) {
                            Class targetClass;
                            Object newRoot;
                            if (!(value instanceof JSONArray)) {
                                if (!String.class.isAssignableFrom(type) && !JSONUtils.isBoolean(type) && !JSONUtils.isNumber(type) && !JSONUtils.isString(type) && !JSONFunction.class.isAssignableFrom(type)) {
                                    if (pd != null) {
                                        targetClass = pd.getPropertyType();
                                        if (jsonConfig.isHandleJettisonSingleElementArray()) {
                                            JSONArray array = (new JSONArray()).element(value, jsonConfig);
                                            Class newTargetClass = resolveClass(classMap, key, name, type);
                                            newRoot = jsonConfig.getNewBeanInstanceStrategy().newInstance(newTargetClass, (JSONObject)value);
                                            if (targetClass.isArray()) {
                                                setProperty(root, key, JSONArray.toArray(array, newRoot, jsonConfig), jsonConfig);
                                            } else if (Collection.class.isAssignableFrom(targetClass)) {
                                                setProperty(root, key, JSONArray.toList(array, newRoot, jsonConfig), jsonConfig);
                                            } else if (JSONArray.class.isAssignableFrom(targetClass)) {
                                                setProperty(root, key, array, jsonConfig);
                                            } else {
                                                setProperty(root, key, toBean((JSONObject)value, newRoot, jsonConfig), jsonConfig);
                                            }
                                        } else {
                                            if (targetClass == Object.class) {
                                                targetClass = resolveClass(classMap, key, name, type);
                                                if (targetClass == null) {
                                                    targetClass = Object.class;
                                                }
                                            }

                                            newRoot = jsonConfig.getNewBeanInstanceStrategy().newInstance(targetClass, (JSONObject)value);
                                            setProperty(root, key, toBean((JSONObject)value, newRoot, jsonConfig), jsonConfig);
                                        }
                                    } else if (root instanceof Map) {
                                        targetClass = findTargetClass(key, classMap);
                                        targetClass = targetClass == null ? findTargetClass(name, classMap) : targetClass;
                                        newRoot = jsonConfig.getNewBeanInstanceStrategy().newInstance(targetClass, (JSONObject)null);
                                        setProperty(root, key, toBean((JSONObject)value, newRoot, jsonConfig), jsonConfig);
                                    } else {
                                        log.warn("Tried to assign property " + key + ":" + type.getName() + " to bean of class " + rootClass.getName());
                                    }
                                } else if (pd != null) {
                                    if (jsonConfig.isHandleJettisonEmptyElement() && "".equals(value)) {
                                        setProperty(root, key, (Object)null, jsonConfig);
                                    } else if (!pd.getPropertyType().isInstance(value)) {
                                        Morpher morpher = JSONUtils.getMorpherRegistry().getMorpherFor(pd.getPropertyType());
                                        if (IdentityObjectMorpher.getInstance().equals(morpher)) {
                                            log.warn("Can't transform property '" + key + "' from " + type.getName() + " into " + pd.getPropertyType().getName() + ". Will register a default BeanMorpher");
                                            JSONUtils.getMorpherRegistry().registerMorpher(new BeanMorpher(pd.getPropertyType(), JSONUtils.getMorpherRegistry()));
                                        }

                                        setProperty(root, key, JSONUtils.getMorpherRegistry().morph(pd.getPropertyType(), value), jsonConfig);
                                    } else {
                                        setProperty(root, key, value, jsonConfig);
                                    }
                                } else if (root instanceof Map) {
                                    setProperty(root, key, value, jsonConfig);
                                } else {
                                    log.warn("Tried to assign property " + key + ":" + type.getName() + " to bean of class " + root.getClass().getName());
                                }
                            } else if (pd != null && !List.class.isAssignableFrom(pd.getPropertyType())) {
                                targetClass = JSONUtils.getInnerComponentType(pd.getPropertyType());
                                Class targetInnerType = findTargetClass(key, classMap);
                                if (targetClass.equals(Object.class) && targetInnerType != null && !targetInnerType.equals(Object.class)) {
                                    targetClass = targetInnerType;
                                }

                                newRoot = jsonConfig.getNewBeanInstanceStrategy().newInstance(targetClass, (JSONObject)null);
                                newRoot = JSONArray.toArray((JSONArray)value, newRoot, jsonConfig);
                                if (!targetClass.isPrimitive() && !JSONUtils.isNumber(targetClass) && !Boolean.class.isAssignableFrom(targetClass) && !JSONUtils.isString(targetClass)) {
                                    if (!newRoot.getClass().equals(pd.getPropertyType()) && !pd.getPropertyType().equals(Object.class)) {
                                        Morpher morpher = JSONUtils.getMorpherRegistry().getMorpherFor(Array.newInstance(targetClass, 0).getClass());
                                        if (IdentityObjectMorpher.getInstance().equals(morpher)) {
                                            ObjectArrayMorpher beanMorpher = new ObjectArrayMorpher(new BeanMorpher(targetClass, JSONUtils.getMorpherRegistry()));
                                            JSONUtils.getMorpherRegistry().registerMorpher(beanMorpher);
                                        }

                                        newRoot = JSONUtils.getMorpherRegistry().morph(Array.newInstance(targetClass, 0).getClass(), newRoot);
                                    }
                                } else {
                                    newRoot = JSONUtils.getMorpherRegistry().morph(Array.newInstance(targetClass, 0).getClass(), newRoot);
                                }

                                setProperty(root, key, newRoot, jsonConfig);
                            } else {
                                targetClass = resolveClass(classMap, key, name, type);
                                newRoot = jsonConfig.getNewBeanInstanceStrategy().newInstance(targetClass, (JSONObject)null);
                                List list = JSONArray.toList((JSONArray)value, newRoot, jsonConfig);
                                setProperty(root, key, list, jsonConfig);
                            }
                        } else if (type.isPrimitive()) {
                            log.warn("Tried to assign null value to " + key + ":" + type.getName());
                            setProperty(root, key, JSONUtils.getMorpherRegistry().morph(type, (Object)null), jsonConfig);
                        } else {
                            setProperty(root, key, (Object)null, jsonConfig);
                        }
                    } catch (JSONException var19) {
                        throw var19;
                    } catch (Exception var20) {
                        throw new JSONException("Error while setting property=" + name + " type " + type, var20);
                    }
                }
            }
        } else {
            return root;
        }
    }

    private static JSONObject _fromBean(Object bean, JsonConfig jsonConfig) {
        if (!addInstance(bean)) {
            try {
                return jsonConfig.getCycleDetectionStrategy().handleRepeatedReferenceAsObject(bean);
            } catch (JSONException var6) {
                removeInstance(bean);
                fireErrorEvent(var6, jsonConfig);
                throw var6;
            } catch (RuntimeException var7) {
                removeInstance(bean);
                JSONException jsone = new JSONException(var7);
                fireErrorEvent(jsone, jsonConfig);
                throw jsone;
            }
        } else {
            fireObjectStartEvent(jsonConfig);
            JsonBeanProcessor processor = jsonConfig.findJsonBeanProcessor(bean.getClass());
            JSONObject json;
            if (processor != null) {
                json = null;

                try {
                    json = processor.processBean(bean, jsonConfig);
                    if (json == null) {
                        json = (JSONObject)jsonConfig.findDefaultValueProcessor(bean.getClass()).getDefaultValue(bean.getClass());
                        if (json == null) {
                            json = new JSONObject(true);
                        }
                    }

                    removeInstance(bean);
                    fireObjectEndEvent(jsonConfig);
                    return json;
                } catch (JSONException var8) {
                    removeInstance(bean);
                    fireErrorEvent(var8, jsonConfig);
                    throw var8;
                } catch (RuntimeException var9) {
                    removeInstance(bean);
                    JSONException jsone = new JSONException(var9);
                    fireErrorEvent(jsone, jsonConfig);
                    throw jsone;
                }
            } else {
                json = defaultBeanProcessing(bean, jsonConfig);
                removeInstance(bean);
                fireObjectEndEvent(jsonConfig);
                return json;
            }
        }
    }

    private static JSONObject defaultBeanProcessing(Object bean, JsonConfig jsonConfig) {
        Class beanClass = bean.getClass();
        PropertyNameProcessor propertyNameProcessor = jsonConfig.findJsonPropertyNameProcessor(beanClass);
        Collection exclusions = jsonConfig.getMergedExcludes(beanClass);
        JSONObject jsonObject = new JSONObject();

        try {
            PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(bean);
            PropertyFilter jsonPropertyFilter = jsonConfig.getJsonPropertyFilter();

            String key;
            for(int i = 0; i < pds.length; ++i) {
                boolean bypass = false;
                 key = pds[i].getName();
                if (!exclusions.contains(key) && (!jsonConfig.isIgnoreTransientFields() || !isTransientField(key, beanClass, jsonConfig))) {
                    Class type = pds[i].getPropertyType();

                    try {
                        pds[i].getReadMethod();
                    } catch (Exception var17) {
                        String warning = "Property '" + key + "' of " + beanClass + " has no read method. SKIPPED";
                        fireWarnEvent(warning, jsonConfig);
                        log.info(warning);
                        continue;
                    }

                    if (pds[i].getReadMethod() != null) {
                        if (!isTransient(pds[i].getReadMethod(), jsonConfig)) {
                            Object value = PropertyUtils.getProperty(bean, key);
                            if (jsonPropertyFilter == null || !jsonPropertyFilter.apply(bean, key, value)) {
                                JsonValueProcessor jsonValueProcessor = jsonConfig.findJsonValueProcessor(beanClass, type, key);
                                if (jsonValueProcessor != null) {
                                    value = jsonValueProcessor.processObjectValue(key, value, jsonConfig);
                                    bypass = true;
                                    if (!JsonVerifier.isValidJsonValue(value)) {
                                        throw new JSONException("Value is not a valid JSON value. " + value);
                                    }
                                }

                                if (propertyNameProcessor != null) {
                                    key = propertyNameProcessor.processPropertyName(beanClass, key);
                                }

                                setValue(jsonObject, key, value, type, jsonConfig, bypass);
                            }
                        }
                    } else {
                        key = "Property '" + key + "' of " + beanClass + " has no read method. SKIPPED";
                        fireWarnEvent(key, jsonConfig);
                        log.info(key);
                    }
                }
            }

            try {
                if (!jsonConfig.isIgnorePublicFields()) {
                    Field[] fields = beanClass.getFields();

                    for(int i = 0; i < fields.length; ++i) {
                        boolean bypass = false;
                        Field field = fields[i];
                        key = field.getName();
                        if (!exclusions.contains(key) && (!jsonConfig.isIgnoreTransientFields() || !isTransient(field, jsonConfig))) {
                            Class type = field.getType();
                            Object value = field.get(bean);
                            if (jsonPropertyFilter == null || !jsonPropertyFilter.apply(bean, key, value)) {
                                JsonValueProcessor jsonValueProcessor = jsonConfig.findJsonValueProcessor(beanClass, type, key);
                                if (jsonValueProcessor != null) {
                                    value = jsonValueProcessor.processObjectValue(key, value, jsonConfig);
                                    bypass = true;
                                    if (!JsonVerifier.isValidJsonValue(value)) {
                                        throw new JSONException("Value is not a valid JSON value. " + value);
                                    }
                                }

                                if (propertyNameProcessor != null) {
                                    key = propertyNameProcessor.processPropertyName(beanClass, key);
                                }

                                setValue(jsonObject, key, value, type, jsonConfig, bypass);
                            }
                        }
                    }
                }
            } catch (Exception var16) {
                log.trace("Couldn't read public fields.", var16);
            }

            return jsonObject;
        } catch (JSONException var18) {
            removeInstance(bean);
            fireErrorEvent(var18, jsonConfig);
            throw var18;
        } catch (Exception var19) {
            removeInstance(bean);
            JSONException jsone = new JSONException(var19);
            fireErrorEvent(jsone, jsonConfig);
            throw jsone;
        }
    }

    private static JSONObject _fromDynaBean(DynaBean bean, JsonConfig jsonConfig) {
        if (bean == null) {
            fireObjectStartEvent(jsonConfig);
            fireObjectEndEvent(jsonConfig);
            return new JSONObject(true);
        } else if (!addInstance(bean)) {
            try {
                return jsonConfig.getCycleDetectionStrategy().handleRepeatedReferenceAsObject(bean);
            } catch (JSONException var13) {
                removeInstance(bean);
                fireErrorEvent(var13, jsonConfig);
                throw var13;
            } catch (RuntimeException var14) {
                removeInstance(bean);
                JSONException jsone = new JSONException(var14);
                fireErrorEvent(jsone, jsonConfig);
                throw jsone;
            }
        } else {
            fireObjectStartEvent(jsonConfig);
            JSONObject jsonObject = new JSONObject();

            try {
                DynaProperty[] props = bean.getDynaClass().getDynaProperties();
                Collection exclusions = jsonConfig.getMergedExcludes();
                PropertyFilter jsonPropertyFilter = jsonConfig.getJsonPropertyFilter();

                for(int i = 0; i < props.length; ++i) {
                    boolean bypass = false;
                    DynaProperty dynaProperty = props[i];
                    String key = dynaProperty.getName();
                    if (!exclusions.contains(key)) {
                        Class type = dynaProperty.getType();
                        Object value = bean.get(dynaProperty.getName());
                        if (jsonPropertyFilter == null || !jsonPropertyFilter.apply(bean, key, value)) {
                            JsonValueProcessor jsonValueProcessor = jsonConfig.findJsonValueProcessor(type, key);
                            if (jsonValueProcessor != null) {
                                value = jsonValueProcessor.processObjectValue(key, value, jsonConfig);
                                bypass = true;
                                if (!JsonVerifier.isValidJsonValue(value)) {
                                    throw new JSONException("Value is not a valid JSON value. " + value);
                                }
                            }

                            setValue(jsonObject, key, value, type, jsonConfig, bypass);
                        }
                    }
                }
            } catch (JSONException var15) {
                removeInstance(bean);
                fireErrorEvent(var15, jsonConfig);
                throw var15;
            } catch (RuntimeException var16) {
                removeInstance(bean);
                JSONException jsone = new JSONException(var16);
                fireErrorEvent(jsone, jsonConfig);
                throw jsone;
            }

            removeInstance(bean);
            fireObjectEndEvent(jsonConfig);
            return jsonObject;
        }
    }

    private static JSONObject _fromJSONObject(JSONObject object, JsonConfig jsonConfig) {
        if (object != null && !object.isNullObject()) {
            if (!addInstance(object)) {
                try {
                    return jsonConfig.getCycleDetectionStrategy().handleRepeatedReferenceAsObject(object);
                } catch (JSONException var10) {
                    removeInstance(object);
                    fireErrorEvent(var10, jsonConfig);
                    throw var10;
                } catch (RuntimeException var11) {
                    removeInstance(object);
                    JSONException jsone = new JSONException(var11);
                    fireErrorEvent(jsone, jsonConfig);
                    throw jsone;
                }
            } else {
                fireObjectStartEvent(jsonConfig);
                JSONArray sa = object.names(jsonConfig);
                Collection exclusions = jsonConfig.getMergedExcludes();
                JSONObject jsonObject = new JSONObject();
                PropertyFilter jsonPropertyFilter = jsonConfig.getJsonPropertyFilter();
                Iterator i = sa.iterator();

                while(true) {
                    String key;
                    Object value;
                    do {
                        do {
                            if (!i.hasNext()) {
                                removeInstance(object);
                                fireObjectEndEvent(jsonConfig);
                                return jsonObject;
                            }

                            Object k = i.next();
                            if (k == null) {
                                throw new JSONException("JSON keys cannot be null.");
                            }

                            if (!(k instanceof String) && !jsonConfig.isAllowNonStringKeys()) {
                                throw new ClassCastException("JSON keys must be strings.");
                            }

                            key = String.valueOf(k);
                            if ("null".equals(key)) {
                                throw new NullPointerException("JSON keys must not be null nor the 'null' string.");
                            }
                        } while(exclusions.contains(key));

                        value = object.opt(key);
                    } while(jsonPropertyFilter != null && jsonPropertyFilter.apply(object, key, value));

                    if (jsonObject.properties.containsKey(key)) {
                        jsonObject.accumulate(key, value, jsonConfig);
                        firePropertySetEvent(key, value, true, jsonConfig);
                    } else {
                        jsonObject.setInternal(key, value, jsonConfig);
                        firePropertySetEvent(key, value, false, jsonConfig);
                    }
                }
            }
        } else {
            fireObjectStartEvent(jsonConfig);
            fireObjectEndEvent(jsonConfig);
            return new JSONObject(true);
        }
    }

    private static JSONObject _fromJSONString(JSONString string, JsonConfig jsonConfig) {
        return _fromJSONTokener(new JSONTokener(string.toJSONString()), jsonConfig);
    }

    private static JSONObject _fromJSONTokener(JSONTokener tokener, JsonConfig jsonConfig) {
        try {
            if (tokener.matches("null.*")) {
                fireObjectStartEvent(jsonConfig);
                fireObjectEndEvent(jsonConfig);
                return new JSONObject(true);
            } else if (tokener.nextClean() != '{') {
                throw tokener.syntaxError("A JSONObject text must begin with '{'");
            } else {
                fireObjectStartEvent(jsonConfig);
                Collection exclusions = jsonConfig.getMergedExcludes();
                PropertyFilter jsonPropertyFilter = jsonConfig.getJsonPropertyFilter();
                JSONObject jsonObject = new JSONObject();

                while(true) {
                    while(true) {
                        char c = tokener.nextClean();
                        switch(c) {
                            case '\u0000':
                                throw tokener.syntaxError("A JSONObject text must end with '}'");
                            case '}':
                                fireObjectEndEvent(jsonConfig);
                                return jsonObject;
                            default:
                                tokener.back();
                                String key = tokener.nextValue(jsonConfig).toString();
                                c = tokener.nextClean();
                                if (c == '=') {
                                    if (tokener.next() != '>') {
                                        tokener.back();
                                    }
                                } else if (c != ':') {
                                    throw tokener.syntaxError("Expected a ':' after a key");
                                }

                                char peek = tokener.peek();
                                boolean quoted = peek == '"' || peek == '\'';
                                Object v = tokener.nextValue(jsonConfig);
                                if (!quoted && JSONUtils.isFunctionHeader(v)) {
                                    String params = JSONUtils.getFunctionParams((String)v);
                                    int i = 0;
                                    StringBuffer sb = new StringBuffer();

                                    do {
                                        char ch = tokener.next();
                                        if (ch == 0) {
                                            break;
                                        }

                                        if (ch == '{') {
                                            ++i;
                                        }

                                        if (ch == '}') {
                                            --i;
                                        }

                                        sb.append(ch);
                                    } while(i != 0);

                                    if (i != 0) {
                                        throw tokener.syntaxError("Unbalanced '{' or '}' on prop: " + v);
                                    }

                                    String text = sb.toString();
                                    text = text.substring(1, text.length() - 1).trim();
                                    Object value = new JSONFunction(params != null ? StringUtils.split(params, ",") : null, text);
                                    if (jsonPropertyFilter == null || !jsonPropertyFilter.apply(tokener, key, value)) {
                                        if (jsonObject.properties.containsKey(key)) {
                                            jsonObject.accumulate(key, value, jsonConfig);
                                            firePropertySetEvent(key, value, true, jsonConfig);
                                        } else {
                                            jsonObject.element(key, (Object)value, jsonConfig);
                                            firePropertySetEvent(key, value, false, jsonConfig);
                                        }
                                    }
                                } else {
                                    if (exclusions.contains(key)) {
                                        switch(tokener.nextClean()) {
                                            case ',':
                                            case ';':
                                                if (tokener.nextClean() == '}') {
                                                    fireObjectEndEvent(jsonConfig);
                                                    return jsonObject;
                                                }

                                                tokener.back();
                                                continue;
                                            case '}':
                                                fireObjectEndEvent(jsonConfig);
                                                return jsonObject;
                                            default:
                                                throw tokener.syntaxError("Expected a ',' or '}'");
                                        }
                                    }

                                    if (jsonPropertyFilter == null || !jsonPropertyFilter.apply(tokener, key, v)) {
                                        if (quoted && v instanceof String && (JSONUtils.mayBeJSON((String)v) || JSONUtils.isFunction(v))) {
                                            v = "\"" + v + "\"";
                                        }

                                        if (jsonObject.properties.containsKey(key)) {
                                            jsonObject.accumulate(key, v, jsonConfig);
                                            firePropertySetEvent(key, v, true, jsonConfig);
                                        } else {
                                            jsonObject.element(key, v, jsonConfig);
                                            firePropertySetEvent(key, v, false, jsonConfig);
                                        }
                                    }
                                }

                                switch(tokener.nextClean()) {
                                    case ',':
                                    case ';':
                                        if (tokener.nextClean() == '}') {
                                            fireObjectEndEvent(jsonConfig);
                                            return jsonObject;
                                        }

                                        tokener.back();
                                        break;
                                    case '}':
                                        fireObjectEndEvent(jsonConfig);
                                        return jsonObject;
                                    default:
                                        throw tokener.syntaxError("Expected a ',' or '}'");
                                }
                        }
                    }
                }
            }
        } catch (JSONException var15) {
            fireErrorEvent(var15, jsonConfig);
            throw var15;
        }
    }

    private static JSONObject _fromMap(Map map, JsonConfig jsonConfig) {
        if (map == null) {
            fireObjectStartEvent(jsonConfig);
            fireObjectEndEvent(jsonConfig);
            return new JSONObject(true);
        } else if (!addInstance(map)) {
            try {
                return jsonConfig.getCycleDetectionStrategy().handleRepeatedReferenceAsObject(map);
            } catch (JSONException var12) {
                removeInstance(map);
                fireErrorEvent(var12, jsonConfig);
                throw var12;
            } catch (RuntimeException var13) {
                removeInstance(map);
                JSONException jsone = new JSONException(var13);
                fireErrorEvent(jsone, jsonConfig);
                throw jsone;
            }
        } else {
            fireObjectStartEvent(jsonConfig);
            Collection exclusions = jsonConfig.getMergedExcludes();
            JSONObject jsonObject = new JSONObject();
            PropertyFilter jsonPropertyFilter = jsonConfig.getJsonPropertyFilter();

            try {
                Iterator entries = map.entrySet().iterator();

                label73:
                while(true) {
                    String key;
                    Object value;
                    boolean bypass;
                    do {
                        Entry entry;
                        do {
                            if (!entries.hasNext()) {
                                break label73;
                            }

                            bypass = false;
                            entry = (Entry)entries.next();
                            Object k = entry.getKey();
                            if (k == null) {
                                throw new JSONException("JSON keys cannot be null.");
                            }

                            if (!(k instanceof String) && !jsonConfig.isAllowNonStringKeys()) {
                                throw new ClassCastException("JSON keys must be strings.");
                            }

                            key = String.valueOf(k);
                            if ("null".equals(key)) {
                                throw new NullPointerException("JSON keys must not be null nor the 'null' string.");
                            }
                        } while(exclusions.contains(key));

                        value = entry.getValue();
                    } while(jsonPropertyFilter != null && jsonPropertyFilter.apply(map, key, value));

                    if (value != null) {
                        JsonValueProcessor jsonValueProcessor = jsonConfig.findJsonValueProcessor(value.getClass(), key);
                        if (jsonValueProcessor != null) {
                            value = jsonValueProcessor.processObjectValue(key, value, jsonConfig);
                            bypass = true;
                            if (!JsonVerifier.isValidJsonValue(value)) {
                                throw new JSONException("Value is not a valid JSON value. " + value);
                            }
                        }

                        setValue(jsonObject, key, value, value.getClass(), jsonConfig, bypass);
                    } else if (jsonObject.properties.containsKey(key)) {
                        jsonObject.accumulate(key, JSONNull.getInstance());
                        firePropertySetEvent(key, JSONNull.getInstance(), true, jsonConfig);
                    } else {
                        jsonObject.element(key, (Object)JSONNull.getInstance());
                        firePropertySetEvent(key, JSONNull.getInstance(), false, jsonConfig);
                    }
                }
            } catch (JSONException var14) {
                removeInstance(map);
                fireErrorEvent(var14, jsonConfig);
                throw var14;
            } catch (RuntimeException var15) {
                removeInstance(map);
                JSONException jsone = new JSONException(var15);
                fireErrorEvent(jsone, jsonConfig);
                throw jsone;
            }

            removeInstance(map);
            fireObjectEndEvent(jsonConfig);
            return jsonObject;
        }
    }

    private static JSONObject _fromString(String str, JsonConfig jsonConfig) {
        if (str != null && !"null".equals(str)) {
            return _fromJSONTokener(new JSONTokener(str), jsonConfig);
        } else {
            fireObjectStartEvent(jsonConfig);
            fireObjectEndEvent(jsonConfig);
            return new JSONObject(true);
        }
    }

    private static Object convertPropertyValueToArray(String key, Object value, Class targetType, JsonConfig jsonConfig, Map classMap) {
        Class innerType = JSONUtils.getInnerComponentType(targetType);
        Class targetInnerType = findTargetClass(key, classMap);
        if (innerType.equals(Object.class) && targetInnerType != null && !targetInnerType.equals(Object.class)) {
            innerType = targetInnerType;
        }

        JsonConfig jsc = jsonConfig.copy();
        jsc.setRootClass(innerType);
        jsc.setClassMap(classMap);
        Object array = JSONArray.toArray((JSONArray)value, jsc);
        if (!innerType.isPrimitive() && !JSONUtils.isNumber(innerType) && !Boolean.class.isAssignableFrom(innerType) && !JSONUtils.isString(innerType)) {
            if (!array.getClass().equals(targetType) && !targetType.equals(Object.class)) {
                Morpher morpher = JSONUtils.getMorpherRegistry().getMorpherFor(Array.newInstance(innerType, 0).getClass());
                if (IdentityObjectMorpher.getInstance().equals(morpher)) {
                    ObjectArrayMorpher beanMorpher = new ObjectArrayMorpher(new BeanMorpher(innerType, JSONUtils.getMorpherRegistry()));
                    JSONUtils.getMorpherRegistry().registerMorpher(beanMorpher);
                }

                array = JSONUtils.getMorpherRegistry().morph(Array.newInstance(innerType, 0).getClass(), array);
            }
        } else {
            array = JSONUtils.getMorpherRegistry().morph(Array.newInstance(innerType, 0).getClass(), array);
        }

        return array;
    }

    private static List convertPropertyValueToList(String key, Object value, JsonConfig jsonConfig, String name, Map classMap) {
        Class targetClass = findTargetClass(key, classMap);
        targetClass = targetClass == null ? findTargetClass(name, classMap) : targetClass;
        JsonConfig jsc = jsonConfig.copy();
        jsc.setRootClass(targetClass);
        jsc.setClassMap(classMap);
        List list = (List)JSONArray.toCollection((JSONArray)value, jsc);
        return list;
    }

    private static Collection convertPropertyValueToCollection(String key, Object value, JsonConfig jsonConfig, String name, Map classMap, Class collectionType) {
        Class targetClass = findTargetClass(key, classMap);
        targetClass = targetClass == null ? findTargetClass(name, classMap) : targetClass;
        JsonConfig jsc = jsonConfig.copy();
        jsc.setRootClass(targetClass);
        jsc.setClassMap(classMap);
        jsc.setCollectionType(collectionType);
        return JSONArray.toCollection((JSONArray)value, jsc);
    }

    private static Class resolveClass(Map classMap, String key, String name, Class type) {
        Class targetClass = findTargetClass(key, classMap);
        if (targetClass == null) {
            targetClass = findTargetClass(name, classMap);
        }

        if (targetClass == null && type != null) {
            if (List.class.equals(type)) {
                targetClass = ArrayList.class;
            } else if (Map.class.equals(type)) {
                targetClass = LinkedHashMap.class;
            } else if (Set.class.equals(type)) {
                targetClass = LinkedHashSet.class;
            } else if (!type.isInterface() && !Object.class.equals(type)) {
                targetClass = type;
            }
        }

        return targetClass;
    }

    private static Class findTargetClass(String key, Map classMap) {
        Class targetClass = (Class)classMap.get(key);
        if (targetClass == null) {
            Iterator i = classMap.entrySet().iterator();

            while(i.hasNext()) {
                Entry entry = (Entry)i.next();
                if (RegexpUtils.getMatcher((String)entry.getKey()).matches(key)) {
                    targetClass = (Class)entry.getValue();
                    break;
                }
            }
        }

        return targetClass;
    }

    private static boolean isTransientField(String name, Class beanClass, JsonConfig jsonConfig) {
        try {
            Field field = beanClass.getDeclaredField(name);
            return (field.getModifiers() & 128) == 128 ? true : isTransient(field, jsonConfig);
        } catch (Exception var4) {
            log.info("Error while inspecting field " + beanClass + "." + name + " for transient status.", var4);
            return false;
        }
    }

    private static boolean isTransient(AnnotatedElement element, JsonConfig jsonConfig) {
        Iterator annotations = jsonConfig.getIgnoreFieldAnnotations().iterator();

        while(annotations.hasNext()) {
            try {
                String annotationClassName = (String)annotations.next();
                Class classs = Class.forName(annotationClassName);
                if (element.getAnnotation(classs) != null) {
                    return true;
                }
            } catch (Exception var4) {
                log.info("Error while inspecting " + element + " for transient status.", var4);
            }
        }

        return false;
    }

    private static Object morphPropertyValue(String key, Object value, Class type, Class targetType) {
        Morpher morpher = JSONUtils.getMorpherRegistry().getMorpherFor(targetType);
        if (IdentityObjectMorpher.getInstance().equals(morpher)) {
            log.warn("Can't transform property '" + key + "' from " + type.getName() + " into " + targetType.getName() + ". Will register a default Morpher");
            if (Enum.class.isAssignableFrom(targetType)) {
                JSONUtils.getMorpherRegistry().registerMorpher(new EnumMorpher(targetType));
            } else {
                JSONUtils.getMorpherRegistry().registerMorpher(new BeanMorpher(targetType, JSONUtils.getMorpherRegistry()));
            }
        }

        value = JSONUtils.getMorpherRegistry().morph(targetType, value);
        return value;
    }

    private static void setProperty(Object bean, String key, Object value, JsonConfig jsonConfig) throws Exception {
        PropertySetStrategy propertySetStrategy = jsonConfig.getPropertySetStrategy() != null ? jsonConfig.getPropertySetStrategy() : PropertySetStrategy.DEFAULT;
        propertySetStrategy.setProperty(bean, key, value, jsonConfig);
    }

    private static void setValue(JSONObject jsonObject, String key, Object value, Class type, JsonConfig jsonConfig, boolean bypass) {
        boolean accumulated = false;
        if (value == null) {
            value = jsonConfig.findDefaultValueProcessor(type).getDefaultValue(type);
            if (!JsonVerifier.isValidJsonValue(value)) {
                throw new JSONException("Value is not a valid JSON value. " + value);
            }
        }

        if (jsonObject.properties.containsKey(key)) {
            if (String.class.isAssignableFrom(type)) {
                Object o = jsonObject.opt(key);
                if (o instanceof JSONArray) {
                    ((JSONArray)o).addString((String)value);
                } else {
                    jsonObject.properties.put(key, (new JSONArray()).element(o).addString((String)value));
                }
            } else {
                jsonObject.accumulate(key, value, jsonConfig);
            }

            accumulated = true;
        } else if (!bypass && !String.class.isAssignableFrom(type)) {
            jsonObject.setInternal(key, value, jsonConfig);
        } else {
            jsonObject.properties.put(key, value);
        }

        value = jsonObject.opt(key);
        if (accumulated) {
            JSONArray array = (JSONArray)value;
            value = array.get(array.size() - 1);
        }

        firePropertySetEvent(key, value, accumulated, jsonConfig);
    }

    public JSONObject() {
        this.properties = new ListOrderedMap();
    }

    public JSONObject(boolean isNull) {
        this();
        this.nullObject = isNull;
    }

    public JSONObject accumulate(String key, boolean value) {
        return this._accumulate(key, value ? Boolean.TRUE : Boolean.FALSE, new JsonConfig());
    }

    public JSONObject accumulate(String key, double value) {
        return this._accumulate(key, value, new JsonConfig());
    }

    public JSONObject accumulate(String key, int value) {
        return this._accumulate(key, value, new JsonConfig());
    }

    public JSONObject accumulate(String key, long value) {
        return this._accumulate(key, value, new JsonConfig());
    }

    public JSONObject accumulate(String key, Object value) {
        return this._accumulate(key, value, new JsonConfig());
    }

    public JSONObject accumulate(String key, Object value, JsonConfig jsonConfig) {
        return this._accumulate(key, value, jsonConfig);
    }

    public void accumulateAll(Map map) {
        this.accumulateAll(map, new JsonConfig());
    }

    public void accumulateAll(Map map, JsonConfig jsonConfig) {
        Iterator entries;
        Entry entry;
        String key;
        Object value;
        if (map instanceof JSONObject) {
            entries = map.entrySet().iterator();

            while(entries.hasNext()) {
                entry = (Entry)entries.next();
                key = (String)entry.getKey();
                value = entry.getValue();
                this.accumulate(key, value, jsonConfig);
            }
        } else {
            entries = map.entrySet().iterator();

            while(entries.hasNext()) {
                entry = (Entry)entries.next();
                key = String.valueOf(entry.getKey());
                value = entry.getValue();
                this.accumulate(key, value, jsonConfig);
            }
        }

    }

    public void clear() {
        this.properties.clear();
    }

    public int compareTo(Object obj) {
        if (obj != null && obj instanceof JSONObject) {
            JSONObject other = (JSONObject)obj;
            int size1 = this.size();
            int size2 = other.size();
            if (size1 < size2) {
                return -1;
            }

            if (size1 > size2) {
                return 1;
            }

            if (this.equals(other)) {
                return 0;
            }
        }

        return -1;
    }

    public boolean containsKey(Object key) {
        return this.properties.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.containsValue(value, new JsonConfig());
    }

    public boolean containsValue(Object value, JsonConfig jsonConfig) {
        try {
            value = this.processValue(value, jsonConfig);
        } catch (JSONException var4) {
            return false;
        }

        return this.properties.containsValue(value);
    }

    public JSONObject discard(String key) {
        this.verifyIsNull();
        this.properties.remove(key);
        return this;
    }

    public JSONObject element(String key, boolean value) {
        this.verifyIsNull();
        return this.element(key, (Object)(value ? Boolean.TRUE : Boolean.FALSE));
    }

    public JSONObject element(String key, Collection value) {
        return this.element(key, value, new JsonConfig());
    }

    public JSONObject element(String key, Collection value, JsonConfig jsonConfig) {
        if (!(value instanceof JSONArray)) {
            value = JSONArray.fromObject(value, jsonConfig);
        }

        return this.setInternal(key, value, jsonConfig);
    }

    public JSONObject element(String key, double value) {
        this.verifyIsNull();
        Double d = new Double(value);
        JSONUtils.testValidity(d);
        return this.element(key, (Object)d);
    }

    public JSONObject element(String key, int value) {
        this.verifyIsNull();
        return this.element(key, (Object)(new Integer(value)));
    }

    public JSONObject element(String key, long value) {
        this.verifyIsNull();
        return this.element(key, (Object)(new Long(value)));
    }

    public JSONObject element(String key, Map value) {
        return this.element(key, value, new JsonConfig());
    }

    public JSONObject element(String key, Map value, JsonConfig jsonConfig) {
        this.verifyIsNull();
        return value instanceof JSONObject ? this.setInternal(key, value, jsonConfig) : this.element(key, (Map)fromObject(value, jsonConfig), jsonConfig);
    }

    public JSONObject element(String key, Object value) {
        return this.element(key, value, new JsonConfig());
    }

    public JSONObject element(String key, Object value, JsonConfig jsonConfig) {
        this.verifyIsNull();
        if (key == null) {
            throw new JSONException("Null key.");
        } else {
            if (value != null) {
                value = this.processValue(key, value, jsonConfig);
                this._setInternal(key, value, jsonConfig);
            } else {
                this.remove(key);
            }

            return this;
        }
    }

    public JSONObject elementOpt(String key, Object value) {
        return this.elementOpt(key, value, new JsonConfig());
    }

    public JSONObject elementOpt(String key, Object value, JsonConfig jsonConfig) {
        this.verifyIsNull();
        if (key != null && value != null) {
            this.element(key, value, jsonConfig);
        }

        return this;
    }

    public Set entrySet() {
        return Collections.unmodifiableSet(this.properties.entrySet());
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof JSONObject)) {
            return false;
        } else {
            JSONObject other = (JSONObject)obj;
            if (this.isNullObject()) {
                return other.isNullObject();
            } else if (other.isNullObject()) {
                return false;
            } else if (other.size() != this.size()) {
                return false;
            } else {
                Iterator keys = this.properties.keySet().iterator();

                Object o2;
                label127:
                do {
                    while(keys.hasNext()) {
                        String key = (String)keys.next();
                        if (!other.properties.containsKey(key)) {
                            return false;
                        }

                        Object o1 = this.properties.get(key);
                        o2 = other.properties.get(key);
                        if (JSONNull.getInstance().equals(o1)) {
                            continue label127;
                        }

                        if (JSONNull.getInstance().equals(o2)) {
                            return false;
                        }

                        if (o1 instanceof String && o2 instanceof JSONFunction) {
                            if (!o1.equals(String.valueOf(o2))) {
                                return false;
                            }
                        } else if (o1 instanceof JSONFunction && o2 instanceof String) {
                            if (!o2.equals(String.valueOf(o1))) {
                                return false;
                            }
                        } else if (o1 instanceof JSONObject && o2 instanceof JSONObject) {
                            if (!o1.equals(o2)) {
                                return false;
                            }
                        } else if (o1 instanceof JSONArray && o2 instanceof JSONArray) {
                            if (!o1.equals(o2)) {
                                return false;
                            }
                        } else if (o1 instanceof JSONFunction && o2 instanceof JSONFunction) {
                            if (!o1.equals(o2)) {
                                return false;
                            }
                        } else if (o1 instanceof String) {
                            if (!o1.equals(String.valueOf(o2))) {
                                return false;
                            }
                        } else if (o2 instanceof String) {
                            if (!o2.equals(String.valueOf(o1))) {
                                return false;
                            }
                        } else {
                            Morpher m1 = JSONUtils.getMorpherRegistry().getMorpherFor(o1.getClass());
                            Morpher m2 = JSONUtils.getMorpherRegistry().getMorpherFor(o2.getClass());
                            if (m1 != null && m1 != IdentityObjectMorpher.getInstance()) {
                                if (!o1.equals(JSONUtils.getMorpherRegistry().morph(o1.getClass(), o2))) {
                                    return false;
                                }
                            } else if (m2 != null && m2 != IdentityObjectMorpher.getInstance()) {
                                if (!JSONUtils.getMorpherRegistry().morph(o1.getClass(), o1).equals(o2)) {
                                    return false;
                                }
                            } else if (!o1.equals(o2)) {
                                return false;
                            }
                        }
                    }

                    return true;
                } while(JSONNull.getInstance().equals(o2));

                return false;
            }
        }
    }

    public Object get(Object key) {
        return key instanceof String ? this.get((String)key) : null;
    }

    public Object get(String key) {
        this.verifyIsNull();
        return this.properties.get(key);
    }

    public boolean getBoolean(String key) {
        this.verifyIsNull();
        Object o = this.get(key);
        if (o != null) {
            if (o.equals(Boolean.FALSE) || o instanceof String && ((String)o).equalsIgnoreCase("false")) {
                return false;
            }

            if (o.equals(Boolean.TRUE) || o instanceof String && ((String)o).equalsIgnoreCase("true")) {
                return true;
            }
        }

        throw new JSONException("JSONObject[" + JSONUtils.quote(key) + "] is not a Boolean.");
    }

    public double getDouble(String key) {
        this.verifyIsNull();
        Object o = this.get(key);
        if (o != null) {
            try {
                return o instanceof Number ? ((Number)o).doubleValue() : Double.parseDouble((String)o);
            } catch (Exception var4) {
                throw new JSONException("JSONObject[" + JSONUtils.quote(key) + "] is not a number.");
            }
        } else {
            throw new JSONException("JSONObject[" + JSONUtils.quote(key) + "] is not a number.");
        }
    }

    public int getInt(String key) {
        this.verifyIsNull();
        Object o = this.get(key);
        if (o != null) {
            return o instanceof Number ? ((Number)o).intValue() : (int)this.getDouble(key);
        } else {
            throw new JSONException("JSONObject[" + JSONUtils.quote(key) + "] is not a number.");
        }
    }

    public JSONArray getJSONArray(String key) {
        this.verifyIsNull();
        Object o = this.get(key);
        if (o != null && o instanceof JSONArray) {
            return (JSONArray)o;
        } else {
            throw new JSONException("JSONObject[" + JSONUtils.quote(key) + "] is not a JSONArray.");
        }
    }

    public JSONObject getJSONObject(String key) {
        this.verifyIsNull();
        Object o = this.get(key);
        if (JSONNull.getInstance().equals(o)) {
            return new JSONObject(true);
        } else if (o instanceof JSONObject) {
            return (JSONObject)o;
        } else {
            throw new JSONException("JSONObject[" + JSONUtils.quote(key) + "] is not a JSONObject.");
        }
    }

    public long getLong(String key) {
        this.verifyIsNull();
        Object o = this.get(key);
        if (o != null) {
            return o instanceof Number ? ((Number)o).longValue() : (long)this.getDouble(key);
        } else {
            throw new JSONException("JSONObject[" + JSONUtils.quote(key) + "] is not a number.");
        }
    }

    public String getString(String key) {
        this.verifyIsNull();
        Object o = this.get(key);
        if (o != null) {
            return o.toString();
        } else {
            throw new JSONException("JSONObject[" + JSONUtils.quote(key) + "] not found.");
        }
    }

    public boolean has(String key) {
        this.verifyIsNull();
        return this.properties.containsKey(key);
    }

    public int hashCode() {
        int hashcode = 19;
        if (this.isNullObject()) {
            return hashcode + JSONNull.getInstance().hashCode();
        } else {
            Object key;
            Object value;
            for(Iterator entries = this.properties.entrySet().iterator(); entries.hasNext(); hashcode += key.hashCode() + JSONUtils.hashCode(value)) {
                Entry entry = (Entry)entries.next();
                key = entry.getKey();
                value = entry.getValue();
            }

            return hashcode;
        }
    }

    public boolean isArray() {
        return false;
    }

    public boolean isEmpty() {
        return this.properties.isEmpty();
    }

    public boolean isNullObject() {
        return this.nullObject;
    }

    public Iterator keys() {
        this.verifyIsNull();
        return this.keySet().iterator();
    }

    public Set keySet() {
        return Collections.unmodifiableSet(this.properties.keySet());
    }

    public JSONArray names() {
        this.verifyIsNull();
        JSONArray ja = new JSONArray();
        Iterator keys = this.keys();

        while(keys.hasNext()) {
            ja.element(keys.next());
        }

        return ja;
    }

    public JSONArray names(JsonConfig jsonConfig) {
        this.verifyIsNull();
        JSONArray ja = new JSONArray();
        Iterator keys = this.keys();

        while(keys.hasNext()) {
            ja.element(keys.next(), jsonConfig);
        }

        return ja;
    }

    public Object opt(String key) {
        this.verifyIsNull();
        return key == null ? null : this.properties.get(key);
    }

    public boolean optBoolean(String key) {
        this.verifyIsNull();
        return this.optBoolean(key, false);
    }

    public boolean optBoolean(String key, boolean defaultValue) {
        this.verifyIsNull();

        try {
            return this.getBoolean(key);
        } catch (Exception var4) {
            return defaultValue;
        }
    }

    public double optDouble(String key) {
        this.verifyIsNull();
        return this.optDouble(key, 0.0D / 0.0);
    }

    public double optDouble(String key, double defaultValue) {
        this.verifyIsNull();

        try {
            Object o = this.opt(key);
            return o instanceof Number ? ((Number)o).doubleValue() : new Double((String)o);
        } catch (Exception var5) {
            return defaultValue;
        }
    }

    public int optInt(String key) {
        this.verifyIsNull();
        return this.optInt(key, 0);
    }

    public int optInt(String key, int defaultValue) {
        this.verifyIsNull();

        try {
            return this.getInt(key);
        } catch (Exception var4) {
            return defaultValue;
        }
    }

    public JSONArray optJSONArray(String key) {
        this.verifyIsNull();
        Object o = this.opt(key);
        return o instanceof JSONArray ? (JSONArray)o : null;
    }

    public JSONObject optJSONObject(String key) {
        this.verifyIsNull();
        Object o = this.opt(key);
        return o instanceof JSONObject ? (JSONObject)o : null;
    }

    public long optLong(String key) {
        this.verifyIsNull();
        return this.optLong(key, 0L);
    }

    public long optLong(String key, long defaultValue) {
        this.verifyIsNull();

        try {
            return this.getLong(key);
        } catch (Exception var5) {
            return defaultValue;
        }
    }

    public String optString(String key) {
        this.verifyIsNull();
        return this.optString(key, "");
    }

    public String optString(String key, String defaultValue) {
        this.verifyIsNull();
        Object o = this.opt(key);
        return o != null ? o.toString() : defaultValue;
    }

    public Object put(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        } else {
            Object previous = this.properties.get(key);
            this.element(String.valueOf(key), value);
            return previous;
        }
    }

    public void putAll(Map map) {
        this.putAll(map, new JsonConfig());
    }

    public void putAll(Map map, JsonConfig jsonConfig) {
        Iterator entries;
        Entry entry;
        String key;
        Object value;
        if (map instanceof JSONObject) {
            entries = map.entrySet().iterator();

            while(entries.hasNext()) {
                entry = (Entry)entries.next();
                key = (String)entry.getKey();
                value = entry.getValue();
                this.properties.put(key, value);
            }
        } else {
            entries = map.entrySet().iterator();

            while(entries.hasNext()) {
                entry = (Entry)entries.next();
                key = String.valueOf(entry.getKey());
                value = entry.getValue();
                this.element(key, value, jsonConfig);
            }
        }

    }

    public Object remove(Object key) {
        return this.properties.remove(key);
    }

    public Object remove(String key) {
        this.verifyIsNull();
        return this.properties.remove(key);
    }

    public int size() {
        return this.properties.size();
    }

    public JSONArray toJSONArray(JSONArray names) {
        this.verifyIsNull();
        if (names != null && names.size() != 0) {
            JSONArray ja = new JSONArray();

            for(int i = 0; i < names.size(); ++i) {
                ja.element(this.opt(names.getString(i)));
            }

            return ja;
        } else {
            return null;
        }
    }

    public String toString() {
        if (this.isNullObject()) {
            return JSONNull.getInstance().toString();
        } else {
            try {
                Iterator keys = this.keys();
                StringBuffer sb = new StringBuffer("{");

                while(keys.hasNext()) {
                    if (sb.length() > 1) {
                        sb.append(',');
                    }

                    Object o = keys.next();
                    sb.append(JSONUtils.quote(o.toString()));
                    sb.append(':');
                    sb.append(JSONUtils.valueToString(this.properties.get(o)));
                }

                sb.append('}');
                return sb.toString();
            } catch (Exception var4) {
                return null;
            }
        }
    }

    public String toString(int indentFactor) {
        if (this.isNullObject()) {
            return JSONNull.getInstance().toString();
        } else {
            return indentFactor == 0 ? this.toString() : this.toString(indentFactor, 0);
        }
    }

    public String toString(int indentFactor, int indent) {
        if (this.isNullObject()) {
            return JSONNull.getInstance().toString();
        } else {
            int n = this.size();
            if (n == 0) {
                return "{}";
            } else if (indentFactor == 0) {
                return this.toString();
            } else {
                Iterator keys = this.keys();
                StringBuffer sb = new StringBuffer("{");
                int newindent = indent + indentFactor;
                Object o;
                if (n == 1) {
                    o = keys.next();
                    sb.append(JSONUtils.quote(o.toString()));
                    sb.append(": ");
                    sb.append(JSONUtils.valueToString(this.properties.get(o), indentFactor, indent));
                } else {
                    label55:
                    while(true) {
                        int i;
                        if (!keys.hasNext()) {
                            if (sb.length() > 1) {
                                sb.append('\n');

                                for(i = 0; i < indent; ++i) {
                                    sb.append(' ');
                                }
                            }

                            i = 0;

                            while(true) {
                                if (i >= indent) {
                                    break label55;
                                }

                                sb.insert(0, ' ');
                                ++i;
                            }
                        }

                        o = keys.next();
                        if (sb.length() > 1) {
                            sb.append(",\n");
                        } else {
                            sb.append('\n');
                        }

                        for(i = 0; i < newindent; ++i) {
                            sb.append(' ');
                        }

                        sb.append(JSONUtils.quote(o.toString()));
                        sb.append(": ");
                        sb.append(JSONUtils.valueToString(this.properties.get(o), indentFactor, newindent));
                    }
                }

                sb.append('}');
                return sb.toString();
            }
        }
    }

    public Collection values() {
        return Collections.unmodifiableCollection(this.properties.values());
    }

    public Writer write(Writer writer) {
        try {
            if (this.isNullObject()) {
                writer.write(JSONNull.getInstance().toString());
                return writer;
            } else {
                boolean b = false;
                Iterator keys = this.keys();
                writer.write(123);

                for(; keys.hasNext(); b = true) {
                    if (b) {
                        writer.write(44);
                    }

                    Object k = keys.next();
                    writer.write(JSONUtils.quote(k.toString()));
                    writer.write(58);
                    Object v = this.properties.get(k);
                    if (v instanceof JSONObject) {
                        ((JSONObject)v).write(writer);
                    } else if (v instanceof JSONArray) {
                        ((JSONArray)v).write(writer);
                    } else {
                        writer.write(JSONUtils.valueToString(v));
                    }
                }

                writer.write(125);
                return writer;
            }
        } catch (IOException var6) {
            throw new JSONException(var6);
        }
    }

    private JSONObject _accumulate(String key, Object value, JsonConfig jsonConfig) {
        if (this.isNullObject()) {
            throw new JSONException("Can't accumulate on null object");
        } else {
            if (!this.has(key)) {
                this.setInternal(key, value, jsonConfig);
            } else {
                Object o = this.opt(key);
                if (o instanceof JSONArray) {
                    ((JSONArray)o).element(value, jsonConfig);
                } else {
                    this.setInternal(key, (new JSONArray()).element(o).element(value, jsonConfig), jsonConfig);
                }
            }

            return this;
        }
    }

    protected Object _processValue(Object value, JsonConfig jsonConfig) {
        if (value instanceof JSONTokener) {
            return _fromJSONTokener((JSONTokener)value, jsonConfig);
        } else {
            return value != null && Enum.class.isAssignableFrom(value.getClass()) ? ((Enum)value).name() : super._processValue(value, jsonConfig);
        }
    }

    private JSONObject _setInternal(String key, Object value, JsonConfig jsonConfig) {
        this.verifyIsNull();
        if (key == null) {
            throw new JSONException("Null key.");
        } else {
            if (JSONUtils.isString(value) && JSONUtils.mayBeJSON(String.valueOf(value))) {
                this.properties.put(key, value);
            } else if (CycleDetectionStrategy.IGNORE_PROPERTY_OBJ != value && CycleDetectionStrategy.IGNORE_PROPERTY_ARR != value) {
                this.properties.put(key, value);
            }

            return this;
        }
    }

    private Object processValue(Object value, JsonConfig jsonConfig) {
        if (value != null) {
            JsonValueProcessor processor = jsonConfig.findJsonValueProcessor(value.getClass());
            if (processor != null) {
                value = processor.processObjectValue((String)null, value, jsonConfig);
                if (!JsonVerifier.isValidJsonValue(value)) {
                    throw new JSONException("Value is not a valid JSON value. " + value);
                }
            }
        }

        return this._processValue(value, jsonConfig);
    }

    private Object processValue(String key, Object value, JsonConfig jsonConfig) {
        if (value != null) {
            JsonValueProcessor processor = jsonConfig.findJsonValueProcessor(value.getClass(), key);
            if (processor != null) {
                value = processor.processObjectValue((String)null, value, jsonConfig);
                if (!JsonVerifier.isValidJsonValue(value)) {
                    throw new JSONException("Value is not a valid JSON value. " + value);
                }
            }
        }

        return this._processValue(value, jsonConfig);
    }

    private JSONObject setInternal(String key, Object value, JsonConfig jsonConfig) {
        return this._setInternal(key, this.processValue(key, value, jsonConfig), jsonConfig);
    }

    private void verifyIsNull() {
        if (this.isNullObject()) {
            throw new JSONException("null object");
        }
    }
}
