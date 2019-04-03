package com.nico.index.writer;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

import com.hankcs.lucene.HanLPAnalyzer;
import com.nico.index.TestData;
import com.nico.util.LuceneUtil;

/**
 * 
 * @Title: lucene writer demo
 * @Package com.nico.index.seacher
 * @Description:
 * @author fangshu
 * @date 2018年6月21日
 * @version
 */
public class WriterDemo extends TestData {


	/**
	 * 
	 * @Title: 创建索引 
	 * @Description: 
	 * @date 2018年6月21日 
	 * @throws Exception 
	 * @throws
	 */
	@Test
	public void index() {
		//
		Analyzer analyzer = new HanLPAnalyzer();
		IndexWriter indexWriter = LuceneUtil.getIndexWriter(INDEX_PATH, analyzer);
		//
		//IndexReader indexReader = LuceneUtil.getIndexReader(INDEX_PATH, analyzer);
		//
		//IndexSearcher indexSearcher = LuceneUtil.getIndexSearcher(indexReader);
		//
		try {
			for (int i = 0; i < IDS.length; i++) {
				Document doc = new Document();
				// doc.add(new StringField("id", IDS[i],Field.Store.YES));
				// id用来排序
				doc.add(new StringField("id", IDS[i], Field.Store.YES));
				doc.add(new IntField("id", Integer.parseInt(IDS[i]), Field.Store.YES));
				// name不分词
				doc.add(new StringField("name", NAMES[i], Field.Store.YES));
				// group不分词
				doc.add(new Field("group", GROUPS[i],StringField.TYPE_STORED));
				// group分组      SortedDocValuesField
				doc.add(new SortedDocValuesField("group2", new BytesRef(GROUPS[i])));
				
				doc.add(new SortedDocValuesField("group3", new BytesRef(GROUPS_INDEX[i])));
				// title不分词
				doc.add(new Field("title2", TITLES[i],StringField.TYPE_STORED));
				// title分词
				doc.add(new TextField("title", TITLES[i], Field.Store.YES));
				// contents分词--并保存原文内容
				doc.add(new TextField("content", CONTENTS[i], Field.Store.YES));
				// contents_no_save分词不保存--不保存原文内容(只能检索不能展示)
				doc.add(new TextField("contents_no_save", CONTENTS_NO_SAVE[i], Field.Store.NO));
				// contents不分词
				doc.add(new Field("contents_string", CONTENTS[i],StringField.TYPE_STORED));
//				{
//					// 第一种方案，先查后判断
//					QueryParser queryParser = new QueryParser("id", analyzer);
//					Query parse = queryParser.parse(IDS[i]);
//					TopDocs search = indexSearcher.search(parse, 1);
//					// 如果ID存在，则更新，否则新建
//					if (search.totalHits > 0) {
//						indexWriter.updateDocument(new Term("id", IDS[i]), doc);
//					} else {
//						indexWriter.addDocument(doc);
//					}
//				}

				{
					// 第二种方案
					// 实际indexWriter.updateDocument，lucene的做法是如果存在就更新，不存在就新增
					
					indexWriter.updateDocument(new Term("id", IDS[i]), doc);

				}
			}
			//必须提交，否则会造成lock异常
			indexWriter.commit();
			System.out.println("max doc :"+indexWriter.maxDoc());
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			//必须关闭，否则会造成内存泄漏---关闭indexWriter,则Directory也关闭了
			//测试indexWriter.getDirectory().close(); 执行完毕后，indexWriter.close();失败
			try {
				indexWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
//			try {
//				indexReader.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}

	}
	
	/**
	 * 
	 * @Title: 标记删除--文件还在
	 * @Description: 
	 * @date 2018年6月21日  
	 * @throws IOException        
	 * @throws
	 */
	@Test
	public void deleteDocsByIdent() throws IOException{
		Analyzer analyzer = new HanLPAnalyzer();
		IndexWriter indexWriter = LuceneUtil.getIndexWriter(INDEX_PATH, analyzer);
		indexWriter.deleteDocuments(new Term("id", "2"));
		indexWriter.commit();
		indexWriter.close();
	}
	
	/**
	 * 
	 * @Title: 强制删除--现将文件进行标记删除，然后再forceMergeDeletes一次性删除
	 * @Description: 
	 * @date 2018年6月21日  
	 * @throws IOException        
	 * @throws
	 */
	@Test
	public void deleteDocsByForce() throws IOException{
		Analyzer analyzer = new HanLPAnalyzer();
		IndexWriter indexWriter = LuceneUtil.getIndexWriter(INDEX_PATH, analyzer);
		indexWriter.deleteDocuments(new Term("id", "3"));
		indexWriter.forceMergeDeletes(false);
		indexWriter.commit();
		indexWriter.close();
	}
	
	
	/**
	 * @throws IOException 
	 * 
	 * @Title: 
	 * @Description: 
	 * @date 2018年6月21日          
	 * @throws
	 */
	@Test
	public void docNumbers() throws IOException{
		//
		Analyzer analyzer = new HanLPAnalyzer();
		IndexWriter indexWriter = LuceneUtil.getIndexWriter(INDEX_PATH, analyzer);
		
		System.out.println("DOC 总数："+indexWriter.maxDoc());
		System.out.println("DOC 实际数："+indexWriter.numDocs());
		
		indexWriter.close();
		
	}
	
}
