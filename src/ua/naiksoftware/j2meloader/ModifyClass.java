package ua.naiksoftware.j2meloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author kiriman
 */
public class ModifyClass {

    private static final int MAGIC = 0xCAFEBABE; // О_о
    private static final int CLASS = 7;
    private static final int FIELDREF = 9;
    private static final int METHODREF = 10;
    private static final int INTERFACEMETHODREF = 11;
    private static final int STRING = 8;
    private static final int INTEGER = 3;
    private static final int FLOAT = 4;
    private static final int LONG = 5;
    private static final int DOUBLE = 6;
    private static final int NAMEANDTYPE = 12;
    private static final int UTF8 = 1;
    private static int class_version, // Версия класса
            count_constants_cp, // Количество констант
            max_stack; // Максимальный размер стека
    private static byte[] tag_cp, // Тэги записей в пуле
            code; // Основной код
    private static Vector cpool, // Сами записи
            indexC;

    private static int getIndex(int p, int l) {
        int a1, a2;
        a1 = (p >> l) & 0xff;
        a2 = (p >> (l - 8)) & 0xff;
        return ((a1 << 8) | (a2)) - 1;
    }

    public static byte[] modifyClass(FileInputStream fis, String name_class) {
        try {
            DataInputStream dis = new DataInputStream(fis);
            dis.readInt(); // MAGIC
            class_version = dis.readInt();
            count_constants_cp = dis.readUnsignedShort();
            tag_cp = new byte[count_constants_cp];
            cpool = new Vector();
            Vector mref = new Vector(); // Для METHODREF записей
            Vector temp_i = new Vector();
            int skip = 10; // Кол-во прочтенных байт(int + int + short)
            boolean z = false;

            for (int i = 0; i < count_constants_cp; i++) {
                tag_cp[i] = dis.readByte();
                skip++;
                switch (tag_cp[i]) {
                    case METHODREF:
                        int lol = dis.readInt();
                        temp_i.addElement(new Integer(i + 1));
                        cpool.addElement(new Integer(lol));
                        mref.addElement(new Integer(lol));
                        skip += 4;
                        break;
                    case UTF8:
                        String s = dis.readUTF();
                        skip += s.length() + 2;
                        cpool.addElement(s);
                        if (s.contains("getResourceAsStream")) {
                            z = true; // Шоу продолжается!
                        }
                        break;
                    case NAMEANDTYPE:
                    case INTERFACEMETHODREF:
                    case FIELDREF:
                    case INTEGER:
                    case FLOAT:
                        cpool.addElement(new Integer(dis.readInt()));
                        skip += 4;
                        break;
                    case DOUBLE:
                    case LONG:
                        cpool.addElement(new Long(dis.readLong()));
                        skip += 8;
                        break;
                    case CLASS:
                    case STRING:
                        cpool.addElement(new Short(dis.readShort()));
                        skip += 2;
                        break;
                }
            }

            if (!z) { // Класс годится для эмуляции
                return null;
            }

            /*
             * Разбор METHODREF записей на составляющие(индекс класса и имя метода с типом возвращаемых значений)
             * проверяем, есть ли нужный нам метод - InputStream getResourceAsStream(String)
             * и изменяем класс, ведь его индекс у нас найдется
             */
            indexC = new Vector();
            int i = 0;
            for (Object e : mref) {
                int m = ((Integer) e).intValue();
                int index_class = getClassIndex(getIndex(m, 24));
                if (getNameAndType(getIndex(m, 8)).equals("name=getResourceAsStream, type=(Ljava/lang/String;)Ljava/io/InputStream;")) {
                    indexC.addElement(temp_i.elementAt(i));
                    cpool.setElementAt(name_class, index_class);
                }
                i++;
            }
            code = new byte[(int) fis.getChannel().size() - skip];
            dis.read(code);
            dis.close();
            fis.close();

            rebuildClassFile();

            return code;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static int getClassIndex(int index) {
        if (tag_cp[index] == CLASS || tag_cp[index] == STRING) {
            Object element = cpool.elementAt(index);
            return ((Short) element).shortValue() - 1;
        } else {
            return index;
        }
    }

    private static String getNameAndType(int index) {
        int nat = ((Integer) (cpool.elementAt(index))).intValue();
        String name = (String) cpool.elementAt(getIndex(nat, 24));
        String type = (String) cpool.elementAt(getIndex(nat, 8));
        return "name=" + name + ", type=" + type;
    }

    private static void rebuildClassFile() {
        try {
            // Собираем класс заново!
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            // Constant Pool
            dos.writeInt(MAGIC);
            dos.writeInt(class_version);
            dos.writeShort(count_constants_cp);
            for (int i = 0; i < count_constants_cp; i++) {
                dos.writeByte(tag_cp[i]);
                switch (tag_cp[i]) {
                    case UTF8:
                        dos.writeUTF((String) cpool.elementAt(i));
                        break;
                    case NAMEANDTYPE:
                    case INTERFACEMETHODREF:
                    case METHODREF:
                    case FIELDREF:
                    case INTEGER:
                    case FLOAT:
                        dos.writeInt(((Integer) (cpool.elementAt(i))).intValue());
                        break;
                    case DOUBLE:
                    case LONG:
                        dos.writeLong(((Long) (cpool.elementAt(i))).longValue());
                        break;
                    case CLASS:
                    case STRING:
                        dos.writeShort(((Short) (cpool.elementAt(i))).shortValue());
                        break;
                }
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(code);
            DataInputStream dis = new DataInputStream(bais);
            dos.write(dis.read()); // Флаги доступа класса dos.writeShort(dis.readUnsignedShort());
            dos.writeShort(dis.readUnsignedShort()); // this и super
            dos.writeShort(dis.readUnsignedShort());
            // Интерфейсы
            int count_interfaces = dis.readUnsignedShort(); // Кол-во задействованных интерфейсов
            System.out.println("count_interfaces = " + count_interfaces);
            dos.writeShort(count_interfaces);
            for (int i = 0; i < count_interfaces; i++) {
                dos.writeShort(dis.readUnsignedShort()); // Запись интерфейса(ссылка на запись в пуле)
            }
            // Переменные
            int count_vars = dis.readUnsignedShort(); // Кол-во переменных
            System.out.println("count_vars = " + count_vars);
            dos.writeShort(count_vars);
            for (int i = 0; i < count_vars; i++) {
                dos.writeShort(dis.readUnsignedShort()); // Флаг доступа
                dos.writeShort(dis.readUnsignedShort()); // Ссылка на запись в пуле(имя переменной)
                dos.writeShort(dis.readUnsignedShort()); // Дескриптор
                readAttr(dis, dos);
            }
            // Методы
            int count_methods;
            dos.writeShort(count_methods = dis.readUnsignedShort()); // Кол-во методов
            for (int i = 0; i < count_methods; i++) {
                dos.writeShort(dis.readShort()); // Флаг доступа
                int name_index = dis.readShort();
                System.out.println("Loading method: " + cpool.elementAt(name_index - 1));
                dos.writeShort(name_index);
                dos.writeShort(dis.readShort()); // Дескриптор
                int count_attr = dis.readShort();
                dos.writeShort(count_attr);
                /*
                 * Тут нас интересует атрибут "Code" и размер стека
                 * Здесь же ищем все опкоды 0xB6 и исправляем
                 */
                for (int j = 0; j < count_attr; j++) {
                    int attr_name_index = dis.readUnsignedShort();
                    dos.writeShort(attr_name_index);
                    int len = dis.readInt();
                    //System.out.println("len = " + len);
                    dos.writeInt(len);
                    String name = (String) cpool.elementAt(attr_name_index - 1);
                    System.out.println(name);
                    if (name.equals("Code")) {
                        max_stack = dis.readUnsignedShort();
                        int max_locals = dis.readUnsignedShort();
                        int code_length = dis.readInt();
                        byte[] data = new byte[code_length];
                        dis.read(data);
                        byte[] b = editOpcode(data);
                        dos.writeShort(max_stack);
                        dos.writeShort(max_locals);
                        dos.writeInt(code_length);
                        dos.write(b);
                        int exceptions_table_length = dis.readUnsignedShort();
                        dos.writeShort(exceptions_table_length);
                        for (int k = 0; k < exceptions_table_length; k++) {
                            int start_pc = dis.readUnsignedShort();
                            dos.writeShort(start_pc);
                            int end_pc = dis.readUnsignedShort();
                            dos.writeShort(end_pc);
                            int handler_pc = dis.readUnsignedShort();
                            dos.writeShort(handler_pc);
                            int catch_type = dis.readUnsignedShort();
                            dos.writeShort(catch_type);
                        }
                        readAttr(dis, dos);
                    } else {
                        byte[] data = new byte[len];
                        dis.read(data);
                        dos.write(data);
                    }
                }
            }
            readAttr(dis, dos);
            code = baos.toByteArray();
            dis.close();
            bais.close();
            dos.close();
            baos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void readAttr(DataInputStream dis, DataOutputStream dos) {
        try {
            int count_attr = dis.readUnsignedShort(); // Кол-во атрибутов
            dos.writeShort(count_attr);
            for (int j = 0; j < count_attr; j++) {
                dos.writeShort(dis.readUnsignedShort()); // Ссылка на запись в пуле(имя атрибута)
                int len = dis.readInt(); // Длина атрибута
                dos.writeInt(len);
                byte[] data = new byte[len];
                dis.read(data);
                dos.write(data);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
     *
     * Изменяем опкоды B6 - invokevirtuals на B8 - invokestatic в основном коде!
     * Чтоб получить из этого:
     * getClass().getResourceAs..;
     * вот это:
     * getClass();
     * NewClass.getResourceAs..;
     */
    private static byte[] editOpcode(byte[] code) {
        byte[] data = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(code);
        DataInputStream dis = new DataInputStream(bais);
        ByteArrayOutputStream bois = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bois);
        try {
            int b = 0;
            boolean z = false;
            while ((b = dis.read()) != -1) {
                z = false;
                if (b == 0xb6) {
                    int index = dis.readShort();
                    for (Object e : indexC) {
                        if (index == ((Integer) e).intValue()) {
                            max_stack++;
                            dos.write(0xb8);
                            dos.writeShort(index);
                            z = true;
                            break;
                        }
                    }
                    if (!z) {
                        dos.write(b);
                        dos.writeShort(index);
                    }
                } else {
                    dos.write(b);
                }
            }
            dis.close();
            bais.close();
            data = bois.toByteArray();
            dos.close();
            bois.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return data;
    }
}