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
        //Locale lc = new Locale("th","TH");
        LocalDateTime now = LocalDateTime.now();
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM uuuu",lc);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM uuuu");
        String todayStr = now.format(formatter);
        // "    <link href=\"https://fonts.googleapis.com/css?family=Sriracha\" rel=\"stylesheet\" type='text/css'>\n" +
/*                "    <style type=\"text/css\">th { border-bottom: 1px solid #2cff1a;padding: 15px;text-align: center;}\n" +
                "    @font-face {\n" +
                "        font-family: 'Sriracha';\n" +
                "        unicode-range: U+0E00-0E7F;\n" +
                "    }\n" +*/  //"+todayStr+"
        StringBuilder tempStr = new StringBuilder("<!DOCTYPE html><html lang=\"th\" ><head><meta charset=\"UTF-8\">\n" +
                // "    <link href=\"https://fonts.googleapis.com/css?family=Sriracha\" rel=\"stylesheet\" type='text/css'>\n" +
                "    <style type=\"text/css\">th { border-bottom: 1px solid #2cff1a;padding: 15px;text-align: center;}\n" +
/*                "    @font-face {\n" +
                "        font-family: 'th_krubregular';\n" +
                "        src: url('/static/buttons/th_krub-webfont.eot');\n" +
                "        src: url('/static/buttons/th_krub-webfont.eot?#iefix') format('embedded-opentype'),\n" +
                "        url('/static/buttons/th_krub-webfont.woff2') format('woff2'),\n" +
                "        url('/static/buttons/th_krub-webfont.woff') format('woff'),\n" +
                "        url('/static/buttons/th_krub-webfont.svg#th_krubregular') format('svg');\n" +
                "        font-weight: normal;\n" +
                "        font-style: normal;\n" +
                "\n" +
                "    }\n" +
                "    @font-face {\n" +
                "        font-family: 'th_krubbold';\n" +
                "        src: url('/static/buttons/th_krub_bold-webfont.eot');\n" +
                "        src: url('/static/buttons/th_krub_bold-webfont.eot?#iefix') format('embedded-opentype'),\n" +
                "        url('/static/buttons/th_krub_bold-webfont.woff2') format('woff2'),\n" +
                "        url('/static/buttons/th_krub_bold-webfont.woff') format('woff'),\n" +
                "        url('/static/buttons/th_krub_bold-webfont.svg#th_krubbold') format('svg');\n" +
                "        font-weight: normal;\n" +
                "        font-style: normal;\n" +
                "\n" +
                "    }" +*/

                "td {color:black; border-bottom: 1px solid #9cff8b; padding: 15px; text-align: center;}\n" +
                "table {}\n" +
                "    .blue{\n" +
                "        color:blue;\n" +
                "        background: none;\n" +
                "    }</style>\n" +
                "</head><body style=\"font-family: 'Ubuntu'; font-size: large \"><table cellspacing=\"0\" >\n" +
                "    <tr bgcolor=\"#57b33e\">\n" +
                "        <td colspan=\"4\" style=\"font-family:'Ubuntu'; font-size: x-large; padding: 15px;color:white\">Oil Price Change<h5> with the courtesy of Bangchak</h5></td></tr>\n" +
                "    <tr style=\"color:blue\"><th>"+todayStr+"</th><th>Today</th><th>Tomorrow</th><th>Diff.</th></tr>\n" +
                "    <tr bgcolor=\"orange\" ><th>OilType</th><th>Baht/l.</th><th>Baht/l.</th><th>Baht</th></tr>");

        for (Item item : items) {
            if (item.getoilType().equals("NGV")){
                tempStr.append("</tr><tr bgcolor=\"orange\"><th>OilType</th>" + "<th>Baht/kg.</th>" + "<th>Baht/kg.</th>" + "<th>Bath</th></tr>");
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

        tempStr.append("<tr style=\"border-bottom: 0px;\"><th colspan=\"4\" style=\"padding: 10px;border-bottom: 0px;\">" + "Free oil price alarm just Add LINE &nbsp <span class=\"blue\">@hpd8343b</span></th></tr>" + "<td colspan=\"4\" style=\"padding: 0px;border-bottom: 0px;\"><h6>Thanks for the info from Bangchak</h6>" + "</table></body></html>");
        return tempStr.toString();
    }
}
