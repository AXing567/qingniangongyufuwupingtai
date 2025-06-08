
package com.utils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.plugins.Page;

/**
 * 分页工具类
 */
public class PageUtils implements Serializable {
	private static final long serialVersionUID = 1L;
	//总记录数
	private long total;
	//每页记录数
	private int pageSize;
	//总页数
	private long totalPage;
	//当前页数
	private int currPage;
	//列表数据
	private List<?> list;

	/**
	 * 分页
	 * @param list        列表数据
	 * @param totalCount  总记录数
	 * @param pageSize    每页记录数
	 * @param currPage    当前页数
	 */
	public PageUtils(List<?> list, int totalCount, int pageSize, int currPage) {
		this.list = list;
		this.total = totalCount;
		this.pageSize = pageSize;
		this.currPage = currPage;
		this.totalPage = (int)Math.ceil((double)totalCount/pageSize);
	}

	/**
	 * 构造分页工具类实例
	 *
	 * @param page 分页对象，包含分页所需的信息如当前页、总记录数、页面大小等
	 */
	public PageUtils(Page<?> page) {
	    // 设置当前页的记录列表
	    this.list = page.getRecords();
	    // 设置总记录数
	    this.total = page.getTotal();
	    // 设置每页记录数
	    this.pageSize = page.getSize();
	    // 设置当前页码
	    this.currPage = page.getCurrent();
	    // 设置总页数
	    this.totalPage = page.getPages();
	}

	/*
	 * 空数据的分页
	 */
	public PageUtils(Map<String, Object> params) {
 		Page page =new Query(params).getPage();
		new PageUtils(page);
	}


	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getCurrPage() {
		return currPage;
	}

	public void setCurrPage(int currPage) {
		this.currPage = currPage;
	}

	public List<?> getList() {
		return list;
	}

	public void setList(List<?> list) {
		this.list = list;
	}

	public long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(long totalPage) {
		this.totalPage = totalPage;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

}
