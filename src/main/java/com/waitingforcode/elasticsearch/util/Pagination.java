package com.waitingforcode.elasticsearch.util;

import com.google.common.base.MoreObjects;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

public class Pagination {

    private static final int DEFAULT_PAGE = 1;

    private static final int DEFAULT_PER_PAGE = 100;

    @QueryParam("page")
    @DefaultValue(""+DEFAULT_PAGE)
    private int page = DEFAULT_PAGE;

    @QueryParam("perPage")
    @DefaultValue(""+DEFAULT_PER_PAGE)
    private int perPage = DEFAULT_PER_PAGE;

    public Pagination() {
        this.page = DEFAULT_PAGE;
        this.perPage = DEFAULT_PER_PAGE;
    }

    public Pagination(int page, int perPage) {
        this.page = page;
        this.perPage = perPage;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPerPage() {
        return perPage;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }

    public int getFrom() {
        return page-1;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("page", page).add("perPage", perPage).toString();
    }

}
