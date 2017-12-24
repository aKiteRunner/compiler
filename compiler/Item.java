package compiler;

public class Item {
    public String name;
    public Type type;
    public int value;
    public int level;
    public int address;
    public int size;

    public Item() {
        name = "";
    }

    @Override
    public String toString() {
        return String.format("name: %s, type: %s value: %d, level: %d, address: %d, size: %d",
                name, type, value, level, address, size);
    }
}
enum Type {
    None, Constant, Variable, Procedure
}
