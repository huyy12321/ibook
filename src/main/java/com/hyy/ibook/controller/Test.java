package com.hyy.ibook.controller;



import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hyy.ibook.entity.Channel;
import com.hyy.ibook.vo.BookVO;
import com.hyy.ibook.entity.BookList;
import com.hyy.ibook.entity.BookName;
import com.hyy.ibook.vo.BookListVO;
import com.hyy.ibook.service.BookListService;
import com.hyy.ibook.service.BookNameService;
import com.hyy.ibook.service.ChannelService;
import com.hyy.ibook.service.KeywordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.List;

/**
 * @author huyangyang
 */
@RestController
@RequestMapping("/test")
@CrossOrigin
public class Test {
    @Resource
    private BookNameService bookNameService;
    @Resource
    private BookListService bookListService;
    @Resource
    private KeywordService keywordService;
    @Resource
    private ChannelService channelService;

    @RequestMapping("/book")
    public R<List<BookName>> book(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false,defaultValue = "1") Integer channelId,
                                  @RequestParam Integer page,
                                  @RequestParam Integer limit){
        if(StringUtils.isNotBlank(keyword)) {
            bookNameService.updateBookName(keyword,channelId);
        }
        keywordService.addKeyword(keyword);

        IPage<BookName> rs = bookNameService.page(new Page<>(page, limit), Wrappers.<BookName>lambdaQuery()
                .like(StringUtils.isNotBlank(keyword),BookName::getName, keyword)
                .eq(BookName::getChannelId,channelId)
                .orderByDesc(BookName::getHeat)
                .orderByDesc(BookName::getUpdateTime));
        if(rs.getTotal() == 0) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rs = bookNameService.page(new Page<>(page, limit), Wrappers.<BookName>lambdaQuery()
                    .like(StringUtils.isNotBlank(keyword),BookName::getName, keyword)
                    .eq(BookName::getChannelId,channelId)
                    .orderByDesc(BookName::getHeat)
                    .orderByDesc(BookName::getUpdateTime));
        }
        return R.ok(rs.getRecords());
    }


    @GetMapping("/bookList/{bookId}")
    public R<BookVO> bookList(@PathVariable String bookId){
        BookName byId = bookNameService.getById(bookId);
        List<BookListVO> list = bookListService.listvo(Integer.valueOf(bookId));
        bookNameService.updateBookList(bookId);
        byId.setHeat(byId.getHeat() + 1);
        bookNameService.updateById(byId);
        return R.ok(BookVO.builder().bookLists(list).bookName(byId).build());
    }

    @GetMapping("/info/{listId}")
    public R<List<BookList>> info(@PathVariable Integer listId){
        bookNameService.updateBookInfo(listId);
        BookList byId = bookListService.getById(listId);
        IPage<BookList> page = bookListService.page(new Page<>(1, 2), Wrappers.<BookList>lambdaQuery().
                eq(BookList::getBookId, byId.getBookId()).ge(BookList::getId, listId));

        return R.ok(page.getRecords());
    }

    @GetMapping("/down/{bookId}")
    public R<String> down(@PathVariable Integer bookId) {
        BookName byId = bookNameService.getById(bookId);
        switch (byId.getDownStatus()) {
            case 0:
                bookNameService.down(bookId);
                return R.failed("首次下载，需要一段时间整合数据，请一段时间后再来尝试");
            case 1:
                List<BookList> list = bookListService.list(Wrappers.<BookList>lambdaQuery()
                        .eq(BookList::getBookId, bookId));
                int total = list.size();
                int size = (int) list.stream().filter(t -> StringUtils.isNotBlank(t.getListInfo())).count();
                return R.failed("正文获取中:"+size+"/"+total+"，请稍后在尝试下载");
            case 2:
                bookNameService.down(bookId);
                return R.ok(byId.getDownUrl());
            default:
                return R.failed("发生异常");
        }
    }

    @RequestMapping("/down1")
    public void down1() {
        List<BookName> list = bookNameService.list(Wrappers.<BookName>lambdaQuery().isNull(BookName::getInfo));
        for(BookName bookName : list) {
            bookNameService.updateBookList(bookName.getId().toString());
        }
    }

    @GetMapping("/channel")
    public R<List<Channel>> channel() {
        return R.ok(channelService.list());
    }

    @GetMapping("/update")
    public void update() {
        List<BookList> list = bookListService.list(Wrappers.<BookList>lambdaQuery().eq(BookList::getBookId, 66).isNotNull(BookList::getListInfo));
        list.forEach(t->{
            t.setListInfo(t.getListInfo().replace("<p>", "&nbsp;&nbsp;&nbsp;&nbsp;").replace("</p>", "<br>"));
        });
        bookListService.updateBatchById(list);
    }
}
