package com.hyy.ibook.controller;



import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hyy.ibook.VO.BookVO;
import com.hyy.ibook.Entity.BookList;
import com.hyy.ibook.Entity.BookName;
import com.hyy.ibook.VO.BookListVO;
import com.hyy.ibook.service.BookListService;
import com.hyy.ibook.service.BookNameService;
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
                like(StringUtils.isNotBlank(keyword),BookName::getName, keyword).
                orderByDesc(BookName::getHeat));
        if(StringUtils.isNotBlank(keyword)) {
            bookNameService.updateBookName(keyword);
        }
        return R.ok(rs.getRecords(),rs.getTotal());
    }


    @RequestMapping("/bookList")
    public R<BookVO> bookList(@RequestParam String id){
        BookName byId = bookNameService.getById(id);
        List<BookListVO> list = bookListService.listvo(Integer.valueOf(id));
        bookNameService.updateBookList(id);
        byId.setHeat(byId.getHeat() + 1);
        bookNameService.updateById(byId);
        return R.ok(BookVO.builder().bookLists(list).bookName(byId).build());
    }

    @RequestMapping("/info")
    public R<List<BookList>> info(@RequestParam String id,
                                  @RequestParam Integer listId){
        bookNameService.updateBookInfo(id,listId);
        IPage<BookList> page = bookListService.page(new Page<>(1, 2), Wrappers.<BookList>lambdaQuery().
                eq(BookList::getBookId, id).ge(BookList::getId, listId));

        return R.ok(page.getRecords());
    }

    @RequestMapping("/down/{id}")
    public R<String> down(@PathVariable Integer id) {
        BookName byId = bookNameService.getById(id);
        switch (byId.getDownStatus()) {
            case 0:
                bookNameService.down(id);
                return R.fail("首次下载，需要一段时间整合数据，请一段时间后再来尝试");
            case 1:
                return R.fail("正文获取中，请稍后在尝试下载");
            case 2:
                bookNameService.down(id);
                return R.ok(byId.getDownUrl());
            default:
                return R.fail("发生异常");
        }
    }
}
