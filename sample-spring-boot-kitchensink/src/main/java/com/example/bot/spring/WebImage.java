package com.example.bot.spring;

//Class that Converts the web page to Image
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public abstract class WebImage
{
    static class Kit extends HTMLEditorKit
    {
        public Document createDefaultDocument() {
            HTMLDocument doc =
                    (HTMLDocument) super.createDefaultDocument();
            doc.setTokenThreshold(Integer.MAX_VALUE);
            doc.setAsynchronousLoadPriority(-1);
            return doc;
        }
    }

    public static BufferedImage create
            (String src, int width, int height) {
        BufferedImage image = null;
        JEditorPane pane = new JEditorPane();
        Kit kit = new Kit();
        pane.setEditorKit(kit);
        pane.setEditable(false);
        pane.setMargin(new Insets(0,0,0,0));
        try {
            //pane.setPage(src);
            pane.setText(src);
            image = new BufferedImage
                    (width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.createGraphics();
            Container c = new Container();
            SwingUtilities.paintComponent
                    (g, pane, c, 0, 0, width, height);
            g.dispose();
        } catch (Exception e) {
            System.out.println(e);
        }
        return image;
    }
}
