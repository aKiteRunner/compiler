package compiler;

import java.util.*;

public class SymbolTable {
    public static final int TABLE_MAX = 1000;
    public static final int SYMBOL_MAX = 12;
    public static final int ADDRESS_MAX = 1000000;
    public static final int LEVEL_MAX = 3;
    public static final int INT_MAX = Integer.MAX_VALUE;

    private LinkedHashMap<String, Item> table;

    public SymbolTable() {
        table = new LinkedHashMap<>();
    }

    public Item get(String name) {
        ListIterator<Map.Entry<String, Item>> i = new ArrayList<>(table.entrySet()).listIterator(table.size());
        while (i.hasPrevious()) {
            Map.Entry<String, Item> entry = i.previous();
            if (entry.getKey().equals(name)) {
                return entry.getValue();
            }
        }
        return null;
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

    public void printTable() {
        for (Map.Entry<String, Item> entry : table.entrySet()) {
            System.out.println(entry.getValue());
        }
    }
}
