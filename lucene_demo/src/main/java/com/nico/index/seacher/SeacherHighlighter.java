package com.nico.index.seacher;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import com.hankcs.lucene.HanLPAnalyzer;
import com.nico.index.TestData;
import com.nico.util.LuceneUtil;

/**
 * 
 * @Title: lucene seacher demo
 * @Package com.nico.index.seacher  
 * @Description: 
 * @author fangshu  
 * @date 2018年6月21日  
 * @version
 */
public class SeacherHighlighter extends TestData {
	/**
	 * 缓存Directory
	 */
	private static Map<String,Directory> directorys=new HashMap<String, Directory>();
	/**
	 * 缓存indexWriters
	 */
	private static  Map<String,IndexWriter> indexWriters=new HashMap<String, IndexWriter>();
	/**
	 * 缓存indexReaders
	 */
	private static  Map<String,IndexReader> indexReaders=new HashMap<String, IndexReader>();
	/**
	 * 解析器
	 */
	private static Analyzer analyzer= new HanLPAnalyzer();
	
	
	/**
	 * 
	 * @Title: 获取searcher
	 * @Description: 
	 * 多次OPEN indexReader 会有比较大的开销，一般重用indexReader
	 * @date 2018年6月21日  
	 * @return        
	 * @throws
	 */
	public static IndexSearcher getIndexSearcher(String path){
		IndexReader indexReader=null;
		if(indexReaders.get(path)!=null){
			 //如果存在就重用
			 indexReader = LuceneUtil.getIndexReader(indexReaders.get(path));
		}else{
			Directory openDirectory=null;
			
			if(directorys.get(path)!=null){
				openDirectory=directorys.get(path);
				
			}else{
				openDirectory = LuceneUtil.openDirectory(path);
				directorys.put(path, openDirectory);
			}
			//如果不存在就新建
			indexReader=LuceneUtil.getIndexReader(openDirectory, analyzer);
			indexReaders.put(path, indexReader);
		}
		
		return new IndexSearcher(indexReader);
	}
	
	public static void CloseIndexReader(String path){
		IndexReader indexReader=null;
		if(indexReaders.get(path)!=null){
			 //如果存在就重用
			 indexReader = indexReaders.get(path);
			 try {
				indexReader.close();
			} catch (IOException e) {
				System.out.println("IndexReader关闭失败!");
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public  void search()throws Exception{
		
		IndexSearcher is=getIndexSearcher(INDEX_PATH);
				
		// Analyzer analyzer=new StandardAnalyzer(); // 标准分词器
		//SmartChineseAnalyzer analyzer=new SmartChineseAnalyzer();//智能中文分词器
		String query_str="广东";
		QueryParser parser=new QueryParser("content", analyzer);
		Query query=parser.parse(query_str);
		long start=System.currentTimeMillis();
		TopDocs hits=is.search(query, 10);
		long end=System.currentTimeMillis();
		System.out.println("匹配 "+query_str+" ，总共花费"+(end-start)+"毫秒"+"查询到"+hits.totalHits+"个记录");
		
		QueryScorer scorer=new QueryScorer(query);
		Fragmenter fragmenter=new SimpleSpanFragmenter(scorer);
		SimpleHTMLFormatter simpleHTMLFormatter=new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
		Highlighter highlighter=new Highlighter(simpleHTMLFormatter, scorer);
		highlighter.setTextFragmenter(fragmenter);
		for(ScoreDoc scoreDoc:hits.scoreDocs){
			Document doc=is.doc(scoreDoc.doc);
			System.out.println(doc.get("name"));
			String content=doc.get("content");
			if(content!=null){
				TokenStream tokenStream=analyzer.tokenStream("content", new StringReader(content));
				System.out.println(highlighter.getBestFragment(tokenStream, content));
			}
		}
		
		CloseIndexReader(INDEX_PATH);
		
	}
	
	
	
	
}
