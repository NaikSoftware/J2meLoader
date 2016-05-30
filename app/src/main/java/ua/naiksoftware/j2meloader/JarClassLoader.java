/**
 *
 * Не используется в программе!
 */
package ua.naiksoftware.j2meloader;

import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.io.IOException;
import java.io.InputStream;
import filelog.Log;

public class JarClassLoader extends ClassLoader {

    private final static String tag = "JarClassLoader";
    private final HashMap<String, Class<?>> cache = new HashMap<String, Class<?>>();
    private final String jarFileName;
    private final String packageName;
    private static final String WARNING = "Warning : No jar file found. Packet unmarshalling won't be possible. Please verify your classpath";

    public JarClassLoader(String jarFileName, String packageName) {
        Log.d(tag, ".......JarClassLoader.......");
        this.jarFileName = jarFileName;
        this.packageName = packageName;
        cacheClasses();
    }

    /**
     * При создании загрузчика извлекаем все классы из jar и кэшируем в памяти
     *
     */
    private void cacheClasses() {
        try {
            String className;
            JarFile jarFile = new JarFile(jarFileName);
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                Log.d(tag, "Elem find: " + entries.hasMoreElements());
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                Log.d(tag, "entry name = " + jarEntry.getName());
                // Одно из назначений хорошего загрузчика - валидация классов на этапе загрузки
                if (match(normalize(jarEntry.getName()), packageName)) {
                    Log.d(tag, "jar entry is valid");
                    byte[] classData = loadClassData(jarFile, jarEntry);
                    if (classData != null) {
                        className = stripClassName(normalize(jarEntry.getName()));
                        Log.d(tag, "define class = " + className);
                        Class<?> clazz = defineClass(className, classData, 0, classData.length);
                        cache.put(clazz.getName(), clazz);
                        Log.d(tag, "== class " + clazz.getName() + " loaded in cache");
                    } else {
                        Log.d(tag, "class data = null");
                    }
                } else {
                    Log.d(tag, "jar entry not a valid class");
                }
            }
        } catch (IOException ioe) {
            // Просто выведем сообщение об ошибке
            Log.d(tag, WARNING);
        }
    }

    /**
     * Собственно метод, который и реализует загрузку класса
     *
     * @param name - имя класса
     * @return - загруженный класс
     * @throws java.lang.ClassNotFoundException
     */
    @Override
    public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> result = cache.get(name);
        // Возможно класс вызывается не по полному имени - добавим имя пакета
        if (result == null) {
            result = cache.get(packageName + "." + name);
        }
        // Если класса нет в кэше то возможно он системный
        if (result == null) {
            result = super.findSystemClass(name);
        }
        Log.d(tag, "== loadClass(" + name + ")");
        return result;
    }

    /**
     * Получаем каноническое имя класса
     *
     * @param className
     * @return
     */
    private String stripClassName(String className) {
        return className.substring(0, className.length() - 6);
    }

    /**
     * Преобразуем имя в файловой системе в имя класса (заменяем слэши на точки)
     *
     * @param className
     * @return
     */
    private String normalize(String className) {
        return className.replace('/', '.');
    }

    /**
     * Валидация класса - проверят принадлежит ли класс заданному пакету и имеет
     * ли он расширение .class
     *
     * @param className
     * @param packageName
     * @return
     */
    private boolean match(String className, String packageName) {
        return className.startsWith(packageName) && className.endsWith(".class");
    }

    /**
     * Извлекаем файл из заданного JarEntry
     *
     * @param jarFile - файл jar-архива из которого извлекаем нужный файл
     * @param jarEntry - jar-сущность которую извлекаем
     * @return null если невозможно прочесть файл
     */
    private byte[] loadClassData(JarFile jarFile, JarEntry jarEntry) throws IOException {
        long size = jarEntry.getSize();
        if (size == -1 || size == 0) {
            return null;
        }
        byte[] data = new byte[(int) size];
        InputStream in = jarFile.getInputStream(jarEntry);
        in.read(data);
        return data;
    }
}
