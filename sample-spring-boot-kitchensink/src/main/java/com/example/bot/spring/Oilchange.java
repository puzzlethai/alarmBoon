package com.example.bot.spring;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;

@Document(collection = "oilchange")
public class Oilchange {

    @Id
    private BigInteger id;

    @Indexed(unique = true)
    private String oilchange;



    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getOilchange() {
        return oilchange;
    }

    public void setOilchange(String oilchange) {
        this.oilchange = oilchange;
    }


//getters and setters
}

