package com.nico.index.seacher;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
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
public class SeacherDemo extends TestData {
	/**
	 * 缓存Directory
	 */
	private static Map<String, Directory> directorys = new HashMap<String, Directory>();
	/**
	 * 缓存indexWriters
	 */
	private static Map<String, IndexWriter> indexWriters = new HashMap<String, IndexWriter>();
	/**
	 * 缓存indexReaders
	 */
	private static Map<String, IndexReader> indexReaders = new HashMap<String, IndexReader>();
	/**
	 * 解析器
	 */
	private static Analyzer analyzer = new HanLPAnalyzer();

	/**
	 * 
	 * @Title: 获取searcher @Description: 多次OPEN indexReader
	 *         会有比较大的开销，一般重用indexReader @date 2018年6月21日 @return @throws
	 */
	public static IndexSearcher getIndexSearcher(String path) {
		IndexReader indexReader = null;
		if (indexReaders.get(path) != null) {
			// 如果存在就重用
			indexReader = LuceneUtil.getIndexReader(indexReaders.get(path));
		} else {
			Directory openDirectory = null;

			if (directorys.get(path) != null) {
				openDirectory = directorys.get(path);

			} else {
				openDirectory = LuceneUtil.openDirectory(path);
				directorys.put(path, openDirectory);
			}
			// 如果不存在就新建
			indexReader = LuceneUtil.getIndexReader(openDirectory, analyzer);
			indexReaders.put(path, indexReader);
		}

		return new IndexSearcher(indexReader);
	}

	@Test
	public void seacherId() throws Exception {
		IndexSearcher indexSearcher = getIndexSearcher(INDEX_PATH);
		QueryParser QueryParser = new QueryParser("id", analyzer);
		Query parse = QueryParser.parse("1");
		TopDocs search = indexSearcher.search(parse, 10);
		System.out.println(search.totalHits);
		for (ScoreDoc scoreDoc : search.scoreDocs) {
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println(doc.get("id"));
			System.out.println(doc.get("name"));
			System.out.println(doc.get("title"));
			System.out.println(doc.get("content"));
			System.out.println(doc.get("contents_no_save"));
			System.out.println(doc.get("contents_string"));
		}

	}

	@Test
	public void seacherContent() throws Exception {
		IndexSearcher indexSearcher = getIndexSearcher(INDEX_PATH);
		QueryParser QueryParser = new QueryParser("content", analyzer);
		Query parse = QueryParser.parse("长江司令");
		TopDocs search = indexSearcher.search(parse, 10);
		System.out.println(search.totalHits);
		for (ScoreDoc scoreDoc : search.scoreDocs) {
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println(doc.get("id"));
			System.out.println(doc.get("name"));
			System.out.println(doc.get("title"));
			System.out.println(doc.get("content"));
			System.out.println(doc.get("contents_no_save"));
		}

	}

	@Test
	public void seacherName() throws Exception {
		IndexSearcher indexSearcher = getIndexSearcher(INDEX_PATH);
		QueryParser QueryParser = new QueryParser("name", analyzer);
		Query parse = QueryParser.parse("江西省");
		// Query parse = QueryParser.parse("江西");
		TopDocs search = indexSearcher.search(parse, 10);
		System.out.println(search.totalHits);
		for (ScoreDoc scoreDoc : search.scoreDocs) {
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println(doc.get("id"));
			System.out.println(doc.get("name"));
			System.out.println(doc.get("title"));
			System.out.println(doc.get("content"));
			System.out.println(doc.get("contents_no_save"));
		}

	}

	@Test
	public void seacherTitle() throws Exception {
		IndexSearcher indexSearcher = getIndexSearcher(INDEX_PATH);
		QueryParser QueryParser = new QueryParser("content", analyzer);
		Query parse = QueryParser.parse("广东在先秦时已存在新石器时代和青铜时代高度文明，是中华文明的发源地之一");
		// Query parse = QueryParser.parse("江西");
		TopDocs search = indexSearcher.search(parse, 10);
		System.out.println("总数:" + search.totalHits);
		for (ScoreDoc scoreDoc : search.scoreDocs) {
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println("分数:" + scoreDoc.score);
			System.out.println(doc.get("id"));
			System.out.println(doc.get("name"));
			System.out.println(doc.get("title"));
			System.out.println("content:" + doc.get("content"));
			System.out.println(doc.get("contents_no_save"));
		}
	}

	@Test
	public void seacherLike() throws Exception {
		long time=System.currentTimeMillis();
		Calendar nowCalendar = Calendar.getInstance();
		nowCalendar.setTimeInMillis(time-1000L*60*60*24*1);
		
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH").format(nowCalendar.getTime()));
		
		
		IndexSearcher indexSearcher = getIndexSearcher(INDEX_PATH);
		IndexReader indexReader = LuceneUtil.getIndexReader(indexReaders.get(INDEX_PATH));
		int refDocId = 1;
		Document refDoc = indexSearcher.doc(refDocId);
		System.out.println("关联文档: 【" + refDoc.get("title") + "】"  );
		System.out.println("关联文档: 【" + refDoc.get("content") + "】" );
		MoreLikeThis mlt = new MoreLikeThis(indexReader);
		//这里的频率指的是用户搜索的频率，如热词  热文档，就是经常访问的
		// 默认值是2，建议自己做限制，否则可能查不出结果--
		mlt.setMinTermFreq(0);//最少的词频
		// 默认值是5，建议自己做限制，否则可能查不出结果
		mlt.setMinDocFreq(0);//最小的文档频率
		mlt.setAnalyzer(analyzer);
		//设定比对的字段
		mlt.setFieldNames(new String[] { "title","content" }); // 用于计算的字段
		int docNum = refDocId;
		Query query = mlt.like(docNum);// 试图找到与docnum=1相似的documents
		System.out.println("doc: "+indexReader.document(docNum));
		System.out.println("query_sql: "+query.toString());// 查看构造的query，后面的就是常规的lucene的检索过程。
		TopDocs topDocs = indexSearcher.search(query, 10);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		System.out.println("hits: "+scoreDocs.length);
		for (ScoreDoc sdoc : scoreDocs) {
			Document doc = indexReader.document(sdoc.doc);
			System.out.println("分数:"+sdoc.score+"\t TITLE:"+doc.get("title")+"\t ID:"+sdoc.doc );
			System.out.println();
		}
	}

}
