package com.hyy.ibook.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hyy.ibook.Entity.BookList;
import com.hyy.ibook.Entity.BookName;
import com.hyy.ibook.Entity.Channel;
import com.hyy.ibook.Entity.SearchKey;
import com.hyy.ibook.mapper.BookNameMapper;
import com.hyy.ibook.service.BookListService;
import com.hyy.ibook.service.BookNameService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hyy
 * @since 2021-10-08
 */
@Service
public class BookNameServiceImpl extends ServiceImpl<BookNameMapper, BookName> implements BookNameService {
    @Resource
    private ChannelService channelService;
    @Resource
    private SearchKeyService searchKeyService;
    @Resource
    private BookListService bookListService;

    @Override
    @Async
    public void updateBookName(String keyword) {
        List<Channel> list = channelService.list();
        for(Channel channel : list) {
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

                    //像js一样，通过id 获取文章列表元素对象
                    Elements tr = document.getElementsByTag("tr");
                    for(Element element : tr) {
                        if(element.child(0).children().size() > 0) {
                            String bookId = element.child(0).child(0).attr("href");
                            BookName one1 = baseMapper.selectOne(Wrappers.<BookName>lambdaQuery().
                                    eq(BookName::getBookId, bookId).
                                    eq(BookName::getChannelId, channel.getId()));
                            if(one1 == null) {
                                baseMapper.insert(BookName.builder().
                                        bookId(bookId).
                                        account(element.child(2).text()).
                                        name(element.child(0).child(0).text()).
                                        channelId(channel.getId()).
                                        updateTime(LocalDateTime.now()).build());
                            } else{
                                baseMapper.updateById(BookName.builder().
                                        bookId(bookId).
                                        id(one1.getId()).
                                        account(element.child(2).text()).
                                        name(element.child(0).child(0).text()).
                                        channelId(channel.getId()).
                                        updateTime(LocalDateTime.now()).build());
                            }

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
    }

    @Override
    @Async
    public void updateBookList(String id) {
        BookName bookName = baseMapper.selectById(id);
        if(StringUtils.isNotBlank(bookName.getInfo()) &&
                bookName.getUpdateTime().isAfter(LocalDateTime.now().minusDays(1))) {
            return;
        }
        Channel channel = channelService.getById(bookName.getChannelId());

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet httpGet = new HttpGet(channel.getInfoUrl().replace("{bookId}",bookName.getBookId()));
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

                if(page.getTotal() > 0 && href.equals(page.getRecords().get(0).getListId())) {
                    return;
                }
                bookName.setUpdateTime(LocalDateTime.now());
                baseMapper.updateById(bookName);

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
    @Async
    public void updateBookInfo(String id) {
        BookName bookName = baseMapper.selectById(id);
        Channel channel = channelService.getById(bookName.getChannelId());
        List<BookList> list = bookListService.list(Wrappers.<BookList>lambdaQuery().
                eq(BookList::getBookId, id).
                isNull(BookList::getListInfo));

        for(BookList bookList : list) {
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
                    bookList.setListInfo(document.getElementById("content").toString());
                    bookList.setUpdateTime(LocalDateTime.now());
                    bookListService.updateById(bookList);
                    Thread.sleep(1000);
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
    }
}
