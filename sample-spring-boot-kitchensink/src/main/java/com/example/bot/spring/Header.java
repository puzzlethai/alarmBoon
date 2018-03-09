package com.example.bot.spring;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@XmlRootElement(name = "header")
@XmlAccessorType(XmlAccessType.FIELD)
public class Header {
    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "copyright")
    private String copyright;

    @XmlElement(name = "update_date")
    private String update_date;

    @XmlElement(name = "price_link")
    private String price_link;

    @XmlElement(name = "remark_th")
    private String remark_th;

    @XmlElement(name = "remark_en")
    private String remark_en;

    @XmlElement(name = "bankchak_logo")
    private String bankchak_logo;

    @XmlElement(name = "item")
    private List<Item> items;

    public void checkDup(){
        List<Item> items = this.items;
        Integer checkDup = 0;
        checkDup = items.size();
        System.out.println("checkDup :"+checkDup);
        if (checkDup == 14) {
            items.remove(0);
            items.remove(0);
            items.remove(0);
            items.remove(0);
            items.remove(0);
            items.remove(0);
            items.remove(0);
        }
        System.out.println("size :"+this.items.size());
    }

/*    @Override
    public String toString() {

        List<Item> items = this.items;
        String temp = " ";
        int i = 0;
        for (Item item : items) {
            temp = temp+ item.toString()+String.valueOf(i++);
        }
        return temp;
    }*/

    public Boolean isSame() {
        List<Item> items = this.items;
        Boolean temp = Boolean.TRUE;
        for (Item item : items) {
            temp = temp&&item.isNotChange();
            item.setChange();
        }
        return temp;
    }



    public String showHTML(){
        List<Item> items = this.items;
        Locale lc = new Locale("th","TH");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM uuuu",lc);

        String todayStr = now.format(formatter);
        StringBuilder tempStr = new StringBuilder("<!DOCTYPE html><html lang=\"th\" ><head><meta charset=\"UTF-8\">\n" +
               // "    <link href=\"https://fonts.googleapis.com/css?family=Sriracha\" rel=\"stylesheet\" type='text/css'>\n" +
/*                "    <style type=\"text/css\">th { border-bottom: 1px solid #2cff1a;padding: 15px;text-align: center;}\n" +
                "    @font-face {\n" +
                "        font-family: 'Sriracha';\n" +
                "        unicode-range: U+0E00-0E7F;\n" +
                "    }\n" +*/
                "td {color:black; border-bottom: 1px solid #9cff8b; padding: 15px; text-align: center;}\n" +
                "table {}\n" +
                "    .blue{\n" +
                "        color:blue;\n" +
                "        background: none;\n" +
                "    }</style>\n" +
                "</head><body style=\"font-family: 'Lucida Grande', sans-serif; font-size: medium\"><table cellspacing=\"0\" >\n" +
                "    <tr bgcolor=\"#57b33e\">\n" + //  style="font-family: 'Lucida Sans Unicode', 'Lucida Grande', sans-serif; font-size: medium"
                "        <td colspan=\"4\" style=\"padding: 0px;color:white\"><h3>ราคาน้ำมัน</h3></td></tr>\n" +
                "    <tr style=\"color:blue\"><th>"+todayStr+"</th><th>วันนี้</th><th>พรุ่งนี้</th><th>ส่วนต่าง</th></tr>\n" +
                "    <tr bgcolor=\"orange\" ><th>ชนิดน้ำมัน</th><th>บาท/ลิตร</th><th>บาท/ลิตร</th><th>บาท</th></tr>");

        for (Item item : items) {
            if (item.getoilType().equals("NGV")){
                tempStr.append("</tr><tr bgcolor=\"orange\"><th>ชนิดน้ำมัน</th>" + "<th>บาท/กก.</th>" + "<th>บาท/กก.</th>" + "<th>บาท</th></tr>");
            }
            String colorStr = "";
            String boldBegin = "";
            String boldEnd = "";
            item.setChange();
            switch (item.getChange().substring(0,1)){
                case "+": colorStr = "style=\"color:red\"";
                    boldBegin = "<b>";
                    boldEnd = "</b>";
                    break;
                case "-": colorStr = "style=\"color:#1fa743\""; // #1fa743 #3cb371 #0bb344 #32cd32
                    boldBegin = "<b>";
                    boldEnd = "</b>";
                    break;
                default: colorStr = "style=\"color:black\"";
            }

            tempStr.append("<tr><td>").append(item.getoilType()).append("</td>").append("<td>").append(item.gettoday()).append("</td>").append("<td ").append(colorStr).append(">").append(boldBegin).append(item.gettomorrow()).append(boldEnd).append("</td>").append("<td ").append(colorStr).append(">").append(boldBegin).append(item.getChange()).append(boldEnd).append("</td></tr>");
        }

        tempStr.append("<tr style=\"border-bottom: 0px;\"><th colspan=\"4\" style=\"padding: 10px;border-bottom: 0px;\">" + "เตือนราคาน้ำมันฟรีแค่ Add LINE &nbsp <span class=\"blue\">@hpd8343b</span></th></tr>" + "<td colspan=\"4\" style=\"padding: 0px;border-bottom: 0px;\"><h6>ขอบคุณข้อมูลจาก บางจาก</h6>" + "</table></body></html>");
        return tempStr.toString();
    }
}
