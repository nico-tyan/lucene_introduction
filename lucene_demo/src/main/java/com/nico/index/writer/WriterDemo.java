package com.nico.index.writer;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
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

	@Test
	public void run() throws Exception {
		System.out.println(11111);
	}

	/**
	 * 
	 * @Title: 创建索引 @Description: @date 2018年6月21日 @throws Exception @throws
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
				// title分词
				doc.add(new TextField("title", TITLES[i], Field.Store.YES));
				// contents分词
				doc.add(new TextField("content", CONTENTS[i], Field.Store.YES));
				// contents_no_save分词不保存
				doc.add(new TextField("contents_no_save", CONTENTS_NO_SAVE[i], Field.Store.NO));
				// 开始创建索引
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
}
