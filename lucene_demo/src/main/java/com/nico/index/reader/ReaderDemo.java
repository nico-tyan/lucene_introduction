package com.nico.index.reader;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.junit.Test;

import com.hankcs.lucene.HanLPAnalyzer;
import com.nico.index.TestData;
import com.nico.util.LuceneUtil;

/**
 * 
 * @Title: lucene reader demo
 * @Package com.nico.index.reader  
 * @Description: 
 * @author fangshu
 * @date 2018年6月21日  
 * @version
 */
public class ReaderDemo extends TestData{
	
	/**
	 * 根据索引读DOC--一般不使用
	 * @Title: 
	 * @Description: 
	 * @date 2018年6月21日  
	 * @throws Exception        
	 * @throws
	 */
	@Test
	public void reader() throws Exception{
		Analyzer analyzer=new HanLPAnalyzer();
		IndexReader indexReader = LuceneUtil.getIndexReader(INDEX_PATH, analyzer);
		Document document = indexReader.document(2);
		System.out.println(document.get("id"));
		System.out.println(document.get("name"));
		System.out.println(document.get("title"));
		System.out.println(document.get("content"));
		System.out.println(document.get("contents_no_save"));
		indexReader.close();
	}
}
