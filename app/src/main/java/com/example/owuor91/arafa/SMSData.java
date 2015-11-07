package com.example.owuor91.arafa;


import java.sql.Timestamp;

public class SMSData {
    private String number;
    private String body;
    private Long date;

    public  String getNumber(){
        return number;
    }

    public void setNumber(String number){
        this.number=number;
    }

    public String getBody(){
        return body;
    }

    public void setBody(String body){
        this.body = body;
    }

    public Long getDate(){
        return date;
    }

    public void  setDate(Long date){
        this.date = date;
    }
}
