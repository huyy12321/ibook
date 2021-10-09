package com.hyy.ibook.controller;



import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hyy.ibook.Entity.BookList;
import com.hyy.ibook.Entity.BookName;
import com.hyy.ibook.service.BookListService;
import com.hyy.ibook.service.BookNameService;
import com.hyy.ibook.service.ChannelService;
import com.hyy.ibook.service.SearchKeyService;
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

    @RequestMapping("/book")
    public R<List<BookName>> book(@RequestParam(required = false) String keyword,
                                  @RequestParam Integer page,
                                  @RequestParam Integer limit){
        IPage<BookName> rs = bookNameService.page(new Page<>(page, limit), Wrappers.<BookName>lambdaQuery().
                like(StringUtils.isNotBlank(keyword),BookName::getName, keyword));
        bookNameService.updateBookName(keyword);
        return R.ok(rs.getRecords(),rs.getTotal());
    }


    @RequestMapping("/info")
    public R<List<BookList>> info(@RequestParam String id,
                                  @RequestParam Integer page,
                                  @RequestParam Integer limit){

        IPage<BookList> rs = bookListService.page(new Page<>(page, limit),
                Wrappers.<BookList>lambdaQuery().eq(BookList::getBookId, id));
        bookNameService.updateBookList(id);

        return R.ok(rs.getRecords(),rs.getTotal());
    }

    @RequestMapping("/info1")
    public R<List<BookList>> info1(@RequestParam String id,
                                   @RequestParam Integer listId){
        bookNameService.updateBookInfo(id);
        IPage<BookList> page = bookListService.page(new Page<>(1, 2), Wrappers.<BookList>lambdaQuery().
                eq(BookList::getBookId, id).ge(BookList::getId, listId));

        return R.ok(page.getRecords());
    }

}
