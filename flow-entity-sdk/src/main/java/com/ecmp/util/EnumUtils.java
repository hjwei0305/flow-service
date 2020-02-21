package com.ecmp.util;

import com.ecmp.annotation.Remark;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <strong>实现功能:</strong>.
 * <p>枚举工具类</p>
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.1 2017/4/27 13:41
 */
@SuppressWarnings("unchecked")
public class EnumUtils {
    private static Map<Class<?>, List<EnumEntity>> enumDatasContainer = new HashMap<Class<?>, List<EnumEntity>>();

    /**
     * 基于Enum类返回对应的key-value Map构建对象
     *
     * @param enumClass 枚举类
     * @return 返回枚举实体对象&lt;EnumEntity&gt;集合
     */
    public static List<EnumEntity> getEnumDataList(Class<? extends Enum> enumClass) {
        List<EnumEntity> enumEntities = null;
        if (enumClass != null) {
            enumEntities = enumDatasContainer.get(enumClass);
            if (enumEntities != null) {
                return enumEntities;
            }
            enumEntities = new ArrayList<EnumEntity>();
            Field[] fields = enumClass.getFields();
            for (Field field : fields) {
                String remark;
                Remark entityComment = field.getAnnotation(Remark.class);
                if (entityComment != null) {
                    remark = entityComment.value();
                } else {
                    remark = field.getName();
                }

                if (enumClass == field.getType()) {
                    String name = field.getName();
                    Enum anEnum = Enum.valueOf(enumClass, name);
                    enumEntities.add(new EnumEntity(anEnum.ordinal(), name, remark, anEnum));
                }
            }
            enumDatasContainer.put(enumClass, enumEntities);
        }
        return enumEntities;
    }

    /**
     * 根据枚举下标获取枚举实例
     *
     * @param <E>       枚举对象实例
     * @param enumClass 枚举类
     * @param ordinal   枚举下标
     * @return 返回枚举对象实例
     */
    public static <E extends Enum<E>> E getEnum(final Class<E> enumClass, final int ordinal) {
        if (ordinal < 0) {
            return null;
        }
        E anEnum = null;
        List<EnumEntity> enumDataList = getEnumDataList(enumClass);
        if (enumDataList != null && !enumDataList.isEmpty()) {
            for (EnumEntity entity : enumDataList) {
                if (ordinal == entity.getValue()) {
                    anEnum = (E) entity.getAnEnum();
                    break;
                }
            }
        }
        return anEnum;
    }

    /**
     * 根据枚举名获取枚举实例
     *
     * @param enumClass 枚举类
     * @param name      枚举名
     * @param <E>       枚举对象实例
     * @return 返回枚举对象实例
     */
    public static <E extends Enum<E>> E getEnum(final Class<E> enumClass, final String name) {
        if (name == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, name);
        } catch (final IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * 根据指定的枚举下标获取枚举名称
     *
     * @param enumClass 枚举类
     * @param ordinal   枚举下标
     * @return 返回枚举下标对应的名称
     */
    public static String getEnumItemName(Class<? extends Enum> enumClass, int ordinal) {
        String name = null;
        if (ordinal >= 0) {
            List<EnumEntity> enumDataList = getEnumDataList(enumClass);
            if (enumDataList != null && !enumDataList.isEmpty()) {
                for (EnumEntity entity : enumDataList) {
                    if (ordinal == entity.getValue()) {
                        name = entity.getName();
                        break;
                    }
                }
            }
        }
        return name;
    }

    /**
     * 根据指定的枚举下标获取枚举描述或名称
     * 若有@MetaData注解将获取@MetaData#value的枚举描述，没有则是枚举名
     *
     * @param enumClass 枚举类
     * @param ordinal   枚举下标
     * @return 返回枚举下标对应的描述或名称
     */
    public static String getEnumItemRemark(Class<? extends Enum> enumClass, int ordinal) {
        String remark = null;
        if (ordinal >= 0) {
            List<EnumEntity> enumDataList = getEnumDataList(enumClass);
            if (enumDataList != null && !enumDataList.isEmpty()) {
                for (EnumEntity entity : enumDataList) {
                    if (ordinal == entity.getValue()) {
                        remark = entity.getRemark();
                        break;
                    }
                }
            }
        }
        return remark;
    }

    /**
     * 根据指定的枚举下标获取枚举描述或名称
     * 若有@MetaData注解将获取@MetaData#value的枚举描述，没有则是枚举名
     *
     * @param enumClass 枚举类
     * @param anEnum    枚举实例
     * @return 返回枚举下标对应的描述或名称
     */
    public static String getEnumItemRemark(Class<? extends Enum> enumClass, Enum anEnum) {
        return getEnumItemRemark(enumClass, anEnum.ordinal());
    }

    static public class EnumEntity {
        private int value;
        private String name;
        private String remark;
        private Enum anEnum;

        public EnumEntity(int value, String name, String remark, Enum anEnum) {
            this.value = value;
            this.name = name;
            this.remark = remark;
            this.anEnum = anEnum;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public String getRemark() {
            return remark;
        }

        public Enum getAnEnum() {
            return anEnum;
        }
    }
}
