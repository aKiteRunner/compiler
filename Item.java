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
}
enum Type {
    None, Constant, Variable, Procedure
}
