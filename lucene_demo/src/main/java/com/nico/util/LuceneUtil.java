package com.nico.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * 
 * @Title: lucene工具类
 * @Package com.nico.util  
 * @Description: 
 * @author fangshu  
 * @date 2018年6月21日  
 * @version
 */
public class LuceneUtil {
	
	/**
	 * 
	 * @Title: 获取索引目录
	 * @Description: 
	 * @date 2018年6月21日  
	 * @param path
	 * @return        
	 * @throws
	 */
	public static Directory openDirectory(String path){
		Directory directory=null;
		try {
			directory = FSDirectory.open(Paths.get(path));
		} catch (IOException e) {
			System.out.println("开启directory失败!");
			e.printStackTrace();
		}
		return directory;
	}
	
	/**
	 * @Title: 获取IndexWriter
	 * @Description: 
	 * @date 2018年6月21日  
	 * @param directory
	 * @param analyzer
	 * @return        
	 * @throws
	 */
	public static IndexWriter getIndexWriter(Directory directory,Analyzer analyzer){
		
		IndexWriter iw=null;
		try {
			iw = new IndexWriter(directory, new IndexWriterConfig(analyzer));
		} catch (IOException e) {
			System.out.println("获取IndexWriter失败!");
			e.printStackTrace();
		}
		return iw;
	}
	
	/**
	 * @Title: 获取IndexWriter
	 * @Description: 
	 * @date 2018年6月21日  
	 * @param path
	 * @param analyzer
	 * @return        
	 * @throws
	 */
	public static IndexWriter getIndexWriter(String path,Analyzer analyzer){
		IndexWriter iw=getIndexWriter(openDirectory(path), analyzer);
		return iw;
	}
	
	/**
	 * 
	 * @Title: 获取IndexReader
	 * @Description: 
	 * @date 2018年6月21日  
	 * @param path
	 * @param analyzer
	 * @return        
	 * @throws
	 */
	public static IndexReader getIndexReader(String path,Analyzer analyzer){
		IndexReader ir=getIndexReader(openDirectory(path), analyzer);
		return ir;
	}
	
	/**
	 * 
	 * @Title: 获取IndexReader
	 * @Description: 
	 * @date 2018年6月21日  
	 * @param path
	 * @param analyzer
	 * @return        
	 * @throws
	 */
	public static IndexReader getIndexReader(Directory directory,Analyzer analyzer){
		IndexReader ir=null;
		try {
			ir = DirectoryReader.open(directory);
		} catch (IOException e) {
			System.out.println("获取IndexWriter失败!");
			e.printStackTrace();
		}
		return ir;
	}
	
	/**
	 * 
	 * @Title: 重用一些旧的IndexReader
	 * @Description: 
	 * @date 2018年6月21日  
	 * @param path
	 * @param analyzer
	 * @return        
	 * @throws
	 */
	public static IndexReader getIndexReader(IndexReader indexReader){
		if(indexReader!=null){
			try {
				return DirectoryReader.openIfChanged((DirectoryReader)indexReader);
			} catch (IOException e) {
				System.out.println("获取indexReader失败!");
				e.printStackTrace();
			}
		}
		System.out.println("indexReader不能为空!");
		return null;
	}
	
	/**
	 * 
	 * @Title: 获取IndexSearcher
	 * @Description: 
	 * @date 2018年6月21日  
	 * @param indexReader
	 * @return        
	 * @throws
	 */
	public static IndexSearcher getIndexSearcher(IndexReader indexReader){
		IndexSearcher is=new IndexSearcher(indexReader);
		return is;
	}
	
}
