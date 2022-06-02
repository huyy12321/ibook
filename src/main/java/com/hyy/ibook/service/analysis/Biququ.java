package com.hyy.ibook.service.analysis;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hyy.ibook.entity.BookList;
import com.hyy.ibook.entity.BookName;
import com.hyy.ibook.entity.Channel;
import com.hyy.ibook.entity.SearchKey;
import com.hyy.ibook.mapper.BookNameMapper;
import com.hyy.ibook.service.BookListService;
import com.hyy.ibook.service.ChannelService;
import com.hyy.ibook.service.SearchKeyService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huyangyang
 */
@Service
public class Biququ extends AnalysisAbstract{
    @Resource
    private SearchKeyService searchKeyService;
    @Resource
    private BookListService bookListService;
    @Resource
    private BookNameMapper bookNameMapper;
    @Resource
    private ChannelService channelService;

    /**
     * 书源
     */
    protected String channelName = "笔趣趣";

    @Override
    public void analysisSearch(String keyword) {
        Channel channel = getChannel();
        SearchKey one = searchKeyService.getOne(Wrappers.<SearchKey>lambdaQuery().
                eq(SearchKey::getChannelId, channel.getId()));
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(channel.getLikeUrl());
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            List<NameValuePair> kvList = new ArrayList<>();
            kvList.add(new BasicNameValuePair(one.getSearch(), keyword));
            //包装成一个Entity对象(后面加字符集是为了向服务端发送数据时不会乱码)
            StringEntity paramEntity = new UrlEncodedFormEntity(kvList,"utf-8");
            httpPost.setEntity(paramEntity);
            response = httpClient.execute(httpPost);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            System.out.println("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                Document document = Jsoup.parse(EntityUtils.toString(responseEntity,"UTF-8"));


                Elements tr = document.getElementsByClass("search-list").get(0).getElementsByTag("ul").get(0).getElementsByTag("li");
                for(int i = 1;i<tr.size();i++) {
                    Elements children = tr.get(i).children();
                    String bookId = children.get(1).child(0).attr("href");
                    BookName one1 = bookNameMapper.selectOne(Wrappers.<BookName>lambdaQuery().
                            eq(BookName::getBookId, bookId).
                            eq(BookName::getChannelId, channel.getId()));
                    if(one1 == null) {
                        bookNameMapper.insert(BookName.builder().
                                bookId(bookId).heat(0).downStatus(0).
                                account(children.get(3).text()).
                                name(children.get(1).text()).
                                channelId(channel.getId()).
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

    }

    @Override
    public void analysisList(String id) {
        BookName bookName = bookNameMapper.selectById(id);
        if(StringUtils.isNotBlank(bookName.getInfo()) &&
                bookName.getUpdateTime().isAfter(LocalDateTime.now().minusDays(1))) {
            return;
        }
        Channel channel = getChannel();

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet httpGet = new HttpGet(bookName.getBookId());
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            System.out.println("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                Document document = Jsoup.parse(EntityUtils.toString(responseEntity, "UTF-8"));
                bookName.setInfo(document.getElementById("intro").text());

                IPage<BookList> page = bookListService.page(new Page<>(1, 1), Wrappers.<BookList>lambdaQuery().
                        eq(BookList::getBookId, Integer.valueOf(id)).orderByDesc(BookList::getId));
                String href = document.getElementById("info").child(4).child(0).attr("href");
                String src = document.getElementById("fmimg").child(0).attr("src");

                bookName.setImgUrl(src);
                bookName.setUpdateTime(LocalDateTime.now());
                bookNameMapper.updateById(bookName);

                if(page.getTotal() > 0 && href.equals(page.getRecords().get(0).getListId())) {
                    return;
                }

                for(Element element : document.getElementsByTag("dt").get(1).nextElementSiblings()) {
                    String listId = element.child(0).attr("href");
                    BookList one = bookListService.getOne(Wrappers.<BookList>lambdaQuery().
                            eq(BookList::getBookId, Integer.valueOf(id)).
                            eq(BookList::getListId, listId));
                    if(one == null) {
                        bookListService.save(BookList.builder().bookId(Integer.valueOf(id)).
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
                System.out.println(document.getElementsByClass("bookname").first().child(0).text());
                bookList.setListInfo(document.getElementById("content").getElementsByClass("read_tj").nextAll().toString().replace("<p>", "&nbsp;&nbsp;&nbsp;&nbsp;").replace("</p>", "<br>"));
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
