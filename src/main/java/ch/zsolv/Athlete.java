package ch.zsolv;

/**
 * A simple object representing one athlete
 */
public class Athlete {

    public String email;
    public String firstname;
    public int gender;
    public int id;
    public String lastname;
    public String nickname;
    public boolean skio;

    public Athlete(String email, String firstname, int gender, int id, String lastname, String nickname, boolean skio){
        this.email = email;
        this.firstname = firstname;
        this.gender = gender;
        this.id = id;
        this.lastname = lastname;
        this.nickname = nickname;
        this.skio = skio;
        if (skio) {
            System.out.println(firstname+" "+lastname+" is skio!");
        }
    }

    @Override
    public boolean equals(Object object){
        Athlete a2 = (Athlete) object;
        if (this == null || a2 == null){
            return false;
        }
        return this.id == a2.id;

    }
}
