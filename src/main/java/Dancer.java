public class Dancer {
    private String idDancerFromChatID;
    private String idASH;//на будущее, код танцора в базе АСХ
    private String sex;
    public static final String LEADER="Партнер";
    public static final String FOLLOWER="Партнерша";
    private String firstName;
    private String lastName;
    private String telegramName;

public Dancer(String firstName,String lastName, String sex, String chatID) {
    this.firstName=firstName;
    this.lastName=lastName;
    this.sex=sex;
    this.idDancerFromChatID=chatID;
}

    public String getIdDancerFromChatID() {
        return idDancerFromChatID;
    }

    public void setIdDancerFromChatID(String idDancerFromChatID) {
        this.idDancerFromChatID = idDancerFromChatID;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTelegramName() {
        return telegramName;
    }

    public void setTelegramName(String telegramName) {
        this.telegramName = telegramName;
    }





}
