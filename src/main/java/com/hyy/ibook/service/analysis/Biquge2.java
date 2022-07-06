package com.hyy.ibook.service.analysis;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hyy.ibook.entity.BookList;
import com.hyy.ibook.entity.BookName;
import com.hyy.ibook.entity.Channel;
import com.hyy.ibook.mapper.BookNameMapper;
import com.hyy.ibook.service.BookListService;
import com.hyy.ibook.service.ChannelService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author huyangyang
 */
@Service
public class Biquge2 extends AnalysisAbstract{
    @Resource
    private BookListService bookListService;
    @Resource
    private BookNameMapper bookNameMapper;
    @Resource
    private ChannelService channelService;

    /**
     * 书源
     */
    protected String channelName = "书源3";

    @Override
    public void analysisSearch(String keyword) {
        Channel channel = getChannel();
        HttpRequest get = HttpUtil.createGet(String.format(channel.getLikeUrl(), keyword));
        String body = get.execute().body();
        String substring = body.substring(1, body.length() - 1);
        JSONArray jsonArray = (JSONArray) JSONUtil.parseObj(substring).get("data");
        jsonArray.forEach(t->{
            JSONObject jsonObject = JSONUtil.parseObj(t);
            BookName one1 = bookNameMapper.selectOne(Wrappers.<BookName>lambdaQuery().
                    eq(BookName::getBookId, jsonObject.get("Id").toString()).
                    eq(BookName::getChannelId, channel.getId()));
            if(one1 == null) {
                bookNameMapper.insert(BookName.builder().
                        bookId(jsonObject.get("Id").toString()).heat(0).downStatus(0).
                        account(jsonObject.get("Author").toString()).
                        name(jsonObject.get("Name").toString()).
                        channelId(channel.getId())
                        .imgUrl(jsonObject.get("Img").toString())
                        .info(jsonObject.get("Desc").toString()).
                        updateTime(LocalDateTime.now()).build());
            }
        });
    }

    @Override
    public void analysisList(String id) {
        BookName bookName = bookNameMapper.selectById(id);
        if(bookName.getUpdateTime().isAfter(LocalDateTime.now().minusDays(1))) {
            return;
        }
        Channel channel = getChannel();
        int count = bookListService.count(Wrappers.<BookList>lambdaQuery()
                .eq(BookList::getBookId, bookName.getId()));
        String replace = channel.getInfoUrl().replace("{bookId}", bookName.getBookId());
        analysis(String.format(replace,count/20 + 1),bookName,channel);
    }

    @SneakyThrows
    @Async
    public void analysis(String url,BookName bookName,Channel channel) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        Document document = null;
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                document = Jsoup.parse(EntityUtils.toString(responseEntity, "UTF-8"));

                for(Element element : document.getElementsByClass("section-list fix").get(1).children()) {
                    String listId = element.child(0).attr("href");
                    BookList one = bookListService.getOne(Wrappers.<BookList>lambdaQuery().
                            eq(BookList::getBookId, bookName.getId()).
                            eq(BookList::getListId, listId));
                    if(one == null) {
                        bookListService.save(BookList.builder().bookId(bookName.getId()).
                                listId(listId).
                                listName(element.child(0).text()).
                                updateTime(LocalDateTime.now()).build());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(document != null) {
            String attr = document.getElementsByClass("right").get(0).child(0).attr("href");
            if(StringUtils.isNotBlank(attr)) {
                attr = attr.substring(1);
                Thread.sleep(1000);
                analysis(channel.getChannelUrl() + attr,bookName,channel);
            }
        }
    }



    @Override
    public void analysisInfo(String id) {
        Channel channel = getChannel();
        BookList bookList = bookListService.getById(id);

        if(StringUtils.isNotBlank(bookList.getListInfo())) {
            return;
        }
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet httpGet = new HttpGet(channel.getChannelUrl() + bookList.getListId());
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            System.out.println("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                Document document = Jsoup.parse(EntityUtils.toString(responseEntity, "UTF-8"));
                bookList.setListInfo(document.getElementById("content").toString());
                bookList.setUpdateTime(LocalDateTime.now());
                bookListService.updateById(bookList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Channel getChannel() {
        return channelService.getOne(Wrappers.<Channel>lambdaQuery().eq(Channel::getChannelName, channelName));
    }

}
