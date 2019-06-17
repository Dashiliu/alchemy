package com.dfire.platform.alchemy.api.util;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.table.shaded.org.apache.commons.lang3.time.FastDateFormat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author congbai
 * @date 2019/5/23
 */
public class ConvertObjectUtil {


    public static Object transform(Object result, TypeInformation<Object> typeAt) {
        if (result == null) {
            return null;
        }
        Class clazz = typeAt.getTypeClass();
        if (clazz == Integer.class){
            return getIntegerVal(result);
        }else if(clazz == Boolean.class){
            return getBoolean(result);
        }else if(clazz == Long.class){
            return getLongVal(result);
        }else if(clazz == Byte.class){
            return getByte(result);
        }else if(clazz == Short.class){
            return getShort(result);
        }else if(clazz == String.class){
            return getString(result);
        }else if(clazz == Float.class){
            return getFloatVal(result);
        }else if(clazz == Double.class){
            return getDoubleVal(result);
        }else if(clazz == BigDecimal.class){
            return getBigDecimal(result);
        }else if(clazz == Date.class){
            return getDate(result);
        }else if(clazz == Timestamp.class){
            return getTimestamp(result);
        }
        return result;
    }
    private static Long getLongVal(Object obj) {
        if (obj instanceof String || obj instanceof Integer) {
            return Long.valueOf(obj.toString());
        } else if (obj instanceof Long || obj instanceof Double) {
            return (Long) obj;
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).longValue();
        }
        throw new RuntimeException("not support type of " + obj.getClass() + " convert to Long.");
    }

    private static Integer getIntegerVal(Object obj) {
        if (obj instanceof String) {
            return Integer.valueOf(obj.toString());
        } else if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof Long) {
            return ((Long) obj).intValue();
        } else if (obj instanceof Double) {
            return ((Double) obj).intValue();
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).intValue();
        } else if (obj instanceof BigInteger) {
            return ((BigInteger) obj).intValue();
        }

        throw new RuntimeException("not support type of " + obj.getClass() + " convert to Integer.");
    }

    private static Float getFloatVal(Object obj) {
        if (obj instanceof String) {
            return Float.valueOf(obj.toString());
        } else if (obj instanceof Float) {
            return (Float) obj;
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).floatValue();
        }
        throw new RuntimeException("not support type of " + obj.getClass() + " convert to Float.");
    }


    private static Double getDoubleVal(Object obj) {
        if (obj instanceof String) {
            return Double.parseDouble(obj.toString());
        } else if (obj instanceof Float) {
            return Double.parseDouble(obj.toString());
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).doubleValue();
        }
        throw new RuntimeException("not support type of " + obj.getClass() + " convert to Double.");
    }


    private static Boolean getBoolean(Object obj) {
        if (obj instanceof String) {
            return Boolean.valueOf(obj.toString());
        } else if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        throw new RuntimeException("not support type of " + obj.getClass() + " convert to Boolean.");
    }

    private static String getString(Object obj) {
        return obj.toString();
    }

    private static Byte getByte(Object obj) {
        if (obj instanceof String) {
            return Byte.valueOf(obj.toString());
        } else if (obj instanceof Byte) {
            return (Byte) obj;
        }
        throw new RuntimeException("not support type of " + obj.getClass() + " convert to Byte.");
    }

    private static Short getShort(Object obj) {
        if (obj instanceof String) {
            return Short.valueOf(obj.toString());
        } else if (obj instanceof Short) {
            return (Short) obj;
        }
        throw new RuntimeException("not support type of " + obj.getClass() + " convert to Short.");
    }

    private static BigDecimal getBigDecimal(Object obj) {
        if (obj instanceof String) {
            return new BigDecimal(obj.toString());
        } else if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        } else if (obj instanceof BigInteger) {
            return new BigDecimal((BigInteger) obj);
        } else if (obj instanceof Number) {
            return new BigDecimal(((Number) obj).doubleValue());
        }
        throw new RuntimeException("not support type of " + obj.getClass() + " convert to BigDecimal.");
    }

    private static Date getDate(Object obj) {
        if (obj instanceof String) {
            FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd");
            try {
                return new Date(format.parse(obj.toString()).getTime());
            } catch (ParseException e) {
                throw new RuntimeException("String convert to Date fail.");
            }
        } else if (obj instanceof Timestamp) {
            return new Date(((Timestamp) obj).getTime());
        } else if (obj instanceof Date) {
            return (Date) obj;
        }
        throw new RuntimeException("not support type of " + obj.getClass() + " convert to Date.");
    }

    private static Timestamp getTimestamp(Object obj) {
        if (obj instanceof Timestamp) {
            return (Timestamp) obj;
        } else if (obj instanceof Date) {
            return new Timestamp(((Date) obj).getTime());
        } else if (obj instanceof String) {
            return new Timestamp(getDate(obj).getTime());
        }
        throw new RuntimeException("not support type of " + obj.getClass() + " convert to Date.");
    }
}
