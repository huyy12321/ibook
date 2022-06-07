package com.hyy.ibook.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hyy.ibook.entity.*;
import com.hyy.ibook.mapper.BookNameMapper;
import com.hyy.ibook.mapper.ChannelMapper;
import com.hyy.ibook.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyy.ibook.service.analysis.AnalysisAbstract;
import com.hyy.ibook.service.analysis.Biquge;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hyy
 * @since 2021-10-08
 */
@Service
@RequiredArgsConstructor
public class BookNameServiceImpl extends ServiceImpl<BookNameMapper, BookName> implements BookNameService {

    private final BookListService bookListService;
    private final Map<String, AnalysisAbstract> map;
    private final ChannelMapper channelMapper;


    @Override
    @Async
    public void updateBookName(String keyword,Integer channelId) {
        Channel channel = channelMapper.selectById(channelId);
        AnalysisAbstract analysisAbstract = map.get(channel.getStrategy());
        analysisAbstract.analysisSearch(keyword);
    }

    @Override
    @Async
    public void updateBookList(String id) {
        BookName bookName = baseMapper.selectById(id);
        Channel channel = channelMapper.selectById(bookName.getChannelId());
        AnalysisAbstract analysisAbstract = map.get(channel.getStrategy());
        analysisAbstract.analysisList(id);
    }

    @Override
    public void updateBookInfo(Integer listId) {
        BookList byId = bookListService.getById(listId);
        BookName bookName = baseMapper.selectById(byId.getBookId());
        Channel channel = channelMapper.selectById(bookName.getChannelId());
        AnalysisAbstract analysisAbstract = map.get(channel.getStrategy());
        analysisAbstract.analysisInfo(listId.toString());
    }

    @Override
    @Async
    public void down(Integer id) {
        BookName bookName = baseMapper.selectById(id);

        List<BookList> list = bookListService.list(Wrappers.<BookList>lambdaQuery()
                .eq(BookList::getBookId, id).isNull(BookList::getListInfo));
        if(StringUtils.isNotBlank(bookName.getDownUrl()) && list.size() == 0) {
            return;
        }
        baseMapper.updateById(BookName.builder().id(id).downStatus(1).build());
        for(BookList bookList : list) {
            updateBookInfo(bookList.getId());
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // todo 后续可以改成往文件里追加的方式
        List<BookList> list1 = bookListService.list(Wrappers.<BookList>lambdaQuery().eq(BookList::getBookId, id));
        File file = new File("/hyy/static/book/"+bookName.getName() + ".txt");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            for(BookList bookList : list1) {
                fileOutputStream.write(("\n" + bookList.getListName() + "\n").getBytes());
                fileOutputStream.write(bookList.getListInfo()
                        .replace("<div id=\"center_tip\">","")
                        .replace("<div id=\"content\">","")
                        .replace("&nbsp;&nbsp;&nbsp;&nbsp;","")
                        .replace("<b>最新网址：www.mayiwxw.com</b>","")
                        .replace("<br>","")
                        .replace("</div>","")
                        .getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        baseMapper.updateById(BookName.builder().id(id)
                .downUrl("http://124.222.253.95/static/book/" +bookName.getName() +".txt")
                .downNewList(list1.get(list1.size()-1).getId()).downStatus(2).build());
    }
}
