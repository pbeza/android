package pl.edu.pw.mini.shoppinglist;

public class ShoppingItem {

    private static String itemsPostfix;
    private final int id, number;
    private final String name;

    public ShoppingItem(int id, String name, int number, String itemsPostfix) {
        this.id = id;
        this.name = name;
        this.number = number;
        ShoppingItem.itemsPostfix = itemsPostfix;
    }

    @Override
    public String toString() {
        return name + " (" + number + " " + itemsPostfix + ")";
    }

    public int getId() {
        return id;
    }
}
