package compiler;

import java.util.*;

public class SymbolTable {
    public static final int TABLE_MAX = 1000;
    public static final int SYMBOL_MAX = 12;
    public static final int ADDRESS_MAX = 1000000;
    public static final int LEVEL_MAX = 3;
    public static final int INT_MAX = Integer.MAX_VALUE;
    public int tablePtr;
    private Item[] table;


    public SymbolTable() {
        table = new Item[TABLE_MAX];
        tablePtr = 0;
    }

    public Item get(int index) {
        if (table[index] == null) {
            table[index] = new Item();
        }
        return table[index];
    }

    public int index(String id) {
        for (int i = tablePtr; i > 0; i--) {
            if (get(i).name.equals(id)) {
                return i;
            }
        }
        return 0;
    }

    private void add(Item item) throws ParseException {
        if (tablePtr == TABLE_MAX) {
            throw new ParseException("Table too large");
        }
        tablePtr++;
        table[tablePtr] = item;
    }

    public void addConstant(Token token, int value) throws ParseException{
        Item item = new Item();
        item.name = token.name;
        item.value = value;
        item.type = Type.Constant;
        add(item);
    }


    public void addVariable(Token token, int level, int address) throws ParseException{
        Item item = new Item();
        item.name = token.name;
        item.level = level;
        item.address = address;
        item.type = Type.Variable;
        add(item);
    }

    public void addProcedure(Token token, int level) throws ParseException{
        Item item = new Item();
        item.name = token.name;
        item.level = level;
        item.type = Type.Procedure;
        add(item);
    }

    public void printTable() {
        for (int i = 0; i < tablePtr; i++) {
            System.out.println(table[i]);
        }
        System.out.println();
    }
}
