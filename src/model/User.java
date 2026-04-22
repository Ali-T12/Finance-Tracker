
package model;

public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;

    public User() {
    }

    public User(int id, String firstName, String lastName, String email, String passwordhash) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordhash;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the passwordhash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * @param passwordhash the passwordhash to set
     */
    public void setPasswordHash(String passwordhash) {
        this.passwordHash = passwordhash;
    }

    @Override
    public String toString() {
        return id + "," + firstName + "," + lastName + "," + email + "," + passwordHash;
    }
    
    
 }
