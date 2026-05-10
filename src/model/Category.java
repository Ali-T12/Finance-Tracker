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
    private int userId;
    private String name;

    public Category(int id, int userId, String name) {
        this.id = id;
        this.userId = userId;
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }
     public int getUserId() {
        return userId;
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
        return id + "," + userId + "," + name;
    }
}
