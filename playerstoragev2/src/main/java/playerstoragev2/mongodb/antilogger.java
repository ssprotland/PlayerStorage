package playerstoragev2.mongodb;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.diagnostics.logging.Loggers;

import playerstoragev2.PlayerStorage;
import sun.misc.Unsafe;

public class antilogger {
    public static void disableMbdLogging() {
        PlayerStorage.debug("disabeling mongodb logging...");
        try {
            // force logger to be JULL
            Field loggerType = Loggers.class.getDeclaredField("USE_SLF4J");
            loggerType.setAccessible(true);
            // boolean bol = false;
            // setFinalStatic(loggerType, bol);
            PlayerStorage.debug(Boolean.toString(loggerType.getBoolean(null)));

            // recreate logger with new setting
            Class<?> c = Class.forName("com.mongodb.internal.connection.SingleServerCluster");
            Field logger = c.getDeclaredField("LOGGER");
            setFinalStatic(logger, Loggers.getLogger("hacked"));

            c = Class.forName("com.mongodb.internal.connection.AbstractMultiServerCluster");
            logger = c.getDeclaredField("LOGGER");
            setFinalStatic(logger, Loggers.getLogger("hacked"));

            c = Class.forName("com.mongodb.internal.connection.InternalStreamConnection");
            logger = c.getDeclaredField("LOGGER");
            setFinalStatic(logger, Loggers.getLogger("hacked"));

            c = Class.forName("com.mongodb.internal.connection.BaseCluster");
            logger = c.getDeclaredField("LOGGER");
            setFinalStatic(logger, Loggers.getLogger("hacked"));

            c = Class.forName("com.mongodb.internal.connection.DefaultServerMonitor");
            logger = c.getDeclaredField("LOGGER");
            setFinalStatic(logger, Loggers.getLogger("hacked"));

            // disabel logger in JULL
            Logger mongoLogger = Logger.getLogger("org.mongodb.driver.hacked");
            mongoLogger.setLevel(Level.WARNING);

        } catch (Exception e) {
            e.printStackTrace();
        }
        PlayerStorage.debug("sucsesfuly disabeled!");
    }

    static void setFinalStaticv1(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }

    static void setFinalStatic(Field field, Object newValue) throws Exception {
        final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        final Unsafe unsafe = (Unsafe) unsafeField.get(null);

        final Field ourField = field;
        final Object staticFieldBase = unsafe.staticFieldBase(ourField);
        final long staticFieldOffset = unsafe.staticFieldOffset(ourField);
        unsafe.putObject(staticFieldBase, staticFieldOffset, newValue);
    }
}
