package Jvakt;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package projektarbetegr11;

/**
 *
 * @author Annika
 */
public class consoleROW {

    public String userName;
    private String forName;
    private String lastName;
    private String height;
    private String weight;
    private String DOB;
    public int row;
    public int col = 0;
    private String kommentar;

    public consoleROW() {
        this.userName = " ";
        this.forName = " ";
        this.lastName = " ";
        this.height = " ";
        this.weight = " ";
        this.DOB = " ";
        this.kommentar = " ";
    }

    public String getuserName() {
        return userName;
    }

    public String getforName() {
        return forName;
    }

    public String getlastName() {
        return lastName;
    }

    public String getheight() {
        return height;
    }

    public String getweight() {
        return weight;
    }

    public String getDOB() {
        return DOB;
    }

    public String getkommentar() {
        return kommentar;
    }

    public void setuserName(String userName) {
        this.userName = userName;
    }

    public void setforName(String forName) {
        this.forName = forName;
    }

    public void setlastName(String lastName) {
        this.lastName = lastName;
    }

    public void setheight(String height) {
        this.height = height;
    }

    public void setweight(String weight) {
        this.weight = weight;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public void setkommentar(String kommentar) {
        this.kommentar = kommentar;
    }
}
