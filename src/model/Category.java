/**
 *علي حمال اسعيد 120220484  
 * محمد منذر الغزالي 120220852
 * تحسين وسام عودة 120220463
 */
package model;

/**
 *
 * @author Ali
 */
public class Category {
    private int id;
    private String name;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
     @Override
    public String toString() {
        return id + "," + name;
    }
}
