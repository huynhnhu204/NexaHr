package com.nexahr.service;

import com.nexahr.dto.response.SearchResultResponse;

import java.util.List;

public interface SearchService {
    List<SearchResultResponse> globalSearch(String query);
}
