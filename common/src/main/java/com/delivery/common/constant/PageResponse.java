package com.delivery.common.constant;

import lombok.Getter;

import java.util.List;

@Getter
public class PageResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
}
