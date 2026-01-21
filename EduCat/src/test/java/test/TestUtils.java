package test;

import java.lang.reflect.Field;

public class TestUtils {
    // Metodo per iniettare mock in campi privati (es. 'utenzaDAO' dentro LoginServlet)
    public static void injectPrivateField(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        Field field = null;

        // Cerca il campo nella classe corrente o nelle superclassi
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        if (field == null) {
            throw new NoSuchFieldException("Campo '" + fieldName + "' non trovato nella classe " + target.getClass());
        }

        field.setAccessible(true);
        field.set(target, value);
    }
}

