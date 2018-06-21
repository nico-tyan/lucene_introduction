package com.nico.index.reader;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
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
	
	@Test
	public void reader() throws Exception{
		Analyzer analyzer=new HanLPAnalyzer();
		IndexReader indexReader = LuceneUtil.getIndexReader(INDEX_PATH, analyzer);
		
		
		indexReader.close();
	}
}
