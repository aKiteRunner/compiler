import java.util.HashMap;

public class SymbolTable {
    public static final int TABLE_MAX = 1000;
    public static final int SYMBOL_MAX = 12;
    public static final int ADDRESS_MAX = 1000000;
    public static final int LEVEL_MAX = 3;
    public static final int INT_MAX = Integer.MAX_VALUE;

    private HashMap<String, Item> table;

    public SymbolTable() {
        table = new HashMap<>();
    }

    public Item get(String name) {
        return table.get(name);
    }

    private void add(Item item) throws ParseException {
        if (table.size() == TABLE_MAX) {
            throw new ParseException("Table too large");
        }
        table.put(item.name, item);
    }

    public void addConstant(Token token, int value) throws ParseException{
        Item item = new Item();
        item.name = token.name;
        item.value = value;
        add(item);
    }


    public void addVariable(Token token, int level, int address) throws ParseException{
        Item item = new Item();
        item.name = token.name;
        item.level = level;
        item.address = address;
        add(item);
    }

    public void addProcedure(Token token, int level) throws ParseException{
        Item item = new Item();
        item.name = token.name;
        item.level = level;
        table.put(item.name, item);
        add(item);
    }
}
