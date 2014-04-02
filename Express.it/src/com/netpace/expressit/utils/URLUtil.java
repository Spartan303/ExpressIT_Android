package com.netpace.expressit.utils;

import com.netpace.expressit.constants.AppConstants;

public class URLUtil {

	public static String getURI(String url){
		return AppConstants.DOMAIN_URL+url;
	}
	
	public static String getURIWithPageNoAndSize(String url, Integer pageNo, Integer size){
		return AppConstants.DOMAIN_URL+url+"?page="+pageNo+"&size="+size;
	}
}
