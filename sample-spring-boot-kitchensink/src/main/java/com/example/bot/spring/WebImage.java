package com.example.bot.spring;

//Class that Converts the web page to Image
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.awt.*;
import java.awt.image.*;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
@Slf4j
public abstract class WebImage
{
    static class Kit extends HTMLEditorKit
    {
        private static final long serialVersionUID = 1;
        public Document createDefaultDocument() {
            HTMLDocument doc =
                    (HTMLDocument) super.createDefaultDocument();
            doc.setTokenThreshold(Integer.MAX_VALUE);
            doc.setAsynchronousLoadPriority(-1);
            return doc;
        }
    }
    private static String createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(path).build()
                .toUriString();
    }
    public static BufferedImage create
            (String src, int width, int height) {
        BufferedImage image = null;
        JEditorPane pane = new JEditorPane();
         Font fontT = new Font("sansserif",Font.PLAIN,20);

        Kit kit = new Kit();
        pane.setEditorKit(kit);
        pane.setEditable(false);
        pane.setMargin(new Insets(0,0,0,0));
        try {
/*            String bodyRule2 = "@charset \"UTF-8\";";
            ((HTMLDocument)pane.getDocument()).getStyleSheet().addRule(bodyRule2);*/
/*            String fontUrl = createUri("/static/buttons/THKrub.ttf");
            FileInputStream fis = new FileInputStream( fontUrl);
            Font fontT = Font.createFont(Font.TRUETYPE_FONT, fis)*/;
/*            ClassPathResource classPathResource = new ClassPathResource("/static/buttons/THKrub.ttf");
            InputStream inputStream = classPathResource.getInputStream();
            Font fontT = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            fontT = fontT.deriveFont ( Font.PLAIN, 30f );*/

            //pane.setPage(src);
            //pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
/*            Font fontT = UIManager.getFont("Label.font");*/
            pane.setFont(fontT);
/*            String bodyRule = "body { font-family: " + fontT.getFamily() + "; " +
                    "font-size: " + fontT.getSize() + "pt; }"; */

           String bodyRule = "body { font-family:sansserif; font-size: 20pt; }"; //sansserif
            log.info("bodyRule : "+bodyRule);
            ((HTMLDocument)pane.getDocument()).getStyleSheet().addRule(bodyRule);

           // pane.setContentType("text/html; charset=UTF-8"); //new
            pane.setText(src);
            image = new BufferedImage
                    (width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.createGraphics();
            Container c = new Container();
            SwingUtilities.paintComponent
                    (g, pane, c, 0, 0, width, height);
            g.dispose();
        } catch (Exception e) {
            // System.out.println(e);
            e.printStackTrace();
            log.info("InputStream error", e.getMessage());
        }
        return image;
    }
}
