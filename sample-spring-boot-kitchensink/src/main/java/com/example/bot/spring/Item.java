package com.example.bot.spring;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
// import javax.xml.bind.annotation.XmlRootElement;

// Controls whether fields or Javabean properties are serialized by default.
// By default all public members will be searialized,
// XmlAccessType.FIELD Serializes using FIELDS

// Use @XmlRootElement to customize the XML root tag, by default it defaults
// to class name 'Employee'

@XmlAccessorType(XmlAccessType.FIELD)
public class Item {
    // Below annotation is to customize the oilType XML tag
    @XmlElement(name = "type")
    private String oilType;

    private String today;

    private String tomorrow;

    // private String designation;
    private String image;
    private String image2;
    private String unit_th;
    private String unit_en;
    private String change;

    public Item() {
    }

    public Item(String oilType,String today, String tomorrow, String image, String image2, String unit_th, String unit_en) {
        super();
        this.oilType = oilType;
        this.today = today;
        this.tomorrow = tomorrow;
        this.image = image;
        this.image2 = image2;
        this.unit_th = unit_th;
        this.unit_en = unit_en;
    }

    public String getoilType() {
        return oilType;
    }

    public void setoilType(String oilType) {
        this.oilType = oilType;
    }

    public String gettoday() {
        return today;
    }

    public void settoday(String today) {
        this.today = today;
    }

    public String gettomorrow() {
        return tomorrow;
    }

    public void settomorrow(String tomorrow) {
        this.tomorrow = tomorrow;
    }

    public String getimage() {
        return image;
    }

    public void setimage(String image) {
        this.image = image;
    }
    public String getimage2() {
        return image2;
    }
    public void setimage2(String image2) {
        this.image = image2;
    }
    public String getunit_th() {
        return unit_th;
    }

    public void setunit_th(String unit_th) {
        this.unit_th = unit_th;
    }
    public String getunit_en() {
        return unit_en;
    }

    public void setunit_en(String unit_en) {
        this.unit_en = unit_en;
    }
    public Boolean isNotChange(){
        /*if (this.today == this.tomorrow)
            return Boolean.TRUE;
            else
                return Boolean.FALSE;*/
        return this.today.equals(this.tomorrow);
    }
    public void setChange(){
        Double todayPrice = Double.valueOf(this.today);
        Double tomorrowPrice = Double.valueOf(this.tomorrow);

        if (this.today.equals(this.tomorrow)) {
            this.change = "0.00";
        } else if (todayPrice<tomorrowPrice) {
            this.change = "+"+String.format("%.2f",(tomorrowPrice-todayPrice));
        } else {
            this.change = "-"+String.format("%.2f",(todayPrice-tomorrowPrice));
        }
    }
    public String getChange(){
        return change;
    }
    @Override
    public String toString() {
        return "item [oilType=" + oilType + ", today=" + today + ", tomorrow=" + tomorrow + ", change= " + change + ", unit_th="
                + unit_th + "]";
    }
}
