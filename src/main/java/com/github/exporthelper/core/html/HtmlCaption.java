package com.github.exporthelper.core.html;

public class HtmlCaption extends HtmlTagWithContent {

	private String title;
	
	public HtmlCaption(){
	}

	public String getTitle(){
		return title;
	}

	public void setTitle(String title){
		this.title = title;
	}

}