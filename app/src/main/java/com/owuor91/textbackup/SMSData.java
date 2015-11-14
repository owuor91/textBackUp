package com.owuor91.textbackup;


public class SMSData {
    private String number;
    private String body;
    private Long date;
    private int type;

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

    public int getType(){
        return type;
    }

    public void setType(int type){
        this.type = type;
    }
}
