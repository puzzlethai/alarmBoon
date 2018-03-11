/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.example.bot.spring;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat; //Just Add
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.action.DatetimePickerAction;
import com.linecorp.bot.model.message.template.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled; //Just Add
import org.springframework.stereotype.Component; // Just Add
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.io.ByteStreams;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
//import com.linecorp.bot.model.event.BeaconEvent;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.AudioMessageContent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.message.VideoMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.AudioMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.ImagemapMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.VideoMessage;
import com.linecorp.bot.model.message.imagemap.ImagemapArea;
import com.linecorp.bot.model.message.imagemap.ImagemapBaseSize;
import com.linecorp.bot.model.message.imagemap.MessageImagemapAction;
import com.linecorp.bot.model.message.imagemap.URIImagemapAction;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;       // Just Add
import org.slf4j.LoggerFactory; // Just Add

// TestMongoDB
import com.example.bot.spring.Domain;
import com.example.bot.spring.DomainRepository;
import com.example.bot.spring.Customer;
import com.example.bot.spring.CustomerRepository;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

@Slf4j
@LineMessageHandler
public class KitchenSinkController {
    private static String tomorrow_fm = "Start";
    @Autowired
    private LineMessagingClient lineMessagingClient;
    @Autowired
    private DomainRepository domainRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
        TextMessageContent message = event.getMessage();
        handleTextContent(event.getReplyToken(), event, message);
    }

    @EventMapping
    public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
        handleSticker(event.getReplyToken(), event.getMessage());
    }

    @EventMapping
    public void handleLocationMessageEvent(MessageEvent<LocationMessageContent> event) {
        LocationMessageContent locationMessage = event.getMessage();
        reply(event.getReplyToken(), new LocationMessage(
                locationMessage.getTitle(),
                locationMessage.getAddress(),
                locationMessage.getLatitude(),
                locationMessage.getLongitude()
        ));
    }

    @EventMapping
    public void handleImageMessageEvent(MessageEvent<ImageMessageContent> event) throws IOException {
        // You need to install ImageMagick
        handleHeavyContent(
                event.getReplyToken(),
                event.getMessage().getId(),
                responseBody -> {
                    DownloadedContent jpg = saveContent("jpg", responseBody);
                    DownloadedContent previewImg = createTempFile("jpg");
                    system(
                            "convert",
                            "-resize", "240x",
                            jpg.path.toString(),
                            previewImg.path.toString());
                    reply(((MessageEvent) event).getReplyToken(),
                          new ImageMessage(jpg.getUri(), jpg.getUri()));
                });
    }

    @EventMapping
    public void handleAudioMessageEvent(MessageEvent<AudioMessageContent> event) throws IOException {
        handleHeavyContent(
                event.getReplyToken(),
                event.getMessage().getId(),
                responseBody -> {
                    DownloadedContent mp4 = saveContent("mp4", responseBody);
                    reply(event.getReplyToken(), new AudioMessage(mp4.getUri(), 100));
                });
    }

    @EventMapping
    public void handleVideoMessageEvent(MessageEvent<VideoMessageContent> event) throws IOException {
        // You need to install ffmpeg and ImageMagick.
        handleHeavyContent(
                event.getReplyToken(),
                event.getMessage().getId(),
                responseBody -> {
                    DownloadedContent mp4 = saveContent("mp4", responseBody);
                    DownloadedContent previewImg = createTempFile("jpg");
                    system("convert",
                           mp4.path + "[0]",
                           previewImg.path.toString());
                    reply(((MessageEvent) event).getReplyToken(),
                          new VideoMessage(mp4.getUri(), previewImg.uri));
                });
    }

    @EventMapping
    public void handleUnfollowEvent(UnfollowEvent event) {
        log.info("unfollowed this bot: {}", event);
        String userId = event.getSource().getUserId();
        if (userId != null) {
            Customer temp_user;
            temp_user = customerRepository.findByUserId(userId);
            if (temp_user != null) {
                customerRepository.delete(temp_user);
            }
        }
    }

    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Got followed event");
        String userId = event.getSource().getUserId();
        if (userId != null) {
            Customer temp_user;
            temp_user = customerRepository.findByUserId(userId);
            if (temp_user == null) {
                try {
                    Customer customer = new Customer();
                    customer.setUserId(userId);
                    customer.setMonkDay(Boolean.TRUE);
                    customerRepository.save(customer);
                    this.pushText(userId,"Add to DB successful");
                } catch  (Exception e) {
                    this.pushText(userId,"can't Add to DB");
                    log.info("duplicate key", e);
                }
            }
        }

    }

    @EventMapping
    public void handleJoinEvent(JoinEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Joined " + event.getSource());
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Got postback data " + event.getPostbackContent().getData() + ", param " + event.getPostbackContent().getParams().toString());
    }

/*    @EventMapping
    public void handleBeaconEvent(BeaconEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Got beacon message " + event.getBeacon().getHwid());
    }*/

    @EventMapping
    public void handleOtherEvent(Event event) {
        log.info("Received message(Ignored): {}", event);
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    private void pushT(@NonNull String userId, @NonNull Message message) {
        pushT(userId, Collections.singletonList(message));
    }

    private void pushT(@NonNull String userId, @NonNull List<Message> messages) {
        try {
            BotApiResponse apiResponse = lineMessagingClient
                    .pushMessage(new PushMessage(userId, messages))
                    .get();
            log.info("Sent messages: {}", apiResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        try {
            BotApiResponse apiResponse = lineMessagingClient
                    .replyMessage(new ReplyMessage(replyToken, messages))
                    .get();
            log.info("Sent messages: {}", apiResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
     private void pushText(@NonNull String userId, @NonNull String message)  {
         if (userId.isEmpty()) {
             throw new IllegalArgumentException("userId must not be empty");
         }
         if (message.length() > 1000) {
             message = message.substring(0, 1000 - 2) + "……";
         }
         this.pushT(userId, new TextMessage(message));

    }
    private void replyText(@NonNull String replyToken, @NonNull String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "……";
        }
        this.reply(replyToken, new TextMessage(message));
    }

    private void handleHeavyContent(String replyToken, String messageId,
                                    Consumer<MessageContentResponse> messageConsumer) {
        final MessageContentResponse response;
        try {
            response = lineMessagingClient.getMessageContent(messageId)
                                          .get();
        } catch (InterruptedException | ExecutionException e) {
            reply(replyToken, new TextMessage("Cannot get image: " + e.getMessage()));
            throw new RuntimeException(e);
        }
        messageConsumer.accept(response);
    }

    private void handleSticker(String replyToken, StickerMessageContent content) {
        reply(replyToken, new StickerMessage(
                content.getPackageId(), content.getStickerId())
        );
    }
    private void multipushT(@NonNull Set<String> userId, @NonNull Message messages) {  //6-3-61
        try {
            BotApiResponse apiResponse = lineMessagingClient
                    .multicast(new Multicast(userId,messages))
                    .get();
            log.info("Sent messages: {}", apiResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    private void handleTextContent(String replyToken, Event event, TextMessageContent content)
            throws Exception {
        String text = content.getText();

        log.info("Got text message from {}: {}", replyToken, text);
        switch (text) {
            case "profile": {
                String userId = event.getSource().getUserId();
                if (userId != null) {
                    lineMessagingClient
                            .getProfile(userId)
                            .whenComplete((profile, throwable) -> {
                                if (throwable != null) {
                                    this.replyText(replyToken, throwable.getMessage());
                                    return;
                                }

                                this.reply(
                                        replyToken,
                                        Arrays.asList(new TextMessage(
                                                              "Display name: " + profile.getDisplayName()),
                                                      new TextMessage("Status message: "
                                                                      + profile.getStatusMessage()))
                                );

                            });


                    List<Domain> users = domainRepository.findAll();
                    for (Domain user : users)
                    this.pushText(userId,"Hello tomorrow is :"+user.getDomain());

                } else {
                    this.replyText(replyToken, "Bot can't use profile API without user ID");
                }


                break;
            }
            case "bye": {
                Source source = event.getSource();
                if (source instanceof GroupSource) {
                    this.replyText(replyToken, "Leaving group");
                    lineMessagingClient.leaveGroup(((GroupSource) source).getGroupId()).get();
                } else if (source instanceof RoomSource) {
                    this.replyText(replyToken, "Leaving room");
                    lineMessagingClient.leaveRoom(((RoomSource) source).getRoomId()).get();
                } else {
                    this.replyText(replyToken, "Bot can't leave from 1:1 chat");
                }
                break;
            }
            case "confirm": {
                ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                        "Do it?",
                        new MessageAction("Yes", "Yes!"),
                        new MessageAction("No", "No!")
                );
                TemplateMessage templateMessage = new TemplateMessage("Confirm alt text", confirmTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "buttons": {
                String imageUrl = createUri("/static/buttons/1040.jpg");
                ButtonsTemplate buttonsTemplate = new ButtonsTemplate(
                        imageUrl,
                        "My button sample",
                        "Hello, my button",
                        Arrays.asList(
                                new URIAction("Go to line.me",
                                              "https://line.me"),
                                new PostbackAction("Say hello1",
                                                   "hello こんにちは"),
                                new PostbackAction("言 hello2",
                                                   "hello こんにちは",
                                                   "hello こんにちは"),
                                new MessageAction("Say message",
                                                  "Rice=米")
                        ));
                TemplateMessage templateMessage = new TemplateMessage("Button alt text", buttonsTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "carousel": {
                String imageUrl = createUri("/static/buttons/1040.jpg");
                CarouselTemplate carouselTemplate = new CarouselTemplate(
                        Arrays.asList(
                                new CarouselColumn(imageUrl, "hoge", "fuga", Arrays.asList(
                                        new URIAction("Go to line.me",
                                                      "https://line.me"),
                                        new URIAction("Go to line.me",
                                                "https://line.me"),
                                        new PostbackAction("Say hello1",
                                                           "hello こんにちは")
                                )),
                                new CarouselColumn(imageUrl, "hoge", "fuga", Arrays.asList(
                                        new PostbackAction("言 hello2",
                                                           "hello こんにちは",
                                                           "hello こんにちは"),
                                        new PostbackAction("言 hello2",
                                                "hello こんにちは",
                                                "hello こんにちは"),
                                        new MessageAction("Say message",
                                                          "Rice=米")
                                )),
                                new CarouselColumn(imageUrl, "Datetime Picker", "Please select a date, time or datetime", Arrays.asList(
                                        new DatetimePickerAction("Datetime",
                                                "action=sel",
                                                "datetime",
                                                "2017-06-18T06:15",
                                                "2100-12-31T23:59",
                                                "1900-01-01T00:00"),
                                        new DatetimePickerAction("Date",
                                                "action=sel&only=date",
                                                "date",
                                                "2017-06-18",
                                                "2100-12-31",
                                                "1900-01-01"),
                                        new DatetimePickerAction("Time",
                                                "action=sel&only=time",
                                                "time",
                                                "06:15",
                                                "23:59",
                                                "00:00")
                                ))
                        ));
                TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "image_carousel": {
                String imageUrl = createUri("/static/buttons/1040.jpg");
                ImageCarouselTemplate imageCarouselTemplate = new ImageCarouselTemplate(
                        Arrays.asList(
                                new ImageCarouselColumn(imageUrl,
                                        new URIAction("Goto line.me",
                                                "https://line.me")
                                ),
                                new ImageCarouselColumn(imageUrl,
                                        new MessageAction("Say message",
                                                "Rice=米")
                                ),
                                new ImageCarouselColumn(imageUrl,
                                        new PostbackAction("言 hello2",
                                                "hello こんにちは",
                                                "hello こんにちは")
                                )
                        ));
                TemplateMessage templateMessage = new TemplateMessage("ImageCarousel alt text", imageCarouselTemplate);
                this.reply(replyToken, templateMessage);
                break;
            }
            case "test": { //6-3-61
                String userId = event.getSource().getUserId();
/*                String imageUrl = createUri("/static/buttons/1040.jpg");
                DownloadedContent previewImg = createTempFile("jpg");
                pushT(userId,new ImageMessage(imageUrl, previewImg.getUri()));*/
                BufferedImage ire;
/*                ire = WebImage.create("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Title</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <H1> I LOVE U</H1>\n" +
                "</body>\n" +
                "</html>", 533, 740);*/
                InputStream inputStream = null;
                try {
                    inputStream = new URL("https://crmmobile.bangchak.co.th/webservice/oil_price.aspx").openStream();
                } catch (IOException e){
                    e.printStackTrace();
                }
                try {
                    JAXBContext jaxbContext = JAXBContext.newInstance(Header.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                    Header oilprice = (Header) unmarshaller.unmarshal(inputStream);

                    ire = WebImage.create(oilprice.showHTML(), 533, 740);
//You can convert the BufferedImage to any format that you wish, jpg I thought was the best format

                    DownloadedContent jpg = saveImage("png", ire);
                    DownloadedContent previewImg = createTempFile("png");
                    system(
                            "convert",
                            "-resize", "240x",
                            jpg.path.toString(),
                            previewImg.path.toString());
                    reply(((MessageEvent) event).getReplyToken(),
                            new ImageMessage(jpg.getUri(), jpg.getUri()));

                } catch (Exception e) {
                    e.printStackTrace();
                }



                this.reply(replyToken, new TextMessage(tomorrow_fm));
                break;
            }
            case "imagemap":
                this.reply(replyToken, new ImagemapMessage(
                        createUri("/static/rich"),
                        "This is alt text",
                        new ImagemapBaseSize(1040, 1040),
                        Arrays.asList(
                                new URIImagemapAction(
                                        "https://store.line.me/family/manga/en",
                                        new ImagemapArea(
                                                0, 0, 520, 520
                                        )
                                ),
                                new URIImagemapAction(
                                        "https://store.line.me/family/music/en",
                                        new ImagemapArea(
                                                520, 0, 520, 520
                                        )
                                ),
                                new URIImagemapAction(
                                        "https://store.line.me/family/play/en",
                                        new ImagemapArea(
                                                0, 520, 520, 520
                                        )
                                ),
                                new MessageImagemapAction(
                                        "URANAI!",
                                        new ImagemapArea(
                                                520, 520, 520, 520
                                        )
                                )
                        )
                ));
                break;
            default:
                log.info("Returns echo message {}: {}", replyToken, text);
                this.replyText(
                        replyToken,
                        text+tomorrow_fm
                );
                break;
        }
    }

    private static String createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                                          .path(path).build()
                                          .toUriString();
    }

    private void system(String... args) {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        try {
            Process start = processBuilder.start();
            int i = start.waitFor();
            log.info("result: {} =>  {}", Arrays.toString(args), i);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            log.info("Interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    private static DownloadedContent saveContent(String ext, MessageContentResponse responseBody) {
        log.info("Got content-type: {}", responseBody);

        DownloadedContent tempFile = createTempFile(ext);
        try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
            ByteStreams.copy(responseBody.getStream(), outputStream);
            log.info("Saved {}: {}", ext, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    private static DownloadedContent saveImage(String ext, BufferedImage bfimage) {


        DownloadedContent tempFile = createTempFile(ext);
        try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bfimage, ext, os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            ByteStreams.copy(is, outputStream);
            log.info("Saved {}: {}", ext, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    private static DownloadedContent createTempFile(String ext) {
        String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID().toString() + '.' + ext;
        Path tempFile = KitchenSinkApplication.downloadedContentDir.resolve(fileName);
        tempFile.toFile().deleteOnExit();
        return new DownloadedContent(tempFile, createUri("/downloaded/" + tempFile.getFileName()));
    }

    @Value
    public static class DownloadedContent {
        Path path;
        String uri;
    }
    @Component
    public class ScheduledTasks {

        private final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        private void pushT(@NonNull String userId, @NonNull Message message) {
            pushT(userId, Collections.singletonList(message));
        }
        /*
        private void multipushT(@NonNull String userId, @NonNull Message message) {
            multipushT((Set<String>) Collections.singletonList(userId), Collections.singletonList(message));
        }
        */
        private void pushT(@NonNull String userId, @NonNull List<Message> messages) {
            try {
                BotApiResponse apiResponse = lineMessagingClient
                        .pushMessage(new PushMessage(userId, messages))
                        .get();
                log.info("Sent messages: {}", apiResponse);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        private void multipushT(@NonNull Set<String> userId, @NonNull Message messages) {
            try {
                BotApiResponse apiResponse = lineMessagingClient
                        .multicast(new Multicast(userId,messages))
                        .get();
                log.info("Sent messages: {}", apiResponse);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        private void pushText(@NonNull String userId, @NonNull String message)  {
            if (userId.isEmpty()) {
                throw new IllegalArgumentException("userId must not be empty");
            }
            if (message.length() > 1000) {
                message = message.substring(0, 1000 - 2) + "……";
            }
            this.pushT(userId, new TextMessage(message));

        }
        /*
        private void multipushText(@NonNull String userId, @NonNull String message)  {
            if (userId.isEmpty()) {
                throw new IllegalArgumentException("userId must not be empty");
            }
            if (message.length() > 1000) {
                message = message.substring(0, 1000 - 2) + "……";
            }
            this.multipushT(userId, new TextMessage(message));

        }
        */
        @Scheduled(cron = @reboot )
        public void runatstart(){
            tomorrow_fm = "runatreboot";
        }


/*        @Scheduled(initialDelay=60000, fixedRate=300000)
        public void reportCurrentTime() {
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Bangkok"));
            LocalDate tomorrow = today.plusDays(1);
            DateTimeFormatter patternFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            tomorrow_fm = patternFormatter.format(tomorrow);
            Domain monkDay;
            monkDay = domainRepository.findByDomain(tomorrow_fm);

            if (monkDay != null){ // tomorrow is monkDay
                if (!monkDay.isDisplayAds()) { // not notify monkday yet
                    List<Customer> customers = customerRepository.findAll();
                    Set<String> setUserId = new HashSet<String>();
                    if (customers.size() < 150) { // only one multicast
                        for (Customer customer : customers) {
                            if (customer.getUserId() != null)
                                setUserId.add(customer.getUserId());
                        }
                        multipushT(setUserId,new TextMessage("พรุ่งนี้วันพระ"));

                    } else { // more than one muticast
                        int i = 0;
                        for (Customer customer : customers) {
                            i=i+1;
                            if (customer.getUserId() != null)
                                setUserId.add(customer.getUserId());
                            if (i%150 == 0){
                                multipushT(setUserId,new TextMessage("พรุ่งนี้วันพระ"));
                                // don't forget little delay
                                i=0;
                                setUserId.clear();

                            }
                        }
                        if (setUserId.size()!=0){  // last batch of userID
                            multipushT(setUserId,new TextMessage("พรุ่งนี้วันพระ"));
                            setUserId.clear();
                        }
                    }
                    domainRepository.updateDomain(monkDay.getDomain(), true);
                }

            }
        }*/
    }
}
