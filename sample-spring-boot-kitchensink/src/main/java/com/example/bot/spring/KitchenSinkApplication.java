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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.common.io.ByteStreams;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import static com.example.bot.spring.KitchenSinkController.tomorrow_fm;
@Slf4j
@SpringBootApplication
// @EnableScheduling
public class KitchenSinkApplication {
    static Path downloadedContentDir;

    @Autowired
    private LineMessagingClient lineMessagingClient;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private DomainRepository domainRepository;
    @Autowired
    private OilchangeRepository oilchangeRepository;
    public static String today_fm = "begin";
    public static void main(String[] args) throws IOException {
        downloadedContentDir = Files.createTempDirectory("line-bot");
        SpringApplication.run(KitchenSinkApplication.class, args);
    }
    @Value
    public static class DownloadedContent {
        Path path;
        String uri;
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
    private static String createUri(String path) {
        try {
            RequestAttributes reqAttr = RequestContextHolder.currentRequestAttributes();

            if (reqAttr instanceof ServletRequestAttributes) {

                return ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(path).build()
                        .toUriString();

            }
        } catch (IllegalStateException e) {
            log.info("Unable to obtain request context user via RequestContextHolder.", e);
            RequestContextHolder.resetRequestAttributes();
        }
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

    @Component
    public class ApplicationStartup
            implements ApplicationListener<ApplicationReadyEvent> {

        /**
         * This event is executed as late as conceivably possible to indicate that
         * the application is ready to service requests.
         */

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
        private void pushText(@NonNull String userId, @NonNull String message)  {
            if (userId.isEmpty()) {
                throw new IllegalArgumentException("userId must not be empty");
            }
            if (message.length() > 1000) {
                message = message.substring(0, 1000 - 2) + "……";
            }
            this.pushT(userId, new TextMessage(message));

        }
        private void multipushImage(@NonNull Set<String> userId, @NonNull Message messages) {  //6-3-61
            try {
                BotApiResponse apiResponse = lineMessagingClient
                        .multicast(new Multicast(userId,messages))
                        .get();
                log.info("Sent messages: {}", apiResponse);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void onApplicationEvent(final ApplicationReadyEvent event) {
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Bangkok"));
            LocalDate tomorrow = today.plusDays(1);
            DateTimeFormatter patternFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            tomorrow_fm = patternFormatter.format(tomorrow);
            Domain monkDay;
            monkDay = domainRepository.findByDomain(tomorrow_fm);

            if (monkDay != null) { // tomorrow is monkDay
                if (!monkDay.isDisplayAds()) { // not notify monkday yet
                    List<Customer> customers = customerRepository.findAll();
                    Set<String> setUserId = new HashSet<String>();
                    if (customers.size() < 150) { // only one multicast
                        for (Customer customer : customers) {
                            if (customer.getUserId() != null)
                                setUserId.add(customer.getUserId());
                        }
                        multipushT(setUserId, new TextMessage("พรุ่งนี้วันพระ"));

                    } else { // more than one muticast
                        int i = 0;
                        for (Customer customer : customers) {
                            i = i + 1;
                            if (customer.getUserId() != null)
                                setUserId.add(customer.getUserId());
                            if (i % 150 == 0) {
                                multipushT(setUserId, new TextMessage("พรุ่งนี้วันพระ"));
                                // don't forget little delay
                                i = 0;
                                setUserId.clear();

                            }
                        }
                        if (setUserId.size() != 0) {  // last batch of userID
                            multipushT(setUserId, new TextMessage("พรุ่งนี้วันพระ"));
                            setUserId.clear();
                        }
                    }
                    domainRepository.updateDomain(monkDay.getDomain(), true);
                }

            }
            // pushText("U989982d2db82e4ec7698facb3186e0b3", "In App Ready");
            today_fm = patternFormatter.format(today);
            List<Oilchange> oilchangeDate;
            oilchangeDate = oilchangeRepository.findAll();
            String lastChangeDate = oilchangeDate.get(0).getOilchange();

            if (!today_fm.equals(lastChangeDate)) {
                BufferedImage ire = null;

                InputStream inputStream = null;
                ImageMessage oilPriceImg = null;
                try {
                    inputStream = new URL("https://crmmobile.bangchak.co.th/webservice/oil_price.aspx").openStream();
                } catch (IOException e) {
                    e.printStackTrace();
                    pushText("U989982d2db82e4ec7698facb3186e0b3", "error with webservice Bangchak");
                }
                try {
                    JAXBContext jaxbContext = JAXBContext.newInstance(Header.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                    Header oilprice = (Header) unmarshaller.unmarshal(inputStream);
                    if (oilprice.isSame()) {
                        pushText("U989982d2db82e4ec7698facb3186e0b3", "ราคาน้ำมันเท่าเดิม");
                    } else {
                        try {
                            ire = WebImage.create(oilprice.showHTML(), 533, 740);



                        } catch (Exception e) {
                            pushText("U989982d2db82e4ec7698facb3186e0b3", "error with create img"+e.getMessage());
                            e.printStackTrace();
                        }
                        DownloadedContent jpg = saveImage("png", ire);
                        DownloadedContent previewImg = createTempFile("png"); //

                        system(
                                "convert",
                                "-resize", "240x",
                                jpg.path.toString(),
                                previewImg.path.toString());
                        oilPriceImg = new ImageMessage(jpg.getUri(), jpg.getUri());
                        try {
                            List<Customer> customers = customerRepository.findAll();
                            Set<String> setUserId = new HashSet<String>();
                            if (customers.size() < 150) { // only one multicast
                                for (Customer customer : customers) {
                                    if (customer.getUserId() != null)
                                        setUserId.add(customer.getUserId());
                                }
                                multipushImage(setUserId, oilPriceImg);

                            } else { // more than one muticast
                                int i = 0;
                                for (Customer customer : customers) {
                                    i = i + 1;
                                    if (customer.getUserId() != null)
                                        setUserId.add(customer.getUserId());
                                    if (i % 150 == 0) {
                                        multipushImage(setUserId, oilPriceImg);
                                        // don't forget little delay
                                        i = 0;
                                        setUserId.clear();

                                    }
                                }
                                if (setUserId.size() != 0) {  // last batch of userID
                                    multipushImage(setUserId, oilPriceImg);
                                    setUserId.clear();
                                }
                            }

                        } catch (Exception e) {
                            pushText("U989982d2db82e4ec7698facb3186e0b3", "error with customer DB");
                            e.printStackTrace();
                        }
                        pushText("U989982d2db82e4ec7698facb3186e0b3", "ราคาน้ำมันเปลี่ยน");

                        oilchangeRepository.delete(oilchangeDate.get(0));

                        Oilchange newOilChange = new Oilchange();
                        newOilChange.setOilchange(today_fm);
                        oilchangeRepository.save(newOilChange);
                        pushText("U989982d2db82e4ec7698facb3186e0b3", "change DB with " + today_fm);
                    }
                } catch (Exception e) {
                    pushText("U989982d2db82e4ec7698facb3186e0b3", "error with DB"+e.getMessage());
                    e.printStackTrace();
                }
            } else {  // today send already
                pushText("U989982d2db82e4ec7698facb3186e0b3", "today already send ");
            }

            return;
        }
    }

}
