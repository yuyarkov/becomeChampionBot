package pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;

public class Dancer {
    public static final String LEADER = "Партнер";
    public static final String FOLLOWER = "Партнерша";



    @JsonProperty("chatID")
    private long chatID;

    @JsonProperty("First Name")
    private String firstName;

    @JsonProperty("Last Name")
    private String lastName;

    @JsonProperty("Telegram NickName")
    private String telegramName;

    @JsonProperty("Sex")
    private String sex;

    @JsonIgnore
    private String idASH;//на будущее, код танцора в базе АСХ


    public Dancer() {
    }


    public Dancer(String firstName, String lastName, String sex, long chatID, String telegramName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.chatID = chatID;
        this.telegramName = telegramName;
    }

    public long getChatID() {
        return chatID;
    }

    public void setChatID(long chatID) {
        this.chatID = chatID;
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


    @Override
    public boolean equals(Object obj){
        if(obj == this)return true;
        Dancer dancer = (Dancer) obj; //я сделаю без сравнения типов
        return (dancer.chatID == this.chatID && dancer.getLastName().equals(this.lastName));
    }



    @Override
    public String toString() {
        return "Dancer [id:" + chatID + ", Имя " + firstName + ", Фамилия: " + lastName + ", Ник в телеграме: " + telegramName + ", Пол: " + sex + "]";
    }


}


