package pl.edu.pw.mini.shoppinglist;

public class ShoppingItem {

    private int id, number;
    private String name;

    public ShoppingItem(int id, String name, int number) {
        this.id = id;
        this.name = name;
        this.number = number;
    }

    @Override
    public String toString() {
        return name + " (" + number + " szt.)";
    }

    public int getId() {
        return id;
    }
}
