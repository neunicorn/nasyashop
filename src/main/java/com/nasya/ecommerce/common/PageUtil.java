package com.nasya.ecommerce.common;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class PageUtil {

    public static List<Sort.Order> parseSortOrderRequest(String[] sort ){
        List<Sort.Order> orders = new ArrayList<>();
        if(sort[0].contains(",")){
            for(String sortOrder:sort){
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(getSortDirection(_sort[1]),_sort[0]));
            }
        }else {
            orders.add(new Sort.Order(getSortDirection(sort[1]),sort[0]));
        }
        return orders;
    }

    private static Sort.Direction getSortDirection(String direction){
        if(direction.equals("asc")){
            return Sort.Direction.ASC;
        } else if(direction.equals("desc")){
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }
}
