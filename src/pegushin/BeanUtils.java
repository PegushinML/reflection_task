package pegushin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Maxim on 11/13/2016.
 */
public class BeanUtils {


    /**
     * Scans object "from" for all getters. If object "to"
     * contains correspondent setter, it will invoke it
     * to set property value for "to" which equals to the property
     * of "from".
     * <p/>
     * The type in setter should be compatible to the value returned
     * by getter (if not, no invocation performed).
     * Compatible means that parameter type in setter should
     * be the same or be superclass of the return type of the getter.
     * <p/>
     * The method takes care only about public methods.
     *
     * @param to   Object which properties will be set.
     * @param from Object which properties will be used to get values.
     */
    public static void assign(Object to, Object from) {
        Map<Method, Method> getterSetterMap = createGetterSetterMap(to, from);
        Set<Method> getterSet = getterSetterMap.keySet();

        for(Method getter : getterSet) {
            try {
                Object fromValue = getter.invoke(from);
                getterSetterMap.get(getter).invoke(to, fromValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<Method, Method> createGetterSetterMap(Object to, Object from) {
        Map<Method, Method> getterSetterMap =  new HashMap<>();
        Method[] getterMethods = to.getClass().getMethods();
        for(int i = 0; i < getterMethods.length; i++) {
            if(isGetter(getterMethods[i])) {
                Method setterMethod = findAppropriateMethod(getterMethods[i], from);
                if(setterMethod != null) {
                    getterSetterMap.put(getterMethods[i], setterMethod);
                }
            }
        }
        return getterSetterMap;
    }

    private static boolean isGetter(Method method){
        if(!method.getName().startsWith("get"))      return false;
        if(method.getParameterTypes().length != 0)   return false;
        if(void.class.equals(method.getReturnType())) return false;
        return true;
    }

    private static Method findAppropriateMethod(Method to, Object from) {
        String setterName = "s" + to.getName().substring(1);
        Method[] fromMethods = from.getClass().getMethods();
        for(int i = 0; i < fromMethods.length; i++) {
            if(fromMethods[i].getName().equals(setterName) && compareParameterTypes(to, fromMethods[i])) {
                return fromMethods[i];
            }
        }
        return null;
    }

    private static boolean compareParameterTypes(Method from, Method to) {
        Class<?>[] toParameters = to.getParameterTypes();
        Class<?> fromType = from.getReturnType();
        return  toParameters[0].isAssignableFrom(fromType);
    }
}

